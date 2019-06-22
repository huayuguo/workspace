package com.camera.usbcamera;

class UVCJni {

    // JNI functions
    static native int prepareCamera(int videoid);
    static native int getBufferSize();
    static native int setDirectBuffer(Object buffer, int length);
    static native int readFrame();
    static native void stopCamera();
    static native int startRecording();
    static native int stopRecording();
    static native int captureImage();
    static native int recording();
    
    static {
        System.loadLibrary("UVCJni");
    } 
}
