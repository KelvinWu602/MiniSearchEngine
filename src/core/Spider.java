package core;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.htmlparser.util.ParserException;

import IRUtilities.DBFinder;
import IRUtilities.PreprocessedPage;
import IRUtilities.Preprocessor;
import IRUtilities.TableNames;
import jdbm.htree.HTree;

public class Spider {
    private Queue<String> urls;
    private Preprocessor preprocessor;
    private Index contentIndex;
    private Index titleIndex;

    public Spider(String dbPath) {
        urls = new LinkedList<String>();
        preprocessor = new Preprocessor();
        try {
            preprocessor.loadStopwords("stopwords.txt");
        }catch (IOException e){
            System.err.println("Error loading stopwords.txt");
            e.printStackTrace();
        }
        try {
            DBFinder.init(dbPath);
        } catch (IOException e) {
            System.err.println("Error initializing DBFinder with dbPath:" + dbPath);            
            e.printStackTrace();
        }
        try {
            contentIndex = new Index(TableNames.CONTENT_INDEX);
            titleIndex = new Index(TableNames.TITLE_INDEX);
        } catch (IOException e) {
            System.err.println("Error initializing Index.");            
            e.printStackTrace();
        }
    }

    public void setStartingURL(String url) {
        urls.offer(url);
    }

    public int crawlPage() {
        //return 0 if first visit and updated index
        //return 1 if first visit and did not update index
        //return 2 if already visited
        //return 3 if exception
        String url = urls.poll();
        System.out.println("Crawling: " +url);

        try {
            Long pageID = DBFinder.pageIDHandler.getID(url);
            //test if url is visited
            if(DBFinder.cyclicCheck.isVisited(pageID)){
                System.out.println("Already visited");
                return 2;
            }
            //if not visited, add to visited list
            DBFinder.cyclicCheck.visit(pageID);
            //Deal with child links
            PreprocessedPage pp = preprocessor.preprocessPage(url, null);
            DBFinder.linkHandler.addLink(pageID, pp.childlinkIDList);
            urls.addAll(pp.childlinkList);
            System.out.println("First visit in this execution. " + pp.childlinkList.size() + " ChildLink added.");
            //test if index info of this url is updated
            if(DBFinder.pageMetadata.pageUpToDate(pageID,pp.metadata.lastModified)){
                System.out.println(url + " is up to date. Not saved.");
                return 1;
            }
            //if not updated, delete old index info
            contentIndex.removePage(pageID);
            titleIndex.removePage(pageID);
            //add new index info
            DBFinder.pageMetadata.addMetadata(pageID, pp.metadata);
            contentIndex.forwardIndexAdd(pageID,pp.words);
            titleIndex.forwardIndexAdd(pageID, pp.title);
            long position = 0;
            for(Long wordID : pp.words){
                contentIndex.invertedIndexAdd(pageID,wordID, position++);
            }
            position = 0;
            for(Long wordID : pp.title){
                titleIndex.invertedIndexAdd(pageID,wordID, position++);
            }
        }catch (IOException e) {
			// e.printStackTrace();
			System.out.println("IOException when crawling "+url);
			try {
				DBFinder.recman.rollback();
				System.out.println("Rollback successfully");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				System.out.println("Rollback failed, dbfile corrupted. Please delete the dbfile and restart the program");
                System.exit(1);
			}
			return 3;
		} catch (ParserException e) {
			// e.printStackTrace();
			System.out.println("ParserException (certificate problem) when crawling "+url);
			try {
				DBFinder.recman.rollback();
				System.out.println("Rollback successfully");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				System.out.println("Rollback failed, dbfile corrupted. Please delete the dbfile and restart the program");
                System.exit(1);
			}
			return 3;
		} 
        return 0;
    }

    public void crawlPages(long n) {
		long crawled = 0;
		while(crawled<n && urls.size()>0) {
			System.out.println("URL No. " + crawled + ":");
			int state = crawlPage();
			if(state==0 || state==1) {
				crawled++;
				try {
					DBFinder.recman.commit();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Failed to save.");
				}
			}
		}
        try{
            DBFinder.close();
        }catch(IOException e){
            System.out.println("Failed to close DBFinder.");
            e.printStackTrace();
        }
	}

    public static void main(String[] args) {
        // int numOfPages = Integer.parseInt(args[0]);
        Spider spider = new Spider("test");
        // spider.setStartingURL("http://www.cse.ust.hk");
        // spider.setStartingURL("http://ias.ust.hk/");
        // spider.setStartingURL("http://ias.ust.hk/web/ias/eng/");
        spider.setStartingURL("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm");
        // spider.crawlPages(numOfPages);
        spider.crawlPages(300);
        System.out.println("Complete");
    }
}
