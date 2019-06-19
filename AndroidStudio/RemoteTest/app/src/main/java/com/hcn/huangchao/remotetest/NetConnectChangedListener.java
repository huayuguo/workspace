package com.hcn.huangchao.remotetest;

public interface NetConnectChangedListener {
    void onLoginStateChange(int state);
    void onNodeInfoChange(String param);
}
