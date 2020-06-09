package se;

import java.util.HashMap;


/**
 * Class for modeling Fasttext embeddings
 * @author frank
 *
 */
public class FasttextEmbeddings {
	
	String label="";
	String language="";
	HashMap<String, Integer> words;
	float[][] wordVectors;
	int vocabularySize = 0;
	int vectorDimensions = 0;
	
	
	FasttextEmbeddings(String _label, int _vocabularySize, int _vectorDimensions) {
		
		label = _label;
		vocabularySize = _vocabularySize;
		vectorDimensions = _vectorDimensions;
		language = _label.split("\\.")[1];
		words = new HashMap<String, Integer>();
		wordVectors =  new float[vocabularySize][vectorDimensions];
	}


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}


	public HashMap<String, Integer> getWords() {
		return words;
	}


	public void setWords(HashMap<String, Integer> words) {
		this.words = words;
	}


	public float[][] getWordVectors() {
		return wordVectors;
	}


	public void setWordVectors(float[][] wordVectors) {
		this.wordVectors = wordVectors;
	}


	public String getLanguage() {
		return language;
	}


	public void setLanguage(String language) {
		this.language = language;
	}


	public int getVocabularySize() {
		return vocabularySize;
	}


	public void setVocabularySize(int vocabularySize) {
		this.vocabularySize = vocabularySize;
	}


	public int getVectorDimensions() {
		return vectorDimensions;
	}


	public void setVectorDimensions(int vectorDimensions) {
		this.vectorDimensions = vectorDimensions;
	}

}
