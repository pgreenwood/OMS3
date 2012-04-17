/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms3.io;

import oms3.Conversions;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.Date;

/**
 *
 * @author Olaf David
 */
public class TimeStepTest {

   boolean debugTest = false;    // if true, prints out data vesus golden values for comparison
     
     
    private static int Jan=0;
    private static int Feb=1;
    private static int Mar=2;
    private static int Apr=3;
    private static int May=4;
    private static int Jun=5;
    private static int Jul=6;
    private static int Aug=7;
    private static int Sep=8;
    private static int Oct=9;
    private static int Nov=10;
    private static int Dec=11;
    
    File r, r2, sdr;
   

    private void printDebug(String str) {
        if (debugTest) {
            System.out.println(str);
        }
    }

    @Before
    public void init() throws FileNotFoundException {
        r = new File(this.getClass().getResource("timestep_test.csv").getFile());
        r2 = new File(this.getClass().getResource("yampa_data.csv").getFile());
        sdr = new File(this.getClass().getResource("timestep_subdivide.csv").getFile());
    }

    @After
    public void done() throws IOException {
    }

    @Test
    public void testTimeStep_DAILY() throws Exception {
        printDebug("----------------------------");
        printDebug("DAILY: " + this.toString());
        printDebug("----------------------------");
        CSTable t = DataIO.table(r, "obs");
        Assert.assertNotNull(t);

        // 1 YEAR
        Date start = Conversions.convert("1981-1-01", Date.class);
        Date end = Conversions.convert("1981-1-14", Date.class);


        printDebug("Start = " + start.toString() + "; End = " + end.toString());
        double[] obsval = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.DAILY);
        printDebug("# values = " + obsval.length);
        Assert.assertTrue(obsval.length == 14);

        // Values directly from table
        double[] goldenVal = new double[14];
        goldenVal[0] = 72;
        goldenVal[1] = 70;
        goldenVal[2] = 83;
        goldenVal[3] = 80;
        goldenVal[4] = 75;
        goldenVal[5] = 65;
        goldenVal[6] = 64;
        goldenVal[7] = 64;
        goldenVal[8] = 65;
        goldenVal[9] = 66;
        goldenVal[10] = 66;
        goldenVal[11] = 68;
        goldenVal[12] = 71;
        goldenVal[13] = 73;
        //  goldenVal[14] = 75;

        for (int i = 0; i < obsval.length; i++) {
            printDebug("obs[" + i + "] = " + obsval[i] + ";\tGolden = " + goldenVal[i]);
        }

