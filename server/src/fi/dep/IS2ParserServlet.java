package fi.dep;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.output.StringBuilderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.dep.is2.Parser;

public class IS2ParserServlet extends SuperServlet {

	private Logger logger = LoggerFactory.getLogger(IS2ParserServlet.class);

	private static final long serialVersionUID = 1L;

	private final static String MODEL_PARSER = "model/parser.model";

	private Parser parser = null;

	@Override
	public void init() throws ServletException {
		super.init();
		logger.info("Initializing...");

		// init parser
		parser = new Parser(MODEL_PARSER);
		try {
			// load model
			logger.info(String.format("Loading {}...", MODEL_PARSER));
			parser.loadModel();

			// do initial parse to do final init of parser
			BufferedReader br = new BufferedReader(new StringReader("1\thei\thei\t_\t_\t_\t_\t_\t_\t_\t_\t_\t_"));
			BufferedWriter sbw = new BufferedWriter(new StringBuilderWriter());
			parser.parse(br, sbw);

			logger.info("Initializing... Done.");
		} catch (Exception e) {
			throw new ServletException(e);
		}

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().println("Hello from servlet. GET not supported.");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		req.setCharacterEncoding(StandardCharsets.UTF_8.name());

		resp.setContentType("text/plain");
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.setStatus(HttpServletResponse.SC_OK);

		try {
			//read input to string
			String inputText=readInputStreamToString(req.getInputStream());
			
			//#                             1  2    3     4      5   6    7    8     9    10    11     12      13      14  
			//for colIdx,col in enumerate("ID FORM LEMMA PLEMMA POS PPOS FEAT PFEAT HEAD PHEAD DEPREL PDEPREL FILLPRED PRED".split()):
			//replaces $PYTHON conllUtil.py --swap LEMMA:=PLEMMA,POS:=PPOS,FEAT:=PFEAT
			//in tag.sh
			StringBuilder sbi=new StringBuilder();
			for (String line : inputText.split("\n")) {
				String[] cols = line.split("\t");
				if (cols.length >= 11) {
					cols[2] = cols[3];
					cols[4] = cols[5];
					cols[6] = cols[7];
					sbi.append(String.join("\t", cols));
				}
				sbi.append("\n");
			}
			inputText=sbi.toString();
			
			BufferedReader br = new BufferedReader(new StringReader(inputText));
			//BufferedReader br = req.getReader();
			BufferedWriter bw = new BufferedWriter(resp.getWriter());

			StringBuilder sb = new StringBuilder();
			BufferedWriter bw2 = new BufferedWriter(new StringBuilderWriter(sb));
			parser.parse(br, bw2);

			// replaced $PYTHON conllUtil.py --swap HEAD:=PHEAD,DEPREL:=PDEPREL
			// in my_parser_wrapper.sh
			String text = sb.toString();
			for (String line : text.split("\n")) {
				String[] cols = line.split("\t");
				if (cols.length >= 11) {
					// HEAD=column 8
					// PHEAD=column 9
					// DEPREL=column 10
					// PDEPREL=column 11
					cols[8] = cols[9];
					cols[10] = cols[10];
					bw.write(String.join("\t", cols));
				}
				bw.write("\n");
			}

			// System.out.println(text);
			// bw.write(text);
			bw.flush();

		} catch (Exception e) {
			logger.error("Parsing failed.", e);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		} finally {
			resp.flushBuffer();
		}

	}

}