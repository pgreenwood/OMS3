/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ngmf.sim.test;

import oms3.annotations.*;

/**
 *
 * @author od
 */
public class Mainentry {

    @In public double p1;
    @In public double p2;
    @In public double p3;
    
    @Out public double est_coeff;
    @Out public double estimate;

    @Execute
    public void execute() {
        for (int a = 0; a < 100000; a++) {
            est_coeff = p1 * 1.2;
            estimate = p2 + p2 + p3;
            double d = Math.atan2(1.0666, 3.45);
            d = Math.sin(1.0666);
        }
    }
}
