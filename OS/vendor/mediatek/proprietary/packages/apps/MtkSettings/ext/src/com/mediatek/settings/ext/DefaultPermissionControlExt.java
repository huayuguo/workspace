package com.mediatek.settings.ext;

import android.content.Context;
import android.content.ContextWrapper;
import android.provider.SearchIndexableData;
import android.support.v7.preference.PreferenceGroup;
import android.util.Log;
import java.util.List;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class DefaultPermissionControlExt extends ContextWrapper implements IPermissionControlExt {
    private static final String TAG = "DefaultPermissionControlExt";
    private static final String APK_PATH = "/system/plugin/PermissionControl/PermissionControl.apk";
    private static final String PKG_NAME = "com.mediatek.security";
    private static final String TARGET_NAME =
      "com.mediatek.security.plugin.PermissionControlPlugIn";
    private Object mCustomeExt;
    private Object mContext;

    public DefaultPermissionControlExt(Context context) {
        super(context);
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

    public void addPermSwitchPrf(PreferenceGroup prefGroup) {
       if (mCustomeExt != null) {
            Log.d(TAG, "will add PermissionControl preference");
            try {
                Method method = mCustomeExt.getClass().getDeclaredMethod("addPermSwitchPrf",
                                  PreferenceGroup.class);
                method.invoke(mCustomeExt, prefGroup);
            } catch (Exception e) {
                Log.e(TAG, "addDataPrf failed! " + e);
            }
        } else {
            Log.d(TAG, "will not add permission Control preference by default");
        }
    }

    public void enablerResume() {
        Log.d(TAG, "enablerResume() default");
    }

    public void enablerPause() {
        Log.d(TAG, "enablerPause() default");
    }

    public void addAutoBootPrf(PreferenceGroup prefGroup) {
       if (mCustomeExt != null) {
            Log.d(TAG, "will add Autoboot preference");
            try {
                Method method = mCustomeExt.getClass().getDeclaredMethod("addAutoBootPrf",
                                  PreferenceGroup.class);
                method.invoke(mCustomeExt, prefGroup);
            } catch (Exception e) {
                Log.e(TAG, "add autoboot preference failed! " + e);
            }
        } else {
            Log.d(TAG, "will not add autoboot preference by default");
        }
    }

    public List<SearchIndexableData> getRawDataToIndex(boolean enabled) {
       if (mCustomeExt != null) {
            Log.d(TAG, "getRawDataToIndex");
            try {
                Method method = mCustomeExt.getClass().getDeclaredMethod("getRawDataToIndex",
                                  boolean.class);
                return (List<SearchIndexableData>)method.invoke(mCustomeExt, enabled);
            } catch (Exception e) {
                Log.e(TAG, "getRawDataToIndex failed! " + e);
            }
        } else {
            Log.d(TAG, "getRawDataToIndex fail");
        }
        return null;
    }

}
