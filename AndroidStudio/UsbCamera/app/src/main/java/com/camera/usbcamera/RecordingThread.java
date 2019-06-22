package com.camera.usbcamera;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class RecordingThread extends Thread {
	
	private static final String TAG="WebCam";
	
	public static final int RECORDING_START_MSG = 0;
	public static final int RECORDING_STOP_MSG = 1;
	public static final int RECORDING_SAVE_MSG = 2;
	
	private Handler mRecordingHandler;
	
	public RecordingThread() {
		
	}
	
	static class RecordingHandler extends Handler {
		
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case RECORDING_START_MSG:
                Log.e(TAG, "Handling RECORDING_START_MSG");
                UVCJni.startRecording();
				break;
			case RECORDING_STOP_MSG:
                Log.e(TAG, "Handling RECORDING_STOP_MSG");
                UVCJni.stopRecording();
				break;
			case RECORDING_SAVE_MSG:
                Log.e(TAG, "Handling RECORDING_SAVE_MSG");
                UVCJni.recording();
				break;
			default:
				Log.w(TAG, "Invalid msg.what(" + msg.what + ")");
				break;
			}
		}
	}
	
	public Handler getRecordingHandler() {
		if (null != mRecordingHandler) {
			return mRecordingHandler;
		} else {
			Log.w(TAG, "Recording Handler isn't initialized");
			return null;
		}
	}
	
	private void sendMessage(int what) {
		if (null != mRecordingHandler) {
			Message msg = Message.obtain();
			msg.what = what;
			mRecordingHandler.sendMessage(msg);
		} else {
			Log.e(TAG, "mRecordingHandler isn't initialized");
		}
	}
	
	public void startRecording() {
        Log.e(TAG, "Send RECORDING_START_MSG");
		sendMessage(RECORDING_START_MSG);
	}
	
	public void stopRecording() {
        Log.e(TAG, "Send RECORDING_STOP_MSG");
		sendMessage(RECORDING_STOP_MSG);
	}
	
	public void saveFrame() {
        Log.e(TAG, "Send RECORDING_SAVE_MSG");
		sendMessage(RECORDING_SAVE_MSG);
	}
	
	public void run() {
		Looper.prepare();
		mRecordingHandler = new RecordingHandler();
		Looper.loop();
	}
}
