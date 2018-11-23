package com.mediatek.factorymode;

import android.service.oemlock.OemLockManager;
import android.service.persistentdata.PersistentDataBlockManager;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

public class MasterClear extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.master_clear_confirm);
		Button mButton = (Button) findViewById(R.id.execute_master_clear);
		mButton.setOnClickListener(mFinalClickListener);
		super.onCreate(savedInstanceState);
	}
	
    private Button.OnClickListener mFinalClickListener = new Button.OnClickListener() {

        public void onClick(View v) {
            if (ActivityManager.isUserAMonkey()) {
                return;
            }

            final PersistentDataBlockManager pdbManager = (PersistentDataBlockManager)
                    getSystemService(Context.PERSISTENT_DATA_BLOCK_SERVICE);
            final OemLockManager oemLockManager = (OemLockManager)
                    getSystemService(Context.OEM_LOCK_SERVICE);

            if (pdbManager != null && !oemLockManager.isOemUnlockAllowed() &&
            		Settings.Global.getInt(getContentResolver(),
                            Settings.Global.DEVICE_PROVISIONED, 0) != 0) {
                // if OEM unlock is allowed, the persistent data block will be wiped during FR
                // process. If disabled, it will be wiped here, unless the device is still being
                // provisioned, in which case the persistent data block will be preserved.
                new AsyncTask<Void, Void, Void>() {
                    int mOldOrientation;
                    ProgressDialog mProgressDialog;

                    @Override
                    protected Void doInBackground(Void... params) {
                        pdbManager.wipe();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        mProgressDialog.hide();
                        setRequestedOrientation(mOldOrientation);
                        doMasterClear();
                    }

                    @Override
                    protected void onPreExecute() {
                        mProgressDialog = getProgressDialog();
                        mProgressDialog.show();

                        // need to prevent orientation changes as we're about to go into
                        // a long IO request, so we won't be able to access inflate resources on flash
                        mOldOrientation =getRequestedOrientation();
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                    }
                }.execute();
            } else {
                doMasterClear();
            }
        }
        
        private void doMasterClear() {
            Intent intent = new Intent(Intent.ACTION_FACTORY_RESET);
            intent.setPackage("android");
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            intent.putExtra(Intent.EXTRA_REASON, "MasterClearConfirm");
            intent.putExtra(Intent.EXTRA_WIPE_EXTERNAL_STORAGE, false);
            intent.putExtra(Intent.EXTRA_WIPE_ESIMS, false);
            sendBroadcast(intent);
            // Intent handling is asynchronous -- assume it will happen soon.
        }

        private ProgressDialog getProgressDialog() {
            final ProgressDialog progressDialog = new ProgressDialog(MasterClear.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(
                    getString(R.string.master_clear_progress_title));
            progressDialog.setMessage(
                    getString(R.string.master_clear_progress_text));
            return progressDialog;
        }
    };

}
