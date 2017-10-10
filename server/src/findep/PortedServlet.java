package findep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import findep.marmot.Annotator;
import findep.ported.ParserLog;
import findep.ported.ParserLogImpl;
import findep.ported.Tag;
import findep.ported.TagImpl;
import findep.ported.UConverter;
import findep.ported.UConverterImpl;
import findep.utils.SimpleStats;
import marmot.morph.MorphTagger;
import net.sf.hfst.HfstOptimizedLookupObj;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;


public class PortedServlet extends HttpServlet {

	private final static String SENTENCE_MODEL_FILE = "model/fi-sent.bin";
	private final static String TOKEN_MODEL_FILE = "model/fi-token.bin";

	private final static String MODEL_MORPHOLOGY = "model/morphology.finntreebank.hfstol";
	private HfstOptimizedLookupObj hfst_morphology = null;
	
	// marmot stuff
	private final static String MODEL_MARMOT="model/fin_model.marmot";
	private MorphTagger tagger=null;
	
	private SentenceDetectorME sentenceDetector = null;
	private Tokenizer tokenizer = null;

	private String workDirName = "/Finnish-dep-parser";
	private Path workDir;

	private String inputFileName = "input_from_client.txt";
	private String outputFileName = "parsed_text.conllu";
	private String errorFileName = "syserr.txt";

	private int waitTimeForLockInSeconds = 3600 * 4;// four hours in case there
													// are huge amount of
													// requests incoming
	// Hfst may run into problems is accessing many times
	// workaround to make it singlethreaded
	//private final static Semaphore lock = new Semaphore(1, true);

	private SimpleStats SIMPLE_STATS= SimpleStats.getInstance();

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {
		super.init();
		log("Initializing "+getClass().getName());
		workDir = FileSystems.getDefault().getPath(workDirName);

		
		try {
			// Not 100% sure do we have to use this - anyhow....
			SentenceModel sentenceModel = new SentenceModel(new File(SENTENCE_MODEL_FILE));
			sentenceDetector = new SentenceDetectorME(sentenceModel);

			TokenizerModel model = new TokenizerModel(new File(TOKEN_MODEL_FILE));
			tokenizer = new TokenizerME(model);

			// this is used to check the tree model
			hfst_morphology =  new HfstOptimizedLookupObj(MODEL_MORPHOLOGY);

			// this is used to parse the 'POS' for each word,
			// so comparing the pos from here to the 'treebank' hits - we select the correct lemma
			tagger= marmot.util.FileUtils.loadFromFile(MODEL_MARMOT);
			
		} catch (Exception e) {
			System.err.println("Sentence model load failed.");
			throw new ServletException(e);
		}

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain");
		resp.setStatus(HttpServletResponse.SC_OK);
		PrintWriter pw=resp.getWriter();
		pw.println("Hello from finnish-dep-parser server. Post Finnish text to this URL and get CoNLL-U back.");

		pw.println("");
		pw.println(SIMPLE_STATS.getStatistics());
	}

	/* THESE DID NOT HELP 
	public synchronized String[] safeSentences(String in) {
		return sentenceDetector.sentDetect(in);
	}
	public synchronized String[] safeTokens(String in) {
		return tokenizer.tokenize(in);
	}
	*/
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		long startTimeNano = System.nanoTime();
		long startTimeMsec = System.currentTimeMillis();
		log("START");
		req.setCharacterEncoding(StandardCharsets.UTF_8.name());

		// TODO: convert scripts to this servlet
		// TODO: use input and outputstreams directly without files

		// read input to string
		BufferedReader br = req.getReader();
		String line = br.readLine();
		StringBuilder sb = new StringBuilder();
		int inputSize = 0;
		while (line != null) {
		//	log(line);
			sb.append(line);
			inputSize = inputSize + line.length();
			line = br.readLine();
			// add space after each line
			// so this removes all new lines from input text
			// sb.append(" ");
			// or add new line so that text is like incoming text
			sb.append("\n");
		}
		br.close();

		Path tmpDir = null;
		//int rv = -1;
		String outputText = "";
		
		String errorString = "";
		boolean errorHappened=false;
		//try {
			// TODO: multithreading ADD HERE OR USE ONLY ONE THREAD + CONFIGURE QUEUE LENGTH TO THE APP SERVER
		//	if (lock.tryAcquire(1, waitTimeForLockInSeconds, TimeUnit.SECONDS)) {
		//		try {

