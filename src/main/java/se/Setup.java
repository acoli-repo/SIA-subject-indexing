package se;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.cxf.helpers.FileUtils;

import com.google.common.io.Files;
import com.optimaize.langdetect.DetectedLanguage;

/**
 * Class generates document and keyword vectors
 * @author frank
 *
 */
public class Setup  {

	Properties prop;
	Pattern expattern = Pattern.compile(".*[0-9;!?\\[\\]\\/\\(\\)\\*@]+");
	Pattern vecFilePattern = Pattern.compile(".*_(\\d+)(_..\\.vec)");
	Map<String, FasttextEmbeddings> loadedEmbeddings = new HashMap<String, FasttextEmbeddings>();
	Utils utils;
	HashMap<String, Integer> languages = new HashMap<String, Integer>();

	
	public Setup(Properties properties, boolean init) {
		
		this.prop = properties;
		utils = new Utils();
	    
		try {
			// Read properties with file locations
			File documentRootDir = new File(prop.getProperty("DocumentRootDir"));
			File keywordMappingFile = new File(prop.getProperty("KeywordMappingFile"));
			File embeddingsDir = new File(prop.getProperty("EmbeddingsDir"));
			File dataPartitionFile = new File(prop.getProperty("DataPartitionFile"));
			File keywordVectorDir = new File(prop.getProperty("KeywordVectorDir"));
			int evaluationPartitionSize = Integer.parseInt(prop.getProperty("evaluationPortion"));

			if (!keywordVectorDir.exists()) {
				try {
					Files.createParentDirs(new File(keywordVectorDir, "test"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			// Read embeddings
			for (File file : embeddingsDir.listFiles()) {
				
				if (file.isFile() && Files.getFileExtension(file.getName()).toLowerCase().equals("vec")) {

					FasttextEmbeddings emb = utils.readFasttextEmbedings(file);
					System.out.println(emb.getLanguage() +" "+emb.getVectorDimensions());
					loadedEmbeddings.put(emb.getLanguage(), emb);
				}
			}
			
			if (init){
				
				
				// Partition data
				HashMap<String, Integer> map = makeDataPartition(documentRootDir, evaluationPartitionSize);
				Utils.writeDataPartition(dataPartitionFile, map);
				
				computeDocumentVectors(documentRootDir, map);
				computeKeywordVectors(documentRootDir, keywordMappingFile, map);
				
				System.out.println("Document languages :");
				for (String lang : languages.keySet()) {
					System.out.println(lang+"\t"+languages.get(lang));
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Divide all data into training and evaluation data. Data for not supported languages (no embedding available is
	 * discarded automatically
	 * @param rootDir
	 * @param evaluationPartitionSize
	 * @return A map with directory names relative to rootdir where values (0/1) indicate 0=training data, 1=evaluation data
	 */
	public HashMap<String, Integer> makeDataPartition(File rootDir, int evaluationPartitionSize) {
		
		System.out.println("makeDataPartition");
		ArrayList<String> dataIds = new ArrayList<String>();
		
		for (File x : rootDir.listFiles()) {
			System.out.println(x.getAbsolutePath());
			if (x.isDirectory()) {
				
				boolean foundTrainingPDF = false;
				for (File file : x.listFiles()) {
					
					String fileExt = Files.getFileExtension(file.getName()).toLowerCase();
					if (fileExt.equals("pdf")) {
				
						TextConversionResult tar = convertPdf2Text(file, loadedEmbeddings.keySet());
						if (tar != null) {
							foundTrainingPDF = true;
							break;
						}
					}
				}
				if (foundTrainingPDF) dataIds.add(x.getName());
			}
		}
		
		return Utils.partitionData(dataIds, evaluationPartitionSize);
	}
	
	
	public void computeDocumentVectors(File rootDir, HashMap<String, Integer> dataPartition) {
		
		System.out.println("computeDocumentVectors");
		
		int i = 1;
		for (String x : dataPartition.keySet()) {
			File dir = new File (rootDir,x);
			System.out.println(i+" "+dir.getName());i++;
			computeDocumentVector(dir, dataPartition.get(x) == Utils.EVALUATION);
		}
		
		
		/*int i = 1;
		for (File x : rootDir.listFiles()) {
			System.out.println(x.getAbsolutePath());
			if (x.isDirectory()) {
				System.out.println(i+" "+x.getName());i++;
				computeDocumentVector(x);
			}
		}*/
	}
	
	/**
	 * Convert pdf file to text and thereby detect language of text (TODO Multiple languages not supported).
	 * Returns null if text extraction failed or no language could be detected
	 * @param file
	 * @param allowedLanguages ISO639-1 / ISO639-3 codes of allowed languages
	 * @return TextConversionResult or null if extracted text is empty or no language was detected
	 */
	public TextConversionResult convertPdf2Text(File file, Set<String> allowedLanguages) {
		
		TextConversionResult tar = new TextConversionResult();
		
		// Convert pdf files -> text
		String fnameWithoutExtension = Files.getNameWithoutExtension(file.getName());
		File textFile = new File(file.getParent(), fnameWithoutExtension+".txt");
		String cmd = "pdftotext "+file.getAbsolutePath()+" "+textFile.getAbsolutePath();
		String error = Utils.runShellCmd(cmd);
		if (!error.isEmpty()) return null;
		
		// Read generated text file
		String text = Utils.readFile(textFile);
		
		// Replaces escape character with space 
		text = text.replaceAll("\n", " "); 
		text = text.replaceAll("--", ""); 
		text = text.replaceAll("[,.:]","");
		
		// Detect text language
		List<DetectedLanguage> result = utils.detectLanguage(text.substring(0, Math.min(100, text.length()-1)));
		if (result.size() == 0) return null; // skip file if no language was detected
		String lang = result.get(0).getLocale().getLanguage();
		System.out.println("Found language : "+ lang+" in file "+file.getAbsolutePath());
		
		if (!allowedLanguages.isEmpty() && !allowedLanguages.contains(lang)) return null;
		
		// Save result
		tar.setText(text);
		tar.getLanguages().add(lang);
		return tar;
	}
	
	
	
	/**
	 * Compute document vector for PDF in dir. If more than one language is considered then multiple resulting document 
	 * vectors are possible (not implemented).
	 * @param dir
	 * @param isEvaluationData
	 * @return
	 */
	public List<File> computeDocumentVector(File dir, boolean isEvaluationData) {

		List<File> processedPdfFiles = new ArrayList<File>();
		String lang="";
		
		// Delete files in pdf directory
		for (File file : dir.listFiles()) {
			String fileExt = Files.getFileExtension(file.getName()).toLowerCase();
			if (!fileExt.equals("pdf")) {
				FileUtils.delete(file);
			}
		}
		
		// stop here because the data is for evaluation
		if (isEvaluationData) return null;
		
	
		// Start processing of document vector here
		for (File file : dir.listFiles()) {
					
			String fileExt = Files.getFileExtension(file.getName()).toLowerCase();
			if (!fileExt.equals("pdf")) {
				continue;
			}
			lang="";
			System.out.println(file.getAbsolutePath());
			String fnameWithoutExtension = Files.getNameWithoutExtension(file.getName());
			
			// Convert pdf to text
			TextConversionResult tar = convertPdf2Text(file, loadedEmbeddings.keySet());
			if (tar == null) continue;

			String text = tar.getText();
			if (tar.isMultiLangDocument()) {
				continue; // TODO multiple languages in document not handled
			} else {	
				lang = tar.getLanguages().iterator().next();
			}
			
			// Count languages for statistic
			if (!languages.containsKey(lang)) {
				languages.put(lang, 1);
			} else {
				languages.put(lang, languages.get(lang)+1);
			}
			
			if (!loadedEmbeddings.keySet().contains(lang)) {
				System.out.println("Skipping file because language embeddings not loaded");
				continue;
			}
			
			FasttextEmbeddings emb = loadedEmbeddings.get(lang);
			
			// Compute vocabulary of text and build document vector
			HashSet<String> vocabulary = new HashSet<String>();
			HashSet<String> matchedVocab = new HashSet<String>();

			int matched=0;
			double[] documentVector = new double[emb.vectorDimensions];
			
			for (String t : text.split(" ")) {
				
				t = t.trim();
				
				if(t.length() == 1 || expattern.matcher(t).find()) {
					//System.out.println("filtering token "+t);
					continue;
				}
				else {
						
					//filter NP if (!lang.equals("de") || NLPUtils.isNoun(t)) {}
					if (!vocabulary.contains(t)) {
						
						vocabulary.add(t);
						
						// Check if embeddings contain token
						if (emb.getWords().keySet().contains(t)){
							documentVector = 
								Utils.vectorSum(documentVector, emb.getWordVectors()[emb.getWords().get(t)]);
							matchedVocab.add(t);
							matched++;
						}
					}
				}
				
			}
			
			if (vocabulary.size() == 0) continue;
			
			// Write vocabulary file
			System.out.println("Vocabulary size : "+vocabulary.size());
			File vocFile = new File(dir,fnameWithoutExtension+"-vocab_"+vocabulary.size()+"_.txt");
			Utils.writeFile(vocFile, new ArrayList<String>(vocabulary));
			
			// Write matched vocabulary file
			System.out.println("Matched vocabulary size : "+matchedVocab.size());
			File matchedVocFile = new File(dir,fnameWithoutExtension+"-matched_"+matchedVocab.size()+"_.txt");
			Utils.writeFile(matchedVocFile, new ArrayList<String>(matchedVocab));
			
			// Write document vector file
			if (matched > 0) {
				File vecFile = new File(dir, fnameWithoutExtension+"_"+matched+"_"+lang+".vec");
				utils.writeVector(vecFile, documentVector);
				//utils.writeVector(vecFile, documentVector/matched); // if vector length matters
				processedPdfFiles.add(vecFile);
			}
		}
		
		return processedPdfFiles;
	}
	
	
	/**
	 * Construct keyword vectors by summing up document vectors containing a specific keyword.
	 * @param rootDir PDF document root directory
	 * @param keywordMapFile Mapping from keywords to pdf-file folder names
	 * @param partitionMap Mapping from pdf-file folder names to EVALUATION/TRANING
	 */
	public void computeKeywordVectors(File rootDir, File keywordMapFile, HashMap<String, Integer> partitionMap) {
		
		int good=0;
		int bad=0;
		
		HashMap<String, ArrayList<String>> keywordmap = Utils.readKeywordMap(keywordMapFile);
			
		boolean success;
		for (String k : keywordmap.keySet()) {
			
			// Only use training data for building keyword vectors
			HashSet<String> keywordDocumentFolders = new HashSet<String>();
			for (String folder : keywordmap.get(k)) {
				if (partitionMap.containsKey(folder) && partitionMap.get(folder) == Utils.TRANING) {
					keywordDocumentFolders.add(folder);
				}
			}
			success = makeKeywordVector(rootDir, k, keywordDocumentFolders);
			if (success) good++;
			else bad++;
		}
			//if (len(keywordmap[keyword]) > 20):
			//	good+=1
			//	print (keyword,str(keywordmap[keyword]))
		System.out.println("keywords "+keywordmap.size());
		System.out.println("good "+good);
		System.out.println("bad "+bad);
	}
		
		
	public boolean makeKeywordVector(File rootDir, String keyword, HashSet<String> pdfFolders) {
		double[] keyvec = null;
		
		// Initialize keyvector
		for (String lang : loadedEmbeddings.keySet()) {
			keyvec = new double[loadedEmbeddings.get(lang).getVectorDimensions()];
			break;
		}
		
		int good=0;
		System.out.println("Making vector for keyword : "+keyword);
		for (String y : pdfFolders) {
			System.out.println(y);
		}
		
		for (String pdfFolder : pdfFolders) {
			
			pdfFolder = pdfFolder.replaceAll("\"", "");			
			File folder = new File (rootDir, pdfFolder);
			if (!folder.exists()) continue;
			System.out.println(folder.getAbsolutePath());
			
			// Sum up vectors in .vec files
			for (File file : folder.listFiles()) {
				if (file.isDirectory()) continue;
				if (Files.getFileExtension(file.getName()).toLowerCase().equals("vec")) {
					System.out.println("Read document vector "+file.getAbsolutePath());
					keyvec = Utils.vectorSum(keyvec, utils.readVector(file));
					good+=1;
				}
			}
		}
		
		if (good>0) {
			utils.writeVector(
					Utils.getKeywordVectorFile(keyword, prop.getProperty("KeywordVectorDir"), good),
					keyvec
					);
		}
		else {
			System.out.println("no document vectors found for : "+keyword);
		}
		
		return (good>0);
	}


	public Properties getProp() {
		return prop;
	}


	public void setProp(Properties prop) {
		this.prop = prop;
	}

}