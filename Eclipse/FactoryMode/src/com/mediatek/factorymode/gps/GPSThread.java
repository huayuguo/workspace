package com.mediatek.factorymode.gps;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class GPSThread
{
  private Context mContext;
  private GPSUtil mGpsUtil;
  private Handler mHandler;
  private boolean mIsSuccess = false;

  class GPSThread1 extends Handler
  {
    public void handleMessage(Message paramMessage)
    {
     switch (paramMessage.what)
      {
      default:
      case 0:
      }
      start();
      /*while (true)
      {
        return;
        GPSThread.access$000(this.this$0);
      }*/
    }
  }
  
  public GPSThread(Context paramContext)
  {
    GPSThread1 local1 = new GPSThread1();
    this.mHandler = local1;
    this.mContext = paramContext;
    Context localContext = this.mContext;
    GPSUtil localGpsUtil = new GPSUtil(localContext);
    this.mGpsUtil = localGpsUtil;
  }

  private void getSatelliteInfo()
  {
    int i = 0;
    if (this.mGpsUtil.getSatelliteNumber() > 2)
    {
      this.mHandler.removeMessages(i);
      this.mIsSuccess = true;
      closeLocation();
    }
    else
    this.mHandler.sendEmptyMessageDelayed(i, 2000L);
    /*while (true)
    {
      return;
      this.mHandler.sendEmptyMessageDelayed(i, 2000L);
    }*/
  }

  public void closeLocation()
  {
    this.mGpsUtil.closeLocation();
  }

  public int getSatelliteNum()
  {
    return this.mGpsUtil.getSatelliteNumber();
  }

  public String getSatelliteSignals()
  {
    return this.mGpsUtil.getSatelliteSignals();
  }

  public boolean isSuccess()
  {
    return this.mIsSuccess;
  }

  public void start()
  {
    getSatelliteInfo();
  }
}