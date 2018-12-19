package edu.fromatoz.littlesearch.dataintegrator;

import java.io.UnsupportedEncodingException;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import edu.fromatoz.littlesearch.tool.Separator;
import edu.fromatoz.littlesearch.tool.ValuesFileReader;

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

	private Set<String> tokens;

	public FrenchAnalyser(String text) {

		this.text = text;
		// For monitoring...
		System.out.print((Separator.NEW_LINE).getValue() + this.text);
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
	 * <li><b>annotate proper nouns</b> (by the method "{@code annotate}");</li>
	 * <li><b>disambiguate "être" tokens</b> (by the method "{@code disambiguate}");</li>
	 * <li><b>disambiguate "avoir" tokens</b> (by the method "{@code disambiguate}");</li>
	 * <li><b>disambiguate "aujourd'hui" tokens</b> (by the method "{@code disambiguate}");</li>
	 * <li><b>disambiguate "J.-C." tokens</b> (by the method "{@code disambiguate}");</li>
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
		tokens = tokenizeByWhitespace();
		// Does the task 4: Filters the stop words
		filterStopWords();
		// Does the task 5: Annotates proper nouns (for a NER (Named-Entity Recognition) for proper noun)
		annotate("proper.nouns");
		// Does the task 6: Disambiguates "être" tokens
		disambiguate("être", "été");
		disambiguate("être", "est");
		// Does the task 7: Disambiguates "avoir" tokens
		disambiguate("avoir", "a");
		// Does the task 8: Disambiguates "aujourd'hui" tokens
		disambiguate("aujourd'hui", "aujourd");
		disambiguate("aujourd'hui", "hui");
		// Does the task 9: Disambiguates "J.-C." tokens
		disambiguate("J.-C.", "-C");

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
	 * Filters the stop words.
	 */
	private void filterStopWords() {

		Set<String> stopWords = getWords("pronouns");
		stopWords.addAll(getWords("determinants"));
		stopWords.addAll(getWords("articles"));
		stopWords.addAll(getWords("others"));

		stopWords.stream().forEach(w -> tokens.removeIf(t -> (t.toLowerCase()).equals(w)));

		tokens.removeIf(String::isEmpty);
	}

	/**
	 * Annotates a token which is contained by the set of tokens.
	 * 
	 * @param namedEntityFileName
	 *  the name of the named-entity file
	 */
	private void annotate(String namedEntityFileName) {

		for (String properNoun : getWords(namedEntityFileName)) {
			disambiguate(properNoun + "[" + (namedEntityFileName.substring(0, namedEntityFileName.length() - 1)).toUpperCase() + "]", properNoun);
		}
	}

	/**
	 * Disambiguates a token which is contained by the set of tokens.
	 * 
	 * @param disambiguatedToken
	 *  a token for disambiguating the token in question
	 * @param ambiguousToken
	 *  the token to be disambiguated
	 */
	private void disambiguate(String disambiguatedToken, String ambiguousToken) {

		if (tokens.contains(ambiguousToken)) {
			tokens.add(disambiguatedToken);
			tokens.remove(ambiguousToken);
		}
	}

	/**
	 * Returns words as a set of {@code String}.
	 * 
	 * @param valuesFileName
	 *  the name of the values file
	 * 
	 * @return words as a {@code Set<String>}
	 */
	private Set<String> getWords(String valuesFileName) {

		Set<String> words = new TreeSet<>();

		Set<String> keys = VALUES_FILE_READER.getKeys(valuesFileName);
		for (String key : keys) {
			for (String value : (VALUES_FILE_READER.getStringValue(valuesFileName, key)).split("\\s")) {
				try {
					words.add(new String(value.getBytes("ISO-8859-1")));
				} catch (UnsupportedEncodingException uee) {
					uee.printStackTrace();
				}
			}
		}

		return words;
	}

}
