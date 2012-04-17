/*
 * NashSutclitte.java
 *
 * Created on January 28, 2007, 10:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ngmf.util.cosu.luca.of;

import oms3.ObjectiveFunction;
import oms3.util.Statistics;

/**
 *
 */
public class IOA2 implements ObjectiveFunction {

    @Override
    public double calculate(double[] obs, double[] sim, double missing)  {
        return Statistics.ioa(obs, sim, 2.0, missing);
    }

    @Override
    public boolean positiveDirection() {
        return true;
    }
}
