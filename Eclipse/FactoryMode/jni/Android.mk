LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

TARGET_PLATFORM := android-3
LOCAL_MODULE    := libfactory_test_jni
LOCAL_SRC_FILES := em3096_jni.c \
									SerialPort.c
LOCAL_SHARED_LIBRARIES    := liblog
include $(BUILD_SHARED_LIBRARY)