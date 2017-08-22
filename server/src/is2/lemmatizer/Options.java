package is2.lemmatizer;

import java.io.File;
import java.io.IOException;

import is2.util.OptionsSuper;


public final class Options extends OptionsSuper {

	
	public Options (String[] args) throws IOException {
		

		
		for(int i = 0; i < args.length; i++) {

			if (args[i].equals("--help")) explain();
			
			if (args[i].equals("-normalize")) {
				normalize=Boolean.parseBoolean(args[++i]);
			} else if (args[i].equals("-features")) {
				features= args[i+1]; i++;			
			} else if (args[i].equals("-hsize")) {
				hsize= Integer.parseInt(args[i+1]); i++;			
			} else if (args[i].equals("-len")) {
				maxLen= Integer.parseInt(args[i+1]); i++;			
			} else	if (args[i].equals("-tmp")) {
				tmp = args[i+1]; i++;
			} else	if (args[i].equals("-uc")) {
				upper=true;
				System.out.println("set uppercase "+upper);

			} else super.addOption(args, i);
					
		}

		if (trainfile!=null) {
			
			
			if (tmp!=null) trainforest = File.createTempFile("train", ".tmp", new File(tmp));
			else trainforest = File.createTempFile("train", ".tmp"); //,new File("F:\\")
			trainforest.deleteOnExit();
		}

	

		
	}

	private void explain() {
		System.out.println("Usage: ");
		System.out.println("java -class mate.jar is2.lemmatizer.Lemmatizer [Options]");
		System.out.println();
		System.out.println("Options:");
		System.out.println("");
		System.out.println(" -train  <file>    the corpus a model is trained on; default "+this.trainfile);
		System.out.println(" -test   <file>    the input corpus for testing; default "+this.testfile);
		System.out.println(" -out    <file>    the output corpus (result) of a test run; default "+this.outfile);
		System.out.println(" -model  <file>    the parsing model for traing the model is stored in the files");
		System.out.println("                   and for parsing the model is load from this file; default "+this.modelName);
		System.out.println(" -i      <number>  the number of training iterations; good numbers are 10 for smaller corpora and 6 for bigger; default "+this.numIters);
		System.out.println(" -count  <number>  the n first sentences of the corpus are take for the training default "+this.count);
		
		System.exit(0);
	}
}
