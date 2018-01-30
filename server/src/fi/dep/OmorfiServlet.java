package fi.dep;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.hfst.HfstOptimizedLookupObj;

/*
 * Replaces hsft-process in omorfi_wrapper.py
 * 
 */
public class OmorfiServlet extends SuperServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static String MODEL_MORPHOLOGY = "model/morphology.finntreebank.hfstol";
	private final static String MODEL_GENERATION = "model/generation.finntreebank.hfstol";

	private HfstOptimizedLookupObj hfst_morphology = null;
	private HfstOptimizedLookupObj hfst_generation = null;

	@Override
	public void init() throws ServletException {
		super.init();
		SYSOUTLOGGER.sysout(-1,"Initializing "+getClass().getName());

		// load models
		try {
			SYSOUTLOGGER.sysout(-1,String.format("Loading %s...", MODEL_MORPHOLOGY));
			hfst_morphology = new HfstOptimizedLookupObj(MODEL_MORPHOLOGY);
			SYSOUTLOGGER.sysout(-1,String.format("Loading %s...", MODEL_GENERATION));
			hfst_generation = new HfstOptimizedLookupObj(MODEL_GENERATION);
		} catch (Exception e) {
			log("Init failed: " + e.toString(),e);
			throw new ServletException(e);

		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding(StandardCharsets.UTF_8.name());

		String model = req.getParameter("model");
		String word = req.getParameter("word");
		String output = "";

		if (model == null || word == null) {
			output = "Missing parameters: model and/or word";
		} else {

			if (model.equals("M")) {
				output = hfst_morphology.runTransducer(word);
			}
			if (model.equals("G")) {
				output = hfst_generation.runTransducer(word);
			}
		}
		resp.setContentType("text/plain");
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().println(output);
	}

}