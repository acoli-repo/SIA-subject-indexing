package se;

import java.io.File;
import java.util.List;

import com.optimaize.langdetect.DetectedLanguage;

/**
 * @author frank
 *
 */
public interface Utilities {
	
  List<DetectedLanguage> detectLanguage(String text);
			
  void writeVector(File vecFile, double[] vector);
				
  double[] readVector(File vecFile);
  
  FasttextEmbeddings readFasttextEmbedings(File file);
  
}
