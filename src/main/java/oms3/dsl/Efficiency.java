/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms3.dsl;

import static oms3.SimConst.ABSDIF;
import static oms3.SimConst.ABSDIFLOG;
import static oms3.SimConst.AVE;
import static oms3.SimConst.DSGRAD;
import static oms3.SimConst.GRAD;
import static oms3.SimConst.IOA;
import static oms3.SimConst.IOA2;
import static oms3.SimConst.NS;
import static oms3.SimConst.NS2LOG;
import static oms3.SimConst.NSLOG;
import static oms3.SimConst.PBIAS;
import static oms3.SimConst.PMCC;
import static oms3.SimConst.R2;
import static oms3.SimConst.RMSE;
import static oms3.SimConst.ROCE;
import static oms3.SimConst.TRMSE;
import static oms3.SimConst.WR2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Locale;

import oms3.ComponentException;
import oms3.Compound;
import oms3.Conversions;
import oms3.Notification.DataflowEvent;
import oms3.Notification.Listener;
import oms3.Notification.Type;
import oms3.util.Statistics;

/**
 *
 * @author od
 */
public class Efficiency implements Buildable {

    String methods = NS;
    String obs;
    int[] obs_idx;
    String sim;
    String precip;
    List<Number> obs_l = new ArrayList<Number>();
    List<Number> sim_l = new ArrayList<Number>();
    List<Number> precip_l = new ArrayList<Number>();

    // output file optional
    String file;

    public void setFile(String file) {
        this.file = file;
    }

    public void setMethods(String methods) {
        this.methods = methods;
    }

    public void setPrecip(String precip) {
        this.precip = precip;
    }

    public void setObs(String obs) {
        String[] l = Conversions.parseArrayElement(obs);
        this.obs = l[0];
        obs_idx = Util.arraysDims(l);
    }

    public void setSim(String sim) {
        this.sim = sim;
    }

    @Override
    public Buildable create(Object name, Object value) {
        return LEAF;
    }

    public void setup(Object comp) {
        if (obs == null || sim == null) {
            throw new ComponentException("obs/sim variable not set.");
        }
        if (comp instanceof Compound) {
            Compound c = (Compound) comp;
            c.addListener(new Listener() {

                @Override
                public void notice(Type T, EventObject E) {
                    if (T == Type.OUT) {
                        DataflowEvent e = (DataflowEvent) E;
                        if (e.getAccess().getField().getName().equals(obs)) {
                            if (obs_idx == null) {
                                obs_l.add((Number) e.getValue());
                            } else {
                                obs_l.add((Number) Util.accessArray(obs, e.getValue(), obs_idx));
                            }
                        } else if (e.getAccess().getField().getName().equals(sim)) {
                            sim_l.add((Number) e.getValue()); //TODO use arrays here too.
                        }
                        if (e.getAccess().getField().getName().equals(precip)) {
                            precip_l.add((Number)e.getValue());
                        }
//                    System.err.println(E.getAccess().getField().getName() + "/" +
//                    E.getComponent().getClass().getName() + E.getValue());
                    }
                }
            });
        }
    }

    String result() {
        if (sim_l.size() != obs_l.size()) {
            throw new ComponentException("obs/sim mismatch: " + obs_l.size() + "/" + sim_l.size());
        }
        if (methods.isEmpty()) {
            return "No efficiency specified.";
        }

        StringBuffer b = new StringBuffer(String.format(Locale.US, "%-15s ", "Efficiencies"));
        for (String m : methods.split(" ")) {
            b.append(String.format(Locale.US, "%10s ", m));
        }
        b.append('\n');
        b.append(String.format(Locale.US, "%15s ", obs + "/" + sim));

        double[] obsarr = Util.convertNumber(obs_l);
        double[] simarr = Util.convertNumber(sim_l);

        double eff = 0;
        for (String m : methods.split(" ")) {
            if (NS.trim().equals(m)) {
                eff = Statistics.nashSutcliffe(obsarr, simarr, 2, -9999);
            } else if (NSLOG.trim().equals(m)) {
                eff = Statistics.nashSutcliffeLog(obsarr, simarr, 1);
            } else if (NS2LOG.trim().equals(m)) {
                eff = Statistics.nashSutcliffeLog(obsarr, simarr, 2);
            } else if (IOA.trim().equals(m)) {
                eff = Statistics.ioa(obsarr, simarr, 1, -9999);
            } else if (IOA2.trim().equals(m)) {
                eff = Statistics.ioa(obsarr, simarr, 2, -9999);
            } else if (R2.trim().equals(m)) {
                double[] rc = Statistics.linearReg(obsarr, simarr);
                eff = rc[2];
            } else if (GRAD.trim().equals(m)) {
                double[] rc = Statistics.linearReg(obsarr, simarr);
                eff = rc[1];
            } else if (WR2.trim().equals(m)) {
                double[] rc = Statistics.linearReg(obsarr, simarr);
                if (rc[1] <= 1) {
                    eff = Math.abs(rc[1]) * rc[2];
                } else {
                    eff = Math.pow(Math.abs(rc[1]), -1.0) * rc[2];
                }
            } else if (DSGRAD.trim().equals(m)) {
                eff = Statistics.dsGrad(obsarr, simarr);
            } else if (AVE.trim().equals(m)) {
                eff = Statistics.absVolumeError(obsarr, simarr);
            } else if (RMSE.trim().equals(m)) {
                eff = Statistics.rmse(obsarr, simarr);
            } else if (PBIAS.trim().equals(m)) {
                eff = Statistics.pbias(obsarr, simarr);
            } else if (PMCC.trim().equals(m)) {
                eff = Statistics.pearsonsCorrelatrion(obsarr, simarr, -9999);
            } else if (ABSDIF.trim().equals(m)) {
                eff = Statistics.absDiff(obsarr, simarr, -9999);
            } else if (ABSDIFLOG.trim().equals(m)) {
                eff = Statistics.absDiffLog(obsarr, simarr, -9999);
            } else if (TRMSE.trim().equals(m)) {
                eff = Statistics.transformedRmse(obsarr, simarr);
            } else if (ROCE.trim().equals(m)) {
                if (precip_l.size() == 0) {
                    throw new ComponentException("missing precip for computing ROCE");
                }
                double[] precarr = Util.convertNumber(precip_l);
                eff = Statistics.runoffCoefficientError(obsarr, simarr, precarr);
            } else {
                throw new ComponentException("Unknown Efficiency'"+ m + '"');
            }
            b.append(String.format(Locale.US, "%10.5f ", eff));
        }
        return b.toString();
    }

    void printEff(File dir) throws IOException {
        PrintWriter w;
        if (file!= null) {
            w  = new PrintWriter(new FileWriter(new File(dir,file), true));
        } else {
            w = new PrintWriter(new OutputStreamWriter(System.out));
        }
        w.println(result());
        w.flush();
        if (file != null) {
            w.close();
        }
    }
    
    public static void main(String[] args) {
        String[] a = (ABSDIF + ABSDIFLOG + NSLOG).split(" ");
        for (String string : a) {
            System.out.println("'" + string + "'");
        }
    }
}
