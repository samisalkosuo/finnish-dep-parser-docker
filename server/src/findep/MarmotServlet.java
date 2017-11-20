package findep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import findep.marmot.Annotator;

/*
 * Replaces marmot annotator java subprocess in marmot-tag.py
 * Replaces call to marmot.morph.cmd.Annotator class
 */
public class MarmotServlet extends SuperServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static String MODEL_MARMOT = "model/fin_model.marmot";

	private Annotator annotator = null;

	private Map<String, Integer> wordCounts = new HashMap<String, Integer>();

	@Override
	public void init() throws ServletException {
		super.init();
		SYSOUTLOGGER.sysout(-1,"Initializing " + getClass().getName());
		SYSOUTLOGGER.sysout(-1,String.format("Loading model %s...",MODEL_MARMOT));

		// load models
		annotator = new Annotator(MODEL_MARMOT);

		SYSOUTLOGGER.sysout(-1,"Reading word counts from vocab-fi...");
		String file = "word_counts.csv.zip";
		long wordCount = 0;
		long totalCount=0;
		try {
			//read file from zip
			//https://stackoverflow.com/a/26257086
			// outputName, name of the file to extract from the zip file
			String csvFileName= "word_counts.csv";
			// location to store the extracted file to
			File csvFile = new File(csvFileName);
			// path to the zip file
			Path zipFile = Paths.get(file);
			// load zip file as filesystem
			FileSystem fileSystem = FileSystems.newFileSystem(zipFile, null);
			// copy file from zip file to output location
			Path source = fileSystem.getPath(csvFileName);
			Files.copy(source, csvFile.toPath());
			
			BufferedReader br = new BufferedReader(new FileReader(csvFile));

			String line = br.readLine();
			while (line != null) {
				wordCount=wordCount+1;
				String[] items = line.split(",");
				int count=Integer.parseInt(items[1]);
				totalCount=totalCount+count;
				wordCounts.put(items[0], count);
				if (wordCount % 500000 == 0) {
					SYSOUTLOGGER.sysout(-1,String.format("Words: %d, Counts: %d",wordCount,totalCount));
				}				
				line = br.readLine();
			}
			SYSOUTLOGGER.sysout(-1,String.format("Words: %d, Counts: %d",wordCount,totalCount));
			br.close();
		} catch (Exception e) {
			throw new ServletException(e);			
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding(StandardCharsets.UTF_8.name());

		String output = "OK";
		String getWordCount = req.getParameter("wordCount");
		if (getWordCount != null) {
			Integer c = wordCounts.get(getWordCount);
			if (c == null) {
				output = "0";
			} else {
				output = Integer.toString(c);
			}
			//log(String.format("Word: %s, count: %s",getWordCount,output));
			
		} else {

			// call HfstOptimizedLookupObj
			// and modify omorfi_wrapper.py and remove java process
			// and change lookup to call this servlet using GET
			String predFile = req.getParameter("predfile");
			String testFile = req.getParameter("testfile");

			if (predFile == null || testFile == null) {
				output = "Missing parameters: testfile and/or predfile";
			} else {
				try {
					annotator.annotate(predFile, testFile);
				} catch (IOException ioe) {
					output = ioe.toString();
				}

			}
		}

		resp.setContentType("text/plain");
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().println(output);
	}

}