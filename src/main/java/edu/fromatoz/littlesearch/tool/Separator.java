package edu.fromatoz.littlesearch.tool;

/**
 * {@code Separator} is an enum representing signs of separation.
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public enum Separator {

	/**
	 * The singleton instance for the <b>colon</b>.
	 */
	COLON(":"),

	/**
	 * The singleton instance for the <b>comma</b>.
	 */
	COMMA(","),

	/**
	 * The singleton instance for the <b>hyphen</b>.
	 */
	HYPHEN("-"),

	/**
	 * The singleton instance for the <b>point</b>.
	 */
	POINT("."),

	/**
	 * The singleton instance for the <b>slash</b>.
	 */
	SLASH("/"),

	/**
	 * The singleton instance for the <b>space</b>.
	 */
	SPACE(" ");

	private final String value;

	private Separator(String value) {

		this.value = value;
	}

	/**
	 * Returns the value of the separator.
	 * 
	 * @return the value of the separator
	 */
	public String getValue() {

		return value;
	}

}
