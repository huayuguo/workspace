#include "jni.h"

//#include <utils/Log.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>              /* low-level i/o */
#include <unistd.h>
#include <errno.h>
#include <malloc.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/time.h>
#include <sys/mman.h>
#include <sys/ioctl.h>

#include <asm/types.h>          /* for videodev2.h */

#include <linux/videodev2.h>
#include <linux/usbdevice_fs.h>

#include "ioctlLog.h"

static void printV4l2Buffer(struct v4l2_buffer *pBuf) {
    if (NULL != pBuf) {
        LOGE("\ttype: %d", pBuf->type);
        LOGE( "\tindex: %d", pBuf->index);
        LOGE( "\tbytesused: %d", pBuf->bytesused);
        LOGE( "\tflags: %d", pBuf->flags);
        LOGE( "\tfield: %d", pBuf->field);
        LOGE( "\tsequence: %d", pBuf->sequence);
        LOGE( "\tmemory: %d", pBuf->memory);
        LOGE( "\toffset: %d", pBuf->m.offset);
        LOGE( "\tlength: %d", pBuf->length);
        //LOGE( "\tinput: %d", pBuf->input);
    }
}

static void printV4l2Requestbuffers(struct v4l2_requestbuffers *pBuf) {
    if (NULL != pBuf) {
        LOGE( "\tcount: %d", pBuf->count);
        LOGE( "\ttype: %d", pBuf->type);
        LOGE( "\tmemory: %d", pBuf->memory);
    }
}

static void printV4l2Fmtdesc(struct v4l2_fmtdesc *pFmtdesc) {
    if (NULL != pFmtdesc) {
        LOGE("\tindex: 0x%x", pFmtdesc->index);
        LOGE("\ttype: 0x%x", pFmtdesc->type);
        LOGE("\tflags: 0x%x", pFmtdesc->flags);
        LOGE("\tpixelformat: 0x%x", pFmtdesc->pixelformat);
        LOGE("\tdesc: %s", (char* )(pFmtdesc->description));
    }
}

static void printV4l2Capability(struct v4l2_capability *pBuf) {
    if (NULL != pBuf) {
        LOGE( "\tcapabilities: 0x%x", pBuf->capabilities);
        LOGE( "\tdevice_caps: 0x%x", pBuf->device_caps);
    }
}

static void printV4l2CropCap(struct v4l2_cropcap * pCropcap) {
    if (NULL != pCropcap) {
        LOGE( "\ttype: %d", pCropcap->type);
        LOGE( "\tbounds(left=%d, top=%d, height=%d, width=%d)", pCropcap->bounds.left, pCropcap->bounds.top, pCropcap->bounds.width, pCropcap->bounds.height);
        LOGE( "\tdefrect(left=%d, top=%d, height=%d, width=%d)", pCropcap->defrect.left, pCropcap->defrect.top, pCropcap->defrect.width, pCropcap->defrect.height);
        LOGE( "\tpixelaspect(numerator=%d, denominator=%d)", pCropcap->pixelaspect.numerator, pCropcap->pixelaspect.denominator);
    }
}

static void printV4l2Crop(struct v4l2_crop *pCrop) {
    if (NULL != pCrop) {
        LOGE( "\ttype: %d", pCrop->type);
        LOGE( "\tc(left=%d, top=%d, height=%d, width=%d)", pCrop->c.left, pCrop->c.top, pCrop->c.width, pCrop->c.height);
    }
}

static void printV4l2Format(struct v4l2_format *pFormat) {
    if (NULL != pFormat) {
        LOGE( "\ttype: %d", pFormat->type);
        LOGE( "\tfmt.pix(width=%d, height=%d, pixelformat=0x%x, filed=%d, bytesperline=%d, sizeimage=%d, colorspace=%d)", \
            pFormat->fmt.pix.width, pFormat->fmt.pix.height, pFormat->fmt.pix.pixelformat, \
            pFormat->fmt.pix.field, pFormat->fmt.pix.bytesperline, pFormat->fmt.pix.sizeimage, \
            pFormat->fmt.pix.colorspace);
    }
}

static void printV4l2Streamparm(struct v4l2_streamparm *pParam) {
    if (NULL != pParam) {
        LOGE( "\ttype: %d", pParam->type);
        LOGE( "\tparm.capture(cap=%d, capmode=%d, extendedmode=%d, readbuffers=%d, numerator=%d, denominator=%d)", \
            pParam->parm.capture.capability, pParam->parm.capture.capturemode, \
            pParam->parm.capture.extendedmode, pParam->parm.capture.readbuffers, \
            pParam->parm.capture.timeperframe.numerator, pParam->parm.capture.timeperframe.denominator);
    }
}

