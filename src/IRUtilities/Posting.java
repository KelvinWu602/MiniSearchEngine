package IRUtilities;

import java.io.Serializable;
import java.util.LinkedList;

public class Posting implements Serializable{
    public long pageID;
    public long frequency;
    public LinkedList<Long> positions;
    public Posting(long pageID) {
        this.pageID = pageID;
        this.frequency = 0;
        this.positions = new LinkedList<Long>();
    }
}
