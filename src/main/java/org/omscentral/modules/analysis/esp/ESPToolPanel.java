/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ESPPanel.java
 *
 * Created on Dec 8, 2009, 3:13:10 PM
 */
package org.omscentral.modules.analysis.esp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import oms3.io.CSProperties;
import oms3.io.CSVTableWriter;
import oms3.io.DataIO;

/**
 *
 * @author od
 */
public class ESPToolPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 389026251362838555L;
    private ModelDateTime analysisStart = new ModelDateTime();
    private ModelDateTime analysisEnd = new ModelDateTime();
    private EnsembleData ensembleData;
    private TimeSeriesPlotter plotter;
    private DefaultListModel listModel = new DefaultListModel();
    private String result;

    /**
     * Creates new form ESPPanel
     *
     * @param ed
     */
    public ESPToolPanel(EnsembleData ed) {
        this.ensembleData = ed;
        initComponents();

        traceListList.setModel(listModel);

        // Add the plotter
        plotter = new TimeSeriesPlotter(ed.getName(), "Date", "Flow");
        rightPanel.add(plotter.getPanel());

        // Plot the data for the init period
        plotData(ed.getInitialization());

        // Forecast period id the default analysis period.
        ArrayList forecasts = ed.getForecasts();
        TimeSeriesCookie forecastTS = (TimeSeriesCookie) (forecasts.get(0));

        analysisStart = (ModelDateTime) forecastTS.getStart().clone();
        analysisEnd = (ModelDateTime) forecastTS.getEnd().clone();

        setDefaultDates();
        ed.setAnalysisPeriod(analysisStart, analysisEnd);

        loadList();
    }

    public void plotData(TimeSeriesCookie tsc) {
        plotter.addTrace(tsc);
    }

    public void plotData(TimeSeriesCookie[] tscs) {
        for (int i = 0; i < tscs.length; i++) {
            plotData(tscs[i]);
        }
    }

    private void setDefaultDates() {
        ModelDateTime initStart = ensembleData.getInitializationStart();
        ModelDateTime initEnd = ensembleData.getInitializationEnd();
        ModelDateTime forecastStart = ensembleData.getForecastStart();
        ModelDateTime forecastEnd = ensembleData.getForecastEnd();

        initStartLabel.setText(initStart.getJDBCDate().toString());
        initEndLabel.setText(initEnd.getJDBCDate().toString());
        ensembleStartLabel.setText(forecastStart.getJDBCDate().toString());
        ensembleEndLabel.setText(forecastEnd.getJDBCDate().toString());

        analysisStartSpinner.setModel(new SpinnerDateModel(analysisStart.getTime(),
                initStart.getTime(), forecastEnd.getTime(), Calendar.DAY_OF_MONTH));
        analysisStartSpinner.setEditor(new JSpinner.DateEditor(analysisStartSpinner, "yyyy-MM-dd"));

        analysisEndSpinner.setModel(new SpinnerDateModel(analysisEnd.getTime(),
                initStart.getTime(), forecastEnd.getTime(), Calendar.DAY_OF_MONTH));
        analysisEndSpinner.setEditor(new JSpinner.DateEditor(analysisEndSpinner, "yyyy-MM-dd"));
    }

    private void loadList() {
        listModel.clear();
        ArrayList order;
        if (ensembleData.getSortOrder() == EnsembleData.VOLUME) {
            order = ensembleData.getStatsInVolumeOrder();
        } else if (ensembleData.getSortOrder() == EnsembleData.PEAK) {
            order = ensembleData.getStatsInPeakOrder();
        } else {
            order = ensembleData.getStats();
        }

        for (int i = 0; i < order.size(); i++) {
            EnsembleListLabel tsi = (EnsembleListLabel) (order.get(i));
            listModel.addElement(tsi);
        }
    }

    public void setResult(String resultFile) {
        result = resultFile;
    }

    public String getResult() {
        return result;
    }

    public EnsembleData getEnsembleData() {
        return this.ensembleData;
    }

    public void setEnsembleData(EnsembleData ensembleData) {
        this.ensembleData = ensembleData;
        setDefaultDates();
    }

    public JList getTraceListList() {
        return this.traceListList;
    }

    public void setTraceListList(JList traceListList) {
        this.traceListList = traceListList;
    }

    public ModelDateTime getAnalysisStart() {
        return this.analysisStart;
    }

    public void setAnalysisStart(ModelDateTime analysisStart) {
        this.analysisStart = analysisStart;
        analysisStartSpinner.setValue(analysisStart.getJDBCDate().toString());
    }

    public ModelDateTime getAnalysisEnd() {
        return this.analysisEnd;
    }

    public void setAnalysisEnd(ModelDateTime analysisEnd) {
        this.analysisEnd = analysisEnd;
        analysisEndSpinner.setValue(analysisEnd.getJDBCDate().toString());
    }

    void writeReport0(Writer w) {

//# The first table covers the calibration period,
//# the second one contains all traces, merged with report table information
//# as meta data.
//
//@T, efc-init
// created_at, Thu Nov 10 12:46:33 MST 2011
// date_format, yyyy-MM-dd
// start, 1982-10-01 12:00:00
// end, 1984-04-30 12:00:00
//
//@H,   date,   basin_cfs,  
// Type,Date,   Real, 
//,1982-10-01,  116.49 
//,1982-10-02,  114.97 
// ...
//,1984-04-29,  578.65 
//,1984-04-30,  542.97 
//
//
//# Trace table
//
//@T, efc-traces
// created_at, Thu Nov 10 12:46:33 MST 2011
// date_format, yyyy-MM-dd
// start, 1984-05-01
// end,   1984-08-31
//
//@H,   date,   basin_cfs_1, basin_cfs_2,  basin_cfs_3, 
// Type,Date,   Real,        Real,         Real
// hist_year,,  1981,        1982,         1983
// vol_cfs,,    90299.0,     86184.7,      84786.9
// vol_rank,,   1,           3,            4
// vol_exc,,    14.3,        42.9,         57.1
// peak_cfs,,   2372.5,      1702.0,       2597.5
// ...
//
//,1984-05-01,  533.07,      531.07,       535.07,
//,1984-05-02,  571.03,      576.03,       579.03,
//,1984-05-03,  585.04,      586.04,       584.04,
// ...
//,1984-08-30,  177.96,      178.96,       173.96,
//,1984-08-31,  175.70,      171.70,       176.70, 

        try {
            CSProperties p = DataIO.properties(new File(getResult()), "Result");
            w.append("@S, esp\n");
            w.append(" description, ESP summary .\n");
            w.append(" created_at, " + new Date().toString() + "\n");
            w.append(" created_by, " + System.getProperty("user.name") + "\n");
            w.append(" results_from, " + getResult() + "\n");
            w.append(" output_var, " + ensembleData.getName() + "\n");
            w.append(" init_period, " + ensembleData.getInitializationStart().getSQLDate()
                    + " / " + ensembleData.getInitializationEnd().getSQLDate() + "\n");
            w.append(" forecast_period, " + ensembleData.getForecastStart().getSQLDate()
                    + " / " + ensembleData.getForecastEnd().getSQLDate() + "\n");
            w.append(" historical_years, " + p.getInfo().get("firstyear")
                    + " / " + p.getInfo().get("lastyear") + "\n");
            w.append("\n");

            CSVTableWriter initTable = new CSVTableWriter(w, "esp-init", new String[][]{
                        {"description", "initialization period"},
                        {"date_format", "yyyy-MM-dd"},
                        {"date_start", ensembleData.getInitializationStart().getSQLDate()},
                        {"date_end", ensembleData.getInitializationEnd().getSQLDate()}
                    });

            initTable.writeHeader(new String[][]{
                        {"Type", "Date", "Real"}
            }, "Date", ensembleData.getName());

            DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

            TimeSeriesCookie init = ensembleData.getInitialization();
            double[] vals = init.getVals();
            double[] dates = init.getDates();
            for (int i = 0; i < dates.length; i++) {
                ModelDateTime d = new ModelDateTime(dates[i]);
                double v = vals[i];
                initTable.writeRow(fmt.format(d.getTime()), v);
            }

            w.append("\n");

            CSVTableWriter traceTable = new CSVTableWriter(w, "esp-traces", new String[][]{
                        {"description", "traces for forecasting period"},
                        {"date_format", "yyyy-MM-dd"},
                        {"date_start", ensembleData.getForecastStart().getSQLDate()},
                        {"date_end", ensembleData.getForecastEnd().getSQLDate()}
                    });

            ModelDateTime mdt = new ModelDateTime();

            ArrayList<EnsembleListLabel> stats = getEnsembleData().getStats();
            int l = stats.size() + 1;

            String[] years = new String[l];
            String[] vol_cfsdays = new String[l];
            String[] vol_acft = new String[l];
            String[] vol_rank = new String[l];
            String[] vol_exc = new String[l];
            String[] peak_cfs = new String[l];
            String[] peak_rank = new String[l];
            String[] peak_exc = new String[l];
            String[] peak_date = new String[l];

            Format f1 = new Format("%11.1f");
            Format f2 = new Format("%3i");
            Format f3 = new Format("%8.1f");
            Format f4 = new Format("%4.1f");

            for (int i = 1; i < years.length; i++) {
                years[i] = Integer.toString(stats.get(i - 1).getTraceYear());
                vol_cfsdays[i] = f1.form(stats.get(i - 1).getTraceVolume());
                vol_acft[i] = f1.form(stats.get(i - 1).getTraceVolume() * 1.9835);
                vol_rank[i] = f2.form(stats.get(i - 1).getVolumeRank());
                vol_exc[i] = f4.form(stats.get(i - 1).getActVolumeProb());
                peak_cfs[i] = f3.form(stats.get(i - 1).getTracePeak());
                peak_rank[i] = f2.form(stats.get(i - 1).getPeakRank());
                peak_exc[i] = f4.form(stats.get(i - 1).getActPeakProb());
                mdt.setJul2Greg(stats.get(i - 1).getTimeToPeak());
                peak_date[i] = mdt.toString();
            }

            int numberTraces = ensembleData.getForecasts().size();
            String[] types = new String[numberTraces + 1];
            String[] header = new String[numberTraces + 1];

            types[0] = "Date";
            header[0] = "Date";
            for (int i = 1; i < types.length; i++) {
                types[i] = "Real";
                header[i] = ensembleData.getName();
            }

            Map<String, String[]> meta = new LinkedHashMap<String, String[]>();
            meta.put("Type", types);
            meta.put("Year", years);
            meta.put("Volume [cfs/days]", vol_cfsdays);
            meta.put("Volume [acre/ft]", vol_acft);
            meta.put("Volume Rank", vol_rank);
            meta.put("Volume Exceedance", vol_exc);
            meta.put("Peak [cfs]", peak_cfs);
            meta.put("Peak Rank", peak_rank);
            meta.put("Peak Exceedance", peak_exc);
            meta.put("Peak Date", peak_date);

            traceTable.writeHeader(meta, header);

            ArrayList<ESPTimeSeries> forecasts = ensembleData.getForecasts();
            int len = forecasts.get(0).getVals().length;

            List<String> row = new ArrayList<String>();
            for (int i = 0; i < len; i++) {
                ModelDateTime d = new ModelDateTime(forecasts.get(0).getDates()[i]);
                row.add(fmt.format(d.getTime()));
                for (ESPTimeSeries fc : forecasts) {
                    row.add(Double.toString(fc.getVals()[i]));
                }
                traceTable.writeRow(row);
                row.clear();
            }
            w.append("\n");
        } catch (IOException E) {
            E.printStackTrace(System.err);
        }
    }

    static void writeReport(ESPToolPanel top, Writer out) {

        Format f1 = new Format("%11.1f");
        Format f2 = new Format("%3i");
        Format f3 = new Format("%8.1f");
        Format f4 = new Format("%4.1f");

        EnsembleData ed = top.getEnsembleData();
        ArrayList<EnsembleListLabel> stats = ed.getStats();
        ModelDateTime mdt = new ModelDateTime();

        try {
            out.write("Report for '" + ed.getName() + "'\n");
            out.write("        Analysis Period: " + top.getAnalysisStart() + " to " + top.getAnalysisEnd() + "\n");
            out.write("  Initialization Period: " + ed.getInitializationStart() + " to " + ed.getInitializationEnd() + "\n");
            out.write("        Forecast Period: " + ed.getForecastStart() + " to " + ed.getForecastEnd() + "\n\n\n");
            /*
             ** Write the report for all traces.
             */
            out.write("                        Summary of All Traces for Analysis Period\n\n");
            out.write("Historic   Volume      Volume    Volume    Volume    Peak   Peak    Peak\n");
            out.write("  Year    (cfs-days)  (acre-ft)   Rank   Exceedance  (cfs)  Rank  Exceedance   Date of Peak\n");
            out.write("--------  ----------  ---------  ------  ----------  ----   ----  ----------   ------------\n");

            for (EnsembleListLabel ell : stats) {
                mdt.setJul2Greg(ell.getTimeToPeak());
                out.write("  " + ell.getTraceYear() + "  "
                        + f1.form(ell.getTraceVolume()) + ""
                        + f1.form(ell.getTraceVolume() * 1.9835) + "    "
                        + f2.form(ell.getVolumeRank()) + "       "
                        + f4.form(ell.getActVolumeProb()) + "  "
                        + f3.form(ell.getTracePeak()) + "  "
                        + f2.form(ell.getPeakRank()) + "      "
                        + f4.form(ell.getActPeakProb()) + "       "
                        + mdt + "\n");
            }
            /*
             * Write the report for selected traces.
             */
            Object[] sel = top.getTraceListList().getSelectedValues();
            ArrayList<EnsembleListLabel> selStatsInVolumeOrder = new ArrayList<EnsembleListLabel>(sel.length);
            ArrayList<EnsembleListLabel> selStatsInPeakOrder = new ArrayList<EnsembleListLabel>(sel.length);
            ArrayList<EnsembleListLabel> selYearOrder = new ArrayList<EnsembleListLabel>(sel.length);
            /*
             * Make copies
             */
            for (int i = 0; i < sel.length; i++) {
                EnsembleListLabel tsi = new EnsembleListLabel((EnsembleListLabel) (sel[i]));
                selStatsInVolumeOrder.add(i, tsi);
                selStatsInPeakOrder.add(i, tsi);
                selYearOrder.add(i, tsi);
            }

            EnsembleData.sort(selStatsInVolumeOrder, selStatsInPeakOrder);
            ArrayList order;

            if (sel.length > 0) {
                if (ed.getSortOrder() == ed.VOLUME) {
                    order = selStatsInVolumeOrder;
                    out.write("\n\n\n               Summary of Selected Traces for Analysis Period (by Volume)\n\n");
                } else if (ed.getSortOrder() == ed.PEAK) {
                    order = selStatsInPeakOrder;
                    out.write("\n\n\n              Summary of Selected Traces for Analysis Period (by Peak)\n\n");
                } else {
                    order = selYearOrder;
                    out.write("\n\n\n              Summary of Selected Traces for Analysis Period (by Year)\n\n");
                }

                out.write("Historic   Volume      Volume    Volume    Volume    Peak   Peak    Peak\n");
                out.write("  Year    (cfs-days)  (acre-ft)   Rank   Exceedance  (cfs)  Rank  Exceedance   Date of Peak\n");
                out.write("--------  ----------  ---------  ------  ----------  ----   ----  ----------   ------------\n");

                /*
                 ** Sort peak (if report by volume) or volume (if report by
                 * peak) of * the selected traces.
                 */
                Iterator it = order.iterator();
                while (it.hasNext()) {
                    EnsembleListLabel ell = (EnsembleListLabel) (it.next());
                    mdt.setJul2Greg(ell.getTimeToPeak());
                    out.write("  " + ell.getTraceYear() + "  "
                            + f1.form(ell.getTraceVolume()) + ""
                            + f1.form(ell.getTraceVolume() * 1.9835) + "    "
                            + f2.form(ell.getVolumeRank()) + "       "
                            + f4.form(ell.getActVolumeProb()) + "  "
                            + f3.form(ell.getTracePeak()) + "  "
                            + f2.form(ell.getPeakRank()) + "      "
                            + f4.form(ell.getActPeakProb()) + "       "
                            + mdt + "\n");
                }
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeReport(String report) throws IOException {
        FileWriter w = new FileWriter(report);
        writeReport(this, w);
        w.close();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        rightPanel = new javax.swing.JPanel();
        reportButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        selectYearsCombo = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        traceListList = new javax.swing.JList();
        jLabel4 = new javax.swing.JLabel();
        initStartLabel = new javax.swing.JLabel();
        initEndLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        ensembleStartLabel = new javax.swing.JLabel();
        ensembleEndLabel = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        analysisStartSpinner = new javax.swing.JSpinner();
        analysisEndSpinner = new javax.swing.JSpinner();
        exportButton = new javax.swing.JButton();

        rightPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        rightPanel.setLayout(new java.awt.BorderLayout());

        reportButton.setText("Save Statistics");
        reportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reportButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Traces: ");

        selectYearsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "La Nina", "El Nino ", "ENSO Neutral", "PDO < -0.5", "PDO > 0.5", "PDO Neutral" }));
        selectYearsCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                selectYearsComboItemStateChanged(evt);
            }
        });

        jLabel3.setText("Sort by:");

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("Volume");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("Peak");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setText("Year");
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });

        traceListList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                traceListListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(traceListList);

        jLabel4.setText("Init:");

        initStartLabel.setText("jLabel5");

        initEndLabel.setText("jLabel6");

        jLabel7.setText("Forecast:");

        ensembleStartLabel.setText("jLabel8");

        ensembleEndLabel.setText("jLabel9");

        jLabel11.setText("Analysis");

        exportButton.setText("Save Data and Results ");
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(analysisStartSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(analysisEndSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jRadioButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton3))
                    .addComponent(jLabel3)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(ensembleEndLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ensembleStartLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(initEndLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                            .addComponent(initStartLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectYearsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(reportButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exportButton)
                        .addGap(0, 116, Short.MAX_VALUE))
                    .addComponent(rightPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(selectYearsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addComponent(jLabel3)
                        .addGap(3, 3, 3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jRadioButton1)
                            .addComponent(jRadioButton2)
                            .addComponent(jRadioButton3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(initStartLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(initEndLabel)
                        .addGap(9, 9, 9)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7)
                            .addComponent(ensembleStartLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ensembleEndLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(analysisStartSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(rightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(analysisEndSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reportButton)
                    .addComponent(exportButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void reportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reportButtonActionPerformed

//        Writer w = new PrintWriter(System.out);
//        writeReport(this, w);

        JFileChooser fc = new JFileChooser(getResult());
        fc.setDialogTitle("Save ESP Statistics Report");
        fc.setSelectedFile(new File("esp-report.txt"));
        int r = fc.showSaveDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            FileWriter w = null;
            try {
                w = new FileWriter(fc.getSelectedFile());
                writeReport(this, w);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            } finally {
                try {
                    w.close();
                } catch (IOException ex) {
                }
            }
        }

    }//GEN-LAST:event_reportButtonActionPerformed

    private void selectYearsComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_selectYearsComboItemStateChanged
        int sel = selectYearsCombo.getSelectedIndex();
        int flag = ElNino.UNKNOWN;
        switch (sel) {
            case 0:
                flag = ElNino.LA_NINA;
                break;
            case 1:
                flag = ElNino.EL_NINO;
                break;
            case 2:
                flag = ElNino.NEUTRAL;
                break;
            case 3:
                flag = ElNino.NEG_PDO;
                break;
            case 4:
                flag = ElNino.POS_PDO;
                break;
            case 5:
                flag = ElNino.NEU_PDO;
                break;
            default:
                throw new IllegalArgumentException("combo");
        }
        selectListItems(flag);
        /*
         ** Definition of categories: * LA_NINA - Water Year NINO3.4 SSTs <
         * -0.5 C * EL_NINO - Water Year NINO3.4 SSTs > 0.5 C * NEUTRAL - Water
         * Year NINO3.4 SSTs < 0.5 and > -0.5 C * NEG_PDO - PDO < -0.5 * POS_PDO
         * - PDO > 0.5 * NEU_PDO - PDO Neutral
         */
    }//GEN-LAST:event_selectYearsComboItemStateChanged

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        ensembleData.setSortOrder(EnsembleData.VOLUME);
        loadList();
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        ensembleData.setSortOrder(EnsembleData.PEAK);
        loadList();
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
        ensembleData.setSortOrder(EnsembleData.YEAR);
        loadList();
    }//GEN-LAST:event_jRadioButton3ActionPerformed

    private void traceListListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_traceListListValueChanged
        if (!evt.getValueIsAdjusting()) {
            /*
             * Clear the ensemble traces in the plot
             */
            Iterator it = ensembleData.getStats().iterator();
            while (it.hasNext()) {
                EnsembleListLabel ell = (EnsembleListLabel) (it.next());
                plotter.clearTrace(ell.getTraceName());
            }

            if (traceListList.getSelectedIndex() != -1) {
                Object[] sel = traceListList.getSelectedValues();
                for (int i = 0; i < sel.length; i++) {
                    EnsembleListLabel ell = (EnsembleListLabel) (sel[i]);
                    plotData(ell.getForecast());
                }
            }
        }
    }//GEN-LAST:event_traceListListValueChanged

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed

//            StringWriter w = new StringWriter();
//                writeReport0(w);

        JFileChooser fc = new JFileChooser(getResult());
        fc.setDialogTitle("Save ESP Data, Traces, and Results");
        fc.setSelectedFile(new File("esp-results.csv"));
        int r = fc.showSaveDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            FileWriter w = null;
            try {
                w = new FileWriter(fc.getSelectedFile());
                writeReport0(w);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            } finally {
                try {
                    w.close();
                } catch (IOException ex) {
                }
            }
        }
    }//GEN-LAST:event_exportButtonActionPerformed

    public void selectListItems(int enso_code) {
        int count = 0;
        for (int j = 0; j < listModel.getSize(); j++) {
            EnsembleListLabel label = (EnsembleListLabel) (listModel.getElementAt(j));
            if (ElNino.lookUp(enso_code, label)) {
                count++;
            }
        }

        int[] sel = new int[count];
        count = 0;
        for (int j = 0; j < listModel.getSize(); j++) {
            EnsembleListLabel label = (EnsembleListLabel) (listModel.getElementAt(j));
            if (ElNino.lookUp(enso_code, label)) {
                sel[count++] = j;
            }
        }
        traceListList.setSelectedIndices(sel);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner analysisEndSpinner;
    private javax.swing.JSpinner analysisStartSpinner;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel ensembleEndLabel;
    private javax.swing.JLabel ensembleStartLabel;
    private javax.swing.JButton exportButton;
    private javax.swing.JLabel initEndLabel;
    private javax.swing.JLabel initStartLabel;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton reportButton;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JComboBox selectYearsCombo;
    private javax.swing.JList traceListList;
    // End of variables declaration//GEN-END:variables
}
