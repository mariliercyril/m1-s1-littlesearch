package edu.fromatoz.littlesearch;

import java.io.IOException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

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

	// The Analyzer as a FrenchAnalyzer (constructed with the default stop words for the French language).
	private static final Analyzer ANALYZER = new FrenchAnalyzer();

	// The name of the field which should contain content of txt file...
	private static String contents = "contents";

	// The name of the field which should contain path to txt file...
	private static String path = "path";

	// The directory where the index will be stored:
	private static Directory indexDirectory;

	private static IndexWriter indexWriter = null;

	// Adds a private constructor to hide the implicit public one (indicated by SonarQube).
	private Littlesearch() {

		throw new IllegalStateException("Utility class");
	}

	public static boolean indexFiles() throws IOException{

        // Opens the directory, on the disk (normally in a temporary way), where the index is going to be stored.
        indexDirectory = FSDirectory.open(Paths.get(System.getProperty("java.io.tmpdir")));

        // Defines a configuration for giving the analyzer to the index writer...
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(ANALYZER);
        //create a new index if there is not already an index at the provided path
		// and otherwise open the existing index.
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        // Init an index writer.
		indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);

        // Deletes, if exist, all the documents which are in the index.
        indexWriter.deleteAll();

        final Path path = Paths.get(App.docsPath);

	    //check if path is Directory
        if(Files.isDirectory(path) && path.toFile().list().length > 0){
            //Iterate directory
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs){
                    //index this file
                    if(index(file))
                    {
                        return FileVisitResult.CONTINUE;
                    }
                    return FileVisitResult.TERMINATE;
                }
            });
        }
        //if it is not directory
        else{
        	throw new NoSuchFileException("Directory is empty or it's not a directory");
		}

		try {
			if (indexWriter != null) {
				indexWriter.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
        return true;
    }

    /**
     * Indexes texts (which are as parameters).
     *
     * @param file
     *  doc with the text corpus
     *
     * @return <i>true</i>, if the indexing of the texts is successful; <i>false</i>, if it isn't
     */
	private static boolean index(Path file) {

		boolean indexingIsSuccessful = false;

		try(InputStream stream = Files.newInputStream(file)) {
			// Indexes the file...
			Document doc = new Document();
			//store path of current file
			doc.add(new StringField(path, file.toString(),Field.Store.YES));
			//store content of current file
			doc.add(new TextField(contents, new String(Files.readAllBytes(file),StandardCharsets.ISO_8859_1),Field.Store.YES));
			//index current doc
            indexWriter.updateDocument(new Term(path,file.toString()),doc);
			indexingIsSuccessful = true;
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
		return indexingIsSuccessful;
	}

	/**
	 * Searches, in the indexed texts, for the words (which are as a parameter).
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
			QueryParser queryParser = new QueryParser(contents, ANALYZER);
			Query query = queryParser.parse(words);
			// Gets meta-information of the top 10 documents (sorted by relevance, the default sorting mode)...
			ScoreDoc[] hits = indexSearcher.search(query, 10, new Sort()).scoreDocs;
			// Adds the corresponding documents to the list...
			for (ScoreDoc hit : hits) {
				hitDocuments.add(indexSearcher.getIndexReader().document(hit.doc));
			}

			directoryReader.close();

			// Closes the directory where the index is stored.
			indexDirectory.close();
		} catch (IOException | ParseException ioe) {
			ioe.printStackTrace();
		}

        return hitDocuments;
	}

}
