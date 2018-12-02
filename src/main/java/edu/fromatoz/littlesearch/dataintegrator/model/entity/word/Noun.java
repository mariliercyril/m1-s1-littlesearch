package edu.fromatoz.littlesearch.dataintegrator.model.entity.word;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import edu.fromatoz.littlesearch.dataintegrator.model.entity.Word;

/**
 * The {@code Noun} class defines a noun (<i>substantive</i> or <i>adjective</i>)
 * as a Java object which should be serialised to JSON.
 * <p>({@linkplain com.fasterxml.jackson.annotation Jackson annotations} are used.)</p>
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
@JsonPropertyOrder({ "canonical_form", "other_forms" })
public final class Noun extends Word {

	private Object[] otherForms;

	public Noun(String canonicalForm, Object[] otherForms) {

		super(canonicalForm);
		this.otherForms = otherForms;
	}

	@JsonGetter("other_forms")
	public Object[] getOtherForms() {

		return otherForms;
	}

}
