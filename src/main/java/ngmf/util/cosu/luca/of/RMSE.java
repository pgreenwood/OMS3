/*
 * NormalizedRootMeanSquareError.java
 *
 * Created on January 28, 2007, 10:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ngmf.util.cosu.luca.of;

import oms3.ObjectiveFunction;
import oms3.util.Statistics;

/**
 */
public class RMSE implements ObjectiveFunction {

    @Override
    public double calculate(double[] obs, double[] sim, double missing) {
        return Statistics.rmse(obs, sim);
    }
    
    @Override
    public boolean positiveDirection() {
        return false;
    }
}
