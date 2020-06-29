package se;

import java.util.HashSet;

/**
 * @author frank
 *
 */
public class TextConversionResult {

	private HashSet<String> languages = new HashSet<String>();
	private String text = "";
	
	
	public TextConversionResult(){};
	
	public TextConversionResult (String text, HashSet<String> languages) {
		this.text = text;
		this.languages = languages;
	}
	
	
	public HashSet<String> getLanguages() {
		return languages;
	}
	public void setLanguages(HashSet<String> languages) {
		this.languages = languages;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	public boolean isMultiLangDocument() {
		if (languages.size() > 1) return true;
		else return false;
	}
}
