package IRUtilities;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import jdbm.helper.FastIterator;

public class DocVector {
    //hashmap, key is dimension, value is component
    public HashMap<Long,Double> v;

    public DocVector(){
        v = new HashMap<Long,Double>();
    }

    public DocVector(HashMap<Long,Double> v){
        this.v = v;
    }

    public DocVector(HTable<Long,Double> htable) throws IOException {
        v = new HashMap<Long,Double>();
        FastIterator iter = htable.keys();
        Long key;
        while((key=(Long)iter.next())!=null){
            v.put(key, (double)htable.get(key));
        }
    }

    public void addDimension(long dimension, double component){
        v.put(dimension, component);
    }

    public DocVector clone() {
        DocVector result = new DocVector();
        for (Long dimension : v.keySet()) {
            result.addDimension(dimension, v.get(dimension));
        }
        return result;
    }

    public void add(DocVector b) {
        for (Long dimension : b.v.keySet()) {
            if (v.containsKey(dimension)) {
                v.put(dimension, v.get(dimension) + b.v.get(dimension));
            } else {
                v.put(dimension, b.v.get(dimension));
            }
        }
    }

    public static DocVector add(DocVector a, DocVector b) {
        DocVector result = a.clone();
        result.add(b);
        return result;
    }

    public static double dot(DocVector a, DocVector b) {
        double result = 0;
        for (Long dimension : a.v.keySet()) {
            if (b.v.containsKey(dimension)) {
                result += a.v.get(dimension) * b.v.get(dimension);
            }
        }
        return result;
    }

    public double norm() {
        double result = 0;
        for (Long dimension : v.keySet()) {
            result += v.get(dimension) * v.get(dimension);
        }
        return Math.sqrt(result);
    }

    public double cosineSimilarity(DocVector b){
        return dot(this,b)/(norm()*b.norm());
    }

    public LinkedList<Entry> linearize() {
        LinkedList<Entry> result = new LinkedList<Entry>();
        for (Long dimension : v.keySet()) {
            result.add(new Entry(dimension, v.get(dimension)));
        }
        return result;
    }

    public void multiplyScalar(double m) {
        for (Long dimension : v.keySet()) {
            double component = v.get(dimension);
            v.put(dimension, component*m);
        }
    }

    public void put(long dimension, double component) {
        v.put(dimension, component);
    }

    public int size() {
        return v.size();
    }

    public HashMap<Long,Double> getHashMap(){
        return clone().v;
    }
}