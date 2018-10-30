package android.hardware.miscdev;

/**
 * Created by huangchao on 2017/9/11.
 */

public interface IMiscDevManager {
    /**
     * misc device service name.
     */
    public String MISCDEV_SERVICE = "MiscdevService";

    public static int INDEX_IRCUT = 0;
    public static int INDEX_IRLED = 1;
    public static int INDEX_LASER = 2;

    static int STATE_ON = 1;
    static int STATE_OFF = 0;


    public int setMiscDevState(int dev_index, int state);
    public int getMiscDevState(int dev_index);
    public int setLightThreshold(int low, int high);
}

