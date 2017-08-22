package is2.parser;

import is2.util.OptionsSuper;


public final class Options extends OptionsSuper {

	
	public Options (String[] args) {
		

		
		for(int i = 0; i < args.length; i++) {

			if (args[i].equals("--help")) explain();
			
			if (args[i].equals("-decode")) {
				decodeProjective = args[i+1].equals("proj"); i++;
			} else if (args[i].equals("-decodeTH")) {		
				decodeTH = Double.parseDouble(args[i+1]); i++;
			} else if (args[i].equals("-nonormalize")) {
				normalize=false;
			} else if (args[i].equals("-features")) {
				features= args[i+1]; i++;			
			} else if (args[i].equals("-hsize")) {
				hsize= Integer.parseInt(args[i+1]); i++;			
			} else if (args[i].equals("-len")) {
				maxLen= Integer.parseInt(args[i+1]); i++;			
			} else if (args[i].equals("-cores")) {
				cores= Integer.parseInt(args[i+1]); i++;			
			} else if (args[i].equals("-no2nd")) {
				no2nd= true;			
			} else if (args[i].equals("-few2nd")) {
				few2nd= true;			
			} else super.addOption(args, i);
					
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
