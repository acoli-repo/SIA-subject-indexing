package se;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;

import com.google.common.io.Files;
import com.optimaize.langdetect.DetectedLanguage;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;



public class Utils implements Utilities {
	
	private static LanguageProfileReader languageProfileReader;
	private static List<LanguageProfile> languageProfiles;
	private static LanguageDetector languageDetector;
	private static TextObjectFactory textObjectFactory;
	private static boolean languagesInitialized = false;


/**
 * Cosinus similarity
 * @author frank
 *
 */
public static double cosDistance(double [] v1, double[] v2) {
	 
	 int dim;
	 dim = v1.length;
	 if (dim != v2.length) return 0d;
	 
	 double sp = 0;
	 double v1n = 0;
	 double v2n = 0;
     for (int column = 0; column < dim; column++) {
         sp += v1[column] * v2[column];
         v1n+= v1[column] * v1[column];
         v2n+= v2[column] * v2[column];
     }
     
     double vn = Math.sqrt(v1n)*Math.sqrt(v2n);
     
	 return sp/vn;
 }

/**
 * Vectors have to be of same length, otherwise null is returned
 * @param v1
 * @param v2
 * @return sum
 */
public static double[] vectorSum(double [] v1, double[] v2) {
	
	if (v1.length != v2.length) return null;
	
	double[] sum = new double[v1.length];
	
	 for (int column = 0; column < v1.length; column++) {
         sum[column] = v1[column] + v2[column];
     }
	
	return sum;
}

/**
 * Vectors have to be of same length, otherwise null is returned
 * @param v1
 * @param v2
 * @return sum
 */
public static double[] vectorSum(float [] v1, float[] v2) {
	
	if (v1.length != v2.length) return null;
	
	double[] sum = new double[v1.length];
	
	 for (int column = 0; column < v1.length; column++) {
         sum[column] = v1[column] + v2[column];
     }
	
	return sum;
}

/**
 * Vectors have to be of same length, otherwise null is returned
 * @param v1
 * @param v2
 * @return sum
 */
public static double[] vectorSum(double [] v1, float[] v2) {
	
	if (v1.length != v2.length) return null;
	
	double[] sum = new double[v1.length];
	
	 for (int column = 0; column < v1.length; column++) {
         sum[column] = v1[column] + v2[column];
     }
	
	return sum;
}



/**
 * Write single vector to file
 * @see se.Utilities#writeVector(java.io.File, double[])
 */
public void writeVector(File vecFile, double[] vector) {
	
	// serialize array
	try {
	     FileOutputStream f_out = new FileOutputStream(vecFile);
	     ObjectOutputStream obj_out = new ObjectOutputStream (f_out);
	     obj_out.writeObject (vector);
	     obj_out.close();
	
	} catch (IOException e) {
		e.printStackTrace();
	}
}


/** Read single vector from file
 * @see se.Utilities#readVector(java.io.File)
 */
public double[] readVector(File vecFile) {
	
	// de-serialize array
	try {
		FileInputStream f_in = new FileInputStream(vecFile);
		ObjectInputStream obj_in = new ObjectInputStream (f_in);
		double [] tmp = (double[])obj_in.readObject();
		obj_in.close();
		return tmp;
	} catch (Exception e) {
		e.printStackTrace();
	}
	return null;
}


/**
 * Read Fasttext embeddings from .vec file. Each line contains a word in the first position and the word embedding vector in the following
 * positions. All values are separated with space.
 * @see se.Utilities#readEmbedings(java.io.File)
 * @return FasttextEmbeddings
 */
public FasttextEmbeddings readFasttextEmbedings(File file) {

	FasttextEmbeddings embeddings = null;
	int lineCount=0;
	int dimensions=0;
	String word="";
	String[] tmp={};
	
	
	try {
		
		System.out.println("Reading embeddings from : "+file.getAbsolutePath()+" ...");
		
	    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));         