					// detect sentences
					String[] sentences = sentenceDetector.sentDetect(sb.toString()); //safeSentences(sb.toString()); 
					sb = new StringBuilder();
					for (String sentence : sentences) {

						// tokenize NOTE THIS HAS BEEN CHANGED TO JUST PASS CLEAR TEXT IN 
						String[] tokens = tokenizer.tokenize(sentence); //safeTokens(sentence); 
						// replaces txt_to_09.py
						for (int i = 0; i < tokens.length; i++) {
							String token = tokens[i];
							//sb.append(String.format("%d\t%s\t_\t_\t_\t_\t_\t_\t_\t_\t_\t_\t_\t_\n", i + 1, token));
							// This is alternative way - we want to use marmot as the parser
							// In this case we want to go one sentence at a time.
							sb.append(token);
							if(i<tokens.length-1) {
								sb.append(" ");
							}
						}
						sb.append("\n");
					}

					String inputText = sb.toString();

					// create tmpDir for this request
				//	tmpDir = Files.createTempDirectory(workDir, "tmp_data");
					// call parser
					outputText = callParserProcess(inputText, tmpDir);

			//} finally {
			//	lock.release();
			//}
			//}
	//	} catch (InterruptedException e) {
	//		errorString = e.toString();
	//		e.printStackTrace();
			//rv = -234566;
	//	}

		resp.setContentType("text/plain");
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

		PrintWriter pw = resp.getWriter();
		if (outputText == null || "".equals(outputText)) {
			// error when executing this servlet
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			pw.println("Waiting for lock interrupted.");
			pw.println(errorString);
			errorHappened=true;
		} else {
			// read output file
			/*
			File f;
			if (rv == 0) {
				resp.setStatus(HttpServletResponse.SC_OK);
				// if success, read stdout file
				f = new File(tmpDir.toFile(), outputFileName);

			} else {
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				// if error, read stderr file
				f = new File(tmpDir.toFile(), errorFileName);
				errorHappened=true;
			}

			br = new BufferedReader(new FileReader(f));
			for (line = br.readLine(); line != null; line = br.readLine()) {
				pw.println(line);
			}
			br.close();
*/
			pw.print(outputText);
			
			// delete temp dir
			/*
			try {
				if (tmpDir != null) {
					FileUtils.deleteDirectory(tmpDir.toFile());
				}

			} catch (IOException ioe) {
				// tmpdir delete failed
				log("Temp dir delete failed: " + ioe.toString());
			}
			*/
			
		}
		long endTimeNano = System.nanoTime();
		long endTimeMsec = System.currentTimeMillis();

		double elapsedTime = (endTimeMsec - startTimeMsec) / 1000.0;
		log("END " + elapsedTime + " secs");
		//log("");

		SIMPLE_STATS.addRequest(startTimeNano, endTimeNano, startTimeMsec, endTimeMsec, inputSize,errorHappened);

	}

		
	private String callParserProcess(String inputText, Path tmpDir) throws IOException {
	
		// This is similar to the FinDepServlet, but carrying out operations
		// without calling the python scripts
		ParserLog log = new ParserLogImpl();
		//UConverter uconverter = new UConverterImpl(log);
		Tag tag = new TagImpl(log,hfst_morphology,tagger);
		
		String outputText = "";
		
		try {
			// log.debug("in:\n"+inputText);
			outputText = tag.quickParse(inputText);
			//log.debug(""+outputText);
			//String input09Text = uconverter.convertUto09(inputText);
			//log.debug("09:\n"+input09Text);
			//Set words = tag.sortUnique(input09Text);
			//log.debug("Called set");
			//outputText+=words.toString();
			// TODO add the rest
		} catch (Exception e) {
			log.error("Failed to parse", e);
		}
		
		// calls my_parser_wrapper.sh script
		/*
		File f = new File(tmpDir.toFile(), inputFileName);
		FileWriter fw = new FileWriter(f);
		fw.write(inputText);
		fw.close();

		List<String> command = new ArrayList<String>();
		command.add("./my_parser_wrapper.sh");

		ProcessBuilder pb = new ProcessBuilder(command);
		pb.directory(new File(workDirName));

		// set environment variables for parser scripts
		Map<String, String> env = pb.environment();
		env.put("TMPDIR", tmpDir.toString());
		env.put("INPUT_TEXT_FILE", inputFileName);
		env.put("OUTPUT_CONLLU_FILE", outputFileName);
		env.put("ERROR_FILE", errorFileName);
		// sentences longer than this will be chopped and parsed chunked
		env.put("MAX_SEN_LEN", "100");
		// length of the chunk into which the sentences will be chopped (the
		// actual chunk size will differ a bit, depending where a suitable place
		// can be found to cut the chunks)
		env.put("SEN_CHUNK", "33");
		env.put("PYTHON", "python");

		// log("ENV: "+pb.environment());

		Process p = pb.start();
		int rv = -1;
		try {
			rv = p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		log("parser completed. "); //return value: " + rv);

		return outputText;
	}

}