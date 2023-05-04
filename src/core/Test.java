package core;

import java.util.HashMap;
import java.util.Map;

import IRUtilities.LRUCache;

class A {
    public int a;
    public A(int a) {
        this.a = a;
    }
}

public class Test {
    public static void main(String[]args){
        HashMap<Integer,A> cache = new HashMap<>();
        cache.put(1,new A(1));
        cache.put(2,new A(1));
        cache.put(3,new A(1));

        
        for(Map.Entry<Integer,A> e : cache.entrySet()){
            e.getValue().a = 3;
        }

        for(Map.Entry<Integer,A> e : cache.entrySet()){
            System.out.println(e.getValue().a);
        }
    }
}
