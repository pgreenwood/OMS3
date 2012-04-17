/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms3.dsl;

import static oms3.SimConst.COUNT;
import static oms3.SimConst.LAG1;
import static oms3.SimConst.MAX;
import static oms3.SimConst.MEAN;
import static oms3.SimConst.MEANDEV;
import static oms3.SimConst.MEDIAN;
import static oms3.SimConst.MIN;
import static oms3.SimConst.MONTHLY;
import static oms3.SimConst.PROD;
import static oms3.SimConst.Q1;
import static oms3.SimConst.Q2;
import static oms3.SimConst.Q3;
import static oms3.SimConst.RANGE;
import static oms3.SimConst.STDDEV;
import static oms3.SimConst.SUM;
import static oms3.SimConst.VAR;
import static oms3.SimConst.WEEKLY;
import static oms3.SimConst.YEARLY;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EventObject;
import java.util.List;
import java.util.Locale;

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
public class Summary implements Buildable {

    private static final String[] opt = {MONTHLY, YEARLY, WEEKLY};
    //
    String time;
    String var;
    int idx[];
    Calendar cal;
    StringBuffer out;
    List<Number> var_l = new ArrayList<Number>();
    int field = Calendar.DAY_OF_MONTH;
    String moments = MEAN;
    // output file optional
    String file;

    public void setFile(String file) {
        this.file = file;
    }

    public void setPeriod(String period) {
        if (period.equals(WEEKLY)) {
            field = Calendar.DAY_OF_WEEK;
        } else if (period.equals(MONTHLY)) {
            field = Calendar.DAY_OF_MONTH;
        } else if (period.equals(YEARLY)) {
            field = Calendar.DAY_OF_YEAR;
        } else {
            throw new IllegalArgumentException(period);
        }
    }

    public void setMoments(String moments) {
        this.moments = moments;
    }

    public void setVar(String var) {
        String[] l = Conversions.parseArrayElement(var);
        this.var = l[0];
        idx = Util.arraysDims(l);
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public Buildable create(Object name, Object value) {
        return LEAF;
    }

    void setup(Object comp) {
        if (comp instanceof Compound) {
            Compound c = (Compound) comp;

            out = new StringBuffer("Summary for '" + var + "' (" + opt[field - 5] + ")\n");
            out.append(String.format(Locale.US, "%19s", time) + "  ");
            for (String s : moments.split(" ")) {
                out.append(String.format(Locale.US, "%14s", s));
            }
            out.append('\n');

            c.addListener(new Listener() {

                @Override
                public void notice(Type T, EventObject E) {
                    if (T == Type.OUT) {
                        DataflowEvent e = (DataflowEvent) E;
                        if (e.getAccess().getField().getName().equals(time)) {
                            if (cal == null) {
                                cal = (Calendar) e.getValue();
                            }
                        } else if (e.getAccess().getField().getName().equals(var)) {
                            if (idx == null) {
                                var_l.add((Number) e.getValue());
                            } else {
                                var_l.add((Number) Util.accessArray(var, e.getValue(), idx));
                            }
                            if (cal == null) {
                                // TODO dangerous
                                return;
                            }
                            if (cal.get(field) == 1) {
                                double[] d = Util.convertNumber(var_l);
                                double eff = 0;
                                out.append(Conversions.formatISO(cal.getTime()));
                                out.append("  ");
                                for (String m : moments.split(" ")) {
                                    if (MAX.startsWith(m)) {
                                        eff = Statistics.max(d);
                                    } else if (MIN.startsWith(m)) {
                                        eff = Statistics.min(d);
                                    } else if (MEAN.startsWith(m)) {
                                        eff = Statistics.mean(d);
                                    } else if (COUNT.startsWith(m)) {
                                        eff = Statistics.length(d);
                                    } else if (RANGE.startsWith(m)) {
                                        eff = Statistics.range(d);
                                    } else if (MEDIAN.startsWith(m)) {
                                        eff = Statistics.median(d);
                                    } else if (STDDEV.startsWith(m)) {
                                        eff = Statistics.stddev(d);
                                    } else if (VAR.startsWith(m)) {
                                        eff = Statistics.variance(d);
                                    } else if (MEANDEV.startsWith(m)) {
                                        eff = Statistics.meandev(d);
                                    } else if (SUM.startsWith(m)) {
                                        eff = Statistics.sum(d);
                                    } else if (PROD.startsWith(m)) {
                                        eff = Statistics.product(d);
                                    } else if (Q1.startsWith(m)) {
                                        eff = Statistics.quantile(d, 0.25);
                                    } else if (Q2.startsWith(m)) {
                                        eff = Statistics.quantile(d, 0.50);
                                    } else if (Q3.startsWith(m)) {
                                        eff = Statistics.quantile(d, 0.75);
                                    } else if (LAG1.startsWith(m)) {
                                        eff = Statistics.lag1(d);
                                    } else {
                                        throw new IllegalArgumentException(m);
                                    }
                                    out.append(String.format(Locale.US, "%14.5f", eff));
                                }
                                out.append('\n');
                                var_l.clear();
                            }
//                    System.err.println(E.getAccess().getField().getName() + "/" +
//                    E.getComponent().getClass().getName() + E.getValue());
                        }
                    }
                }
            });
        }
    }

    public void printSum(File dir) throws IOException {
        PrintWriter w;
        if (file != null) {
            w = new PrintWriter(new FileWriter(new File(dir, file), true));
        } else {
            w = new PrintWriter(new OutputStreamWriter(System.out));
        }
        w.println(out.toString());
        w.flush();
        if (file != null) {
            w.close();
        }
    }
}
