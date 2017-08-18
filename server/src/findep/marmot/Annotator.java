package findep.marmot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import marmot.core.Sequence;
import marmot.core.Tagger;
import marmot.morph.MorphTagger;
import marmot.morph.Sentence;
import marmot.morph.Word;
import marmot.morph.io.SentenceReader;
import marmot.util.FileUtils;

/*
 * Call in tag.sh
 * $PYTHON marmot-tag.py 
 * --marmot $THIS/LIBS/marmot.jar 
 * --tempdir $TMPDIR 
 * --ud 
 * --hardpos 
 * --mreadings $TMPDIR/all_readings.ud 
 * -m model/fin_model.marmot 
 * > $TMPDIR/input_tagged_1.conll09
 * 
 * actual java  call
 * java -cp /Finnish-dep-parser/LIBS/marmot.jar marmot.morph.cmd.Annotator 
 * --model-file model/fin_model.marmot 
 * --pred-file tmp_data/marmot_out 
 * --test-file form-index=0,token-feature-index=1,tmp_data/marmot_in
 */
public class Annotator {
	private static final char SEPERATOR_ = '\t';
	private static final String EMPTY_ = "_";

	private MorphTagger tagger=null;
	
	public Annotator(String modelFile)
	{
		tagger = FileUtils.loadFromFile(modelFile);
		
	}
	
	public void annotate(String predFile, String testFile ) throws IOException
	{
		//System.err.println(getClass().getName()+": Annotating: predFile="+predFile+", testFile="+testFile);
		Writer writer=null;
		if (predFile == null) {
			writer = new BufferedWriter(new OutputStreamWriter(System.out));	
		} else {
			writer = new FileWriter(predFile);
		}
		annotate(tagger, testFile, writer);
		writer.flush();
		if (writer!=null)
		{
			writer.close();			
		}
	}
	
	public void annotate(Tagger tagger, String text_file, Writer writer) throws IOException {	
		SentenceReader reader = new SentenceReader(text_file);
		
		for (Sequence sequence : reader) {
			Sentence sentence = (Sentence) sequence;
			
			if (sentence.isEmpty()) {
				System.err.println("Warning: Skipping empty sentence!");
				continue;
			}
			
			List<List<String>> tags;
			
			try {
			
			tags = tagger.tag(sentence);
			
			} catch (OutOfMemoryError e) {
				
				tags = new ArrayList<List<String>>(sentence.size());
				
				List<String> tag = Collections.singletonList("_");
				
				for (int index = 0; index < sentence.size(); index ++) {
					tags.add(tag);
				}
				
				System.err.format("Warning: Can't tag sentence of length: %d (Not enough memory)!\n", sentence.size());
				
			}
			
			for (int i = 0; i < sentence.size(); i ++) {
				Word word = sentence.getWord(i);
				
				writer.append(Integer.toString(i + 1));
				writer.append(SEPERATOR_);
				writer.append(word.getWordForm());
				
				// Lemma
				writer.append(SEPERATOR_);
				writer.append(EMPTY_);
				writer.append(SEPERATOR_);
				writer.append(EMPTY_);
				
				// Pos
				writer.append(SEPERATOR_);
				writer.append((word.getPosTag() != null ) ? word.getPosTag() : EMPTY_ );
				writer.append(SEPERATOR_);
				writer.append(tags.get(i).get(0));
				
				// Feat
				writer.append(SEPERATOR_);
				writer.append((word.getMorphTag() != null ) ? word.getMorphTag() : EMPTY_);
				writer.append(SEPERATOR_);
				writer.append((tags.get(i).size() > 1) ? tags.get(i).get(1) : EMPTY_);

//				// Head
//				writer.append(SEPERATOR_);
//				writer.append(EMPTY_);
//				writer.append(SEPERATOR_);
//				writer.append(EMPTY_);
//				
//				// Deprel
//				writer.append(SEPERATOR_);
//				writer.append(EMPTY_);
//				writer.append(SEPERATOR_);
//				writer.append(EMPTY_);
//
//				// Predicate
//				writer.append(SEPERATOR_);
//				writer.append(EMPTY_);
//				
//				// Yield
//				writer.append(SEPERATOR_);
//				writer.append(EMPTY_);
					
				writer.append('\n');
			}
			writer.append('\n');
		}
	}

