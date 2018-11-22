package edu.fromatoz.littlesearch;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import org.apache.lucene.document.Document;

/**
 * This class contains the main method of the application:
 * When we use Littlesearch, the application tries to index the texts and,
 * if the indexing is successful, search for the words entered by the user
 * (in the texts indexed).
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public class App {
	//Input folder
	protected static final String docsPath = "data";

	public static void main(String[] args) throws IOException {

		// Allows to get the words to be searched by the command.
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
		if (!Littlesearch.indexFiles()) {
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
					System.out.println("Document " + ++i + ":\n" + hitDocument.get("path"));
				}
			}
		}
	}

}
