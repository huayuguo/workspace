LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_C_INCLUDES += \
		$(JNI_H_INCLUDE) \
		$(LOCAL_PATH)

LOCAL_SRC_FILES := libuvc_jni.c ImageProc.c ioctlLog.c ioctlUtil.c avilib.c

#LOCAL_SHARED_LIBRARIES += liblog libnativehelper libz -ljnigraphics
LOCAL_LDLIBS += -llog -lz -ljnigraphics -lc

#LOCAL_SHARED_LIBRARIES += libcutils libnetutils libc

LOCAL_MODULE := libUVCJni
include $(BUILD_SHARED_LIBRARY)