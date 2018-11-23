package com.microarray.fingerprint;

import java.io.File;
import java.lang.reflect.Method;

import com.mediatek.factorymode.fingerprint.MAFactoryActivity;
import com.mediatek.factorymode.R;

public class FingerprintTest {
	
	public static final int PASS = 0;
	public static final int FAIL = -1;
	
	public static final int NO_FINGER = 0;
	
	static {
		String LIBPATH = "";
		if (MAFactoryActivity.CONTEXT != null) {
			LIBPATH = MAFactoryActivity.CONTEXT.getResources().getString(R.string.custom_lib);
		}
		if (new File(LIBPATH).exists() && LIBPATH.contains("/lib") && LIBPATH.contains(".so")) {
			String libname = LIBPATH.substring(LIBPATH.lastIndexOf("/lib") + 4, LIBPATH.length() - 3);
			Utils.dprint("library name: " + libname);
			System.loadLibrary(libname);
		} else {
			if (isX64()) {
				Utils.dprint("library name: fprint-x64");
				System.loadLibrary("fprint-x64");
			} else {
				Utils.dprint("library name: fprint-x32");
				System.loadLibrary("fprint-x32");
			}
		}
	}
	
    private static boolean isX64() {
        String value = "";
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method get = clazz.getMethod("get", String.class, String.class);
            value = (String) get.invoke(clazz, "ro.product.cpu.abi", "");
        } catch (Exception e) { }
        return value.contains("64");
    }
	
	/* 切换到工厂模式
	 * @return 0: 成功
	 */
	static native public int nativeSet2FactoryMode();

	/* 切换到正常模式
	 * @return 0: 成功
	 */
	static native public int nativeSet2NormalMode();

	/* 检测SPI通信
	 * @return 0: 成功, -1: 失败
	 */
	static native public int nativeSPICommunicate();

	/* 检测坏点
	 * @return 0: 成功, -1: 失败
	 */
	static native public int nativeBadImage();

	/* 检测中断
	 * @return 0: 成功, -1: 失败
	 */
	static native public int nativeInterrupt();
    
    /* 检测按压
     * @return >0: energy值, 0: 未按压
     */
	static native public int nativePress();

	/* 注册指纹
	 * @return >0: 一次注册成功, -111: 失败, -113: 重复区域
	 */
	static native public int nativeEnroll();

	/* 匹配指纹
	 * @return: 0: 成功, -1: 失败
	 */
	static native public int nativeAuthenticate();

	/* 清空指纹模板
	 */
	static native public int nativeRemove();

	/* 获得芯片信息
	 * @return: 芯片信息
	 */
	static native public String nativeGetVendor();
	
	/* 校准 (for 120)
	 */
	static native public int nativeCalibrate();
	
}
