package com.mediatek.settings.ext;

import android.content.Context;
import android.support.v7.preference.PreferenceGroup;

import android.util.Log;

import dalvik.system.PathClassLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DefaultDataProtectionExt implements IDataProtectionExt {
    private static final String TAG = "DefaultDataProectionExt";

    private static final String APK_PATH = "/system/plugin/DataProtection/DataProtection.apk";
    private static final String PKG_NAME = "com.mediatek.dataprotection";
    private static final String TARGET_NAME =
      "com.mediatek.dataprotection.plugin.DataProtectionPlugIn";

    private Object mCustomeExt;
    private Object mContext;

    public DefaultDataProtectionExt(Context context) {
        mContext = context;

        Log.d(TAG, "load factory");
        try {
            ClassLoader classLoader;
            if (context != null) {
                classLoader = new PathClassLoader(APK_PATH, context.getClassLoader());
            } else {
                classLoader = new PathClassLoader(APK_PATH,
                                        ClassLoader.getSystemClassLoader().getParent());
            }

            Class<?> clazz = classLoader.loadClass(TARGET_NAME);
            Log.d(TAG, "Load class : " +  TARGET_NAME
                        + " successfully with classLoader:" + classLoader);

            if (context != null) {
                try {
                    Constructor<?> constructor = clazz.getConstructor(Context.class);
                    Context opContext = context.createPackageContext(PKG_NAME,
                            Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
                    mCustomeExt = constructor.newInstance(opContext);
                } catch (NoSuchMethodException e) {
                    // Use default constructor
                    Log.d(TAG, "Exception occurs when using constructor with Context");
                } catch (InvocationTargetException e) {
                    // Use default constructor
                    Log.e(TAG, "Exception occurs when execute constructor with Context", e);
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Exception when initial instance", ex);
        }
    }

    public void addDataPrf(PreferenceGroup prefGroup) {
        if (mCustomeExt != null) {
            Log.d(TAG, "will add data protection preference");
            try {
                Method method = mCustomeExt.getClass().getDeclaredMethod("addDataPrf",
                                  PreferenceGroup.class);
                method.invoke(mCustomeExt, prefGroup);
            } catch (Exception e) {
                Log.e(TAG, "addDataPrf failed! " + e);
            }
        } else {
            Log.d(TAG, "will not add data protection preference by default");
        }
    }
}