static void printV4l2Framesize(struct v4l2_frmsizeenum *pFrmsizeenum) {
    if (NULL != pFrmsizeenum) {
        LOGE("\tindex: %d", pFrmsizeenum->index);
        LOGE("\tpixel_format: 0x%x", pFrmsizeenum->pixel_format);
        LOGE("\ttype: %d", pFrmsizeenum->type);
        switch(pFrmsizeenum->type) {
            case V4L2_FRMSIZE_TYPE_DISCRETE:
                LOGE("\tdiscrete.width: %d", pFrmsizeenum->discrete.width);
                LOGE("\tdiscrete.height: %d", pFrmsizeenum->discrete.height);
                break;
            case V4L2_FRMSIZE_TYPE_STEPWISE:
                LOGE("\tmin_width: %d", pFrmsizeenum->stepwise.min_width);
                LOGE("\tmin_height: %d", pFrmsizeenum->stepwise.min_height);
                LOGE("\tmax_width: %d", pFrmsizeenum->stepwise.max_width);
                LOGE("\tmax_height: %d", pFrmsizeenum->stepwise.max_height);
                LOGE("\tstep_width: %d", pFrmsizeenum->stepwise.step_width);
                LOGE("\tstep_height: %d", pFrmsizeenum->stepwise.step_height);
                break;
            case V4L2_FRMSIZE_TYPE_CONTINUOUS:
                break;
            default:
                break;
        }
    }
}

void printArgInfo(long request ,void *arg) {
    struct v4l2_buffer *pBuf;
    enum v4l2_buf_type *pType;
    struct v4l2_requestbuffers *pRequestBuf;
    struct v4l2_capability *pCap;
    struct v4l2_cropcap *pCropcap;
    struct v4l2_crop *pCrop;
    struct v4l2_format *pFormat;
    struct v4l2_streamparm *pParam;
    struct v4l2_fmtdesc *pFmtdesc;
    struct v4l2_frmsizeenum *pFrmsize;

    if (arg == NULL) {
        return;
    }
    
    switch(request) {
        case VIDIOC_DQBUF:
        {
            LOGE("Command: VIDIOC_DQBUF");
            pBuf = (struct v4l2_buffer *)arg;
            printV4l2Buffer(pBuf);
            break;
        }
        case VIDIOC_QBUF:
        {
            LOGE("Command: VIDIOC_QBUF");
            pBuf = (struct v4l2_buffer *)arg;
            printV4l2Buffer(pBuf);
            break;
        }
        case VIDIOC_QUERYBUF:
        {
            LOGE("Command: VIDIOC_QUERYBUF");
            pBuf = (struct v4l2_buffer *)arg;
            printV4l2Buffer(pBuf);
            break;
        }
        case VIDIOC_STREAMOFF:
        {
            LOGE("Command: VIDIOC_STREAMOFF");
            pType = (enum v4l2_buf_type *)arg;
            LOGE( "\ttype: %d", *pType);
            break;
        }
        case VIDIOC_STREAMON:
        {
            LOGE("Command: VIDIOC_STREAMON");
            pType = (enum v4l2_buf_type *)arg;
            LOGE( "\ttype: %d", *pType);
            break;
        }
        case VIDIOC_REQBUFS:
        {
            LOGE("Command: VIDIOC_REQBUFS");
            pRequestBuf = (struct v4l2_requestbuffers *)arg;
            printV4l2Requestbuffers(pRequestBuf);
            break;
        }
        case VIDIOC_QUERYCAP:
        {
            LOGE("Command: VIDIOC_QUERYCAP");
            pCap = (struct v4l2_capability *)arg;
            printV4l2Capability(pCap);
            break;
        }
        case VIDIOC_CROPCAP:
        {
            LOGE("Command: VIDIOC_CROPCAP");
            pCropcap = (struct v4l2_cropcap *)arg;
            printV4l2CropCap(pCropcap);
            break;
        }
        case VIDIOC_S_CROP:
        {
            LOGE("Command: VIDIOC_S_CROP");
            pCrop = (struct v4l2_crop *)arg;
            printV4l2Crop(pCrop);
            break;
        }
        case VIDIOC_S_FMT:
        {
            LOGE("Command: VIDIOC_S_FMT");
            pFormat = (struct v4l2_format *)arg;
            printV4l2Format(pFormat);
            break;
        }
        case VIDIOC_G_FMT:
        {
            LOGE("Command: VIDIOC_G_FMT");
            pFormat = (struct v4l2_format *)arg;
            printV4l2Format(pFormat);
            break;
        }
        case VIDIOC_G_PARM:
        {
            LOGE("Command: VIDIOC_G_PARM");
            pParam = (struct v4l2_streamparm *)arg;
            printV4l2Streamparm(pParam);
            break;
        }
        case VIDIOC_S_PARM:
        {
            LOGE("Command: VIDIOC_S_PARM");
            pParam = (struct v4l2_streamparm *)arg;
            printV4l2Streamparm(pParam);
            break;
        }
        case VIDIOC_ENUM_FMT:
        {
            LOGE("Command: VIDIOC_ENUM_FMT");
            pFmtdesc = (struct v4l2_fmtdesc *)arg;
            printV4l2Fmtdesc(pFmtdesc);
            break;
        }
        case VIDIOC_ENUM_FRAMESIZES:
        {
            LOGE("Command: VIDIOC_ENUM_FRAMESIZES");
            pFrmsize = (struct v4l2_frmsizeenum *)arg;
            printV4l2Framesize(pFrmsize);
        }
        default:
            LOGE( "No information provided");
    }

}

int errnoexit(const char *s)
{
	LOGE("%s error %d, %s", s, errno, strerror (errno));
	return ERROR_LOCAL;
}

