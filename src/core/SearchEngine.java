package core;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;


import IRUtilities.DBFinder;
import IRUtilities.DocVector;
import IRUtilities.Entry;
import IRUtilities.HTable;
import IRUtilities.PageMetadata;
import IRUtilities.PageSummary;
import IRUtilities.Preprocessor;
import IRUtilities.Query;
import IRUtilities.TableNames;
import jdbm.helper.FastIterator;

public class SearchEngine {
    private static Index contentIndex;
    private static Index titleIndex;
    private static Preprocessor preprocessor;

    public static void setup() {
        try {
            DBFinder.init("test");
            contentIndex = new Index(TableNames.CONTENT_INDEX);
            titleIndex = new Index(TableNames.TITLE_INDEX);
        } catch (IOException e) {
            System.err.println("Failed to open database.");
            System.exit(1);
        }   
        preprocessor = new Preprocessor();
        try {
            preprocessor.loadStopwords("stopwords.txt");
        } catch (IOException e) {
            System.err.println("Failed to load stopwords.txt.");
            System.exit(1);
        }
    }

    public static LinkedList<Entry> search(String query, int numResults) {
        LinkedList<Entry> result = new LinkedList<Entry>();
        Query q;
        try {
            q = preprocessor.preprocessQuery(query);
        } catch (IOException e) {
            System.err.println("Failed to preprocess query.");
            return result;
        }

        System.out.println("Query.words: " + q.words.toString());
        System.out.println("Query.phrases: " + q.phrases.toString());

        //vectorize Query
        DocVector qVec = q.vectorize();

        //get cosine score (Long: pageID, Double: score)
        System.out.println("Getting cosine score from content");
        DocVector contentscores = new DocVector(getCosineScore(contentIndex, q, qVec, 1));
        // for(Entry e : contentscores.linearize()){
        //     System.out.println("PageID: " + e.dimension + " Score: " + e.component);
        // }
        // System.out.println("");
        
        System.out.println("Getting cosine score from title");
        DocVector titlescores = new DocVector(getCosineScore(titleIndex, q, qVec, 5));
        // for(Entry e : titlescores.linearize()){
        //     System.out.println("PageID: " + e.dimension + " Score: " + e.component);
        // }
        // System.out.println("");

        //PageRank Score
        DocVector pagerankScore = null;
        try {
            pagerankScore = new DocVector(DBFinder.pagerankScore);
        } catch (IOException e1) {
            System.err.println("Unable to read pagerank score file. Use only consine similarity.");
            e1.printStackTrace();
        }

        //combine scores
        DocVector scores = DocVector.add(contentscores,titlescores);
        if(pagerankScore!=null) scores = DocVector.add(pagerankScore, scores);
        // System.out.println("contentscores size:" + contentscores.size());
        // System.out.println("titlescores size:" + titlescores.size());
        // System.out.println("Scores size:" + scores.size());
        //sort scores
        LinkedList<Entry> sortedScores = scores.linearize();
        sortedScores.sort(new Comparator<Entry>() {
            @Override
            public int compare(Entry o1, Entry o2) {
                return Double.compare(o2.component,o1.component);
            }
        });

        //get top numResults results
        while(sortedScores.size()>numResults){
            sortedScores.removeLast();
        }
        return sortedScores;
    }

    public static HashMap<Long,Double> getCosineScore(Index index, Query q, DocVector qVec, double weight){
        HashMap<Long,Double> scores = new HashMap<Long,Double>();
        LinkedList<Long> pages;
        //find all documents contains any of the words in query
        for(Long wordID : q.words){
            try {
                pages = Index.toPageIDs(index.pageContains(wordID));
                System.out.println(pages.size() + " pages contains word " + DBFinder.wordIDHandler.getString(wordID));
            } catch (IOException e) {
                System.err.println("Failed to get pages contains wordID: " + wordID);
                continue;
            }
            for(Long pageID : pages){
                // if(scores.containsKey(pageID)) continue;
                DocVector docV;
                try {
                    // System.out.println(pageID + " : " + DBFinder.pageIDHandler.getString(pageID));
                    docV = index.getDocVector(pageID, q.phrases);
                } catch (IOException e) {
                    System.err.println("Failed to get DocVector with pageID: " + pageID);
                    continue;
                }
                double similarity = docV.cosineSimilarity(qVec)*weight;
                scores.put(pageID, similarity);
            }
        }
        //find all documents contains any of the phrase in query
        for(LinkedList<Long> phrase : q.phrases){
            try {
                pages = Index.toPageIDs(index.pageContains(phrase));
                System.out.println(pages.size() + " pages contains phrase " + phrase.toString());
            } catch (IOException e) {
                System.err.println("Failed to get pages contains phrase: " + phrase.toString());
                continue;
            }
            for(Long pageID : pages){
                DocVector docV;
                try {
                    // System.out.println(pageID + " : " + DBFinder.pageIDHandler.getString(pageID));
                    docV = index.getDocVector(pageID, q.phrases);
                } catch (IOException e) {
                    System.err.println("Failed to get DocVector with pageID: " + pageID);
                    continue;
                }
                double similarity = docV.cosineSimilarity(qVec)*weight;
                scores.put(pageID, similarity);
            }
        }
        return scores;
    }

    private static LinkedList<String> pageIDToURLs(LinkedList<Long> pageIDs){
        LinkedList<String> urls = new LinkedList<String>();
        for(Long pageID : pageIDs){
            try {
                urls.add(DBFinder.pageIDHandler.getString(pageID));
            } catch (IOException e) {
                System.err.println("Failed to get URL with pageID: " + pageID);
                continue;
            }
        }
        return urls;
    }

    public static PageSummary getPageSummary(long pageID, double score) {
        PageSummary summary = new PageSummary();
        try {
            PageMetadata metadata = DBFinder.pageMetadata.getMetadata(pageID);
            summary.url = DBFinder.pageIDHandler.getString(pageID);
            summary.score = score;
            summary.metadata = metadata;
            summary.parentLinks = pageIDToURLs(DBFinder.linkHandler.getParents(pageID));
            summary.childLinks = pageIDToURLs(DBFinder.linkHandler.getChildren(pageID));

            summary.keywords = new HashMap<>();
            LinkedList<Long> wordIDList = contentIndex.getWordListOfPage(pageID);
            for(Long wordID : wordIDList) {
                long tf = contentIndex.getFrequencyInPage(pageID, wordID);
                summary.keywords.put(DBFinder.wordIDHandler.getString(wordID), tf);
            }
        } catch (IOException e) {
            System.err.println("Failed to get page summary.");
        }
        return summary;
    }


    public static void main(String[] args) {
        setup();
        LinkedList<Entry> result = search("facebook instagram \"HKUST\"", 10);
        System.out.println("result length:" + result.size());
        for (Entry e : result) {
            try{
                PageSummary ps = getPageSummary(e.dimension, e.component);
                System.out.println("Page ID: " + e.dimension + " , Score: " + e.component + " , URL:" + DBFinder.pageIDHandler.getString(e.dimension));
            }catch(IOException ee){}
        }
    }
}

