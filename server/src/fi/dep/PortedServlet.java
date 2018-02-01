package fi.dep;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.dep.ported.Tag;
import fi.dep.ported.TagImpl;
import fi.dep.utils.SimpleStats;
import marmot.morph.MorphTagger;
import net.sf.hfst.HfstOptimizedLookupObj;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;


public class PortedServlet extends HttpServlet {

	private Logger logger = LoggerFactory.getLogger(PortedServlet.class);

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

	// ???
	private int waitTimeForLockInSeconds = 3600 * 4;// four hours in case there
	// are huge amount of
	// requests incoming
	// Hfst may run into problems is accessing many times
	// workaround to make it singlethreaded
	private final static Semaphore lock = new Semaphore(1, true);

	private SimpleStats SIMPLE_STATS= SimpleStats.getInstance();

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {
		super.init();
		logger.info("Initializing...");

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

			logger.info("Initializing... Done.");

		} catch (Exception e) {
			logger.error("Sentence model load failed.",e);
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

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		long startTimeNano = System.nanoTime();
		long startTimeMsec = System.currentTimeMillis();
		logger.debug("START");
		req.setCharacterEncoding(StandardCharsets.UTF_8.name());

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
	//	try {
			// TODO: multithreading ADD HERE OR USE ONLY ONE THREAD + CONFIGURE QUEUE LENGTH TO THE APP SERVER
			// It is better, if queu length runs out, then requests fail quick. This indicates node corrupt, 
			// and can be restarted. Thus setting thread pool size 1 for this servlet with configured queue length
			// makes more sense.
		//	if (lock.tryAcquire(1, waitTimeForLockInSeconds, TimeUnit.SECONDS)) {
		//		try {

									outputText = callParserProcess(sb.toString());

		//		} finally {
		//			lock.release();
		//		}
		//	}
	//	} catch (InterruptedException e) {
	//		log("InterruptedException:\n"+e);
	//		outputText=null;
	//		lock.release(); 
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
			pw.print(outputText);			
		}
		long endTimeNano = System.nanoTime();
		long endTimeMsec = System.currentTimeMillis();

		double elapsedTime = (endTimeMsec - startTimeMsec) / 1000.0;
		logger.debug("END " + elapsedTime + " secs");

		SIMPLE_STATS.addRequest(startTimeNano, endTimeNano, startTimeMsec, endTimeMsec, inputSize,errorHappened);

	}

	public synchronized String callParserProcess(String in) throws IOException {

		// detect sentences
		String[] sentences = sentenceDetector.sentDetect(in); //safeSentences(sb.toString()); 
		StringBuilder sb = new StringBuilder();
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
		// call parser

		
		// This is similar to the FinDepServlet, but carrying out operations
		// without calling the python scripts
		//UConverter uconverter = new UConverterImpl(log);
		Tag tag = new TagImpl(hfst_morphology,tagger);

		String outputText = "";

		try {
			// log.debug("in:\n"+inputText);
			outputText = tag.quickParse(inputText);
		} catch (Exception e) {
			logger.error("Failed to parse", e);
		}
		logger.debug("parser completed. "); 

		return outputText;
	}

}