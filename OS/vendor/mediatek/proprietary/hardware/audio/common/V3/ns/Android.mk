#
# Copyright (C) 2014 MediaTek Inc.
# Modification based on code covered by the mentioned copyright
# and/or permission notice(s).
#
#
# Copyright (C) 2008 The Android Open Source Project
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
#
LOCAL_PATH := $(my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES += \
	noise_process.c \
	noise_suppression.c

LOCAL_SHARED_LIBRARIES := \
    libcutils

LOCAL_CFLAGS += -Werror -std=gnu99 -lm
LOCAL_C_INCLUDES := $(libcutils_c_includes)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := libwebrtcns

include $(BUILD_STATIC_LIBRARY)
