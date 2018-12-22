package edu.fromatoz.littlesearch.app;

import java.io.File;
import java.io.UnsupportedEncodingException;

import java.util.List;
import java.util.ListIterator;

import edu.fromatoz.littlesearch.searchengine.Synonymysearch;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;

import edu.fromatoz.littlesearch.searchengine.Littlesearch;

import edu.fromatoz.littlesearch.tool.Extension;
import edu.fromatoz.littlesearch.tool.Separator;

/**
 * The {@code SearchEngine} class is the "main" class of our search engine.
 * (Lucene is used.)
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public class SearchEngine {

	/**
	 * The name of the directory of the Text Corpus.
	 */
	private static final String TEXT_CORPUS_DIRECTORY_NAME = "mathematicians";
	/**
	 * The directory of the Text Corpus.
	 */
	public static final File TEXT_CORPUS_DIRECTORY = new File(TEXT_CORPUS_DIRECTORY_NAME);

	/**
	 * Format of the extended name of a text file.
	 */
	private static final String TEXT_FILE_EXTENDED_NAME_FORMAT = "%S" + Separator.POINT.getValue() + Extension.TEXT.getValue();
	/**
	 * Format of the path of a text file.
	 */
	public static final String TEXT_FILE_PATH_FORMAT = TEXT_CORPUS_DIRECTORY + Separator.SLASH.getValue() + TEXT_FILE_EXTENDED_NAME_FORMAT;

	private static StringBuilder exactWordsBuilder = new StringBuilder();
	private static StringBuilder wordsBuilder = new StringBuilder();

    /**
     *Logger
     */
    private static final Logger LOGGER = Logger.getLogger(SearchEngine.class);


    /**
	 * Allows an user to search for words into the text of the corpus.
	 * 
	 * @param args
	 *  the words which we would to find
	 */
	public static void main(String[] args) {

		// Gives the search words to the engine...
		String words = "";
		if (args.length > 0) {
			for (String word : args) {
				try {
					word = new String(word.getBytes("UTF-8"));
					buildWords(word);
				} catch (UnsupportedEncodingException uee) {
					uee.printStackTrace();
				}
			}
			words = exactWordsBuilder.toString();
			words += Synonymysearch.search(wordsBuilder.toString());
			words = words.trim();

			// For demo...
            LOGGER.debug("[" + String.join(", ", words.split((Separator.SPACE).getValue())) + "]" + (Separator.NEW_LINE).getValue());
			//System.out.println("[" + String.join(", ", words.split((Separator.SPACE).getValue())) + "]" + (Separator.NEW_LINE).getValue());
		} else {
		    LOGGER.info("Use: ./searchFor <word>...");
			//System.out.println("Use: ./searchFor <word>...");
			System.exit(0);
		}

		// Indexes the texts...
		if (!(Littlesearch.indexTexts())) {
			System.exit(1);
		} else {
			// If the indexing is successful, searches for words and for synonyms if exist...
			List<Document> hitDocuments = Littlesearch.search(words);
			if (hitDocuments.isEmpty()) {
			    LOGGER.info("Littlesearch ne trouve rien pour \"" + words + "\".");
				//System.out.println("Littlesearch ne trouve rien pour \"" + words + "\".");
				System.exit(1);
			} else {
				// If the engine finds at least one of the searched words in an indexed document, returns the document in question...
				ListIterator<Document> hitDocumentsIterator = hitDocuments.listIterator();
				int i = 0;
				while (hitDocumentsIterator.hasNext()) {
					Document hitDocument = hitDocumentsIterator.next();
					LOGGER.info("Document " + ++i + ":" + (Separator.NEW_LINE).getValue() + hitDocument.get("content"));
					//System.out.println("Document " + ++i + ":" + (Separator.NEW_LINE).getValue() + hitDocument.get("content"));
				}
			}
		}
	}

	private static void buildWords(String word) {

		if (word.startsWith("_") && word.endsWith("_")) {
			word = word.substring(1, word.length() - 1);
			exactWordsBuilder.append(word + (Separator.SPACE).getValue());
		} else {
			wordsBuilder.append(word + (Separator.SPACE).getValue());
		}
	}

}
