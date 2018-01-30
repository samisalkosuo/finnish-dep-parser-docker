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


	public List<String> getHypernymStrings(String _word, String partofspeech,HYPERNYM_FORMAT format, SENSES_TO_RETURN sensesToReturn);
	
	public List<String> getHypernymStringsWithSenses(String _word, String partofspeech) throws Exception; 
	
	public String getSynonyms(String _word, String partofspeech);
}
