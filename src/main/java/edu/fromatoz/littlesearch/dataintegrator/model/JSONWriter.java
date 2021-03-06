package edu.fromatoz.littlesearch.dataintegrator.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import edu.fromatoz.littlesearch.dataintegrator.controller.FrenchTagger;

import edu.fromatoz.littlesearch.dataintegrator.model.entity.SynonymsSet;

import edu.fromatoz.littlesearch.tool.Extension;
import edu.fromatoz.littlesearch.tool.Separator;

import org.apache.log4j.Logger;

/**
 * The {@code JSONWriter} class defines a writer for any JSON file corresponding to a word.
 * 
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public class JSONWriter {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(JSONWriter.class);

	// The Data Warehouse
	private static final String DATA_WAREHOUSE = "data_warehouse";
	// The JSON file name extended format...
	private static final String JSON_FILE_NAME_EXTENDED_FORMAT = "%s" + (Separator.POINT).getValue() + (Extension.JSON).getValue();
	// The JSON file path format...
	private static final String JSON_FILE_PATH_FORMAT = DATA_WAREHOUSE + (Separator.SLASH).getValue() + JSON_FILE_NAME_EXTENDED_FORMAT;

	public void load(String word) {

		FrenchTagger tagger = new FrenchTagger(word);

		if (tagger.getPartOfSpeech() != null) {
			File jsonFile = new File(String.format(JSON_FILE_PATH_FORMAT, tagger.getCanonicalForm()));
			if (!(jsonFile.exists())) {
				SynonymsSet synonymsSet = new SynonymsSet(tagger);
				if ((synonymsSet.getSynonyms()).length > 0) {
					/**
					 * An {@link ObjectMapper} word by data binding.
					 */
					ObjectWriter objectWripper = (new ObjectMapper()).writerWithDefaultPrettyPrinter();
					try {
						objectWripper.writeValue(new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.ISO_8859_1), synonymsSet);

						// For monitoring...
						LOGGER.info(objectWripper.writeValueAsString(synonymsSet));
					} catch (IOException ioe) {
						LOGGER.error(ioe);
					}
				}
			}
		}
	}

}
