package fi.dep;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.dep.ported.UConverter;
import fi.dep.ported.UConverterImpl;
import fi.dep.utils.ApplicationProperties;
import fi.dep.utils.SimpleStats;
import fi.dep.utils.cache.MyCache;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.TokenizerStream;
import opennlp.tools.tokenize.WhitespaceTokenStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.ParagraphStream;
import opennlp.tools.util.PlainTextByLineStream;

public class FinDepServlet extends SuperServlet {

	private Logger logger = LoggerFactory.getLogger(FinDepServlet.class);

	private final static String SENTENCE_MODEL_FILE = "model/fi-sent.bin";
	private final static String TOKEN_MODEL_FILE = "model/fi-token.bin";

	// models are instantiated once
	private SentenceModel sentenceModel;
	private TokenizerModel tokenModel;

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
	private final static Semaphore lock = new Semaphore(1, true);

	private SimpleStats SIMPLE_STATS = SimpleStats.getInstance();

	private MyCache CACHE = MyCache.getInstance();

	// for testing
	private boolean doNotDeleteTempDir = false;

	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {
		super.init();

		logger.info("Initializing...");
		doNotDeleteTempDir = Boolean.parseBoolean(System.getenv("do_not_delete_tmp_dir"));

		workDir = FileSystems.getDefault().getPath(workDirName);

		try {
			// models are loaded once
			sentenceModel = new SentenceModel(new File(SENTENCE_MODEL_FILE));
			tokenModel = new TokenizerModel(new File(TOKEN_MODEL_FILE));
		} catch (IOException e) {
			logger.error("Sentence model load failed.", e);
			throw new ServletException(e);
		}
		logger.info("Initializing... Done.");

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding(StandardCharsets.UTF_8.name());
		String inputText = req.getParameter("text");
		if (inputText != null) {
			long startTimeNano = System.nanoTime();
			long startTimeMsec = System.currentTimeMillis();
			logger.debug("START");
			handleParsingRequest(inputText, req, resp, startTimeNano, startTimeMsec);

		} else {

			resp.setContentType("text/plain");
			resp.setStatus(HttpServletResponse.SC_OK);
			PrintWriter pw = resp.getWriter();
			pw.println("Hello from finnish-dep-parser server.");
			pw.println("HTTP POST Finnish text to this URL and get CoNLL-U back.");
			pw.println("HTTP GET (with param 'text') Finnish text to this URL and get CoNLL-U back.");
			pw.println("");
			pw.println(
					"Version " + ApplicationProperties.version() + " (" + ApplicationProperties.buildtimestamp() + ")");
			pw.println("");

			pw.println(SIMPLE_STATS.getStatistics());
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		long startTimeNano = System.nanoTime();
		long startTimeMsec = System.currentTimeMillis();
		logger.debug("START");
		req.setCharacterEncoding(StandardCharsets.UTF_8.name());

		// read input to string
		/*
		 * BufferedInputStream bis = new
		 * BufferedInputStream(req.getInputStream()); ByteArrayOutputStream baos
		 * = new ByteArrayOutputStream(); int inputSize = 0; byte[] inputBytes =
		 * new byte[1024]; int bytesRead = 0; while ((bytesRead =
		 * bis.read(inputBytes)) != -1) { inputSize = inputSize + bytesRead;
		 * baos.write(inputBytes, 0, bytesRead); } baos.close();
		 * 
		 * String inputText = new String(baos.toByteArray(),
		 * Charset.forName(StandardCharsets.UTF_8.name()));
		 */
		String inputText = readInputStreamToString(req.getInputStream());
		handleParsingRequest(inputText, req, resp, startTimeNano, startTimeMsec);
	}

	private void handleParsingRequest(String inputText, HttpServletRequest req, HttpServletResponse resp,
			long startTimeNano, long startTimeMsec) throws ServletException, IOException {

		int inputSize = inputText.length();

		logger.debug(inputText);

		resp.setContentType("text/plain");
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		PrintWriter pw = resp.getWriter();
		boolean errorHappened = false;

		// check if input is empty
		if (inputText.trim().equals("")) {
			// do not parse empty input, finnish dependency parser does not
			// handle empty input
			// send single empty CoNLL-U line as response
			String emptyLine = "1	 	 	_	_	_	_	_	_	_";
			pw.println(emptyLine);

		} else {
			// input is not empty, parse it

			ParseReturnObject pro = new ParseReturnObject();
			try {
				if (CACHE.isEnabled()) {
					// check from cache
					pro = getFromCache(inputText);
				} else {
					pro = parse(inputText);
				}
			} catch (Exception e) {
				logger.error(e.toString(), e);
				throw new ServletException(e);
			}

			errorHappened = pro.errorHappened;

			if (pro.rv == -234566) {
				// error when executing this servlet
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				pw.println("Waiting for lock interrupted.");
				pw.println(pro.errorString);
			} else {
				if (pro.rv == 0) {
					resp.setStatus(HttpServletResponse.SC_OK);

				} else {
					resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}

				BufferedReader br = pro.reader;
				for (String line = br.readLine(); line != null; line = br.readLine()) {
					pw.println(line);
				}
				br.close();
				pro.deleteTmpDir();
			}
		}

		long endTimeNano = System.nanoTime();
		long endTimeMsec = System.currentTimeMillis();

		double elapsedTime = (endTimeMsec - startTimeMsec) / 1000.0;
		logger.debug("END {} secs", elapsedTime);

		if (SYSOUTLOGGER.LOG_LEVEL == 1) {
			// log only if log level is 1
			SYSOUTLOGGER.sysout(elapsedTime + " secs, " + inputText);
		}

		String logText = null;
		if (SYSOUTLOGGER.LOG_LEVEL == 0) {
			// do not add logtext to stats unless log level is 0
			logText = elapsedTime + " secs, " + inputText;
		}
		// add latest parsed time and excerpt to stats
		SIMPLE_STATS.addRequest(startTimeNano, endTimeNano, startTimeMsec, endTimeMsec, inputSize, errorHappened,
				logText);

	}

	private ParseReturnObject getFromCache(String inputText) throws Exception {
		ParseReturnObject pro = new ParseReturnObject();

		String md5Hex = DigestUtils.md5Hex(inputText);
		String conlluText = CACHE.get(md5Hex);
		// String conlluText = lfuCache.get(md5Hex);
		if (conlluText != null) {
			// found from cache
			logger.debug("Found from cache.");
			SIMPLE_STATS.increaseCacheHits();
		} else {
			// not in cache
			logger.debug("Not found from cache");
			pro = parse(inputText);
			BufferedReader br = pro.reader;// new BufferedReader(new
											// FileReader(f));
			StringBuilder sb = new StringBuilder();
			PrintWriter pw = new PrintWriter(new StringBuilderWriter(sb));
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				pw.println(line);
			}
			pw.close();
			br.close();
			conlluText = sb.toString();
			CACHE.put(md5Hex, conlluText);
			pro.deleteTmpDir();
			logger.debug("Key {} added to cache.", md5Hex);
		}
		// set reader
		pro.reader = new BufferedReader(new StringReader(conlluText));

		return pro;
	}

