#include <string.h>

//#include <utils/Log.h>
#include <errno.h>

#include <linux/videodev2.h>
#include <sys/ioctl.h>

#include "ioctlLog.h"

#define CLEAR(x) memset(x, 0, sizeof (*x))

static int xioctl(int fh, int request, void *arg)
{
    int r;

    do {
        LOGE( "Running Request: 0x%x", request);
        printArgInfo(request, arg);
        r = ioctl(fh, request, arg);
    } while (-1 == r && EINTR == errno);

    LOGE( "After Running Request: 0x%x", request);
    printArgInfo(request, arg);
    
    return r;
}

int v4l2_ioctl_fmtdesc(int fd, struct v4l2_fmtdesc *pFmtdesc) {
    if (NULL != pFmtdesc && -1 != fd) {
        CLEAR(pFmtdesc);
        
        pFmtdesc->index = 0;
        pFmtdesc->type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        do {
            if ( -1 == xioctl(fd, VIDIOC_ENUM_FMT, pFmtdesc)) {
               errnoexit("VIDIOC_ENUM_FMT");
            }
            
            pFmtdesc->type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
            pFmtdesc->index++ ;
            
        }while (EINVAL != errno);
    } else {
        return errnoexit("Invalid arguments");
    }

    return SUCCESS_LOCAL;
}

int v4l2_ioctl_querycap(int fd, struct v4l2_capability *pCap) {
    if (-1 != fd && NULL != pCap) {
        CLEAR(pCap);
        
    	if (-1 == xioctl (fd, VIDIOC_QUERYCAP, pCap)) {
    		if (EINVAL == errno) {
    			LOGE("This device is no V4L2 device");
    			return ERROR_LOCAL;
    		} else {
    			return errnoexit ("VIDIOC_QUERYCAP");
    		}
    	}

    	if (!(pCap->capabilities & V4L2_CAP_VIDEO_CAPTURE)) {
    		LOGE("This device is no video capture device");
    		return ERROR_LOCAL;
    	}

    	if (!(pCap->capabilities & V4L2_CAP_STREAMING)) {
    		LOGE("This device does not support streaming i/o");
    		return ERROR_LOCAL;
    	}
    }else {
        return errnoexit("Invalid arguments");
    }

    return SUCCESS_LOCAL;
}

int v4l2_ioctl_cropcap(int fd, struct v4l2_cropcap *pCropcap) {
    if (-1 != fd && NULL != pCropcap) {
    	CLEAR (pCropcap);

    	pCropcap->type = V4L2_BUF_TYPE_VIDEO_CAPTURE;

    	if (0 != xioctl (fd, VIDIOC_CROPCAP, pCropcap)) {
    	    LOGE("Command: VIDIOC_CROPCAP error, errno: %d, %s", errno, strerror(errno));
            return ERROR_LOCAL;
    	}
    }else {
        return errnoexit("Invalid arguments");
    }

    return SUCCESS_LOCAL;
}

int v4l2_ioctl_set_crop(int fd, struct v4l2_crop *pCrop, struct v4l2_cropcap *pCropcap) {
    if (-1 != fd && NULL != pCrop) {
        CLEAR(pCrop);
        
        pCrop->type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        pCrop->c = pCropcap->defrect; 
        
        if (-1 == xioctl (fd, VIDIOC_S_CROP, pCrop)) {
            if (EINVAL == errno) {
                LOGE("VIDIOC_S_CROP isn't supported");
            }
        }
    } else {
        return errnoexit("Invalid arguments");
    }

    return SUCCESS_LOCAL;
}

int v4l2_ioctl_get_fmt(int fd, struct v4l2_format *pFormat) {    
    if (-1 != fd && NULL != pFormat) {
        CLEAR(pFormat);
        
        pFormat->type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        if (0 != xioctl(fd, VIDIOC_G_FMT, pFormat)) {
            return errnoexit("VIDIOC_G_FMT");
        }
    } else {
        return errnoexit("Invalid arguments");
    }

    return SUCCESS_LOCAL;
}

int v4l2_ioctl_set_fmt(int fd, struct v4l2_format *pFormat) {
    int retry = 0;
    int maxRetry = 3;
    
    if (-1 != fd && NULL != pFormat) {
        CLEAR(pFormat);
        
    	pFormat->type                = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    	pFormat->fmt.pix.width       = IMG_WIDTH; 
    	pFormat->fmt.pix.height      = IMG_HEIGHT;
    	pFormat->fmt.pix.pixelformat = V4L2_PIX_FMT_MJPEG; // assumed this device supports MJPEG 
    	pFormat->fmt.pix.field       = V4L2_FIELD_INTERLACED;

        for (retry = 0; retry < maxRetry; retry++) {
            if (0 != xioctl (fd, VIDIOC_S_FMT, pFormat)) {
        		LOGE("Command VIDIOC_S_FMT error, retry...");
                continue;
            }
        }
    } else {
        return errnoexit("Invalid arguments");
    }
    return SUCCESS_LOCAL;
}

int v4l2_ioctl_set_streamparam(int fd, struct v4l2_streamparm *pStreamparam) {
    if (-1 != fd && NULL != pStreamparam) {
        CLEAR(pStreamparam);
        
        pStreamparam->type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        pStreamparam->parm.capture.timeperframe.numerator = 1;
        pStreamparam->parm.capture.timeperframe.denominator = 30;
        if (0 != xioctl(fd, VIDIOC_S_PARM, pStreamparam)) {
            return errnoexit("VIDIOC_S_PARM");
        }
    } else {
        return errnoexit("Invalid arguments");
    }

    return SUCCESS_LOCAL;
}

