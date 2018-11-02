package com.mediatek.settings.wifi;

import android.app.AlertDialog;
import android.content.Context;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.GroupCipher;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.PairwiseCipher;
import android.net.wifi.WifiConfiguration.Protocol;
import android.os.SystemProperties;
import android.security.Credentials;
import android.security.KeyStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settingslib.wifi.AccessPoint;
import com.android.settings.wifi.WifiConfigController;
import com.android.settings.wifi.WifiConfigUiBase;
import com.mediatek.settings.FeatureOption;

import com.mediatek.settings.ext.IWifiExt;
import com.mediatek.settings.FeatureOption;
import com.mediatek.settings.UtilsExt;

import java.util.ArrayList;
import java.util.Arrays;

public class WifiConfigControllerExt {
    private static final String TAG = "WifiConfigControllerExt";

    // EAP SIM/AKA SIM slot selection @{
    private static final String SIM_STRING = "SIM";
    private static final String AKA_STRING = "AKA";
    private static final String AKA_PLUS_STRING = "AKA\'";
    private static final int WIFI_EAP_METHOD_DUAL_SIM = 2;
    private Spinner mSimSlot;
    // @}

    // Add for WAPI @{
    private Spinner mWapiCert;
    private static final String WLAN_PROP_KEY = "persist.sys.wlan";
    private static final String WIFI = "wifi";
    private static final String WAPI = "wapi";
    private static final String WIFI_WAPI = "wifi-wapi";
    private static final String DEFAULT_WLAN_PROP = WIFI_WAPI;
    public static final int SECURITY_WAPI_PSK = 4;
    public static final int SECURITY_WAPI_CERT = 5;
    // @}

    // Add for plug in
    private IWifiExt mExt;
    private Context mContext;
    private View mView;
    private WifiConfigUiBase mConfigUi;
    private WifiConfigController mController;

    public WifiConfigControllerExt(WifiConfigController controller, WifiConfigUiBase configUi,
            View view) {
        mController = controller;
        mConfigUi = configUi;
        mContext = mConfigUi.getContext();
        mView = view;
        mExt = UtilsExt.getWifiExt(mContext);
    }

    public void addViews(WifiConfigUiBase configUi, String security) {
        ViewGroup group = (ViewGroup) mView.findViewById(R.id.info);
        // add security information
        View row = configUi.getLayoutInflater().inflate(R.layout.wifi_dialog_row, group, false);
        ((TextView) row.findViewById(R.id.name)).setText(configUi.getContext().getString(
                R.string.wifi_security));
        mExt.setSecurityText((TextView) row.findViewById(R.id.name));
        ((TextView) row.findViewById(R.id.value)).setText(security);
        group.addView(row);
    }

    public void setConfig(WifiConfiguration config, int accessPointSecurity, TextView passwordView,
            Spinner eapMethodSpinner) {
        // get priority of configuration
        config.priority = mExt.getPriority(config.priority);
        switch (accessPointSecurity) {
        // EAP SIM/AKA sim slot config @{
        case AccessPoint.SECURITY_EAP:
            config.simSlot = addQuote(-1);
            String eapMethodStr = (String) eapMethodSpinner.getSelectedItem();
            Log.d(TAG, "selected eap method:" + eapMethodStr);
            if (AKA_STRING.equals(eapMethodStr) || SIM_STRING.equals(eapMethodStr)
                    || AKA_PLUS_STRING.equals(eapMethodStr)) {
                if (mSimSlot == null) {
                    mSimSlot = (Spinner) mView.findViewById(R.id.sim_slot);
                }
                if (TelephonyManager.getDefault().getPhoneCount() == WIFI_EAP_METHOD_DUAL_SIM) {
                    int simSlot = mSimSlot.getSelectedItemPosition() - 1;
                    if (simSlot > -1) {
                        config.simSlot = addQuote(simSlot);
                    }
                }
                Log.d(TAG, "EAP SIM/AKA config: " + config.toString());
            }
            break;
        // @}
        // Add WAPI_PSK & WAPI_CERT @{
        case SECURITY_WAPI_PSK:
            config.allowedKeyManagement.set(KeyMgmt.WAPI_PSK);
            config.allowedProtocols.set(Protocol.WAPI);
            if (passwordView.length() != 0) {
                String password = passwordView.getText().toString();
                config.preSharedKey = '"' + password + '"';
            }
            break;

        case SECURITY_WAPI_CERT:
            config.allowedKeyManagement.set(KeyMgmt.WAPI_CERT);
            config.allowedProtocols.set(Protocol.WAPI);
            if (mWapiCert.getSelectedItemPosition() != 0) {
                config.mAliases = (String) mWapiCert.getSelectedItem();
            }
            break;
        // @}
        default:
            break;
        }
    }

