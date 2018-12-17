package edu.fromatoz.littlesearch.app;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ListIterator;

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
	private static final String TEXT_CORPUS_DIRECTORY_NAME = "texts";
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

	/**
	 * Allows an user to search for words into the text of the corpus.
	 * 
	 * @param args
	 *  the words which we would to find
	 */
	public static void main(String[] args) {

		// Gives the search words to the engine...
		StringBuilder wordsBuilder = new StringBuilder();
		String words = "";
		if (args.length > 0) {
			for (String word : args) {
				try {
					word = new String(word.getBytes("UTF-8"));
				} catch (UnsupportedEncodingException uee) {
					uee.printStackTrace();
				}
				wordsBuilder.append(word + " ");
			}
			words = wordsBuilder.toString();
			words = words.substring(0, words.lastIndexOf(' '));
		} else {
			System.out.println("Use: ./searchFor <word>...");
			System.exit(0);
		}

		// Indexes the texts...
		if (!(Littlesearch.indexTexts())) {
			System.exit(1);
		} else {
			// If the indexing is successful, searches for words...
			List<Document> hitDocuments = Littlesearch.search(words);
			if (hitDocuments.isEmpty()) {
				System.out.println("Littlesearch ne trouve rien pour \"" + words + "\".");
				System.exit(1);
			} else {
				// If the engine finds at least one of the searched words in an indexed document, returns the document in question...
				ListIterator<Document> hitDocumentsIterator = hitDocuments.listIterator();
				int i = 0;
				while (hitDocumentsIterator.hasNext()) {
					Document hitDocument = hitDocumentsIterator.next();
					System.out.println("Document " + ++i + ":\n" + hitDocument.get("content"));
				}
			}
		}
	}

}
