package edu.fromatoz.littlesearch;

import org.apache.log4j.Logger;

import org.tartarus.snowball.ext.FrenchStemmer;

public class StandardFrenchStemmerTest {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(StandardFrenchStemmerTest.class);

	public static void main(String[] args) {

		FrenchStemmer frenchStemmer = new FrenchStemmer();

		stem(frenchStemmer, "systèmes");
		stem(frenchStemmer, "mathématiciens");
	}

	private static void stem(FrenchStemmer frenchStemmer, String word) {

		frenchStemmer.setCurrent(word);
		frenchStemmer.stem();

		LOGGER.info(word + "\t[stem: " + frenchStemmer.getCurrent() + "]");
	}

}
