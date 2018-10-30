package android.hardware.miscdev;

/**
 * Created by huangchao on 2017/9/11.
 */

class IMiscDevService {
    ;
}

public class MiscDevManager implements IMiscDevManager  {
    public MiscDevManager(IMiscDevService service) {
    }

    public int setMiscDevState(int dev_index, int state) {
        return 0;
    }

    public int setLightThreshold(int low, int high) {
        return 0;
    }

    public int getMiscDevState(int dev_index) {
        return 0;
    }
}
