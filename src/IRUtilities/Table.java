package IRUtilities;

import java.io.IOException;
import java.io.Serializable;


public interface Table<K extends Serializable,V extends Serializable> {    
    public void put(K key, V value) throws IOException;
    public V get(K key) throws IOException; 
    public void remove(K key) throws IOException;
    public long size();
    public boolean contains(K key) throws IOException;
    public long getID();
}