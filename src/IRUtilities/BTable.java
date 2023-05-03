package IRUtilities;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import jdbm.btree.BTree;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;

public class BTable<K extends Serializable,V extends Serializable> implements Table<K,V> {
    private BTree table;
    private LRUCache<K,V> cache;
    private static final long cacheSize = 256L;

    public BTable(BTree src) throws IOException{
        table = src;
        cache = new LRUCache<K,V>(cacheSize);
        TupleBrowser tb = table.browse();
        Tuple t = new Tuple();
        long size = 0;
        while(size++<cacheSize && tb.getNext(t)){
            cache.put((K)t.getKey(), (V)t.getValue());
        }
    }

    @Override
    public void put(K key, V value) throws IOException {
        table.insert(key, value, get(key) != null);
        cache.put(key, value);
    }

    @Override
    public V get(K key) throws IOException{
        //if hit, then return cache value
        if(cache.get(key)!=null) return cache.get(key);
        //if not hit, then return storage value, also put it in cache
        V value = (V)table.find(key);
        cache.put(key, value);
        return value;
    }

    @Override
    public long size() {
        return table.size();
    }

    @Override
    public boolean contains(K key) throws IOException{
        return cache.get(key)!=null || table.find(key)!=null;
    }

    @Override
    public long getID() {
        return table.getRecid();
    }

    public TupleBrowser entry() throws IOException{
        return table.browse();
    }

    public HashMap<K,V> toHashMap() throws IOException {
        HashMap<K,V> map = new HashMap<K,V>();
        Tuple t = new Tuple();
        TupleBrowser browser = table.browse();
        while(browser.getNext(t)){
            map.put((K)t.getKey(),(V)t.getValue());
        }
        return map;
    }

    @Override
    public void remove(K key) throws IOException {
        if(contains(key)) {
            cache.remove(key);
            table.remove(key);
        }
    }
}