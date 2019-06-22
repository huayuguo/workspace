#include "jni.h"
#include "JNIHelp.h"

#include <unistd.h>
#include <utils/Log.h>
#include <android/bitmap.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <fcntl.h>              /* low-level i/o */
#include <errno.h>
#include <malloc.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/time.h>
#include <sys/mman.h>
#include <sys/ioctl.h>
#include <pthread.h>

#include <asm/types.h>          /* for videodev2.h */

#include <linux/videodev2.h>
#include <linux/usbdevice_fs.h>

#include "ioctlUtil.h"
#include "ioctlLog.h"
#include "avilib.h"


#define TRUE (1)
#define FALSE (0)
#define CLEAR(x) memset (&(x), 0, sizeof (x))
#define MIN_AVAILABLE_SIZE_FOR_VIDEO (2*1024*1024)
#define MIN_AVAILABLE_SIZE_FOR_IMAGE (20*1024)


#define FILEPATH_LEN (64)
#define IMAGE_FILE_PATH ("/sdcard/DCIM/Camera/")
#define VIDEO_FILE_PATH ("/sdcard/DCIM/Camera/")

#define FRAMEBUFFER_MAX_COUNT (10)

typedef struct FrameBuffer{
    char *buffer;
    int size;
    int usedSize;
}FrameBuffer;

typedef struct FrameBufferQueue{
    FrameBuffer frameBuffer[FRAMEBUFFER_MAX_COUNT];
    int readIndex;
    int writeIndex;
    int usedCount;
    int count;
}FrameBufferQueue;


struct buffer {
        void *                  start;
        size_t                  length;
};

static char             dev_name[16];
static int              fd              = -1;
struct buffer *         buffers         = NULL;
static unsigned int     n_buffers       = 0;

static int camerabase = -1;

static char *pImageBuf = NULL;
static int imageBufSize = 0;
static int realImageSize = 0;


static int image_index = 0;

/*
    Flag to capture image as file
    0: Not save the current frame to file
    1: Save the current frame to file
*/
static int _startCaptureImage = FALSE;

/* 
    Flag to indicate if this camera is opened normally
    0: Not opened
    1: Opend normally
*/
static int _cameraStart = FALSE;

/*
    Flag to indicate if the recording is required
    0: Not recording
    1: Recording
*/
static int _startRecording = FALSE;

/* the start time of this video file */
static struct timeval _recordingStart;

/* the end time of this recorded video file */
static struct timeval _recordingStop; 

/* Save the recorded video file path */
char videoFilePath[FILEPATH_LEN] = {0};

/* Save the captured image's path */
char imageFilePath[FILEPATH_LEN] = {0};

/* For avilib */
static avi_t *avifile = NULL;

/* Save the count of frames in the recorded video file */
static int framecount = 0;

/* Record the sequence of frame */
static int _sequence = 0;

static FrameBufferQueue _frameBufferQueue;
static pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;


static int xioctl(int fh, int request, void *arg);
static int initMmap(void);
static int readFrame(void);
static int stopCaptureImage(void);
static int computeVideoDuration(struct timeval * start,struct timeval * stop);
static int save2Avi(const unsigned char *pImageData, int len);

static int createMutex(void);
static int destroyMutex(void);
static int lockMutex(void);
static int unlockMutex(void);

static int initFrameBuffer(void);
static void uninitFrameBuffer(void);
static int qFrameBuffer(const char *imageData, int len);
static FrameBuffer *dqFrameBuffer(void);


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


static void buildFileName(int isVideo) {
    // Get the current time
    char filename[32] = {0};
    time_t t_time;
    struct tm *pTime;

    ENTER_FUNC_LOG();
    
    time(&t_time);
    pTime = gmtime(&t_time);
    sprintf(filename, "%d%02d%02d%02d%02d_%02d",\
            (1900+pTime->tm_year), (1+pTime->tm_mon), pTime->tm_mday, \
            pTime->tm_hour, pTime->tm_min, pTime->tm_sec);
    
    if (isVideo == TRUE) {
        memset(videoFilePath, 0, sizeof(char) * FILEPATH_LEN);
        sprintf(videoFilePath, "%s%s.%s", VIDEO_FILE_PATH, filename, "avi");
        LOGE("video file path: %s", videoFilePath);
    } else {
        memset(imageFilePath, 0, sizeof(char) * FILEPATH_LEN);
        sprintf(imageFilePath, "%s%s.%s", IMAGE_FILE_PATH, filename, "jpeg");
        LOGE("image file path: %s", imageFilePath);
    }
    
    EXIT_FUNC_LOG();
}

