package se;

import java.util.List;

/**
 * @author frank
 *
 */
public class Evaluator {
	
	
	private List<DocumentResult> documentResults;

	public Evaluator (List<DocumentResult> results) {
		documentResults = results;
	}
	
	
	public void evaluateTopKComputedKeywords(int K) {
		
		for (int k=1; k <= K; k++) {
			
			int i = 0;
			float precision = 0;
			float recall = 0;
			float f1Score = 0;
			int truePositives = 0;
			for (DocumentResult r : this.documentResults) {
				
				// call evaluation function
				r.atLeastOneOfTheTopKComputedKeywordsMatchted(k);
				
				truePositives+=r.getTruePositives();
				precision+=r.getPrecision();
				recall=r.getRecall();
				f1Score=r.getF1Score();
				i++;
			}
			
			System.out.println("\nTest if the top "+k+" computed keyword(s) of a document matched at least one of the manually assigned keywords");
			System.out.println("Tested documents : "+i);
			System.out.println("True positives : "+truePositives);
			System.out.println("Precision : "+precision/i);
			//System.out.println("Recall : "+recall/i);
			//System.out.println("F1Score : "+f1Score/i);
		}
		
	}

}
