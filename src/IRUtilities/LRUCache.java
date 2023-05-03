package IRUtilities;

import java.util.HashMap;

class Node<K,V> {
    public K key = null;
    public V value = null;
    public Node<K,V> next = null;
    public Node<K,V> previous = null;

    public Node(K key, V value) {
        this.key = key;
        this.value = value;
    }
}

class DoublyLinkedList<K,V>{
    public Node<K,V> head = null;
    public Node<K,V> tail = null;
    public long size = 0;

    public void addToHead(Node<K,V> node){
        if(head==null){
            //empty list
            head = node;
            tail = node;
            node.next = node.previous = null;
        }else{
            node.previous = null;
            node.next = head;
            head.previous = node;
            head = node;    
        }
        size++;
    }

    public void removeLast() {
        if(tail!=null){
            if(head==tail){
                //only one node
                tail.previous = null;
                tail.next = null;
                head = null;
                tail = null;                
            }else{
                //remove the tail
                tail.previous.next = null;
                tail = tail.previous;
            }
            size--;
        }
    }

    public void moveToTop(Node<K,V> node){
        //assume the node is in the chain
        //if the node is already the head
        if(head!=null && head.key == node.key) return;
        //if the node is at the end
        if(node.previous !=null && node.next==null){
            removeLast();
        }
        //if the node is in the middle
        else{
            node.previous.next = node.next;
            node.next.previous = node.previous;
        }
        addToHead(node);
    }
}

public class LRUCache<K,V> {
    private long capacity;
    private HashMap<K,Node<K,V>> map;
    private DoublyLinkedList<K,V> list;

    public LRUCache(long size){
        this.capacity = size;
        this.map = new HashMap<K,Node<K,V>>();
        this.list = new DoublyLinkedList<K,V>();
    }

    public void put(K key, V value){
        if(map.containsKey(key)){
            map.get(key).value = value;
            return;
        }
        if(list.size>=capacity){
            //if cache overflow, remove the least recently used
            map.remove(list.tail.key);
            list.removeLast();
        }
        //Add it to the head
        Node<K,V> newnode = new Node<K,V>(key, value);
        list.addToHead(newnode);
        map.put(key, newnode);
    }

    public V get(K key){
        //return Null if not hit
        if(!map.containsKey(key)) return null;
        //move the node to the head
        Node<K,V> node = map.get(key);
        list.moveToTop(node);
        return node.value;
    }   

    public void remove(K key){
        if(map.containsKey(key)){
            Node<K,V> node = map.get(key);
            //if A - node - A
            if(node.previous!=null && node.next!=null){
                node.previous.next = node.next;
                node.next.previous = node.previous;
            }
            //if A - node
            else if(node.previous !=null && node.next == null){
                node.previous.next = null;
                list.tail = node.previous;
            }
            //if node - A
            else if(node.previous==null && node.next!=null){
                node.next.previous = null;
                list.head = node.next;
            }
            //if node
            else if(node.previous==null && node.next==null){
                list.head = null;
                list.tail = null;
            }
            map.remove(key);
        }
    }

    public void print(){
        Node<K,V> node = list.head;
        while(node!=null){
            System.out.print(node.key + " ");
            node = node.next;
        }
        System.out.println(" : size = " + list.size);
    }

    public long size(){
        return list.size;
    }
}
