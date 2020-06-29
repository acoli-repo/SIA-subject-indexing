package se;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author frank
 *
 */
public class DocumentResult {

	int truePos = 0;
	int falsePos = 0;
	int trueNeg = 0;
	int falseNeg = 0;
	private ArrayList<Entry<String, Double>> computedKeywords;
	private HashSet<String> assignedKeywords;
	
	/**
	 * Constructor
	 * @param foundKeywords
	 * @param assignedKeywords
	 */
	public DocumentResult (HashMap<String, Double> foundKeywords, HashSet<String> assignedKeywords) {
	
		this.computedKeywords = DocumentResult.sortDistances(foundKeywords);
		this.assignedKeywords = assignedKeywords;
		this.checkBestFoundKeywordMatches();
	}
	
	
	/**
	 * 
	 */
	private void checkBestFoundKeywordMatches() {
		
		if (this.isbestFoundKeywordMatched()) {
			truePos=1;
			falsePos=0;
			falseNeg=0;
		} else {
			truePos=0;
			falsePos=1;
			falseNeg=0;
		}
	}


	public boolean isbestFoundKeywordMatched() {
		
		if (computedKeywords.size() == 0) return false;
		return assignedKeywords.contains(extractKeywordFromFileName(computedKeywords.get(0).getKey()));
	}
	
	private String extractKeywordFromFileName(String kfn) {
		
		// Interkulturelles Verstehen_8_.vec - remove suffix _8_.vec 
		int ix = kfn.indexOf("_");
		return kfn.substring(0, ix);
	}
	
	
	public void printResult() {
		
		System.out.println("\nManually assigned keywords :");
		int i_ = 1;
		for (String x : assignedKeywords) {
			System.out.println(i_+++"\t"+x);
		}
		System.out.println();
		
		final int limit = 20;
		int i = 1;
		System.out.println("Computed keywords : (top "+limit+")");
		for (Entry<String, Double> k : computedKeywords) {
			System.out.println(i+"\t"+k.getKey()+"\n"+k.getValue());
			if (i++ >= limit) break;
		}
	}
	
	
	/**
	 * @return
	 */
	public float getPrecision() {
		return Utils.precision(truePos, falsePos);
	}
	
	/**
	 * @return
	 */
	public float getRecall() {
		return Utils.recall(truePos, falseNeg);
	}
	
	/**
	 * @return
	 */
	public float getF1Score() {
		return Utils.f1Score(getPrecision(), getRecall());
	}
	
	
	
	public static ArrayList<Entry<String, Double>> sortDistances(HashMap<String, Double> distances) {
		
		ArrayList<Entry<String, Double>> y = new ArrayList<Entry<String, Double>>(distances.entrySet());
		Collections.sort(y, (new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                    Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        }));
		
		return y;
	}
	
	
}
