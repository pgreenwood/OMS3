/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms3.dsl.cosu;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import ngmf.util.cosu.luca.ParameterData;
import oms3.SimConst;
import oms3.dsl.Buildable;
import oms3.io.CSProperties;
import oms3.io.DataIO;

/**
 *
 * @author od
 */
public class Calibration implements Buildable {

    String strategy = SimConst.MEAN;
    String range = "0-*";
    String file;
    String calibParam = null;
    String table;

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String ConvertStrategy(int calibType) {
        if (calibType == ParameterData.MEAN)
            return SimConst.MEAN;
        else if (calibType == ParameterData.BINARY)
            return SimConst.BINARY;
        else if (calibType == ParameterData.INDIVIDUAL)
            return SimConst.INDIVIDUAL;
        else 
            return "Unknown";
    }
        

    public void setRange(String range) {
        this.range = range;
    }

    public void setFile(String file) {
        this.file = file;
        System.out.println("Calib. File = " + file);
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setparam(String param) {
        this.calibParam = param;
        //System.out.println("Calibration selection based on = " + this.calibParam);
    }

    public String getStrategy() {
        if (strategy == null) {
            throw new RuntimeException("Missing strategy.");
        }
        if (!strategy.equals(SimConst.MEAN) && !strategy.equals(SimConst.INDIVIDUAL) && !strategy.equals(SimConst.BINARY)) {
            throw new RuntimeException("Strategy " + strategy + "unsupported.");
        }
        return strategy;
    }

    public String getRange() {
        return range;
    }

    public int getStrategyAsInt() {
        if (strategy.equals(SimConst.MEAN)) {
            return ParameterData.MEAN;
        } else if (strategy.equals(SimConst.INDIVIDUAL)) {
            return ParameterData.INDIVIDUAL;
        } else if (strategy.equals(SimConst.BINARY)) {
            return ParameterData.BINARY;
        } else {
            throw new IllegalArgumentException("Calibration strategy " + strategy + "not valid.");
        }
    }

    public boolean[] getCalibrateFlags(int length) throws IOException {
        parseRange pr = new parseRange();  
        if (calibParam == null) { // determine calibFlags based on range matching param index           
            boolean[] calibrationFlags = pr.getArray(range, length);  
            return calibrationFlags;  
        }
        else { // determine calibFlags based on range matching specified calibration parameter
            if (file == null) {
                throw new IllegalArgumentException("Must specify calibration file when calibration parameter specified");
            }
          
            CSProperties prop = DataIO.properties(new FileReader(new File(file)), "Parameter");  
            if (prop.containsKey(calibParam) == false) { 
                throw new IllegalArgumentException("Calibration param " + calibParam + " not foundin file " + file);
            }
                 
          
            String values = (String) prop.get(calibParam);         
           // System.out.println("Calibrate Match Data found as = " + calibParam + " = " + values);
            int[] parr = pr.StringToIntArray(values); 
            
           //  Find max calib param value to limit range array size.
            int pmax = Integer.MIN_VALUE;
            for (int i=0; i<parr.length; i++) { 
                if (parr[i] > pmax) pmax = parr[i];
            }
          
            List<Integer> rlist = pr.parse(this.range, pmax+1);
            boolean [] calibrationFlags = new boolean[parr.length];
            for (int i=0; i<parr.length; i++) {
                 calibrationFlags[i] = false;
                 for (Integer r : rlist) {
                   //  System.out.println("parr[ "+i+"], r = " + parr[i] + ", " + r);
                    if (parr[i] == r) {
                       calibrationFlags[i] = true; 
                      // System.out.println("CalFlag["+i+"] = true");
                    }
                 }
            }
           return calibrationFlags;
        }  
    }
    
    public String getFile() {
        if (file == null) {
            throw new RuntimeException("missing file name.");
        }
        return file;
    }

    public String getTable() {
        return table;   // can be null
    }

    public String getcalibParam() {
        if (calibParam == null) {
            throw new RuntimeException("missing calibParam name.");
        }
        return calibParam;
    }

    @Override
    public Buildable create(Object name, Object value) {
        return LEAF;
    }
}
