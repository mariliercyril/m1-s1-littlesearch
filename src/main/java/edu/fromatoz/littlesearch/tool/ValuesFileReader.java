package edu.fromatoz.littlesearch.tool;

import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;
import java.util.Set;

/**
 * The {@code ValuesFileReader} class defines the Singleton instance for the reader of values file.
 * <p>The single instance of this class should properly behave in a multi-threaded environment.</p>
 */
public final class ValuesFileReader {

	private static final String VALUES_FILE_SUFFIX = (Separator.POINT).getValue() + (Extension.VALUES).getValue();

	private static final String VALUES_FILE_PATH_FORMAT = (Separator.SLASH).getValue() + "%s" + VALUES_FILE_SUFFIX;

	// The ValuesFileReader Singleton should never be instantiated from outside the class.
	private ValuesFileReader() {
	}

	/**
	 * The {@code ValuesFileReaderHolder} is the holder of the responsibility
	 * for the only instantiation of the {@code ValuesFileReader} Singleton.
	 */
    private static class ValuesFileReaderHolder {

        private static final ValuesFileReader INSTANCE = new ValuesFileReader();
    }

    /**
     * Returns the single instance of the {@code ValuesFileReader}.
     * 
     * @return INSTANCE
     *  the single instance of the ValuesFileReader
     */
    public static ValuesFileReader getInstance() {

        return ValuesFileReaderHolder.INSTANCE;
    }

	/**
	 * Returns the {@code String} value corresponding to a key in the values file.
	 * 
	 * @param valuesFileName
	 *  the name of the values file
	 * @param key
	 * 	the key in the values file
	 * 
	 * @return value
	 *  the value, as a <i>String</i>, corresponding to a key in the values file
	 */
	public String getStringValue(String valuesFileName, String key) {

		String value = "";

		try {
			Values values = this.getValues(String.format(VALUES_FILE_PATH_FORMAT, valuesFileName));
			value = values.getValue(key);
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}

		return value;
	}

	/**
	 * Returns the keys of the file of which the name is as a parameter.
	 * 
	 * @param valuesFileName
	 *  the name of the values file
	 * 
	 * @return keys
	 *  the keys as a set
	 */
	public Set<String> getKeys(String valuesFileName) {

		Values values = this.getValues(String.format(VALUES_FILE_PATH_FORMAT, valuesFileName));

		return values.stringValueNames();
	}

	/**
	 * Returns the values of the file of which the name is as a parameter.
	 * 
	 * @param valuesFilePath
	 *  the path of the values file
	 * 
	 * @return values
	 *  the values as a list
	 */
	private Values getValues(String valuesFilePath) {

		Values values = new Values();

		InputStream inputStream = (this.getClass()).getResourceAsStream(valuesFilePath);
		try {
			values.load(inputStream);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		return values;
	}

	private class Values extends Properties {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private String getValue(String key) {

			return this.getProperty(key);
		}

		private Set<String> stringValueNames() {

			return this.stringPropertyNames();
		}

	}

}
