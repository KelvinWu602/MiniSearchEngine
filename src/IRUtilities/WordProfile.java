package IRUtilities;

import java.io.Serializable;

public class WordProfile implements Serializable, Comparable<WordProfile> {
    public Long wordID;
    public Long frequency;
    public String word;
    public WordProfile(long wordID, long frequency){
        this.wordID = wordID;
        this.frequency = frequency;
        this.word = null;
    }
    @Override
    public int compareTo(WordProfile o) {
        // TODO Auto-generated method stub
        return this.frequency.compareTo(o.frequency);
    }
}
