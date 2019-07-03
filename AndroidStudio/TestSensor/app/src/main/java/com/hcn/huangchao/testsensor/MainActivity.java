package com.hcn.huangchao.testsensor;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.hcn.huangchao.testsensor.fragment.AccelerometerFragment;
import com.hcn.huangchao.testsensor.fragment.GyroscopeFragment;
import com.hcn.huangchao.testsensor.fragment.MagneticFragment;
import com.hcn.huangchao.testsensor.fragment.OrientationFragment;
import com.hcn.huangchao.testsensor.fragment.SensorListFragment;
import com.hcn.huangchao.testsensor.fragment.TemperatureFragment;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private boolean isFloatWindowVisible;

    private static String currentFragmentTag;
    String[] mTitles;

    private Fragment mListFragment;
    private Fragment mAccelerometerFragment;
    private Fragment mTemperatureFragment;
    private Fragment mGyroscopeFragment;
    private Fragment mMagneticFragment;
    private Fragment mOrientationFragment;
    private Fragment mLightFragment;
    private Fragment mPressureFragment;

    private HashMap<Integer, Fragment> fragmentHashMap;
    private FragmentManager mFragmentManager;

    private SensorManager mSensorManager;

    private RadioGroup mTitleGroup;
    private RadioButton mListRb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initValues();
        initViews();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void initViews() {
        mTitleGroup = findViewById(R.id.main_title_id);
        mListRb = findViewById(R.id.rb_base_info);
        mListRb.setTag(0);
        mListRb.setOnClickListener(this);
        Resources res =getResources();
        mTitles = res.getStringArray(R.array.main_tiles);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> list = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor:list){
            if(fragmentHashMap.get(sensor.getType()) == null)
                continue;
            RadioButton button = new RadioButton(this);
            button.setText(mTitles[sensor.getType()]);
            button.setPadding(10,10,10,10);
            button.setBackgroundResource(R.drawable.title_selector);
            button.setTextColor(getResources().getColor(R.color.white));
            button.setButtonDrawable(getResources().getDrawable(android.R.color.transparent));
            button.setTag(sensor.getType());
            button.setOnClickListener(this);
            mTitleGroup.addView(button, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);

            TextView text = new TextView(this);
            text.setWidth(1);
            text.setBackgroundResource(R.color.title_divider_bg);
            mTitleGroup.addView(text, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        }
    }

    private void initValues() {
        currentFragmentTag = MainActivity.class.getSimpleName();
        mFragmentManager = getSupportFragmentManager();
        fragmentHashMap = new HashMap<>();

        mListFragment = new SensorListFragment();
        fragmentHashMap.put(0, mListFragment);
        mAccelerometerFragment = new AccelerometerFragment();
        fragmentHashMap.put(Sensor.TYPE_ACCELEROMETER, mAccelerometerFragment);
        mGyroscopeFragment = new GyroscopeFragment();
        fragmentHashMap.put(Sensor.TYPE_GYROSCOPE, mGyroscopeFragment);
        mMagneticFragment = new MagneticFragment();
        fragmentHashMap.put(Sensor.TYPE_MAGNETIC_FIELD, mMagneticFragment);
        mOrientationFragment = new OrientationFragment();
        fragmentHashMap.put(Sensor.TYPE_ORIENTATION, mOrientationFragment);
        mTemperatureFragment = new TemperatureFragment();
        fragmentHashMap.put(Sensor.TYPE_AMBIENT_TEMPERATURE, mTemperatureFragment);
        mLightFragment = new TemperatureFragment();
        fragmentHashMap.put(Sensor.TYPE_LIGHT, mLightFragment);
        mPressureFragment = new TemperatureFragment();
        fragmentHashMap.put(Sensor.TYPE_PRESSURE, mPressureFragment);
        changeFrament(mListFragment, null, SensorListFragment.class.getSimpleName());
    }

    /**
     * 设置显示的页面
     * <p>
     * fragment传入点击的按钮对应的Fragment对象
     * bundle参数
     * tag标识符
     */
    public void changeFrament(Fragment fragment, Bundle bundle, String tag) {
        if (!currentFragmentTag.equals(tag)) {
            for (int i = 0, count = mFragmentManager.getBackStackEntryCount(); i < count; i++) {
                mFragmentManager.popBackStack();
            }
            currentFragmentTag = tag;
            FragmentTransaction fg = mFragmentManager.beginTransaction();
            fragment.setArguments(bundle);
            fg.add(R.id.main_fragment_root_view, fragment, tag);
            fg.addToBackStack(tag);
            fg.commit();
        }
    }

    @Override
    public void onClick(View view) {
        Fragment fragment = fragmentHashMap.get(view.getTag());
        if(fragment != null)
            changeFrament(fragment, null, fragment.getClass().getSimpleName());
    }

    public void setFloatWindowVisible(boolean visible) {
        this.isFloatWindowVisible = visible;
    }

    public boolean isFloatWindowVisible() {
        return isFloatWindowVisible;
    }
}