int v4l2_ioctl_get_streamparam(int fd, struct v4l2_streamparm *pStreamparam) {
    if (-1 != fd && NULL != pStreamparam) {
        CLEAR(pStreamparam);
        
        pStreamparam->type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        if (0 != xioctl(fd, VIDIOC_G_PARM, pStreamparam)) {
            return errnoexit("VIDIOC_G_PARM");
        }
    } else {
        return errnoexit("Invalid arguments");
    }

    return SUCCESS_LOCAL;
}

int v4l2_ioctl_supported_framesize(int fd, struct v4l2_frmsizeenum *pFrmsize, int *pPixelFormat, int len) {
    int idx = 0;
    
    if (-1 != fd && NULL != pFrmsize && NULL != pPixelFormat) {
        CLEAR(pFrmsize);

        pFrmsize->index = 0;
        for (idx = 0; idx < len; idx++) {
            pFrmsize->pixel_format = pPixelFormat[idx];
            do {
                if (0 == xioctl(fd, VIDIOC_ENUM_FRAMESIZES, pFrmsize)) {
                    if (pFrmsize->type == V4L2_FRMSIZE_TYPE_DISCRETE) {
                        CLEAR(pFrmsize);

                        pFrmsize->index++;
                        pFrmsize->pixel_format = pPixelFormat[idx];
                        continue;
                    } else {
                        break;
                    }
                } else {
                    if (EINVAL == errno && pFrmsize->type == V4L2_FRMSIZE_TYPE_DISCRETE) {
                        LOGE("End to list supported frame size");
                    }
                    break;
                }
            } while(1);
        }
    } else {
        return errnoexit("Invalid arguments");
    }

    return SUCCESS_LOCAL;
}

int v4l2_ioctl_reqbuf(int fd, struct v4l2_requestbuffers *pReqBuf) {
    if (-1 != fd && NULL != pReqBuf) {
    	CLEAR (pReqBuf);
        
    	pReqBuf->count               = 4;
    	pReqBuf->type                = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    	pReqBuf->memory              = V4L2_MEMORY_MMAP;
        
    	if (0 != xioctl (fd, VIDIOC_REQBUFS, pReqBuf)) {
    		if (EINVAL == errno) {
    			LOGE("This device does not support memory mapping");
    			return ERROR_LOCAL;
    		} else {
    			return errnoexit ("VIDIOC_REQBUFS");
    		}
	    }
    } else {
        return errnoexit("Invalid arguments");
    }

    return SUCCESS_LOCAL;
}

int v4l2_ioctl_querybuf(int fd, struct v4l2_buffer *pBuf, int bufIndex) {
    if (-1 != fd && NULL != pBuf) {
        CLEAR(pBuf);
        
		pBuf->type        = V4L2_BUF_TYPE_VIDEO_CAPTURE;
		pBuf->memory      = V4L2_MEMORY_MMAP;
		pBuf->index       = bufIndex;

		if (0 != xioctl (fd, VIDIOC_QUERYBUF, pBuf))
			return errnoexit ("VIDIOC_QUERYBUF");
    } else {
        return errnoexit("Invalid arguments");
    }

    return SUCCESS_LOCAL;
}

int v4l2_ioctl_qbuf(int fd, struct v4l2_buffer *pBuf, int bufIndex) {
    if (-1 != fd && NULL != pBuf) {
        CLEAR (pBuf);
        
        pBuf->type        = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        pBuf->memory      = V4L2_MEMORY_MMAP;
        pBuf->index       = bufIndex;
        
        if (0 != xioctl (fd, VIDIOC_QBUF, pBuf)) {
            return errnoexit ("VIDIOC_QBUF");
        }
    }else {
        return errnoexit("Invalid arguments");
    }

    return SUCCESS_LOCAL;
}

int v4l2_ioctl_dqbuf(int fd, struct v4l2_buffer *pBuf) {
    if (-1 != fd && NULL != pBuf) {
    	CLEAR (pBuf);    
        
    	pBuf->type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    	pBuf->memory = V4L2_MEMORY_MMAP;

    	if (-1 == xioctl (fd, VIDIOC_DQBUF, pBuf)) {
            LOGE("Command VIDIOC_DQBUF error, errno: %d, %s", errno, strerror(errno));
    		switch (errno) {
    			case EAGAIN:
    			case EIO:
    			default:
    				errnoexit ("VIDIOC_DQBUF");
                    return errno;
    		}
    	}
    } else {
        return errnoexit("Invalid arguments");
    }

    return SUCCESS_LOCAL;
}

int v4l2_ioctl_streamon(int fd) {
	enum v4l2_buf_type type;

    if (-1 != fd) {
    	type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        
    	if (0 != xioctl (fd, VIDIOC_STREAMON, &type)) {
    		return errnoexit ("VIDIOC_STREAMON");
        }
    } else {
        return errnoexit("Invalid arguments");
    }

    return SUCCESS_LOCAL;
}

int v4l2_ioctl_streamoff(int fd) {
	enum v4l2_buf_type type;

    if (-1 != fd) {
    	type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        
    	if (0 != xioctl (fd, VIDIOC_STREAMOFF, &type)) {
    		return errnoexit ("VIDIOC_STREAMON");
        }
    } else {
        return errnoexit("Invalid arguments");
    }

    return SUCCESS_LOCAL;
}

