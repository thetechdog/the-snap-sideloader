package org.sideloader;
import org.apache.commons.text.similarity.CosineSimilarity;

import java.util.*;

public class SimilarAppsFinder {
    String packageDescription;
    String packageSummary;
    ArrayList[] randPackageSuggestions;
    ArrayList<String> randPackageDescriptions = new ArrayList<>();
    ArrayList<String> randPackageSummaries = new ArrayList<>();
    public SimilarAppsFinder(String packageSummary, String packageDescription, ArrayList[] randPackageSuggestions, DBOperator dbop) {
        this.packageDescription = packageDescription;
        this.packageSummary = packageSummary;
        this.randPackageSuggestions = randPackageSuggestions;
        //get descriptions for each package based on package name
        for(int i=0;i<randPackageSuggestions[1].size();i++) {
            randPackageDescriptions.add(dbop.simpleQueryExecutor("SELECT Description FROM PACKAGES WHERE PackageName=\""+ randPackageSuggestions[1].get(i) +"\""));//null if no description
            randPackageSummaries.add(dbop.simpleQueryExecutor("SELECT Summary FROM PACKAGES WHERE PackageName=\""+ randPackageSuggestions[1].get(i) +"\""));
        }
        //System.out.println("Descriptions: "+randPackageDescriptions);
        //System.out.println("Summaries: "+randPackageSummaries);

    }

    public ArrayList<String> startSimilarityFinder() {
        CosineSimilarity cosineSimilarity = new CosineSimilarity();
        Map<CharSequence, Integer> currentPackageTermFreqS = getTermFreqMap(packageSummary);//get tf for currently displayed package summary
        Map<CharSequence, Integer> currentPackageTermFreqD = getTermFreqMap(packageDescription);//get tf for currently displayed package description
        //similarity list with package name:similarity score
        List<Map.Entry<String, Double>> similarityScores = new ArrayList<>();
        for(int i=0;i<randPackageDescriptions.size();i++){
            double similarity=0;//total similarity
            double summarySimilarity=0;//summary similarity
            double descriptionSimilarity=0;//description similarity
            //get tf and similarity for this iteration's pkg summary
            Map<CharSequence, Integer> otherPackageTermFreqS = getTermFreqMap(randPackageSummaries.get(i));
            if(!currentPackageTermFreqS.isEmpty() && !otherPackageTermFreqS.isEmpty()) summarySimilarity=cosineSimilarity.cosineSimilarity(currentPackageTermFreqS, otherPackageTermFreqS);
            //get tf and similarity for this iteration's pkg description
            Map<CharSequence, Integer> otherPackageTermFreqD = getTermFreqMap(randPackageDescriptions.get(i));
            if(!currentPackageTermFreqD.isEmpty() && !otherPackageTermFreqD.isEmpty()) descriptionSimilarity=cosineSimilarity.cosineSimilarity(currentPackageTermFreqD, otherPackageTermFreqD);
            //get and add total similarity score to list
            similarity=summarySimilarity*0.2+descriptionSimilarity*0.8;
            similarityScores.add(new AbstractMap.SimpleEntry<>((String)randPackageSuggestions[1].get(i), similarity)); //package:desc similarity, using AbstractMap as it's an actual class, not an interface
        }
        ArrayList<String> actualSuggestedPackages=new ArrayList<>();
        similarityScores.sort(Map.Entry.comparingByValue(Collections.reverseOrder())); //sort by similarity score, highest to lowest
        List<Map.Entry<String, Double>> top3 = similarityScores.stream().limit(3).toList();
        for(Map.Entry<String, Double> entry: top3){
            actualSuggestedPackages.add(entry.getKey());//get the top 3 packages
        }
        return actualSuggestedPackages;
    }

    private HashMap<CharSequence, Integer> getTermFreqMap(String description) {
        HashMap<CharSequence, Integer> termFreqMap = new HashMap<>();
        if(description==null) return termFreqMap;
        String[] words = description.split("\\W+");//split by non-word characters
        for (String word : words) {
            word = word.toLowerCase();
            if(word.isBlank()) continue; //skip blank strings
            if (termFreqMap.containsKey(word)) {//if word is already in map, increment by 1 apparition
                termFreqMap.put(word, termFreqMap.get(word) + 1);
            } else {//create entry for word and set to 1
                termFreqMap.put(word, 1);
            }
        }
        return termFreqMap;
    }
}
