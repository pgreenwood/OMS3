/*
 * AbsoluteDifference.java
 *
 * Created on January 28, 2007, 10:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ngmf.util.cosu.luca.of;

import oms3.ObjectiveFunction;
import oms3.util.Statistics;

/**
 *
 * @author Makiko
 */
public class ABSDIF implements ObjectiveFunction {

    @Override
    public double calculate(double[] obs, double[] sim, double missingValue) {
        return Statistics.absDiff(obs, sim, missingValue);
    }

    @Override
    public boolean positiveDirection() {
        return false;
    }
}
