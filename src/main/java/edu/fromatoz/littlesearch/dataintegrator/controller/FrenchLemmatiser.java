package edu.fromatoz.littlesearch.dataintegrator.controller;

import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

import org.jsoup.nodes.Element;

import org.jsoup.select.Elements;

import edu.fromatoz.littlesearch.app.DataIntegrator;
import edu.fromatoz.littlesearch.app.DataIntegrator.CNRTLParser;

import edu.fromatoz.littlesearch.dataintegrator.model.entity.Word;

import edu.fromatoz.littlesearch.dataintegrator.model.entity.word.Noun;
import edu.fromatoz.littlesearch.dataintegrator.model.entity.word.Verb;

import edu.fromatoz.littlesearch.tool.Separator;

/**
 * The {@code FrenchLemmatiser} class defines a lemmatiser for French words:
 * it joins the lemmas of a French word from the canonical form.
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public class FrenchLemmatiser {

	private FrenchTagger.PartOfSpeech partOfSpeech;
	private String canonicalForm;

	private CNRTLParser cnrtlParser;
	private Word word;

	private Set<String> otherForms;

	public FrenchLemmatiser(FrenchTagger tagger) {

		partOfSpeech = tagger.getPartOfSpeech();
		canonicalForm = tagger.getCanonicalForm();

		constructWord();
	}

	public String getCanonicalForm() {

		return canonicalForm;
	}

	public Word getWord() {

		return word;
	}

	/**
	 * Constructs the {@link Verb} corresponding to the word which is currently treated.
	 */
	private void constructWord() {

		switch (partOfSpeech) {
			case VERB:
				word = new Verb(canonicalForm);
				break;
			case NOUN:
			case ADJECTIVE:
				getOtherForms();
				word = new Noun(canonicalForm, otherForms.toArray());
				break;
			default:
				//...
		}
	}

	/**
	 * Returns the set of other forms.
	 * (It could be empty.)
	 */
	private void getOtherForms() {

		cnrtlParser = new CNRTLParser();

		otherForms = new TreeSet<>();

		// Gets at least the plural of the canonical form ordinarily...
		getPluralFormFrom(canonicalForm);
		// Closes the set of the singular forms
		String singularForm = closeSingularFormsSet();
		// Closes the set of the plural forms
		getPluralFormFrom(singularForm);
		// Removes the canonical form from the set of other forms, if the set in question contains it
		if (otherForms.contains(canonicalForm)) {
			otherForms.remove(canonicalForm);
		}
	}

	/**
	 * Returns the singular form which closes the set of the singular forms.
	 * 
	 * @return the canonical form or the other singular form
	 */
	private String closeSingularFormsSet() {

		String singularForm = canonicalForm;

		String url = String.format(DataIntegrator.DEFINITION_FORMAT, canonicalForm + Separator.SLASH.getValue() + partOfSpeech.getValue());
		Element htmlElement = cnrtlParser.getFirstHTMLElement(url, "li[id=vitemselected]");

		if (htmlElement != null) {
			String htmlElementText = (htmlElement.select("span")).text();
			// Parses the HTML element text in the <span> tag...
			if (htmlElementText.contains(Separator.COMMA.getValue())) {
				// Rebuilds the singular form from the canonical form...
				String termination = htmlElementText.substring(htmlElementText.indexOf(Separator.COMMA.getValue()) + 1);
				if (termination.contains(Separator.COMMA.getValue())) {
					termination = termination.substring(0, termination.indexOf(Separator.COMMA.getValue()));
				}
				if (termination.contains("(") && termination.contains(")")) {
					termination = termination.replaceAll("\\(|\\)", "");
				}
				termination = termination.trim();
				if (termination.startsWith(Separator.HYPHEN.getValue())) {
					termination = termination.substring(1);
				}
				termination = termination.toLowerCase();
				String common = termination;
				while (!(canonicalForm.contains(common))) {
					common = common.substring(0, common.length() - 1);
				}
				singularForm = (canonicalForm.substring(0, canonicalForm.lastIndexOf(common)) + termination);
			}
		}

		return singularForm;
	}

	/**
	 * Returns at least the plural form from a singular form (as a parameter).
	 * 
	 * @param singularForm
	 *  a singular form (the canonical form, for example)
	 */
	private void getPluralFormFrom(String singularForm) {

		String url = String.format(DataIntegrator.MORPHOLOGY_FORMAT, singularForm + (Separator.SLASH).getValue() + partOfSpeech.getValue());
		Elements pluralForms = cnrtlParser.getHTMLElements(url, "span[class*=morf_sound]");

		if (pluralForms != null) {
			ListIterator<Element> pluralFormsListIterator = pluralForms.listIterator();
			while (pluralFormsListIterator.hasNext()) {
				Element pluralForm = pluralFormsListIterator.next();
				otherForms.add(pluralForm.text());
			}
		}
	}

}
