package android.hardware.miscdev;

class IMiscDevService{
    ;
}

public class MiscDevManager {

    public MiscDevManager(IMiscDevService service) {
        ;
    }

    public int setMiscDevState(int dev_index, int state) {
        return 0;
    }
    public int setLightThreshold(int low, int high) {return 0;}
}