	    String line;
	    int i=0;
	    while ((line = br.readLine()) != null) {
	    	
	        if (i == 0) {
	        	
	        	// Read line count and dimensions
	        	lineCount = Integer.parseInt(line.split(" ")[0].trim());
	        	dimensions = Integer.parseInt(line.split(" ")[1].trim());
	        	String language = file.getName().split("\\.")[2];
	        	System.out.println("lines : "+lineCount);
	        	System.out.println("dimensions : "+dimensions);
	        	System.out.println("language : "+language+" (check language and adjust code if wrong)");

	        	embeddings = new FasttextEmbeddings(file.getName(), lineCount, dimensions, language);
	        	System.out.println("dimensions : "+dimensions);
	        	
	        } else {
	        	
	        	// Parse word and embedding vector
	        	tmp=line.split(" ");
	        	word = tmp[0];
	        	if (tmp.length != (dimensions+1)) continue;
	        	
        		int j=1;
	        	float[]v = new float[dimensions];
	        	while (j <= dimensions) {
	        		v[j-1] = Float.parseFloat(tmp[j]);
	        		j++;
	        	}
	        	
	        	embeddings.getWords().put(word, i-1);
	        	embeddings.getWordVectors()[i-1] = v;
	        }
	        
	        if (i % 100000 == 0) System.out.println(i);
	        
	        i++;
	    }
	    
	    br.close();
	    System.out.println("OK");

	} catch (IOException e) {
	    e.printStackTrace();
	    return null;
	}
	
	return embeddings;
}



/**
 * Detect language of text. In case of an error returns null
 * @param Text sample
 * @return List of all detected languages in descending order by probability.
 * @see se.Utilities#detectLanguage(java.lang.String)
 */
