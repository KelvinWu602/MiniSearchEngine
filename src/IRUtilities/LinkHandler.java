package IRUtilities;

import java.io.IOException;
import java.util.LinkedList;

public class LinkHandler {
    private HTable<Long, LinkedList<Long>> forwardLinks;
    private HTable<Long, LinkedList<Long>> backLinks;

    public LinkHandler() throws IOException {
        forwardLinks = new HTable<Long, LinkedList<Long>>(DBFinder.getHTree(TableNames.PARENT_CHILDREN));
        backLinks = new HTable<Long, LinkedList<Long>>(DBFinder.getHTree(TableNames.CHILD_PARENT));
    }

    public void addLink(Long parent, LinkedList<Long> children) throws IOException {
        forwardLinks.put(parent, children);
        for(Long child : children) {
            LinkedList<Long> parents = backLinks.get(child);
            if(parents==null) {
                parents = new LinkedList<Long>();
            }
            parents.add(parent);
            backLinks.put(child, parents);
        }
    }

    public LinkedList<Long> getChildren(Long parent) throws IOException {
        return forwardLinks.get(parent);
    }

    public LinkedList<Long> getParents(Long child) throws IOException {
        return backLinks.get(child);
    }
}
