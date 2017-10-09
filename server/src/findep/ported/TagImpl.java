package findep.ported;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.*;

import org.apache.commons.math.util.MultidimensionalCounter.Iterator;

import net.sf.hfst.HfstOptimizedLookupObj;

import marmot.core.Tagger;
import marmot.morph.MorphTagger;
import marmot.morph.Sentence;
import marmot.morph.Word;



public class TagImpl implements Tag {

	ParserLog logger = null;
	MorphTagger tagger = null;
	
	HfstOptimizedLookupObj hfst_morphology = null;
	
	public TagImpl(ParserLog logIn, HfstOptimizedLookupObj morphoIn,MorphTagger taggerIn) {
		logger = logIn;
		hfst_morphology = morphoIn;
		tagger = taggerIn;
	}
	

	
	// This methods will take a column and sort unique words
	public Set sortUnique(String input) {

		StringTokenizer st = new StringTokenizer(input,"\n",false);

		Set<String> words = new TreeSet<String>();
		
		while(st.hasMoreTokens()) {
			String lause = st.nextToken();			
			// lets just add comments and empty lines
			if(lause != null && ("".equals(lause.trim()) || "#".equals(lause.substring(0, 1)))) {
				logger.debug("skipping line:"+lause);
			} else {
				// then we want to split this lause into words
				StringTokenizer wordt = new StringTokenizer(lause,"\t",false);
				
				String First = wordt.nextToken();
				String Second = wordt.nextToken();
				if(Second != null && !"".equals(Second)) {
					// Looks like we need to keep the order
					words.add(Second.trim());
				}	
			}
		}
		
		// debug
		java.util.Iterator<String> i = words.iterator();
		while(i.hasNext()) {
			logger.debug(""+i.next());
		}
		return words;
	}
	
	@Override
	public Map allReadings(String input) {
		// TODO Auto-generated method stub
		return null;
		
		/*

osboxes@osboxes:~/koodi/finnish-dep-parser-docker/server$ time curl 'http://localhost:8080/omorfi?model=M&word=Hylkää'
Hylkää	hylätä<V><Act><Ind><Prs><Sg3><Cap>	0.0
Hylkää	hylätä<V><Act><Imprt><Sg2><Cap>	0.0
Hylkää	hylätä<V><Ind><Prs><ConNeg><Cap>	0.0
Hylkää	hyljätä<V><Act><Ind><Prs><Sg3><Cap>	0.0
Hylkää	hyljätä<V><Act><Imprt><Sg2><Cap>	0.0
Hylkää	hyljätä<V><Ind><Prs><ConNeg><Cap>	0.0


ud format:

,       ,       Punct   _

.       .       Punct   _

Hän     hän     Pron    SUBCAT=Pers|NUM=Sg|CASE=Nom|CASECHANGE=Up

hänelle hän     Pron    SUBCAT=Pers|NUM=Sg|CASE=All

hylkää  hyljätä V       PRS=Sg2|VOICE=Act|MOOD=Imprt
hylkää  hyljätä V       PRS=Sg3|VOICE=Act|TENSE=Prs|MOOD=Ind
hylkää  hyljätä V       TENSE=Prs|MOOD=Ind|NEG=ConNeg
hylkää  hylätä  V       PRS=Sg2|VOICE=Act|MOOD=Imprt
hylkää  hylätä  V       PRS=Sg3|VOICE=Act|TENSE=Prs|MOOD=Ind
hylkää  hylätä  V       TENSE=Prs|MOOD=Ind|NEG=ConNeg

ilkamoi ilkamoida       V       PRS=Sg2|VOICE=Act|MOOD=Imprt
ilkamoi ilkamoida       V       PRS=Sg3|VOICE=Act|TENSE=Prt|MOOD=Ind
ilkamoi ilkamoida       V       TENSE=Prs|MOOD=Ind|NEG=ConNeg

sd format:

,       ,       PUNCT   _

.       .       PUNCT   _

Hän     hän     PRON    Case=Nom|Number=Sing|Person=3|PronType=Prs

hänelle hän     PRON    Case=All|Number=Sing|Person=3|PronType=Prs

hylkää  hyljätä VERB    Mood=Imp|Number=Sing|Person=2|VerbForm=Fin|Voice=Act
hylkää  hyljätä VERB    Mood=Ind|Number=Sing|Person=3|Tense=Pres|VerbForm=Fin|Voice=Act
hylkää  hyljätä VERB    Connegative=Yes|Mood=Ind|Tense=Pres|VerbForm=Fin
hylkää  hylätä  VERB    Mood=Imp|Number=Sing|Person=2|VerbForm=Fin|Voice=Act
hylkää  hylätä  VERB    Mood=Ind|Number=Sing|Person=3|Tense=Pres|VerbForm=Fin|Voice=Act
hylkää  hylätä  VERB    Connegative=Yes|Mood=Ind|Tense=Pres|VerbForm=Fin

ilkamoi ilkamoida       VERB    Mood=Imp|Number=Sing|Person=2|VerbForm=Fin|Voice=Act
ilkamoi ilkamoida       VERB    Mood=Ind|Number=Sing|Person=3|Tense=Past|VerbForm=Fin|Voice=Act
ilkamoi ilkamoida       VERB    Connegative=Yes|Mood=Ind|Tense=Pres|VerbForm=Fin



 */
		
		
	}

