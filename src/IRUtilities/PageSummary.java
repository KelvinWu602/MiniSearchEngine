package IRUtilities;

import java.util.HashMap;
import java.util.LinkedList;

/**
 *  score page title
        url
        last modification date, size of page
        keyword 1 freq 1; keyword 2 freq 2; . . .
        Parent link 1
        Parent link 2
        ... ...
        Child link 1
        Child link 2
        ... .. 
 */


public class PageSummary {
    public double score;
    public String url;
    public PageMetadata metadata;
    public LinkedList<WordProfile> keywords;
    public LinkedList<String> parentLinks;
    public LinkedList<String> childLinks;
}