static int isCameraOpened() {
    if (-1 != fd && TRUE == _cameraStart) {
        return TRUE;
    } else {
        LOGE("Camera doesn't run");
    }

    return FALSE;
}

int checkCamerabase(void){
	struct stat st;
	int i;
	int start_from_4 = 1;
	
	/* if /dev/video[0-3] exist, camerabase=4, otherwise, camrerabase = 0 */
	for(i=0 ; i<4 ; i++){
		sprintf(dev_name,"/dev/video%d",i);
		if (-1 == stat (dev_name, &st)) {
			start_from_4 &= 0;
		}else{
			start_from_4 &= 1;
		}
	}

	if(start_from_4){
		return 4;
	}else{
		return 0;
	}
}

int openDevice(int i)
{
	struct stat st;

    ENTER_FUNC_LOG();

	sprintf(dev_name,"/dev/video%d",i);

    /* for debugging to check if the current application is system app or not */
	uid_t uid = getuid();
	uid_t euid = geteuid();
	LOGE("uid: %d, euid: %d", uid, euid);

    /* Get this device's information */ 
	if (-1 == stat (dev_name, &st)) {
		LOGE("Cannot identify '%s': %d, %s", dev_name, errno, strerror (errno));
		return ERROR_LOCAL;
	}

	if (!S_ISCHR (st.st_mode)) {
		LOGE("%s is no device", dev_name);
		return ERROR_LOCAL;
	}

	fd = open (dev_name, O_RDWR | O_NONBLOCK, 0);
	if (-1 == fd) {
		LOGE("Cannot open '%s': %d, %s", dev_name, errno, strerror (errno));
		return ERROR_LOCAL;
	}
    
	EXIT_FUNC_LOG();
    
	return SUCCESS_LOCAL;
}

int initDevice(void) 
{
	struct v4l2_capability cap;
	struct v4l2_cropcap cropcap;
	struct v4l2_crop crop;
	struct v4l2_format fmt;
	struct v4l2_streamparm params;
    struct v4l2_fmtdesc fmtdesc;
	unsigned int min;
    int retry = 0;
    int maxRetry = 3;
    int ret = 0;
    int formatIndex = 0;

    LOGE("Starting %s", __FUNCTION__);

    /* Fetch the capability of the device */
    if (SUCCESS_LOCAL != v4l2_ioctl_querycap(fd, &cap)) {
        return errnoexit("VIDIOC_QUERYCAP");
    }

    //if (SUCCESS_LOCAL != v4l2_ioctl_fmtdesc(fd, &fmtdesc)) {
    //    return errnoexit("VIDIOC_ENUM_FMT");
    //}

    if (SUCCESS_LOCAL == v4l2_ioctl_cropcap(fd, &cropcap)) {
        // Try to set the crop, don't care the return value
        v4l2_ioctl_set_crop(fd, &crop, &cropcap);
    } else {
        errnoexit("VIDIOC_S_FMT");
    }
    
    //if (SUCCESS_LOCAL != v4l2_ioctl_get_fmt(fd, &fmt)){
    //    errnoexit("VIDIOC_G_FMT");
    //}

    if (SUCCESS_LOCAL != v4l2_ioctl_set_fmt(fd, &fmt)) {
        return errnoexit("VIDIOC_S_FMT");
    }

    /* Get the streaming parameters */
    //if (SUCCESS_LOCAL != v4l2_ioctl_get_streamparam(fd, &params)) {
    //    errnoexit("VIDIOC_G_PARM");
    //}

    /* set frame rate 30fps. */
    if (SUCCESS_LOCAL != v4l2_ioctl_set_streamparam(fd, &params)) {
        errnoexit("VIDIOC_S_PARM");
    }
    
	min = fmt.fmt.pix.width * 2;
	if (fmt.fmt.pix.bytesperline < min)
		fmt.fmt.pix.bytesperline = min;
	min = fmt.fmt.pix.bytesperline * fmt.fmt.pix.height;
	if (fmt.fmt.pix.sizeimage < min)
		fmt.fmt.pix.sizeimage = min;

    /* Store the image size that driver wants */
    imageBufSize = fmt.fmt.pix.sizeimage;

	if (SUCCESS_LOCAL !=initMmap ()) {
        return ERROR_LOCAL;
    }

    if (SUCCESS_LOCAL != createMutex()) {
        return ERROR_LOCAL;
    }

    return SUCCESS_LOCAL;
}