	/*
	public static void main(String[] args) {
		MorphOptions options = new MorphOptions();
		options.setPropertiesFromStrings(args);
		
		options.dieIfPropertyIsEmpty(MorphOptions.MODEL_FILE);
		options.dieIfPropertyIsEmpty(MorphOptions.PRED_FILE);
		options.dieIfPropertyIsEmpty(MorphOptions.TEST_FILE);
		
		MorphTagger tagger = FileUtils.loadFromFile(options.getModelFile());
		
		String lemmatizer_file = options.getLemmatizerFile();
		if (!lemmatizer_file.isEmpty()) {
			Lemmatizer lemmatizer = FileUtils.loadFromFile(lemmatizer_file);
			tagger.setPipeLineLemmatizer(lemmatizer);
		}
		
		if (options.getVerbose()) {
			System.err.format("Loaded model, currently using %g MB of RAM\n", Sys.getUsedMemoryInMegaBytes());
		}
		
		if (!options.getMorphDict().isEmpty()) {
			MorphWeightVector vector = (MorphWeightVector) tagger.getWeightVector();
			MorphDictionary dict = vector.getMorphDict();
			if (dict != null) {
				dict.addWordsFromFile(options.getMorphDict());
			} else {
				System.err.format("Warning: Can't add words from morph. dictionary, because morph. dictionary is null!\n");
			}
		}
		
		try {
			String pred_file = options.getPredFile();
			Writer writer;
			if (pred_file.isEmpty()) {
				writer = new BufferedWriter(new OutputStreamWriter(System.out));	
			} else {
				writer = FileUtils.openFileWriter(pred_file);
			}
			annotate(tagger, options.getTestFile(), writer);
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
*/
	/*
	private void annotate(MorphTagger tagger, String text_file, Writer writer) throws IOException {	
		SentenceReader reader = new SentenceReader(text_file);
		
		for (Sequence sequence : reader) {
			annotate(tagger, sequence, writer);
		}
	}

	private void annotate(MorphTagger tagger, Sequence sequence, Writer writer) throws IOException {
		Sentence sentence = (Sentence) sequence;
		
		if (sentence.isEmpty()) {
			System.err.println("Warning: Skipping empty sentence!");
			return;
		}
		
		List<List<String>> lemma_tags;
		
		try {
		
		lemma_tags = tagger.tagWithLemma(sentence);
		
		} catch (OutOfMemoryError e) {
			
			lemma_tags = new ArrayList<List<String>>(sentence.size());
			
			List<String> lemma_tag = Arrays.asList(EMPTY_, EMPTY_);
			
			for (int index = 0; index < sentence.size(); index ++) {
				lemma_tags.add(lemma_tag);
			}
			
			System.err.format("Warning: Can't tag sentence of length: %d (Not enough memory)!\n", sentence.size());
			
		}
		
		for (int i = 0; i < sentence.size(); i ++) {
			Word word = sentence.getWord(i);
			
			List<String> token_lemma_tags = lemma_tags.get(i);
			
			writer.append(Integer.toString(i + 1));
			writer.append(SEPARATOR_);
			writer.append(word.getWordForm());
			
			// Lemma
			writer.append(SEPARATOR_);
			writer.append(word.getLemma() != null ? word.getLemma() : EMPTY_);
			writer.append(SEPARATOR_);
			
			String lemma = token_lemma_tags.get(0);
			writer.append(lemma != null ? lemma : EMPTY_ );
			
			// Pos
			writer.append(SEPARATOR_);
			writer.append(word.getPosTag() != null ? word.getPosTag() : EMPTY_ );
			writer.append(SEPARATOR_);
			
			String pos = token_lemma_tags.get(1);
			writer.append(pos);
			
			// Feat
			writer.append(SEPARATOR_);
			writer.append(word.getMorphTag() != null ? word.getMorphTag() : EMPTY_);
			writer.append(SEPARATOR_);
			String morph = EMPTY_;
			if (2 < token_lemma_tags.size()) {
				morph = token_lemma_tags.get(2);
			}
			writer.append(morph);

			writer.append('\n');
		}
		writer.append('\n');
	
	}
*/
}
