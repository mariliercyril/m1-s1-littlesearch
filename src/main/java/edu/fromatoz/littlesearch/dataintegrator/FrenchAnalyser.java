package edu.fromatoz.littlesearch.dataintegrator;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.jsoup.nodes.Element;

import edu.fromatoz.littlesearch.app.DataIntegrator;
import edu.fromatoz.littlesearch.app.DataIntegrator.CNRTLParser;

import edu.fromatoz.littlesearch.tool.ValuesFileReader;
import edu.fromatoz.littlesearch.tool.Separator;

/**
 * The {@code FrenchAnalyser} class defines an analyser for the French language.
 * (The analysis of the French language by Lucene is not always reliable.)
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public class FrenchAnalyser {

	private static final ValuesFileReader VALUES_FILE_READER = ValuesFileReader.getInstance();

	private String text;

	private CNRTLParser cnrtlParser;

	public FrenchAnalyser(String text) {

		this.text = text;
		// For monitoring...
		System.out.print("\n" + this.text);

		cnrtlParser = new CNRTLParser();
	}

	/**
	 * Returns a set of <b>tokens</b> from the text in question.
	 * <p>
	 * The <i>tokens</i> are returned after performing the following scheduling of tasks:
	 * <ol>
	 * <li><b>discard the punctuation</b> (by the method "{@code discardPunctuation}");</li>
	 * <li><b>discard the digits</b> (by the method "{@code discardDigits}");</li>
	 * <li><b>tokenize the text</b> (by the method "{@code tokenizeByWhitespace}"), task which returns a first set of <i>tokens</i>;</li>
	 * <li><b>filter the stop words</b> (by the method "{@code filterStopWords}");</li>
	 * <li><b>filter the proper nouns</b> (by the method "{@code filterProperNouns}");</li>
	 * <li><b>annotate proper nouns which have not been filtered</b> (by the method "{@code annotate}");</li>
	 * <li><b>disambiguate "être" tokens</b> (by the method "{@code disambiguate}");</li>
	 * <li><b>disambiguate "avoir" tokens</b> (by the method "{@code disambiguate}");</li>
	 * <li><b>disambiguate "aujourd'hui" tokens</b> (by the method "{@code disambiguate}");</li>
	 * </ol>
	 * <p>(See the official page on the <a href="https://lucene.apache.org/solr/guide/7_5/language-analysis.html">Language Analysis</a>
	 * for the <i>search platform</i> <b>Solr</b>, for example.)
	 * 
	 * @return a set of tokens from the text which is as a parameter of the constructor
	 */
	public Set<String> getTokens() {

		// Does the task 1: Discards the punctuation...
		discardPunctuation();
		// Does the task 2: Discards the digits...
		// TODO: POSSIBLE FEATURE: To develop a NER (Named-Entity Recognition) for date (in particular).
		discardDigits();
		// Does the task 3: Tokenizes the text by whitespace (HORIZONTAL_TABULATION, LINE_FEED, FORM_FEED and CARRIAGE_RETURN)
		Set<String> tokens = tokenizeByWhitespace();
		// Does the task 4: Filters the stop words
		tokens = filterStopWords(tokens);
		/*
		 * A NER (Named-Entity Recognition) for proper noun...
		 */
		// Does the task 5: Filters the proper nouns
		tokens = filterProperNouns(tokens);
		// Does the task 6: Annotates proper nouns which have not been filtered
		tokens = annotate(tokens, "proper.noun");
		// Does the task 7: Disambiguates "être" tokens
		tokens = disambiguate(tokens, "été", "être");
		tokens = disambiguate(tokens, "est", "être");
		tokens = disambiguate(tokens, "Être", "être");
		// Does the task 8: Disambiguates "avoir" tokens
		tokens = disambiguate(tokens, "a", "avoir");
		// Does the task 9: Disambiguates "aujourd'hui" tokens
		tokens = disambiguate(tokens, "aujourd", "aujourd'hui");
		tokens = disambiguate(tokens, "hui", "aujourd'hui");

		// For monitoring...
		System.out.println(tokens);

		return tokens;
	}

	/**
	 * Discards the punctuation.
	 */
	private void discardPunctuation() {

		text = text.replaceAll("[\\.\\?!,;:\\(\\)\\[\\]\\{\\}\"'«»]", " ");
	}

	/**
	 * Discards the digits.
	 */
	private void discardDigits() {

		text = text.replaceAll("\\d", " ");
	}

	/**
	 * Returns a first version of the set of tokens.
	 * 
	 * @return a first version of the set of tokens
	 */
	private Set<String> tokenizeByWhitespace() {

		return new TreeSet<>(Arrays.asList(text.split("\\s")));
	}

	/**
	 * Filter the stop words and returns the set of tokens.
	 * 
	 * @param tokens
	 * 	an initial version of the set of tokens
	 * 
	 * @return a final version of the set of tokens (it could be the initial version)
	 */
	private Set<String> filterStopWords(Set<String> tokens) {

		Set<String> stopWords = getWords("pronouns");
		stopWords.addAll(getWords("determinants"));
		stopWords.addAll(getWords("articles"));
		stopWords.addAll(getWords("others"));

		stopWords.stream().forEach(w -> tokens.removeIf(t -> (t.toLowerCase()).equals(w)));

		tokens.removeIf(t -> t.isEmpty());

		return tokens;
	}

	/**
	 * Filter the proper nouns and returns the set of tokens.
	 * 
	 * @param tokens
	 * 	an initial version of the set of tokens
	 * 
	 * @return a final version of the set of tokens (it could be the initial version)
	 */
	private Set<String> filterProperNouns(Set<String> tokens) {

		tokens.removeIf(t -> {
			// Gets the initial...
			String initial = String.valueOf(t.charAt(0));
			// If the initial is in uppercase, tries to find the token in question...
			if (initial.equals(initial.toUpperCase())) {
				String url = String.format(DataIntegrator.SYNONYMY_FORMAT, t + Separator.SLASH.getValue());
				Element htmlElement = cnrtlParser.getFirstHTMLElement(url, "li[id=vitemselected]");
				return (htmlElement == null);
			} else {
				return false;
			}
		});

		return tokens;
	}

	/**
	 * Annotates a token which is contained by the set of tokens and returns the set of tokens.
	 * 
	 * @param tokens
	 * 	an initial version of the set of tokens
	 * @param nameEntityFileName
	 *  the name of the name entity file
	 * 
	 * @return a final version of the set of tokens (it could be the initial version)
	 */
	private Set<String> annotate(Set<String> tokens, String nameEntityFileName) {

		for (String properNoun : getWords(nameEntityFileName)) {
			tokens = disambiguate(tokens, properNoun, properNoun + "[" + nameEntityFileName.toUpperCase() + "]");
		}

		return tokens;
	}

	/**
	 * Disambiguates a token which is contained by the set of tokens and returns the set of tokens.
	 * 
	 * @param tokens
	 * 	an initial version of the set of tokens
	 * @param ambiguousToken
	 *  the token to be disambiguated
	 * @param disambiguatedToken
	 *  a token for disambiguating the token in question
	 * 
	 * @return a final version of the set of tokens (it could be the initial version)
	 */
	private Set<String> disambiguate(Set<String> tokens, String ambiguousToken, String disambiguatedToken) {

		if (tokens.contains(ambiguousToken)) {
			tokens.add(disambiguatedToken);
			tokens.remove(ambiguousToken);
		}

		return tokens;
	}

	private Set<String> getWords(String valuesFileName) {

		Set<String> words = new TreeSet<>();

		Set<String> keys = VALUES_FILE_READER.getKeys(valuesFileName);
		for (String key : keys) {
			for (String value : (VALUES_FILE_READER.getStringValue(valuesFileName, key)).split("\\s")) {
				words.add(value);
			}
		}

		return words;
	}

}