static int initMmap(void)
{
	struct v4l2_requestbuffers req;
    struct v4l2_buffer buf;

    ENTER_FUNC_LOG();

    if (SUCCESS_LOCAL != v4l2_ioctl_reqbuf(fd, &req)) {
        return errnoexit("VIDIOC_REQBUFS");
    }

	if (req.count < 2) {
		LOGE("Insufficient buffer memory on %s", dev_name);
		return ERROR_LOCAL;
 	}

	buffers = (struct buffer*)calloc (req.count, sizeof (*buffers));
	if (!buffers) {
		LOGE("Out of memory");
		return ERROR_LOCAL;
	}

	for (n_buffers = 0; n_buffers < req.count; ++n_buffers) {
        
        if ( SUCCESS_LOCAL != v4l2_ioctl_querybuf(fd, &buf, n_buffers)) {
            return errnoexit("VIDIOC_QUERYBUF");
        }

		buffers[n_buffers].length = buf.length;
		buffers[n_buffers].start = mmap (NULL , 
                                         buf.length, 
                                         PROT_READ | PROT_WRITE,
                            			 MAP_SHARED,
                            			 fd, 
                            			 buf.m.offset);

		if (MAP_FAILED == buffers[n_buffers].start) {
			return errnoexit ("mmap");
        }

        LOGE("buffers[%d].start = 0x%x", n_buffers, buffers[n_buffers].start);
        
        memset(buffers[n_buffers].start, 0xab, buf.length);
	}

	EXIT_FUNC_LOG();
	
	return SUCCESS_LOCAL;
}

int startCapturing(void)
{
	unsigned int i;
	struct v4l2_buffer buf;

    ENTER_FUNC_LOG();
    
	for (i = 0; i < n_buffers; ++i) {
        //if (SUCCESS_LOCAL != v4l2_ioctl_qbuf(fd, &buf, i)) {
        //    return errnoexit("VIDIOC_QBUF");
        //}
        struct v4l2_buffer buf;

        CLEAR(buf);
        buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        buf.memory = V4L2_MEMORY_MMAP;
        buf.index = i;

        if (-1 == xioctl(fd, VIDIOC_QBUF, &buf))
            errnoexit("VIDIOC_QBUF");
	}

    /* Ask this device to start capture */
	if (SUCCESS_LOCAL != v4l2_ioctl_streamon(fd)) {
		return errnoexit ("VIDIOC_STREAMON");
    } else {
        // Set flag to indicate this camera is opened normally
        _cameraStart = 1;
    }
    
    _sequence = 0;

	EXIT_FUNC_LOG();
    
	return SUCCESS_LOCAL;
}

int readframeonce(void)
{
    int ret = 0;

    ENTER_FUNC_LOG();
	for (;;) {
		fd_set fds;
		struct timeval tv;
		int r;

		FD_ZERO (&fds);
		FD_SET (fd, &fds);

		tv.tv_sec = 2000;
		tv.tv_usec = 0;

		r = select (fd + 1, &fds, NULL, NULL, &tv);

		if (-1 == r) {
			if (EINTR == errno) {
                errnoexit("select");
                continue;
            }

			return errnoexit ("select");
		}

		if (0 == r) {
			LOGE("select timeout");
			return ERROR_LOCAL;

		}

        ret = readFrame();
		if (ret == SUCCESS_LOCAL) {
			break;
        } else if ( ret == 19 ) {
            LOGE("Error return");
            return ERROR_LOCAL;
        } else {
            continue;
        }

	}

	EXIT_FUNC_LOG();

	return SUCCESS_LOCAL;

}

