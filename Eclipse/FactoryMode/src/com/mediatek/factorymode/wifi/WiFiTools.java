package com.mediatek.factorymode.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
//import dalvik.annotation.Signature;
import java.util.BitSet;
import java.util.List;
import com.mediatek.factorymode.R;
public class WiFiTools
{
  public static final int WIFI_STATE_DISABLED = 1;
  public static final int WIFI_STATE_DISABLING = 0;
  public static final int WIFI_STATE_ENABLED = 3;
  public static final int WIFI_STATE_ENABLING = 2;
  static boolean mResult;
  private static WifiManager mWifiManager = null;
  Context context;
  String info = "";
  WifiInfo wifiinfo;
  String strengthinfo = "";

  static
  {
    boolean bool = mResult;
  }

  public WiFiTools(Context paramContext)
  {
    this.context = paramContext;
    mWifiManager = (WifiManager)paramContext.getSystemService("wifi");
    WifiInfo localWifiInfo = mWifiManager.getConnectionInfo();
    this.wifiinfo = localWifiInfo;
    mWifiManager.startScan();
 
    


  }
  
  public String Getsinglestrength()
  {
	  //if(mWifiManager.getWifiState()==WIFI_STATE_ENABLED)
	  {
		  GetWifiInfo();
			StringBuilder StringBuilder1 = new StringBuilder()
			.append(R.string.WiFi_strength).append(wifiinfo.getRssi())
			.append("\n");
         strengthinfo = StringBuilder1.toString();	  
	  }

	  
	  return strengthinfo;
	//  if(mWifiManager.getWifiState()==WIFI_STATE_ENABLED)
	 //return  wifiinfo.getRssi();
 
	  
	  
  }
  
  

  public String GetState()
  {
      //return "";
   // int i = 2131230778;WiFi_info_open
    String str1 = null;
    if (!mWifiManager.isWifiEnabled())
    {
      mWifiManager.setWifiEnabled(true);}
      switch (mWifiManager.getWifiState())
      {
      case 0://WIFI_STATE_DISABLING
    	  str1 = this.context.getString(R.string.WiFi_info_closeing);
    	  break;
      case 1://WIFI_STATE_DISABLED
    	  str1 = this.context.getString(R.string.WiFi_info_close);
    	  break;
      case 2:
    	  str1 = this.context.getString(R.string.WiFi_info_opening);
    	  break;
      case 3://enabled
    	  str1 = this.context.getString(R.string.WiFi_info_open);
    	  break;
      default:
          str1 = this.context.getString(R.string.WiFi_info_unknown);
          break;
      }
    
    this.info = str1 ;
    return this.info;
        
  }

  public WifiInfo GetWifiInfo()
  {
    WifiInfo localWifiInfo = mWifiManager.getConnectionInfo();
    this.wifiinfo = localWifiInfo;
    return this.wifiinfo;
  }

  public Boolean IsConnection()
  {
      boolean iscon = false;
   // int i = 1;//ConnectivityManager.TYPE_WIFI
    ConnectivityManager mCm = (ConnectivityManager)this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = mCm.getNetworkInfo(1);
    NetworkInfo.State localObject = networkInfo.getState();
    //Object localObject = ((ConnectivityManager)this.context.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
    NetworkInfo.State localState = NetworkInfo.State.CONNECTED;
    if (localObject == localState);
       iscon = true ;
    return iscon;

    //for (localObject = Boolean.valueOf(i); ; localObject = Boolean.valueOf(null))
    //  return localObject;
  }

  //@Signature({"(", "Ljava/util/List", "<", "Landroid/net/wifi/ScanResult;", ">;", "Landroid/net/wifi/ScanResult;", "Ljava/lang/String;", ")Z"})
  public boolean addWifiConfig(List paramList, ScanResult paramScanResult, String paramString)
  {
    WifiConfiguration localWifiConfiguration = new WifiConfiguration();
    StringBuilder localStringBuilder = new StringBuilder().append(paramScanResult.SSID).append("\"");
    //String str1 = paramScanResult.SSID;
    //String str2 = str1 + "\"";
    localWifiConfiguration.SSID = localStringBuilder.toString();
    localWifiConfiguration.allowedKeyManagement.set(0);
    localWifiConfiguration.status = 2;
    int i = mWifiManager.addNetwork(localWifiConfiguration);
    localWifiConfiguration.networkId = i;
    WifiManager localWifiManager = mWifiManager;
    int j = localWifiConfiguration.networkId;
    return localWifiManager.enableNetwork(j, true);
  }

  public void closeWifi()
  {
    //if (!mWifiManager.isWifiEnabled())
     // return;
  if(mWifiManager.isWifiEnabled()&&(mWifiManager.getWifiState()==
	         WifiManager.WIFI_STATE_ENABLED))
    mWifiManager.setWifiEnabled(false);
  }

  public boolean openWifi()
  {
   /* boolean bool1 = true;
    boolean bool2 = mWifiManager.isWifiEnabled();
    if (!bool2);
    for (bool2 = mWifiManager.setWifiEnabled(bool1); ; bool2 = bool1)
      return bool2;*/
    if(mWifiManager.isWifiEnabled())
    {    	
    	 return true;
    }
    else{
         mWifiManager.setWifiEnabled(true);
    return false;}
  }

  //@Signature({"()", "Ljava/util/List", "<", "Landroid/net/wifi/ScanResult;", ">;"})
  public List scanWifi()
  {
    return mWifiManager.getScanResults();
  }
}