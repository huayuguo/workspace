/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.factorymode.touchscreen;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;
import com.mediatek.factorymode.simcard.SimTest;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.*;
import java.util.Vector;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;

public class LineTest extends Activity implements View.OnTouchListener {
  public DiversityCanvas mDiversityCanvas;
  public boolean mRun=false;
  public double mDiversity = 0;
  public Vector<Point> mInput = new Vector<Point>();

  private int mZoom = 1;
  private int mRectWidth;
  private int mRectHeight;
  
  SharedPreferences mSp;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      DisplayMetrics dm = new DisplayMetrics();
      dm = this.getApplicationContext().getResources().getDisplayMetrics();
      this.mSp = getSharedPreferences("FactoryMode", 0);
      
      mRectWidth = dm.widthPixels;
      mRectHeight = dm.heightPixels;
      if((480 == mRectWidth && 800 == mRectHeight) || (800 == mRectWidth && 480 == mRectHeight))
      {
    	  mZoom = 2;
      }

      mDiversityCanvas = new DiversityCanvas((Context)this);
      setContentView(mDiversityCanvas);
      mDiversityCanvas.setOnTouchListener(this);
      Log.i("TVL", "Oncreate");
      
  }
  
  @Override
  public void onResume()
  {
	//  mDiversityCanvas = new DiversityCanvas((Context)this);
	 // setContentView(mDiversityCanvas);
	 // mDiversityCanvas.setOnTouchListener(this);
	  super.onResume();
	  Log.i("TVL", "onResume");
  }
  @Override
   public void onPause()
  {
	  Log.i("TVL", "onPause");	  
	  super.onPause();
	  
  }
 
	  public boolean onTouch(View v, MotionEvent e) {

		  if(MotionEvent.ACTION_DOWN == e.getAction()|| MotionEvent.ACTION_MOVE == e.getAction())
		  {
		      if(v==mDiversityCanvas)
		      {
		          mInput.add(new Point((int)e.getX(),(int)e.getY()));
		      }
		  }
		  else if(MotionEvent.ACTION_UP == e.getAction()){
		      
                      new AlertDialog.Builder(this).setTitle(R.string.TouchScreen)
                      .setPositiveButton(R.string.Success, new DialogInterface.OnClickListener(){
                          public void onClick(DialogInterface dialog, int which) {
                              // TODO Auto-generated method stub
                              Utils.SetPreferences(LineTest.this, LineTest.this.mSp, R.string.touchscreen_name, "success");
                              LineTest.this.finish();
                          }
                      })
                      .setNegativeButton(R.string.Failed, new DialogInterface.OnClickListener(){
                          public void onClick(DialogInterface dialog, int which) {
                              // TODO Auto-generated method stub
                              Utils.SetPreferences(LineTest.this, LineTest.this.mSp, R.string.touchscreen_name, "failed");
                              LineTest.this.finish();
                          }
                      })
                      .create()
                      .show();
                  }
	      return true;
	  }

  
  class DiversityCanvas extends SurfaceView implements SurfaceHolder.Callback {
      DiversityThread mThread = null;
      public DiversityCanvas(Context context) {
          super(context);
          SurfaceHolder holder = getHolder();
          holder.addCallback(this);
      }

      public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
      }
      public void surfaceCreated(SurfaceHolder holder) {
	      Log.i("TVL", "surfaceCreated");
	      mRun=true;
	      
	      mThread = new DiversityThread(holder, null);
          mThread.start();
      }
      public void surfaceDestroyed(SurfaceHolder holder) {
	      mRun=false;
	      Log.i("TVL", "surfaceDestroyed");
      }

      class DiversityThread extends Thread {
          private SurfaceHolder mSurfaceHolder = null;
          private Paint mLinePaint = null;
          private Paint mTextPaint = null;
          private Paint mRectPaint = null;
          private Rect mRect = null;
          private boolean bDraw = false;
          public DiversityThread(SurfaceHolder s, Context c) {
              mSurfaceHolder = s;
              mLinePaint = new Paint();
              mLinePaint.setAntiAlias(true);
              mTextPaint = new Paint();
              mTextPaint.setAntiAlias(true);
              mTextPaint.setTextSize(9.0f * mZoom);
              mTextPaint.setARGB(255,0,0,0);
              mRect = new Rect(0,0,mRectWidth,mRectHeight);
              mRectPaint = new Paint();
              mRectPaint.setARGB(255,255,255,255);
          }
          @Override
          public void run() {
              while(mRun) {
                  Canvas c = null;
                  try {
                      c = mSurfaceHolder.lockCanvas(null);
                      synchronized (mSurfaceHolder) {
                          if(c!=null) doDraw(c);
                      }
                  } finally {
                      if(c!=null) mSurfaceHolder.unlockCanvasAndPost(c);
                  }
              }
          }
          private void doDraw(Canvas canvas) {
              int i;
              Point p1,p2;
              if(!bDraw){
                  canvas.drawRect(mRect,mRectPaint);  
                  mLinePaint.setARGB(255,0,0,255);
                  canvas.drawLine(20,20,mRectWidth - 20, 20,mLinePaint);
                  canvas.drawLine(20,20,20,mRectHeight - 80,mLinePaint);
                  canvas.drawLine(mRectWidth - 20,20,mRectWidth - 20,mRectHeight - 80,mLinePaint);
                  canvas.drawLine(20,mRectHeight - 80,mRectWidth - 20,mRectHeight - 80,mLinePaint);
              }


              mLinePaint.setARGB(255,255,0,0);
              for(i=0;i<mInput.size()-1;i++) 
              {
                  p1 = mInput.get(i);
                  p2 = mInput.get(i+1);
                  canvas.drawLine(p1.x,p1.y,p2.x,p2.y,mLinePaint);
              }
          }
      }
  }
}
