package edu.fromatoz.littlesearch.dataintegrator;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * The {@code FrenchAnalyser} class defines an analyser for the French language.
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public class FrenchAnalyser {

	private String text;

	public FrenchAnalyser(String text) {

		this.text = text;
	}

	/**
	 * Returns a set of <b>tokens</b> from the text in question.
	 * <p>
	 * Gets the <i>tokens</i> after performing the following scheduling of tasks:
	 * <ol>
	 * <li><i>remove all the signs of punctuation</i> (by the method "<b>unpunctuate()</b>");</li>
	 * <li><i>filter the digits</i> (by the method "<b>filterDigits()</b>");</li>
	 * <li><i>tokenize the text</i> (by the method "<b>tokenize()</b>"), task which returns a first set of <i>tokens</i>;</li>
	 * <li><i>filter the stop words</i> (by the method "<b>filterStopWords(</b>tokens<b>)</b>");</li>
	 * <li><i>filter the proper nouns</i> (by the method "<b>filterProperNouns(</b>tokens<b>)</b>");</li>
	 * <li><i>disambiguate the verb "est"</i> (by the method "<b>disambiguate(</b>tokens<b>,</b> "est"<b>,</b> "être"<b>)</b>");</li>
	 * <li><i>disambiguate the word "a"</i> (by the method "<b>disambiguate(</b>tokens<b>,</b> "a"<b>,</b> "avoir"<b>)</b>");</li>
	 * </ol>
	 * 
	 * @return a set of tokens from the text as a parameter
	 */
	public Set<String> getTokens() {

		// Does the task 1: Removes all the signs of punctuation...
		unpunctuate();
		// Does the task 2: Filters the digits...
		// TODO: POSSIBLE FEATURE: To develop a NER (Named-Entity Recognition) for digital data.
		filterDigits();
		// Does the task 3: Tokenizes the text
		Set<String> tokens = tokenize();
		// Does the task 4: Filters the stop words
		tokens = filterStopWords(tokens);
		// Does the task 5: Filters the proper nouns
		// TODO: POSSIBLE FEATURE: To develop a NER (Named-Entity Recognition) for proper noun.
		tokens = filterProperNouns(tokens);
		// Does the task 6: Disambiguates the verb "est"
		tokens = disambiguate(tokens, "est", "être");
		// Does the task 7: Disambiguates the verb "a"
		tokens = disambiguate(tokens, "a", "avoir");

		return tokens;
	}

	/**
	 * Removes all the signs of punctuation.
	 */
	private void unpunctuate() {

		text = text.replaceAll("[\\.\\?!,;:\\(\\)\\[\\]\\{\\}\"'«»]", " ");
	}

	/**
	 * Filter the digits.
	 */
	private void filterDigits() {

		text = text.replaceAll("\\d", " ");
	}

	/**
	 * Returns a first version of the set of tokens.
	 * 
	 * @return a first version of the set of tokens
	 */
	private Set<String> tokenize() {

		return new TreeSet<>(Arrays.asList(text.split(" ")));
	}

	/**
	 * Filter the stop words and returns a new version of the set of tokens.
	 * 
	 * @param tokens
	 * 	a version of the set of tokens
	 * 
	 * @return a new version of the set of tokens (it could be the previous version,
	 * the version which is as a parameter of the method)
	 */
	private Set<String> filterStopWords(Set<String> tokens) {

		for (StopFrenchWords stopFrenchWords : StopFrenchWords.values()) {
			(stopFrenchWords.getStopWords()).stream().forEach(w -> {
				tokens.removeIf(t -> t.equals(w));
			});
		}

		tokens.removeIf(t -> t.isEmpty());

		return tokens;
	}

	/**
	 * Filter the proper nouns and returns a new version of the set of tokens.
	 * 
	 * @param tokens
	 * 	a version of the set of tokens
	 * 
	 * @return a new version of the set of tokens (it could be the previous version,
	 * the version which is as a parameter of the method)
	 */
	private Set<String> filterProperNouns(Set<String> tokens) {

		tokens.removeIf(t -> (String.valueOf(t.charAt(0))).matches("[A-Z]"));

		return tokens;
	}

	/**
	 * Disambiguate a word, <i>w</i>, which is contained by the set of tokens
	 * and returns a new version of the set of tokens.
	 * 
	 * @param tokens
	 * 	a version of the set of tokens
	 * @param ambiguousWord
	 *  the word to be disambiguated
	 * @param unambiguousWord
	 *  a word for disambiguating the word in question
	 * 
	 * @return a new version of the set of tokens (it could be the previous version,
	 * the version which is as a parameter of the method)
	 */
	private Set<String> disambiguate(Set<String> tokens, String ambiguousWord, String unambiguousWord) {

		if (tokens.contains(ambiguousWord)) {
			tokens.add(unambiguousWord);
			tokens.remove(ambiguousWord);
		}

		return tokens;
	}

	/**
	 * A stop-French-words, such as "le".
	 * <p>
	 * {@code StopFrenchWords} is an enum representing current stop French words distributed into parts of speech:
	 * <ul>
	 * <li>DEFINITE ARTICLES</li>
	 * <li>INDEFINITE ARTICLES</li>
	 * <li>OTHER ARTICLES</li>
	 * <li>OTHER STOP WORDS</li>
	 * <li>...</li>
	 * </ul>
	 * 
	 * @author Andrei Zabolotnîi
	 * @author Cyril Marilier
	 */
	private enum StopFrenchWords {

		/**
		 * The singleton instance for the <b>definite articles</b>.
		 */
		DEFINITE_ARTICLES("le", "la", "les"),
		/**
		 * The singleton instance for the <b>indefinite articles</b>.
		 */
		INDEFINITE_ARTICLES("un", "une", "des"),
		/**
		 * The singleton instance for <b>other articles</b>.
		 */
		OTHER_ARTICLES("l", "d", "de", "du"),
		/**
		 * The singleton instance for <b>other stop words</b>.
		 */
		OTHER_STOP_WORDS("à", "ici", "là");

		private final String[] stopWords;

		private StopFrenchWords(String... stopWords) {

			this.stopWords = stopWords;
		}

		/**
		 * Returns the stop words.
		 * 
		 * @return the stop words as a {@link TreeSet}
		 */
		private Set<String> getStopWords() {

			return new TreeSet<>(Arrays.asList(stopWords));
		}

	}

}
