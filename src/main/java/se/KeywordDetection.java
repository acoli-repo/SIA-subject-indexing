package se;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements keyword detection for PDF file.
 * @author frank
 *
 */
public class KeywordDetection {
	
	/**
	 * Find keywords for PDFs
	 * @param docDir Directory with input PDF files
	 * @param keywordVectorsDir Directory where keyword vectors are stored
	 * @param setup
	 */
	public static void computeKeywordsForDoc(File docDir, File keywordVectorsDir, Setup setup) {
		
		Utils utils = new Utils();
		
		int keywordMinDocSupport = new Integer(setup.getProp().getProperty("KeywordMinDocSupport"));
		File keywordMappingFile = new File(setup.getProp().getProperty("KeywordMappingFile"));

		Pattern keyVecFilePattern = Pattern.compile(".*_(\\d+)(_.vec)");
		
		// result list with distances for each keyword
		HashMap<String, Double> distances = new HashMap<String, Double>();
		
		// make vec for new doc
		List<File> docVecs = setup.computeDocumentVector(docDir);
		
		// read vec for new doc
		for (File docVecFile : docVecs) {
			double[] docVec = utils.readVector(docVecFile);
			
			System.out.println("Keyword distances for "+docVecFile.getAbsolutePath()+ " :");
			
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
				distances.put(keyVecFile.getName(), delta);						
			}
			
			int limit = 100;
			int i = 1;
			System.out.println("Top 100");
			for (Entry<String, Double> k : sortDistances(distances)) {
				System.out.println(i+"\t"+k.getKey()+"\n"+k.getValue());
				if (i++ > limit) break;
			}
			
			// Show original keywords (only for testing training PDFs)
			HashMap<String, ArrayList<String>> keywordMap = Utils.readKeywordMap(keywordMappingFile);
			System.out.println("Original keywords (only for testing results for training PDFs):");
			int j=1;
			for (String key : keywordMap.keySet()) {
				if (keywordMap.get(key).contains(docDir.getName())) {
					System.out.println(j+++" "+key);
				}
			}
		}
	}
	
	/**
	 * Sort keywords by distance
	 * @param distances
	 * @return
	 */
	private static ArrayList<Entry<String, Double>> sortDistances(HashMap<String, Double> distances) {
		
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
