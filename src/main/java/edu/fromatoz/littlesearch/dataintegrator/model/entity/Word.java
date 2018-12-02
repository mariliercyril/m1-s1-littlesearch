package edu.fromatoz.littlesearch.dataintegrator.model.entity;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * This abstract class provides the minimal implementation for all the classes which define a word
 * (<i>verb</i>, <i>noun</i>, etc.) as a Java object which should be serialised to JSON:
 * an instance of {@code Word} should be at least defined with the canonical form of the word.
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public abstract class Word {

	private String canonicalForm;

	public Word(String canonicalForm) {

		this.canonicalForm = canonicalForm;
	}

	@JsonGetter("canonical_form")
	public String getCanonicalForm() {

		return canonicalForm;
	}

}
