package IRUtilities;

import java.io.IOException;
import java.util.LinkedList;

public class IDHandler {
    private HTable<String,Long> strToID;
    private HTable<Long,String> IDToStr;
    private long nextID = 0;
    
    public IDHandler(String string_to_id, String id_to_string) throws IOException {
        strToID = new HTable<String,Long>(DBFinder.getHTree(string_to_id),256L);
        IDToStr = new HTable<Long,String>(DBFinder.getHTree(id_to_string),256L);
        nextID = strToID.size();
    }

    public long getID(String str) throws IOException {
        Long id = (Long)strToID.get(str);
        if(id==null) {
            id = nextID++;
            strToID.put(str, id);
            IDToStr.put(id, str);
        }
        return id;
    }

    public String getString(long id) throws IOException {
        return (String)IDToStr.get(id);
    }

    public long size() {
        return strToID.size();
    }

    public long getPhraseTempID(LinkedList<Long> phrase){
        //the phrase ID is a temporary ID, used to indicate the dimension of the phrase only
        //its lifespan is for 1 search. After the search is done, the phrase ID is useless
        
        //depends on the words in phrase as well as the current size of dictionary
        //only a tiny probability of collision, neglect the case
        return nextID + phrase.hashCode()%(Long.MAX_VALUE-nextID);
    }
}