    private static String addQuote(int s) {
        return "\"" + s + "\"";
    }

    public void setEapmethodSpinnerAdapter() {
        // set array for eap method spinner. CMCC will show only eap and sim
        Context context = mConfigUi.getContext();
        String[] eapString = context.getResources().getStringArray(R.array.wifi_eap_method);
        ArrayList<String> eapList = new ArrayList<String>(Arrays.asList(eapString));
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, eapList);
        if (mController.getAccessPoint() != null) {
            mExt.setEapMethodArray(adapter, getAccessPointSsid(), getAccessPointSecurity());
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Add for triggering onItemSelected
        Spinner eapMethodSpinner = (Spinner) mView.findViewById(R.id.method);
        eapMethodSpinner.setAdapter(adapter);
    }

    public void setEapMethodFields(boolean edit) {
        Spinner eapMethodSpinner = (Spinner) mView.findViewById(R.id.method);
        int eapMethod = eapMethodSpinner.getSelectedItemPosition();
        // for CMCC-AUTO eap Method config information
        if (mController.getAccessPoint() != null) {
            eapMethod = mExt.getEapMethodbySpinnerPos(eapMethod, getAccessPointSsid(),
                    getAccessPointSecurity());
        }
        Log.d(TAG, "showSecurityFields modify method = " + eapMethod);
        // for CMCC ignore some config information
        mExt.hideWifiConfigInfo(new IWifiExt.Builder().setAccessPoint(mController.getAccessPoint())
                .setEdit(edit).setViews(mView), mConfigUi.getContext());
    }

