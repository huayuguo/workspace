#ifndef __MTK_LBS_UTILITY_H__
#define __MTK_LBS_UTILITY_H__

#include <time.h>
#include <stdint.h>
#include <cutils/sockets.h>
#include <log/log.h>     /*logging in logcat*/

#ifdef __cplusplus
extern "C" {
#endif

/*************************************************
* Basic Utilities
**************************************************/
#ifndef UNUSED
#define UNUSED(x) (x)=(x)
#endif


void tag_log(int type, const char* tag, const char *fmt, ...);

#ifdef LOGD
#undef LOGD
#endif
#ifdef LOGW
#undef LOGW
#endif
#ifdef LOGE
#undef LOGE
#endif
#define LOGD(fmt, arg ...) ALOGD("%s: " fmt, __FUNCTION__ , ##arg)
#define LOGW(fmt, arg ...) ALOGW("%s: " fmt, __FUNCTION__ , ##arg)
#define LOGE(fmt, arg ...) ALOGE("%s: " fmt, __FUNCTION__ , ##arg)
void msleep(int interval);

#define MNLD_STRNCPY(dst,src,size) do{\
                                       strncpy((char *)(dst), (src), (size - 1));\
                                      (dst)[size - 1] = '\0';\
                                     }while(0)

// in millisecond
time_t get_tick();

// in millisecond
time_t get_time_in_millisecond();

/*************************************************
* Epoll
**************************************************/
// -1 means failure
int epoll_add_fd(int epfd, int fd);

// -1 failed
int epoll_add_fd2(int epfd, int fd, uint32_t events);

// -1 failed
int epoll_del_fd(int epfd, int fd);

int epoll_mod_fd(int epfd, int fd, uint32_t events);

/*************************************************
* Local UDP Socket
**************************************************/
// -1 means failure
int socket_bind_udp(const char* path);

// -1 means failure
int socket_set_blocking(int fd, int blocking);

// -1 means failure
int safe_sendto(const char* path, const char* buff, int len);

// -1 means failure
int safe_recvfrom(int fd, char* buff, int len);

#ifdef __cplusplus
}
#endif

#endif
