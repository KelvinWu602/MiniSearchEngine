package IRUtilities;

import java.io.IOException;
import java.io.Serializable;

import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

public class HTable<K extends Serializable,V extends Serializable> implements Table<K,V> {
    private HTree table;
    private long size;
    private LRUCache<K,V> cache;

    public HTable(HTree src, long cacheSize) throws IOException{
        table = src;
        size = 0;
        cache = new LRUCache<K,V>(cacheSize);
        //count the number of keys in the table
        //also fill up the cache
        FastIterator iter = table.keys();
        while(iter.next()!=null){
            size++;
        }
    }

    @Override
    public void put(K key, V value) throws IOException {
        V previous = get(key);
        if(previous==null) size ++;
        if(previous!=null && value==null) size--;
        //store it in both storage and cache
        table.put(key, value);
        cache.put(key, value);
    }

    @Override
    public V get(K key) throws IOException{
        //if hit, then return cache value
        if(cache.get(key)!=null) return cache.get(key);
        //if not hit, then return storage value, also put it in cache
        V value = (V)table.get(key);
        cache.put(key, value);
        return value;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public boolean contains(K key) throws IOException{
        //if it is found in cache, great
        //otherwise if it is found in storage, great
        return cache.get(key)!=null || table.get(key)!=null;
        // return table.get(key)!=null;
    }

    @Override
    public long getID() {
        return table.getRecid();
    }

    public FastIterator keys() throws IOException{
        return table.keys();
    }

    public FastIterator values() throws IOException{
        return table.values();
    }

    @Override
    public void remove(K key) throws IOException {
        if(contains(key)) {
            size--;
            cache.remove(key);
            table.remove(key);
        }
    }

}