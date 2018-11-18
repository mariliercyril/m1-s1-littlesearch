package edu.fromatoz.littlesearch;

import java.io.IOException;

import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * The {@code Littlesearch} class defines a search engine.
 * <p>
 * Therefore, this class provides the methods for indexing then searching:<ul>
 * <li>{@code index(String... texts)}</li>
 * <li>{@code search(String words)}</li>
 * </ul>
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public class Littlesearch {

	// The path of the directory for storing the index files:
	private static final String INDEX_FILES_DIRECTORY_PATH = "c:\\tmp\\index_test";

	// TODO: To replace the StandardAnalyzer with a FrenchAnalyzer (to construct an analyzer with a stop words for French).
	private static final Analyzer ANALYZER = new StandardAnalyzer();

	// The directory where the index will be stored:
	private static Directory indexDirectory;

	/**
	 * Indexes texts (which are as parameters).
	 * 
	 * @param texts
	 *  the text corpus
	 * 
	 * @return <i>true</i> if the indexing of the texts is successful; <i>false</i> if it isn't
	 */
	public static boolean index(String... texts) {

		boolean indexingIsSuccessful = false;

		try {
		    // Opens the directory, on the disk (normally on a temporary way), where the index is going to be stored.
			indexDirectory = FSDirectory.open(Paths.get(INDEX_FILES_DIRECTORY_PATH));

			// Defines a configuration for giving the analyzer to the index writer...
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(ANALYZER);

			// Creates an index writer.
			IndexWriter indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);
			// Deletes, if exist, all the documents which are in the index.
			indexWriter.deleteAll();
			// Indexes the text about Henri Poincaré...
			Document documentHenriPoincare = new Document();
			documentHenriPoincare.add(new Field("field_name", texts[0], TextField.TYPE_STORED));
			indexWriter.addDocument(documentHenriPoincare);
			// Indexes the text about Bernhard Riemann...
			Document documentBernhardRiemann = new Document();
			documentBernhardRiemann.add(new Field("field_name", texts[1], TextField.TYPE_STORED));
			indexWriter.addDocument(documentBernhardRiemann);

			indexWriter.close();

			indexingIsSuccessful = true;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return indexingIsSuccessful;
	}

	/**
	 * Searches, in the indexed texts, the words (which are as a parameter).
	 * 
	 * @param words
	 *  the set of words which should be searched
	 * 
	 * @return the list of document where at least one of the words appears (this list could be empty)
	 */
	public static List<Document> search(String words) {

		List<Document> hitDocuments = new ArrayList<Document>();

		try {
		    // Opens the index directory in order to allowing the search...
			DirectoryReader directoryReader = DirectoryReader.open(indexDirectory);
			IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
			// Parses a query for searching for words.
			QueryParser queryParser = new QueryParser("field_name", ANALYZER);
			Query query = queryParser.parse(words);
			// Gets the top 10 document's numbers, if exist.
			ScoreDoc[] hits = indexSearcher.search(query, 10, new Sort()).scoreDocs;
			// Adds the corresponding documents to the list...
			for (ScoreDoc hit : hits) {
				hitDocuments.add(indexSearcher.getIndexReader().document(hit.doc));
			}

			directoryReader.close();

			// Closes the directory where the index is stored.
			indexDirectory.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (ParseException pe) {
			pe.printStackTrace();
		}

		return hitDocuments;
	}

}
