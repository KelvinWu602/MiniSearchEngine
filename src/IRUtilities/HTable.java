package IRUtilities;

import java.io.IOException;
import java.io.Serializable;

import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

public class HTable<K extends Serializable,V extends Serializable> implements Table<K,V> {
    HTree table;
    long size;

    public HTable(HTree src) throws IOException{
        table = src;
        size = 0;
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
        table.put(key, value);
    }

    @Override
    public V get(K key) throws IOException{
        return (V)table.get(key);
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public boolean contains(K key) throws IOException{
        return table.get(key)!=null;
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
            table.remove(key);
        }
    }
}