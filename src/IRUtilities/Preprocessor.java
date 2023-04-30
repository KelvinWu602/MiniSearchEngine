package IRUtilities;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.htmlparser.Parser;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class Preprocessor {
    private StringBean sb = new StringBean();
    private LinkBean lb = new LinkBean();
    private Parser parser = new Parser();
    private Porter porter = new Porter();
    private HashSet<String> stopwords = new HashSet<String>();

    public void loadStopwords(String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;
		while((line = reader.readLine())!=null) {
			stopwords.add(line);
		}
		reader.close();
    }

    public PreprocessedPage preprocessPage(String url, PageMetadata metadata) throws ParserException, IOException {
        PreprocessedPage page = new PreprocessedPage();
        page.metadata = (metadata==null)?getMetadata(url):metadata;
        sb.setURL(url);
        String content = sb.getStrings();
        //replace metadata.size with actual content size if not specified
        page.metadata.size = (page.metadata.size==0)? content.length() : page.metadata.size;
        //extract words
        page.words = longifyString(content);
        //extract title
        page.title = longifyString(page.metadata.title);
        //extract childlinks
        lb.setURL(url);
		URL[] children = lb.getLinks();
        page.childlinkIDList = new LinkedList<Long>();
        page.childlinkList = new LinkedList<String>();
        for(URL child : children) {
            page.childlinkIDList.add(DBFinder.pageIDHandler.getID(child.toString()));
            page.childlinkList.add(child.toString());
        }
        return page;
    }

    public LinkedList<Long> longifyString(String str) throws IOException {
        LinkedList<Long> longs = new LinkedList<Long>();
        str = str.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase(); 
        StringTokenizer st = new StringTokenizer(str);
        while(st.hasMoreTokens()){
            String token = st.nextToken();
            if(!stopwords.contains(token)){
                token = porter.stripAffixes(token);
                longs.add(DBFinder.wordIDHandler.getID(token));
            }
        }
        return longs;
    }

    public Query preprocessQuery(String query) throws IOException {
        System.out.println("query: " + query);
        Query q = new Query();
        query = query.replaceAll("[^a-zA-Z0-9\"]", " ").toLowerCase();
        System.out.println("purified query: " + query);
        String[] tokens = query.split(" ");
        int i = 0;
        while(i<tokens.length){
            System.out.println("Token"+i +  ": " + tokens[i]);
            if(tokens[i].startsWith("\"")){
                LinkedList<Long> phrase = new LinkedList<>();
                i = extractPhrase(tokens, phrase, i);
                q.phrases.add(phrase);
                continue;
            }
            if(!stopwords.contains(tokens[i])){
                System.out.println("Is a content word");
                String token = porter.stripAffixes(tokens[i]);
                q.words.add(DBFinder.wordIDHandler.getID(token));
            }
            i++;
        }
        return q;
    }

    private int extractPhrase(String[] tokens, LinkedList<Long> phrase, int i) throws IOException {
        System.out.println("extractPhrase:");
        boolean doing = true;
        while(doing && i<tokens.length) {
            String token = tokens[i];
            System.out.println("Phrase token:" + token);
            if(token.startsWith("\"")) token = token.substring(1);
            if(token.endsWith("\"")){
                token = token.substring(0, token.length()-1);
                doing = false;
            } 
            if(!stopwords.contains(token)){
                token = porter.stripAffixes(token);
                phrase.add(DBFinder.wordIDHandler.getID(token));
            }
            i++;
        }
        return i;
    }

    public PageMetadata getMetadata(String url) throws ParserException {
        parser.reset();
		parser.setURL(url);
		URLConnection con = parser.getConnection();
		//use content length as size, if not specified, use 0 
        //and LATER change to content length in proprocessPage function
		long size = con.getContentLengthLong();
		size = (size<0)?0:size;
		//use last modified as timestamp, otherwise use date
		long lastmod = con.getLastModified();
		lastmod = (lastmod==0)? con.getDate():lastmod;
		//get the title
		NodeList list = parser.parse(new TagNameFilter("Title"));
		String title = list.elementAt(0).toPlainTextString();
		return new PageMetadata(title,size,lastmod);
    }
}