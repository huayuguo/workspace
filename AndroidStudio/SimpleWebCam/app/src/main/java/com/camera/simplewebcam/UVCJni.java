package com.camera.simplewebcam;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.nio.ByteBuffer;

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
    static native int stopCaptureImage();
    static native int setImagePath(String path);
    
    static {
        System.loadLibrary("UVCJni");
    } 
}
