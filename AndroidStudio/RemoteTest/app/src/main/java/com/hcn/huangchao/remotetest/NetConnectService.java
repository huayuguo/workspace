package com.hcn.huangchao.remotetest;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class NetConnectService extends Service {
    private static final String TAG = "NetConnectService";
    private static NetConnectInterfaceImpl mBinder;
    private Map<String,String> sessionId;
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        sessionId = new HashMap<String, String>();
        if (null == mBinder){
            initService();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (null == mBinder){
            initService();
        }
        return START_STICKY;
    }

    private class NetConnectInterfaceImpl extends INetConnectInterface.Stub {
        protected RemoteCallbackList<ILoginCallBackInterface> mRemoteCallbackList = new RemoteCallbackList<ILoginCallBackInterface>();
        private SharedPreferences userPreferences;
        private String mEmail;
        private String mPassword;
        private PullThread pullThread;

        public NetConnectInterfaceImpl() {
            Log.d(TAG, "NetConnectInterfaceImpl");
            userPreferences= PreferenceManager.getDefaultSharedPreferences(NetConnectService.this);
            if(userPreferences.getBoolean("main",false)) {
                mEmail = userPreferences.getString("user", "");
                mPassword = userPreferences.getString("password", "");
            }
        }

        @Override
        public void registerUser(String name, String password) throws RemoteException {
            Log.d(TAG, "registerUser");
            mEmail = name;
            mPassword = password;
            SharedPreferences.Editor editor = userPreferences.edit();
            editor.putString("user", mEmail);
            editor.putString("password", mPassword);
            editor.putBoolean("main", false);
            editor.commit();
            doLoginServer();
        }

        @Override
        public void loginServer() throws RemoteException {
            Log.d(TAG, "loginServer");
            doLoginServer();
        }

        @Override
        public void requestServerData(long millis) throws RemoteException {
            Log.d(TAG, "requestServerData");
            if(pullThread == null) {
                pullThread = new PullThread();
                pullThread.setUpdateDelay(millis);
                pullThread.start();
            }
        }

        public void removeServerData() throws RemoteException {
            Log.d(TAG, "removeServerData");
            if(pullThread != null) {
                pullThread.shouldExit = true;
                try {
                    pullThread.join();
                    pullThread = null;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        void doLoginServer() {
            Log.d(TAG,"doLoginServer:" + mEmail + "," + mPassword);
            new Thread() {
                @Override
                public void run() {
                    String result = HttpURLConnectionClient.getInstance().httpsPost(
                            "https://112.74.42.29/a7_demo/authentication_phone.php",
                            null,
                            "name=" + mEmail + "&password=" + mPassword + "&type=login");
                    SharedPreferences.Editor editor = userPreferences.edit();
                    Log.d(TAG, result);
                    if(result.startsWith("Error:") || result.isEmpty()) {
                        listener.onLoginStateChange(0);
                        editor.putBoolean("main", false);
                    } else {
                        int pos = result.indexOf('=');
                        if (pos == -1) {
                            return;
                        }
                        String v = result.substring(pos + 1);
                        sessionId.put("Cookie", "PHPSESSID=" + v);
                        //Log.d(TAG, v);
                        listener.onLoginStateChange(1);
                        editor.putBoolean("main", true);
                    }
                    editor.commit();
                }
            }.start();
        }

        //注册回调
        @Override
        public void registerCallBack(ILoginCallBackInterface c)
                throws RemoteException {
            Log.d(TAG, "registerCallBack");
            mRemoteCallbackList.register(c);
        }

        //注销回调
        @Override
        public void unRegisterCallBack(ILoginCallBackInterface c)
                throws RemoteException {
            Log.d(TAG, "unRegisterCallBack");
            mRemoteCallbackList.unregister(c);
        }

        //调用回调方法
        private NetConnectChangedListener listener = new NetConnectChangedListener() {
            @Override
            public void onLoginStateChange(int state) {
                synchronized (mRemoteCallbackList) {
                    int len = mRemoteCallbackList.beginBroadcast();
                    Log.d(TAG, "onLoginStateChange(" + len + ") : " + state);
                    for (int i = 0; i < len; i++) {
                        try {
                            mRemoteCallbackList.getBroadcastItem(i).onLoginStateChange(state);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    mRemoteCallbackList.finishBroadcast();
                }
            }

            @Override
            public void onNodeInfoChange(String param) {
                synchronized (mRemoteCallbackList) {
                    int len = mRemoteCallbackList.beginBroadcast();
                    Log.d(TAG, "onNodeInfoChange(" + len + ") : " + param);
                    for (int i = 0; i <len; i++) {
                        try {
                            mRemoteCallbackList.getBroadcastItem(i).onNodeInfoChange(param);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    mRemoteCallbackList.finishBroadcast();
                }
            }
        };

        class PullThread extends Thread {
            private long millis;
            public volatile boolean shouldExit = false;

            public void setUpdateDelay(long millis) {
                this.millis = millis;
            }

            public void run() {
                do {
                    String result = HttpURLConnectionClient.getInstance().httpsPost(
                            "https://112.74.42.29/a7_demo/node_view.php",
                            sessionId, null);
                    Log.d(TAG, "PullThread: " + result);
                    if (!result.startsWith("node_view_ok")) {
                        Log.d(TAG, "jump to login web");
                        return;
                    }
                    listener.onNodeInfoChange(result);
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } while(!shouldExit);
                shouldExit = false;
            }
        }
    }
    //初始化服务，主要是向系统注册服务
    private void initService(){
        Log.d(TAG, "initService");
        if (null == mBinder){
            mBinder = new NetConnectInterfaceImpl();
        }
    }
}
