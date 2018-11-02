/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 */

package com.mediatek.settings;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.os.SystemProperties;
import com.mediatek.settings.ext.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class UtilsExt {

    private static final String TAG = "UtilsExt";
    private static IDataUsageSummaryExt sDataUsageSummaryExt;
    private static ISimManagementExt sSimManagementExt;
    private static ISimRoamingExt sSimRoamingExt;
    private static ISmsDialogExt sSmsDialogExt;
    private static ISmsPreferenceExt sSmsPreferenceExt;
    private static IWfcSettingsExt sWfcSettingsExt;
    private static IWifiTetherSettingsExt sWifiTetherSettingsExt;

    /// Disable apps @ {
    // Disable apps list file location
    private static final String FILE_DISABLE_APPS_LIST = "/vendor/etc/disableapplist.txt";
    // Read the file to get the need special disable app list
    public static ArrayList<String> disableAppList = readFile(FILE_DISABLE_APPS_LIST);
    /// @}

    public static IDataUsageSummaryExt getDataUsageSummaryExt(Context context) {
        if (sDataUsageSummaryExt == null) {
            synchronized (IDataUsageSummaryExt.class) {
                if (sDataUsageSummaryExt == null) {
                    sDataUsageSummaryExt = OpSettingsCustomizationUtils.getOpFactory(context)
                            .makeDataUsageSummaryExt();
                    log("[getDataUsageSummaryExt]create ext instance: " + sDataUsageSummaryExt);
                }
            }
        }
        return sDataUsageSummaryExt;
    }

    public static ISimManagementExt getSimManagementExt(Context context) {
        if (sSimManagementExt == null) {
            synchronized (ISimManagementExt.class) {
                if (sSimManagementExt == null) {
                    sSimManagementExt = OpSettingsCustomizationUtils.getOpFactory(context)
                            .makeSimManagementExt();
                    log("[getSimManagementExt] create ext instance: " + sSimManagementExt);
                }
            }
        }
        return sSimManagementExt;
    }

    public static ISimRoamingExt getSimRoamingExt(Context context) {
        if (sSimRoamingExt == null) {
            synchronized (ISimRoamingExt.class) {
                if (sSimRoamingExt == null) {
                    sSimRoamingExt = OpSettingsCustomizationUtils.getOpFactory(context)
                            .makeSimRoamingExt();
                    log("[getSimRoamingExt] create ext instance: " + sSimRoamingExt);
                }
            }
        }
        return sSimRoamingExt;
    }

    public static ISmsDialogExt getSmsDialogExt(Context context) {
        if (sSimRoamingExt == null) {
            synchronized (ISmsDialogExt.class) {
                if (sSmsDialogExt == null) {
                    sSmsDialogExt = OpSettingsCustomizationUtils.getOpFactory(context)
                            .makeSmsDialogExt();
                    log("[getSmsDialogExt] create ext instance: " + sSmsDialogExt);
                }
            }
        }
        return sSmsDialogExt;
    }

    public static ISmsPreferenceExt getSmsPreferenceExt(Context context) {
        if (sSimRoamingExt == null) {
            synchronized (ISmsPreferenceExt.class) {
                if (sSmsPreferenceExt == null) {
                    sSmsPreferenceExt = OpSettingsCustomizationUtils.getOpFactory(context)
                            .makeSmsPreferenceExt();
                    log("[getSmsPreferenceExt] create ext instance: " + sSmsPreferenceExt);
                }
            }
        }
        return sSmsPreferenceExt;
    }

    public static IWfcSettingsExt getWfcSettingsExt(Context context) {
        if (sWfcSettingsExt == null) {
            synchronized (IWfcSettingsExt.class) {
                if (sWfcSettingsExt == null) {
                    sWfcSettingsExt = OpSettingsCustomizationUtils.getOpFactory(context)
                            .makeWfcSettingsExt();
                    log("[getWfcSettingsExt] create ext instance: " + sWfcSettingsExt);
                }
            }
        }
        return sWfcSettingsExt;
    }


    // M: Add for MTK Wifi Tether feature.
    public static IWifiTetherSettingsExt getWifiTetherSettingsExt(Context context) {
        if (sWifiTetherSettingsExt == null) {
            synchronized (IWifiTetherSettingsExt.class) {
                if (sWifiTetherSettingsExt == null) {
                    sWifiTetherSettingsExt = OpSettingsCustomizationUtils.getOpFactory(context)
                            .makeWifiTetherSettingsExt(context);
                    log("[getWifiTetherSettingsExt] create ext instance: "
                            + sWifiTetherSettingsExt);
                }
            }
        }
        return sWifiTetherSettingsExt;
    }

    // M: create settigns plugin object
    public static ISettingsMiscExt getMiscPlugin(Context context) {
        return OpSettingsCustomizationUtils.getOpFactory(context).makeSettingsMiscExt(context);
    }

    public static IDisplaySettingsExt getDisplaySettingsExt(Context context) {
        return OpSettingsCustomizationUtils.getOpFactory(context).makeDisplaySettingsExt(context);
    }

    public static IApnSettingsExt getApnSettingsExt(Context context) {
        return OpSettingsCustomizationUtils.getOpFactory(context).makeApnSettingsExt(context);
    }

    public static IRCSSettings getRCSSettingsExt(Context context) {
        return OpSettingsCustomizationUtils.getOpFactory(context).makeRCSSettings(context);
    }

    public static IWWOPJoynSettingsExt getWWOPJoynSettingsExt(Context context) {
        return OpSettingsCustomizationUtils.getOpFactory(context).makeWWOPJoynSettingsExt(context);
    }

    // M: Add for MTK Wifi AP dialog feature.
    public static IWifiApDialogExt getWifiApDialogExt(Context context) {
        return OpSettingsCustomizationUtils.getOpFactory(context).makeWifiApDialogExt();
    }

    // M: Add for MTK Wifi feature.
    public static IWifiExt getWifiExt(Context context) {
        return OpSettingsCustomizationUtils.getOpFactory(context).makeWifiExt(context);
    }

    // M: Add for MTK Wifi settings feature.
    public static IWifiSettingsExt getWifiSettingsExt(Context context) {
        return OpSettingsCustomizationUtils.getOpFactory(context).makeWifiSettingsExt();
    }

    public static IAppListExt getAppListExt(Context context) {
        return OpSettingsCustomizationUtils.getOpFactory(context).makeAppListExt(context);
    }

    public static IAppsExt getAppsExt(Context context) {
        return OpSettingsCustomizationUtils.getOpFactory(context).makeAppsExt(context);
    }

    public static IAudioProfileExt getAudioProfileExt(Context context) {
        return OpSettingsCustomizationUtils.getOpFactory(context).makeAudioProfileExt(context);
    }

    public static IDevExt getDevExt(Context context) {
        return OpSettingsCustomizationUtils.getOpFactory(context).makeDevExt(context);
    }

    public static IDeviceInfoSettingsExt getDeviceInfoSettingsExt(Context context) {
        return OpSettingsCustomizationUtils.getOpFactory(context).makeDeviceInfoSettingsExt();
    }

    public static IStatusBarPlmnDisplayExt getStatusBarPlmnDisplayExt(Context context) {
        return OpSettingsCustomizationUtils.getOpFactory(context)
                                           .makeStatusBarPlmnDisplayExt(context);
    }

    public static IRcseOnlyApnExt getRcseOnlyApnExt(Context context) {
        return OpSettingsCustomizationUtils.getOpFactory(context).makeRcseOnlyApnExt();
    }

    public static IStatusExt getStatusExt(Context context) {
        return OpSettingsCustomizationUtils.getOpFactory(context).makeStatusExt();
    }

    // M: Add for MTK in house Data-Protection.
    public static IDataProtectionExt getDataProectExtPlugin(Context context) {
        return new DefaultDataProtectionExt(context);
    }

    //M: Add for Privacy Protection Lock Settings Entry.
    public static IPplSettingsEntryExt getPrivacyProtectionLockExtPlugin(Context context) {
        return DefaultPplSettingsEntryExt.getInstance(context);
    }

    // M: Add for MTK in house Permission Control.
    public static IPermissionControlExt getPermControlExtPlugin(Context context) {
        return new DefaultPermissionControlExt(context);
    }

    // / Disable apps @ {
    private static ArrayList<String> readFile(String path) {
        ArrayList<String> appsList = new ArrayList<String>();
        appsList.clear();
        File file = new File(path);
        FileReader fr = null;
        BufferedReader br = null;
        try {
            if (file.exists()) {
                fr = new FileReader(file);
            } else {
                Log.d(TAG, "file in " + path + " does not exist!");
                return null;
            }
            br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                Log.d(TAG, " read line " + line);
                appsList.add(line);
            }
            return appsList;
        } catch (IOException io) {
            Log.d(TAG, "IOException");
            io.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
        return null;
    }

    /**
     * do not show SIM Activity Dialog for auto sanity.
     * 1.FeatureOption.MTK_AUTOSANITY is true
     * 2.FeatureOption.MTK_BUILD_TYPE is ENG
     * @return true disable SIM Dialog
     */
    public static boolean shouldDisableForAutoSanity() {
        boolean autoSanity = SystemProperties.get("ro.mtk.autosanity").equals("1");
        String buildType = SystemProperties.get("ro.build.type", "");
        Log.d(TAG, "autoSanity: " + autoSanity + " buildType: " + buildType);
        if (autoSanity && (!TextUtils.isEmpty(buildType)) && buildType.endsWith("eng")) {
            Log.d(TAG, "ShouldDisableForAutoSanity()...");
            return true;
        }
        return false;
    }

    // / @}

    private static void log(String msg) {
        Log.d(TAG, msg);
    }
}
