package com.camera.simplewebcam;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.ElementList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main extends Activity {

    private static final String TAG = "WebCam";
	
	CameraPreview cp;

    private AlertDialog mErrorDialog;

    Button mGalleryButton;
	Button mCaptureButton;
	Button mPrintButton;
	Button mAdviceButton;
	
	private String part1 = "<!DOCTYPE html>" +
			"<html xmlns=\"http://www.www.sz19zn.com\">" +
			"<head>" +
			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />" +
			"</head>" +
			"<body>" +
			"<div style=\"font-weight: bold;\" align=\"center\">";
	private String part2 = "<br/>数码电子阴道镜影像报告单</div>" +
			"<div style=\"font-weight: bold;\" align=\"right\">编号：";
	private String part3 = "</div><br/><hr/><br/>" +
			"<table width=\"100%\" border=\"0\">" +
			"<col align=\"left\"/>" +
			"<col align=\"left\"/>" +
			"<col align=\"left\"/>" +
			"<tr><td>姓名：";
	private String part4 = "</td><td >性别：女";
	private String part5 = "</td><td >年龄：";
	private String part6 = "</td></tr><tr><td>住院号：";
	private String part7 = "</td><td>门诊号：";
	private String part8 = "</td><td>科室：";
	private String part9 = "</td></tr></table><br/><hr/><br/>" +	
			"<div style=\"font-weight: bold;\">电子影像：</div>" +
			"<br/><p class=\"img_wrap\"><img src=\"";
	private String part10 = "\" width=\"400px\" height=\"300px\" align=\"middle\"/>" +
			"</p><br/><hr/><br/><div style=\"font-weight: bold;\">诊断结果：</div>";
	private String part11 = "<br/><br/><br/><table style=\"width:100%;\" border=\"0\">" +
			"<tr><td align=\"left\">报告医生：";
    private String part12 = "</td><td align=\"right\">报告日期：";
	private String part13 = "</td></tr></table><br/><hr/><br/>" +
			"<div style=\"font-size:12px;\">此报告仅供临床医生参考。</div></body></html>";
	private String mHospital = "湖北省人民医院";
	private String mNumber;
	private String mName;
	private String mAge;
	private String mHospitalNo;
	private String mPatientNo;
	private String mDepartments;
	private String mDate;
	private String mAttitude;
	private String mProject;
	private String mAdvice;
    private String mDoctor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate");
		Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //window.setFlags(flag, flag);
		setContentView(R.layout.main);
		
		InputStream checkbox = getResources().openRawResource(R.raw.checkbox);
		copyFile(checkbox, getFilesDir() + "/checkbox.png");
		InputStream simsun = getResources().openRawResource(R.raw.simsun);
		copyFile(simsun, getFilesDir() + "/simsun.ttf");
		
		//UVCJni.setImagePath(getCacheDir() + "/");
		
		cp = (CameraPreview) findViewById(R.id.cp);
        cp.setMainActivity(this);
        
        mGalleryButton = (Button)findViewById(R.id.galleryButton);
        mGalleryButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Goto Gallery");
				//Intent intentImage = new Intent(Intent.ACTION_VIEW);
				//intentImage.addCategory(Intent.CATEGORY_DEFAULT);
				//File file = new File(Environment.getExternalStorageDirectory() + "/DICM/tmp.jpg");
				//intentImage.setDataAndType(Uri.fromFile(file), "image/*");
				//startActivity(intentImage);
				
/*				Intent intent = new Intent(Intent.ACTION_VIEW);
				String filePath = Environment.getExternalStorageDirectory() + "/DCIM";
				File file = new File(filePath);
				Uri uri;
				if (Build.VERSION.SDK_INT >= 24) {				    
				    Log.i("mine",file.length()+"");
				    uri = FileProvider.getUriForFile(Main.this, getApplicationContext().getPackageName() + ".fileprovider", file);
				    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//注意加上这句话
				    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

				} else {
				    uri = Uri.fromFile(file);
				}
				Log.i("mine", "Uri:" + uri);
				intent.setDataAndType(uri, "image/*");
				startActivity(intent);*/
				
				Intent intent = new Intent(Intent.ACTION_VIEW, null);
				intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null);
				startActivity(intent);
				
				String capture = (String) getResources().getString(R.string.capture);
				mCaptureButton.setText(capture);
				cp.startCapture = 0;
			}
			
		});	
        
		mCaptureButton = (Button)findViewById(R.id.captureButton);
		mCaptureButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				String capture = (String) getResources().getString(R.string.capture);
				String watch = (String) getResources().getString(R.string.watch);
				if(mCaptureButton.getText().equals(capture)) {
					// TODO Auto-generated method stub
					Log.d(TAG, "Call captureImage()");
					mCaptureButton.setText(watch);
					//UVCJni.setImagePath(getCacheDir() + "/");
	                //UVCJni.captureImage();
	                cp.imageName = mName;
					cp.imagePATH = Environment.getExternalStorageDirectory() + "/DCIM/Camera";
					cp.startCapture = 1;
	                //mPrintButton.setEnabled(true);
				} else {
					Log.d(TAG, "Call stopCaptureImage()");
					mCaptureButton.setText(capture);
					//UVCJni.stopCaptureImage();
					cp.startCapture = 0;
					//mPrintButton.setEnabled(false);
				}
			}
			
		});	
        
        mPrintButton = (Button)findViewById(R.id.printButton);
        mPrintButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG, "Print Report");
				createReport();
				doPdfPrint(getCacheDir() + "/demo.pdf");
			}
		});
        
        mAdviceButton = (Button)findViewById(R.id.adviceButton);
        mAdviceButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG, "Doctor Advice");
	            Intent intent = new Intent();
	            intent.setClass(Main.this, AdviceInputer.class);
	            startActivity(intent);

				String capture = (String) getResources().getString(R.string.capture);
				mCaptureButton.setText(capture);
				cp.startCapture = 0;
			}
		});
		
        buildErrorDialog();
	}

    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		Log.d(TAG, "onResume");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mNumber = prefs.getString("number", "");;
        mName = prefs.getString("name", "TMP");
        mAge = prefs.getString("age", "");
        mHospitalNo = prefs.getString("hospital_no", "");
        mPatientNo = prefs.getString("patient_no", "");
        mDepartments = prefs.getString("departments", "");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
		Date date = new Date(System.currentTimeMillis());
		mDate = simpleDateFormat.format(date);
        mAdvice = prefs.getString("advice", "");
        mDoctor = prefs.getString("doctor", "");
        //Toast.makeText(this,"onResume: " + prefs.getString("name", "unknow"), Toast.LENGTH_LONG).show();
        Log.i("SimpleWebCam", "name = " + prefs.getString("name", "unknow"));
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		Log.d(TAG, "onPause");
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
    
    private void doPdfPrint(String filePath) {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        MyPrintPdfAdapter myPrintAdapter = new MyPrintPdfAdapter(filePath);
        printManager.print("jobName", myPrintAdapter, null);
    }

    public class MyPrintPdfAdapter extends PrintDocumentAdapter {
        private String mFilePath;

        public MyPrintPdfAdapter(String file) {
            this.mFilePath = file;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                             CancellationSignal cancellationSignal,
                             LayoutResultCallback callback, Bundle extras) {
            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }
            PrintDocumentInfo info = new PrintDocumentInfo.Builder("name")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build();
            callback.onLayoutFinished(info, true);
        }

        @Override
        public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                            CancellationSignal cancellationSignal,
                            WriteResultCallback callback) {
        	FileInputStream input = null;
            FileOutputStream output = null;

            try {
                input = new FileInputStream(mFilePath);
                output = new FileOutputStream(destination.getFileDescriptor());

                byte[] buf = new byte[1024];
                int bytesRead;

                while ((bytesRead = input.read(buf)) > 0) {
                    output.write(buf, 0, bytesRead);
                }

                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			super.onFinish();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Main.this);
            if(prefs != null){
            	SharedPreferences.Editor editor = prefs.edit();
				//editor.remove("name");
				//editor.remove("age");
				//editor.remove("hospital_no");
				//editor.remove("patient_no");
                editor.putString("name", "");
                editor.putString("age", "");
                editor.putString("hospital_no", "");
                editor.putString("patient_no", "");
                editor.commit();
            }
			Intent intent = new Intent();
            intent.setClass(Main.this, PatientInfo.class);
            startActivity(intent);
            //Toast.makeText(MyPrintPdfAdapter.this,"onFinish",Toast.LENGTH_LONG).show();
		}
        
    }
    
    private void createHtml(Document document) {
        try {
            /*InputStream inputStream = null;
            try {
            	inputStream = this.getAssets().open("demo.html");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            StringBuilder sb = new StringBuilder();
            String line;

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String pocketDescription = sb.toString();*/
        	String pocketDescription = part1 + mHospital + part2 + mNumber + part3 + mName + part4 +
            		part5 + mAge + part6 + mHospitalNo + part7 + mPatientNo + part8 + mDepartments +
            		part9 + getCacheDir() + "/tmp.jpg" + part10 + mAdvice + part11 + mDoctor + part12 +
            		mDate + part13;
            
			try {
				pocketDescription = new String(pocketDescription.getBytes("UTF-8"), "UTF-8");
				InputStream in_nocode = new ByteArrayInputStream(pocketDescription.getBytes());
	            copyFile(in_nocode, Environment.getExternalStorageDirectory() + "/tmp2.html");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}            
            String css = "hr {height:1px;border-width:0;background-color:black} " +
            		"body { font-size:20px; margin: 72px; }" +
            		".img_wrap { width: 480px;height: 270px;border: 1px dashed #ccc;display: table-cell;vertical-align: middle;text-align: center;}";
            Paragraph context = new Paragraph();
            MyXMLWorkerHelper.setFontPath(getFilesDir() + "/simsun.ttf");
            ElementList elementList = MyXMLWorkerHelper.parseToElementList(pocketDescription, css);
            for (Element element : elementList) {
                context.add(element);
            }
            document.add(context);
        }
        catch (Exception e){
        	e.printStackTrace();  
        }
    }
    
    void createReport() {
		try {
    		Document document = new Document(PageSize.A4, 72, 72, 72,72);
    		PdfWriter.getInstance(document, new FileOutputStream(getCacheDir()+ "/demo.pdf"));      		
    		document.open();
    		createHtml(document);
    		document.close();  
		} catch (Exception e) {
            e.printStackTrace();  
    	}
    }
    
    static boolean copyFile(InputStream fis,String des){
    	boolean result=true;
    	FileOutputStream fos=null;
    	
    	try{
    		File fout=new File(des);
    		if(!fout.exists()){
    			fos=new FileOutputStream(fout);
    			byte[] b=new byte[1024];
    			int len;
    			while((len=fis.read(b))!=-1){
    				fos.write(b, 0, len);
    			}
    		}
    	}catch(IOException e){
    		e.printStackTrace();
    		result=false;
    	}finally{
    		if(fos!=null){
    			try{
    				fos.close();
    			}catch(IOException e){
    				e.printStackTrace();
    				result=false;
    			}
    		}
    		if(fis!=null){
    			try{
    				fis.close();
    			}catch(IOException e){
    				e.printStackTrace();
    				result=false;
    			}
    		}
    	}
    	return result;
    }
}
