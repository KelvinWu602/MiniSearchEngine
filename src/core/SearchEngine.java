package core;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

import IRUtilities.DBFinder;
import IRUtilities.DocVector;
import IRUtilities.Entry;
import IRUtilities.HTable;
import IRUtilities.PageMetadata;
import IRUtilities.PageSummary;
import IRUtilities.Preprocessor;
import IRUtilities.Query;
import IRUtilities.TableNames;
import IRUtilities.WordProfile;

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

    public static Query preprocessQuery(String query) {
        Query q;
        try {
            q = preprocessor.preprocessQuery(query);
        } catch (IOException e) {
            System.err.println("Failed to preprocess query.");
            return new Query();
        }
        return q;
    }

    // a function for testing
    public static DocVector getContentDocVector(long pageID, LinkedList<LinkedList<Long>> phrases) throws IOException{
        return contentIndex.getDocVector(pageID, phrases);
    }

    public static LinkedList<Entry> search(Query q, int numResults) {
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
        //Do not return all pagerank score, only add pagerank score to matched pages
        HTable<Long,Double> pagerankScore = null;
        pagerankScore = DBFinder.pagerankScore;

        //combine scores
        DocVector scores = DocVector.add(contentscores,titlescores);
        if(pagerankScore!=null) {
            for(Entry e : scores.linearize()){
                Double PRscore = null;
                try {
                    PRscore = pagerankScore.get(e.dimension);
                } catch (IOException e1) {}
                if(PRscore!=null){
                   scores.put(e.dimension, e.component + PRscore);
                }
            }
        }
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
        System.out.println("SortedScores length:" + sortedScores.size());
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
                if(scores.containsKey(pageID)) continue;
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
                if(scores.containsKey(pageID)) continue;
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
        if(pageIDs==null) return urls;
        for(Long pageID : pageIDs){
            String url = "";
            try {
                url = DBFinder.pageIDHandler.getString(pageID);
            } catch (IOException e) {
                System.err.println("Failed to get URL with pageID: " + pageID);
                continue;
            }
            if(url!=null) urls.add(url);
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

            summary.keywords = contentIndex.getWordListOfPage(pageID, 5);
            for(WordProfile word : summary.keywords) {
                word.word = DBFinder.wordIDHandler.getString(word.wordID);
            }
        } catch (Exception e) {
            System.err.println("Failed to get page summary for pageID: " + pageID);
        }
        return summary;
    }

    public static LinkedList<Long> get5MostFrequentWords(long pageID) throws IOException {
        LinkedList<WordProfile> wordIDList = contentIndex.getWordListOfPage(pageID,5);
        LinkedList<Long> result = new LinkedList<Long>();
        for(WordProfile word : wordIDList) {
            result.add(word.wordID);
        }
        return result;
    }

    public static LinkedList<Entry> relevanceFeedbackSearch(Query rawQ, int numResults, LinkedList<Long> relevantWordIDs) {
        rawQ.words.addAll(relevantWordIDs);
        return search(rawQ, numResults);
    }


    public static void main(String[] args) {
        setup();
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.println("Input query: ");
            String query = scanner.nextLine();
            long start = System.currentTimeMillis();
            Query q = preprocessQuery(query);
            // Query q = preprocessQuery("blood title sand search aka \"harry potter\"");
            LinkedList<Entry> result = search(q, 50);
            System.out.println("result length:" + result.size());
            for (Entry e : result) {
                try{
                    PageSummary ps = getPageSummary(e.dimension, e.component);
                    System.out.println("Page ID: " + e.dimension + " , Score: " + e.component + " , URL:" + DBFinder.pageIDHandler.getString(e.dimension));
                    System.out.println(ps.metadata.lastModified + " " + ps.metadata.title + " " + ps.metadata.size);
                    int i = 0;
                    for(WordProfile entry : ps.keywords){
                        if(i++>5) break;
                        System.out.print(entry.word + " " + entry.frequency + "; ");
                    }
                    System.out.println("");
                    System.out.println("parent");
                    i = 0;
                    for(String s : ps.parentLinks){
                        if(i++>5) break;
                        System.out.println(s);
                    }
                    i=0;
                    System.out.println("child");
                    for(String s : ps.parentLinks){
                        if(i++>5) break;
                        System.out.println(s);
                    }
                    System.out.println();
                }catch(IOException ee){}
            }
            System.out.println("Time used: " + (System.currentTimeMillis() - start) + " ms");

            // System.out.println("Relevance feedback test");
            // LinkedList<Long> wordIDs;
            // try {
            //     wordIDs = SearchEngine.get5MostFrequentWords(result.getFirst().dimension);
            //     LinkedList<Entry> result2 = SearchEngine.relevanceFeedbackSearch(q, 50, wordIDs);
            //     System.out.println("result length:" + result2.size());
            //     for (Entry e : result2) {
            //         try{
            //             PageSummary ps = getPageSummary(e.dimension, e.component);
            //             System.out.println("Page ID: " + e.dimension + " , Score: " + e.component + " , URL:" + DBFinder.pageIDHandler.getString(e.dimension));
            //         }catch(IOException ee){}
            //     }
            // } catch (IOException e1) {
            //     // TODO Auto-generated catch block
            //     e1.printStackTrace();
            // }
        }
    }
}