	private ParseReturnObject parse(String inputText) throws Exception {

		ParseReturnObject pro = new ParseReturnObject();
		Path tmpDir = null;
		int rv = -1;
		String errorString = "";

		try {
			// TODO: multithreading
			if (lock.tryAcquire(1, waitTimeForLockInSeconds, TimeUnit.SECONDS)) {
				try {

					// instantiate sentence detector and tokenizer using
					// preloaded models
					SentenceDetectorME sentenceDetector = new SentenceDetectorME(sentenceModel);
					TokenizerME tokenizer = new TokenizerME(tokenModel);

					// this is how sentencedetector works in opennlp command
					// line tool
					// opennlp used by Finnish-dep-parser is 1.5.3
					ObjectStream<String> paraStream = new ParagraphStream(
							new PlainTextByLineStream(new InputStreamReader(
									new ByteArrayInputStream(inputText.getBytes(StandardCharsets.UTF_8.name())))));
					StringBuilder sb = new StringBuilder();

					String para;
					while ((para = paraStream.read()) != null) {

						String[] sentences = sentenceDetector.sentDetect(para);
						for (String sentence : sentences) {
							// this is how tokenizer works in opennlp command
							// line tool
							ObjectStream<String> untokenizedLineStream = new PlainTextByLineStream(
									new InputStreamReader(new ByteArrayInputStream(
											sentence.getBytes(StandardCharsets.UTF_8.name()))));

							ObjectStream<String> tokenizedLineStream = new WhitespaceTokenStream(
									new TokenizerStream(tokenizer, untokenizedLineStream));

							String tokenizedLine;
							while ((tokenizedLine = tokenizedLineStream.read()) != null) {
								if (tokenizedLine.equals(""))
									continue;

								String[] tokens = tokenizedLine.split(" ");
								addTokens(sb, tokens);
							}
						}
					}
					paraStream.close();
					String _inputText = sb.toString();

					// create tmpDir for this request
					tmpDir = Files.createTempDirectory(workDir, "tmp_data");
					pro.tmpDir = tmpDir;

					// call parser
					rv = callParserProcess(_inputText, tmpDir);
					pro.rv = rv;

					// set reader
					File f;
					String fileName = outputFileName;
					if (rv != 0) {
						// if error, read stderr file
						fileName = errorFileName;
						pro.errorHappened = true;
					}
					f = new File(tmpDir.toFile(), fileName);
					pro.reader = new BufferedReader(new FileReader(f));
					pro.errorHappened = false;
				} finally {
					lock.release();
				}
			}
		} catch (InterruptedException e) {
			errorString = e.toString();
			e.printStackTrace();
			rv = -234566;
			pro.errorString = errorString;
			pro.rv = rv;
			pro.errorHappened = true;
		}

		return pro;

	}