const static unsigned char dht_data[] = {
    0xff, 0xc4, 0x01, 0xa2, 0x00, 0x00, 0x01, 0x05, 0x01, 0x01, 0x01, 0x01,
    0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x02,
    0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x01, 0x00, 0x03,
    0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00,
    0x00, 0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
    0x0a, 0x0b, 0x10, 0x00, 0x02, 0x01, 0x03, 0x03, 0x02, 0x04, 0x03, 0x05,
    0x05, 0x04, 0x04, 0x00, 0x00, 0x01, 0x7d, 0x01, 0x02, 0x03, 0x00, 0x04,
    0x11, 0x05, 0x12, 0x21, 0x31, 0x41, 0x06, 0x13, 0x51, 0x61, 0x07, 0x22,
    0x71, 0x14, 0x32, 0x81, 0x91, 0xa1, 0x08, 0x23, 0x42, 0xb1, 0xc1, 0x15,
    0x52, 0xd1, 0xf0, 0x24, 0x33, 0x62, 0x72, 0x82, 0x09, 0x0a, 0x16, 0x17,
    0x18, 0x19, 0x1a, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x34, 0x35, 0x36,
    0x37, 0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a,
    0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66,
    0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a,
    0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93, 0x94, 0x95,
    0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8,
    0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2,
    0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5,
    0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe1, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7,
    0xe8, 0xe9, 0xea, 0xf1, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8, 0xf9,
    0xfa, 0x11, 0x00, 0x02, 0x01, 0x02, 0x04, 0x04, 0x03, 0x04, 0x07, 0x05,
    0x04, 0x04, 0x00, 0x01, 0x02, 0x77, 0x00, 0x01, 0x02, 0x03, 0x11, 0x04,
    0x05, 0x21, 0x31, 0x06, 0x12, 0x41, 0x51, 0x07, 0x61, 0x71, 0x13, 0x22,
    0x32, 0x81, 0x08, 0x14, 0x42, 0x91, 0xa1, 0xb1, 0xc1, 0x09, 0x23, 0x33,
    0x52, 0xf0, 0x15, 0x62, 0x72, 0xd1, 0x0a, 0x16, 0x24, 0x34, 0xe1, 0x25,
    0xf1, 0x17, 0x18, 0x19, 0x1a, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x35, 0x36,
    0x37, 0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a,
    0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66,
    0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a,
    0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93, 0x94,
    0x95, 0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7,
    0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba,
    0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4,
    0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7,
    0xe8, 0xe9, 0xea, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8, 0xf9, 0xfa
};

static int insert_huffman(const void *in_buf, int buf_size, void *out_buf) 
{
	int pos = 0;
	int size_start = 0;   
	char *pcur = (char *)in_buf;    
	char *pdeb = (char *)in_buf;   
	char *plimit = (char *)in_buf + buf_size;    
	char *jpeg_buf = (char *)out_buf;    
	/* find the SOF0(Start Of Frame 0) of JPEG */    
	while ( (((pcur[0] << 8) | pcur[1]) != 0xffc0) && (pcur < plimit) ){ 
		pcur++;
	}

    LOGE("pcur: %d, plimit: %d", pcur, plimit);
	
	/* SOF0 of JPEG exist */   
	if (pcur < plimit){        
		if (jpeg_buf != NULL)        
		{            
			//fprintf(stderr, ">");  
			/* insert huffman table after SOF0 */            
			size_start = pcur - pdeb;            
			memcpy(jpeg_buf, in_buf, size_start);           
			pos += size_start;       
			memcpy(jpeg_buf + pos, dht_data, sizeof(dht_data));           
			pos += sizeof(dht_data);           
			memcpy(jpeg_buf + pos, pcur, buf_size - size_start);           
			pos += buf_size - size_start;           
			return pos;       
		}  
	} else{       
		//fprintf(stderr, "x");   
	}   
	return 0;
}

