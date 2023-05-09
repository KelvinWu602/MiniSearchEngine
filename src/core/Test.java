package core;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Scanner;
import IRUtilities.DocVector;
import IRUtilities.Entry;
import IRUtilities.Query;

class A implements Comparable<A>{
    public int a;
    public A(int a) {
        this.a = a;
    }

    @Override
    public int compareTo(A o) {
        // TODO Auto-generated method stub
        return this.a - o.a;
    }
}

public class Test {
    public static void main(String[]args){
        SearchEngine.setup();
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.println("Input query: ");
            String query = scanner.nextLine();
            Query q = SearchEngine.preprocessQuery(query);
            // Query q = preprocessQuery("blood title sand search aka \"harry potter\"");
            LinkedList<Entry> result = SearchEngine.search(q, 10);
            System.out.println("result length:" + result.size());
            DocVector target = null;
            try {
                target = SearchEngine.getContentDocVector(result.getFirst().dimension, q.phrases);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            double averageSim = 0;
            for (Entry e : result) {
                try{
                    DocVector dv = SearchEngine.getContentDocVector(e.dimension, q.phrases);
                    averageSim +=  target.cosineSimilarity(dv);
                }catch(IOException ee){}
            }
            System.out.println("Average similarity: " + averageSim/result.size());

            System.out.println("Relevance feedback test");
            LinkedList<Long> wordIDs;
            try {
                wordIDs = SearchEngine.get5MostFrequentWords(result.getFirst().dimension);
                LinkedList<Entry> result2 = SearchEngine.relevanceFeedbackSearch(q, 10, wordIDs);
                System.out.println("result length:" + result2.size());
                double averageSim2 = 0;
                for (Entry e : result2) {
                    try{
                        DocVector dv = SearchEngine.getContentDocVector(e.dimension, q.phrases);
                        averageSim2 +=  target.cosineSimilarity(dv);
                    }catch(IOException ee){}
                }
            System.out.println("Average similarity: " + averageSim2/result.size());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }
}
