package finwordnet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;

import findep.utils.SystemOutLogger;
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

	private SystemOutLogger SYSOUTLOGGER = SystemOutLogger.getInstance();

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
		System.out.println(wordnet.getHypernymJSONs("saalimäärä", "NOUN"));
	}

	@Override
	public void init() {
		try {
			dict = Dictionary.getDefaultResourceInstance();
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public List<String> getHypernymJSONs(String _word, String partofspeech) {
		List<List<List<String>>> allHypernyms = null;
		List<String> hypernymJSONs = null;

		POS pos = getPOS(partofspeech);
		if (pos != null) {

			try {
				IndexWord word = dict.getIndexWord(pos, _word);
				if (word != null) {
					List<Synset> senses = word.getSenses();
					allHypernyms = new ArrayList<List<List<String>>>();

					for (Synset sense : senses) {
						PointerTargetTree senseHypernyms = PointerUtils.getHypernymTree(sense);
						List<PointerTargetNodeList> n1 = senseHypernyms.toList();
						List<List<String>> hypernymLevels = new ArrayList<List<String>>();

						for (PointerTargetNodeList ptnl : n1) {
							// System.out.println(ptnl);
							// System.out.println();
							Iterator<PointerTargetNode> ptni = ptnl.iterator();
							while (ptni.hasNext()) {
								PointerTargetNode ptn = ptni.next();

								List<Word> words = ptn.getSynset().getWords();
								int size = words.size();
								List<String> synonymsForHypernymLevel = new ArrayList<String>();

								for (int i = 0; i < size; i++) {
									String lemma = words.get(i).getLemma();
									/*
									 * System.out.println(lemma); try {
									 * System.out.println(new
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

						}
						allHypernyms.add(hypernymLevels);

					}
				}
				if (allHypernyms != null) {
					hypernymJSONs = generateHypernymAnnotations(allHypernyms);
				}

			} catch (JWNLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return hypernymJSONs;
	}

	private POS getPOS(String posString) {
		POS pos = null;

		// set POSses to enable for concept discovery

		if (posString.equals("NOUN")) {
			pos = POS.NOUN;
		}
		/*
		 * if (posString.equals("VERB")) { pos = POS.VERB; }
		 * 
		 * if (posString.equals("ADJ")) { pos = POS.ADJECTIVE; }
		 * 
		 * if (posString.equals("ADV")) { pos = POS.ADVERB; }
		 */
		return pos;
	}

	private List<String> generateHypernymAnnotations(List<List<List<String>>> hypernyms) {
		List<String> allConceptJSONs = null;
		try {
			allConceptJSONs = new ArrayList<String>();
			for (List<List<String>> senses : hypernyms) {
				String parent = "$Root";
				int hypernymLevel = 0;
				for (int i = senses.size() - 1; i >= 0; i--) {
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

							String conceptJSON = sb.toString();
							// "{\"parent\":\"" + parent + "\",\"pos\":\"n\"" +
							// //
							// Parent
							// ",\"child\":\"" + child + "\"," + // Concept or
							// // Hypernym
							// "\"leaf\":" + leaf + "}";

							if (!allConceptJSONs.contains(conceptJSON)) {
								allConceptJSONs.add(conceptJSON);
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
