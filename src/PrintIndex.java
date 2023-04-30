import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import IRUtilities.BTable;
import IRUtilities.DBFinder;
import IRUtilities.HTable;
import IRUtilities.Porter;
import IRUtilities.Posting;
import IRUtilities.Preprocessor;
import IRUtilities.TableNames;
import jdbm.helper.FastIterator;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;

public class PrintIndex {
    public static void main(String[] args) throws IOException{
        DBFinder.init("test");
        Index contentIndex = new Index(TableNames.CONTENT_INDEX);
        System.out.println("\nFirst 5 words:");
        for(int i = 0 ; i < 5 ; i++){
            System.out.println("word: " + DBFinder.wordIDHandler.getString(i) + " , " + i);
        }
        System.out.println("\nFirst 3 URL:");
        for(int i = 0 ; i < 3 ; i++){
            System.out.println("page: " + DBFinder.pageIDHandler.getString(i) + " , " + i);
        }

        // System.out.println("All key value pairs in inverted index:");
        // HTable<Long,Long> invertedIndex = new HTable<Long, Long>(DBFinder.getHTree(TableNames.CONTENT_INDEX + "_invertedIndex"));
        // FastIterator it = invertedIndex.keys();
        // Long key;
        // while((key = (Long)it.next())!=null){
        //     System.out.println(key + ":" + invertedIndex.get(key));
        // }
        // System.out.println("No. of index words: " + DBFinder.wordIDHandler.size());
        // System.out.println("No. of pages: " + DBFinder.pageIDHandler.size());

        // BTable<Long,Posting> postingList2 = contentIndex.pageContains(DBFinder.wordIDHandler.getID("school"));
        // TupleBrowser bt2 = postingList2.entry();
        // Tuple t2 = new Tuple();
        // System.out.println("\nNo. of pages containing the word:" + postingList2.size());
        // while(bt2.getNext(t2)){
        //     System.out.println("Page ID:" + t2.getKey());
        // }



        Porter porter = new Porter();
        LinkedList<Long> phrase = new LinkedList<>();
        String phrases = "hong kong";
        for(String word : phrases.split(" ")){
            phrase.add(DBFinder.wordIDHandler.getID(porter.stripAffixes(word)));
        }
        System.out.println("Phrase:" + phrase.toString());

        HashMap<Long,Posting> postingList = contentIndex.pageContains(phrase);
        System.out.println("\nNo. of pages containing the word:" + postingList.size());
        
        postingList = contentIndex.pageContains(phrase);
        System.out.println("\nNo. of pages containing the word:" + postingList.size());
        
    }
}
