/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms3.dsl.cosu;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import ngmf.util.cosu.luca.of.ABSDIF;
import ngmf.util.cosu.luca.of.ABSDIFLOG;
import ngmf.util.cosu.luca.of.AVE;
import ngmf.util.cosu.luca.of.BIAS;
import ngmf.util.cosu.luca.of.IOA;
import ngmf.util.cosu.luca.of.IOA2;
import ngmf.util.cosu.luca.of.NS;
import ngmf.util.cosu.luca.of.NS2LOG;
import ngmf.util.cosu.luca.of.NSLOG;
import ngmf.util.cosu.luca.of.PMCC;
import ngmf.util.cosu.luca.of.RMSE;
import ngmf.util.cosu.luca.of.TRMSE;
import oms3.ObjectiveFunction;
import oms3.SimConst;
import oms3.dsl.Buildable;
import oms3.io.CSTable;
import oms3.io.DataIO;

/**
 * Objective function handling.
 *
 * @author od
 */
public class ObjFunc implements Buildable {

    static final List<String> months = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
    static final boolean showCacheStats = false;
    static final int checkCachePercentage = 5;
    static int usedCache = 0;
    static int readData = 0;
    //
    double weight = Double.NaN;
    String timestep = SimConst.DAILY;
    ObjectiveFunction of;
    //
    CSVColumn sim;
    CSVColumn obs;
    CSVColumn sd = null;
    //
    String method;
    private boolean enableCache = false; // default.
    private boolean cache_valid = false;
    private double[] cache = new double[1];
    boolean[] periodMask = null;
    private int subDivideValue = Integer.MIN_VALUE;
    boolean[] subDivideMask = null;
    double invalidData = -90.0;

    public CSVColumn getSimulated() {
        if (sim == null) {
            throw new IllegalArgumentException("Missing 'sim' argument");
        }
        return sim;
    }

    public CSVColumn getObserved() {
        if (obs == null) {
            throw new IllegalArgumentException("Missing 'obs' argument");
        }
        return obs;
    }

    @Override
    public Buildable create(Object name, Object value) {
        if (name.equals("sim")) {
            return sim = new CSVColumn();
        } else if (name.equals("obs")) {
            return obs = new CSVColumn();
        } else if (name.equals("subdivide")) {
            return sd = new CSVColumn();
        }
        throw new IllegalArgumentException(name.toString());
    }

    public void setMethod(String method) {
        this.method = method;
        if (method.equals(SimConst.ABSDIF)) {
            of = new ABSDIF();
        } else if (method.equals(SimConst.ABSDIFLOG)) {
            of = new ABSDIFLOG();
        } else if (method.equals(SimConst.AVE)) {
            of = new AVE();
        } else if (method.equals(SimConst.IOA)) {
            of = new IOA();
        } else if (method.equals(SimConst.IOA2)) {
            of = new IOA2();
        } else if (method.equals(SimConst.NS)) {
            of = new NS();
        } else if (method.equals(SimConst.NSLOG)) {
            of = new NSLOG();
        } else if (method.equals(SimConst.NS2LOG)) {
            of = new NS2LOG();
        } else if (method.equals(SimConst.NBIAS)) {
            of = new BIAS();
        } else if (method.equals(SimConst.PMCC)) {
            of = new PMCC();
        } else if (method.equals(SimConst.RMSE)) {
            of = new RMSE();
        } else if (method.equals(SimConst.TRMSE)) {
            of = new TRMSE();
        } else {
            try {
                // load this as a class name from default classpath
                Class<?> c = Class.forName(method);
                of = (ObjectiveFunction) c.newInstance();
            } catch (Exception E) {
                throw new IllegalArgumentException("No such method: " + method);
            }
        }
    }

    public String getMethod() {
        return method;
    }

    public void setTimestep(String timestep) {
        if ((!timestep.equals(SimConst.DAILY))
                && (!timestep.equals(SimConst.DAILY_MEAN))
                && (!timestep.equals(SimConst.MONTHLY_MEAN))
                && (!timestep.equals(SimConst.MEAN_MONTHLY))
                && (!timestep.equals(SimConst.ANNUAL_MEAN))
                && (!timestep.equals(SimConst.PERIOD_MAXIMUM))
                && (!timestep.equals(SimConst.PERIOD_MININUM))
                && (!timestep.equals(SimConst.PERIOD_MEDIAN))
                && (!timestep.equals(SimConst.PERIOD_STANDARD_DEVIATION))) {
            throw new IllegalArgumentException("SetTimeStep:  Illegal timestep: " + timestep);
        }
        this.timestep = timestep;

    }

    public void setInvalidDataValue(String invalidDataValue) {
        this.invalidData = Double.parseDouble(invalidDataValue);
        // System.out.println("Using " + this.invalidData + "as invalid data value.");
    }

