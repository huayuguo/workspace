package com.mediatek.factorymode.flashlight;

import java.io.IOException;
import java.util.List;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.hardware.Camera;

public class FlashLight extends Activity implements OnClickListener {
	private Button btnOpen, btnClose;
	private Button btnOk, btnFail;
	private SharedPreferences mSp;
	private SurfaceView localSurfaceView = null;
	private SurfaceHolder localSurfaceHolder =null;
	private Camera.Parameters mCameraParam;
	
	private Camera camera = null;  
       private Parameters parameters = null;  
       
   	class LightCamera implements SurfaceHolder.Callback {
		public void surfaceChanged(SurfaceHolder paramSurfaceHolder,
				int paramInt1, int paramInt2, int paramInt3) {
			if (null != camera) {
				mCameraParam = camera.getParameters();

				Size size = mCameraParam.getPictureSize();
				// PreviewFrameLayout frameLayout = (PreviewFrameLayout)
				// findViewById(R.id.frame_layout);
				// frameLayout.setAspectRatio((double) size.width / size.height);

				List<Size> sizes = mCameraParam.getSupportedPreviewSizes();
				Size optimalSize = null;
				if (size != null && size.height != 0)
					optimalSize = getOptimalPreviewSize(sizes, (double) size.width
							/ size.height);
				if (optimalSize != null) {
					mCameraParam.setPreviewSize(optimalSize.width,
							optimalSize.height);
				}
				// mCamera.setDisplayOrientation(180);
				// end
				// mCameraParam.setPreviewSize(mPrvW, mPrvH);
				mCameraParam.set("fps-mode", 0); // Frame rate is normal
				mCameraParam.set("cam-mode", 0); // Cam mode is preview
				camera.setParameters(mCameraParam);

			}

			camera.startPreview();
		}

		public void surfaceCreated(SurfaceHolder paramSurfaceHolder) {
			if (camera == null) {
				camera = Camera.open();

//				int displayRotation = getDisplayRotation(this);
//				int displayOrientation = getDisplayOrientation(displayRotation, 0);
//				Log.d("FactoryMode","openCamera displayRotation="+displayRotation+" displayOrientation="+displayOrientation);
//				mCamera.setDisplayOrientation(displayOrientation);
				mCameraParam = camera.getParameters();
				mCameraParam.setFlashMode(Parameters.FLASH_MODE_ON);
				if (null != mCameraParam && null != camera) {
					// mCameraParam.setISOSpeedEng(mISO);
					camera.setParameters(mCameraParam);
				}

			}
			
			try {
				camera.setPreviewDisplay(paramSurfaceHolder);
			} catch (IOException exception) {
				if (null != camera) {
					camera.release();
					camera = null;
				}
			}
		}

		public void surfaceDestroyed(SurfaceHolder paramSurfaceHolder) {
			if (camera == null)
				return;
			camera.stopPreview();
			camera.release();
			camera = null;
			localSurfaceHolder = null;
		}
	}
   	
   	
   	private Camera.Size getOptimalPreviewSize(List<Size> sizes,
			double targetRatio) {
		final double ASPECT_TOLERANCE = 0.05;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		// Because of bugs of overlay and layout, we sometimes will try to
		// layout the viewfinder in the portrait orientation and thus get the
		// wrong size of mSurfaceView. When we change the preview size, the
		// new overlay will be created before the old one closed, which causes
		// an exception. For now, just get the screen size

		Display display = getWindowManager().getDefaultDisplay();
		int targetHeight = Math.min(display.getHeight(), display.getWidth());

		if (targetHeight <= 0) {
			// We don't know the size of SurefaceView, use screen height
			WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
			targetHeight = windowManager.getDefaultDisplay().getHeight();
		}

		// try to find a size larger but closet to the desired preview size
		for (Size size : sizes) {
			if (targetHeight > size.height)
				continue;

			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// not found, apply origional policy.
		if (optimalSize == null) {

			// Try to find an size match aspect ratio and size
			for (Size size : sizes) {
				double ratio = (double) size.width / size.height;
				if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
					continue;
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			// Log.v(TAG, "No preview size match the aspect ratio");
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flashlight);
		btnOpen = (Button) findViewById(R.id.btn_open);
		btnClose = (Button) findViewById(R.id.btn_close);
		localSurfaceView = (SurfaceView) findViewById(R.id.camera_flash);
		localSurfaceHolder = localSurfaceView.getHolder();

		btnOpen.setOnClickListener(this);
		btnClose.setOnClickListener(this);

		btnOk = (Button) findViewById(R.id.bt_ok);
		btnFail = (Button) findViewById(R.id.bt_failed);
		btnOk.setOnClickListener(this);
		btnFail.setOnClickListener(this);
		
		mSp = getSharedPreferences("FactoryMode", 0);
	    camera = Camera.open();   
		LightCamera lightCamera = new LightCamera();
		localSurfaceHolder.addCallback(lightCamera);
	//	FlashLightControl.openDevice();
	}

	@Override
	protected void onDestroy() {
	//	FlashLightControl.closeDevice();
		super.onDestroy();
	}
 
	

	
	@Override
	public void onClick(View v) {
		SharedPreferences localSharedPreferences = this.mSp;
		switch (v.getId()) {
		case R.id.btn_open:
			//FlashLightControl.setFlashLight(true);
			if(camera ==null)
			{
				camera = Camera.open();
			}
                    parameters = camera.getParameters();  
                    parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(parameters);  
			break;
		case R.id.btn_close:
			//FlashLightControl.setFlashLight(false);
			if(camera ==null)
			{
				camera = Camera.open();   
			}
			parameters = camera.getParameters();  
			parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                    camera.setParameters(parameters);  
			break;
		case R.id.bt_ok:
			Utils.SetPreferences(this, localSharedPreferences,
					R.string.flashlight, "success");
			camera.release();
			finish();
			break;
		case R.id.bt_failed:
			Utils.SetPreferences(this, localSharedPreferences,
					R.string.flashlight, "failed");
			camera.release();
			finish();
			break;
		default:
			break;
		}
	}

}
