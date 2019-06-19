// INetConnectInterface.aidl
package com.hcn.huangchao.remotetest;

import com.hcn.huangchao.remotetest.ILoginCallBackInterface;

// Declare any non-default types here with import statements

interface INetConnectInterface {

    void registerUser(String name, String password);

    void loginServer();

    void registerCallBack(ILoginCallBackInterface callback);

    void unRegisterCallBack(ILoginCallBackInterface callback);

    void requestServerData(long millis);

    void removeServerData();
}
