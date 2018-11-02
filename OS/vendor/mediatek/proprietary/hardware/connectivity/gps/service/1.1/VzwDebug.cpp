/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "VzwDebugInterface"

#include <log/log.h>

#include "VzwDebug.h"

namespace vendor {
namespace mediatek {
namespace hardware {
namespace gnss {
namespace V1_1 {
namespace implementation {

sp<IVzwDebugCallback> VzwDebug::sVzwDebugCbIface = nullptr;
VzwDebugCallbacks VzwDebug::sHalVzwDeubgCb = {
    .vzw_debug_cb = vzwDebugMessageCb,
};


VzwDebug::VzwDebug(const VzwDebugInterface* vzwDebugIface) : mHalVzwDebugIface(vzwDebugIface) {}


// Methods from ::vendor::mediatek::hardware::gnss::V1_1::IVzwDebug follow.
Return<bool> VzwDebug::init(const sp<IVzwDebugCallback>& callback)  {
    ALOGE("%s: Vzw debug init, to set callback", __func__);
    if (mHalVzwDebugIface == nullptr) {
        ALOGE("%s: Vzw debug HAL interface is unavailable", __func__);
        return false;
    }

    sVzwDebugCbIface = callback;

    return (mHalVzwDebugIface->init(&sHalVzwDeubgCb) == 0);
}


void VzwDebug::vzwDebugMessageCb(VzwDebugData* vzw_message) {
    if (sVzwDebugCbIface == nullptr) {
        ALOGE("%s: Vzw Debug Callback Interface configured incorrectly", __func__);
        return;
    }

    IVzwDebugCallback::VzwDebugData* pMsg = new IVzwDebugCallback::VzwDebugData();
    pMsg->size = strlen(vzw_message->vzw_msg_data);
    if (pMsg->size >= VZW_DEBUG_STRING_MAXLEN) {
        pMsg->size = VZW_DEBUG_STRING_MAXLEN - 1;
    }

    uint8_t *dst = reinterpret_cast<uint8_t *>(&pMsg->vzw_msg_data[0]);
    const uint8_t *src = reinterpret_cast<uint8_t *>(&vzw_message->vzw_msg_data[0]);
    memcpy(dst, src, pMsg->size + 1);

    auto ret = sVzwDebugCbIface->vzwDebugCb(*pMsg);
    if (!ret.isOk()) {
        ALOGE("%s: Unable to invoke callback", __func__);
    }
    delete pMsg;
}



// Methods from ::android::hardware::gnss::V1_0::IGnssDebug follow.
Return<void> VzwDebug::setVzwDebugScreen(bool enabled)  {
    if (mHalVzwDebugIface == nullptr) {
        ALOGE("%s: Vzw debug HAL interface is unavailable", __func__);
        return Void();
    }

    mHalVzwDebugIface->set_vzw_debug_screen(enabled);
    return Void();
}

}  // namespace implementation
}  // namespace V1_1
}  // namespace gnss
}  // namespace hardware
}  // namespace mediatek
}  // namespace vendor
