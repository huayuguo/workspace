#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>
#include <linux/ioctl.h>
#include <sys/ioctl.h>
#include <errno.h>

#include "em3096_jni.h"

//#include "android/log.h"
#include "utils/Log.h"
//#include <android/log.h>
#define LOG_TAG "Em3096Native"


#define EM3096_IOC	'R'

#define EM3096_IOCTRL_POWER_ON  _IO(EM3096_IOC, 0x00)
#define EM3096_IOCTRL_POWER_OFF _IO(EM3096_IOC, 0x01)
#define EM3096_IOCTRL_RST_ON    _IO(EM3096_IOC, 0x02)
#define EM3096_IOCTRL_RST_OFF   _IO(EM3096_IOC, 0x03)
#define EM3096_IOCTRL_NTFIG_ON    _IO(EM3096_IOC, 0x04)
#define EM3096_IOCTRL_NTFIG_OFF   _IO(EM3096_IOC, 0x05)

const char *EM3096_DEV_FILE = "/dev/em3096";

int gEm3096Fd = -1;

JNIEXPORT void JNICALL Java_com_yjzn_Em3096Native_power(JNIEnv *env, jobject thiz,jint poweron)
{
        int res;

        gEm3096Fd = open(EM3096_DEV_FILE, O_RDONLY);
        if (gEm3096Fd <= 0)
        {
                ALOGD("open em3096 file error , errno:%d", errno);
                //return errno;
        }
        if (poweron != 0)
        {
                ALOGD("em3096 power on");
                res = ioctl(gEm3096Fd, EM3096_IOCTRL_POWER_ON, 0);
                if (res < 0) {
                        ALOGE("power on: ioctl failed (%s)\n", strerror(errno));
                }
        }
        else
        {
                ALOGD("em3096 power off");
                res = ioctl(gEm3096Fd, EM3096_IOCTRL_POWER_OFF, 0);
                if (res < 0) {
                        ALOGE("power off: ioctl failed (%s)\n", strerror(errno));
                }
        }
        close(gEm3096Fd);
        //return 0;
}

JNIEXPORT void JNICALL Java_com_yjzn_Em3096Native_start_1scan
  (JNIEnv *env, jobject thiz)
{
        int res;

        gEm3096Fd = open(EM3096_DEV_FILE, O_RDONLY);
        if (gEm3096Fd <= 0)
        {
                ALOGE("open em3096 file error , errno:%d", errno);
                return;
        }

        ALOGD("em3096 start scan");
        res = ioctl(gEm3096Fd, EM3096_IOCTRL_NTFIG_ON, 0);
        if (res < 0) {
                ALOGE("NTFIG_ON: ioctl failed (%s)\n", strerror(errno));
        }

        close(gEm3096Fd);
}

JNIEXPORT void JNICALL Java_com_yjzn_Em3096Native_stop_1scan
  (JNIEnv *env, jobject thiz)
{
        int res;

        gEm3096Fd = open(EM3096_DEV_FILE, O_RDONLY);
        if (gEm3096Fd <= 0)
        {
                ALOGE("open em3096 file error , errno:%d", errno);
                return ;
        }

        ALOGD("em3096 stop scan");
        res = ioctl(gEm3096Fd, EM3096_IOCTRL_NTFIG_OFF, 0);
        if (res < 0) {
                ALOGE("NTFIG_OFF: ioctl failed (%s)\n", strerror(errno));
        }

        close(gEm3096Fd);
}