    public void setSubdivide_value(String subdivide_value) {
        this.subDivideValue = Integer.parseInt(subdivide_value);
    }

    public int getSubDivideValue() {
        return this.subDivideValue;
    }

    public void setSubDivideMask(boolean[] in) {
        this.subDivideMask = in;
    }

    public String getSubDivideString() {
        if (sd == null) {
            return null;
        } else {
            return "File: " + sd.getFile() + ",  Table: " + sd.getTable() + ", Column: " + sd.getColumn();
        }
    }

    public boolean[] getSubDivideMask(CSVColumn sdi, Date start, Date end, File folder) {
        if (sdi != null) {
            try {
                File fname = ObjFunc.resolve(sdi.getFile(), folder);
                CSTable sdt = DataIO.table(fname, sdi.getTable());
                String column = sdi.getColumn();

                double[] retData = DataIO.getColumnDoubleValuesInterval(start, end, sdt, column, DataIO.DAILY, 0, null, null);
                int[] subDivideData = new int[retData.length];
                for (int i = 0; i < retData.length; i++) {
                    subDivideData[i] = (int) retData[i];
                }

                if ((subDivideMask == null) && (subDivideData != null)) {
                    this.subDivideMask = new boolean[subDivideData.length];
                    for (int i = 0; i < subDivideData.length; i++) {
                        this.subDivideMask[i] = ((subDivideData[i] == this.getSubDivideValue()) || (this.getSubDivideValue() == Integer.MIN_VALUE));
                    }
                }
                return this.subDivideMask;
            } catch (IOException E) {
                throw new RuntimeException(E);
            }
        } else {
            return null;
        }
    }

    public void setEnableCache(String enableCache) {
        if (enableCache.equals("true") || enableCache.equals("t") || enableCache.equals("1")) {
            this.enableCache = true;
        } else {
            this.enableCache = false;
        }
        if (showCacheStats) {
            String cstr = this.enableCache ? "enabled" : "disabled";
            System.out.println("Cache is " + cstr + " for ObjFunc with method = " + this.getMethod());
        }
    }

    public void setPeriodRange(String periodRange) {
        System.out.println("*****Setting Period Range to " + periodRange);
        parseRange pr = new parseRange();
        boolean[] range_tmp = pr.getArray(periodRange, 13);
        this.periodMask = new boolean[12];
        for (int i = 0; i < 12; i++) {
            this.periodMask[i] = range_tmp[i + 1];  // convert from 1-12 to 0-13.
        }
    }

    public boolean[] getPeriodMask() {
        return this.periodMask;
    }

    public String getPeriodStartName(int start) {
        return months.get(start);
    }

    public String getPeriodEndName(int start) {
        int periodEnd = (start + 11) % 12;
        return months.get(periodEnd);
    }

    public void setWeight(double weight) {
        if (weight <= 0 || weight > 1) {
            throw new IllegalArgumentException("of weight out of range: " + weight);
        }
        this.weight = weight;
    }

    String getTimestep() {
        return timestep;
    }

    double getWeight() {
        return weight;
    }

    ObjectiveFunction getOF() {
        if (of == null) {
            throw new IllegalArgumentException("No Objective function method defined.");
        }
        return of;
    }

    // static 
    public static boolean isInc(List<ObjFunc> ofs) {
        if (ofs.isEmpty()) {
            throw new IllegalArgumentException("No Objective function(s) defined. ");
        }
        boolean inc = ofs.get(0).getOF().positiveDirection();
        for (ObjFunc of : ofs) {
            if (of.getOF().positiveDirection() != inc) {
                throw new IllegalArgumentException("Objective function(s) optimization direction mismatch!");
            }
        }
        return inc;
    }

    public static void adjustWeights(List<ObjFunc> ofs) {
        int noOf = ofs.size();
        for (ObjFunc of : ofs) {
            if (Double.isNaN(of.getWeight())) {
                of.setWeight((double) 1 / noOf);
            }
        }
    }

    private void setcache(double[] data) {
        this.cache = data;
        this.cache_valid = this.enableCache;
    }

    private double[] getCacheData() {
        return this.cache;
    }

    private Boolean getCacheValid() {
        return this.cache_valid;
    }

    public static void checkCache(double[] cache, double[] read) {
        boolean error = false;

        if (cache.length != read.length) {
            System.out.println("\tERROR: Cache array length difference : cache length = " + cache.length
                    + "\t obsval length = " + read.length);
            error = true;
        }
        for (int i = 0; i < cache.length; i++) {
            if (cache[i] != read[i]) {
                System.out.println("\tERROR: Data " + i + "mismatch: cache = " + cache[i] + "\t computed = " + read[i]);
                error = true;
            }
        }

        if (error == true) {
            throw new RuntimeException("\tERROR: !!!Check on validity of observed cache data check showed an Error!!!");
        }
        error = false;
        if (showCacheStats) {
            System.out.print("\n\tRandom cache data validity check passed.\t");
        }
    }

