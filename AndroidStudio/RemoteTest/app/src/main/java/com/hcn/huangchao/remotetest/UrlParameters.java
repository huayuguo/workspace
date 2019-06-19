package com.hcn.huangchao.remotetest;

import android.text.TextUtils;

import java.util.LinkedHashMap;

public class UrlParameters {
    static private LinkedHashMap<String, String> mMap = new LinkedHashMap<>(128);
    static public void unflatten(String flattened) {
        mMap.clear();

        TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(';');
        splitter.setString(flattened);
        for (String kv : splitter) {
            int pos = kv.indexOf('=');
            if (pos == -1) {
                continue;
            }
            String k = kv.substring(0, pos);
            String v = kv.substring(pos + 1);
            mMap.put(k, v);
        }
    }

    static public String get(String key) {
        return mMap.get(key);
    }

    static public int getInt(String key) {
        return Integer.parseInt(mMap.get(key));
    }

    static public double getDouble(String key) {
        String value = mMap.get(key);
        if(value != null) {
            return Double.parseDouble(mMap.get(key));
        } else {
            return 0;
        }
    }
}
