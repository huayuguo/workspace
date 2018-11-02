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

#ifndef VENDOR_MEDIATEK_HARDWARE_GNSS_V1_1_SATELLITEMODE_H
#define VENDOR_MEDIATEK_HARDWARE_GNSS_V1_1_SATELLITEMODE_H

#include <vendor/mediatek/hardware/gnss/1.1/ISatelliteMode.h>
#include <hidl/MQDescriptor.h>
#include <hidl/Status.h>
#include <hardware/gps_mtk.h>

namespace vendor {
namespace mediatek {
namespace hardware {
namespace gnss {
namespace V1_1 {
namespace implementation {

using ::android::hidl::base::V1_0::DebugInfo;
using ::android::hidl::base::V1_0::IBase;
using ::vendor::mediatek::hardware::gnss::V1_1::ISatelliteMode;
using ::android::hardware::hidl_array;
using ::android::hardware::hidl_memory;
using ::android::hardware::hidl_string;
using ::android::hardware::hidl_vec;
using ::android::hardware::Return;
using ::android::hardware::Void;
using ::android::sp;

/* Interface for GNSS Debug support. */
struct SatelliteMode : public ISatelliteMode {
    SatelliteMode(const SatelliteModeInterface* smIface);

    // Methods from ::vendor::mediatek::hardware::gnss::V1_1::IVzwDebug follow.
    Return<void> setSatelliteMode(int mode) override;

 private:
    const SatelliteModeInterface* mHalSMIface = nullptr;   // HAL interface pointer
};

}  // namespace implementation
}  // namespace V1_1
}  // namespace gnss
}  // namespace hardware
}  // namespace mediatek
}  // namespace vendor

#endif  // VENDOR_MEDIATEK_HARDWARE_GNSS_V1_1_VZWDEBUG_H
