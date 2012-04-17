package oms3.dsl.cosu;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ngmf.util.cosu.luca.ParameterData;
import oms3.dsl.Param;
import oms3.dsl.Params;
import oms3.dsl.cosu.Step.Data;

public class SummaryWriter {

    File summaryFile;
    String summaryFileName;
    String fpath;
    PrintWriter w = null;
    
    private boolean wroteStepHeader = false;
    private boolean openedFile = false;
    
    ArrayList<Double> iData=  new ArrayList<Double>();
    int inxt = 0;
    
    public SummaryWriter() {  
    }
    
    public void setFile(String name) {
        this.summaryFileName = name;
        checkFile();
    }
  
    
    public void setModelInfo(int rounds, int steps, String modelName, List<Params> params, Date calibStart, Date runStart, Date runEnd, String outName) {
            this.fpath = outName;
            checkFile();
            if (w != null) {
          
       
            w.println("# Rounds = " + rounds);
            w.println("# Steps = " + steps);
            w.println();
            w.println("Input Information");
            w.println("\tModel Executable: " + modelName);  
            
            for (Params p : params) {
                w.println("\tParameter File Name = " + p.getFile());
                List<Param> param = p.getParam();
                for (Param prm : param) {
                    w.println("\t" + prm.getName() + " = " + prm.getValue().toString());
                }
            }
        
            w.println();
            w.format("Model Run Period  : \t%tB %td,%tY - %tB %td,%tY\n", runStart,runStart, runStart, runEnd,runEnd,runEnd);
            w.format("Calibration Period: \t%tB %td,%tY - %tB %td,%tY\n", calibStart,calibStart, calibStart, runEnd,runEnd,runEnd);
            w.println();
            
            w.println("\nOutput Information"); 
            w.println("\tOutput Folder: " + this.fpath);
            w.println();
                
        }
    }
    
  
    public void checkFile() {
        if (openedFile == false) {
            if ((summaryFileName != null) && (fpath != null)) {
                String fileName = fpath + "/" + summaryFileName;
            
                System.out.println("Writing summary file to " + fileName);
                summaryFile = new File(fileName);
                if (w == null) {
                    try {
                    w = new PrintWriter(summaryFile);
                    } catch (IOException E) {
                    throw new RuntimeException(E);
                    }
                    openedFile = true;
                    w.println("************************************************");
                    w.println("*          Luca Calibration Summary            *");
                    w.println("************************************************\n");
                }
            }
        }
    }

              
    public void writeInitialStep(int s, Step step, Data stepData, double origOF) {
        checkFile();
        if (w != null) {
            w.println("-------------------------------");
            w.println("Initial Setup for Step " + step.getName());
            w.println("-------------------------------");
            
            // Show parameters used in step
            ParameterData[] paramData = stepData.paramData;
            
            for (int i = 0; i < paramData.length; i++) {
                ParameterData paramInstance = paramData[i];
                
                int calibType = paramInstance.getCalibrationType();
                Calibration cal = new Calibration();
                String calibName = cal.ConvertStrategy(calibType);
                
                w.println("\n>>> Parameter Name: " + paramInstance.getName());
                w.println();
                w.println("  " + calibName + " was used for calibration.");
                w.println("  Lower Bound = " + paramInstance.getOriginalLowerBound() + "\t\t Actual Lower Bound " + paramInstance.getLowerBound() );
                w.println("  Upper Bound = " + paramInstance.getOriginalUpperBound() + "\t\t Actual Upper Bound " + paramInstance.getUpperBound() );
                w.println();
                
                w.println("  Parameter Values:");
                w.println("\t#\tinit value\t\tcalibrated?");
                double[] pdata = paramInstance.getDataValue();
                for (int k=0; k<pdata.length; k++) {
                    iData.add(pdata[k]);
                }
                
                boolean[] calibrated = paramInstance.getCalibrationFlag();
                if (pdata.length < 2) calibrated[0] = true;
                int calibrationCount = 0;
                double mean = 0;
                for(int j=0; j<pdata.length; j++) {
                    w.format("\t"+ (j+1) + "\t%f"  + "\t\t" + calibrated[j] + "\n", pdata[j]);
                    if (calibrated[j]) {
                       mean += pdata[j];
                       calibrationCount++;
                     }
                    
                }
           
                mean = mean/calibrationCount;
                w.println("\tMean\t" + mean);
                w.println("\tInitial Parameter OF\t"+origOF);
                w.flush();
             }
                
                // Show SCE control info in step
                w.println();
                w.println(">>> SCE Control Parameter Information");
                w.println("\tNumber of complexes in the initial population: " + step.getInitComplexes());
                w.println("\tNumber of points in each complex: " + step.getPointsPerComplex());
                w.println("\tNumber of points in a sub-complex: " + step.getPointsPerSubcomplex());
                w.println("\tNumber of evolution steps before shuffling: " + step.getEvolutions());
                w.println("\tMininum number of complexes required: " + step.getMinComplexes());
                w.println("\tMaximum number of model executions: " + step.getMaxExec());
                w.println("\tShuffling loops in which the objective function value must change by given % before optimization is terminated: " + step.shufflingLoops);
                w.println("\tPercentage for the objective function value: " + step.getOfPercentage() + "%");
                w.println();
                
                w.println(">>> Objective Function (OF) Information");
                int numOF = step.ofs.size();
                w.println("\tThe number of objective functions: " + numOF);

                // Go through each objective function for step.
                int ocount = 1;
                for (ObjFunc ofs : step.ofs) {
                    
                  // Get weight- assumes even across multiple OF's if not already set.  
                  double weight = ofs.getWeight();
                  if (Double.isNaN(weight)) {
                      weight = 1.0/numOF;
                  }
                
                  w.println("\t-- OF #" + ocount + " with weight = " + weight);
                  String direction = "MINIMIZED";
                  if (ofs.of.positiveDirection()) 
                      direction = "MAXIMIZED";
                  w.println("\t\tThe objective function is: " + direction);
                  w.println("\t\tObjective Function Type: " + ofs.getMethod());
                  w.println("\t\tTime Step: " + ofs.getTimestep()); 
                  w.println("\t\tPeriod: " + ofs.getPeriodStartName(step.getStartMonth()) + " to " + ofs.getPeriodEndName(step.getStartMonth()));
                  w.println("\t\tSimulated Variable: " + ofs.getSimulated().getColumn());
                  w.println("\t\tObserved Variable: "  + ofs.getObserved().getColumn());
                  if (ofs.getSubDivideString()!=null) w.println("\t\tSubdivide info: \n\t\t\t" + ofs.getSubDivideString());
                  if (ofs.getSubDivideValue()!= Integer.MIN_VALUE) w.println("\t\tSubdivide File Value: " + ofs.getSubDivideValue());
                  w.println();
                  ocount++;
                }
        w.println();  
        w.flush();
        }
        
      
    }

