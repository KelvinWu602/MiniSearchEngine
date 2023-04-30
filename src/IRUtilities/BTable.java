package IRUtilities;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import jdbm.btree.BTree;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;

public class BTable<K extends Serializable,V extends Serializable> implements Table<K,V> {
    BTree table;

    public BTable(BTree src) throws IOException{
        table = src;
    }

    @Override
    public void put(K key, V value) throws IOException {
        table.insert(key, value, get(key) != null);
    }

    @Override
    public V get(K key) throws IOException{
        return (V)table.find(key);
    }

    @Override
    public long size() {
        return table.size();
    }

    @Override
    public boolean contains(K key) throws IOException{
        return table.find(key)!=null;
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
        if(contains(key)) table.remove(key);
    }
}