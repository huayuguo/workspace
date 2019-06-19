package com.android.tv.mysettings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TvSettingsPreference extends Preference {
    private static final String TAG = "PreferenceWithTip";
    private String prefTitle = null;
    private String prefValue = null;

    @SuppressLint("Recycle")
    public TvSettingsPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // 获取自定义参数
        Log.i(TAG, "TextTvSettingPreference invoked");
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TextTvSettingPreference);
        prefTitle = ta.getString(R.styleable.TextTvSettingPreference_title);
        prefValue = ta.getString(R.styleable.TextTvSettingPreference_value);
        ta.recycle();
    }

    public TvSettingsPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        titleView.setText(prefTitle);
        TextView valueView = (TextView) view.findViewById(R.id.value);
        valueView.setText(prefValue);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        return LayoutInflater.from(getContext()).inflate(R.layout.tvsettings_preference,
                parent, false);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        prefTitle = title.toString();
    }

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
        prefValue = summary.toString();
    }
}
