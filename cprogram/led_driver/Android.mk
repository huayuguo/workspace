LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := led_test
LOCAL_SRC_FILES := test.c
LOCAL_SHARED_LIBRARIES := \
        libcutils \
        libutils
include $(BUILD_EXECUTABLE)
