package finwordnet;

import java.util.List;

public interface IWordNet {

	
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
	public List<String> getHypernymJSONs(String _word, String partofspeech);
	
	public String getSynonyms(String _word, String partofspeech);
}
