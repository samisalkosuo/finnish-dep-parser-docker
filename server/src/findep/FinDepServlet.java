package findep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.activemq.util.LFUCache;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.StringBuilderWriter;

import findep.utils.SimpleStats;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class FinDepServlet extends HttpServlet {

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

	private boolean useConlluCache = false;
	private LFUCache<String, String> lfuCache = null;

	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {
		super.init();
		log("Initializing " + getClass().getName());

		String cacheSize = System.getenv("conllu_cache_size");
		if (cacheSize != null) {
			log("conllu_cache_size: " + cacheSize);
			try {
				int _cacheSize = Integer.parseInt(cacheSize);
				// set up cache
				useConlluCache = true;
				SIMPLE_STATS.maxCacheSize=_cacheSize;
				lfuCache = new LFUCache<String, String>(_cacheSize, 0.2f);

			} catch (NumberFormatException nfe) {
				log(nfe.toString());
				log("conllu LFU cache is not used");
			}
		} else {
			log("conllu LFU cache is not used");
		}

		workDir = FileSystems.getDefault().getPath(workDirName);

		try {
			// models are loaded once
			sentenceModel = new SentenceModel(new File(SENTENCE_MODEL_FILE));
			tokenModel = new TokenizerModel(new File(TOKEN_MODEL_FILE));
		} catch (IOException e) {
			System.err.println("Sentence model load failed.");
			throw new ServletException(e);
		}

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain");
		resp.setStatus(HttpServletResponse.SC_OK);
		PrintWriter pw = resp.getWriter();
		pw.println("Hello from finnish-dep-parser server. Post Finnish text to this URL and get CoNLL-U back.");

		pw.println("");
		pw.println(SIMPLE_STATS.getStatistics(lfuCache));
	}

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
			log(line);
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

		boolean errorHappened = false;
		String inputText = sb.toString();

		ParseReturnObject pro = new ParseReturnObject();
		if (useConlluCache == true) {
			// check from cache
			pro = getFromCache(inputText);
		} else {
			pro = parse(inputText);
		}

		resp.setContentType("text/plain");
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		errorHappened = pro.errorHappened;
		PrintWriter pw = resp.getWriter();

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

			br = pro.reader;
			for (line = br.readLine(); line != null; line = br.readLine()) {
				pw.println(line);
			}
			br.close();
			pro.deleteTmpDir();
		}
		long endTimeNano = System.nanoTime();
		long endTimeMsec = System.currentTimeMillis();

		double elapsedTime = (endTimeMsec - startTimeMsec) / 1000.0;
		log("END " + elapsedTime + " secs");
		log("");

		SIMPLE_STATS.addRequest(startTimeNano, endTimeNano, startTimeMsec, endTimeMsec, inputSize, errorHappened);

	}

	private ParseReturnObject getFromCache(String inputText) throws IOException {
		ParseReturnObject pro = new ParseReturnObject();

		String md5Hex = DigestUtils.md5Hex(inputText);
		String conlluText = lfuCache.get(md5Hex);
		if (conlluText != null) {
			// found from cache
			log("found from LFU cache");
			SIMPLE_STATS.increaseCacheHits();
			int freq = lfuCache.frequencyOf(md5Hex);
			log(String.format("Doc %s accessed >= %d times", md5Hex, freq));
		} else {
			// not in cache
			log("not found from LFU cache");
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
			lfuCache.put(md5Hex, conlluText);
			pro.deleteTmpDir();
			log(String.format("Doc %s added to LFU cache", md5Hex));
		}
		// set reader
		pro.reader = new BufferedReader(new StringReader(conlluText));

		return pro;
	}

	private ParseReturnObject parse(String inputText) throws IOException {

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

					String[] sentences = sentenceDetector.sentDetect(inputText);
					StringBuilder sb = new StringBuilder();
					for (String sentence : sentences) {

						// tokenize
						String[] tokens = tokenizer.tokenize(sentence);
						// replaces txt_to_09.py
						for (int i = 0; i < tokens.length; i++) {
							String token = tokens[i];
							sb.append(String.format("%d\t%s\t_\t_\t_\t_\t_\t_\t_\t_\t_\t_\t_\t_\n", i + 1, token));
						}
						sb.append("\n");
					}

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

	private int callParserProcess(String inputText, Path tmpDir) throws IOException {
		// calls my_parser_wrapper.sh script

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
		/*
		 * BufferedOutputStream toProcessInputStream=new
		 * BufferedOutputStream(p.getOutputStream());
		 * 
		 * StringReader sr=new StringReader(inputText);
		 * 
		 * for(int b=sr.read();b!=-1;b=sr.read()) {
		 * toProcessInputStream.write(b); }
		 * 
		 * toProcessInputStream.flush();
		 * 
		 * BufferedOutputStream bos=new BufferedOutputStream(output);
		 * InputStream fromProcessOutputStream=p.getInputStream(); for(int
		 * b=fromProcessOutputStream.read();b!=-1;b=fromProcessOutputStream.read
		 * ()) { bos.write(b); } bos.flush();
		 */
		int rv = -1;
		try {
			rv = p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log("parser completed. return value: " + rv);

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
			try {
				if (tmpDir != null) {
					FileUtils.deleteDirectory(tmpDir.toFile());
					tmpDir = null;
				}

			} catch (IOException ioe) {
				// tmpdir delete failed
				log("Temp dir delete failed: " + ioe.toString());
			}
		}
	}
}
