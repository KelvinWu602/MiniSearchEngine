package IRUtilities;

import java.io.IOException;

public class CyclicChecker {
    private HTable<Long,Boolean> visited;
    private long size = 0;

    public CyclicChecker() throws IOException{
        visited = new HTable<Long,Boolean>(DBFinder.getHTree(0,true), 128L);    
    }

    public void visit(Long pageID) throws IOException{
        visited.put(pageID, true);
    }

    public boolean isVisited(Long pageID) throws IOException{
        Boolean result = visited.get(pageID);
        return result!=null && result;
    }

    public void close() throws IOException {
        DBFinder.recman.delete(visited.getID());
    }


}
