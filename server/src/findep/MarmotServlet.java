package findep;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import findep.marmot.Annotator;

/*
 * Replaces marmot annotator java subprocess in marmot-tag.py
 * Replaces call to marmot.morph.cmd.Annotator class
 */
public class MarmotServlet  extends HttpServlet {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static String MODEL_MARMOT="model/fin_model.marmot";
	
	private Annotator annotator=null;
	
	@Override
	public void init() throws ServletException {
		super.init();
		log("Initializing "+getClass().getName());

		//load models
		annotator=new Annotator(MODEL_MARMOT);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding(StandardCharsets.UTF_8.name());

		
		//call HfstOptimizedLookupObj
		//and modify omorfi_wrapper.py and remove java process
		//and change lookup to call this servlet using GET
		String predFile=req.getParameter("predfile");
		String testFile=req.getParameter("testfile");
		
		String output="OK";
		if (predFile==null || testFile==null)
		{
			output="Missing parameters: testfile and/or predfile";
		}
		else
		{
			try
			{
				annotator.annotate(predFile, testFile);			
			}
			catch (IOException ioe)
			{
				output=ioe.toString();
			}
			
		}
		
		resp.setContentType("text/plain");
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().println(output);
	}


}