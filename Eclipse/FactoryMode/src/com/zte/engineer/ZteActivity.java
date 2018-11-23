
package com.zte.engineer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.mediatek.factorymode.R;

public abstract class ZteActivity extends Activity implements View.OnClickListener {

    public static final int RESULT_PASS = 10;
    public static final int RESULT_FAIL = 20;

    public static final String EXTRA_IS_AUTOTEST = "extra_is_autotest";

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onBackPressed() {
        finishSelf(RESULT_FAIL);
    }

    public void finishSelf(int result) {
        setResult(result);
        finish();
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.btnPass:
                finishSelf(RESULT_PASS);
                break;
            case R.id.btnFail:
                finishSelf(RESULT_FAIL);
                break;
            default:
                finishSelf(RESULT_PASS);
                break;
        }
    }
}
