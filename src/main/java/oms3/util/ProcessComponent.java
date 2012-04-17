/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms3.util;

import java.io.File;
import java.io.StringWriter;
import java.util.logging.Logger;

import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;


/** Generic Process component.
 *
 * @author od
 */
public class ProcessComponent {
    static final Logger log = Logger.getLogger(ProcessComponent.class.getName());

    @In public String exe;
    @In public String[] args;
    @In public String working_dir;

    @Out public String stdout;
    @Out public String stderr;
    
    @Out public int exitValue;

    @Execute
    public void execute() {
        StringWriter out = new StringWriter();
        StringWriter err = new StringWriter();
        try {
            Processes p = new Processes(new File(exe));
            p.setArguments((Object[]) args);

            if (working_dir != null && !working_dir.isEmpty()) {
                p.setWorkingDirectory(new File(working_dir));
            }
            p.redirectOutput(out);
            p.redirectError(err);

            exitValue = p.exec();
            stdout = out.toString();
            stderr = err.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
