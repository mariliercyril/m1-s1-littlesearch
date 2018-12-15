package edu.fromatoz.littlesearch.dataintegrator;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.jsoup.nodes.Element;

import edu.fromatoz.littlesearch.app.DataIntegrator;
import edu.fromatoz.littlesearch.app.DataIntegrator.CNRTLParser;

import edu.fromatoz.littlesearch.tool.Separator;

/**
 * The {@code FrenchAnalyser} class defines an analyser for the French language.
 * (The analysis of the French language by Lucene is not always reliable.)
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public class FrenchAnalyser {

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
	 * <li><b>disambiguate proper nouns which have not been filtered</b> (by the method "{@code disambiguate}");</li>
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
		// Does the task 5: Filters the proper nouns
		// TODO: POSSIBLE FEATURE: To develop a NER (Named-Entity Recognition) for proper noun.
		tokens = filterProperNouns(tokens);
		// Does the task 6: Disambiguates proper nouns which have not been filtered
		tokens = disambiguate(tokens, "Augustin", "Augustin_PROPER_NOUN");
		tokens = disambiguate(tokens, "Babylone", "Babylone_PROPER_NOUN");
		tokens = disambiguate(tokens, "Jersey", "Jersey_PROPER_NOUN");
		tokens = disambiguate(tokens, "Lie", "Lie_PROPER_NOUN");
		tokens = disambiguate(tokens, "Louis", "Louis_PROPER_NOUN");
		tokens = disambiguate(tokens, "Paris", "Paris_PROPER_NOUN");
		tokens = disambiguate(tokens, "Pascal", "Pascal_PROPER_NOUN");
		tokens = disambiguate(tokens, "Sceaux", "Sceaux_PROPER_NOUN");
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

		for (StopFrenchWords stopFrenchWords : StopFrenchWords.values()) {
			(stopFrenchWords.getStopWords()).stream().forEach(w -> {
				tokens.removeIf(t -> (t.toLowerCase()).equals(w));
			});
		}

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

		// The French pronouns
		/**
		 * The singleton instance for the <b>subject personal pronouns</b>.
		 */
		SUBJECT_PERSONAL_PRONOUNS("je", "j", "tu", "il", "elle", "on", "nous", "vous", "ils", "elles"),
		/**
		 * The singleton instance for the <b>complement personal pronouns</b>.
		 */
		COMPLEMENT_PERSONAL_PRONOUNS("me", "m", "moi", "te", "t", "toi", "se", "s", "soi", "le", "lui", "la", "nous", "vous", "leur", "les", "eux"),
		/**
		 * The singleton instance for the <b>possessive pronouns</b>.
		 */
		POSSESSIVE_PRONOUNS("mien", "mienne", "miens", "miennes", "tien", "tienne", "tiens", "tiennes", "sien", "sienne", "siens", "siennes", "nôtre", "nôtres", "vôtre", "vôtres", "leur", "leurs"),
		/**
		 * The singleton instance for the <b>simple relative pronouns</b>.
		 */
		SIMPLE_RELATIVE_PRONOUNS("qui", "que", "qu", "quoi", "dont", "où"),
		/**
		 * The singleton instance for the <b>composed relative pronouns</b>.
		 */
		COMPOSED_RELATIVE_PRONOUNS("lequel", "lesquels", "laquelle", "lesquelles", "auquel", "auxquels", "auxquelles", "duquel", "desquels", "desquelles"),
		/**
		 * The singleton instance for the <b>simple demonstrative pronouns</b>.
		 */
		SIMPLE_DEMONSTRATIVE_PRONOUNS("celui", "celle", "ce", "c", "ç", "ceux", "celles"), 
		/**
		 * The singleton instance for the <b>composed demonstrative pronouns</b>.
		 */
		COMPOSED_DEMONSTRATIVE_PRONOUNS("celui-ci", "celui-là", "celle-ci", "celle-là", "ceux-ci", "ceux-là", "celles-ci", "celles-là", "ceci", "cela", "ça"),

		// The French determinant
		/**
		 * The singleton instance for the <b>possessive determinants</b>.
		 */
		POSSESSIVE_DETERMINANTS("mon", "ma", "mes", "ton", "ta", "tes", "son", "sa", "ses", "notre", "nos", "votre", "vos", "leur", "leurs"),
		/**
		 * The singleton instance for the <b>demonstrative determinants</b>.
		 */
		DEMONSTRATIVE_DETERMINANTS("ce", "cet", "cette", "ces"),

		/**
		 * The singleton instance for the <b>coordination conjunctions</b>.
		 */
		COORDINATION_CONJUNCTIONS("mais", "ou", "et", "donc", "or", "ni", "car"),

		// The French articles
		/**
		 * The singleton instance for the <b>definite articles</b>.
		 */
		DEFINITE_ARTICLES("le", "la", "l", "les", "du", "de", "d", "des", "à", "au", "aux"),
		/**
		 * The singleton instance for the <b>indefinite articles</b>.
		 */
		INDEFINITE_ARTICLES("un", "une", "des", "de", "d"),

		// Other stop words...
		/**
		 * The singleton instance for <b>other stop words</b>.
		 */
		OTHER_STOP_WORDS("ici", "là", "vers", "sur", "sous", "dans", "en", "ne", "n", "pas", "plus", "moins", "fois"),

		// Stop characters...
		/**
		 * The singleton instance for <b>stop characters</b>.
		 */
		STOP_CHARACTERS("-", "–");

		private final String[] stopWords;

		private StopFrenchWords(String... stopWords) {

			this.stopWords = stopWords;
		}

		/**
		 * Returns the stop words.
		 * 
		 * @return the stop words as a {@code TreeSet}
		 */
		private Set<String> getStopWords() {

			return new TreeSet<>(Arrays.asList(stopWords));
		}

	}

}