	public void marmot(String details) {

		/* MARMOT IN
 Väinämöinen     POS_ADJ#POS_PROPN
lähettää        POS_VERB
taikakeinoin    POS_NOUN
Ilmarisen       POS_ADJ#POS_PROPN
vastoin POS_ADP#POS_NOUN#POS_VERB
tämän   POS_PRON
tahtoa  POS_NOUN#POS_VERB
Pohjolaan       POS_NOUN#POS_PROPN
.       POS_PUNCT

Ilmarinen       POS_ADJ#POS_PROPN
takoo   POS_VERB
sammon  POS_NOUN
.       POS_PUNCT

Louhi   POS_NOUN#POS_VERB
sulkee  POS_VERB
sen     POS_ADV#POS_PRON
kivimäkeen      POS_NOUN
.       POS_PUNCT

		 */
		/*
		tagger_input.conll09
		
		1       Väinämöinen     _       _       _       _       _       _       _       _       _       _       _       _
2       lähettää        _       _       _       _       _       _       _       _       _       _       _       _
3       taikakeinoin    _       _       _       _       _       _       _       _       _       _       _       _
4       Ilmarisen       _       _       _       _       _       _       _       _       _       _       _       _
5       vastoin _       _       _       _       _       _       _       _       _       _       _       _
6       tämän   _       _       _       _       _       _       _       _       _       _       _       _
7       tahtoa  _       _       _       _       _       _       _       _       _       _       _       _
8       Pohjolaan       _       _       _       _       _       _       _       _       _       _       _       _
9       .       _       _       _       _       _       _       _       _       _       _       _       _

1       Ilmarinen       _       _       _       _       _       _       _       _       _       _       _       _
2       takoo   _       _       _       _       _       _       _       _       _       _       _       _

		
		*/
		
		/*
input_tagged_1.conll09
	 
	 1       Väinämöinen     _       Väinämö _       ADJ     _       Case=Nom|Degree=Pos|Derivation=Inen|Number=Sing _       _       _       _       _       _
2       lähettää        _       lähettää        _       VERB    _       InfForm=1|Number=Sing|VerbForm=Inf|Voice=Act    _       _       _       _       _
       _
3       taikakeinoin    _       taika#keino     _       NOUN    _       Case=Ins|Number=Plur    _       _       _       _       _       _
4       Ilmarisen       _       Ilmarinen       _       PROPN   _       Case=Gen|Number=Sing    _       _       _       _       _       _
5       vastoin _       vastoin _       ADP     _       AdpType=Post    _       _       _       _       _       _
6       tämän   _       tämä    _       PRON    _       Case=Gen|Number=Sing|PronType=Dem       _       _       _       _       _       _
7       tahtoa  _       tahto   _       NOUN    _       Case=Par|Number=Sing    _       _       _       _       _       _
8       Pohjolaan       _       Pohjola _       PROPN   _       Case=Ill|Number=Sing    _       _       _       _       _       _
9       .       _       .       _       PUNCT   _       _       _       _       _       _       _       _


	 */
	}
/*	
	public synchronized List<List<String>> safeTag(Sentence sentence) { 
			return tagger.tag(sentence);
	}
*/
	
