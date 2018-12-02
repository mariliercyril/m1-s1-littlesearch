package edu.fromatoz.littlesearch.app;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.Set;

import org.jsoup.Jsoup;

import org.jsoup.nodes.Element;

import org.jsoup.select.Elements;

import edu.fromatoz.littlesearch.dataintegrator.FrenchAnalyser;

import edu.fromatoz.littlesearch.dataintegrator.controller.*;

import edu.fromatoz.littlesearch.dataintegrator.model.JSONWriter;

import edu.fromatoz.littlesearch.tool.Separator;

/**
 * The {@code DataIntegrator} class is the "main" class of a data integrator for the search engine.
 * <p>This is a sort of ETL (Extract-Transform-Load) application: it extracts data from various pages of the CNRTL,
 * transforms it by performing NLP (Natural Language Processing) tasks (among others, <i>tokenization</i>
 * with a {@link FrenchAnalyser}, <i>part-of-speech tagging</i> with a {@link FrenchTagger} and <i>lemmatisation</i>
 * with a {@link FrenchLemmatiser}) and loads it into a data warehouse.</p>
 * <p>This integrator extracts data from the <a href="http://www.cnrtl.fr">CNRTL</a> (Centre National de Ressources Textuelles et Lexicales).</p>
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public class DataIntegrator {

	private static final String RESOURCE_PATH_FORMAT = Separator.SLASH.getValue() + "%s";

	private static final String CNRTL_URL_FORMAT = "http" + Separator.COLON.getValue() + "//" + "www.cnrtl.fr" + RESOURCE_PATH_FORMAT;

	public static final String DEFINITION_FORMAT = String.format(CNRTL_URL_FORMAT, "definition") + RESOURCE_PATH_FORMAT;
	public static final String MORPHOLOGY_FORMAT = String.format(CNRTL_URL_FORMAT, "morphologie") + RESOURCE_PATH_FORMAT;
	public static final String SYNONYMY_FORMAT = String.format(CNRTL_URL_FORMAT, "synonymie") + RESOURCE_PATH_FORMAT;

	public static void main(String[] args) {

		// Gives the text file name to the integrator...
		String textFileName = "BERNHARD_RIEMANN";
		if (args.length > 0) {
			if (args.length > 1) {
				System.out.println("Use: ./integrate \"<text_file_name>\"");
				System.exit(0);
			} else {
				textFileName = args[0];
			}
		}
		String textFilePath = String.format(SearchEngine.TEXT_FILE_PATH_FORMAT, textFileName);

		JSONWriter jsonWriter = new JSONWriter();

		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(textFilePath)));
			String paragraph;
			while ((paragraph = bufferedReader.readLine()) != null) {
				// Tokenizes the current paragraph...
				FrenchAnalyser frenchAnalyser = new FrenchAnalyser(paragraph);
				// Gets the token after analysing the current paragraph...
				Set<String> terms = frenchAnalyser.getToken();
				for (String term : terms) {
					// For monitoring...
					System.out.println(term);
					// Writes the term data by integrating it...
					jsonWriter.write(term);
				}
			}
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	/**
	 * The {@code CNRTLParser} class defines a parser for the HTML pages of the CNRTL.
	 * 
	 * @author Andrei Zabolotnîi
	 * @author Cyril Marilier
	 */
	public static class CNRTLParser {

		public Element getFirstHTMLElement(String url, String cssQuery) {

			return getHTMLElements(url, cssQuery).first();
		}

		public Element getLastHTMLElement(String url, String cssQuery) {

			return getHTMLElements(url, cssQuery).last();
		}

		public Elements getHTMLElements(String url, String cssQuery) {

			try {
				return (((Jsoup.connect(url)).get()).select(cssQuery));
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

			return null;
		}

	}

}
