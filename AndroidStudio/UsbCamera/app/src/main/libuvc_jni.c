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

#include <asm/types.h>          /* for videodev2.h */

#include <linux/videodev2.h>
#include <linux/usbdevice_fs.h>

#include "ioctlUtil.h"
#include "ioctlLog.h"
#include "ImageProc.h"


jint 
Java_com_camera_simplewebcam_CameraPreview_prepareCamera( JNIEnv* env,jobject thiz, jint videoid){

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

jint Java_com_camera_simplewebcam_CameraPreview_getBufferSize(JNIEnv *env, jobject thiz) {
    ENTER_FUNC_LOG();

    EXIT_FUNC_LOG();

    return getFrameBufferSize();
}

jint Java_com_camera_simplewebcam_CameraPreview_setDirectBuffer(JNIEnv *env, jobject thiz, 
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


int 
Java_com_camera_simplewebcam_CameraPreview_readFrame( JNIEnv* env,
										jobject thiz){
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

void 
Java_com_camera_simplewebcam_CameraPreview_stopCamera(JNIEnv* env,jobject thiz) {

    ENTER_FUNC_LOG();
    
	stopCapturing ();

	uninitDevice ();

	closeDevice ();

    EXIT_FUNC_LOG();
}

jint Java_com_camera_simplewebcam_CameraPreview_startRecording(JNIEnv *env, jobject thiz) {
    int ret = 0;
    
    ENTER_FUNC_LOG();
    
	ret = startRecording();

    EXIT_FUNC_LOG();
    
    return ret;
}

jint Java_com_camera_simplewebcam_CameraPreview_stopRecording(JNIEnv *env, jobject thiz) {
    int ret = 0;
    
    ENTER_FUNC_LOG();
    
	ret = stopRecording();

    EXIT_FUNC_LOG();
    
    return ret;
}

jint Java_com_camera_simplewebcam_CameraPreview_recording(JNIEnv *env, jobject thiz) {
    int ret = 0;
    
    ENTER_FUNC_LOG();
    
	ret = recording();

    EXIT_FUNC_LOG();
    
    return ret;
}

jint Java_com_camera_simplewebcam_CameraPreview_captureImage(JNIEnv *env, jobject thiz) {
    int ret = 0;
    
    ENTER_FUNC_LOG();
    
	ret = captureImage();

    EXIT_FUNC_LOG();
    
    return ret;
}

/*
static char *pFileBuf = NULL;

static int readMJpeg(const char *filename, char **ppFileBuf, int *pLen) {
    FILE *pFile = NULL;
    int ret = 0;
    struct stat fileStat;

    ENTER_FUNC_LOG();

    // Check arguments are valid or not
    if (NULL == ppFileBuf || NULL == pLen || NULL == filename) {
        return errnoexit("Arguments are invalid");
    }

    // Get this file's information
    memset(&fileStat, 0, sizeof(struct stat));
    ret = stat(filename, &fileStat);
    if (0 != ret) {
        LOGE("stat() failed, errno: %d, %s", errno, strerror(errno));
        return ERROR_LOCAL;
    }

    // Allocate the buffer to store file content
    *ppFileBuf = (char *)malloc(fileStat.st_size);
    memset(*ppFileBuf, 0, fileStat.st_size);

    // Open file
    pFile = fopen(filename, "r");
    if (NULL == pFile) {
        free(*ppFileBuf);
        *ppFileBuf = NULL;
        LOGE("Open %s failed, errno: %d, %s", filename, errno, strerror(errno));
        return ERROR_LOCAL;
    }

    // Read file to *ppFileBuf
    ret = fread(*ppFileBuf, 1, fileStat.st_size, pFile);
    if (ret < fileStat.st_size) {
        LOGE("Read file error, filesize: %l, readsize: %l, errno: %d, %s", fileStat.st_size, ret, errno, strerror(errno));
        
        free(*ppFileBuf);
        *ppFileBuf = NULL;

        // Close file
        ret = fclose(pFile);
        return ERROR_LOCAL;
    }
    
    *pLen = ret;

    fclose(pFile);

    EXIT_FUNC_LOG();

    return SUCCESS_LOCAL;
}

jbyteArray Java_com_camera_simplewebcam_CameraPreview_getImageData(JNIEnv *env, jobject thiz) {
    char *pImageData = NULL;
    int imageSize = 0;
    int ret = 0;

    ENTER_FUNC_LOG();

    ret = readMJpeg("/sdcard/uvc/test_0.jpg", &pImageData, &imageSize);
    if (0 != ret) {
        errnoexit("read /sdcard/uvc/test_0.jpg failed");
        return NULL;
    }

    jbyteArray byteArray = (*env)->NewByteArray(env, imageSize);

    (*env)->SetByteArrayRegion(env, byteArray, 0, imageSize, pImageData);

    free(pImageData);
    pImageData = NULL;

    EXIT_FUNC_LOG();

    return byteArray;
}
*/
/*
static jobjectArray Java_com_camera_simplewebcam_UVCJni_getPixelFormat(JNIEnv *env, jobject thiz) {
    // Check if the length of these two arrays are same    
    const int logoTypeCount = env->GetArrayLength(jLogoType);
    const int bmpFileCount = env->GetArrayLength(jBmpPathArray);
    
    if (0 == logoTypeCount || 0 == bmpFileCount || logoTypeCount != bmpFileCount)    {
        ALOGE("[BOOTLOGO] Arguments(logoTypeCount: %d, bmpFileCount: %d) are invalid in %s\n", logoTypeCount, bmpFileCount, __FUNCTION__);
        return NULL;
    }

    // Get the replaced logo type in C++    
    char const** logoTypeArray = (char const **)malloc(sizeof(char *) * logoTypeCount);
    if (NULL == logoTypeArray) {
        ALOGE("[BOOTLOGO] Allocat memory for logoTypeArray failed in %s\n", __FUNCTION__);
        return NULL;
    }

    memset(logoTypeArray, 0, sizeof(char *) * logoTypeCount);
    for (int i = 0; i < logoTypeCount; i++) {
        logoTypeArray[i] = env->GetStringUTFChars((jstring)env->GetObjectArrayElement(jLogoType, i), NULL);
    }    

    // Get the replaced zpipe file in C++    
    char const** bmpFilePathArray = (char const **)malloc(sizeof(char *) * bmpFileCount);
    if (NULL == bmpFilePathArray) {
        ALOGE("[BOOTLOGO] Allocat memory for bmpFilePathArray failed in %s\n", __FUNCTION__);
        // Free logoTypeArray memory
        free(logoTypeArray);
        logoTypeArray = NULL;
        return NULL; 
    } 

    memset(bmpFilePathArray, 0, sizeof(char *) * bmpFileCount);
    char **img565FilePathArray = (char **)malloc(sizeof(char *) * bmpFileCount);
    memset(img565FilePathArray, 0, sizeof(char *) * bmpFileCount);
    jclass jStringClass = env->FindClass("java/lang/String");
    jobjectArray jImg565FilePathArray = env->NewObjectArray(bmpFileCount, jStringClass, NULL);
    for (int i = 0; i < bmpFileCount; i++) {
        bmpFilePathArray[i] = env->GetStringUTFChars((jstring)env->GetObjectArrayElement(jBmpPathArray, i), NULL);
        int img565PathLen = createFilePath(bmpFilePathArray[i], &img565FilePathArray[i], "img565");
        if (img565PathLen <= 0)        {
            ALOGE("[BOOTLOGO] Create image565 file path failed(%d)\n", img565PathLen);
            return NULL;
        }

        // Create the compressed file path in Java 
        env->SetObjectArrayElement(jImg565FilePathArray, i, env->NewStringUTF(img565FilePathArray[i]));

        // compress the image565 file
        bmp_to_rgb565(bmpFilePathArray[i], img565FilePathArray[i]);
        freeFilePath(&img565FilePathArray[i]);
    }

    free(img565FilePathArray);
    img565FilePathArray = NULL;
    
    return jImg565FilePathArray;
}
*/
    
static JNINativeMethod gMethods[] = {    
    {"prepareCamera", "(I)I",(void *)Java_com_camera_simplewebcam_CameraPreview_prepareCamera},
    {"getBufferSize", "()I", (void *)Java_com_camera_simplewebcam_CameraPreview_getBufferSize},
    {"setDirectBuffer", "(Ljava/lang/Object;I)I", (void *)Java_com_camera_simplewebcam_CameraPreview_setDirectBuffer},
    {"readFrame", "()I", (void *)Java_com_camera_simplewebcam_CameraPreview_readFrame},
    {"stopCamera", "()V", (void *)Java_com_camera_simplewebcam_CameraPreview_stopCamera},
    //{"getImageData", "()[B",(void *)Java_com_camera_simplewebcam_CameraPreview_getImageData},
    {"startRecording", "()I", (void *)Java_com_camera_simplewebcam_CameraPreview_startRecording},
    {"stopRecording", "()I", (void *)Java_com_camera_simplewebcam_CameraPreview_stopRecording},
    {"recording", "()I", (void *)Java_com_camera_simplewebcam_CameraPreview_recording},
    {"captureImage", "()I", (void *)Java_com_camera_simplewebcam_CameraPreview_captureImage},
};



jint JNI_OnLoad(JavaVM* vm, void* reserved) {    
    JNIEnv* env = NULL; 
    int ret = 0;
    
    LOGE("JNI_OnLoad ++");    
    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_6)) {        
        ALOGE("GetEnv failed!");        
        return JNI_ERR;    
    }
    
    //ret = jniRegisterNativeMethods(env, "com/example/reboot/UVCJni", gMethods, NELEM(gMethods));  
    //ret = jniRegisterNativeMethods(env, "com/camera/simplewebcam/CameraPreview", gMethods, NELEM(gMethods));  
    ret = jniRegisterNativeMethods(env, "com/camera/usbcamera/UVCJni", gMethods, NELEM(gMethods));   
    if (ret) {        
        LOGE("call jniRegisterNativeMethods() failed, ret:%d\n", ret);    
    }    
    
    LOGE("JNI_OnLoad done");        

    return JNI_VERSION_1_6;
}


