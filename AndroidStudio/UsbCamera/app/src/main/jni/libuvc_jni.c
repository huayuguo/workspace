#include "jni.h"
#include "com_camera_usbcamera_UVCJni.h"

#include <unistd.h>
//#include <utils/Log.h>
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

#include <asm/types.h>          /* for videodev2.h */

#include <linux/videodev2.h>
#include <linux/usbdevice_fs.h>

#include "ioctlUtil.h"
#include "ioctlLog.h"
#include "ImageProc.h"


JNIEXPORT jint JNICALL Java_com_camera_usbcamera_UVCJni_prepareCamera( JNIEnv* env,jclass thiz, jint videoid){

	int ret;
    int camerabase = -1;
	
	LOGE("Starting %s()\n", __FUNCTION__);

	if(camerabase<0){
		camerabase = checkCamerabase();
	}

	ret = openDevice(camerabase + videoid);
    if (SUCCESS_LOCAL != ret) {
        LOGE("Open device failed");
        return ret;
    }

	ret = initDevice();
    if (SUCCESS_LOCAL != ret) {
        LOGE("init device failed");
        return ret;
    }
    
	ret = startCapturing();
	if(SUCCESS_LOCAL != ret) {
        LOGE("startCapturing() error, then stopCapturing --> uninitDevice --> closeDevice");

        stopCapturing();
		uninitDevice ();
		closeDevice ();
        
        return ret;
	}
    
	return ret;
}

JNIEXPORT jint JNICALL Java_com_camera_usbcamera_UVCJni_getBufferSize(JNIEnv *env, jclass thiz) {
    ENTER_FUNC_LOG();

    EXIT_FUNC_LOG();

    return getFrameBufferSize();
}

JNIEXPORT jint JNICALL Java_com_camera_usbcamera_UVCJni_setDirectBuffer(JNIEnv *env, jclass thiz,
    jobject jbuf, jint jlen) {

    char *pImageBuf = NULL;

    ENTER_FUNC_LOG();

    pImageBuf = (char *)(*env)->GetDirectBufferAddress(env, jbuf);
    if (NULL == pImageBuf) {
        LOGE("Failed to get direct buffer");
        return ERROR_LOCAL;
    }
    
    setDirectBuffer(pImageBuf);
    
    // frame buffer size is ignored
    //imageBufSize = jlen;

    EXIT_FUNC_LOG();

    return SUCCESS_LOCAL;
}

JNIEXPORT jint JNICALL Java_com_camera_usbcamera_UVCJni_readFrame( JNIEnv* env,
										jclass thiz){
	int ret = 0;
    
	ENTER_FUNC_LOG();

	ret = readframeonce();
    
    EXIT_FUNC_LOG();
    
    if (SUCCESS_LOCAL == ret) {
        return getRealImageSize();
    } else {
        return 0;
    }
}

JNIEXPORT void JNICALL Java_com_camera_usbcamera_UVCJni_stopCamera(JNIEnv* env,jclass thiz) {

    ENTER_FUNC_LOG();
    
	stopCapturing ();

	uninitDevice ();

	closeDevice ();

    EXIT_FUNC_LOG();
}

JNIEXPORT jint JNICALL Java_com_camera_usbcamera_UVCJni_startRecording(JNIEnv *env,jclass thiz) {
    int ret = 0;
    
    ENTER_FUNC_LOG();
    
	ret = startRecording();

    EXIT_FUNC_LOG();
    
    return ret;
}

JNIEXPORT jint JNICALL Java_com_camera_usbcamera_UVCJni_stopRecording(JNIEnv *env, jclass thiz) {
    int ret = 0;
    
    ENTER_FUNC_LOG();
    
	ret = stopRecording();

    EXIT_FUNC_LOG();
    
    return ret;
}

jint Java_com_camera_usbcamera_CameraPreview_recording(JNIEnv *env, jobject thiz) {
    int ret = 0;
    
    ENTER_FUNC_LOG();
    
	ret = recording();

    EXIT_FUNC_LOG();
    
    return ret;
}

JNIEXPORT jint JNICALL Java_com_camera_usbcamera_UVCJni_captureImage(JNIEnv *env, jclass thiz) {
    int ret = 0;
    
    ENTER_FUNC_LOG();
    
	ret = captureImage();

    EXIT_FUNC_LOG();
    
    return ret;
}
