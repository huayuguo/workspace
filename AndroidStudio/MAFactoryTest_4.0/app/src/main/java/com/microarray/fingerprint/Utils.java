package com.microarray.fingerprint;

import java.io.DataOutputStream;
import java.io.File;

import android.util.Log;


public class Utils {
	
	private static final String TAG = "ma.factory";
	
	public static void dprint(String msg) {
		Log.d(TAG, msg);
	}

	public static void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}	
	
	public static void deleteFiles(String path) {
		if (!new File(path).exists()) return;
		File[] files = new File(path).listFiles();
		for (int i = 0; files != null && i < files.length; i++) {
			if (files[i].isDirectory()) {
				deleteFiles(files[i].getAbsolutePath());
			} else if (files[i].isFile()) {
				files[i].delete();
			}
		}
		new File(path).delete();
	}
	
    public static boolean runRootCommand(String command) {
        boolean ok = true;
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            ok = false;
        } finally {
            try {
                if (os != null)
                    os.close();
                if (process != null)
                    process.exitValue();
            } catch (IllegalThreadStateException e) {
                process.destroy();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return ok;
    }

}
