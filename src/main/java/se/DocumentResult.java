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

	private int truePos = 0;
	private int falsePos = 0;
	private int falseNeg = 0;
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
	}


	/**
	 * Evaluate the top k computed keywords for a document
	 * @param k
	 * @return Return true if at least one keyword matched the manually assigned keywords
	 */
	public boolean atLeastOneOfTheTopKComputedKeywordsMatchted(int k) {
		
		if (computedKeywords.size() == 0 || k < 1) return false;
		
		int i = 0;
		while (i++ < k) {
			String keyword = extractKeywordFromFileName(computedKeywords.get(i).getKey());
			if (assignedKeywords.contains(keyword)) {
				truePos=1;
				falsePos=0;
				return true;
			}
		}
		truePos=0;
		falsePos=1;
		return false;		
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


	public int getTruePositives() {
		return truePos;
	}


	public int getFalsePositives() {
		return falsePos;
	}


	public int getFalseNegatives() {
		return falseNeg;
	}


	public ArrayList<Entry<String, Double>> getComputedKeywords() {
		return computedKeywords;
	}


	public HashSet<String> getAssignedKeywords() {
		return assignedKeywords;
	}
	
	
}
