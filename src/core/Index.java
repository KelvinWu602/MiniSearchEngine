package core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;


import IRUtilities.BTable;
import IRUtilities.DBFinder;
import IRUtilities.DocVecCache;
import IRUtilities.DocVector;
import IRUtilities.HTable;
import IRUtilities.LRUCache;
import IRUtilities.Posting;
import jdbm.helper.LongComparator;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;

public class Index {
    //content index or title index

    //pageID --> List<wordID>
    private HTable<Long, LinkedList<Long>> forwardIndex; 
    //wordID --> BTree ID (BTree PostingList)
    private HTable<Long, Long> invertedIndex;
    //pageID --> Posting
    private BTable<Long, Posting> postingList;
    //pageID --> DocVecCache {tfmax, {wordID --> tfidf}}
    private LRUCache<Long, DocVecCache> docVecCache;

    public Index(String name) throws IOException {
        forwardIndex = new HTable<Long, LinkedList<Long>>(DBFinder.getHTree(name + "_forwardIndex"), 256L);
        invertedIndex = new HTable<Long, Long>(DBFinder.getHTree(name + "_invertedIndex"), 256L);
        docVecCache = new LRUCache<Long, DocVecCache>(512); //cache 512 pages
    }

    public void forwardIndexAdd(long pageID,LinkedList<Long> wordIDList) throws IOException {
        LinkedList<Long> wordIDs = forwardIndex.get(pageID);
        if(wordIDs==null) {
            wordIDs = new LinkedList<Long>();
        }
        wordIDs.addAll(wordIDList);
        forwardIndex.put(pageID, wordIDs);
    }

    public void invertedIndexAdd(long pageID, long wordID, long position) throws IOException {
        postingList = pageContains(wordID);
        if(!invertedIndex.contains(wordID)) invertedIndex.put(wordID, postingList.getID());
        Posting posting = postingList.get(pageID);
        if(posting==null) {
            posting = new Posting(pageID);
        }
        posting.frequency++;
        posting.positions.add(position);
        postingList.put(pageID, posting);
    }

    public void removePage(long pageID) throws IOException {
        LinkedList<Long> wordIDs = forwardIndex.get(pageID);
        if(wordIDs==null) return;
        for(Long wordID : wordIDs){
            postingList = pageContains(wordID);
            postingList.remove(pageID);
        }
        forwardIndex.remove(pageID);
    }

    //the document vector of a page
    //new dimension for each phrase
    public DocVector getDocVector(long pageID, LinkedList<LinkedList<Long>> phrases) throws IOException {    
        //forwardIndex pageID --> wordList
        //docVecCache  pageID --> {tfmax, {wordID --> tfidf}}
        //check if the pageID is in the docVecCache
        DocVecCache docVecCacheEntry = docVecCache.get(pageID);
        DocVector docVector;
        double N = forwardIndex.size();
        double tfmax = 1;
        if(docVecCacheEntry!=null) {
            // System.out.println("Hit");
            docVector = new DocVector(docVecCacheEntry.wordIDToTW);
            tfmax = docVecCacheEntry.tfmax;
        }else{
            docVector = new DocVector(); //ok in speed
            LinkedList<Long> wordIDs = forwardIndex.get(pageID); //ok in speed
            if(wordIDs==null || wordIDs.size()==0) {
                return docVector;
            }
            tfmax = 0;

            for(long wordID : wordIDs) {
                //calculate the term weight for this page
                postingList = pageContains(wordID); //slow
                Posting posting = postingList.get(pageID); //slow
                double df = postingList.size(); ///fast
                if(df>0){
                    double tf = (posting!=null)?posting.frequency:0; //fast
                    double idf = Math.log(N/df)/Math.log(2); //fast
                    // System.out.println("term weight for word:" + tf*idf);
                    docVector.addDimension(wordID, tf*idf); //fast
                    //find the tfmax
                    tfmax = Math.max(tf, tfmax);
                }
            }

            //cache the docVector
            docVector.multiplyScalar(1.0/tfmax);
            DocVecCache newDocVecCacheEntry = new DocVecCache();
            newDocVecCacheEntry.tfmax = tfmax;
            newDocVecCacheEntry.wordIDToTW = docVector.getHashMap(); //clone a hashmap for the cache so that it will not include the phrase
            docVecCache.put(pageID, newDocVecCacheEntry);
            // System.out.println("Put " + pageID + " cache size " + docVecCache.size());
        }
        
        //cache tfxidf for each of the phrase
        for(LinkedList<Long> phrase : phrases) {
            HashMap<Long,Posting> postingList = pageContains(phrase);
            Posting posting = postingList.get(pageID);
            double df = postingList.size();
            if(df>0){
                double tf = (posting!=null)?posting.frequency:0;
                double idf = Math.log(N/df)/Math.log(2);
                docVector.addDimension(DBFinder.wordIDHandler.getPhraseTempID(phrase), tf*idf);
            }
            //max single word tf >= max phrase tf
        }    

        docVector.multiplyScalar(1.0/tfmax);
        return docVector;
    }

