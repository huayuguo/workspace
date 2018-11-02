/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.wifi.details;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import com.android.settings.R;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;

/**
 * ActionBar lifecycle observer for {@link WifiNetworkDetailsFragment}.
 */
public class WifiDetailActionBarObserver implements LifecycleObserver, OnStart {

    private final Fragment mFragment;
    private final Context mContext;

    public WifiDetailActionBarObserver(Context context, Fragment fragment) {
        mContext = context;
        mFragment = fragment;
    }

    // M: fix CR:ALPS03579390,when rotate detail screen,setActionBar don't
    // execute in Activity, so fragment don't getActionBar on Create,occur JE.
    @Override
    public void onStart() {
        if (mFragment.getActivity() != null) {
            mFragment.getActivity().getActionBar()
                    .setTitle(mContext.getString(R.string.wifi_details_title));
        }
    }
}
