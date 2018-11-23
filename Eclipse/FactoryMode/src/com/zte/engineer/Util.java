
package com.zte.engineer;

import android.os.Build;
import android.os.StatFs;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Util {

    public static final String TAG = "EngineerCode";
    private static boolean LOG = true;
    public static final int RET_SUCCESS = 1;
    public static final int RET_FAILED = 0;
    public static final int TOLERANCE_20 = 2;
    public static final int TOLERANCE_30 = 3;
    public static final int TOLERANCE_40 = 4;
    /**
     * In release, DEBUG should be set to false; true:Purpose is to debug codes
     * in eclipse, because some test need permission signature(eg.
     * BacklightTest). false:Release or other.
     */
    public static boolean DEBUG = false;

    private Util() {

    }

    public static final void log(String tag, String info) {
        if (LOG) {
            Log.d(TAG, tag + ":" + info);
        }
    }

    public static final void log(String tag, String info, Throwable tr) {
        if (LOG) {
            Log.w(TAG, tag + ":" + info, tr);
        }
    }

    public static long getTotalBytes(StatFs fs) {
        long size = 0;
            size = fs.getTotalBytes();
        return size;
    }

    public static long getAvailableBytes(StatFs fs) {
        long size = 0;
            size = fs.getAvailableBytes();
        return size;
    }

    public static final String getDeviceProc(String procFilePath) {
        StringBuffer content = new StringBuffer();
        BufferedReader localBufferedReader = null;
        try {
            localBufferedReader = new BufferedReader(
                    new FileReader(procFilePath), 4096);
            String line;

            while ((line = localBufferedReader.readLine()) != null) {
                content.append(line);
                Log.i(TAG, "[getDeviceProc] path :" + procFilePath
                        + ",-->>> line = " + line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (localBufferedReader != null) {
                try {
                    localBufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return content.toString();
    }

    /**
     * Return the sim slot number.
     * @return
     */
    public static final int getSimSlotNum() {
        /*int simCount = -1;

        String strSimNum = SystemProperties.get("persist.gemini.sim_num", "");
        try {
            simCount = Integer.parseInt(strSimNum);
            if (simCount > 0) {
                return simCount;
            }
        } catch (NumberFormatException nfe) {
            log(TAG, "unexcepted exception here !!!", nfe);
            simCount = -2;
        }

        String strSimCount = SystemProperties.get("ro.telephony.sim.count", "");
        try {
            simCount = Integer.parseInt(strSimCount);
        } catch (NumberFormatException nfe) {
            log(TAG, "unexcepted exception here !!!", nfe);
            simCount = -3;
        }
        return simCount;*/
        
        return TelephonyManager.getDefault().getPhoneCount();
    }

	public static String[] runCmdInEmSvr(int index, int paramNum, int... param) {
        ArrayList<String> arrayList = new ArrayList<String>();
        AFMFunctionCallEx functionCall = new AFMFunctionCallEx();
        boolean result = functionCall.startCallFunctionStringReturn(index);
        Log.e("chengrq","result:" + result);
        boolean result1 = functionCall.writeParamNo(paramNum);
        Log.e("chengrq","result1" + result1);
        for (int i : param) {
            functionCall.writeParamInt(i);
        }
        if (result) {
            FunctionReturn r;
            do {
                r = functionCall.getNextResult();
                if (r.mReturnString.isEmpty()) {
                    break;
                }
                arrayList.add(r.mReturnString);
            } while (r.mReturnCode == AFMFunctionCallEx.RESULT_CONTINUE);
            if (r.mReturnCode == AFMFunctionCallEx.RESULT_IO_ERR) {
                //Log.d("@M_" + TAG, "AFMFunctionCallEx: RESULT_IO_ERR");
                arrayList.clear();
                arrayList.add("ERROR");
            }
        } else {
            //Log.d("@M_" + TAG, "AFMFunctionCallEx return false");
            arrayList.clear();
            arrayList.add("ERROR");
        }
        return arrayList.toArray(new String[arrayList.size()]);
    }

	/**
	*
	*/
	public static int doGsensorCalibration(int tolerance) {
		Log.e("chengrq","tolerance:" + tolerance);
		String[] ret = runCmdInEmSvr(
				AFMFunctionCallEx.FUNCTION_EM_SENSOR_DO_GSENSOR_CALIBRATION, 1,
				tolerance);
		if (ret.length > 0 && String.valueOf(RET_SUCCESS).equals(ret[0])) {
			return RET_SUCCESS;
		}
		return RET_FAILED;
	}

	public static int clearGsensorCalibration() {
        String[] ret = runCmdInEmSvr(
                AFMFunctionCallEx.FUNCTION_EM_SENSOR_CLEAR_GSENSOR_CALIBRATION, 0);
        if (ret.length > 0 && String.valueOf(RET_SUCCESS).equals(ret[0])) {
            return RET_SUCCESS;
        }
        return RET_FAILED;
    }

	/**
	*
	*/
	public static int doGyroscopeCalibration(int tolerance) {
		Log.e("chengrq","tolerance:" + tolerance);
		String[] ret = runCmdInEmSvr(
				AFMFunctionCallEx.FUNCTION_EM_SENSOR_DO_GYROSCOPE_CALIBRATION, 1,
				tolerance);
		if (ret.length > 0 && String.valueOf(RET_SUCCESS).equals(ret[0])) {
			return RET_SUCCESS;
		}
		return RET_FAILED;
	}

	public static int clearGyroscopeCalibration() {
        String[] ret = runCmdInEmSvr(
                AFMFunctionCallEx.FUNCTION_EM_SENSOR_CLEAR_GYROSCOPE_CALIBRATION, 0);
        if (ret.length > 0 && String.valueOf(RET_SUCCESS).equals(ret[0])) {
            return RET_SUCCESS;
        }
        return RET_FAILED;
    }
}
