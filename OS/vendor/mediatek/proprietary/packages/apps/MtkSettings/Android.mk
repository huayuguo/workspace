LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
        $(call all-logtags-files-under, src)

LOCAL_MODULE := mtksettings-logtags

include $(BUILD_STATIC_JAVA_LIBRARY)

# Build the Settings APK
include $(CLEAR_VARS)

ifeq ($(strip $(MTK_CLEARMOTION_SUPPORT)),no)
# if not support clearmotion, load a small video for clearmotion
LOCAL_ASSET_DIR := $(LOCAL_PATH)/assets_no_clearmotion
else
LOCAL_ASSET_DIR := $(LOCAL_PATH)/assets_clearmotion
endif

LOCAL_PACKAGE_NAME := MtkSettings
LOCAL_OVERRIDES_PACKAGES := Settings
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_MODULE_TAGS := optional
LOCAL_USE_AAPT2 := true

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res_ext

LOCAL_STATIC_ANDROID_LIBRARIES := \
    android-support-v4 \
    android-support-v13 \
    android-support-v7-appcompat \
    android-support-v7-cardview \
    android-support-v7-preference \
    android-support-v7-recyclerview \
    android-support-v14-preference

LOCAL_JAVA_LIBRARIES := \
    bouncycastle \
    core-oj \
    telephony-common \
    ims-common \
    mediatek-framework \
    mediatek-common \
    mediatek-telephony-base \
    mediatek-telephony-common \
    mediatek-ims-common

LOCAL_STATIC_JAVA_LIBRARIES := \
    jsr305 \
    mtksettings-logtags \
    com.mediatek.lbs.em2.utils \
    com.mediatek.settings.ext \
    nfc_settings_adapter

ifeq ($(strip $(MTK_HDMI_SUPPORT)), yes)
ifeq ($(strip $(MTK_PLATFORM)), MT8173)
LOCAL_JAVA_LIBRARIES += hdmimanager
endif
ifeq ($(strip $(MTK_PLATFORM)), MT8167)
LOCAL_JAVA_LIBRARIES += hdmimanager
endif
ifeq ($(strip $(MTK_PLATFORM)), MT6735)
LOCAL_JAVA_LIBRARIES += hdmimanager
endif
ifeq ($(strip $(MTK_PLATFORM)), MT8163)
LOCAL_JAVA_LIBRARIES += hdmimanager
endif
ifeq ($(strip $(MTK_PLATFORM)), MT6771)
LOCAL_JAVA_LIBRARIES += hdmimanager
endif
endif

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

ifneq ($(INCREMENTAL_BUILDS),)
    LOCAL_PROGUARD_ENABLED := disabled
    LOCAL_JACK_ENABLED := incremental
    LOCAL_JACK_FLAGS := --multi-dex native
endif

include frameworks/opt/setupwizard/library/common-gingerbread.mk
include vendor/mediatek/proprietary/packages/apps/SettingsLib/common.mk

include $(BUILD_PACKAGE)

# Use the following include to make our test apk.
ifeq (,$(ONE_SHOT_MAKEFILE))
include $(call all-makefiles-under,$(LOCAL_PATH))
endif