    public static double[] getObs(ObjFunc of, Date start, Date end, CSTable tobs, String columnName, int timeStep, int startMonth,
            boolean[] periodMask, boolean[] sdMask) {
        double[] obsval;
        // System.out.println("CacheValid = " + of.getCacheValid());
        if (of.getCacheValid()) {
            obsval = of.getCacheData();
            if (showCacheStats) {
                usedCache++;
            }

            // Randomally check data
            Random r = new Random();
            if (r.nextInt(100) < checkCachePercentage) {
                //System.out.print(" Verifying cache data validity");
                double[] obsval_orig = DataIO.getColumnDoubleValuesInterval(start, end, tobs, columnName, timeStep, startMonth, periodMask, sdMask);
                checkCache(obsval, obsval_orig);
            }
        } else {
            obsval = DataIO.getColumnDoubleValuesInterval(start, end, tobs, columnName, timeStep, startMonth, periodMask, sdMask);
            if (showCacheStats) {
                readData++;
            }
        }

        of.setcache(obsval);
        return obsval;
    }

    public static double calculateObjectiveFunctionValue(List<ObjFunc> ofs, Date start, Date end, File folder) {
        return calculateObjectiveFunctionValue(ofs, 0, start, end, folder);
    }

    public static double calculateObjectiveFunctionValue(List<ObjFunc> ofs, int startMonthOfYear, Date start, Date end, File folder) {
        try {
            if (ofs.isEmpty()) {
                throw new IllegalArgumentException("No Objective function(s) defined. ");
            }
            double val = 0.0;
            double weight = 0.0;
            adjustWeights(ofs);

            for (ObjFunc of : ofs) {
                CSVColumn obs = of.getObserved();
                String timeStepString = of.getTimestep();
                int timeStep = DataIO.DAILY;
                if (timeStepString.equals(SimConst.DAILY)) {
                    timeStep = DataIO.DAILY;
                } else if (timeStepString.equals(SimConst.MEAN_MONTHLY)) {
                    timeStep = DataIO.MEAN_MONTHLY;
                } else if (timeStepString.equals(SimConst.MONTHLY_MEAN)) {
                    timeStep = DataIO.MONTHLY_MEAN;
                } else if (timeStepString.equals(SimConst.ANNUAL_MEAN)) {
                    timeStep = DataIO.ANNUAL_MEAN;
                } else if (timeStepString.equals(SimConst.PERIOD_MEAN)) {
                    timeStep = DataIO.PERIOD_MEAN;
                } else if (timeStepString.equals(SimConst.PERIOD_MEDIAN)) {
                    timeStep = DataIO.PERIOD_MEDIAN;
                } else if (timeStepString.equals(SimConst.PERIOD_MININUM)) {
                    timeStep = DataIO.PERIOD_MIN;
                } else if (timeStepString.equals(SimConst.PERIOD_MAXIMUM)) {
                    timeStep = DataIO.PERIOD_MAX;
                } else if (timeStepString.equals(SimConst.PERIOD_STANDARD_DEVIATION)) {
                    timeStep = DataIO.PERIOD_STANDARD_DEVIATION;
                } else {
                    throw new IllegalArgumentException("TimeStep " + timeStepString + "unknown.");
                }

                boolean[] pMask = of.getPeriodMask();
                boolean[] sdMask = of.getSubDivideMask(of.sd, start, end, folder);

                CSTable tobs = DataIO.table(resolve(obs.getFile(), folder), obs.getTable());
                String column = obs.getColumn();
                double[] obsval = getObs(of, start, end, tobs, column, timeStep, startMonthOfYear, pMask, sdMask);

                CSVColumn sim = of.getSimulated();
                CSTable tsim = DataIO.table(resolve(sim.getFile(), folder), sim.getTable());
                double[] simval = DataIO.getColumnDoubleValuesInterval(start, end, tsim, sim.getColumn(), timeStep, startMonthOfYear, pMask, sdMask);

                weight += of.getWeight();
                val += of.getOF().calculate(obsval, simval, of.invalidData) * of.getWeight();
            }

            if (showCacheStats) {
                double cacheUse = ((double) usedCache) / ((double) usedCache + (double) readData) * 100;
                int cacheUseInt = (int) cacheUse;
                System.out.print("\t" + cacheUseInt + "% cache use : Read Data " + readData + " times. Used Cache Data " + usedCache + " times");
            }

            if (weight != 1.0) {
                throw new IllegalArgumentException("sum of of weights != 1.0");
            }
            return val;
        } catch (IOException E) {
            throw new RuntimeException(E);
        }
    }

    public static File resolve(String file, File out) {
        File f = new File(file);
        if (!(f.isAbsolute() && f.exists())) {
            f = new File(out, file);
        }
        if (!f.exists()) {
            throw new IllegalArgumentException("File not found: " + file);
        }
        return f;
    }
}