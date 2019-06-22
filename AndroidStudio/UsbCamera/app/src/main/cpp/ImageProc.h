
#ifndef __IMAGE_PROC_H__
#define __IMAGE_PROC_H__

#ifdef __cplusplus
extern "C" {
#endif 

int checkCamerabase(void);
int openDevice(int i);
int initDevice(void);
int startCapturing(void);
int readframeonce(void);
int stopCapturing(void);
int startRecording(void); 
int stopRecording(void);
int recording(void);
int captureImage(void);
int uninitDevice(void);
int closeDevice(void);

int setDirectBuffer(char *pDirectBuffer);
int getFrameBufferSize();
int getRealImageSize();

#ifdef __cplusplus
}
#endif
#endif // __IMAGE_PROC_H__
