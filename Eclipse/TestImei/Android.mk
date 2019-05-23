LOCAL_PATH:= $(call my-dir)

# Build the Settings APK
include $(CLEAR_VARS)


LOCAL_PACKAGE_NAME := TestImei
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_MODULE_TAGS := optional
LOCAL_USE_AAPT2 := true

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_STATIC_ANDROID_LIBRARIES := \
    android-support-v4 \
    android-support-v13 \
    android-support-v7-appcompat \
    android-support-v7-cardview \
    android-support-v7-recyclerview \

LOCAL_JAVA_LIBRARIES := \
    telephony-common

LOCAL_PROGUARD_FLAG_FILES := proguard.flags


include $(BUILD_PACKAGE)

