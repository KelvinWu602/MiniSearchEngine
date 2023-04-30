import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import IRUtilities.BTable;
import IRUtilities.DBFinder;
import IRUtilities.HTable;
import jdbm.helper.LongComparator;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;

public class Test {
    public static void main(String[] args) throws IOException {
        // DBFinder.init("test");
        // BTable<Long,Long> haha = new BTable<>(DBFinder.getBTree("haha",new LongComparator()));
        // haha.put(1L, 2L);
        // haha.put(4L, 3L);
        // haha.put(2L, 6L);
       
        // TupleBrowser bt = haha.entry();
        // Tuple t = new Tuple();
        // while(bt.getNext(t)){
        //     System.out.println((long)t.getKey() + " " + (long)t.getValue());
        // }

        // DBFinder.close();

        HashMap<Long, Long> a = new HashMap<>();
        a.put(2L,3L);
        a.put(4L,4L);
        a.put(5L,5L);
        a.put(6L,6L);
        a.put(7L,7L);
        a.put(8L,8L);
        a.put(9L,9L);


        Iterator<Map.Entry<Long,Long>> iter = a.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<Long, Long> e = iter.next();
            System.out.println(e.getKey() + " "+ e.getValue());
            if(e.getValue()==4L || e.getValue()>=6L ) iter.remove();
        }
        
        iter = a.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<Long, Long> e = iter.next();
            System.out.println(e.getKey() + " "+ e.getValue());
        }
        System.out.println(a.get(6L));
    }
}
