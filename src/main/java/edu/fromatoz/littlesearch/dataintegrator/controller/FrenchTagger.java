package edu.fromatoz.littlesearch.dataintegrator.controller;

import org.jsoup.nodes.Element;

import edu.fromatoz.littlesearch.app.DataIntegrator;
import edu.fromatoz.littlesearch.app.DataIntegrator.CNRTLParser;

import edu.fromatoz.littlesearch.tool.Separator;

/**
 * The {@code FrenchTagger} class defines a tagger for French words:
 * it determines the part of speech for a French word.
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public class FrenchTagger {

	private CNRTLParser cnrtlParser;

	private PartOfSpeech partOfSpeech;
	private String canonicalForm;

	public FrenchTagger(String word) {

		word = word.toLowerCase();

		cnrtlParser = new CNRTLParser();

		String htmlElementTextForPartOfSpeech = getHTMLElementTextForPartOfSpeech(word);
		if (htmlElementTextForPartOfSpeech != null) {
			for (PartOfSpeech pos : PartOfSpeech.values()) {
				if (htmlElementTextForPartOfSpeech.contains(pos.getCNRTLValue())) {
					this.partOfSpeech = pos;
					canonicalForm = lemmatise(word);
					break;
				}
			}
		}
	}

	public PartOfSpeech getPartOfSpeech() {

		return partOfSpeech;
	}

	public String getCanonicalForm() {

		return canonicalForm;
	}

	/**
	 * Returns the text which should contain an indication of the part of speech of a word (as a parameter).
	 * 
	 * @param word
	 *  the word which we would to get the part of speech
	 * 
	 * @return the text which should contain an indication of the part of speech of the word in question
	 */
	private String getHTMLElementTextForPartOfSpeech(String word) {

		String url = String.format(DataIntegrator.SYNONYMY_FORMAT, word + Separator.SLASH.getValue());
		Element htmlElement = cnrtlParser.getFirstHTMLElement(url, "li[id=vitemselected]");

		return ((htmlElement != null) ? htmlElement.text() : null);
	}

	/**
	 * Returns the canonical form of a word...
	 * 
	 * @param word
	 *  the word which we would to get the canonical form
	 * 
	 * @return the canonical form of the word in question
	 */
	private String lemmatise(String word) {

		String url = String.format(DataIntegrator.SYNONYMY_FORMAT, word + Separator.SLASH.getValue() + partOfSpeech.getValue());
		Element htmlElement = cnrtlParser.getFirstHTMLElement(url, "div[class=messagecenter]");

		String htmlElementText = (htmlElement != null) ? htmlElement.text() : "\"" + word + "\"";

		return htmlElementText.substring(htmlElementText.indexOf('"') + 1, htmlElementText.lastIndexOf('"'));
	}

	/**
	 * A part-of-speech found by the FrenchTagger, such as "verbe".
	 * <p>{@code Extension} is an enum representing the French parts of the speech – "verbe", "substantif" and "adjectif".</p>
	 * 
	 * @author Andrei Zabolotnîi
	 * @author Cyril Marilier
	 */
	public enum PartOfSpeech {

		/**
		 * The singleton instance for a <b>verb</b>.
		 */
		VERB("verbe"),

		/**
		 * The singleton instance for a <b>noun</b>.
		 */
		NOUN("substantif"),

		/**
		 * The singleton instance for a <b>adjective</b>.
		 */
		ADJECTIVE("adjectif");

		private final String value;

		private PartOfSpeech(String value) {

			this.value = value;
		}

		/**
		 * Returns the CNRTL value of the part of speech.
		 * 
		 * @return the CNRTL value of the part of speech
		 */
		public String getCNRTLValue() {

			return Separator.COMMA.getValue() + Separator.SPACE.getValue() + value;
		}

		/**
		 * Returns the value of the part of speech.
		 * 
		 * @return the value of the part of speech
		 */
		public String getValue() {

			return value;
		}

	}

}
