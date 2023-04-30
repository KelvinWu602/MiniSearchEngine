package IRUtilities;

import java.util.LinkedList;

public class Query {
    public LinkedList<Long> words = new LinkedList<>();
    public LinkedList<LinkedList<Long>> phrases = new LinkedList<>();

    public DocVector vectorize() {
        DocVector vec = new DocVector();
        if(words!=null) {
            for(Long word : words) {
                vec.addDimension(word, 1);
            }
        }
        // System.out.println("Phrase IDs:" + phrases.size() + " phrases in total.");
        if(phrases!=null) {
            for(LinkedList<Long> phrase : phrases) {
                // System.out.println(DBFinder.wordIDHandler.getPhraseTempID(phrase));
                vec.addDimension(DBFinder.wordIDHandler.getPhraseTempID(phrase), 1);
            }
        }
        return vec;
    }
}
