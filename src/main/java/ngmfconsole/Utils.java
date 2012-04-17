/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ngmfconsole;

import static ngmfconsole.Main.logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author od
 */
public class Utils {

    static private String oms_version;

    private Utils() {
    }

    static synchronized String getOMSAppVersion() {
        if (oms_version == null) {
            try {
                BufferedReader r = new BufferedReader(new InputStreamReader(
                        Utils.class.getResourceAsStream("version.txt")));
                oms_version = r.readLine();
                r.close();
            } catch (Exception ex) {
                oms_version = "?";
            }
        }
        return oms_version;
    }

    static void download(File url, File local) throws IOException {
        logger.info("Installing :" + url + " -> " + local);
        File tmp = File.createTempFile(local.getName() + "-", ".part", local.getParentFile());
        tmp.deleteOnExit();
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(url));
        BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(tmp));
        byte data[] = new byte[4096];
        int len = 0;
        while ((len = in.read(data, 0, data.length)) >= 0) {
            bout.write(data, 0, len);
        }
        bout.close();
        in.close();
        tmp.renameTo(local);
    }

    static File downloadDir() throws MalformedURLException {
        String dir = System.getProperty("user.dir");
        File f = new File(dir, "oms3");
        // installation directory
        if (f.exists()) {
            return f;
        }
        throw new IllegalArgumentException("Not found for install: " + f.toString());
    }

    static void unzip(File targetDir, File zipFile) {
        int BUFFER = 4096;
        try {
            FileInputStream fis = new FileInputStream(zipFile);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            int count;
            byte data[] = new byte[BUFFER];
            while ((entry = zis.getNextEntry()) != null) {
                System.out.println("Extracting: " + entry);
                FileOutputStream fos = new FileOutputStream(new File(targetDir, entry.getName()));
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
            zis.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    static String cp_all(File oms3Home, String omsWork) {
        List<String> ll = new ArrayList<String>();

        for (File file : oms3Home.listFiles()) {
            if (file.getName().endsWith("jar")) {
                ll.add(file.toString());
            }
        }
        if (omsWork != null) {
            File omsLib = new File(omsWork, "lib");
            if (omsLib.exists() && omsLib.isDirectory()) {
                for (File file : omsLib.listFiles()) {
                    if (file.getName().endsWith("jar")) {
                        ll.add(file.toString());
                    }
                }
            }
            File omsDist = new File(omsWork, "dist");
            if (omsDist.exists() && omsDist.isDirectory()) {
                for (File file : omsDist.listFiles()) {
                    if (file.getName().endsWith("jar")) {
                        ll.add(file.toString());
                    }
                }
            }
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < ll.size(); i++) {
            String s = ll.get(i);
            b.append(s);
            if (i < ll.size() - 1) {
                b.append(File.pathSeparatorChar);
            }
        }
        return b.toString();
    }
}
