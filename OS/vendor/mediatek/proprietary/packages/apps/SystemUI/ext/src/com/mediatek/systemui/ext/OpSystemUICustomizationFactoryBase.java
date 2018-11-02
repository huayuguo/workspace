package com.mediatek.systemui.ext;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import com.mediatek.common.util.OperatorCustomizationFactoryLoader;
import com.mediatek.common.util.OperatorCustomizationFactoryLoader.OperatorFactoryInfo;

public class OpSystemUICustomizationFactoryBase {

    public IMobileIconExt makeMobileIcon() {
        return new DefaultMobileIconExt();
    }

    public INavigationBarPlugin makeNavigationBar(Context context) {
        return new DefaultNavigationBarPlugin(context);
    }

    public IQuickSettingsPlugin makeQuickSettings(Context context) {
        return new DefaultQuickSettingsPlugin(context);
    }

    public IStatusBarPlmnPlugin makeStatusBarPlmn(Context context) {
        return new DefaultStatusBarPlmnPlugin(context);
    }

    public ISystemUIStatusBarExt makeSystemUIStatusBar(Context context) {
        return new DefaultSystemUIStatusBarExt(context);
    }

    private static final List<OperatorFactoryInfo> sOpFactoryInfoList
                                                = new ArrayList<OperatorFactoryInfo>();
    static {
        sOpFactoryInfoList.add(
                new OperatorFactoryInfo("OP03SystemUI.apk",
                         "com.mediatek.op03.systemui.Op03SystemUICustomizationFactory",
                         "com.mediatek.op03.systemui",
                         "OP03",
                         "SEGDEFAULT"
                        ));

        sOpFactoryInfoList.add(
                new OperatorFactoryInfo("OP07SystemUI.apk",
                         "com.mediatek.op07.systemui.OP07SystemUICustomizationFactory",
                         "com.mediatek.op07.systemui",
                         "OP07",
                         "SEGDEFAULT"
                        ));

        sOpFactoryInfoList.add(
                new OperatorFactoryInfo("OP18SystemUI.apk",
                         "com.mediatek.op18.systemui.Op18SystemUICustomizationFactory",
                         "com.mediatek.op18.systemui",
                         "OP18",
                         "SEGDEFAULT"
                     ));

        sOpFactoryInfoList.add(
                new OperatorFactoryInfo("OP01SystemUI.apk",
                         "com.mediatek.systemui.op01.Op01SystemUICustomizationFactory",
                         "com.mediatek.systemui.op01",
                         "OP01"
                     ));

        sOpFactoryInfoList.add(
                new OperatorFactoryInfo("OP02SystemUI.apk",
                         "com.mediatek.systemui.op02.Op02SystemUICustomizationFactory",
                         "com.mediatek.systemui.op02",
                         "OP02"
                     ));

        sOpFactoryInfoList.add(
                new OperatorFactoryInfo("OP09SystemUI.apk",
                         "com.mediatek.systemui.op09.Op09SystemUICustomizationFactory",
                         "com.mediatek.systemui.op09",
                         "OP09",
                         "SEGDEFAULT"
                     ));

        sOpFactoryInfoList.add(
                new OperatorFactoryInfo("OP09ClipSystemUI.apk",
                         "com.mediatek.systemui.op09clip.Op09SystemUICustomizationFactory",
                         "com.mediatek.systemui.op09clip",
                         "OP09",
                         "SEGC"
                     ));

        sOpFactoryInfoList.add(
                new OperatorFactoryInfo("OP08SystemUI.apk",
                         "com.mediatek.op08.systemui.Op08SystemUICustomizationFactory",
                         "com.mediatek.op08.systemui",
                         "OP08"
                     ));
    }

    static OpSystemUICustomizationFactoryBase sFactory = null;
    public static synchronized OpSystemUICustomizationFactoryBase getOpFactory(Context context) {
        if (sFactory == null) {
            sFactory = (OpSystemUICustomizationFactoryBase) OperatorCustomizationFactoryLoader
                           .loadFactory(context, sOpFactoryInfoList);
            if (sFactory == null) {
                sFactory = new OpSystemUICustomizationFactoryBase();
            }
        }
        return sFactory;
    }
}