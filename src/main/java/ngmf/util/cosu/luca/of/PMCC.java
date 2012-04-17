/*
 * PearsonsCorrelation.java
 *
 * Created on February 13, 2007, 5:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ngmf.util.cosu.luca.of;

import oms3.ObjectiveFunction;
import oms3.util.Statistics;

/**
 *
 * @author Makiko, od
 */
public class PMCC implements ObjectiveFunction {

    @Override
    public double calculate(double[] obs, double[] sim, double missingValue)  {
       return Statistics.pearsonsCorrelatrion(obs, sim, missingValue);
    }
    
    @Override
    public boolean positiveDirection() {
        return true;
    }
}