static int save2ImageFile(const void *pImageData, int imageSize, const char *pFilename) {
    FILE *f = NULL;
    char fileName[64] = {0};
    int ret = 0;
    struct stat cameraStat;
    int imageDirExist = 0;
    
    ENTER_FUNC_LOG();

    /* Check if the arguments are valid */
    if (NULL == pImageData || NULL == pFilename) {
        LOGE("The arguments are invalid in %s", __FUNCTION__);
        return INVARG_LOCAL;
    }

    if (access(IMAGE_FILE_PATH, F_OK) == 0) {
        if (stat(IMAGE_FILE_PATH, &cameraStat) == 0) {
            if (S_ISDIR(cameraStat.st_mode)) {
                imageDirExist = 1;
            }
        }
    }
    
    if (imageDirExist == 0) {
        if (0 ==mkdir(IMAGE_FILE_PATH, S_IRUSR|S_IWUSR|S_IRGRP|S_IWGRP|S_IROTH|S_IWOTH)) {
            LOGE("Success to create diretory: %s", IMAGE_FILE_PATH);
        } else {
            LOGE("Failed to create %s, errno: %d, %s", IMAGE_FILE_PATH, errno, strerror(errno));
            return ERROR_LOCAL;
        }
    }

    /* Open the file */
    f = fopen(pFilename, "w+b");
    if (f == NULL) {
        LOGE("Failed to open file: %s, errno: %d, %s", pFilename, errno, strerror(errno));
        return ERROR_LOCAL;
    }

    /* Write image data to file */
    ret = fwrite(pImageData, imageSize, 1, f);
    if (ret < 1) {
        LOGE("Failed to write data to file: %s, errno: %d, %s", pFilename, errno, strerror(errno));
    }

    /* Close this file */
    ret = fclose(f);
    if (0 != ret) {
        LOGE("Failed to close file: %s, errno: %d, %s", pFilename, errno, strerror(errno));
    }
    
    EXIT_FUNC_LOG();

    return SUCCESS_LOCAL;
}

int recording(void) {
    ENTER_FUNC_LOG();

    // Retrive the image data from _frameBufferQueue
    FrameBuffer *frameBuffer = dqFrameBuffer();
    if (NULL == frameBuffer) {
        LOGE("Cannot retrieve frame buffer from queue");
        return ERROR_LOCAL;
    }

    /* Save the image data to Avi */
    save2Avi(frameBuffer->buffer, frameBuffer->usedSize);

    EXIT_FUNC_LOG();

    return SUCCESS_LOCAL;
}

static int save2Avi(const unsigned char *pImageData, int len) {
    struct timeval startTime;
    struct timeval endTime;
    
    ENTER_FUNC_LOG();

    gettimeofday(&startTime, 0);
    LOGE("Saving frame start time: %d", startTime.tv_sec);
    
    /* if vd->avifile is NULL, then we need to initialize it */
    if (avifile == NULL) {
        LOGE("Error, video file shouldn't be NULL");
    } else {
        /* if we have a valid avifile, record the frame to it */
        AVI_write_frame(avifile, pImageData, len, framecount);
        framecount++;
    }

    gettimeofday(&endTime, 0);
    LOGE("Saving frame end time: %d, duration: %d", endTime.tv_sec, endTime.tv_sec-startTime.tv_sec);

    EXIT_FUNC_LOG();
    
    return SUCCESS_LOCAL;
}

static void printImageData(const char *pImageData, int imageSize) {
    int i = 0;
    LOGE("pImageData: 0x%x", pImageData);

    LOGE("The first 20 bytes:");
    for (i = 0; i < 20; i++) {
        LOGE("%x", pImageData[i]);
    }
    
    LOGE("The last 20 bytes: ");
    for (i = imageSize-20; i < imageSize; i++) {
        LOGE("%x", pImageData[i]);
    }
}

/*
    Description:
        This function performs to convert the MJPEG data from uvc into the complete MJPEG file
*/
static int convert2MJPEG(const void *p, int size)
{
	char *mjpgBuf = NULL;
	unsigned int image_size = 0;
    
    ENTER_FUNC_LOG();

    LOGE("pImageBuf=x%x, imageBufSize=%d", pImageBuf, imageBufSize);

    if (pImageBuf == NULL) {
        return errnoexit("pImageBuf isn't initialized in JNI");
    }

    /* Clear pImageBuf and realImageSize */
	memset(pImageBuf, 0, imageBufSize);
    realImageSize = 0;

    /* insert dht data to p, and then save them to pImageBuf */
	realImageSize = insert_huffman(p, size, pImageBuf);
	
	if(realImageSize != 0 && realImageSize >= imageBufSize){
		LOGE("Allocate mjpg memory too little.\n");	
        realImageSize = 0;
        return ERROR_LOCAL;
	}

    printImageData(pImageBuf, realImageSize);
    
    EXIT_FUNC_LOG();

    return SUCCESS_LOCAL;
}

