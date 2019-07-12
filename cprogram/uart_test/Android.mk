LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := uart_test
LOCAL_SRC_FILES := uart_test.c
LOCAL_SHARED_LIBRARIES := \
        libcutils \
        libutils
include $(BUILD_EXECUTABLE)
