package findep.is2;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.zip.ZipInputStream;

import findep.is2.io.CONLLReader09;
import findep.is2.io.CONLLWriter09;
import findep.utils.SystemOutLogger;
import is2.data.Cluster;
import is2.data.DataFES;
import is2.data.F2SF;
import is2.data.Instances;
import is2.data.Long2Int;
import is2.data.Long2IntInterface;
import is2.data.Parse;
import is2.data.PipeGen;
import is2.data.SentenceData09;
import is2.parser.Decoder;
import is2.parser.Edges;
import is2.parser.Extractor;
import is2.parser.MFO;
import is2.parser.Options;
import is2.parser.Parameters;
import is2.parser.ParametersFloat;
import is2.parser.Pipe;
import is2.tools.Tool;
import is2.util.DB;
import is2.util.OptionsSuper;

public class Parser implements Tool {

	// output evaluation info
	private static final boolean MAX_INFO = true;

	public static int THREADS = 4;

	public Long2IntInterface l2i;
	public ParametersFloat params;
	public Pipe pipe;
	public OptionsSuper options;

	// keep some of the parsing information for later evaluation
	public Instances is;
	DataFES d2;
	public Parse d = null;

	private String[] types;
	
	private SystemOutLogger SYSOUTLOGGER=SystemOutLogger.getInstance();

	/**
	 * Initialize the parser
	 * 
	 * @param options
	 */
	public Parser(OptionsSuper options) {

		this.options = options;
		Runtime runtime = Runtime.getRuntime();
		THREADS = runtime.availableProcessors();
		is2.parser.Parser.THREADS = THREADS;

		pipe = new Pipe(options);

		params = new ParametersFloat(0);

	}

	/**
	 * @param modelFileName
	 *            The file name of the parsing model
	 */
	public Parser(String modelFileName) {
		this(new Options(new String[] { "-model", modelFileName }));
	}

	public void loadModel() throws Exception {
		// load the model
		readModel(options, pipe, params);

	}

	public void parse(BufferedReader inputReader, BufferedWriter outputWriter) throws Exception {
		this.out(inputReader, outputWriter, options, this.pipe, this.params, !MAX_INFO, options.label);

	}

