package com.mediatek.factorymode.touchscreen;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestUtils;
import com.mediatek.factorymode.Utils;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.content.SharedPreferences;
import android.content.DialogInterface;

public class TouchPadTest extends Activity {
    private float mAverageHeight;
    private float mAverageWidth;
    private Handler mTouchHandler;
    private Runnable mTouchRunanable, mRestartRunnable;
    private boolean mIsRestart;
    private static final String TAG = "TouchPadTest";
    private static final int RIGHT_MESSAGE = 0;
    private static final int WRONG_MESSAGE = 1;
    private static final int RESTART_MESSAGE = 2;
    SharedPreferences mSp;

    private class TouchHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case RIGHT_MESSAGE : {
                    Log.d(TAG, TAG +"   rightpress handle message");
                    mTouchHandler.removeCallbacks(mTouchRunanable);
                    //TestUtils.rightPress(TAG, TouchPadTest.this);
			touchtestresult();
                    break;
                }
                case WRONG_MESSAGE : {
                    Log.d(TAG, TAG +"   wrongpress handle message");
                    //TestUtils.wrongPress(TAG, TouchPadTest.this);
			touchtestresult();
                    break;
                }
            }
        }
    }
    protected void touchtestresult(){
                      new AlertDialog.Builder(this).setTitle(R.string.TouchScreen)
                      .setPositiveButton(R.string.Success, new DialogInterface.OnClickListener(){
                          public void onClick(DialogInterface dialog, int which) {
                              // TODO Auto-generated method stub
                              Utils.SetPreferences(TouchPadTest.this, TouchPadTest.this.mSp, R.string.touchscreen_name, 	"success");
                              TouchPadTest.this.finish();
                          }
                      })
                      .setNegativeButton(R.string.Failed, new DialogInterface.OnClickListener(){
                          public void onClick(DialogInterface dialog, int which) {
                              // TODO Auto-generated method stub
                              Utils.SetPreferences(TouchPadTest.this, TouchPadTest.this.mSp, R.string.touchscreen_name, 	"failed");
                              TouchPadTest.this.finish();
                          }
                      })
                      .create()
                      .show();
} 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gionee xiaolin 20120921 add for CR00693542 start
        //TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120921 add for CR00693542 end
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
       // lp.dispatchAllKey = 1;
	this.mSp = getSharedPreferences("FactoryMode", 0);
        getWindow().setAttributes(lp);

        setContentView(new TouchPadView(this));
        mTouchHandler = new TouchHandler();
        mTouchRunanable = new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Log.d(TAG, TAG +"   wrongpress run");
                mTouchHandler.sendEmptyMessage(WRONG_MESSAGE);
            }

        };
        mTouchHandler.postDelayed(mTouchRunanable, 60000);
    }
    
    public class TouchPadView extends View {      
        
        private float mX, mY;
        private Path  mPath;
        private Canvas  mCanvas;
        private Bitmap mBitmap;
        private Paint mBitmapPaint;
        private Paint mBackGroudPaint, mLinePaint, mPaint;
        private RectF mRf = new RectF();
        private float[] mVertBaseline = new float[16];
        private float[] mHorBaseline = new float[11];
        private static final float TOUCH_TOLERANCE = 4;
        private ArrayList mLeftList = new ArrayList<Integer>();
        private ArrayList mRightList = new ArrayList<Integer>();
        private ArrayList mTopList = new ArrayList<Integer>();
        private ArrayList mBottomList = new ArrayList<Integer>();

        public TouchPadView(Context context) {
            super(context);
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            mBackGroudPaint = new Paint();
            mBackGroudPaint.setColor(R.color.test_blue);

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(Color.RED);
            mPaint.setStyle(Style.STROKE);
            
            mLinePaint = new Paint();
            mLinePaint.setColor(Color.BLACK);
            // TODO Auto-generated constructor stub
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            if (null == mBitmap) {            
                mBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBitmap);
            }
            canvas.drawColor(Color.WHITE);
            if (0 == mAverageHeight) {
                mAverageWidth = getMeasuredWidth()/10;
                mAverageHeight = getMeasuredHeight()/15;
                for(int i=0; i<14; i++) {
                    mVertBaseline[i] = i * mAverageHeight;
                }
                mVertBaseline[14] = getMeasuredHeight() - mAverageHeight;
                mVertBaseline[15] = getMeasuredHeight();
                
                for(int i=0; i<9; i++) {
                    mHorBaseline[i] = i * mAverageWidth;
                }
                mHorBaseline[9] = getMeasuredWidth() - mAverageWidth;
                mHorBaseline[10] = getMeasuredWidth();
            }
            canvas.drawLine(0, mAverageHeight, getMeasuredWidth(), mAverageHeight, mLinePaint);
            for (int i=0; i<12; i++) {
                canvas.drawLine(0, mVertBaseline[i+2], mAverageWidth, mVertBaseline[i+2], mLinePaint);
                canvas.drawLine(getMeasuredWidth()-mAverageWidth, mVertBaseline[i+2], getMeasuredWidth(), mVertBaseline[i+2], mLinePaint);
            }
            canvas.drawLine(0, getMeasuredHeight() - mAverageHeight, getMeasuredWidth(), getMeasuredHeight() - mAverageHeight, mLinePaint);
            
            canvas.drawLine(mAverageWidth, 0, mAverageWidth, getMeasuredHeight(), mLinePaint);
            for (int i=0; i<7; i++) {
                canvas.drawLine(mHorBaseline[i+2], 0, mHorBaseline[i+2], mAverageHeight, mLinePaint);
                canvas.drawLine(mHorBaseline[i+2], getMeasuredHeight() - mAverageHeight,  mHorBaseline[i+2], getMeasuredHeight(), mLinePaint);
            }
            canvas.drawLine(getMeasuredWidth() - mAverageWidth, 0, getMeasuredWidth() -mAverageWidth, getMeasuredHeight(), mLinePaint);
            
            if (false == mLeftList.isEmpty()) {
                for (int i = 0; i < mLeftList.size(); i++) {
                    mRf.set(0, mVertBaseline[((Integer) mLeftList.get(i)).intValue()],
                            mAverageWidth,
                            mVertBaseline[((Integer) mLeftList.get(i)).intValue() + 1]);
                    canvas.drawRect(mRf, mBackGroudPaint);
                }
            }
            
            if (false == mRightList.isEmpty()) {
                for (int i = 0; i < mRightList.size(); i++) {
                    mRf.set(getMeasuredWidth() - mAverageWidth, mVertBaseline[((Integer) mRightList.get(i)).intValue()],
                            getMeasuredWidth(),
                            mVertBaseline[((Integer) mRightList.get(i)).intValue() + 1]);
                    canvas.drawRect(mRf, mBackGroudPaint);
                }
            }
            
            if (false == mTopList.isEmpty()) {
                for (int i = 0; i < mTopList.size(); i++) {
                    mRf.set(mHorBaseline[((Integer) mTopList.get(i)).intValue()], 0,
                            mHorBaseline[((Integer) mTopList.get(i)).intValue() + 1],
                            mAverageHeight);
                    canvas.drawRect(mRf, mBackGroudPaint);
                }
            }
            
            if (false == mBottomList.isEmpty()) {
                for (int i = 0; i < mBottomList.size(); i++) {
                    mRf.set(mHorBaseline[((Integer) mBottomList.get(i)).intValue()], getMeasuredHeight() - mAverageHeight,
                            mHorBaseline[((Integer) mBottomList.get(i)).intValue() + 1],
                            getMeasuredHeight());
                    canvas.drawRect(mRf, mBackGroudPaint);
                }
            }
            
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            
            canvas.drawPath(mPath, mPaint);
        }

        
        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }
        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;
            }
        }
        private void touch_up() {
            if (null != mCanvas) {
                mPath.lineTo(mX, mY);
                mCanvas.drawPath(mPath, mPaint);
                // kill this so we don't double draw
                mPath.reset();
            }
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            // Gionee xiaolin 20121018 modify for CR00715724 start
            if (x < mAverageWidth) {
                int i = gnBinarySearch(y, mVertBaseline, 0, 14);
                if (-1 != i && false == mLeftList.contains(i)) {
                    mLeftList.add(i);
                    Log.e("lich", "mLeftList.add(i) = " + i);
                }
            } else if(x > mHorBaseline[9]) {
                int i = gnBinarySearch(y, mVertBaseline, 0, 14);
                if (-1 != i && false == mRightList.contains(i)) {
                    mRightList.add(i);
                }
            } else if (y < mAverageHeight) {
                int i = gnBinarySearch(x, mHorBaseline, 0, 9);
                if (-1 != i && false == mTopList.contains(i) && i != 9 && i != 0) {
                    mTopList.add(i);
                }
            } else if (y > mVertBaseline[14]) {
                int i;
                    i = gnBinarySearch(x, mHorBaseline, 0, 9);
                if (-1 != i && false == mBottomList.contains(i) && i != 9 && i != 0) {
                    mBottomList.add(i);
                }
            }
            // Gionee xiaolin 20121018 modify for CR00715724 end
            
            
            
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();            
                    if (46 == mLeftList.size() + mRightList.size() + mTopList.size() + mBottomList.size()) {
                        mTouchHandler.sendEmptyMessage(RIGHT_MESSAGE);
                    }
                    break;
            }
            return true;
        }
        
        public int gnBinarySearch(float elem,float[] array,int low,int high) {
            for (int i=0; i<array.length-1; i++) {
                if (elem >= array[i] && elem < array[i+1]) {
                    return i;
                }
            }
            // Gionee xiaolin 20121018 modify for CR00715724 start
            return -1;
            // Gionee xiaolin 20121018 modify for CR00715724 end
        }
        
    }

    @Override
    public boolean dispatchKeyEvent (KeyEvent event) {
        
        return true;
    }
}
