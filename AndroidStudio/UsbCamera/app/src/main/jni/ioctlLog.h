#ifndef __IOCTL_LOG_H__
#define __IOCTL_LOG_H__

#ifdef __cplusplus
extern "C" {
#endif

#define ENTER_FUNC_LOG(...) LOGE("Enter into %s", __FUNCTION__);
#define EXIT_FUNC_LOG(...)  LOGE("Exit from %s", __FUNCTION__);

#define  UVCLOG_TAG    "WebCam"
#define  LOGE(...)  //__android_log_print(ANDROID_LOG_ERROR,UVCLOG_TAG,__VA_ARGS__)

#define SUCCESS_LOCAL   (0)
#define ERROR_LOCAL     (-1)
#define NO_DEIVCE       (-2)
#define INVARG_LOCAL    (-3)
#define DEVICE_NOT_OPEN (-4)

#define IMG_WIDTH	1920//640
#define IMG_HEIGHT	1080//480

void printArgInfo(long request ,void *arg);
int errnoexit(const char *s);

#ifdef __cplusplus
}
#endif

#endif // __IOCTL_LOG_H__