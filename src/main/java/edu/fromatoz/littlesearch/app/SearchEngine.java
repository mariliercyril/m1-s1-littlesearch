package edu.fromatoz.littlesearch.app;

import java.io.File;

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

	// The Text Corpus
	public static final File TEXT_CORPUS = new File("texts");

	private static final String TEXT_FILE_NAME_EXTENDED_FORMAT = "%S" + Separator.POINT.getValue() + Extension.TEXT.getValue();
	public static final String TEXT_FILE_PATH_FORMAT = TEXT_CORPUS + Separator.SLASH.getValue() + TEXT_FILE_NAME_EXTENDED_FORMAT;

	public static void main(String[] args) {

		// Gives the search words to the engine...
		StringBuilder wordsBuilder = new StringBuilder();
		String words = "";
		if (args.length > 0) {
			for (String word : args) {
				wordsBuilder.append(word + " ");
			}
			words = wordsBuilder.toString();
		} else {
			words = "mathématicien";
		}

		// Indexes the texts...
		if (!(Littlesearch.indexTexts())) {
			System.exit(1);
		} else {
			// If the indexing is successful, searches for words...
			List<Document> hitDocuments = Littlesearch.search(words);
			if (hitDocuments.isEmpty()) {
				System.out.println("Littlesearch n'a rien trouvé pour \"" + words + "\".");
				System.exit(1);
			} else {
				// If the engine finds at least one of the searched words in an indexed document, returns the document in question...
				ListIterator<Document> hitDocumentsIterator = hitDocuments.listIterator();
				int i = 0;
				while (hitDocumentsIterator.hasNext()) {
					Document hitDocument = hitDocumentsIterator.next();
					System.out.println("Document " + ++i + ":\n" + hitDocument.get("content") + "\n");
				}
			}
		}
	}

}
