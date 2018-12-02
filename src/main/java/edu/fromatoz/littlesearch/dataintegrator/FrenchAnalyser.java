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

	public Set<String> getToken() {

		// Removes all the signs of punctuation...
		unpunctuate();
		// Filters the digits...
		// TODO: POSSIBLE FEATURE: To develop a NER (Named-Entity Recognition) for digital data.
		filterDigits();
		// Tokenizes the text
		Set<String> token = tokenize();
		// Filters the stop words
		token = filterStopWords(token);
		// Filters the proper names
		// TODO: POSSIBLE FEATURE: To develop a NER (Named-Entity Recognition) for proper noun.
		token = filterProperNouns(token);
		// Disambiguates about the verb "être"
		token = disambiguate(token, "être", "est");
		// Disambiguates about the verb "avoir"
		token = disambiguate(token, "avoir", "a");

		return token;
	}

	private void unpunctuate() {

		text = text.replaceAll("[\\.\\?!,;:\\(\\)\\[\\]\\{\\}\"'«»]", " ");
	}

	private void filterDigits() {

		text = text.replaceAll("\\d", " ");
	}

	private Set<String> tokenize() {

		return new TreeSet<>(Arrays.asList(text.split(" ")));
	}

	private Set<String> filterStopWords(Set<String> token) {

		for (StopFrenchWords stopFrenchWords : StopFrenchWords.values()) {
			(stopFrenchWords.getStopWords()).stream().forEach(s -> {
				token.removeIf(w -> w.equals(s));
			});
		}

		token.removeIf(w -> w.isEmpty());

		return token;
	}

	private Set<String> filterProperNouns(Set<String> token) {

		token.removeIf(w -> (String.valueOf(w.charAt(0))).matches("[A-Z]"));

		return token;
	}

	private Set<String> disambiguate(Set<String> token, String unambiguousWord, String ambiguousWord) {

		if (token.contains(ambiguousWord)) {
			token.add(unambiguousWord);
			token.remove(ambiguousWord);
		}

		return token;
	}

	/**
	 * A stop-French-words, such as "le".
	 * <p>{@code StopFrenchWords} is an enum representing current stop French words distributed into parts of speech:
	 * <ul>
	 * <li>DEFINITE ARTICLES</li>
	 * <li>INDEFINITE ARTICLES</li>
	 * <li>OTHER ARTICLES</li>
	 * <li>OTHER STOP WORDS</li>
	 * <li>...</li>
	 * </ul>
	 * </p>
	 * 
	 * @author Andrei Zabolotnîi
	 * @author Cyril Marilier
	 */
	private enum StopFrenchWords {

		DEFINITE_ARTICLES("le", "la", "les"),
		INDEFINITE_ARTICLES("un", "une", "des"),
		OTHER_ARTICLES("l", "d", "de", "du"),
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
