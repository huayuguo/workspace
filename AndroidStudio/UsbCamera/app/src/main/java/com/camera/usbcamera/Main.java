package com.camera.usbcamera;

import android.app.Activity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Main extends Activity {

    private static final String TAG = "WebCam";
	
	CameraPreview cp;

    private AlertDialog mErrorDialog;
    
	Button mCaptureButton;
	Button mRecordingButton;
	Button mSwitcherButton;
	
	static final int CAPTURE_STT = 0;
	static final int RECORDING_STT = 1;
	
	int mCurrentStatus = CAPTURE_STT;
	
	boolean mRecordingStart = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		cp = (CameraPreview) findViewById(R.id.cp);
        cp.setMainActivity(this);
		
		mCaptureButton = (Button)findViewById(R.id.captureButton);
		mCaptureButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG, "Call captureImage()");
                UVCJni.captureImage();
			}
			
		});
		mRecordingButton = (Button)findViewById(R.id.recordingButton);
		mRecordingButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mRecordingStart) {
					// Stop Recording
					cp.startRecording(false);
					mRecordingButton.setText(R.string.start_recording);
					mRecordingStart = false;
				} else {
					
					// Start Recording
					cp.startRecording(true);
					mRecordingButton.setText(R.string.stop_recording);
					mRecordingStart = true;
				}
			}
			
		});
		mRecordingButton.setVisibility(View.GONE);
		
		mSwitcherButton = (Button)findViewById(R.id.switcherButton);
		mSwitcherButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (CAPTURE_STT == mCurrentStatus) {
					
					mCaptureButton.setVisibility(View.GONE);
					mRecordingButton.setVisibility(View.VISIBLE);
					mRecordingButton.setText(R.string.start_recording);
					
					mCurrentStatus = RECORDING_STT;
					mRecordingStart = false;
					mSwitcherButton.setText(R.string.switch_to_capture);
				} else if (RECORDING_STT == mCurrentStatus) {
					mRecordingButton.setVisibility(View.GONE);
					mCaptureButton.setVisibility(View.VISIBLE);
					
					mSwitcherButton.setText(R.string.switch_to_recording);
					mCurrentStatus = CAPTURE_STT;
					if (mRecordingStart) {
						
						// Should stop recording
                        cp.startRecording(false);
					}
				}
			}
			
		});

        buildErrorDialog();
	}

    private void buildErrorDialog() {
        AlertDialog.Builder builder = new Builder(this);

        builder.setTitle(R.string.error_title);
        builder.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        mErrorDialog = builder.create();

        mErrorDialog.setCancelable(false);
        mErrorDialog.setCanceledOnTouchOutside(false);
    }

    void showErrorDialog(String message) {
        if (mErrorDialog != null) {
            mErrorDialog.setMessage(message);

            mErrorDialog.show();
        } else {
            Log.w(TAG, "mErrorDialog isnt' created");
        }
    }
	
}
