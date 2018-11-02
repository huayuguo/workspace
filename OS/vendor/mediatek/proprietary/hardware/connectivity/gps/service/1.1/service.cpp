#define LOG_TAG "vendor.mediatek.hardware.gnss@1.1-service"

#include <vendor/mediatek/hardware/gnss/1.1/IMtkGnss.h>

#include <hidl/LegacySupport.h>

#include <binder/ProcessState.h>

using vendor::mediatek::hardware::gnss::V1_1::IMtkGnss;
using android::hardware::defaultPassthroughServiceImplementation;

int main() {
    // The GNSS HAL may communicate to other vendor components via
    // /dev/vndbinder
    android::ProcessState::initWithDriver("/dev/vndbinder");
    return defaultPassthroughServiceImplementation<IMtkGnss>();
}