	@Override
	public String quickParse(String input) {

			StringBuffer sb = new StringBuffer();
			StringTokenizer st = new StringTokenizer(input,"\n",false);

			while(st.hasMoreTokens()) {
//				lineNumber++;
				String lause = st.nextToken();
				//logger.debug(lineNumber+":"+lause);
				
				String LEMMA = ""; 
				
				// Lets parser this sentence using the marmot tagger.
				
				if(lause != null && ("".equals(lause.trim()) || "#".equals(lause.substring(0, 1)))) {
					sb.append(lause+"\n");
				} else {
					
					List<Word> tokens = new ArrayList<Word>();
					StringTokenizer wordt = new StringTokenizer(lause," ",false);
					while(wordt.hasMoreTokens()) {
						tokens.add(new Word(""+wordt.nextToken()));
					}
					Sentence sentence  = new Sentence(tokens);

//					long start = System.currentTimeMillis();
					List<List<String>> tags = tagger.tag(sentence);
					//List<List<String>> tags = safeTag(sentence); 
						
//					long end = System.currentTimeMillis();
//					System.out.println("TAGGING took:"+(start-end));
					
					// NOW WE HAVE TAGGED THIS SENTENCE.
					// LETS just create output
					for(int i = 0 ; i < sentence.size(); i++ ) {
						
						Word w = sentence.getWord(i);

						
					String ID = ""+(i+1);
					String FORM = w.getWordForm(); //wordt.nextToken();

					//System.out.println("POS"+w.getPosTag());
					//System.out.println("MorphTag"+w.getMorphTag()); 
	//				System.out.println("****TAGS:"+tags.get(i).get(0));
					// This will contain all the variants
					String morphoString = hfst_morphology.runTransducer(FORM);
	//				System.out.println("****morphoString:\n"+morphoString);
					
					// then we need to use the sentence information, i.e. POS 
					String UCPOS = tags.get(i).get(0); //getPOS(w.getPosTag()); 
					LEMMA = getLemma(morphoString, UCPOS,FORM); // wordt.nextToken();
					//wordt.nextToken();
					//String UPOS = wordt.nextToken();
					//String UFEAT = wordt.nextToken();
					//String UHEAD = wordt.nextToken();
					//String UDEPREL = wordt.nextToken();
					//String UDEPS = wordt.nextToken();
					//String UMISC = wordt.nextToken();
					
					//  print '\t'.join((cols[ID],cols[FORM],cols[LEMMA],cols[LEMMA],cols[UCPOS],cols[UCPOS],cols[UFEAT],cols[UFEAT],cols[UHEAD],cols[UHEAD],cols[UDEPREL],cols[UDEPREL],'_','_'))
					//sb.append(ID+"\t"+FORM+"\t"+LEMMA+ "\t"+LEMMA+ "\t"+UCPOS + "\t"+UCPOS + "\t"+UFEAT+ "\t"+UFEAT+  "\t"+UHEAD+ "\t"+UHEAD+ "\t"+UDEPREL+   "\t"+UDEPREL+ "\t"+"_"+ "\t"+"_" +"\n");
					sb.append(ID+"\t"+FORM+"\t"+LEMMA+ "\t"+UCPOS + "\t_"+ "\t_"+ "\t_"+ "\t_"+ "\t_"+ "\t_\n");		
					}
				}
				// It seems there is an empty line between clauses
				if(st.hasMoreTokens() && (".".equals(LEMMA)||"!".equals(LEMMA)||"?".equals(LEMMA)))
					sb.append("\n");
			}
			return sb.toString();

	}
		
	public Map prepareKeys(String morphoString) {
		
		// loop all lines
		
		// if it exists - do not add
		
		return null;
		
	}

