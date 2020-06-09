package run;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import se.KeywordDetection;
import se.Setup;
import se.Utils;

/**
 * 	 
 * Run the application
 * @author frank
 */
public class Main {

	
	static Setup setup;
	
	public static void main(String[] args) {
		
		
		Options options = new Options();
		
		Option propertyFile = new Option("c", "config", true, "properties file");
		propertyFile.setRequired(true);
	    options.addOption(propertyFile);
	    
	    Option init = new Option("i", "init", false, "init vectors");
	    init.setRequired(false);
	    options.addOption(init);
	    
	    Option keys = new Option("k", "keywords", false, "compute keywords");
	    keys.setRequired(false);
	    options.addOption(keys);
	    
	    Option docDir = new Option("d", "pdfDir", true, "directory with pdf input");
	    docDir.setRequired(false);
	    options.addOption(docDir);
	    
	    CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd=null;

        try {
            cmd = parser.parse(options, args);
            
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("keyword-detect", options);
            System.exit(1);
        }

        
        String props = cmd.getOptionValue("config");		
		Properties properties;
		try {
			properties = Utils.readPropertiesFile(props);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		System.out.println("Properties :");
		System.out.println("PdfTrainingRootDir\t"+properties.getProperty("PdfTrainingRootDir"));
		System.out.println("KeywordMappingFile\t"+properties.getProperty("KeywordMappingFile"));
		System.out.println("KeywordVectorDir\t"+properties.getProperty("KeywordVectorDir"));
		System.out.println("EmbeddingsDir\t\t"+properties.getProperty("EmbeddingsDir"));
		System.out.println();

		
		// Run setup with init
		if (cmd.hasOption("init")) {
			setup = new Setup(properties, true);
			return;
		}
		/////////////////////////////////////////////////////////////////////////////
		
		// Run detection
		if (cmd.hasOption("keywords")) {

			if (cmd.hasOption("pdfDir")) {
				setup = new Setup(properties, false);
				File pdfFile = new File(cmd.getOptionValue("pdfDir"));
				File keywordVectorDir = new File(properties.getProperty("KeywordVectorDir"));
				KeywordDetection.computeKeywordsForDoc(pdfFile, keywordVectorDir, setup);
			} else {
				System.out.println("pdfDir option is required !");
			}
		}
	}
}
