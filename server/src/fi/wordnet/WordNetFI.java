package fi.wordnet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.text.StringEscapeUtils;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.dictionary.Dictionary;

public class WordNetFI implements IWordNet {

	// private SystemOutLogger SYSOUTLOGGER = SystemOutLogger.getInstance();

	private static IWordNet wordnetFI = null;

	private Dictionary dict;

	private WordNetFI() {
	}

	public static IWordNet getInstance() {
		if (wordnetFI == null) {
			wordnetFI = new WordNetFI();
			wordnetFI.init();
		}
		return wordnetFI;
	}

	public static void main(String[] args) {
		IWordNet wordnet = WordNetFI.getInstance();
		String word = "kaupunki";// "sitoumus";
		System.out.println(wordnet.getSynonyms(word, "NOUN"));
		System.out.println(wordnet.getHypernymStrings(word, "NOUN", HYPERNYM_FORMAT.CSV, SENSES_TO_RETURN.A));
	}

	@Override
	public void init() {
		try {
			// wordnet path in docker image
			String dictPath = "/Finnish-dep-parser/finwordnet/net/sf/extjwnl/data/finwordnet/2.0";
			// wordnet path in dev environment
			// dictPath =
			// "c:/Dropbox/git/finnish-dep-parser-docker/server/resources/net/sf/extjwnl/data/finwordnet/2.0";
			// dict = Dictionary.getDefaultResourceInstance();
			dict = Dictionary.getFileBackedInstance(dictPath);
		} catch (JWNLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {

		if (dict != null) {
			try {
				dict.close();
			} catch (JWNLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public List<String> getHypernymStringsWithSenses(String _word, String partofspeech)
			throws Exception {

		POS pos = getPOS(partofspeech);
		List<String> hypernymStringsWithSenses = new Vector<String>();
		if (pos != null) {

			IndexWord word = dict.getIndexWord(pos, _word);

			if (word != null) {
				List<Synset> senses = word.getSenses();

				int senseIndex = 0;
				for (Synset sense : senses) {
					PointerTargetTree senseHypernyms = PointerUtils
							.getHypernymTree(sense);
					List<PointerTargetNodeList> n1 = senseHypernyms.toList();

					int hypernymLevelIndex = 0;
					for (PointerTargetNodeList ptnl : n1) {
						// can include more than 1 sense
						Iterator<PointerTargetNode> ptni = ptnl.iterator();
						while (ptni.hasNext()) {
							PointerTargetNode ptn = ptni.next();

							List<Word> words = ptn.getSynset().getWords();
							int size = words.size();
							for (int i = 0; i < size; i++) {
								String lemma = words.get(i).getLemma();
								hypernymStringsWithSenses.add(senseIndex + ","
										+ hypernymLevelIndex + "," + lemma);
							}
							hypernymLevelIndex = hypernymLevelIndex + 1;

						}

					}
					senseIndex = senseIndex + 1;
				}
			}

		}

		return hypernymStringsWithSenses;
	}
	
	@Override
	public List<String> getHypernymStrings(String _word, String partofspeech, HYPERNYM_FORMAT format,
			SENSES_TO_RETURN sensesToReturn) {
		// public List<String> getHypernymJSONs(String _word, String
		// partofspeech) {
		List<List<List<String>>> allHypernyms = null;
		List<String> hypernymJSONs = null;

		POS pos = getPOS(partofspeech);
		if (pos != null) {

			try {

				IndexWord word = dict.getIndexWord(pos, _word);

				if (word != null) {
					List<Synset> senses = word.getSenses();
					allHypernyms = new ArrayList<List<List<String>>>();
					int sensesSize = senses.size();
					switch (sensesToReturn) {
					case L:
						// Get only last sense
						if (sensesSize > 1) {
							senses = senses.subList(sensesSize - 1, sensesSize);
						}
						break;
					case F:
						// Get only first sense
						if (sensesSize > 1) {
							senses = senses.subList(0, 1);
						}
					default:
						// Get all senses
						break;

					}

					for (Synset sense : senses) {
						// System.out.println(sense);
						PointerTargetTree senseHypernyms = PointerUtils.getHypernymTree(sense);
						List<PointerTargetNodeList> n1 = senseHypernyms.toList();

						// List<List<String>> hypernymLevels = new
						// ArrayList<List<String>>();
						// System.out.println("");
						// System.out.println("sense...");
						// System.out.println("");

						for (PointerTargetNodeList ptnl : n1) {
							// can include more than 1 sense
							List<List<String>> hypernymLevels = new ArrayList<List<String>>();

							// System.out.println(ptnl);
							// System.out.println();
							Iterator<PointerTargetNode> ptni = ptnl.iterator();
							while (ptni.hasNext()) {
								PointerTargetNode ptn = ptni.next();

								List<Word> words = ptn.getSynset().getWords();
								int size = words.size();
								// System.out.println(words.size());
								List<String> synonymsForHypernymLevel = new ArrayList<String>();
								// System.out.println("hypernymlevel...");
								for (int i = 0; i < size; i++) {
									String lemma = words.get(i).getLemma();
									// System.out.println(lemma);
									/*
									 * ; try { System.out.println(new
									 * String(lemma.getBytes("UTF-8"))); } catch
									 * (UnsupportedEncodingException e) { //
									 * TODO Auto-generated catch block
									 * e.printStackTrace(); }
									 * System.out.println();
									 */
									synonymsForHypernymLevel.add(lemma);
									/*
									 * System.out.print(lemma); if (i < (size -
									 * 1)) { System.out.print(","); } else {
									 * System.out.println(); }
									 */
								}
								hypernymLevels.add(synonymsForHypernymLevel);

							}

							allHypernyms.add(hypernymLevels);
						}

					}
				}
				if (allHypernyms != null) {
					hypernymJSONs = generateHypernymList(allHypernyms, format);
				}

			} catch (JWNLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return hypernymJSONs;
	}

	@Override
	public String getSynonyms(String _word, String partofspeech) {
		List<String> synonyms = new ArrayList<String>();
		String synonymString = null;
		POS pos = getPOS(partofspeech);
		if (pos != null) {

			try {

				IndexWord word = dict.getIndexWord(pos, _word);

				if (word != null) {
					List<Synset> senses = word.getSenses();
					/*
					 * int sensesSize = senses.size(); switch (sensesToReturn) {
					 * case L: // Get only last sense if (sensesSize > 1) {
					 * senses = senses.subList(sensesSize - 1, sensesSize); }
					 * break; case F: // Get only first sense if (sensesSize >
					 * 1) { senses = senses.subList(0, 1); } default: // Get all
					 * senses break;
					 * 
					 * }
					 */
					for (Synset sense : senses) {
						// System.out.println(sense);
						PointerTargetTree senseHypernyms = PointerUtils.getHypernymTree(sense);

						List<PointerTargetNodeList> n1 = senseHypernyms.toList();

						PointerTargetNode ptn = n1.get(0).getFirst();

						List<Word> words = ptn.getSynset().getWords();
						int size = words.size();

						for (int i = 0; i < size; i++) {
							String lemma = words.get(i).getLemma();
							if (!synonyms.contains(lemma)) {
								synonyms.add(lemma);
							}

						}
					}
				}
				if (synonyms.size() > 0) {
					Collections.reverse(synonyms);
					synonymString = String.join(",", synonyms);
				}

			} catch (JWNLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return synonymString;
	}

	private POS getPOS(String posString) {
		POS pos = null;

		// set POSses to enable for concept discovery

		if (posString.equals("NOUN")) {
			pos = POS.NOUN;
		}

		if (posString.equals("ADJ")) {
			pos = POS.ADJECTIVE;
		}
		/*
		 * if (posString.equals("VERB")) { pos = POS.VERB; }
		 * 
		 * 
		 * 
		 * if (posString.equals("ADV")) { pos = POS.ADVERB; }
		 */
		return pos;
	}

	private List<String> generateHypernymList(List<List<List<String>>> hypernyms, HYPERNYM_FORMAT format) {
		List<String> allConceptJSONs = null;
		try {
			allConceptJSONs = new ArrayList<String>();
			for (List<List<String>> senses : hypernyms) {
				String parent = "$Root";
				int hypernymLevel = 0;
				for (int i = senses.size() - 1; i >= 0; i--) {
					// System.out.println("hypernymLevel: "+hypernymLevel);
					// loop all hypernyms of each sense
					// start from end because ConceptDiscovery UI show level 0
					// as highest level concept

					String child = String.join(",", senses.get(i));
					int leaf = i == 0 ? 1 : 0;
					// parent=StringEscapeUtils.escapeJson(parent);
					// child=StringEscapeUtils.escapeJson(child);

					// extract synonyms from parent and child
					// =>just one parent and one child, UI takes only first
					// synonym
					for (String p : parent.split(",")) {
						for (String c : senses.get(i)) {

							// note: pos: "n" in JSON means NOUN ==> UI supports
							// only
							// NOUN by default

							StringBuilder sb = new StringBuilder();
							if (format == HYPERNYM_FORMAT.JSON) {
								sb.append(hypernymLevel);
								sb.append("#");

								sb.append("{\"parent\":\"");
								String _p = new String(p.getBytes("UTF-8"));
								_p = StringEscapeUtils.escapeJson(_p);
								sb.append(_p);
								sb.append("\",\"pos\":\"n\",\"child\":\"");
								String _c = new String(c.getBytes("UTF-8"));
								_c = StringEscapeUtils.escapeJson(_c);
								sb.append(_c);
								sb.append("\",\"leaf\":");
								sb.append(leaf);
								sb.append("}");
							}
							if (format == HYPERNYM_FORMAT.CSV) {
								sb.append(hypernymLevel);
								sb.append(",");
								// String _p = new String(p.getBytes("UTF-8"));
								// sb.append(_p);
								// sb.append(",");
								// String _c = new String(,);

								sb.append(c);
								sb.append(",");
								sb.append(leaf);
							}
							String conceptString = sb.toString();
							// "{\"parent\":\"" + parent + "\",\"pos\":\"n\"" +
							// //
							// Parent
							// ",\"child\":\"" + child + "\"," + // Concept or
							// // Hypernym
							// "\"leaf\":" + leaf + "}";

							if (!allConceptJSONs.contains(conceptString)) {
								// System.out.println(" "+conceptString);
								allConceptJSONs.add(conceptString);
							}
						}
					}

					parent = child;
					hypernymLevel = hypernymLevel + 1;
				}

			}

		} catch (Exception e) {
			// catch all errors
			e.printStackTrace();
		}
		return allConceptJSONs;
	}

}
