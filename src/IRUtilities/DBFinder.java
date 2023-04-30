package IRUtilities;

import java.io.IOException;
import java.util.Comparator;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.btree.BTree;
import jdbm.htree.HTree;

public class DBFinder {
    public static RecordManager recman = null;
    public static IDHandler wordIDHandler = null;
    public static IDHandler pageIDHandler = null;
    public static LinkHandler linkHandler = null;
    public static CyclicChecker cyclicCheck = null;
    public static MetadataHandler pageMetadata = null;
    public static HTable<Long,Double> pagerankScore = null;


    public static void init(String dbPath) throws IOException {
        if(recman!=null) return;
        recman = RecordManagerFactory.createRecordManager(dbPath);
        wordIDHandler = new IDHandler(TableNames.WORD_ID,TableNames.ID_WORD);
        pageIDHandler = new IDHandler(TableNames.PAGE_ID,TableNames.ID_PAGE);
        linkHandler = new LinkHandler();
        cyclicCheck = new CyclicChecker();
        pageMetadata = new MetadataHandler();
        pagerankScore = new HTable<>(getHTree(TableNames.ID_PAGERANK));
    }

    public static void close() throws IOException {
        cyclicCheck.close();
        recman.commit();
    }

    public static HTree getHTree(String tableName) throws IOException {
        if(recman==null) {
            System.err.println("Uninitialized database manager.");
            return null;
        }
        long recid = recman.getNamedObject(tableName);
        HTree ht = null;
		if(recid!=0) {
			ht = HTree.load(recman, recid);
		}else {
			ht = HTree.createInstance(recman);
			recman.setNamedObject(tableName, ht.getRecid());
		}
		System.out.println(tableName + ", " + ht.getRecid());
		return ht;        
    }

    public static HTree getHTree(long tableID, boolean create) throws IOException {
        if(recman==null) {
            System.err.println("Uninitialized database manager.");
            return null;
        }
        HTree ht = null;
        if(create){
            ht = HTree.createInstance(recman);
        }else{
            ht = HTree.load(recman, tableID);
        }
        return ht;
    }

    public static BTree getBTree(String tableName, Comparator comp) throws IOException {
        if(recman==null) {
            System.err.println("Uninitialized database manager.");
            return null;
        }
        long recid = recman.getNamedObject(tableName);
        BTree bt = null;
		if(recid!=0) {
			bt = BTree.load(recman, recid);
		}else {
			bt = BTree.createInstance(recman, comp);
			recman.setNamedObject(tableName, bt.getRecid());
		}
		System.out.println(tableName + ", " + bt.getRecid());
		return bt;        
    }

    public static BTree getBTree(Long tableID, Comparator comp) throws IOException {
        if(recman==null) {
            System.err.println("Uninitialized database manager.");
            return null;
        }
        BTree bt = null;
        if(tableID!=null) {
            bt = BTree.load(recman, tableID);
        }
        if(bt==null){
            bt = BTree.createInstance(recman, comp);
        }
        return bt;
    }

}