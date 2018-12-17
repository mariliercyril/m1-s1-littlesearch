package edu.fromatoz.littlesearch.searchengine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import edu.fromatoz.littlesearch.dataintegrator.controller.FrenchTagger;
import edu.fromatoz.littlesearch.dataintegrator.model.entity.Word;
import edu.fromatoz.littlesearch.dataintegrator.model.entity.word.Noun;
import edu.fromatoz.littlesearch.dataintegrator.model.entity.word.Verb;
import edu.fromatoz.littlesearch.tool.Extension;
import edu.fromatoz.littlesearch.tool.Separator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
/**
 * @author Andrei Zabolotnîi
 * @author Cyril Marilier
 */
public class SynonymsSearcher {
    // The Data Warehouse
    private static final String DATA_WAREHOUSE = "data_warehouse";
    // The JSON file name extended format...
    private static final String JSON_FILE_NAME_EXTENDED_FORMAT = "%s" + (Separator.POINT).getValue() + (Extension.JSON).getValue();
    // The JSON file path format...
    private static final String JSON_FILE_PATH_FORMAT = DATA_WAREHOUSE + (Separator.SLASH).getValue() + JSON_FILE_NAME_EXTENDED_FORMAT;

    public String searchSynonyms(String words) {
        String[] separatedWords = words.split(" ");
        StringBuilder allWordsToSearch = new StringBuilder();

        for (String word : separatedWords) {
            FrenchTagger frenchTagger = new FrenchTagger(word);

            if (frenchTagger.getPartOfSpeech() != null) {
                //get the json file form data_warehouse
                File jsonFile = new File(String.format(JSON_FILE_PATH_FORMAT, frenchTagger.getCanonicalForm()));
                if ((jsonFile.exists())) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    ObjectReader objectReader = objectMapper.reader();
                    try {
                        JsonNode synonymsNode = objectReader.readTree(new InputStreamReader(new FileInputStream(jsonFile),StandardCharsets.ISO_8859_1));
                        //get array of synonyms from jsonNode
                        String data = synonymsNode.get("synonyms").toString();
                        switch (frenchTagger.getPartOfSpeech()){
                            case VERB:
                                List<Verb> verbList = objectMapper.readValue(data,
                                        objectMapper.getTypeFactory().constructCollectionType(List.class, Verb.class));
                                if(!verbList.isEmpty()){
                                    for (Verb v:verbList) {
                                        allWordsToSearch.append(v.getCanonicalForm()).append(Separator.SPACE.getValue());
                                    }
                                }
                                break;
                            case NOUN:
                            case ADJECTIVE:
                                List<Noun> nounList = objectMapper.readValue(data,
                                        objectMapper.getTypeFactory().constructCollectionType(List.class, Noun.class));
                                if(!nounList.isEmpty()){
                                    for (Noun n : nounList) {
                                        allWordsToSearch.append(n.getCanonicalForm()).append(Separator.SPACE.getValue());
                                        if(n.getOtherForms().length > 0){
                                            for (Object otherForm:n.getOtherForms()) {
                                                allWordsToSearch.append(otherForm.toString()).append(Separator.SPACE.getValue());
                                            }
                                        }
                                    }
                                }
                                break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else allWordsToSearch.append(word).append(Separator.SPACE.getValue());
            }
            else allWordsToSearch.append(word).append(Separator.SPACE.getValue());
        }
        return allWordsToSearch.toString();
    }
}