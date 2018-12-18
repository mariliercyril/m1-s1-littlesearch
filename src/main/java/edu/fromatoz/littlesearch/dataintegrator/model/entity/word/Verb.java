package edu.fromatoz.littlesearch.dataintegrator.model.entity.word;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.fromatoz.littlesearch.dataintegrator.model.entity.Word;

/**
 * The {@code Verb} class defines a verb as a Java object which should be serialised to JSON.
 * <p>({@linkplain com.fasterxml.jackson.annotation Jackson annotations} are used.)</p>
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public final class Verb extends Word {

	public Verb(@JsonProperty("canonical_form")String canonicalForm) {

		super(canonicalForm);
	}

}
