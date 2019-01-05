package edu.fromatoz.littlesearch.searchengine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.analysis.fr.FrenchAnalyzer;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.fromatoz.littlesearch.app.SearchEngine;

import edu.fromatoz.littlesearch.tool.Separator;

/**
 * The {@code Littlesearch} class defines a search engine.
 * <p>
 * Therefore, this class provides the methods for indexing then searching:
 * <ul>
 * <li>{@code index(String... texts)}</li>
 * <li>{@code search(String words)}</li>
 * </ul>
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public class Littlesearch {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(Littlesearch.class);

	// The Analyzer as a FrenchAnalyzer (constructed with the default stop words for the French language).
	private static final Analyzer ANALYZER = new FrenchAnalyzer();

	// The name of the field which should contain the content of text...
	private static final String CONTENT_FIELD_NAME = "content";

	// The name of the field which should contain the path of a text file...
	private static final String PATH_FIELD_NAME = "path";

	// The directory where the index will be stored:
	private static Directory indexDirectory;

	// The reader of the index:
	private static DirectoryReader indexReader;

	// The writer of the index:
	private static IndexWriter indexWriter;

	// Adds a private constructor to hide the implicit public one (indicated by SonarQube).
	private Littlesearch() {

		throw new IllegalStateException("Littlesearch class");
	}

	/**
	 * Tries to index the texts of the corpus.
	 * 
	 * @return <i>true</i>, if the indexing is successful; <i>false</i>, if it isn't
	 */
	public static boolean indexTexts() {

		try {
			// Opens the directory, on the disk (normally in a temporary way), where the index is going to be stored.
			indexDirectory = FSDirectory.open(Paths.get(System.getProperty("java.io.tmpdir"), (Separator.SLASH).getValue(), "littlesearch_index"));

			// Defines a configuration for giving the analyzer to the index writer...
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(ANALYZER);
			indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

			// Creates an index writer.
			indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);

			// Checks whether the TC is a directory...
			if ((SearchEngine.TEXT_CORPUS_DIRECTORY).isDirectory()) {
				// Checks whether the TC (as a directory) is empty...
				if (((SearchEngine.TEXT_CORPUS_DIRECTORY).list()).length > 0) {
					// Gets the files of the TC
					File[] files = (SearchEngine.TEXT_CORPUS_DIRECTORY).listFiles();
					for (File file : files) {
						index(file);
					}

					// "Commits all pending changes (added and deleted documents, segment merges, added indexes, etc.) to the index..."
					// (Necessary to solve "org.apache.lucene.index.IndexNotFoundException: no segments* file found in MMapDirectory".)
					indexWriter.commit();

					return true;
				} else {
					throw new NoSuchFileException("The TC's directory is empty.");
				}
			} else {
				throw new NoSuchFileException("The TC's directory doesn't exist or it isn't a directory.");
			}
		} catch (IOException ioe) {
			LOGGER.error(ioe);
		}

		return false;
	}

	/**
	 * Indexes the text of a file that is as a parameter.
	 * 
	 * @param textFile
	 *  the file of a text
	 */
	private static void index(File textFile) {

		try {
			// Constructs a document from the file of which the path which is as a parameter...
			org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
			// Stores the path which is as a parameter.
			doc.add(new StringField(PATH_FIELD_NAME, textFile.getPath(), Field.Store.YES));
			// Store the content (which is text) of the file of which the path which is as a parameter.
			doc.add(new TextField(CONTENT_FIELD_NAME, getText(textFile), Field.Store.YES));
			// Indexes the document... (Updates it, if it exists...)
			indexWriter.updateDocument(new Term(PATH_FIELD_NAME, textFile.toString()), doc);
		} catch (IOException ioe) {
			LOGGER.error(ioe);
		}
	}

	/**
	 * Returns the text of a file that is as a parameter.
	 * 
	 * @param textFile
	 *  the file of a text
	 * 
	 * @return the text to be indexed
	 */
	private static String getText(File textFile) {

		String text = "";

		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile.toString())))) {
			StringBuilder textBuilder = new StringBuilder();
			String paragraph;
			while ((paragraph = bufferedReader.readLine()) != null) {
				textBuilder.append(paragraph + (Separator.NEW_LINE).getValue());
			}
			text = textBuilder.toString();
		} catch (FileNotFoundException fnfe) {
			LOGGER.error(fnfe);
		} catch (IOException ioe) {
			LOGGER.error(ioe);
		}

		return text;
	}

	/**
	 * Searches, in the indexed texts, for words (which are as a parameter).
	 * 
	 * @param words
	 *  the set of words which should be searched
	 * 
	 * @return the list of the documents where at least one of the words appears (this list could be empty)
	 */
	public static List<Document> search(String words) {

		List<Document> documents = new ArrayList<>();

		try {
			// Opens the directory where the index was stored...
			indexReader = DirectoryReader.open(indexDirectory);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			// Parses a query for searching for words in the indexed content.
			QueryParser queryParser = new QueryParser(CONTENT_FIELD_NAME, ANALYZER);
			Query query = queryParser.parse(words);
			// Gets meta-information of the top 5 documents (sorted by relevance, the default sorting mode)...
			TopDocs foundDocs = indexSearcher.search(query, 5);
			// Adds the corresponding documents to the list...
			for (ScoreDoc hit : foundDocs.scoreDocs) {
				documents.add(new Littlesearch.Document(hit));
			}
		} catch (IOException ioe) {
			LOGGER.error(ioe);
		} catch (ParseException pe) {
			LOGGER.error(pe);
		} finally {
			try {
				if (indexReader != null) {
					indexReader.close();
				}
				if (indexDirectory != null) {
					indexDirectory.close();
				}
			} catch (IOException ioe) {
				LOGGER.error(ioe);
			}
		}

        return documents;
	}

	public static class Document {

		private int number;
		private String content;
		private float score;

		private Document(ScoreDoc hit) {

			number = hit.doc;
			score = hit.score;
			try {
				content = (indexReader.document(number)).get(CONTENT_FIELD_NAME);
			} catch (IOException ioe) {
				LOGGER.error(ioe);
			}
		}

		public int getNumber() {

			return number;
		}

		public float getScore() {

			return score;
		}

		public String getContent() {

			return content;
		}

	}

}
