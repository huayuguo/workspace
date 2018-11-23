package com.mediatek.factorymode.deviceinfo;

import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;

public class DeviceInfo extends Activity implements OnClickListener {

	private SharedPreferences mSp;
	public static final int MSG_SET_GPU = 10;
	String TAG = "DeviceInfo";
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_SET_GPU:
				setValue(R.id.gpu_val, (String) msg.obj);
				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.deviceinfo);
		this.mSp = getSharedPreferences("FactoryMode", 0);
		;
		findViewById(R.id.bt_ok).setOnClickListener(this);
		findViewById(R.id.bt_failed).setOnClickListener(this);

		// set brand
		setValue(R.id.brand_val, Build.BRAND);
		// set model
		setValue(R.id.model_val, Build.MODEL);
		// set android version
		setValue(R.id.version_val, Build.VERSION.RELEASE);
		// set cpu model
		setValue(R.id.cpu_val, CPUTool.getCpuName());
		// set serialno
		setValue(R.id.serialno_val, getIMSI());

		// set resolution
		setValue(R.id.resolution_val, getResolution());

		// set gpu
		View v = new DemoGLSurfaceView(this);
		ViewGroup surfaceContainer = (ViewGroup) findViewById(R.id.surface_container);
		surfaceContainer.addView(v);

		// set data storage
		setValue(R.id.data_storage_val, getDataStorageInfo());

		// set ram info
		setValue(R.id.ram_val,
				CPUTool.getFreeRamInfo() + "/" + CPUTool.getTotalRamInfo());
		// set internal storage

		setValue(R.id.internal_storage_val, getInternalStorage());

		//set camera info
		getWindow().getDecorView().post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				setCameraValue();
			}
		});
	}

	private String getResolution() {
		DisplayMetrics out = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getRealMetrics(out);
		return out.widthPixels + "x" + out.heightPixels;
	}

	private TextView getValueContainer(int id) {
		return (TextView) findViewById(id);
	}

	private void setValue(int id, String value) {
		getValueContainer(id).setText(value);
	}

	private String getDataStorageInfo() {
		String ret[] = new String[2];
		long blockSize = 0;
		long blockCount = 0;
		long availCount = 0;
		long totalSize = 0;
		long availSize = 0;

		// StatFs rootsf = new StatFs(Environment.getRootDirectory().getPath());
		StatFs rootsf = new StatFs(Environment.getDataDirectory().getPath());
		blockSize = rootsf.getBlockSize();
		blockCount = rootsf.getBlockCount();
		availCount = rootsf.getAvailableBlocks();
		totalSize = blockCount * blockSize / 1024L / 1024L;
		availSize = availCount * blockSize / 1024L / 1024L;
		ret[0] = availSize + "MB";
		ret[1] = totalSize + "MB";

		return ret[0] + "/" + ret[1];
	}

	private String getInternalStorage() {
		StatFs sf = new StatFs("/mnt/sdcard");
		long blockSize = sf.getBlockSize();
		long blockCount = sf.getBlockCount();
		long availCount = sf.getAvailableBlocks();
		long totalSize = blockCount * blockSize / 1024L / 1024L;
		long availSize = availCount * blockSize / 1024L / 1024L;
		return availSize + "MB/" + totalSize + "MB";
	}

	public int getMaxPixelForCameraId(int cameraId){
		Camera camera = null;
		try {
			camera = Camera.open(cameraId);
			Parameters param = camera.getParameters();
			List<Size> list = param.getSupportedPictureSizes();
			final int LEN = list.size();
			Size max = list.get(0);
			for(int i=1; i<LEN; i++){
				Size size = list.get(i);
				if(size.width > max.width && size.height > max.height){
					max = size;
				}
			}
			return max.width * max.height / 10000;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(camera != null){
				camera.release();
			}
		}
		return 0;
	}
	
	private void setCameraValue() {
		int numberOfCameras = Camera.getNumberOfCameras();
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		
		for (int i = 0; i < numberOfCameras; i++) {
			if(i == Camera.CameraInfo.CAMERA_FACING_BACK){
				setValue(R.id.main_camera_val, getMaxPixelForCameraId(i)+"万像素");	
			}else if(i == Camera.CameraInfo.CAMERA_FACING_FRONT){
				setValue(R.id.sub_camera_val, getMaxPixelForCameraId(i)+"万像素");	
			}
		}
	}
	
	private String getIMSI(){
		TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		return String.valueOf(tm.getSimSerialNumber());
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bt_ok:
			Utils.SetPreferences(this, mSp, R.string.device_info, "success");
			finish();

			break;
		case R.id.bt_failed:
			Utils.SetPreferences(this, mSp, R.string.device_info, "failed");
			finish();
			break;

		default:
			break;
		}
	}

	class DemoRenderer implements GLSurfaceView.Renderer {

		public void onSurfaceCreated(GL10 gl,
				javax.microedition.khronos.egl.EGLConfig config) {
			String GlRender = gl.glGetString(GL10.GL_RENDERER);
			String GlVendor = gl.glGetString(GL10.GL_VENDOR);
			String GlVersion = gl.glGetString(GL10.GL_VERSION);
			String GlExtension = gl.glGetString(GL10.GL_EXTENSIONS);

			Log.d("SystemInfo", "GL_RENDERER = " + GlRender);
			Log.d("SystemInfo", "GL_VENDOR = " + GlVendor);
			Log.d("SystemInfo", "GL_VERSION = " + GlVersion);
			Log.i("SystemInfo", "GL_EXTENSIONS = " + GlExtension);

			Message msg = mHandler.obtainMessage();
			msg.what = MSG_SET_GPU;
			msg.obj = GlRender;
			mHandler.sendMessage(msg);
		}

		@Override
		public void onDrawFrame(GL10 arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSurfaceChanged(GL10 arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub

		}

	}

	class DemoGLSurfaceView extends GLSurfaceView {

		DemoRenderer mRenderer;

		public DemoGLSurfaceView(Context context) {
			super(context);
			setEGLConfigChooser(8, 8, 8, 8, 0, 0);
			mRenderer = new DemoRenderer();
			setRenderer(mRenderer);
		}
	}
}
