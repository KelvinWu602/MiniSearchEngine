package core;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import IRUtilities.DBFinder;
import IRUtilities.HTable;

public class PageRank {
    //Each call will update pagerank score of all pages until convergence
    //scores will be stored in a HTable
    private double threshold;
    private double alpha;
    private long N;

    public PageRank(String dbfile, double threshold, double alpha) {
        try {
            DBFinder.init(dbfile);
        } catch (IOException e) {
            System.err.println("Unable to open dbfile.");
            e.printStackTrace();
            System.exit(1);
        }

        this.threshold = threshold;
        this.alpha = alpha;
        this.N = DBFinder.pageIDHandler.size();
    }

    public void initialize() {
        //set the pagerank score of all pages to be 1
        HTable<Long,Double> pagerankScore = DBFinder.pagerankScore;
        for(long pageID = 0; pageID < DBFinder.pageIDHandler.size(); pageID++){
            try {
                pagerankScore.put(pageID, 1.0);
            } catch (IOException e) {
                System.err.println("Error when initializing pagerank score for pageID = " + pageID);
            }
        }
    }

    public void update(){
        long consecutiveConverged = 0;
        long current = 0;
        long sizeOfPages = DBFinder.pageIDHandler.size();
        HashMap<Long,Double> cache = loadCache(current, sizeOfPages);
        int cacheSize = 256;
        long nextCachePoint = (current+cacheSize)%sizeOfPages;

        while(consecutiveConverged<sizeOfPages){
            if(current==nextCachePoint){
                saveCache(cache);
                cache = loadCache(current, cacheSize);
                nextCachePoint+=cacheSize;
                nextCachePoint%=sizeOfPages;
            }
            //update pagerank score for pageID = current
            double oldPRScore;
            double newPRScore;
            try {
                oldPRScore = PR(current,cache);
                newPRScore = calculatePR(current, cache);
                // System.out.println("pageID="+current + ", old="+ oldPRScore + " , new=" + newPRScore);
                //store it in cache
                cache.put(current, newPRScore);
            } catch (IOException e) {
                System.err.println("Error when update pageID = "+current);
                current = (current+1)%sizeOfPages;                
                consecutiveConverged++;
                continue;
            }

            //check convergence
            if(Math.abs(newPRScore-oldPRScore) < threshold) {
                consecutiveConverged ++;
            }else{
                consecutiveConverged = 0;
            }
            //current ++
            current = (current+1) % sizeOfPages;
        }
        try {
            DBFinder.close();
        } catch (IOException e) {
            System.err.println("Failed to store updated pagerank score in dbfile.");
            e.printStackTrace();
        }
    }

    private double calculatePR(long pageID, HashMap<Long,Double> cache) throws IOException{
        //find all the parents of pageID
        LinkedList<Long> parents = DBFinder.linkHandler.getParents(pageID);
        if(parents==null) return 1-alpha;
        double sum = 0;
        for(Long parent : parents){
            //find no. of the children of each of the parent
            LinkedList<Long> children = DBFinder.linkHandler.getChildren(parent);
            long n = 1;
            if(children!=null){
                n = children.size();
                //load the old PR score of each of the parent
                double PR = PR(parent,cache);
                sum += PR/n;
            }
        }
        return (1-alpha) + alpha * sum;
    }

    private HashMap<Long,Double> loadCache(long start, long size){
        //load pageID = (start , size) to memory cache
        HashMap<Long,Double> cache = new HashMap<>((int) size);
        long pageranksize = DBFinder.pagerankScore.size();
        for(;start < size && start < pageranksize; start++){
            try {
                cache.put(start, DBFinder.pagerankScore.get(start));
            } catch (IOException e) {
                System.err.println("Failed to cache pageID:" + start);
                e.printStackTrace();
            }
        }
        return cache;
    }

    private double PR(long pageID, HashMap<Long,Double> cache) throws IOException {
        if(cache.containsKey(pageID)) return cache.get(pageID);
        return DBFinder.pagerankScore.get(pageID);
    }

    private void saveCache(HashMap<Long,Double> cache){
        for(Map.Entry<Long,Double> e : cache.entrySet()){
            try {
                DBFinder.pagerankScore.put((Long)e.getKey(), (Double)e.getValue());
            } catch (IOException e1) {
                System.err.println("Failed to save cache to pageID = " + (long)e.getKey());
                e1.printStackTrace();
            }
        }
    }

    
    public static void main(String [] args) {
        PageRank pagerank = new PageRank("test", 0.001, 0.1);
        pagerank.initialize();
        pagerank.update();
    }

}
