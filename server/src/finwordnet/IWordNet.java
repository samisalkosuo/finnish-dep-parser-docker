package finwordnet;

import java.util.List;

public interface IWordNet {

	
	public enum HYPERNYM_FORMAT {
		JSON, CSV
	};

	// senses to return
	// L= last
	// F= first
	// A= all
	// default is L
	public enum SENSES_TO_RETURN {
		L, A, F
	};

	
	/**
	 * Initialize Wordnet
	 */
	public void init();

	/**
	 * Release all resources
	 */
	public void destroy();


	/**
	 * 
	 * @param _word
	 * @param partofspeech
	 * @return JSON concepts for the word
	 */
	//public List<List<List<String>>> getHypernyms(String _word, String partofspeech);
	public List<String> getHypernymStrings(String _word, String partofspeech,HYPERNYM_FORMAT format, SENSES_TO_RETURN sensesToReturn);
	
	public String getSynonyms(String _word, String partofspeech);
}
