package is2.util;

import is2.io.CONLLReader09;

import java.io.File;

public class OptionsSuper {

	public String trainfile = null;
	public String testfile = null;
	public File trainforest = null;

	public String nbframes = null;
	public String pbframes = null;

	public boolean nopred = false;
	public boolean upper = false;
	
	public boolean train = false;
	public boolean eval = false;
	public boolean test = false;
	public boolean keep = false;
	public boolean flt = false;
	public boolean loadTaggerModels =false;

	public String modelName = "prs.mdl";
	public String modelTaggerName = null;
	
	public String useMapping = null;
	public String device = "C:";
	public String tmp = null;
	public boolean createForest = true;
	public boolean decodeProjective = false;
	public double decodeTH = 0.3d;
	public String format = "CONLL";
	public int formatTask =9;
	public int numIters = 10;
	public int best = 1000;
	public String outfile = "dp.conll";
	public String charset = "UTF-8";
	public String phraseTrain = null;
	public String phraseTest = null;
	public String goldfile = null;
	public String gout = "sec23.gld";
	public String features = null;
	public String lexicon = null;
	public int hsize = 0x07ffffff;
	public int maxLen = 2000;
	public int maxForms = Integer.MAX_VALUE;
	public int beam = 4;
	public float prune = -100000000;
	
	public String third ="";
	public String second ="";
	public String first ="";
	
	public int cross=10;
	
	//public boolean secondOrder = true;
	public boolean useRelationalFeatures = false;
	public int count = 10000000;
	public int cores = Integer.MAX_VALUE;
	public int start = 0;
	public int minOccureForms = 0;
	public int tt=30; // tagger averaging
	public boolean allFeatures =false;
	public boolean normalize =false;
	public boolean no2nd =false;
	public boolean noLemmas=false;
	public boolean few2nd =false,noLinear=false,noMorph=false;
	public String clusterFile;
	
	// output confidence values
	public boolean conf =false;
	public String phraseFormat="penn"; // tiger | penn
	public boolean average = true;
	public boolean label =false;
	public boolean stack=false;
	public boolean oneRoot = false;
	
	public String significant1 =null,significant2 =null;

	
	// horizontal stacking 
	public int minLength =0, maxLength =Integer.MAX_VALUE;
	public boolean overwritegold =false;
	
	
	public static final int MULTIPLICATIVE=1, SHIFT=2;
	public int featureCreation = MULTIPLICATIVE;
	
	
	public OptionsSuper (String[] args, String dummy) {
		
		for(int i = 0; i < args.length; i++) {
			i = addOption(args,i);
		}
				
	}
	
	public OptionsSuper() {}
	
	
	public int  addOption(String args[], int i) {

			if (args[i].equals("-train")) {
				train = true;
				trainfile = args[i+1];
			} else if (args[i].equals("-eval")) {
				eval = true;
				goldfile =args[i+1]; i++;
			} else if (args[i].equals("-gout")) {
				gout =args[i+1]; i++;
			} else if (args[i].equals("-test")) {
				test = true;
				testfile = args[i+1]; i++;
			} else if (args[i].equals("-sig1")) {
				significant1 = args[i+1]; i++;
			} else if (args[i].equals("-sig2")) {
				significant2 = args[i+1]; i++;
			} else if (args[i].equals("-i")) {
				numIters = Integer.parseInt(args[i+1]); i++;
			} else if (args[i].equals("-out")) {
				outfile = args[i+1]; i++;
			} else if (args[i].equals("-cluster")) {
				clusterFile = args[i+1]; i++;
			}

			else if (args[i].equals("-count")) {
				count = Integer.parseInt(args[i+1]); i++;
			} else if (args[i].equals("-model")) {
				modelName = args[i+1]; i++;
			} else if (args[i].equals("-tmodel")) {
				this.modelTaggerName = args[i+1]; i++;
			} else if (args[i].equals("-nonormalize")) {
				normalize=false;
			} else if (args[i].equals("-float")) {
				flt =true;	 
			} else if (args[i].equals("-hsize")) {
				hsize= Integer.parseInt(args[i+1]); i++;			
			} else if (args[i].equals("-charset")) {
				charset= args[++i]; 			
			} else if (args[i].equals("-pstrain")) {
				this.phraseTrain=args[i+1]; i++; 			
			} else if (args[i].equals("-pstest")) {
				this.phraseTest=args[i+1]; i++; 			
			} else if (args[i].equals("-len")) {
				maxLen= Integer.parseInt(args[i+1]); i++;			
			} else if (args[i].equals("-cores")) {
				cores= Integer.parseInt(args[i+1]); i++;			
			} else if (args[i].equals("-start")) {
				start= Integer.parseInt(args[i+1]); i++;			
			} else if (args[i].equals("-max")) {
				maxLength= Integer.parseInt(args[i+1]); i++;			
			} else if (args[i].equals("-min")) {
				minLength= Integer.parseInt(args[i+1]); i++;			
			} else if (args[i].equals("-noLemmas")) {
				noLemmas= true;			
			} else if (args[i].equals("-noavg")) {
				this.average= false;			
			} else if (args[i].equals("-label")) {
				label= true;			
			} else if (args[i].equals("-stack")) {
				stack= true;		
			} else if (args[i].equals("-overwritegold")) {
				overwritegold = true;		
			} else if (args[i].equals("-format")) {
				formatTask = Integer.parseInt(args[++i]);		
			} else if (args[i].equals("-tt")) {
				tt = Integer.parseInt(args[++i]);		
			} else if (args[i].equals("-min-occure-forms")) {
				minOccureForms = Integer.parseInt(args[++i]);		
			} else if (args[i].equals("-loadTaggerModels")) {
				this.loadTaggerModels=true;;
		
			} else if (args[i].equals("-feature_creation")) {
				this.featureCreation = args[++i].equals("shift")?SHIFT:MULTIPLICATIVE;		
			}
			
			return i;
					
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("FLAGS [");
		sb.append("train-file: " + trainfile);
		sb.append(" | ");
		sb.append("test-file: " + testfile);
		sb.append(" | ");
		sb.append("gold-file: " + goldfile);
		sb.append(" | ");
		sb.append("output-file: " + outfile);
		sb.append(" | ");
		sb.append("model-name: " + modelName);
		sb.append(" | ");
		sb.append("train: " + train);
		sb.append(" | ");
		sb.append("test: " + test);
		sb.append(" | ");
		sb.append("eval: " + eval);
		sb.append(" | ");
		sb.append("training-iterations: " + numIters);
		sb.append(" | ");
		sb.append("decode-type: " + decodeProjective);
		sb.append(" | ");
		sb.append("create-forest: " + createForest);
		sb.append(" | ");
		sb.append("format: " + format);
	
		sb.append("]\n");
		return sb.toString();
	}

}