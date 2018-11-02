package com.mediatek.settings.deviceinfo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.android.settings.widget.RadioButtonPreference;

public class StorageRadioButtonPreference extends RadioButtonPreference {
    private static final String TAG = "StorageSettings";
    private String mMountPath;

    public StorageRadioButtonPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public StorageRadioButtonPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StorageRadioButtonPreference(Context context) {
        super(context);
    }

    @Override
    public void onClick() {
        if (isChecked()) {
            Log.d(TAG, "What you select is already the default write path, ignore.");
            return;
        }
        setChecked(true);
        callChangeListener(true);
    }

    public void setPath(String path) {
        mMountPath = path;
    }

    public String getPath() {
        return mMountPath;
    }
}