	private void addTokens(StringBuilder sb, String[] tokens) {
		// replaces txt_to_09.py
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			sb.append(String.format("%d\t%s\t_\t_\t_\t_\t_\t_\t_\t_\t_\t_\t_\t_\n", i + 1, token));
		}
		sb.append("\n");
	}

	private int callParserProcess(String inputText, Path tmpDir) throws Exception {
		// calls my_parser_wrapper.sh script

		// start replace: conv_u_09.py --output=09
		/*UConverter uconv = new UConverterImpl();
		logger.trace("replace: conv_u_09.py --output=09");
		logger.trace("inputText:\n{}",inputText);
		inputText=uconv.convertUto09(inputText);
		logger.trace("inputText:\n{}",inputText);
		*/
		// end replace: conv_u_09.py --output=09

		logger.debug("Calling parser process...");
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
			logger.error(e.toString(), e);
		}
		
		// start replace: conv_u_09.py --output=u
		/*logger.trace("replace: conv_u_09.py --output=u");
		File outFile=new File(outputFileName);
		logger.trace("outputFileName: {}",outputFileName);
		String outputText=FileUtils.readFileToString(outFile, StandardCharsets.UTF_8);
		outputText=uconv.convert09toU(outputText);
		FileUtils.write(outFile, outputText, StandardCharsets.UTF_8);
		*/// end replace: conv_u_09.py --output=u

		
		logger.debug("Parser process completed. return value: {}", rv);

		return rv;
	}

	private class ParseReturnObject {
		boolean errorHappened = false;
		String errorString = null;
		BufferedReader reader = null;
		Path tmpDir = null;
		int rv = 0;

		public ParseReturnObject() {

		}

		public void deleteTmpDir() {
			if (doNotDeleteTempDir != true) {
				try {
					if (tmpDir != null) {
						FileUtils.deleteDirectory(tmpDir.toFile());
						tmpDir = null;
					}

				} catch (IOException ioe) {
					// tmpdir delete failed
					logger.error("Temp dir delete failed.", ioe);
				}
			}

		}
	}
}
