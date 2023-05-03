package IRUtilities;

import java.io.Serializable;
import java.util.HashMap;

public class DocVecCache implements Serializable {
    public double tfmax; //term frequency in a page can never larger than max word tf in that page
    public HashMap<Long,Double> wordIDToTW = new HashMap<Long,Double>();
    //wordID --> tfxidf/tfmax
}
