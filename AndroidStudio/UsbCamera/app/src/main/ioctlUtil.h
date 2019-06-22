#ifndef __IOCTL_UTIL_H__
#define __IOCTL_UTIL_H__

#include <linux/videodev2.h>
#include <linux/usbdevice_fs.h>


int v4l2_ioctl_fmtdesc(int fd, struct v4l2_fmtdesc *pFmtdesc); 
int v4l2_ioctl_querycap(int fd, struct v4l2_capability *pCap); 
int v4l2_ioctl_cropcap(int fd, struct v4l2_cropcap *pCropcap); 
int v4l2_ioctl_set_crop(int fd, struct v4l2_crop *pCrop, struct v4l2_cropcap *pCropcap);
int v4l2_ioctl_get_fmt(int fd, struct v4l2_format *pFormat); 
int v4l2_ioctl_set_fmt(int fd, struct v4l2_format *pFormat); 
int v4l2_ioctl_set_streamparam(int fd, struct v4l2_streamparm *pStreamparam); 
int v4l2_ioctl_get_streamparam(int fd, struct v4l2_streamparm *pStreamparam);
int v4l2_ioctl_supported_framesize(int fd, struct v4l2_frmsizeenum *pFrmsize, int *pPixelFormat, int len);
int v4l2_ioctl_reqbuf(int fd, struct v4l2_requestbuffers *pReqBuf) ;
int v4l2_ioctl_querybuf(int fd, struct v4l2_buffer *pBuf, int bufIndex); 
int v4l2_ioctl_qbuf(int fd, struct v4l2_buffer *pBuf, int bufIndex) ;
int v4l2_ioctl_dqbuf(int fd, struct v4l2_buffer *pBuf) ;
int v4l2_ioctl_streamon(int fd) ;
int v4l2_ioctl_streamoff(int fd);


#endif // __IOCTL_UTIL_H__