	/*
	 * Given one line morpho string, will get the first tag that is then used
	 * For determiningn the POS for lookup. If retrieving the tag fails, we
	 * assume something is wrong and throw exception.
	 */
	public String getFirstTag(String morphoLine)  {
		
		String secondToken ="";
		try {
			StringTokenizer st = new StringTokenizer(morphoLine,"<>");
			String firstToken =  st.nextToken();
			//System.out.println("First token="+firstToken);
			secondToken =  st.nextToken();
			//System.out.println("Second token="+secondToken);
		} catch (Exception e) {
			// System.out.println("Could extract tag:"+morphoLine);
		}
		return secondToken;
	}

	/** 
	 * Retrieves the lemma form from the string.
	 */
	public String parseLemma(String morphoLine) {
		StringTokenizer st = new StringTokenizer(morphoLine, " \t");
		st.nextToken(); //System.out.println("LEMMA FIRST TOKEN: ("+ st.nextToken()+")"); NOTE DO NOT TAKE ANOTHER TOKEN IF PRINTING...
		String raw = (""+st.nextToken()).trim();
		//System.out.println("raw="+raw);
		String out = raw.replaceAll("<[^>]*>", "");
		String out2 = out.replaceAll("\\+", "");
		// One could also replace here # - but the original does not...
		
		//System.out.println("out="+out);
		return  out2;
	}
	
	private Map<String,String> tagMap = null;
	
	public String convertToSDTag(String pos) {
		if(tagMap == null) {
			initializeTagMap();
		}
		String returnTag = tagMap.get(pos);
	//	System.out.println("returnTag:"+returnTag+"\n");
		if(returnTag==null)
			System.out.println("!!!!!  NO MATCH: "+pos);
		return returnTag;
	}
	
	public void initializeTagMap() {
		// TODO this approach can be too simplistic, one may want to add more mappings for ADV, like Adv, Noun etc...
		
		tagMap = new HashMap<String,String>();
		tagMap.put("NOUN","N");
		tagMap.put("VERB","V");	
		tagMap.put("AUX","V");	
		tagMap.put("ADJ", "A");
		tagMap.put("CONJ", "CC");
		tagMap.put("SCONJ", "CS");
		tagMap.put("PRON", "Pron");
		tagMap.put("ADV", "Pcle");
		tagMap.put("NUM", "Num");
		tagMap.put("SCONJ", "CS");
		tagMap.put("INTJ", "Interj");
		tagMap.put("PROPN", "N");
		tagMap.put("ADP", "Adp");
	}
	
	/*

	Not mapped
case "CCONJ":
		case "DET":
		case "PART":
		case "SYM":
		case "X": 
	
		
		----
	
		This from FC plugin code:
		
				case "ADJ":
			tagInt = 4;
			break;
		case "ADP":
			tagInt = 6;
			break;
		case "ADV":
			tagInt = 5;
			break;
		case "AUX":
			break;
		case "CCONJ":
			tagInt = 8;
			break;
		case "CONJ":
			tagInt = 8;
			break;
		case "DET":
			tagInt = 9;
			break;
		case "INTJ":
			tagInt = 7;
			break;
		case "NOUN":
			tagInt = 3;
			break;
		case "NUM":
			tagInt = 10;
			break;
		case "PART":
			break;
		case "PRON":
			tagInt = 1;
			break;
		case "PROPN":
			tagInt = 1;
			break;
		case "PUNCT":
			break;
		case "SCONJ":
			tagInt = 8;
			break;
		case "SYM":
			break;
		case "VERB":
			tagInt = 2;
			break;
		case "X":
			break;

	 */
	
	public String getLemma(String morphoString, String pos, String form) {
		// in case of trouble we set the original word as the lemma
		String lemma = form;
		
		// Punctuations are easy
		if("PUNCT".equals(pos)||"X".equals(pos)||"SYM".equals(pos))
			return form;
	
		String posTag = convertToSDTag(pos);
		// Lets loop through all the strings
		boolean match = false;
		StringTokenizer lineToknizer = new StringTokenizer(morphoString,"\n");
		while(lineToknizer.hasMoreTokens() && !match) {
			String line = lineToknizer.nextToken();
			String tag = getFirstTag(line);
			if((""+posTag).equals(tag)) {
				match = true;
				lemma = parseLemma(line);
			}
		}
		return lemma;
	}
				
}
