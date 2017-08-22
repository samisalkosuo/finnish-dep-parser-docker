package is2.mtag;


import is2.data.Cluster;
import is2.data.FV;
import is2.data.Instances;
import is2.data.InstancesTagger;
import is2.data.Long2Int;
import is2.data.ParametersFloat;
import is2.data.PipeGen;
import is2.data.SentenceData09;
import is2.io.CONLLReader09;
import is2.io.CONLLWriter09;
import is2.tools.IPipe;
import is2.tools.Train;
import is2.tools.Tool;
import is2.util.DB;
import is2.util.OptionsSuper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class Tagger implements Tool, Train {

	ExtractorM pipe;
	ParametersFloat params;


	/**
	 * Initialize 
	 * @param options
	 */
	public Tagger (Options options) {
			
		// load the model
		try {
			readModel(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
				
	}
	
	/**
	 * @param string
	 * @throws IOException 
	 */
	public Tagger(String modelFileName) {
		this(new Options(new String[] {"-model",modelFileName}));
	}

	public Tagger() {	}

	public static void main (String[] args) throws FileNotFoundException, Exception
	{

		Options options = new Options(args);
		
		Tagger tagger = new Tagger();
		
		if (options.train) {

			Long2Int li = new Long2Int(options.hsize);
			tagger.pipe =  new ExtractorM (options,li);
	    	InstancesTagger is = (InstancesTagger)tagger.pipe.createInstances(options.trainfile);
			ParametersFloat params = new ParametersFloat(li.size());
			
			tagger.train(options, tagger.pipe,params,is);
			tagger.writeModel(options, tagger.pipe, params);
		}

		if (options.test) {
			
			tagger.readModel(options);
			tagger.out(options,tagger.pipe, tagger.params);
		}

		if (options.eval) {
			
			System.out.println("\nEvaluate:");
			Evaluator.evaluate(options.goldfile, options.outfile,options.format);
		}
	}

	/* (non-Javadoc)
	 * @see is2.mtag2.Learn#writeModel(is2.mtag2.Options, is2.mtag2.Pipe, is2.data.ParametersFloat)
	 */
	public void writeModel(OptionsSuper options, IPipe pipe,ParametersFloat params)  {
		
		try {
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(options.modelName)));
		zos.putNextEntry(new ZipEntry("data")); 
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(zos));
   
		MFO.writeData(dos);

		MFO.clearData();

		DB.println("number of parameters "+params.parameters.length);
		dos.flush();
		params.write(dos);
		pipe.write(dos);
		dos.flush();
		dos.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see is2.mtag2.Learn#readModel(is2.mtag2.Options)
	 */
	public void readModel(OptionsSuper options) {
		
		try {
		pipe = new ExtractorM(options);
		params = new ParametersFloat(0);

		// load the model
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(options.modelName)));
		zis.getNextEntry();
		DataInputStream dis = new DataInputStream(new BufferedInputStream(zis));
		pipe.mf.read(dis);
		pipe.initValues();
		pipe.initFeatures();
		
		params.read(dis);
		pipe.li = new Long2Int(params.parameters.length);
		pipe.cl = new Cluster(dis);
		pipe.readMap(dis);
		dis.close();

		this.pipe.types = new String[pipe.mf.getFeatureCounter().get(ExtractorM.FFEATS)];
		for(Entry<String,Integer> e :pipe.mf.getFeatureSet().get(ExtractorM.FFEATS).entrySet()) 
			this.pipe.types[e.getValue()] = e.getKey();

  
		DB.println("Loading data finished. "); 

		DB.println("number of parameter "+params.parameters.length);		
		DB.println("number of classes   "+this.pipe.types.length);		
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see is2.mtag2.Learn#train(is2.mtag2.Options, is2.mtag2.Pipe, is2.data.ParametersFloat, is2.data.InstancesTagger)
	 */
	public void train(OptionsSuper options, IPipe pipe, ParametersFloat params, Instances is)  {

		int i = 0;
		int del=0; 
		
		String[] wds = this.pipe.mf.reverse(this.pipe.mf.getFeatureSet().get(ExtractorM.WORD));
		int numInstances = is.size();

		float upd = (options.numIters*numInstances + 1);

		
		for(i = 0; i < options.numIters; i++) {

			long start = System.currentTimeMillis();
		
					
			long last= System.currentTimeMillis();
			
			FV pred = new FV(), gold = new FV();
			int correct =0,count=0;
			
			for(int n = 0; n < numInstances; n++) {

				upd--;

				if((n+1) % 500 == 0) del= PipeGen.outValueErr(n+1, (count-correct),(float)correct/(float)count,del,last,upd);
				
				int length = is.length(n);

				int feats[] = new int[length];
				long[] vs = new long[ExtractorM._FC]; 
					
				
				for(int w1 = 0; w1 < length; w1++) {
						

					count++;

					if (this.pipe.form2morph.get(is.forms[n][w1])!=null){
						correct++;
						continue;
					}
					
					int bestType = this.pipe.fillFeatureVectorsOne(params, w1, wds[is.forms[n][w1]],is, n, is.gfeats[n],vs);
					feats[w1]=bestType;

					
					if (bestType == is.gfeats[n][w1] ) {
						correct++;
						continue;  
					}

					pred.clear();
					int p = bestType << ExtractorM.s_type;
				//	System.out.println("test type "+bestType+" ex type "+ExtractorM.s_type);
					for(int k=0;k<vs.length;k++) {
						if (vs[k]==Integer.MIN_VALUE) break;
						if (vs[k]>=0) pred.add(this.pipe.li.l2i(vs[k]+p));
					}
					
					gold.clear();
					p = is.gfeats[n][w1] << ExtractorM.s_type;
					for(int k=0;k<vs.length;k++) {
						if (vs[k]==Integer.MIN_VALUE) break;
						if (vs[k]>=0) gold.add(this.pipe.li.l2i(vs[k]+p));
					}
					params.update(pred,gold, (float)upd, 1.0f);
				}

			}

			long end = System.currentTimeMillis();
			String info = "time "+(end-start);
			del= PipeGen.outValueErr(numInstances, (count-correct),(float)correct/(float)count,del,last,0,info);
	
			System.out.println();			
		}

		params.average(i*is.size());

	}

	
	public void out (OptionsSuper options, IPipe pipe, ParametersFloat params)  {

		
		try {
		long start = System.currentTimeMillis();

		CONLLReader09 depReader = new CONLLReader09(options.testfile, options.formatTask);
		CONLLWriter09 depWriter = new CONLLWriter09(options.outfile, options.formatTask);

		depReader.normalizeOn=false;

		System.out.print("Processing Sentence: ");
		pipe.initValues();
		
		int cnt = 0;
		int del=0;
		while(true) {

			InstancesTagger is = new InstancesTagger();
			is.init(1, this.pipe.mf);
			cnt++;

			SentenceData09 instance = depReader.getNext(is);
			if (instance == null || instance.forms == null)	 break;
			is.fillChars(instance, 0, ExtractorM._CEND);

			instance = exec(instance, this.pipe, params,(InstancesTagger)is);
						
			SentenceData09 i09 = new SentenceData09(instance);
			i09.createSemantic(instance);
		
			if (options.overwritegold)  i09.ofeats = i09.pfeats;
			
			depWriter.write(i09);
			
			if (cnt%100==0) del=PipeGen.outValue(cnt, del);

		}
		depWriter.finishWriting();

		del=PipeGen.outValue(cnt, del);
		
		long end = System.currentTimeMillis();
		System.out.println(PipeGen.getSecondsPerInstnace(cnt,(end-start)));
		System.out.println(PipeGen.getUsedTime((end-start)));
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	private SentenceData09 exec(SentenceData09 instance, ExtractorM pipe,  ParametersFloat params, InstancesTagger is) {
		
		int length = instance.ppos.length;
		
		short[] feats = new short[instance.gpos.length];
		
		long vs[] = new long[ExtractorM._FC];

		String[] forms = instance.forms;

		instance.pfeats = new String[instance.gpos.length];

		
		for(int j = 0; j < length; j++) {
			if (pipe.form2morph.get(is.forms[0][j])!=null) {
				feats[j] = (short)pipe.form2morph.get(is.forms[0][j]).intValue();
				instance.pfeats[j] = this.pipe.types[feats[j]];
			} else {

				int bestType = pipe.fillFeatureVectorsOne(params,j, forms[j], is, 0,feats,vs); 
				feats[j] =  (short)bestType;
				instance.pfeats[j]= this.pipe.types[bestType];
			}
		}
		for(int j = 0; j < length; j++) {
			if (pipe.form2morph.get(is.forms[0][j])!=null) {
				feats[j] =(short)pipe.form2morph.get(is.forms[0][j]).intValue();
				instance.pfeats[j] = this.pipe.types[feats[j]];
			} else {

				int bestType = pipe.fillFeatureVectorsOne(params,j, forms[j], is, 0,feats,vs); 
				feats[j] =  (short)bestType;
				instance.pfeats[j]= this.pipe.types[bestType];
			}
		}
		return instance;
	}
	
	

	/* (non-Javadoc)
	 * @see is2.tools.Tool#apply(is2.data.SentenceData09)
	 */
	@Override
	public SentenceData09 apply(SentenceData09 snt)   {
		
		try {			
			SentenceData09 it = new SentenceData09();
			it.createWithRoot(snt);
			
			InstancesTagger is = new InstancesTagger();
			is.init(1, pipe.mf);
			is.createInstance09(it.forms.length);
			
			String[] forms = it.forms;
		
			
			int length = forms.length;
			
			//	is.setForm(0, 0, CONLLReader09.ROOT);
			for(int i=0;i<length;i++) is.setForm(0, i, forms[i]);
			for(int i=0;i<length;i++) is.setLemma(0, i, it.plemmas[i]);
			for(int i=0;i<length;i++) is.setPPoss(0, i, it.ppos[i]);
			
			is.fillChars(it, 0, ExtractorM._CEND);

			exec(it,pipe,params,is);
			SentenceData09 i09 = new SentenceData09(it);
			i09.createSemantic(it);
			return i09;
		} catch(Exception e) {
			e.printStackTrace();
		}

		 return null;
	}
}