    //return only the list of pageID containing the word
    public static LinkedList<Long> toPageIDs(BTable<Long,Posting> postingList) throws IOException {
        LinkedList<Long> pageIDs = new LinkedList<Long>();
        TupleBrowser browser = postingList.entry();
        Tuple t = new Tuple();
        while (browser.getNext(t)) {
            pageIDs.add((long)t.getKey());
        }
        return pageIDs;
    }

    public static LinkedList<Long> toPageIDs(HashMap<Long,Posting> postingList) throws IOException {
        LinkedList<Long> pageIDs = new LinkedList<Long>();
        for(Long pageID : postingList.keySet()) {
            pageIDs.add(pageID);
        }
        return pageIDs;
    }

    //return the list of posting containing the word
    public BTable<Long,Posting> pageContains(long wordID) throws IOException {
        Long postingListID = invertedIndex.get(wordID); //ok in speed
        postingList = new BTable<Long, Posting>(DBFinder.getBTree(postingListID, new LongComparator()));
        return postingList;
    }

    //return the list of pageID containing the phrase
    public HashMap<Long,Posting> pageContains(LinkedList<Long> phrase) throws IOException {
        HashMap<Long,Posting> valid = pageContains(phrase.getFirst()).toHashMap();
        //starting from the second word in phrase
        for(int i=1;i<phrase.size();i++) {
            // System.out.println("Valid pages have "+ valid.size() + " , wordA = " + DBFinder.wordIDHandler.getString(phrase.get(i-1)));
            BTable<Long,Posting> temp = pageContains(phrase.get(i));
            //for each posting in the valid list
            Iterator<Map.Entry<Long,Posting>> iter = valid.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Long,Posting> entry = iter.next();
                Long pageID = entry.getKey();
                Posting posting = entry.getValue();
                //if the pageID is not in temp, remove it from valid 
                if(temp.get(pageID)==null){
                    iter.remove();
                    continue;
                }
                //if temp does not have page that have the next word showing up, remove it from valid
                LinkedList<Long> combinedPos = existConsecutive(posting.positions, temp.get(pageID).positions);
                // System.out.println(pageID + " ,word B = " + DBFinder.wordIDHandler.getString(phrase.get(i)));
                // System.out.println("wordA:" + posting.positions.toString());
                // System.out.println("wordB:" + temp.get(pageID).positions.toString());
                // System.out.println("combined:" + combinedPos.toString() + "\n");

                if(combinedPos.size()==0) {
                    iter.remove();
                    continue;
                }
                Posting updatedPosting = new Posting(pageID);
                updatedPosting.frequency = combinedPos.size();
                updatedPosting.positions = combinedPos;
                valid.put(pageID, updatedPosting);
            }            
        }
        return valid;
    }

    private LinkedList<Long> existConsecutive(LinkedList<Long> a, LinkedList<Long> b) throws IOException {
        //check if two sorted list has elements that are consecutive in the other list
        LinkedList<Long> result = new LinkedList<>();
        if(a.size()==0 || b.size()==0) {
            return result;
        }
        Iterator<Long> iterA = a.iterator();
        Iterator<Long> iterB = b.iterator();
        while(iterA.hasNext()) {
            Long aID = iterA.next();
            while(iterB.hasNext()){
                Long bID = iterB.next();
                if(bID<=aID) {
                    continue;
                }
                if(bID==aID+1) {
                    result.add(bID);
                    break;
                }
                if(bID>aID+1){
                    break;
                }
            }
        }
        return result;
    }

    public LinkedList<Long> getWordListOfPage(long pageID) throws IOException {
        return (LinkedList<Long>)forwardIndex.get(pageID);
    }

    public long getFrequencyInPage(long pageID, long wordID) throws IOException {
        long postingsID = invertedIndex.get(wordID);
        BTable<Long,Posting> postingList = new BTable<Long, Posting>(DBFinder.getBTree(postingsID, new LongComparator()));
        Posting posting = postingList.get(pageID);
        // System.out.println("pageID:" + pageID + " word:" + DBFinder.wordIDHandler.getString(wordID) + " frequency:" + posting.frequency + "\n");
        return posting.frequency;
    }

}