	/**
	 * Read the models and mapping
	 * 
	 * @param options
	 * @param pipe
	 * @param params
	 * @throws IOException
	 */
	public void readModel(OptionsSuper options, Pipe pipe, Parameters params) throws IOException {

		DB.println("Reading data started");

		// prepare zipped reader
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(options.modelName)));
		zis.getNextEntry();
		DataInputStream dis = new DataInputStream(new BufferedInputStream(zis));

		pipe.mf.read(dis);

		pipe.cl = new Cluster(dis);

		params.read(dis);
		this.l2i = new Long2Int(params.size());
		DB.println("parsing -- li size " + l2i.size());

		pipe.extractor = new Extractor[THREADS];

		boolean stack = dis.readBoolean();

		options.featureCreation = dis.readInt();

		for (int t = 0; t < THREADS; t++)
			pipe.extractor[t] = new Extractor(l2i, stack, options.featureCreation);
		DB.println("Stacking " + stack);

		Extractor.initFeatures();
		Extractor.initStat(options.featureCreation);

		for (int t = 0; t < THREADS; t++)
			pipe.extractor[t].init();

		Edges.read(dis);

		options.decodeProjective = dis.readBoolean();

		Extractor.maxForm = dis.readInt();

		boolean foundInfo = false;
		try {
			String info = null;
			int icnt = dis.readInt();
			for (int i = 0; i < icnt; i++) {
				info = dis.readUTF();
				System.out.println(info);
			}
		} catch (Exception e) {
			if (!foundInfo)
				System.out.println("no info about training");
		}

		dis.close();

		// moved from out-method
		this.types = new String[pipe.mf.getFeatureCounter().get(PipeGen.REL)];
		for (Entry<String, Integer> e : MFO.getFeatureSet().get(PipeGen.REL).entrySet())
			this.types[e.getValue()] = e.getKey();

		DB.println("Reading data finnished");

		Decoder.NON_PROJECTIVITY_THRESHOLD = (float) options.decodeTH;

		Extractor.initStat(options.featureCreation);

	}

	/**
	 * Do the parsing job
	 * 
	 * @param options
	 * @param pipe
	 * @param params
	 * @throws IOException
	 */
	private void out(BufferedReader inputReader, BufferedWriter outputWriter, OptionsSuper options, Pipe pipe,
			ParametersFloat params, boolean maxInfo, boolean labelOnly) throws Exception {

		long start = System.currentTimeMillis();

		CONLLReader09 depReader = new CONLLReader09(inputReader, options.testfile, options.formatTask);
		CONLLWriter09 depWriter = new CONLLWriter09(outputWriter, options.outfile, options.formatTask);

		int cnt = 0;

		if (maxInfo)
			System.out.println("\nParsing Information ");
		if (maxInfo)
			System.out.println("------------------- ");

		if (maxInfo && !options.decodeProjective)
			System.out.println("" + Decoder.getInfo());

		// these are for printing
		// int del = 0;
		// long last = System.currentTimeMillis();
		
		SYSOUTLOGGER.sysout(2,"Processing sentences...");

		while (true) {

			// Instances is = new Instances();
			// is.init(1, new MFO(),options.formatTask);

			// SentenceData09 instance = pipe.nextInstance(is, depReader);

			SentenceData09 instance = depReader.getNext();
			if (instance == null)
				break;
			cnt++;

			SentenceData09 i09 = this.parse(instance, params, labelOnly, options);

			depWriter.write(i09);

			// does only printing
			// del = PipeGen.outValue(cnt, del, last);

		}
		SYSOUTLOGGER.sysout(2,String.format("Processed sentences: %d", cnt));

		// pipe.close();
		depWriter.finishWriting();
		long end = System.currentTimeMillis();
		// DB.println("errors "+error);
		if (maxInfo)
			System.out.println("Used time " + (end - start));
		if (maxInfo)
			System.out.println("forms count " + Instances.m_count + " unkown " + Instances.m_unkown);

	}

	/**
	 * Parse a single sentence
	 * 
	 * @param instance
	 * @param params
	 * @param labelOnly
	 * @param options
	 * @return
	 */
	public SentenceData09 parse(SentenceData09 instance, ParametersFloat params, boolean labelOnly,
			OptionsSuper options) {
		// moved types to readmodel-method and as instance variable
		/*
		 * String[] types = new
		 * String[pipe.mf.getFeatureCounter().get(PipeGen.REL)]; for
		 * (Entry<String, Integer> e :
		 * MFO.getFeatureSet().get(PipeGen.REL).entrySet()) types[e.getValue()]
		 * = e.getKey();
		 */
		is = new Instances();
		is.init(1, new MFO(), options.formatTask);
		new CONLLReader09().insert(is, instance);

		// use for the training ppos

		SentenceData09 i09 = new SentenceData09(instance);
		i09.createSemantic(instance);

		if (labelOnly) {
			F2SF f2s = params.getFV();

			// repair pheads

			is.pheads[0] = is.heads[0];

			for (int l = 0; l < is.pheads[0].length; l++) {
				if (is.pheads[0][l] < 0)
					is.pheads[0][l] = 0;
			}

			short[] labels = pipe.extractor[0].searchLabel(is, 0, is.pposs[0], is.forms[0], is.plemmas[0], is.pheads[0],
					is.plabels[0], is.feats[0], pipe.cl, f2s);

			for (int j = 0; j < instance.forms.length - 1; j++) {
				i09.plabels[j] = types[labels[j + 1]];
				i09.pheads[j] = is.pheads[0][j + 1];
			}
			return i09;
		}

		if (options.maxLength > instance.length() && options.minLength <= instance.length()) {
			try {
				// System.out.println("prs "+instance.forms[0]);
				// System.out.println("prs "+instance.toString());
				d2 = pipe.fillVector(params.getFV(), is, 0, null, pipe.cl);// cnt-1
				d = Decoder.decode(is.pposs[0], d2, options.decodeProjective, !Decoder.TRAINING); // cnt-1

			} catch (Exception e) {
				e.printStackTrace();
			}

			for (int j = 0; j < instance.forms.length - 1; j++) {
				i09.plabels[j] = types[d.labels[j + 1]];
				i09.pheads[j] = d.heads[j + 1];
			}
		}
		return i09;

	}

	is2.io.CONLLReader09 reader = new is2.io.CONLLReader09(true);

	/*
	 * (non-Javadoc)
	 * 
	 * @see is2.tools.Tool#apply(is2.data.SentenceData09)
	 */
	@Override
	public SentenceData09 apply(SentenceData09 snt09) {

		SentenceData09 it = new SentenceData09();
		it.createWithRoot(snt09);

		SentenceData09 out = null;
		try {

			// for(int k=0;k<it.length();k++) {
			// it.forms[k] = reader.normalize(it.forms[k]);
			// it.plemmas[k] = reader.normalize(it.plemmas[k]);
			// }

			out = parse(it, this.params, false, options);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// do not shutdown threads
		// Decoder.executerService.shutdown();
		// Pipe.executerService.shutdown();

		return out;
	}

	/**
	 * Get the edge scores of the last parse.
	 * 
	 * @return the scores
	 */
	public float[] getInfo() {

		float[] scores = new float[is.length(0)];
		Extractor.encode3(is.pposs[0], d.heads, d.labels, d2, scores);

		return scores;
	}

	/**
	 * Write the parsing model
	 * 
	 * @param options
	 * @param params
	 * @param extension
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	/*
	 * private void writeModell(OptionsSuper options, ParametersFloat params,
	 * String extension, Cluster cs) throws FileNotFoundException, IOException {
	 * 
	 * String name = extension == null ? options.modelName : options.modelName +
	 * extension; // System.out.println("Writting model: "+name);
	 * ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new
	 * FileOutputStream(name))); zos.putNextEntry(new ZipEntry("data"));
	 * DataOutputStream dos = new DataOutputStream(new
	 * BufferedOutputStream(zos));
	 * 
	 * MFO.writeData(dos); cs.write(dos);
	 * 
	 * params.write(dos);
	 * 
	 * dos.writeBoolean(options.stack); dos.writeInt(options.featureCreation);
	 * 
	 * Edges.write(dos);
	 * 
	 * dos.writeBoolean(options.decodeProjective);
	 * 
	 * dos.writeInt(Extractor.maxForm);
	 * 
	 * dos.writeInt(5); // Info count dos.writeUTF("Used parser   " +
	 * Parser.class.toString()); dos.writeUTF("Creation date " + (new
	 * SimpleDateFormat("yyyy.MM.dd HH:mm:ss")).format(new Date()));
	 * dos.writeUTF("Training data " + options.trainfile);
	 * dos.writeUTF("Iterations    " + options.numIters + " Used sentences " +
	 * options.count); dos.writeUTF("Cluster       " + options.clusterFile);
	 * 
	 * dos.flush(); dos.close(); }
	 */
}