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

		cnrtlParser = new DataIntegrator.CNRTLParser();

		String htmlElementTextForPartOfSpeech = getHTMLElementTextForPartOfSpeech(word);
		if (htmlElementTextForPartOfSpeech != null) {
			for (PartOfSpeech partOfSpeech : PartOfSpeech.values()) {
				if (htmlElementTextForPartOfSpeech.contains(partOfSpeech.getCNRTLValue())) {
					this.partOfSpeech = partOfSpeech;
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
	 * @return the text which should contain an indication of the part of speech of the word in question
	 */
	private String getHTMLElementTextForPartOfSpeech(String word) {

		String url = String.format(DataIntegrator.DEFINITION_FORMAT, word + Separator.SLASH.getValue());
		Element htmlElement = cnrtlParser.getFirstHTMLElement(url, "li[id=vitemselected]");

		return ((htmlElement != null) ? htmlElement.text() : null);
	}

	/**
	 * Returns the canonical form of a word...
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
		VERB(Separator.COMMA.getValue() + Separator.SPACE.getValue() + "verbe", "verbe"),

		/**
		 * The singleton instance for a <b>noun</b>.
		 */
		NOUN(Separator.SPACE.getValue() + "subst" + Separator.POINT.getValue(), "substantif"),

		/**
		 * The singleton instance for a <b>adjective</b>.
		 */
		ADJECTIVE(Separator.SPACE.getValue() + "adj" + Separator.POINT.getValue(), "adjectif");

		private final String cnrtlValue;
		private final String value;

		private PartOfSpeech(String cnrtlValue, String value) {

			this.cnrtlValue = cnrtlValue;
			this.value = value;
		}

		/**
		 * Returns the CNRTL value of the part of speech.
		 * 
		 * @return the CNRTL value of the part of speech
		 */
		public String getCNRTLValue() {

			return cnrtlValue;
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
