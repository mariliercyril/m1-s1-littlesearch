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
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.tartarus.snowball.ext.FrenchStemmer;

import java.io.*;
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
    // The JSON file name extended format...
    private static final String JSON_FILE_NAME_EXTENDED_FORMAT = "%s" + (Separator.POINT).getValue() + (Extension.JSON).getValue();
    // The JSON file path format...
    private static final String JSON_FILE_PATH_FORMAT = DATA_WAREHOUSE + (Separator.SLASH).getValue() + JSON_FILE_NAME_EXTENDED_FORMAT;

    public String searchSynonyms(String words) throws IOException {
        List<String> separatedWordsAndSteamed = new ArrayList<>();
        FrenchStemmer frenchStemmer = new FrenchStemmer();

        StringBuilder allWordsToSearch = new StringBuilder();
        for (String w: words.split(" ")) {
            String lowerCase = w.toLowerCase();
            if(!separatedWordsAndSteamed.contains(lowerCase))
                separatedWordsAndSteamed.add(lowerCase);

            frenchStemmer.setCurrent(lowerCase);
            frenchStemmer.stem();
            if(!separatedWordsAndSteamed.contains(frenchStemmer.getCurrent()))
                separatedWordsAndSteamed.add(frenchStemmer.getCurrent());
        }

        for (String word : separatedWordsAndSteamed) {
            //get the json file form data_warehouse
            File jsonFile = new File(String.format(JSON_FILE_PATH_FORMAT, word));
            allWordsToSearch.append(word).append(Separator.SPACE.getValue());
            if ((jsonFile.exists())) {
                ObjectMapper objectMapper = new ObjectMapper();
                ObjectReader objectReader = objectMapper.reader();
                JsonNode synonymsNode = objectReader.readTree(new InputStreamReader(new FileInputStream(jsonFile),StandardCharsets.ISO_8859_1));
                //get array of synonyms from jsonNode
                String data = synonymsNode.get("synonyms").toString();
                //get partOfSpeech from jsonNode
                String partOfSpeech = synonymsNode.get("part_of_speech").asText();
                if (partOfSpeech != null) {
                        switch (partOfSpeech){
                            case "verbe":
                                List<Verb> verbList = objectMapper.readValue(data,
                                        objectMapper.getTypeFactory().constructCollectionType(List.class, Verb.class));
                                if(!verbList.isEmpty()){
                                    for (Verb v:verbList) {
                                        if(!allWordsToSearch.toString().contains(v.getCanonicalForm()))
                                            allWordsToSearch.append(v.getCanonicalForm()).append(Separator.SPACE.getValue());
                                    }
                                }
                                break;
                            case "substantif":
                            case "adjectif":
                                List<Noun> nounList = objectMapper.readValue(data,
                                        objectMapper.getTypeFactory().constructCollectionType(List.class, Noun.class));
                                if(!nounList.isEmpty()){
                                    for (Noun n : nounList) {
                                        if(!allWordsToSearch.toString().contains(n.getCanonicalForm()))
                                        allWordsToSearch.append(n.getCanonicalForm()).append(Separator.SPACE.getValue());
                                        if(n.getOtherForms().length > 0){
                                            for (Object otherForm:n.getOtherForms()) {
                                                if(!allWordsToSearch.toString().contains(otherForm.toString()))
                                                    allWordsToSearch.append(otherForm.toString()).append(Separator.SPACE.getValue());
                                            }
                                        }
                                    }
                                }
                                break;
                        }
                }
            }
        }
        //System.out.println(allWordsToSearch.toString());
        return allWordsToSearch.toString();
    }
}