        Assert.assertArrayEquals(goldenVal, obsval, 0);
    }

    @Test
      public void testTimeStep_DAILY2_period() throws Exception {
        printDebug("----------------------------");
        printDebug("DAILY with Period Range: " + this.toString());
        printDebug("----------------------------");
        CSTable t = DataIO.table(r, "obs");
        Assert.assertNotNull(t);

        // 1 YEAR
        Date start = Conversions.convert("1981-1-01", Date.class);
        Date end = Conversions.convert("1981-2-14", Date.class);


        printDebug("Start = " + start.toString() + "; End = " + end.toString());
        
        int periodStart=May;
        boolean[] periodMask = new boolean[12];
        periodMask[Jan] = false;
        periodMask[Feb] = true;
        periodMask[Mar] = true;
        periodMask[Apr] = false;
        periodMask[May] = false;
        periodMask[Jun] = false;
        periodMask[Jul] = true;
        periodMask[Aug] = true;
        periodMask[Sep] = false;
        periodMask[Oct] = true;
        periodMask[Nov] = true;
        periodMask[Dec] = true;
        
        double[] obsval = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.DAILY, periodStart, periodMask, null);
        printDebug("# values = " + obsval.length);
        Assert.assertTrue(obsval.length == 14);

        // Values directly from table
        double[] goldenVal = new double[14];
        
        
        goldenVal[0] = 83;
        goldenVal[1] = 84;
        goldenVal[2] = 85;
        goldenVal[3] = 86;
        goldenVal[4] = 86;
        goldenVal[5] = 87;
        goldenVal[6] = 88;
        goldenVal[7] = 96;
        goldenVal[8] = 98;
        goldenVal[9] = 91;
        goldenVal[10] = 97;
        goldenVal[11] = 98;
        goldenVal[12] = 107;
        goldenVal[13] = 308;
        //  goldenVal[14] = 75;

        for (int i = 0; i < obsval.length; i++) {
            printDebug("obs[" + i + "] = " + obsval[i] + ";\tGolden = " + goldenVal[i]);
        }

        Assert.assertArrayEquals(goldenVal, obsval, 0);
    }
       @Test
      public void testTimeStep_DAILY3_subdivide() throws Exception {
        printDebug("----------------------------");
        printDebug("DAILY with Subdivide: " + this.toString());
        printDebug("----------------------------");
      
        // Data
        CSTable t = DataIO.table(r, "obs");
        Assert.assertNotNull(t);
        
        // Subdivide data
        CSTable sdt = DataIO.table(sdr,"sd");
        Assert.assertNotNull(sdt);
        
        // 1 YEAR
        Date start = Conversions.convert("1980-10-01", Date.class);
        Date end = Conversions.convert("1981-11-30", Date.class);


        printDebug("Start = " + start.toString() + "; End = " + end.toString());
        
        int periodStart=Jan;
        boolean[] periodMask = null; 
        
        double[] subDivideData = DataIO.getColumnDoubleValuesInterval(start, end, sdt, "sd_data", DataIO.DAILY, 0, null, null);
        boolean[] subDivideMask = new boolean[subDivideData.length];
        for (int i=0; i<subDivideMask.length; i++) {
            subDivideMask[i] = (subDivideData[i] == 2);
            //System.out.println("SDmask["+i+"] = " + subDivideMask[i] + " with data " + subDivideData[i]);
        }
        
        
        double[] obsval = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.DAILY, periodStart, periodMask, subDivideMask);
        printDebug("# values = " + obsval.length);
        Assert.assertTrue(obsval.length == 16);

        // Values directly from table
        double[] goldenVal = new double[16];

     
        goldenVal[0] = 105;
        goldenVal[1] = 102;
        goldenVal[2] = 99;
        goldenVal[3] = 97;
        goldenVal[4] = 96;
        goldenVal[5] = 93;
        goldenVal[6] = 94;
        goldenVal[7] = 92;
        goldenVal[8] = 91;
        goldenVal[9] = 90;
        goldenVal[10] = 93;
        goldenVal[11] = 91;
        goldenVal[12] = 89;
        goldenVal[13] = 86;
        goldenVal[14] = 89;
        goldenVal[15] = 78;
        
        for (int i = 0; i < obsval.length; i++) {
            printDebug("obs[" + i + "] = " + obsval[i] + ";\tGolden = " + goldenVal[i]);
        }

        Assert.assertArrayEquals(goldenVal, obsval, 0);
    }
      
       
    @Test
    public void testTimeStep_MEAN_MONTHLY_1year() throws Exception {
        printDebug("----------------------------");
        printDebug("MEAN_MONTHLY 1 year: " + this.toString());
        printDebug("----------------------------");
        CSTable t = DataIO.table(r, "obs");
        Assert.assertNotNull(t);

        // 1 YEAR
        Date start = Conversions.convert("1981-1-01", Date.class);
        Date end = Conversions.convert("1981-12-31", Date.class);


        //System.out.println("Start = " + start.toString() + "; End = " + end.toString());
        double[] obsval = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.MEAN_MONTHLY);
        //System.out.println("# values = " + obsval.length);
        Assert.assertTrue(obsval.length == 12);

        //Values computed directly from .csv file
        double[] goldenVal = new double[12];
        goldenVal[0] = 2430 / 31.0;
        goldenVal[1] = 3897 / 28.0;
        goldenVal[2] = 4587 / 31.0;
        goldenVal[3] = 14840 / 30.0;
        goldenVal[4] = 21287 / 31.0;
        goldenVal[5] = 11096 / 30.0;
        goldenVal[6] = 3048 / 31.0;
        goldenVal[7] = 2187 / 31.0;
        goldenVal[8] = 1345 / 30.0;
        goldenVal[9] = 1893 / 31.0;
        goldenVal[10] = 8457 / 30.0;
        goldenVal[11] = 13534 / 31.0;


        for (int i = 0; i < obsval.length; i++) {
            printDebug("obs[" + i + "] = " + obsval[i] + ";\tGolden = " + goldenVal[i]);
        }

        Assert.assertArrayEquals(goldenVal, obsval, 0);
    }

    @Test
    public void testTimeStep_MEAN_MONTHLY_2year() throws Exception {
        printDebug("----------------------------");
        printDebug("MEAN_MONTHLY 2 year: " + this.toString());
        printDebug("----------------------------");
        CSTable t = DataIO.table(r, "obs");
        Assert.assertNotNull(t);

        // 1 YEAR
        Date start = Conversions.convert("1981-1-01", Date.class);
        Date end = Conversions.convert("1982-12-31", Date.class);

        //System.out.println("Start = " + start.toString() + "; End = " + end.toString());
        double[] obsval = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.MEAN_MONTHLY);
        //System.out.println("# values = " + obsval.length);
        Assert.assertTrue(obsval.length == 12);

        //Values computed directly from .csv file
        double[] goldenVal = new double[12];
        goldenVal[0] = (2430 + 6923) / 62.0;
        goldenVal[1] = (3897 + 23310) / 56.0;
        goldenVal[2] = (4587 + 12150) / 62.0;
        goldenVal[3] = (14840 + 33811) / 60.0;
        goldenVal[4] = (21287 + 57220) / 62.0;
        goldenVal[5] = (11096 + 49120) / 60.0;
        goldenVal[6] = (3048 + 24614) / 62.0;
        goldenVal[7] = (2187 + 8056) / 62.0;
        goldenVal[8] = (1345 + 6034) / 60.0;
        goldenVal[9] = (1893 + 10177) / 62.0;
        goldenVal[10] = (8457 + 9909) / 60.0;
        goldenVal[11] = (13534 + 9158) / 62.0;

        for (int i = 0; i < obsval.length; i++) {
            printDebug("obs[" + i + "] = " + obsval[i] + ";\tGolden = " + goldenVal[i]);
        }
        Assert.assertArrayEquals(goldenVal, obsval, 0);
    }

    @Test
    public void testTimeStep_PERIOD() throws Exception {
        printDebug("----------------------------");
        printDebug("PERIOD: " + this.toString());
        printDebug("----------------------------");
        
        CSTable t = DataIO.table(r, "obs");
        Assert.assertNotNull(t);

        
        // 4 YEARS
        Date start = Conversions.convert("1981-4-13", Date.class);
        Date end = Conversions.convert("1984-7-12", Date.class);


        //System.out.println("Start = " + start.toString() + "; End = " + end.toString());
        double[] mean = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.PERIOD_MEAN);
        double[] min = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.PERIOD_MIN);
        double[] max = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.PERIOD_MAX);
        double[] median = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.PERIOD_MEDIAN);
        double[] stdev = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.PERIOD_STANDARD_DEVIATION);

        //System.out.println("# values = " + obsval.length);
        Assert.assertTrue(mean.length == 4);
        Assert.assertTrue(min.length == 4);
        Assert.assertTrue(max.length == 4);
        Assert.assertTrue(median.length == 4);
        Assert.assertTrue(stdev.length == 4);

        double[] goldenMean = new double[4];
        double[] goldenMin = new double[4];
        double[] goldenMax = new double[4];
        double[] goldenMedian = new double[4];
        double[] goldenStdev = new double[4];
     
            
        goldenMean[0] = 283.90494296577947;
        goldenMin[0] = 31;
        goldenMax[0] = 2890;
        goldenMedian[0] = 138;
        goldenStdev[0] = 325.54260421929246;

        goldenMean[1] = 686.2520547945205;
        goldenMin[1] = 131;
        goldenMax[1] = 4740;
        goldenMedian[1] = 322;
        goldenStdev[1] = 692.1054096828143;
        
        goldenMean[2] = 876.9123287671233;
        goldenMin[2] = 140;
        goldenMax[2] = 6390;
        goldenMedian[2] = 446;
        goldenStdev[2] = 1080.6484826245241;
        
        goldenMean[3] = 679.6907216494845;
        goldenMin[3] = 208;
        goldenMax[3] = 2310;
        goldenMedian[3] = 482.5;
        goldenStdev[3] = 506.30640168591486;
        
        for (int i=0; i<mean.length; i++) {
            printDebug("Year " + i + ":");
            printDebug("Mean["+i+"] = " + mean[i] + ";  \tGolden = " + goldenMean[i]);
            printDebug("Min["+i+"] = " + min[i] + ";  \tGolden = " + goldenMin[i]);
            printDebug("Max["+i+"] = " + max[i] + ";  \tGolden = " + goldenMax[i]);
            printDebug("Median["+i+"] = " + median[i] + ";  \tGolden = " + goldenMedian[i]);
            printDebug("StDev["+i+"] = " + stdev[i] + ";  \tGolden = " + goldenStdev[i] + "; Delta = " + (goldenStdev[i] - stdev[i]));
            printDebug("");
        
            Assert.assertTrue(mean[i] == goldenMean[i]);
            Assert.assertTrue(min[i] == goldenMin[i]);
            Assert.assertTrue(max[i] == goldenMax[i]);
            Assert.assertTrue(median[i] == goldenMedian[i]);
            double allowedDelta_stdev = 2.28E-13;
            Assert.assertEquals(stdev[i], goldenStdev[i], allowedDelta_stdev);
        }
    }

     @Test
    public void testTimeStep_PERIOD2() throws Exception {
        printDebug("----------------------------");
        printDebug("PERIOD2: " + this.toString());
        printDebug("----------------------------");
        
        CSTable t = DataIO.table(r, "obs");
        Assert.assertNotNull(t);

        
        // 4 YEARS
        Date start = Conversions.convert("1981-11-15", Date.class);
        Date end = Conversions.convert("1984-7-20", Date.class);


        //System.out.println("Start = " + start.toString() + "; End = " + end.toString());
        int periodStart=May;
        boolean[] periodMask = new boolean[12];
        periodMask[Jan] = true;
        periodMask[Feb] = true;
        periodMask[Mar] = true;
        periodMask[Apr] = false;
        periodMask[May] = false;
        periodMask[Jun] = false;
        periodMask[Jul] = true;
        periodMask[Aug] = true;
        periodMask[Sep] = false;
        periodMask[Oct] = true;
        periodMask[Nov] = true;
        periodMask[Dec] = true;
        
        double[] mean = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.PERIOD_MEAN, periodStart, periodMask, null);
        double[] min = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.PERIOD_MIN, periodStart, periodMask, null);
        double[] max = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.PERIOD_MAX, periodStart, periodMask, null);
        double[] median = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.PERIOD_MEDIAN, periodStart, periodMask, null);
        double[] stdev = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.PERIOD_STANDARD_DEVIATION, periodStart, periodMask, null);

        //System.out.println("# values = " + obsval.length);
        Assert.assertTrue(mean.length == 4);
        Assert.assertTrue(min.length == 4);
        Assert.assertTrue(max.length == 4);
        Assert.assertTrue(median.length == 4);
        Assert.assertTrue(stdev.length == 4);

        double[] goldenMean = new double[4];
        double[] goldenMin = new double[4];
        double[] goldenMax = new double[4];
        double[] goldenMedian = new double[4];
        double[] goldenStdev = new double[4];
     
 


        goldenMean[0] =451.3284671532847;
        goldenMin[0] = 158;
        goldenMax[0] = 4740;
        goldenMedian[0] = 289;
        goldenStdev[0] = 551.5395587;

        goldenMean[1] = 397.5245901639344;
        goldenMin[1] = 131;
        goldenMax[1] = 1630;
        goldenMedian[1] = 316.5;
        goldenStdev[1] = 249.8383646;
        
        goldenMean[2] = 522.2367346938776;
        goldenMin[2] = 140;
        goldenMax[2] = 2800;
        goldenMedian[2] = 355;
        goldenStdev[2] = 473.2054318;
        
        
        goldenMean[3] = 464.4;
        goldenMin[3] = 323;
        goldenMax[3] = 667;
        goldenMedian[3] = 405.5;
        goldenStdev[3] = 123.6561361;
        
        
        for (int i=0; i<mean.length; i++) {
            printDebug("Year " + i + ":");
            printDebug("Mean["+i+"] = " + mean[i] + ";  \tGolden = " + goldenMean[i]);
            printDebug("Min["+i+"] = " + min[i] + ";  \tGolden = " + goldenMin[i]);
            printDebug("Max["+i+"] = " + max[i] + ";  \tGolden = " + goldenMax[i]);
            printDebug("Median["+i+"] = " + median[i] + ";  \tGolden = " + goldenMedian[i]);
            printDebug("StDev["+i+"] = " + stdev[i] + ";  \tGolden = " + goldenStdev[i] + "; Delta = " + (goldenStdev[i] - stdev[i]));
            printDebug("");
        
            Assert.assertTrue(mean[i] == goldenMean[i]);
            Assert.assertTrue(min[i] == goldenMin[i]);
            Assert.assertTrue(max[i] == goldenMax[i]);
            Assert.assertTrue(median[i] == goldenMedian[i]);
            double allowedDelta_stdev = 2.28E-7;
            Assert.assertEquals(stdev[i], goldenStdev[i], allowedDelta_stdev);
        }
    }

     
    @Test
    public void testTimeStep_ANNUAL_MEAN() throws Exception {
        printDebug("----------------------------");
        printDebug("Annual Mean: " + this.toString());
        printDebug("----------------------------");
        CSTable t = DataIO.table(r, "obs");
        Assert.assertNotNull(t);

        // 4 YEARS
        Date start = Conversions.convert("1981-10-13", Date.class);
        Date end = Conversions.convert("1985-7-25", Date.class);


        //System.out.println("Start = " + start.toString() + "; End = " + end.toString());
        double[] mean = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.ANNUAL_MEAN);

        double[] goldenMean = new double[5];
        goldenMean[0] = 23184 / 80.0; // for 1981
        goldenMean[1] = 250482 / 365.0; // 1982
        goldenMean[2] = 320073 / 365.0; // 1983
        goldenMean[3] = 158468 / 366.0; // 1984 
        goldenMean[4] = 77621 / 206.0; // 1985 
        for (int i = 0; i < mean.length; i++) {
            printDebug("AnnualMean[" + i + "] = " + mean[i] + ";\tGolden AnnualMean[" + i + "] = " + goldenMean[i]);

        }

        Assert.assertArrayEquals(goldenMean, mean, 0);
    }

     @Test
    public void testTimeStep_ANNUAL_MEAN_OCT_TO_SEP() throws Exception {
        printDebug("----------------------------");
        printDebug("Annual Mean with Period Oct to Sep: " + this.toString());
        printDebug("----------------------------");
        CSTable t = DataIO.table(r, "obs");
        Assert.assertNotNull(t);

        // 4 YEARS
        Date start = Conversions.convert("1980-10-1", Date.class);
        Date end = Conversions.convert("1985-9-30", Date.class);


        //System.out.println("Start = " + start.toString() + "; End = " + end.toString());
        // 9,8 used for oct-sep since base-0
        boolean[] pmask = {true,true, true, true, true, true, true, true, true, true, true, true};
        double[] mean = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.ANNUAL_MEAN, Oct,pmask, null);

        double[] goldenMean = new double[5];
   
        goldenMean[0] = 198.42465753424656; // for 1981
        goldenMean[1] = 671.5671232876713; // 1982
        goldenMean[2] = 856.7232876712329; // 1983
        goldenMean[3] = 498.37704918032784; // 1984 
        goldenMean[4] = 265.81369863013697; // 1985 
        for (int i = 0; i < mean.length; i++) {
            printDebug("AnnualMean_Period[" + i + "] = " + mean[i] + ";\tGolden AnnualMean[" + i + "] = " + goldenMean[i]);

        }

        Assert.assertArrayEquals(goldenMean, mean, 0);
    }
     
    @Test
    public void testTimeStep_ANNUAL_MEAN_YAMPA_OCT() throws Exception {
        printDebug("----------------------------");
        printDebug("Annual Mean with Period Yampa starting in Oct: " + this.toString());
        printDebug("----------------------------");
        CSTable t = DataIO.table(r2, "obs");
        Assert.assertNotNull(t);

        Date start = Conversions.convert("1996-10-1", Date.class);
        Date end = Conversions.convert("2000-09-30", Date.class);


        //System.out.println("Start = " + start.toString() + "; End = " + end.toString());
        // 9,8 used for oct-sep since base-0
        boolean[] pmask = {false, 
                           true, true, true, true, true, true, 
                           false, false, false, false, false};
        double[] mean = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff", DataIO.ANNUAL_MEAN, Oct ,pmask, null);

        double[] goldenMean = new double[4];
   
        goldenMean[0] = 1220.0; 
        goldenMean[1] = 857.1767955801105;
        goldenMean[2] = 739.4751381215469;
        goldenMean[3] = 744.6868131868132;
      
        for (int i = 0; i < mean.length; i++) {
            printDebug("AnnualMean_Period[" + i + "] = " + mean[i] + ";\tGolden AnnualMean[" + i + "] = " + goldenMean[i]);

        }

        Assert.assertArrayEquals(goldenMean, mean, 0);
    }
      
      
     @Test
    public void testTimeStep_ANNUAL_MEAN_YAMPA_Feb() throws Exception {
        printDebug("----------------------------");
        printDebug("Annual Mean with Period Yampa startig in Feb: " + this.toString());
        printDebug("----------------------------");
        CSTable t = DataIO.table(r2, "obs");
        Assert.assertNotNull(t);

        Date start = Conversions.convert("1996-10-1", Date.class);
        Date end = Conversions.convert("2000-09-30", Date.class);


        //System.out.println("Start = " + start.toString() + "; End = " + end.toString());
        // 9,8 used for oct-sep since base-0
        boolean[] pmask = {false, 
                           true, true, true, true, true, true, 
                           false, false, false, false, false};
        double[] mean = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff", DataIO.ANNUAL_MEAN, Feb ,pmask, null);

        double[] goldenMean = new double[4];
   
        goldenMean[0] = 1220.0; 
        goldenMean[1] = 857.1767955801105;
        goldenMean[2] = 739.4751381215469;
        goldenMean[3] = 744.6868131868132;
     
        for (int i = 0; i < mean.length; i++) {
            printDebug("AnnualMean_Period[" + i + "] = " + mean[i] + ";\tGolden AnnualMean[" + i + "] = " + goldenMean[i]);

        }

        Assert.assertArrayEquals(goldenMean, mean, 0);
    }
      
     
    @Test
    public void testTimeStep_MONTHLY_MEAN() throws Exception {
        printDebug("----------------------------");
        printDebug("Monthly Mean: " + this.toString());
        printDebug("----------------------------");
        CSTable t = DataIO.table(r, "obs");
        Assert.assertNotNull(t);

        // 4 YEARS
        Date start = Conversions.convert("1983-10-13", Date.class);
        Date end = Conversions.convert("1984-2-25", Date.class);

        //System.out.println("Start = " + start.toString() + "; End = " + end.toString());
        double[] mean = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.MONTHLY_MEAN);

        double[] goldenMean = new double[5];
        goldenMean[0] = 3034 / 19.0; // for 10-1983
        goldenMean[1] = 15156 / 30.0; // 11-1983
        goldenMean[2] = 15773 / 31.0; // 12-1983
        goldenMean[3] = 10596 / 31.0; // 1-1984
        goldenMean[4] = 5993 / 25.0; // 2-1984
        for (int i = 0; i < mean.length; i++) {
            printDebug("MonthlyMean[" + i + "] = " + mean[i] + ";\tGolden MonthlyMean[" + i + "] = " + goldenMean[i]);

        }

        Assert.assertArrayEquals(goldenMean, mean, 0);
    }
    @Test
    public void testTimeStep_MONTHLY_MEAN_subdivide() throws Exception {
        printDebug("----------------------------");
        printDebug("Monthly Mean with subdivide: " + this.toString());
        printDebug("----------------------------");
        // Data
        CSTable t = DataIO.table(r, "obs");
        Assert.assertNotNull(t);
        // Subdivide
        CSTable sdt = DataIO.table(sdr,"sd");
        Assert.assertNotNull(sdt);
        
        
        // 4 YEARS
        Date start = Conversions.convert("1980-10-01", Date.class);
        Date end = Conversions.convert("1984-11-30", Date.class);

         
        int periodStart=Jan;
        boolean[] periodMask = {true, true, true, true,
                                true, true, true, true,
                                true, true, true, false};
       
        
        double[] subDivideData = DataIO.getColumnDoubleValuesInterval(start, end, sdt, "sd_data", DataIO.DAILY, 0, null, null);
        boolean[] subDivideMask = new boolean[subDivideData.length];
        for (int i=0; i<subDivideMask.length; i++) {
            subDivideMask[i] = (subDivideData[i] == 2);
            //System.out.println("SDmask["+i+"] = " + subDivideMask[i] + " with data " + subDivideData[i]);
        }
        
        
        double[] mean = DataIO.getColumnDoubleValuesInterval(start, end, t, "runoff[0]", DataIO.MONTHLY_MEAN, periodStart, periodMask, subDivideMask);
        //Assert.assertTrue(mean.length == 1);
       
       

        double[] goldenMean = new double[2];
        double goldenSum = 0;
        goldenSum += 105;
        goldenSum += 102;
        goldenSum += 99;
        goldenSum += 97;
        goldenSum += 96;
        goldenSum += 93;
        goldenSum += 94;
        goldenSum += 92;
        goldenSum += 91;
        goldenSum += 90;
        goldenSum += 93;
        goldenSum += 91;
        goldenSum += 89;
        goldenSum += 86;
        goldenSum += 89;
//        goldenSum += 78;
       
        goldenMean[0] = goldenSum/15;
        goldenMean[1] = 78;
        printDebug("Read data length = " + mean.length + ";  goldenMean length = " + goldenMean.length);
        for (int i = 0; i < goldenMean.length; i++) {
            printDebug("MonthlyMean[" + i + "] = " + mean[i] + ";\tGolden MonthlyMean[" + i + "] = " + goldenMean[i]);

        }

        Assert.assertArrayEquals(goldenMean, mean, 0);
    }
}
