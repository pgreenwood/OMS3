/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ngmf.sim.basic;

import oms3.annotations.*;
import junit.framework.Assert;

/**
 *
 * @author od
 */
@Name("Olaf")
public class Mainentry {

    @In public double param;

    @Execute
    public void execute() {
        Assert.assertEquals(100.1, param, 0.0);
        System.out.println(param);
    }
}