static int processFrame (const void *pFrameData, int size)
{
    int ret = 0;

    /* Try to save the image to avi file */
    if (_startRecording) {
        qFrameBuffer((char *)pFrameData, size);
        //save2Avi(pFrameData, size);
    }

    /* Convert the image data to the complete MJPEG file */
    ret = convert2MJPEG(pFrameData ,size);
    if (SUCCESS_LOCAL != ret) {
        if (_startCaptureImage) {
            stopCaptureImage();
        }
        LOGE("Failed to process MJpeg");

        return ret;
    }

    /* Try to save the MJPEG file */
    if (_startCaptureImage) {
        ret = save2ImageFile(pImageBuf, realImageSize, imageFilePath);
        stopCaptureImage();
    }

    return ret;
}

static int readFrame(void)
{
	struct v4l2_buffer buf;
	unsigned int i;
    int ret = 0;

    ENTER_FUNC_LOG();
    
    ret = v4l2_ioctl_dqbuf(fd, &buf);
    if (SUCCESS_LOCAL != ret) {
        if (EAGAIN == ret) {
            LOGE("No buffer was in the outgoing queue");
            return SUCCESS_LOCAL;
        }
        return errnoexit("VIDIOC_DQBUF");
    }

	assert (buf.index < n_buffers);

    /* Check if some frames are lost */
    if ((_sequence + 1) != buf.sequence) {
        LOGE("Some Frames are lost, the last frame is %d, the current frame is %d", _sequence, buf.sequence);
    }
    /* Record the last the sequence of frame */
    _sequence = buf.sequence;
    
	processFrame (buffers[buf.index].start, buf.bytesused);

	if (SUCCESS_LOCAL != v4l2_ioctl_qbuf(fd, &buf, buf.index))
		return errnoexit ("VIDIOC_QBUF");
    
    EXIT_FUNC_LOG();
    
	return SUCCESS_LOCAL;
}

int stopCapturing(void)
{
	ENTER_FUNC_LOG();
    
    _sequence = 0;
    
	if (SUCCESS_LOCAL != v4l2_ioctl_streamoff(fd))
		return errnoexit ("VIDIOC_STREAMOFF");

    EXIT_FUNC_LOG();

	return SUCCESS_LOCAL;
}

static int createMutex(void) {
    if (0 != pthread_mutex_init(&mutex, NULL)) {
        return errnoexit("Init mutex");
    }

    return SUCCESS_LOCAL;
}

static int destroyMutex(void) {
    if (0 !=pthread_mutex_destroy(&mutex)) {
        return errnoexit("Destory mutex");
    }

    return SUCCESS_LOCAL;
}

static int lockMutex(void) {
    if (0 !=pthread_mutex_lock(&mutex)) {
        return errnoexit("Lock mutex");
    }

    return SUCCESS_LOCAL;
}

static int unlockMutex() {
    if (0 !=pthread_mutex_unlock(&mutex)) {
        return errnoexit("Unlock mutex");
    }

    return SUCCESS_LOCAL;
}
static int initFrameBuffer(void) {
    int index = 0;
    ENTER_FUNC_LOG();

    memset(&_frameBufferQueue, 0, sizeof(FrameBufferQueue));

    if (imageBufSize != 0) {
        for (index = 0; index < FRAMEBUFFER_MAX_COUNT; index++) {
            _frameBufferQueue.frameBuffer[index].buffer = (char *)calloc(1, imageBufSize);
            if (NULL != _frameBufferQueue.frameBuffer[index].buffer) {
                _frameBufferQueue.frameBuffer[index].size = imageBufSize;
                _frameBufferQueue.count++;
            } else {
                LOGE("Failed to allocate %d frame buffer", index);
                uninitFrameBuffer();
                return ERROR_LOCAL;
            }
        }
    } else {
        LOGE("imageBufSize isn't initialized");
        return ERROR_LOCAL;
    }

    LOGE("_frameBufferQueue info: count(%d), framebuffer size(%d)", _frameBufferQueue.count, imageBufSize);

    EXIT_FUNC_LOG();

    return SUCCESS_LOCAL;
}

static void uninitFrameBuffer(void) {
    int index = 0;
    ENTER_FUNC_LOG();

    for (index = 0; index < _frameBufferQueue.count; index++){
        free(_frameBufferQueue.frameBuffer[index].buffer);
    }
    memset(&_frameBufferQueue, 0, sizeof(FrameBufferQueue));

    EXIT_FUNC_LOG();
}

