package com.hcn.huangchao.testproperties;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvBuild = findViewById(R.id.tvBuild);
        TextView tvProp = findViewById(R.id.tvProp);

        tvBuild.setText("Build: " + android.os.Build.MODEL);
        tvProp.setText("Prop: " + getProp("ro.product.model"));
    }

    public static String getProp(String propName) {
        Class<?> classType = null;
        String buildVersion = null;
        try {
            classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", new Class<?>[]{String.class});
            buildVersion = (String) getMethod.invoke(classType, new Object[]{propName});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buildVersion;
    }
}
