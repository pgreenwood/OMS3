/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms3.dsl.cosu;


import java.util.ArrayList;
import java.util.List;

/** Objective function handling. 
 *
 * @author od kmo
 */
public class parseRange {


    public List<Integer> parse(String range, int max) {
        List<Integer> idx = new ArrayList<Integer>();
        String[] n = range.split(",");
        for (String s : n) {
            String[] d = s.split("-");
            int mi = Integer.parseInt(d[0]);
            if (mi < 0 || mi >= max) {
                throw new IllegalArgumentException(range);
            }
            if (d.length == 2) {
                if (d[1].equals("*")) {
                    d[1] = Integer.toString(max - 1);
                }
                int ma = Integer.parseInt(d[1]);
                if (ma <= mi || ma >= max || ma < 0) {
                    throw new IllegalArgumentException(range);
                }
                for (int i = mi; i <= ma; i++) {
                    idx.add(i);
                }
            } else {
                idx.add(mi);
            }
        }
        return idx;
    }
   
    
    public boolean[] getArray(String str, int length) {
        boolean[] mask = new boolean[length];
        
        List<Integer> idx = new ArrayList<Integer>();
        if (length > 1) {
            idx = parse(str, length);
        } else // parseRange not happy with "0-*" for length of 1.
        {
            idx.add(0);
        }

        for (int i = 0; i < length; i++) { // match all rows in the range provided
           mask[i] = idx.contains(i);
        }
        return mask;
    }
    
    
    
    public List<Integer> StringToList(String range) {     
        List<Integer> idx = new ArrayList<Integer>();
        String s0 = range.replace(" ","");
        String s1 = s0.replace("{","");
        String s2 = s1.replace("}","");
        String[] s3 = s2.split(",");
        for (String s : s3) {
         int mi = Integer.parseInt(s);
         idx.add(mi);
        }
        return idx;
    }
   
    
    public int[] StringToIntArray(String range) {
        List<Integer> lst = StringToList(range);
        int[] arr = new int[lst.size()];
        for (int i=0; i< arr.length; i++) {
            arr[i] = lst.get(i);
        }
        return arr;
    }

}  
  