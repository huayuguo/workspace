LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	testsurface.cpp

LOCAL_C_INCLUDES := \
	external/skia/include/core

LOCAL_SHARED_LIBRARIES := \
	libcutils \
	libutils \
	libbinder \
    libui \
    libgui \
    libskia

LOCAL_MODULE:= testsurface

LOCAL_MODULE_TAGS := tests

include $(BUILD_EXECUTABLE)