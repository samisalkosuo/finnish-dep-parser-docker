package fi.wordnet;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fi.dep.SuperServlet;
import fi.dep.utils.cache.MyCache;
import fi.wordnet.IWordNet.HYPERNYM_FORMAT;
import fi.wordnet.IWordNet.SENSES_TO_RETURN;

public class FinWordNetServlet extends SuperServlet {

	private static final long serialVersionUID = 1L;

	private IWordNet wordnet = null;
	private MyCache CACHE = MyCache.getInstance();

	private SENSES_TO_RETURN sensesToReturnDefaultValue = SENSES_TO_RETURN.L;

	@Override
	public void init() throws ServletException {
		super.init();

		SYSOUTLOGGER.sysout(-1, "Initializing " + getClass().getName());

		// env variable to get senses
		String _sensesToReturn = System.getenv("fwn_senses_to_return");
		if (_sensesToReturn != null) {
			try {
				sensesToReturnDefaultValue = SENSES_TO_RETURN.valueOf(_sensesToReturn);
			} catch (IllegalArgumentException iae) {
				// if unrecognized value, set default
				sensesToReturnDefaultValue = SENSES_TO_RETURN.L;
			}
		}

		wordnet = WordNetFI.getInstance();

	}

	@Override
	public void destroy() {
		super.destroy();
		wordnet.destroy();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// TODO:
		// add some help if using without or wrong parameters
		// add senses-parameter and for each function some default
		// refactor this code

		req.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.setContentType("text/plain");
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		PrintWriter pw = resp.getWriter();

		String queryString = req.getQueryString();
		if (queryString == null) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			pw.println("No parameters.");
			pw.flush();
			return;
		}

		String function = req.getParameter("function");

		if (function == null) {
			function = "hypernymjson";
		}
		function = function.toLowerCase();

		// get senses to return from request
		// A=all, F=first, L=last
		SENSES_TO_RETURN sensesToReturn = sensesToReturnDefaultValue;
		String sensestoreturn = req.getParameter("senses");
		if (sensestoreturn != null) {
			if (sensestoreturn.equalsIgnoreCase("L")) {
				sensesToReturn = SENSES_TO_RETURN.L;
			}
			if (sensestoreturn.equalsIgnoreCase("A")) {
				sensesToReturn = SENSES_TO_RETURN.A;
			}
			if (sensestoreturn.equalsIgnoreCase("F")) {
				sensesToReturn = SENSES_TO_RETURN.F;
			}
		}

		String word = req.getParameter("word");
		if (word == null) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			pw.println("Parameter 'word' missing.");
		} else {
			String outputString = null;
			String upostag = req.getParameter("upostag");

			// cache key is full query string
			outputString = CACHE.get(queryString);
			if (outputString == null) {

				if (upostag == null) {
					upostag = "NOUN";
				}
				if (function.equals("synonyms")) {
					SYSOUTLOGGER.sysout(2, "Get synonyms for word: " + word);
					// String partofspeech = "NOUN";// req.getParameter("pos");
					outputString = wordnet.getSynonyms(word, upostag);

				}
				if (function.equals("hypernym")) {
					HYPERNYM_FORMAT format = HYPERNYM_FORMAT.JSON;
					if (function.equals("hypernymcsv")) {
						format = HYPERNYM_FORMAT.CSV;
					}
					SYSOUTLOGGER.sysout(2, "Get hypernyms for word: " + word);
					String partofspeech = "NOUN";// req.getParameter("pos");
					List<String> hypernyms = wordnet.getHypernymStrings(word, partofspeech, format, sensesToReturn);

					outputString = String.join("\n", hypernyms);
				}

				if (function.equals("hypernymsenses")) {
					SYSOUTLOGGER.sysout(2, "Get hypernyms with senses for word: " + word);
					String partofspeech = "NOUN";// req.getParameter("pos");
					String hypernymsWithSenses = CACHE.get(queryString);// cache
																		// key
																		// is
																		// full
																		// query
																		// string
					if (hypernymsWithSenses == null) {
						List<String> hypernyms;
						try {
							hypernyms = wordnet.getHypernymStringsWithSenses(word, partofspeech);
							if (hypernyms != null) {
								hypernymsWithSenses = String.join("\n", hypernyms);
								CACHE.put(queryString, hypernymsWithSenses);
							}
						} catch (Exception e) {
							SYSOUTLOGGER.sysout(-1, "ERROR: Get hypernyms with senses for word: " + word);
							e.printStackTrace();

						}
					}
					outputString = hypernymsWithSenses;

				}

				// add to cache
				if (outputString != null && outputString.equals("null")) {
					CACHE.put(queryString, outputString);
				}
			} else {
				outputString = CACHE.get(queryString);
			}

			
			if (outputString == null) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				pw.println("null");
			} else {
				resp.setStatus(HttpServletResponse.SC_OK);
				pw.print(outputString);
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
