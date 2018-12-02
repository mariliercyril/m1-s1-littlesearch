package edu.fromatoz.littlesearch.tool;

/**
 * An extension, such as "json".
 * <p>{@code Extension} is an enum representing the extensions – "json" and "txt".</p>
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public enum Extension {

	/**
	 * The singleton instance for the extension of <b>JSON</b>.
	 */
	JSON("json"),

	/**
	 * The singleton instance for the extension of <b>text</b>.
	 */
	TEXT("txt");

	private final String value;

	private Extension(String value) {

		this.value = value;
	}

	/**
	 * Returns the value of the extension.
	 * 
	 * @return the value of the extension
	 */
	public String getValue() {

		return value;
	}

}
