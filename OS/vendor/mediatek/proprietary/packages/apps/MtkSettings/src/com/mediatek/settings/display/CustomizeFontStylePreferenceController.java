/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.mediatek.settings.display;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.UserManager;
import android.support.v7.preference.Preference;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import java.util.List;

public class CustomizeFontStylePreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin {

    private static final String KEY_FONT_SETTING = "font_setting";
    private static final String LOVELYFONTS = "com.iekie.lovelyfonts.";
    private static final String MEPHONE_FONT_PKG = "com.mephone.fonts";
    private static final String MEPHONE_MAINACTIVITY = "com.mephone.fonts.activity.MainActivity";
    private static final String MT_FONT_PKG = "com.ekesoo.mtfont";
    private static final String IEKIE_FONT_ACTIVITY = "com.iekie.lovelyfonts.fonts.activity";
    private static final String EKESOO_FONT_PKG = "com.ekesoo.font";
    private static final String EKESOO_MAINACTIVITY = "com.ekesoo.font.activity.MainActivity";
    private Intent mFontSettingsIntent = new Intent("com.lovelyfonts.activity.mainTab");

    public CustomizeFontStylePreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
         return isPrimaryUser(mContext) && existFontApk(mContext, mFontSettingsIntent)
                 && !resoleLovelyFontsApp(mContext);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_FONT_SETTING;
    }

    private boolean isPrimaryUser(Context context){
        final UserManager userManager = (UserManager)context.getSystemService(Context.USER_SERVICE);
        return userManager.isSystemUser();
    }

    private boolean existFontApk(Context context, Intent intent) {
        return context.getPackageManager().resolveActivity(intent, 0) != null;
    }

    private boolean resoleLovelyFontsApp(Context context) {
        if (resolveLovelyFontsComponent(context, MEPHONE_FONT_PKG, MEPHONE_MAINACTIVITY)) {
            return true;
        }
        if (resolveLovelyFontsComponent(context, EKESOO_FONT_PKG, EKESOO_MAINACTIVITY)) {
            return true;
        }
        if (resolveLovelyFontsComponent(context, MT_FONT_PKG, IEKIE_FONT_ACTIVITY)) {
            return true;
        }
        boolean found = false;
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> mList = pm.getInstalledApplications(0);
        for (ApplicationInfo info : mList) {
            if (info.packageName.startsWith(LOVELYFONTS)) {
                if (resolveLovelyFontsComponent(context, info.packageName, IEKIE_FONT_ACTIVITY)) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    private boolean resolveLovelyFontsComponent(Context context, String pkgName, String comp) {
        Intent intent = new Intent();
        intent.setClassName(pkgName, comp);
        return context.getPackageManager().resolveActivity(intent, 0) != null;
    }
}