    /**
     * Show EAP SIM/AKA sim slot by method.
     *
     * @param eapMethod
     *            The EAP method of AP.
     */
    public void showEapSimSlotByMethod(int eapMethod) {
        // for CMCC-AUTO eap Method config information
        if (mController.getAccessPoint() != null) {
            eapMethod = mExt.getEapMethodbySpinnerPos(eapMethod, getAccessPointSsid(),
                    getAccessPointSecurity());
        }

        if (eapMethod == WifiConfigController.WIFI_EAP_METHOD_SIM
                || eapMethod == WifiConfigController.WIFI_EAP_METHOD_AKA
                || eapMethod == WifiConfigController.WIFI_EAP_METHOD_AKA_PRIME) {
            if (TelephonyManager.getDefault().getPhoneCount() == WIFI_EAP_METHOD_DUAL_SIM) {
                mView.findViewById(R.id.sim_slot_fields).setVisibility(View.VISIBLE);
                mSimSlot = (Spinner) mView.findViewById(R.id.sim_slot);
                Context context = mConfigUi.getContext();
                String[] tempSimAkaMethods = context.getResources()
                        .getStringArray(R.array.sim_slot);
                TelephonyManager telephonyManager = (TelephonyManager) mContext
                        .getSystemService(Context.TELEPHONY_SERVICE);
                int sum = telephonyManager.getSimCount();
                Log.d(TAG, "the num of sim slot is :" + sum);
                String[] simAkaMethods = new String[sum + 1];
                for (int i = 0; i < (sum + 1); i++) {
                    if (i < tempSimAkaMethods.length) {
                        simAkaMethods[i] = tempSimAkaMethods[i];
                    } else {
                        simAkaMethods[i] = tempSimAkaMethods[1].replaceAll("1", "" + i);
                    }
                }
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                        android.R.layout.simple_spinner_item, simAkaMethods);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSimSlot.setAdapter(adapter);

                if (mController.getAccessPoint() != null
                        && mController.getAccessPoint().isSaved()) {
                    WifiConfiguration config = getAccessPointConfig();
                    if (config != null && config.simSlot != null) {
                        String simSlot = config.simSlot.replace("\"", "");
                        if (!simSlot.isEmpty()) {
                            mSimSlot.setSelection(Integer.parseInt(simSlot) + 1);
                        }
                    }
                }
            }
        } else {
            if (TelephonyManager.getDefault().getPhoneCount() == WIFI_EAP_METHOD_DUAL_SIM) {
                mView.findViewById(R.id.sim_slot_fields).setVisibility(View.GONE);
            }
        }
    }

    /**
     * add for EAP & WAPI
     */
    public boolean showSecurityFields(int accessPointSecurity, boolean edit) {
        Log.d(TAG, "showSecurityFields, accessPointSecurity = " + accessPointSecurity);
        Log.d(TAG, "showSecurityFields, edit = " + edit);

        // show WAPI_CERT field
        if (accessPointSecurity == SECURITY_WAPI_CERT) {
            mView.findViewById(R.id.security_fields).setVisibility(View.GONE);
            mView.findViewById(R.id.wapi_cert_fields).setVisibility(View.VISIBLE);
            mWapiCert = (Spinner) mView.findViewById(R.id.wapi_cert);
            mWapiCert.setOnItemSelectedListener(mController);
            loadCertificates(mWapiCert);

            if (mController.getAccessPoint() != null && mController.getAccessPoint().isSaved()) {
                WifiConfiguration config = getAccessPointConfig();
                setCertificate(mWapiCert, config.mAliases);
            }
            return true;
        } else {
            mView.findViewById(R.id.wapi_cert_fields).setVisibility(View.GONE);
        }

        // for CMCC ignore some config information
        mExt.hideWifiConfigInfo(new IWifiExt.Builder().setAccessPoint(mController.getAccessPoint())
                .setEdit(edit).setViews(mView), mConfigUi.getContext());
        return false;
    }

    private void setCertificate(Spinner spinner, String cert) {
        Log.d(TAG, "setSelection, cert = " + cert);
        if (cert != null) {
            @SuppressWarnings("unchecked")
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
            for (int i = adapter.getCount() - 1; i >= 0; --i) {
                if (cert.equals(adapter.getItem(i))) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    public boolean enableSubmitIfAppropriate(TextView passwordView, int accessPointSecurity,
            boolean pwInvalid) {
        boolean passwordInvalid = pwInvalid;
        if (passwordView != null
                && ((accessPointSecurity == AccessPoint.SECURITY_WEP && !isWepKeyValid(passwordView
                        .getText().toString())) ||
                        ((accessPointSecurity == AccessPoint.SECURITY_PSK && passwordView
                        .length() < 8) || (accessPointSecurity == SECURITY_WAPI_PSK && (passwordView
                        .length() < 8
                        || 64 < passwordView.length()))))) {
            passwordInvalid = true;
        }
        return passwordInvalid;
    }

    public int getEapMethod(int eapMethod) {
        Log.d(TAG, "getEapMethod, eapMethod = " + eapMethod);
        int result = eapMethod;
        if (mController.getAccessPoint() != null) {
            result = mExt.getEapMethodbySpinnerPos(eapMethod, getAccessPointSsid(),
                    getAccessPointSecurity());
        }
        Log.d(TAG, "getEapMethod, result = " + result);
        return result;
    }

    public void setEapMethodSelection(Spinner eapMethodSpinner, int eapMethod) {
        int eapMethodPos = eapMethod;
        if (mController.getAccessPoint() != null) {
            eapMethodPos = mExt.getPosByEapMethod(eapMethod, getAccessPointSsid(),
                    getAccessPointSecurity());
        }
        eapMethodSpinner.setSelection(eapMethodPos);
        Log.d(TAG, "[skyfyx]showSecurityFields modify pos = " + eapMethodPos);
        Log.d(TAG, "[skyfyx]showSecurityFields modify method = " + eapMethod);

    }

    public void setProxyText(View view) {
        // Set text of proxy exclusion list
        TextView proxyText = (TextView) view.findViewById(R.id.proxy_exclusionlist_text);
        mExt.setProxyText(proxyText);
    }

    /**
     * 1.Add some more security spinners to support WAPI 2.Switch spinner
     * according to WIFI & WAPI config
     */
    public void addWifiConfigView(boolean edit) {
        // set security text
        TextView securityText = (TextView) mView.findViewById(R.id.security_text);
        mExt.setSecurityText(securityText);
        if (mController.getAccessPoint() == null) {
            // set array for wifi security
            Spinner securitySpinner = ((Spinner) mView.findViewById(R.id.security));
            int securityEntriesId = R.array.wifi_security;
            if (FeatureOption.MTK_WAPI_SUPPORT) {
                String type = SystemProperties.get(WLAN_PROP_KEY, DEFAULT_WLAN_PROP);
                Log.d(TAG, "addWifiConfigView, type = " + type);
                if (type.equals(WIFI_WAPI)) {
                    securityEntriesId = R.array.wapi_security; // WIFI + WAPI
                } else if (type.equals(WIFI)) {
                    securityEntriesId = R.array.wifi_security; // WIFI only, AOSP
                } else if (type.equals(WAPI)) {
                    securityEntriesId = R.array.wapi_only_security; // WAPI only
                }
            } else {
                securityEntriesId = R.array.wifi_security; // WIFI only, AOSP
            }
            String[] items = mContext.getResources().getStringArray(securityEntriesId);
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(mContext,
                    android.R.layout.simple_spinner_item, items);
            spinnerAdapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);
            securitySpinner.setAdapter(spinnerAdapter);
        } else {
            WifiConfiguration config = getAccessPointConfig();
            Log.d(TAG, "addWifiConfigView, config = " + config);
            // Whether to show access point priority select spinner.
            mExt.setAPNetworkId(config);
            if (mController.getAccessPoint().isSaved() && config != null) {
                Log.d(TAG, "priority=" + config.priority);
                mExt.setAPPriority(config.priority);
            }
            mExt.setPriorityView((LinearLayout) mView.findViewById(R.id.priority_field), config,
                    edit);
        }

        // for CMCC ignore some config information
        mExt.hideWifiConfigInfo(new IWifiExt.Builder().setAccessPoint(mController.getAccessPoint())
                .setEdit(edit).setViews(mView), mConfigUi.getContext());
    }

    private boolean isWepKeyValid(String password) {
        if (password == null || password.length() == 0) {
            return false;
        }
        int keyLength = password.length();
        /// M: ALPS03001760, confirmed by wap_supplicant, we don't support WEP-152 AP
        if (((keyLength == 10 || keyLength == 26) && password
                .matches("[0-9A-Fa-f]*"))
                || (keyLength == 5 || keyLength == 13)) {
            return true;
        }
        return false;
    }

    private void loadCertificates(Spinner spinner) {
        final Context context = mConfigUi.getContext();
        String autoSelectCert = context.getString(R.string.wapi_auto_sel_cert);
        String[] certs = KeyStore.getInstance().list("WAPI_CACERT_", android.os.Process.WIFI_UID);
        if (certs == null || certs.length == 0) {
            certs = new String[] { autoSelectCert };
        } else {
            final String[] array = new String[certs.length + 1];
            array[0] = autoSelectCert;
            System.arraycopy(certs, 0, array, 1, certs.length);
            certs = array;
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, certs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /**
     * get the security to its corresponding security spinner position
     */
    public int getSecurity(int accessPointSecurity) {
        Log.d(TAG, "getSecurity, accessPointSecurity = " + accessPointSecurity);
        // Only WPAI supported
        if (FeatureOption.MTK_WAPI_SUPPORT) {
            String type = SystemProperties.get(WLAN_PROP_KEY, DEFAULT_WLAN_PROP);
            if (type.equals(WAPI) && accessPointSecurity > 0) {
                accessPointSecurity += SECURITY_WAPI_PSK - AccessPoint.SECURITY_WEP;
            }
        }
        Log.d(TAG, "getSecurity, accessPointSecurity = " + accessPointSecurity);
        return accessPointSecurity;
    }

    private WifiConfiguration getAccessPointConfig() {
        if (mController.getAccessPoint() != null) {
            return mController.getAccessPoint().getConfig();
        }
        return null;
    }

    private String getAccessPointSsid() {
        if (mController.getAccessPoint() != null) {
            return mController.getAccessPoint().getSsidStr();
        }
        return null;
    }

    private int getAccessPointSecurity() {
        if (mController.getAccessPoint() != null) {
            return mController.getAccessPoint().getSecurity();
        }
        return 0;
    }

    private DetailedState getAccessPointState() {
        if (mController.getAccessPoint() != null) {
            return (mController.getAccessPoint().getNetworkInfo() != null ? mController
                    .getAccessPoint().getNetworkInfo().getDetailedState() : null);
        }
        return null;
    }
}
