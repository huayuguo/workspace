// ILoginCallBackInterface.aidl
package com.hcn.huangchao.remotetest;

// Declare any non-default types here with import statements

interface ILoginCallBackInterface {
    void onLoginStateChange(int state);
    void onNodeInfoChange(String param);
}
