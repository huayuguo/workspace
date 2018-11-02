LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := vendor.mediatek.hardware.gnss@1.1-impl
LOCAL_PROPRIETARY_MODULE := true
LOCAL_MODULE_OWNER := mtk
LOCAL_MODULE_RELATIVE_PATH := hw
LOCAL_SRC_FILES := \
    ThreadCreationWrapper.cpp \
    AGnss.cpp \
    AGnssRil.cpp \
    Gnss.cpp \
    GnssBatching.cpp \
    GnssDebug.cpp \
    GnssGeofencing.cpp \
    GnssMeasurement.cpp \
    GnssNavigationMessage.cpp \
    GnssNi.cpp \
    GnssXtra.cpp \
    GnssConfiguration.cpp \
    GnssUtils.cpp \
    MtkGnss.cpp \
    VzwDebug.cpp \

LOCAL_C_INCLUDES += $(MTK_PATH_SOURCE)/hardware/connectivity/gps/gps_hal/inc

LOCAL_SHARED_LIBRARIES := \
    liblog \
    libhidlbase \
    libhidltransport \
    libutils \
    android.hardware.gnss@1.0 \
    libhardware \
    vendor.mediatek.hardware.gnss@1.1_vendor \

LOCAL_CFLAGS += -Werror

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE_RELATIVE_PATH := hw
LOCAL_PROPRIETARY_MODULE := true
LOCAL_MODULE_OWNER := mtk
LOCAL_MODULE := vendor.mediatek.hardware.gnss@1.1-service
LOCAL_INIT_RC := vendor.mediatek.hardware.gnss@1.1-service.rc
LOCAL_SRC_FILES := \
    service.cpp \

LOCAL_SHARED_LIBRARIES := \
    liblog \
    libcutils \
    libdl \
    libbase \
    libutils \
    libhardware \
    libbinder \

LOCAL_SHARED_LIBRARIES += \
    libhidlbase \
    libhidltransport \
    android.hardware.gnss@1.0 \
    vendor.mediatek.hardware.gnss@1.1_vendor

include $(BUILD_EXECUTABLE)
