package edu.fromatoz.littlesearch.searchengine;

import com.fasterxml.jackson.core.JsonParseException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.fromatoz.littlesearch.dataintegrator.model.entity.SynonymsSet;
import edu.fromatoz.littlesearch.dataintegrator.model.entity.Word;

import edu.fromatoz.littlesearch.dataintegrator.model.entity.word.Noun;

import edu.fromatoz.littlesearch.tool.Separator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public class SynonymsSearcher {

	// The Data Warehouse
	private static final String DATA_WAREHOUSE = "data_warehouse";

	public String searchSynonyms(String words) {

		String[] separatedWords = words.split((Separator.SPACE).getValue());
		List<String> wordsList = new ArrayList<>(Arrays.asList(separatedWords));

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
						if ((new String((synonym.getCanonicalForm()).getBytes("UTF-8"))).equals(word)) {
							wordsList.remove(word);
							wordsList.add((new String((synonyms[0].getCanonicalForm()).getBytes("UTF-8"))));
							switch (partOfSpeech) {
								case "substantif":
								case "adjectif":
									Object[] otherContextualForms = ((Noun)synonyms[0]).getOtherForms();
									if (otherContextualForms.length > 0) {
										for (Object otherContextualForm : otherContextualForms) {
											wordsList.add((new String((otherContextualForm.toString()).getBytes("UTF-8"))));
										}
									}
									break;
							}
						} else {
							switch (partOfSpeech) {
								case "substantif":
								case "adjectif":
									Object[] otherForms = ((Noun)synonym).getOtherForms();
									if (otherForms.length > 0) {
										for (Object otherForm : otherForms) {
											if ((new String((otherForm.toString()).getBytes("UTF-8"))).equals(word)) {
												wordsList.remove(word);
												wordsList.add((new String((synonyms[0].getCanonicalForm()).getBytes("UTF-8"))));
												Object[] otherInitialForms = ((Noun)synonyms[0]).getOtherForms();
												if (otherInitialForms.length > 0) {
													for (Object otherInitialForm : otherInitialForms) {
														wordsList.add((new String((otherInitialForm.toString()).getBytes("UTF-8"))));
													}
												}
											}
										}
									}
									break;
							}
						}
					}
				} catch (JsonParseException jpe) {
					jpe.printStackTrace();
				} catch (JsonMappingException jme) {
					jme.printStackTrace();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}

		System.out.println(wordsList + "\n");

		return String.join(" ", wordsList);
	}

}
