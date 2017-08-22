package is2.util;

import is2.util.OptionsSuper;

import java.io.File;


public final class Options extends OptionsSuper {

	

	public Options (String[] args) {
		
		for(int i = 0; i < args.length; i++) {
			String[] pair = args[i].split(":");

			if (pair[0].equals("--help")) explain();
			else if (pair[0].equals("-train")) {
				train = true;
				trainfile = args[i+1];
			} else if (pair[0].equals("-eval")) {
				eval = true;
				goldfile =args[i+1]; i++;
			} else if (pair[0].equals("-test")) {
				test = true;
				testfile = args[i+1]; i++;
			} else if (pair[0].equals("-i")) {
				numIters = Integer.parseInt(args[i+1]); i++;
			}
			else if (pair[0].equals("-out")) {
				outfile = args[i+1]; i++;
			}
			else if (pair[0].equals("-decode")) {
				decodeProjective = args[i+1].equals("proj"); i++;
			}
			else if (pair[0].equals("-confidence")) {
				
				conf = true;
			}

			else if (pair[0].equals("-count")) {
				count = Integer.parseInt(args[i+1]); i++;
			} else if (pair[0].equals("-model")) {
				modelName = args[i+1]; i++;
			} 
			else if (pair[0].equals("-device")) {
				device = args[i+1]; i++;
			} else	if (pair[0].equals("-tmp")) {
				tmp = args[i+1]; i++;
			} else if (pair[0].equals("-format")) {
				//format = args[i+1];
				formatTask = Integer.parseInt(args[i+1]); i++;
			} else if (pair[0].equals("-allfeatures")) {
				allFeatures=true;
			} else if (pair[0].equals("-nonormalize")) {
				normalize=false;
			}else if (pair[0].equals("-nframes")) {
				//format = args[i+1];
				nbframes= args[i+1]; i++;
				 
			
			} else if (pair[0].equals("-pframes")) {
				//format = args[i+1];
				pbframes= args[i+1]; i++;
			} else if (pair[0].equals("-nopred")) {
				nopred =true;	 
			} else if (pair[0].equals("-divide")) {
				keep =true;	 
			} else if (pair[0].equals("-lexicon")) {
 				lexicon= args[i+1]; i++;			

			} else super.addOption(args, i);
			
		}

		
		


		try {
			
			if (trainfile!=null) {
		
				if (keep && tmp!=null) {
					trainforest = new File(tmp);
					if (!trainforest.exists()) keep=false;
					
				} else 
				if (tmp!=null) {
					trainforest = File.createTempFile("train", ".tmp", new File(tmp));
					trainforest.deleteOnExit();
				}
				else {
					trainforest = File.createTempFile("train", ".tmp"); //,new File("F:\\")
					trainforest.deleteOnExit();
				}
				
				
			}

	
		} catch (java.io.IOException e) {
			System.out.println("Unable to create tmp files for feature forests!");
			System.out.println(e);
			System.exit(0);
		}
	}

	private void explain() {
		System.out.println("Usage: ");
		System.out.println("java -class mate.jar is2.parser.Parser [Options]");
		System.out.println();
		System.out.println("Example: ");
		System.out.println(" java -class mate.jar is2.parser.Parser -model eps3.model -train corpora/conll08st/train/train.closed -test corpora/conll08st/devel/devel.closed  -out b3.test -eval corpora/conll08st/devel/devel.closed  -count 2000 -i 6");
		System.out.println("");
		System.out.println("Options:");
		System.out.println("");
		System.out.println(" -train  <file>    the corpus a model is trained on; default "+this.trainfile);
		System.out.println(" -test   <file>    the input corpus for testing; default "+this.testfile);
		System.out.println(" -out    <file>    the output corpus (result) of a test run; default "+this.outfile);
		System.out.println(" -model  <file>    the parsing model for traing the model is stored in the files");
		System.out.println("                   and for parsing the model is load from this file; default "+this.modelName);
		System.out.println(" -i      <number>  the number of training iterations; good numbers are 10 for smaller corpora and 6 for bigger; default "+this.numIters);
		System.out.println(" -count  <number>  the n first sentences of the corpus are take for the training default "+this.count);
		System.out.println(" -format <number>  conll format of the year 8 or 9; default "+this.formatTask);
		
		System.exit(0);
	}
}