static int qFrameBuffer(const char *imageData, int len) {
    ENTER_FUNC_LOG();
    
    // Check if the argument is valid
    if (NULL == imageData) {
        LOGE("Invalid argument(imageData) in %s", __FUNCTION__);
        return INVARG_LOCAL;
    }
    
    lockMutex();
    if (0 == _frameBufferQueue.count) {
        LOGE("_frameBufferQueue isn't initialized");
        unlockMutex();
        return ERROR_LOCAL;
    } else {
        if (_frameBufferQueue.usedCount == _frameBufferQueue.count) {
            LOGE("_frameBufferQueue is full, this frame will be missed");
            unlockMutex();
            return ERROR_LOCAL;
        } else if (_frameBufferQueue.writeIndex >= _frameBufferQueue.count) {
            LOGE("_frameBufferQueue.writeIndex(%d) is out of range, count(%d)", _frameBufferQueue.writeIndex, _frameBufferQueue.count);
            unlockMutex();
            return ERROR_LOCAL;
        } else {
            memset(_frameBufferQueue.frameBuffer[_frameBufferQueue.writeIndex].buffer, 0, _frameBufferQueue.frameBuffer[_frameBufferQueue.writeIndex].size);
            _frameBufferQueue.frameBuffer[_frameBufferQueue.writeIndex].usedSize = 0;
            if (_frameBufferQueue.frameBuffer[_frameBufferQueue.writeIndex].size >= len) {
                memcpy(_frameBufferQueue.frameBuffer[_frameBufferQueue.writeIndex].buffer, imageData, len);
                _frameBufferQueue.frameBuffer[_frameBufferQueue.writeIndex].usedSize = len;
                _frameBufferQueue.writeIndex = (_frameBufferQueue.writeIndex + 1)  % _frameBufferQueue.count;
                _frameBufferQueue.usedCount++;
            } else {
                LOGE("image data size(%d) exceeds buffer size(%d)", len, _frameBufferQueue.frameBuffer[_frameBufferQueue.writeIndex].size);
            }
        }
    }
    unlockMutex();
    
    EXIT_FUNC_LOG();
    
    return SUCCESS_LOCAL;
}

static FrameBuffer *dqFrameBuffer(void) {
    int readIndex = 0;
    
    ENTER_FUNC_LOG();
    
    lockMutex();
    
    if (0 == _frameBufferQueue.count) {
        LOGE("_frameBufferQueue isn't initialized");
        unlockMutex();
        return NULL;
    } else {
        if (_frameBufferQueue.usedCount == 0) {
            LOGE("_frameBufferQueue is empty");
            unlockMutex();
            return NULL;
        } else if (_frameBufferQueue.readIndex >= _frameBufferQueue.count) {
            LOGE("_frameBufferQueue.readIndex(%d) is out of range, count(%d)", _frameBufferQueue.readIndex, _frameBufferQueue.count);
            unlockMutex();
            return NULL;
        } else if (_frameBufferQueue.frameBuffer[_frameBufferQueue.readIndex].usedSize == 0) {
            LOGE("_frameBufferQueue.frameBuffer[%d] is empty", _frameBufferQueue.readIndex);
            unlockMutex();
            return NULL;
        } else {
            readIndex = _frameBufferQueue.readIndex;
            _frameBufferQueue.readIndex = (_frameBufferQueue.readIndex + 1) % _frameBufferQueue.count;
            _frameBufferQueue.usedCount--;
        }
    }

    unlockMutex();

    EXIT_FUNC_LOG();

    return &_frameBufferQueue.frameBuffer[readIndex];
}

int startRecording(void) {
    ENTER_FUNC_LOG();

    /* Check if the camera is running */
    if (TRUE != isCameraOpened()) {
        LOGE("Camera doesn't run");

        return DEVICE_NOT_OPEN;
    }

    /* Re-build the file name */
    buildFileName(TRUE);

    /* Prepare the buffer to store the frame */
    initFrameBuffer();

    /* Open video file */
    avifile = AVI_open_output_file(videoFilePath);

    /* if avifile is NULL, there was an error */
    if (avifile == NULL ) {
        LOGE("Error opening avifile test.avi\n");
    }
    else {
        /* we default the fps to 15, we'll reset it on close */
        AVI_set_video(avifile, 640, 480, 15, "MJPG");
        LOGE("recording to test.avi\n");
    }

    /* Save the recording start time */
    memset(&_recordingStart, 0, sizeof(_recordingStart));
    gettimeofday(&_recordingStart, 0);

    /* Set the flag to ask recording */
    _startRecording = 1;
    
    EXIT_FUNC_LOG();
    return SUCCESS_LOCAL;
}

