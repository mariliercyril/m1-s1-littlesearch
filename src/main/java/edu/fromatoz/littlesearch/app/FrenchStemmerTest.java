package edu.fromatoz.littlesearch.app;

import org.tartarus.snowball.ext.FrenchStemmer;

public class FrenchStemmerTest {

	public static void main(String[] args) {

		FrenchStemmer frenchStemmer = new FrenchStemmer();

		stem(frenchStemmer, "systèmes");
		stem(frenchStemmer, "mathématiciens");
	}

	private static void stem(FrenchStemmer frenchStemmer, String word) {

		frenchStemmer.setCurrent(word);
		frenchStemmer.stem();

		System.out.println(word + "\t[stem: " + frenchStemmer.getCurrent() + "]");
	}

}