    public void writeStepHeader() {
        checkFile();
        if (wroteStepHeader == false) { // only write once.
         if (w != null) {
          w.println("********************************************");
          w.println(" Calibrated Parameter Values");
          w.println("********************************************");  
         }
        }
        wroteStepHeader = true;
     }
        
              
    public void writeStep(int r, int s, Step step, Data stepData, double finalOF) {
        checkFile();
        if (w != null) {
      
            this.writeStepHeader();     
            
            w.println("-------------------------------");
            w.println("Round " + (r+1) + " Step " + step.getName());
            w.println("-------------------------------\n");
            w.println();
               
            w.println("Objective Function Values for round " + (r+1) + " = " + finalOF);
                    
            ParameterData[] paramData = stepData.paramData;
            
            for (int i = 0; i < paramData.length; i++) {
                ParameterData paramInstance = paramData[i];          
                
                w.println(">>> Parameter Name: " + paramInstance.getName());
                w.println();
                w.println("Lower Bound = " + paramInstance.getOriginalLowerBound() );
                w.println("Upper Bound = " + paramInstance.getOriginalUpperBound() );
                w.println();
                
                w.println("Parameter Values:");
                w.println("\t#\tinit value\t\tRound " + r + " data");
                double[] pdata = paramInstance.getDataValue();
                
                
                boolean[] calibrated = paramInstance.getCalibrationFlag();
                int calibrationCount = 0;
                double mean = 0;
                for(int j=0; j<pdata.length; j++) {
                    
                    w.format("\t"+ (j+1) + "\t%f\t\t%f\n", iData.get(inxt),  pdata[j]);
                    inxt++;
                    if (inxt >= iData.size()) inxt = 0;
                    if (calibrated[j]) {
                       mean += pdata[j];
                       calibrationCount++;
                     }
                    
                }
           
                mean = mean/calibrationCount;
                w.println("\tMean\t\t\t" + mean);
                w.flush();
            }          
        
        w.flush();
        }
    }

    
    
    
    public void close() {
        if (w != null) {
            w.close();
        }
    }
}
