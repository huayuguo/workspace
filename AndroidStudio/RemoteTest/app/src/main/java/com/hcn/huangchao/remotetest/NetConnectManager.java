package com.hcn.huangchao.remotetest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class NetConnectManager {
    private static final String TAG = "NetConnectManager";
    private static NetConnectManager mNetConnectManager;
    private static INetConnectInterface mService = null;
    private static Context mContext = null;
    private static WeakReference<MyServiceConnecton> myServiceConnection;

    public static synchronized NetConnectManager getInstance(){
        if (null == mNetConnectManager){
            synchronized (NetConnectManager.class) {
                if (null == mNetConnectManager){
                    mNetConnectManager = new NetConnectManager();
                }
            }
        }
        return mNetConnectManager;
    }

    private NetConnectManager(){
        Log.d(TAG, "NetConnectManager...begin");
    }

    public void init(Context context) {
        if(mContext == null) {
            mContext = context;
            context.bindService(new Intent(context, NetConnectService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
            //Intent service = new Intent(mContext, NetConnectService.class);
            //mContext.startService(service);
        }
    }

    public void uninit(Context context) {
        context.unbindService(mServiceConnection);
    }

    public void setMyServiceConnecton(MyServiceConnecton connection) {
        myServiceConnection = new WeakReference<MyServiceConnecton>(connection);
        if(mService != null)
            connection.onServiceConnected(null,null);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName pComponentName, IBinder pIBinder) {
            Log.d(TAG, "onServiceConnected");
            mService =  INetConnectInterface.Stub.asInterface((IBinder) pIBinder);
            //mCallBack = new LoginCallBack(listeners);
            try {
                mService.asBinder().linkToDeath(new AidlLinkToDeath(), 0);
            } catch (RemoteException e) {
                e.printStackTrace();
                mService = null;
            }
            MyServiceConnecton connection = null;
            if(myServiceConnection != null)
                connection = myServiceConnection.get();
            if (connection != null) {
                connection.onServiceConnected(pComponentName,pIBinder);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName pComponentName) {
            Log.d(TAG, "onServiceDisconnected");
            MyServiceConnecton connection = null;
            if(myServiceConnection != null)
                connection = myServiceConnection.get();
            if (connection != null) {
                connection.onServiceDisconnected(pComponentName);
            }
        }
    };

    /**
     * 监控aidl连接异常，即binder异常
     */
    private class AidlLinkToDeath implements IBinder.DeathRecipient {

        @Override
        public void binderDied() {
            Log.e(TAG, "binderDied, aidl connect exception.");
            mService = null;
        }
    }

    public void registerUser(String name, String password){
        Log.d(TAG, "registerUser");
        if (null == mService){
            Log.e(TAG, "registerUser mService is null!");
            return ;
        }
        try{
            mService.registerUser(name, password);
        }catch(RemoteException e){
            e.printStackTrace();
        }
    }

    public void loginServer(){
        Log.d(TAG, "loginServer");
        if (null == mService){
            Log.e(TAG, "loginServer mService is null!");
            return ;
        }
        try{
            mService.loginServer();
        }catch(RemoteException e){
            e.printStackTrace();
        }
    }

    public void requestServerData(long millis){
        Log.d(TAG, "requestServerData");
        if (null == mService){
            Log.e(TAG, "requestServerData mService is null!");
            return ;
        }
        try{
            mService.requestServerData(millis);
        }catch(RemoteException e){
            e.printStackTrace();
        }
    }

    public void removeServerData(){
        Log.d(TAG, "removeServerData");
        if (null == mService){
            Log.e(TAG, "removeServerData mService is null!");
            return ;
        }
        try{
            mService.removeServerData();
        }catch(RemoteException e){
            e.printStackTrace();
        }
    }

    private ListenerTransport wrapListener(NetConnectChangedListener listener, Looper looper) {
        if (listener == null) return null;
        synchronized (mListeners) {
            ListenerTransport transport = mListeners.get(listener);
            if (transport == null) {
                transport = new ListenerTransport(listener, looper);
            }
            mListeners.put(listener, transport);
            return transport;
        }
    }

    private HashMap<NetConnectChangedListener,ListenerTransport> mListeners =
            new HashMap<NetConnectChangedListener,ListenerTransport>();

    private class ListenerTransport extends ILoginCallBackInterface.Stub {
        private static final int TYPE_STATUS_CHANGED = 1;
        private static final int TYPE_DATA_CHANGED = 2;

        private NetConnectChangedListener mListener;
        private final Handler mListenerHandler;

        ListenerTransport(NetConnectChangedListener listener, Looper looper) {
            mListener = listener;

            if (looper == null) {
                mListenerHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        _handleMessage(msg);
                    }
                };
            } else {
                mListenerHandler = new Handler(looper) {
                    @Override
                    public void handleMessage(Message msg) {
                        _handleMessage(msg);
                    }
                };
            }
        }

        @Override
        public void onNodeInfoChange(String param) throws RemoteException {
            Log.d(TAG, "onNodeInfoChange:" + param);
            Message msg = Message.obtain();
            msg.what = TYPE_DATA_CHANGED;
            Bundle b = new Bundle();
            b.putString("data", param);
            msg.obj = b;
            mListenerHandler.sendMessage(msg);
        }

        @Override
        public void onLoginStateChange(int status) {
            Log.d(TAG, "onLoginStateChange");
            Message msg = Message.obtain();
            msg.what = TYPE_STATUS_CHANGED;
            Bundle b = new Bundle();
            b.putInt("status", status);
            msg.obj = b;
            mListenerHandler.sendMessage(msg);
        }

        private void _handleMessage(Message msg) {
            Bundle b;
            switch (msg.what) {
                case TYPE_STATUS_CHANGED:
                    b = (Bundle) msg.obj;
                    int status = b.getInt("status");
                    mListener.onLoginStateChange(status);
                    break;
                case TYPE_DATA_CHANGED:
                    b = (Bundle) msg.obj;
                    String data = b.getString("data");
                    Log.d(TAG, data);
                    mListener.onNodeInfoChange(data);
                    break;
            }
            /*
            try {
                mService.locationCallbackFinished(this);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }*/
        }
    }

    public int registerNetConnectChangedListener(NetConnectChangedListener listener) {
        checkListener(listener);
        return registerNetConnectChangedListener(listener, null);
    }

    private int registerNetConnectChangedListener(NetConnectChangedListener listener, Looper looper) {
        //String packageName = mContext.getPackageName();
        if (null == mService){
            Log.e(TAG, "registerNetConnectChangedListener mService is null!");
            return -1;
        }

        // wrap the listener class
        ListenerTransport transport = wrapListener(listener, looper);

        try {
            mService.registerCallBack(transport);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -2;
        }
        return 0;
    }

    private static void checkListener(NetConnectChangedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("invalid listener: " + listener);
        }
    }

    public void unRegisterNetConnectChangedListener(NetConnectChangedListener listener) {
        checkListener(listener);
        //String packageName = mContext.getPackageName();

        if (null == mService){
            Log.e(TAG, "unRegisterNetConnectChangedListener mService is null!");
            return ;
        }

        ListenerTransport transport;
        synchronized (mListeners) {
            transport = mListeners.remove(listener);
        }
        if (transport == null) return;

        try {
            mService.unRegisterCallBack(transport);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
