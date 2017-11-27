package findep;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.output.StringBuilderWriter;

import findep.is2.Parser;

public class IS2ParserServlet extends SuperServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static String MODEL_PARSER = "model/parser.model";

	private Parser parser = null;

	@Override
	public void init() throws ServletException {
		super.init();
		SYSOUTLOGGER.sysout(-1, "Initializing " + getClass().getName());

		// init parser
		parser = new Parser(MODEL_PARSER);
		try {
			// load model
			SYSOUTLOGGER.sysout(-1, String.format("Loading %s...", MODEL_PARSER));
			parser.loadModel();

			// do initial parse to do final init of parser
			BufferedReader br = new BufferedReader(new StringReader("1\thei\thei\t_\t_\t_\t_\t_\t_\t_\t_\t_\t_"));
			BufferedWriter sbw = new BufferedWriter(new StringBuilderWriter());
			parser.parse(br, sbw);

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
			// reads requst input to parser and parser writes output to response
			BufferedReader br = req.getReader();
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
			log("Parsing failed.", e);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		} finally {
			resp.flushBuffer();
		}

	}

}