@Override
public List<DetectedLanguage> detectLanguage(String text) {
	
	try {
		
		if (!languagesInitialized) {
			
			// Read 70 internal language profiles
			languageProfileReader = new LanguageProfileReader();
			languageProfiles = languageProfileReader.readAllBuiltIn();
	
			languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
				.probabilityThreshold(0.1d)
		        .withProfiles(languageProfiles)
		        .build();
		
			textObjectFactory = CommonTextObjectFactories.forIndexingCleanText();
			languagesInitialized=true;
		}
		
		TextObject textObject = textObjectFactory.forText(text);
		
		// Full method : 
		// Retrieve all detected languages as a sorted list, starting with the language with the
		// highest probability
		List<DetectedLanguage> detectedLanguages = languageDetector.getProbabilities(textObject);
		
		
		// Short method :
		// Returns only one language with high probability (> 0,85) or none.
		// com.google.common.base.Optional<LdLocale> lang = languageDetector.detect(textObject);
		// if (lang.isPresent()) {
		// language = lang.get().getLanguage();
		
		// Output language with prob
		if (!detectedLanguages.isEmpty()) {
			for (DetectedLanguage xy : detectedLanguages) {
					System.out.println(xy.getLocale().getLanguage()+" "+xy.getProbability());
			}
		}
		
		return detectedLanguages;
		
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


/**
 * Read properties file
 * @param file
 * @return Properties
 */
public static Properties readPropertiesFile(String fileName) throws IOException {
    FileInputStream fis = null;
    Properties prop = null;
    try {
       fis = new FileInputStream(fileName);
       prop = new Properties();
       prop.load(fis);
    } catch(FileNotFoundException fnfe) {
       fnfe.printStackTrace();
    } catch(IOException ioe) {
       ioe.printStackTrace();
    } finally {
       fis.close();
    }
    return prop;
 }

/**
 * Run shell command
 * @param cmd
 */
public static String runShellCmd(String cmd){
	
	System.out.println(cmd);
	CommandLine oCmdLine = CommandLine.parse(cmd);
    DefaultExecutor oDefaultExecutor = new DefaultExecutor();
    
  	try {
		oDefaultExecutor.execute(oCmdLine);
		System.out.println("ok");
		return "";
	} catch (IOException e) {
		return e.getMessage();
	}
}


/**
 * Read text file into String
 */
public static String readFile(File file) {
	
	try {
		String text = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
		return text;
	} catch (IOException e) {
		e.printStackTrace();
	}
	return "";
}


/**
 * Write lines of text to file
 * @param file
 * @param lines
 */
public static void writeFile(File file, List<String> lines) {
	
	try {
		FileUtils.writeLines(file, "UTF-8", lines);
	} catch (IOException e) {
		e.printStackTrace();
	}
}


/**
 * Read lines from text file
 * @param file
 */
public static List<String> readFileLines(File file) {
	
	try {
		return FileUtils.readLines(file, "UTF-8");
	} catch (IOException e) {
		e.printStackTrace();
		return null;
	}
}


/**
 * Construct a filename for a keyword. Remove unwanted characters (e.g. /)
 * @param keyword
 * @param keywordVectorDir target directory
 * @param builtFromCountDocs Number of training documents that support the keyword
 */
public static File getKeywordVectorFile(String keyword, String KeywordVectorDir, int builtFromCountDocs) {

	keyword=keyword.replaceAll("/","@");
	
	return new File(KeywordVectorDir,keyword+"_"+builtFromCountDocs+"_.vec");
}



public static void main(String[] args) {	

	System.out.println(Files.getFileExtension("hello.pdf"));
	Pattern expattern = Pattern.compile(".*[0-9;!?\\[\\]\\/\\(\\)\\*@]+");
	String text = "abads)dfs";
	System.out.println(expattern.matcher(text).find());
	
	Utils utils = new Utils();
	//File fasttextFile = new File("/u02/oradata/tmp/cc.en.300.vec");
	//utils.readFasttextEmbedings(fasttextFile);*/
	
	utils.detectLanguage("Kosinus-Ähnlichkeit ist ein Maß für die Ähnlichkeit zweier Vektoren. Dabei wird der Kosinus des Winkels zwischen beiden Vektoren bestimmt. Der Kosinus des eingeschlossenen Winkels Null ist eins; für jeden anderen Winkel ist der Kosinus des eingeschlossenen Winkels kleiner als eins.");
	utils.detectLanguage("These bounds apply for any number of dimensions, and the cosine similarity is most commonly used in high-dimensional positive spaces. For example, in information retrieval and text mining, each term is notionally assigned a different dimension and a document is characterised by a vector where the value in each dimension corresponds to the number of times the term appears in the document");
    utils.detectLanguage("La similarité cosinus est fréquemment utilisée en tant que mesure de ressemblance entre deux documents. Il pourra s'agir de comparer les textes issus d'un corpus dans une optique de classification (regrouper tous les documents relatifs à une thématique particulière), ou de recherche d'information (dans ce cas, un document vectorisé est constitué par les mots de la requête et est comparé par mesure de cosinus de l'angle avec des vecteurs correspondant à tous les documents présents dans le corpus");
    utils.detectLanguage("Per rendere più efficace il confronto, in genere, si eliminano le parole più corte e molto frequenti che servono a costruire le frasi, come e, che, ma, quindi e altre, che possono essere identificate velocemente con un'euristica appropriata. È possibile anche usare la similarità per riconoscere la lingua in cui è scritto un testo, senza ovviamente ignorare le parole corte e frequenti");
    utils.detectLanguage("Para el cálculo del coseno suave, se introduce la matriz s que contiene la similitud entre las características. Se puede calcular utilizando la distancia Levenshtein u otras medidas de similitud, por ejemplo, diversas medidas de similitud de WordNet. Luego solo se multiplica por esta matriz.");
}

/**
 * Read keyword mapping file
 * @param keywordMapFile
 * @return 
 * @return keyword map
 */
public static HashMap<String, ArrayList<String>> readKeywordMap(File keywordMapFile) {
		
	HashMap<String, ArrayList<String>> keywordmap = new HashMap<String, ArrayList<String>>();
	
	String docFolder;
	String keyword;
	
	// read keyword_map_file and make keywordmap
	List<String> lines = Utils.readFileLines(keywordMapFile);
	for (String line : lines) {
		String[] cols = line.split("\t");
		docFolder = cols[0].replace("\"", "");
		keyword = cols[2].replace("\"", "");
		if(!keywordmap.containsKey(keyword)) {
			ArrayList<String> tmp = new ArrayList<String>();
			tmp.add(docFolder);
			keywordmap.put(keyword, tmp);
		} else {
			ArrayList<String> tmp = keywordmap.get(keyword);
			tmp.add(docFolder);
			keywordmap.put(keyword, tmp);
		}
	}
	
	return keywordmap;
	}
	

}