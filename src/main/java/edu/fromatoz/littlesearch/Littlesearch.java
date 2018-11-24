package edu.fromatoz.littlesearch;

import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;

import java.nio.file.attribute.BasicFileAttributes;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.analysis.fr.FrenchAnalyzer;

import org.apache.lucene.document.Document;
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

	// The Analyzer as a FrenchAnalyzer (constructed with the default stop words for the French language).
	private static final Analyzer ANALYZER = new FrenchAnalyzer();

	// The name of the field which should contain the content of a text...
	private static String content = "content";

	// The name of the field which should contain the path of a text file...
	private static String path = "path";

	// The directory where the index will be stored:
	private static Directory indexDirectory;

	// The reader of the index:
	private static DirectoryReader indexReader;

	// The writer of the index:
	private static IndexWriter indexWriter;
	
	

	// Adds a private constructor to hide the implicit public one (indicated by SonarQube).
	private Littlesearch() {

		throw new IllegalStateException("Utility class");
	}

	/**
	 * Tries to index the texts of the corpus.
	 * 
	 * @return <i>true</i>, if the indexing is successful; <i>false</i>, if it isn't
	 */
	public static boolean indexTexts() {

		try {
			// Opens the directory, on the disk (normally in a temporary way), where the index is going to be stored.
			indexDirectory = FSDirectory.open(Paths.get(System.getProperty("java.io.tmpdir")));

			// Defines a configuration for giving the analyzer to the index writer...
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(ANALYZER);

			// Creates an index writer.
			indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);

			// Checks whether the TC is a directory...
			if ((App.TEXT_CORPUS).isDirectory()) {
				// Checks whether the TC (as a directory) is empty...
				if (((App.TEXT_CORPUS).list()).length > 0) {
					// Walks in the TC as in a file tree.
					Files.walkFileTree((App.TEXT_CORPUS).toPath(), new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path textFilePath, BasicFileAttributes attributes) {
							// Indexes text in the file...
							if (index(textFilePath)) {
								return FileVisitResult.CONTINUE;
							}
							return FileVisitResult.TERMINATE;
						}
					});
					return true;
				} else {
					throw new NoSuchFileException("The TC's directory is empty.");
				}
			} else {
				throw new NoSuchFileException("The TC's directory doesn't exist or it isn't a directory.");
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (indexWriter != null) {
					indexWriter.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		return false;
	}

	/**
	 * Indexes the text of a file of which the path is as a parameter.
	 * 
	 * @param textFilePath
	 *  the path of a text file
	 * 
	 * @return <i>true</i>, if the indexing of the text in question is successful; <i>false</i>, if it isn't
	 */
	private static boolean index(Path textFilePath) {

		try {
			// Constructs a document from the file of which the path which is as a parameter...
			Document doc = new Document();
			// Stores the path which is as a parameter.
			doc.add(new StringField(path, textFilePath.toString(), Field.Store.YES));
			// Store the content (which is text) of the file of which the path which is as a parameter.
			doc.add(new TextField(content, new String(Files.readAllBytes(textFilePath), StandardCharsets.UTF_8), Field.Store.YES));
			// Indexes the document... (Updates it, if exists...)
			indexWriter.updateDocument(new Term(path, textFilePath.toString()), doc);
		} catch (IOException ioe) {
			return false;
		}

		return true;
	}

	/**
	 * Searches, in the indexed texts, for the words (which are as a parameter).
	 * 
	 * @param words
	 *  the set of words which should be searched
	 * 
	 * @return the list of the documents where at least one of the words appears (this list could be empty)
	 */
	public static List<Document> search(String words) {

		List<Document> hitDocs = new ArrayList<Document>();

		try {
			// Opens the directory where the index was stored...
			indexReader = DirectoryReader.open(indexDirectory);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			// Parses a query for searching for words in the indexed content.
			QueryParser queryParser = new QueryParser(content, ANALYZER);
			Query query = queryParser.parse(words);
			// Gets meta-information of the top 10 documents (sorted by relevance, the default sorting mode)...
			TopDocs foundDocs = indexSearcher.search(query, 10);
			// Adds the corresponding documents to the list...
			for (ScoreDoc hit : foundDocs.scoreDocs) {
				hitDocs.add(indexSearcher.getIndexReader().document(hit.doc));
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (ParseException pe) {
			pe.printStackTrace();
		} finally {
			try {
				if (indexReader != null) {
					indexReader.close();
				}
				if (indexDirectory != null) {
					indexDirectory.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

        return hitDocs;
	}

}
