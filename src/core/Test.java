package core;

import IRUtilities.LRUCache;

public class Test {
    public static void main(String[]args){
        LRUCache<Integer,Integer> cache = new LRUCache<>(5);
        cache.put(1,1);
        cache.print();
        cache.put(2,1);
        cache.print();
        cache.put(3,1);
        cache.print();
        cache.put(4,1);
        cache.print();
        cache.put(5,1);
        cache.print();
        cache.put(6,1);
        cache.print();
        cache.put(7,1);
        cache.print();
        cache.put(8,1);
        cache.print();
        cache.put(9,1);
        cache.print();
        cache.put(10,1);
        cache.print();
    }
}
