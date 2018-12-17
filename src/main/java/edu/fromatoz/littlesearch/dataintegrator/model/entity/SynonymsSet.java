package edu.fromatoz.littlesearch.dataintegrator.model.entity;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.jsoup.nodes.Element;

import org.jsoup.select.Elements;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import edu.fromatoz.littlesearch.app.DataIntegrator;
import edu.fromatoz.littlesearch.app.DataIntegrator.CNRTLParser;

import edu.fromatoz.littlesearch.dataintegrator.controller.FrenchLemmatiser;
import edu.fromatoz.littlesearch.dataintegrator.controller.FrenchTagger;

import edu.fromatoz.littlesearch.tool.Separator;

/**
 * The {@code Synonyms} class defines a synonyms set as a Java object which should be serialized to JSON.
 * ({@linkplain com.fasterxml.jackson.annotation Jackson annotations} are used.)
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
@JsonPropertyOrder({ "part_of_speech", "synonyms" })
public class SynonymsSet {

	// The log format...
	private static final String LOG_FORMAT = " [canonical_form: %S; part_of_speech: %S]";

	// The date format for logs...
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private String partOfSpeech;
	private Word[] synonyms;

	/**
	 * Constructs a synonyms set from a word.
	 * 
	 * @param tagger
	 *  the part of speech of the synonyms (should be common to all the synonyms of the set)...
	 */
	public SynonymsSet(FrenchTagger tagger) {

		partOfSpeech = (tagger.getPartOfSpeech()).getValue();

		List<Word> jsonSynonyms = new ArrayList<>();

		// Lemmatises the reference word
		FrenchLemmatiser lemmatiser = new FrenchLemmatiser(tagger);
		// The reference word is a member of the set...
		jsonSynonyms.add(lemmatiser.getWord());
		// For monitoring the integration of the reference word...
		monitor(tagger);

		String url = String.format(DataIntegrator.SYNONYMY_FORMAT, tagger.getCanonicalForm() + (Separator.SLASH).getValue() + tagger.getPartOfSpeech());
		Elements synonymElements = (new CNRTLParser()).getHTMLElements(url, "td[class*=syno_format]");
		ListIterator<Element> synonymElementsListIterator = synonymElements.listIterator();
		while (synonymElementsListIterator.hasNext()) {
			Element synonymElement = synonymElementsListIterator.next();
			// Tags the synonym in question
			FrenchTagger synonymTagger = new FrenchTagger(synonymElement.text());
			if ((synonymTagger.getPartOfSpeech() != null) && (synonymTagger.getPartOfSpeech()).equals(tagger.getPartOfSpeech())) {
				// Lemmatises the synonym in question
				FrenchLemmatiser synonymLemmatiser = new FrenchLemmatiser(synonymTagger);
				// Adds the synonym in question to the set...
				jsonSynonyms.add(synonymLemmatiser.getWord());
				// For monitoring the integration of the synonyms...
				monitor(synonymTagger);
			}
		}

		synonyms =  jsonSynonyms.stream().toArray(Word[]::new);
	}

	@JsonGetter("part_of_speech")
	public String getPartOfSpeech() {

		return partOfSpeech;
	}

	@JsonGetter("synonyms")
	public Word[] getSynonyms() {

		return synonyms;
	}

	/**
	 * Prints information of the word which is currently processing.
	 * 
	 * @param tagger
	 *  the canonical form of the word, the part of speech in which it is
	 */
	private void monitor(FrenchTagger tagger) {

		// TODO: To replace by log...
		System.out.print(simpleDateFormat.format(new Date()));
		System.out.println(String.format(LOG_FORMAT, tagger.getCanonicalForm(), tagger.getPartOfSpeech()));
	}

}
