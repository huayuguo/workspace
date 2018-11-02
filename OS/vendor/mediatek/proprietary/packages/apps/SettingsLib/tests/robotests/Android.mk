# Copyright (C) 2016 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


############################################################
# SettingsLib Shell app just for Robolectric test target.  #
############################################################
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := MtkSettingsLibShell
LOCAL_MODULE_TAGS := optional

LOCAL_PRIVILEGED_MODULE := true

LOCAL_RESOURCE_DIR := \
    $(LOCAL_PATH)/res

include vendor/mediatek/proprietary/packages/apps/SettingsLib/common.mk

include $(BUILD_PACKAGE)

#############################################
# SettingsLib Robolectric test target. #
#############################################
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)

# Include the testing libraries (JUnit4 + Robolectric libs).
LOCAL_STATIC_JAVA_LIBRARIES := \
    mockito-robolectric-prebuilt \
    truth-prebuilt

LOCAL_JAVA_LIBRARIES := \
    junit \
    platform-robolectric-prebuilt

LOCAL_INSTRUMENTATION_FOR := MtkSettingsLibShell
LOCAL_MODULE := MtkSettingsLibRoboTests

LOCAL_MODULE_TAGS := optional

include $(BUILD_STATIC_JAVA_LIBRARY)

#############################################################
# SettingsLib runner target to run the previous target. #
#############################################################
include $(CLEAR_VARS)

LOCAL_MODULE := MtkRunSettingsLibRoboTests

LOCAL_SDK_VERSION := current

LOCAL_STATIC_JAVA_LIBRARIES := \
    MtkSettingsLibRoboTests

LOCAL_TEST_PACKAGE := MtkSettingsLibShell

include prebuilts/misc/common/robolectric/run_robotests.mk
