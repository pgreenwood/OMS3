/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ngmf.sim;

import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author od
 */
public abstract class AbstractSimTesting {
    
    static String default_ll = "WARNING";
    
    static {
        System.setProperty("oms.prj", System.getProperty("user.dir"));
    }

    protected void testMe(String script, String ll) throws Throwable {
        try {
            oms3.CLI.sim(getClass().getResource(script).getFile(), ll, "run");
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
    
    protected void testMe(String script) throws Throwable {
        testMe(script, default_ll);
    }
}
