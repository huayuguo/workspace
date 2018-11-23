package com.mediatek.factorymode.flashlight;

import android.util.Log;

public class FlashLightControl {

	static{
		try {
			System.loadLibrary("jni_flashlight");		
			Log.d("FlashLight","loadLibrary success !");
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("FlashLight","loadLibrary fail !");
		}
	}
	
	public static native boolean openDevice();
	public static native void closeDevice();
	
	public static native void setFlashLight(boolean on);

}
