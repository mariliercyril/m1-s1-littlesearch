package edu.fromatoz.littlesearch.searchengine;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.fromatoz.littlesearch.dataintegrator.model.entity.SynonymsSet;
import edu.fromatoz.littlesearch.dataintegrator.model.entity.Word;

import edu.fromatoz.littlesearch.dataintegrator.model.entity.word.Noun;

import edu.fromatoz.littlesearch.tool.Separator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.nio.charset.StandardCharsets;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public class Synonymysearch {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(Littlesearch.class);

	// The Data Warehouse
	private static final String DATA_WAREHOUSE = "data_warehouse";

	// Adds a private constructor to hide the implicit public one (indicated by SonarQube).
	private Synonymysearch() {

		throw new IllegalStateException("Synonymysearch class");
	}

	public static String search(String words) {

		String[] separatedWords = words.split((Separator.SPACE).getValue());
		Set<String> contextualForms = new TreeSet<>(Arrays.asList(separatedWords));

		for (String word : separatedWords) {
			// Gets the JSON files of the data warehouse
			File[] files = (new File(String.format(DATA_WAREHOUSE))).listFiles();
			for (File file : files) {
				ObjectMapper objectMapper = new ObjectMapper();
				try {
					SynonymsSet synonymsSet = objectMapper.readValue(new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1), SynonymsSet.class);
					String partOfSpeech = synonymsSet.getPartOfSpeech();

					Word[] synonyms = synonymsSet.getSynonyms();
					for (Word synonym : synonyms) {
						Set<String> forms = new TreeSet<>();
						forms = getForms(forms, synonym, partOfSpeech);

						if (forms.stream().anyMatch(f -> f.equals(word))) {
							contextualForms = getForms(contextualForms, synonyms[0], partOfSpeech);
						}
					}
				} catch (IOException ioe) {
					LOGGER.error(ioe);
				}
			}
		}

		return String.join(" ", contextualForms);
	}

	private static Set<String> getForms(Set<String> forms, Word synonym, String partOfSpeech) {

		try {
			forms.add(new String((synonym.getCanonicalForm()).getBytes("UTF-8")));
			if (partOfSpeech.equals("substantif") || partOfSpeech.equals("adjectif")) {
				Object[] otherForms = ((Noun)synonym).getOtherForms();
				if (otherForms.length > 0) {
					for (Object otherForm : otherForms) {
						forms.add(new String((otherForm.toString()).getBytes("UTF-8")));
					}
				}
			}
		} catch (UnsupportedEncodingException uee) {
			LOGGER.error(uee);
		}

		return forms;
	}

}
