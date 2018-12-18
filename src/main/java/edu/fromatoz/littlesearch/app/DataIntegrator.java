package edu.fromatoz.littlesearch.app;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.Set;

import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.jsoup.select.Elements;

import edu.fromatoz.littlesearch.dataintegrator.FrenchAnalyser;

import edu.fromatoz.littlesearch.dataintegrator.controller.*;

import edu.fromatoz.littlesearch.dataintegrator.model.JSONWriter;

import edu.fromatoz.littlesearch.tool.Separator;

/**
 * The {@code DataIntegrator} class is the "main" class of an integrator of data for the search engine.
 * <p>This is a sort of ETL (Extract-Transform-Load) application: this extracts data from several pages of
 * the <a href="http://www.cnrtl.fr">CNRTL</a> (Centre National de Ressources Textuelles et Lexicales),
 * transforms it by performing NLP (Natural Language Processing) tasks (among others, <i>tokenization</i>
 * with a {@link FrenchAnalyser}, <i>part-of-speech tagging</i> with a {@link FrenchTagger} and <i>lemmatisation</i>
 * with a {@link FrenchLemmatiser}) and loads it into a warehouse.</p>
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public class DataIntegrator {

	/**
	 * The <b>communication protocol</b>.
	 */
	private static final String COMMUNICATION_PROTOCOL = "http";
	/**
	 * Format of the path of a resource.
	 */
	private static final String RESOURCE_PATH_FORMAT = Separator.SLASH.getValue() + "%s";

	/**
	 * The CNRTL's <b>domain name</b>.
	 */
	private static final String CNRTL_DOMAIN_NAME = "www.cnrtl.fr";
	/**
	 * The URL of the CNRTL's home page.
	 */
	private static final String CNRTL_HOME_PAGE_URL = COMMUNICATION_PROTOCOL + Separator.COLON.getValue() + "//" + CNRTL_DOMAIN_NAME;
	/**
	 * Format of the URL of a CNRTL's page.
	 */
	private static final String CNRTL_URL_FORMAT = CNRTL_HOME_PAGE_URL + RESOURCE_PATH_FORMAT;

	/**
	 * Format of the URL of the CNRTL's page of definition of a word.
	 */
	public static final String DEFINITION_FORMAT = String.format(CNRTL_URL_FORMAT, "definition") + RESOURCE_PATH_FORMAT;
	/**
	 * Format of the URL of the CNRTL's page of morphology of a word.
	 */
	public static final String MORPHOLOGY_FORMAT = String.format(CNRTL_URL_FORMAT, "morphologie") + RESOURCE_PATH_FORMAT;
	/**
	 * Format of the URL of the CNRTL's page of synonymy of a word.
	 */
	public static final String SYNONYMY_FORMAT = String.format(CNRTL_URL_FORMAT, "synonymie") + RESOURCE_PATH_FORMAT;

	/**
	 * Allows a developer to integrate data from the text of the corpus.
	 * 
	 * @param args
	 *  the name of the file of the text which we would to integrate
	 */
	public static void main(String[] args) {

		// Gives the text file name to the integrator...
		String textFileName = "RENE_DESCARTES";
		if (args.length > 0) {
			textFileName = args[0];
		}
		String textFilePath = String.format(SearchEngine.TEXT_FILE_PATH_FORMAT, textFileName);

		JSONWriter jsonWriter = new JSONWriter();

		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(textFilePath)))) {
			StringBuilder textBuilder = new StringBuilder();
			String paragraph;
			while ((paragraph = bufferedReader.readLine()) != null) {
				textBuilder.append(paragraph + "\n");
			}
			String text = textBuilder.toString();
			if (!(text.isEmpty())) {
				// Injects the text in question to the French analyser...
				FrenchAnalyser frenchAnalyser = new FrenchAnalyser(text);
				// Then gets the tokens after analysing the text in question
				Set<String> words = frenchAnalyser.getTokens();
				for (String word : words) {
					// For monitoring...
					System.out.println(word);
					// Loads the word data into the warehouse (as a JSON file)
					jsonWriter.load(word);
				}
			}
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * The {@code CNRTLParser} class defines a parser for the HTML pages of the CNRTL.
	 * <p>Several of the tools which we have developed for the data integrator
	 * need to parse the HTML of CNRTL's pages.</p>
	 * 
	 * @author Andrei Zabolotnîi
	 * @author Cyril Marilier
	 */
	public static class CNRTLParser {

		/**
		 * Returns the first of the elements returned by the value of the CSS class (or the CSS ID).
		 * 
		 * @param url
		 *  the URL of the CNRTL's page in question
		 * @param cssQuery
		 *  the CSS containing the value of the class (or the id)
		 * 
		 * @return elements by the value of the CSS class (or the CSS ID).
		 */
		public Element getFirstHTMLElement(String url, String cssQuery) {

			Elements elements = getHTMLElements(url, cssQuery);

			if (elements != null) {
				return elements.first();
			} else {
				return null;
			}
		}

		/**
		 * Returns the last of the elements returned by the value of the CSS class (or the CSS ID).
		 * 
		 * @param url
		 *  the URL of the CNRTL's page in question
		 * @param cssQuery
		 *  the CSS containing the value of the class (or the id)
		 * 
		 * @return elements by the value of the CSS class (or the CSS ID).
		 */
		public Element getLastHTMLElement(String url, String cssQuery) {

			Elements elements = getHTMLElements(url, cssQuery);

			if (elements != null) {
				return elements.last();
			} else {
				return null;
			}
		}

		/**
		 * Returns elements by the value of the value of the CSS class (or the CSS ID).
		 * 
		 * @param url
		 *  the URL of the CNRTL's page in question
		 * @param cssQuery
		 *  the CSS containing the value of the class (or the id)
		 * 
		 * @return elements by the value of the CSS class (or the CSS ID).
		 */
		public Elements getHTMLElements(String url, String cssQuery) {

			Elements elements = null;

			try {
				Thread.sleep(2_000);
				
				Document document = (Jsoup.connect(url)).get();
				if (document != null) {
					elements = document.select(cssQuery);
				}
			} catch (InterruptedException | IOException ie) {
				Thread.currentThread().interrupt();
			}

			return elements;
		}

	}

}
