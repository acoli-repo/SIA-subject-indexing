package se;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements keyword detection for PDF file.
 * @author frank
 *
 */
public class KeywordDetection {
	
	
	
	public static void evaluateData(File keywordVectorDir, Setup setup) {
				
		File documentRootDir = new File(setup.getProp().getProperty("DocumentRootDir"));

		// Read data partition table
		File dataPartitionFile = new File(setup.getProp().getProperty("DataPartitionFile"));	
		HashMap<String, Integer> dataPartitionTable = Utils.readDataPartition(dataPartitionFile);
		
		System.out.println("\nEVALUATION starting ...");
		int afiles = dataPartitionTable.size();
		int efiles = 0;
		int tfiles = 0;
		System.out.println("All data files : "+afiles);
		
		for (String x : dataPartitionTable.keySet()) {
			if (dataPartitionTable.get(x) == 1) {
				efiles++;
			} else {
				tfiles++;
			}
		}
		System.out.println("Evaluation files : "+efiles);
		System.out.println("Training files : "+tfiles);
		
		
		ArrayList<DocumentResult> results = new ArrayList<DocumentResult>();
		
		for (String dataId :  dataPartitionTable.keySet()) {
			
			if (dataPartitionTable.get(dataId) == Utils.TRANING) continue;
			
			File documentFolder = new File(documentRootDir, dataId);
			
			System.out.println("Evaluating PDF "+documentFolder);
			
			// Run keyword detection for evaluation document
			DocumentResult dr = computeKeywordsForDoc(documentFolder, keywordVectorDir, setup);
			dr.printResult();
			results.add(dr);
		}
		
		
		Evaluator evaluator = new Evaluator(results);
		evaluator.evaluateTopKComputedKeywords(5);
	}
	
	
	/**
	 * Compute keywords for PDF
	 * @param docDir Directory with input PDF file(s)
	 * @param keywordVectorsDir Directory where keyword vectors are stored
	 * @param setup
	 * @return 
	 */
	public static DocumentResult computeKeywordsForDoc(File docDir, File keywordVectorsDir, Setup setup) {
		
		Utils utils = new Utils();
		
		int keywordMinDocSupport = new Integer(setup.getProp().getProperty("KeywordMinDocSupport"));
		File keywordMappingFile = new File(setup.getProp().getProperty("KeywordMappingFile"));

		Pattern keyVecFilePattern = Pattern.compile(".*_(\\d+)(_.vec)");
		
		// Result with distances for each computed keyword
		HashMap<String, Double> computedKeywordDistanceMap = new HashMap<String, Double>();
		
		// Compute document vector(s) for input document
		List<File> docVecs = setup.computeDocumentVector(docDir, false);
		
		// Use computed documented vector(s) to compute similarity with keywords
		for (File docVecFile : docVecs) {

			// Read document vector
			double[] docVec = utils.readVector(docVecFile);
			
			System.out.println("Keyword evaluation results for "+docVecFile.getAbsolutePath()+ " :");
			
			// compare document vector with all keyword vectors
			for (File keyVecFile : new File(setup.getProp().getProperty("KeywordVectorDir")).listFiles()) {
				
				// Filter keywords with low document support
				Matcher matcher = keyVecFilePattern.matcher(keyVecFile.getName());
				if (matcher.find()) {
					if (Integer.parseInt(matcher.group(1)) <= keywordMinDocSupport) continue;
				} else {
					// Error : keyword vector file has no count in filename
					continue;
				}
				
				double[] keyVec = utils.readVector(keyVecFile);
						
				// compute cos distance
				double delta = Utils.cosDistance(docVec, keyVec);
				computedKeywordDistanceMap.put(keyVecFile.getName(), delta);						
			}
			
			// Map from keywords to files containing a keyword
			HashMap<String, ArrayList<String>> keywordMap = Utils.readKeywordMap(keywordMappingFile);
			// Get manually assigned keywords for input document
			HashSet<String> correctKeywords = new HashSet<String>();
			for (String keyword : keywordMap.keySet()) {
				if (keywordMap.get(keyword).contains(docDir.getName())) {
					correctKeywords.add(keyword);
				}
			}
			
			return new DocumentResult(computedKeywordDistanceMap, correctKeywords);
		}
		return null;
	}
	
}
