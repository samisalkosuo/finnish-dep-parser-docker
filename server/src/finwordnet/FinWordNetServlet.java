package finwordnet;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import findep.SuperServlet;
import findep.utils.StringUtils;
import findep.utils.cache.MyCache;

public class FinWordNetServlet extends SuperServlet {

	private static final long serialVersionUID = 1L;

	private IWordNet wordnet = null;
	private MyCache CACHE = MyCache.getInstance();

	@Override
	public void init() throws ServletException {
		super.init();

		SYSOUTLOGGER.sysout(-1, "Initializing " + getClass().getName());

		wordnet = WordNetFI.getInstance();

	}

	@Override
	public void destroy() {
		super.destroy();
		wordnet.destroy();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String word = req.getParameter("word");
		resp.setContentType("text/plain");
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		PrintWriter pw = resp.getWriter();
		if (word == null) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			pw.println("Parameter 'word' missing.");
		} else {

			SYSOUTLOGGER.sysout(2, "Get hypernyms for word: " + word);
			String partofspeech = "NOUN";// req.getParameter("pos");
			String hypernymJSON = CACHE.get(word);

			if (hypernymJSON == null) {
				List<String> hypernyms = wordnet.getHypernymJSONs(word, partofspeech);
				if (hypernyms != null) {
					hypernymJSON = String.join("\n", hypernyms);
					CACHE.put(word, hypernymJSON);
				}
			}
			if (hypernymJSON == null) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				pw.println("null");
			} else {
				resp.setStatus(HttpServletResponse.SC_OK);
				pw.print(hypernymJSON);
				pw.flush();

			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain");
		resp.setStatus(HttpServletResponse.SC_OK);
		PrintWriter pw = resp.getWriter();
		pw.println("POST is not supported.");
		pw.println("");
	}

}
