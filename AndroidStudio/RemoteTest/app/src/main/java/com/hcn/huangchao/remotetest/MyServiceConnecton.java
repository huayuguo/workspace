package com.hcn.huangchao.remotetest;

import android.content.ComponentName;
import android.os.IBinder;

public interface MyServiceConnecton {
    void onServiceConnected(ComponentName name, IBinder service);
    void onServiceDisconnected(ComponentName name);
}