static int computeVideoDuration(struct timeval *start, struct timeval *stop) {
    /* Check if the arguments are valid or not */
    if (NULL == start || NULL == stop) {
        LOGE("The arguments are invalide in %s", __FUNCTION__);

        return INVARG_LOCAL;
    }

    ENTER_FUNC_LOG();

    if (stop->tv_sec >= start->tv_sec) {
        return (stop->tv_sec - start->tv_sec);
    } else {
        return ERROR_LOCAL;
    }

    EXIT_FUNC_LOG();
}

int stopRecording(void) {
    ENTER_FUNC_LOG();
    int ret = 0;
    int videoDuration = 0;
    
    _startRecording = 0;

    uninitFrameBuffer();

    if (avifile != NULL) {
        memset(&_recordingStop, 0, sizeof(_recordingStop));
        gettimeofday(&_recordingStop, 0);
        
        videoDuration = computeVideoDuration(&_recordingStart, &_recordingStop);
        if (videoDuration > 0) {
            float fps=(framecount/videoDuration);
            
            AVI_set_video(avifile, IMG_WIDTH, IMG_HEIGHT, fps, "MJPG");
        }
        
        AVI_close(avifile);
        
        LOGE("Recording: start time(%d), stop time(%d), framecount(%d), videoDuration(%d)", \
            _recordingStart.tv_sec, _recordingStop.tv_sec, framecount, videoDuration);
        
        if (0 != ret) {
            LOGE("Failed to close video file");
            return ret;
        }
    }
    
    EXIT_FUNC_LOG();
    
    return SUCCESS_LOCAL;
}

int captureImage(void) {
    ENTER_FUNC_LOG();

    /* Check if the camera is running */
    if (TRUE != isCameraOpened()) {
        LOGE("Camera doesn't run");

        return DEVICE_NOT_OPEN;
    }

    /* Re-build the file name */
    buildFileName(FALSE);
    
    _startCaptureImage = TRUE;

    EXIT_FUNC_LOG();
    return SUCCESS_LOCAL;
}

static int stopCaptureImage(void) {
    ENTER_FUNC_LOG();
    
    _startCaptureImage = FALSE;

    EXIT_FUNC_LOG();
    return SUCCESS_LOCAL;
}

int uninitDevice(void)
{
	unsigned int i;
	
	ENTER_FUNC_LOG();

	for (i = 0; i < n_buffers; ++i)
		if (-1 == munmap (buffers[i].start, buffers[i].length))
			return errnoexit ("munmap");

	free (buffers);
    buffers = NULL;

    destroyMutex();
	
	EXIT_FUNC_LOG();

	return SUCCESS_LOCAL;
}

int closeDevice(void)
{
	ENTER_FUNC_LOG();

    if (-1 != fd) {
        if (0 != close (fd)){
    		fd = -1;
    		return errnoexit ("close device failed");
    	} else {
            LOGE("Close device success");
        }

        fd = -1;
    }
    
    EXIT_FUNC_LOG();
    
	return SUCCESS_LOCAL;
}

int setDirectBuffer(char *pDirectBuffer) {
    ENTER_FUNC_LOG();
    if (NULL != pDirectBuffer) {
        pImageBuf = pDirectBuffer;
        LOGE("pImageBuf: 0x%x, pDirectBuffer: 0x%x", pImageBuf, pDirectBuffer);
    } else {
        return INVARG_LOCAL;
    }

    EXIT_FUNC_LOG();
    return SUCCESS_LOCAL;
}

int getFrameBufferSize() {
    ENTER_FUNC_LOG();

    EXIT_FUNC_LOG();

    return imageBufSize;
}

int getRealImageSize() {
    ENTER_FUNC_LOG();

    EXIT_FUNC_LOG();

    return realImageSize;
}

