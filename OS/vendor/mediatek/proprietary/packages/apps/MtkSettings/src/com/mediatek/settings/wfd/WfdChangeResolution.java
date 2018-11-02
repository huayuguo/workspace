package com.mediatek.settings.wfd;

import android.app.FragmentManager;
import android.content.Context;
import android.hardware.display.WifiDisplayStatus;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.settings.R;
import com.mediatek.provider.MtkSettingsExt;

import java.util.ArrayList;
import java.util.Arrays;

public class WfdChangeResolution {
    private static final String TAG = "WfdChangeResolution";
    private Context mContext;

    // Change resolution menu id
    private static final int MENU_ID_CHANGE_RESOLUTION = Menu.FIRST + 1;

    /*
     * Device resolution:
     * 0: 720p 30fps menu disabled
     * 1: 1080p 30fps menu disabled
     * 2: 1080p 30fps
     * 3: 720p 30fps
     */
    public static final ArrayList<Integer> DEVICE_RESOLUTION_LIST = new ArrayList(
            Arrays.asList(2, 3));

    public WfdChangeResolution(Context context) {
        mContext = context;
    }

    /**
     * Add change resolution option menu.
     *
     * @param menu
     *            the menu that change resolution menu item will be added
     * @param status
     *            current WFD status
     */
    public void onCreateOptionMenu(Menu menu, WifiDisplayStatus status) {
        int currentResolution = Settings.Global.getInt(mContext.getContentResolver(),
                MtkSettingsExt.Global.WIFI_DISPLAY_RESOLUTION, 0);
        Log.d("@M_" + TAG, "current resolution is " + currentResolution);
        if (DEVICE_RESOLUTION_LIST.contains(currentResolution)) {
            menu.add(Menu.NONE, MENU_ID_CHANGE_RESOLUTION,
                0, R.string.wfd_change_resolution_menu_title)

                    .setEnabled(
                        status.getFeatureState() == WifiDisplayStatus.FEATURE_STATE_ON
                        && status.getActiveDisplayState()
                        != WifiDisplayStatus.DISPLAY_STATE_CONNECTING)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }

    /**
     * Called when the option menu is selected.
     *
     * @param item
     *            the selected menu item
     * @param fragmentManager
     *            Fragment manager used to show new fragment
     * @return true, change resolution item is selected, otherwise false
     */
    public boolean onOptionMenuSelected(MenuItem item, FragmentManager fragmentManager) {
        if (item.getItemId() == MENU_ID_CHANGE_RESOLUTION) {
            new WfdChangeResolutionFragment().show(
                    fragmentManager, "change resolution");
            return true;
        }
        return false;
    }

}
