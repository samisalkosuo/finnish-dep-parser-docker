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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

public class FinDepServlet extends HttpServlet {

	private final static String SENTENCE_MODEL_FILE = "model/fi-sent.bin";
	private final static String TOKEN_MODEL_FILE = "model/fi-token.bin";

	// private SentenceDetectorME sentenceDetector = null;
	// private Tokenizer tokenizer =null;
	private String workDirName = "/Finnish-dep-parser";
	private Path workDir;

	private String inputFileName = "input_from_client.txt";
	private String outputFileName = "parsed_text.conllu";
	private String errorFileName = "syserr.txt";

	private int waitTimeForLockInSeconds = 3600*4;//four hours in case there are huge amount of requests incoming
	// Hfst may run into problems is accessing many times
	// workaround to make it singlethreaded
	private final static Semaphore lock = new Semaphore(1, true);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {
		super.init();

		workDir = FileSystems.getDefault().getPath(workDirName);
		/*
		 * try { SentenceModel sentenceModel = new SentenceModel(new
		 * File(SENTENCE_MODEL_FILE)); sentenceDetector = new
		 * SentenceDetectorME(sentenceModel);
		 * 
		 * TokenizerModel model = new TokenizerModel(new
		 * File(TOKEN_MODEL_FILE)); tokenizer = new TokenizerME(model);
		 * 
		 * } catch (IOException e) {
		 * System.err.println("Sentence model load failed."); throw new
		 * ServletException(e); }
		 */
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter()
				.println("Hello from finnish-dep-parser server. Post Finnish text to this URL and get CoNLL-U back.");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		log("START");
		long startTime = System.currentTimeMillis();
		req.setCharacterEncoding(StandardCharsets.UTF_8.name());

		// TODO: convert scripts to this servlet
		// TODO: use input and outputstreams directly without files

		// read input to string
		BufferedReader br = req.getReader();
		String line = br.readLine();
		StringBuilder sb = new StringBuilder();
		while (line != null) {
			log(line);
			sb.append(line);
			line = br.readLine();
			// add space after each line
			// so this removes all new lines from input text
			// sb.append(" ");
			// or add new line so that text is like incoming text
			sb.append("\n");
		}
		br.close();

		// TODO: add opennlp to this servlet
		// detect sentences
		
		/*
		 * String[] sentences = sentenceDetector.sentDetect(sb.toString());
		 * //tokenize sb=new StringBuilder(); for (String sentence : sentences)
		 * { sb.append(tokenizer.) }
		 */
		Path tmpDir = null;
		int rv = -1;
		String errorString = "";
		try {
			//TODO: multithreading
			if (lock.tryAcquire(1, waitTimeForLockInSeconds, TimeUnit.SECONDS)) {
				try {
					// create tmpDir for this request
					tmpDir = Files.createTempDirectory(workDir, "tmp_data");
					// call parser
					rv = callParserProcess(sb.toString(), tmpDir);

				} finally {
					lock.release();
				}
			}
		} catch (InterruptedException e) {
			errorString = e.toString();
			e.printStackTrace();
			rv = -234566;
		}

		resp.setContentType("text/plain");
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

		PrintWriter pw = resp.getWriter();
		if (rv == -234566) {
			// error when executing this servlet
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			pw.println("Waiting for lock interrupted.");
			pw.println(errorString);
		} else {
			// read output file
			File f;
			if (rv == 0) {
				resp.setStatus(HttpServletResponse.SC_OK);
				// if success, read stdout file
				f = new File(tmpDir.toFile(), outputFileName);

			} else {
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				// if error, read stderr file
				f = new File(tmpDir.toFile(), errorFileName);
			}

			br = new BufferedReader(new FileReader(f));
			for (line = br.readLine(); line != null; line = br.readLine()) {
				pw.println(line);
			}
			br.close();

			// delete temp dir
			try {
				if (tmpDir != null) {
					FileUtils.deleteDirectory(tmpDir.toFile());
				}

			} catch (IOException ioe) {
				// tmpdir delete failed
				log("Temp dir delete failed: " + ioe.toString());
			}
		}
		long endTime = System.currentTimeMillis();

		double elapsedTime = (endTime - startTime) / 1000.0;
		log("END " + elapsedTime + " seconds");
		log("");

	}

	private int callParserProcess(String inputText, Path tmpDir) throws IOException {
		// calls parser_wrapper.sh script

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
		env.put("PATH", env.get("PATH") + ":" + workDir + "/LIBS/apache-opennlp-1.5.3/bin/:" + workDir + "/LIBS/");

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

}