#include <cutils/memory.h>
 
#include <unistd.h>
#include <utils/Log.h>
 
#include <binder/IPCThreadState.h>
#include <binder/ProcessState.h>
#include <binder/IServiceManager.h>
 
#include <gui/Surface.h>
#include <gui/SurfaceComposerClient.h>
#include <gui/ISurfaceComposer.h>
#include <ui/DisplayInfo.h>
#include <ui/Rect.h>
#include <ui/Region.h>
#include <android/native_window.h>
#include <SkGraphics.h>
#include <SkBitmap.h>
#include <SkCanvas.h>
#include <SkDevice.h>
#include <SkStream.h>
#include <SkImageDecoder.h>
 
#include <hardware/hwcomposer_defs.h>
using namespace android;
 
static inline SkBitmap::Config convertPixelFormat(PixelFormat format) {
    /* note: if PIXEL_FORMAT_RGBX_8888 means that all alpha bytes are 0xFF, then
        we can map to SkBitmap::kARGB_8888_Config, and optionally call
        bitmap.setIsOpaque(true) on the resulting SkBitmap (as an accelerator)
    */
    switch (format) {
    case PIXEL_FORMAT_RGBX_8888:    return SkBitmap::kARGB_8888_Config;
    case PIXEL_FORMAT_RGBA_8888:    return SkBitmap::kARGB_8888_Config;
    case PIXEL_FORMAT_RGB_565:      return SkBitmap::kRGB_565_Config;
    default:                        return SkBitmap::kNo_Config;
    }
}
 
int main(int argc, char** argv)
{
    // set up the thread-pool
    sp<ProcessState> proc(ProcessState::self());
    ProcessState::self()->startThreadPool();
 
    // create a client to surfaceflinger
    sp<SurfaceComposerClient> client = new SurfaceComposerClient();
    //DisplayoutBuffer display;
    //client->getDisplayoutBuffer(client->getBuiltInDisplay(HWC_DISPLAY_PRIMARY), &display);
	sp<IBinder> dtoken(SurfaceComposerClient::getBuiltInDisplay(
            ISurfaceComposer::eDisplayIdMain));
	DisplayInfo dinfo;
	//获取屏幕的宽高等信息
    status_t status = SurfaceComposerClient::getDisplayInfo(dtoken, &dinfo);
	printf("w=%d,h=%d,xdpi=%f,ydpi=%f,fps=%f,ds=%f\n", 
        dinfo.w, dinfo.h, dinfo.xdpi, dinfo.ydpi, dinfo.fps, dinfo.density);
    if (status)
        return -1;
    sp<SurfaceControl> surfaceControl = client->createSurface(String8("testsurface"),
            dinfo.w, dinfo.h, PIXEL_FORMAT_RGB_565, 0);
 
/****************************第一张图******************************************************/
    SurfaceComposerClient::openGlobalTransaction();
    surfaceControl->setLayer(100000);//设定Z坐标
	surfaceControl->setPosition(0, 0);//以左上角为(0,0)设定显示位置
    SurfaceComposerClient::closeGlobalTransaction();
	surfaceControl->show();//感觉没有这步,图片也能显示
	sp<Surface> surface = surfaceControl->getSurface();
 
	ANativeWindow_Buffer outBuffer;
	//Surface::SurfaceoutBuffer outBuffer;
	surface->lock(&outBuffer,NULL);//获取surface缓冲区的地址
    ssize_t bpr = outBuffer.stride * bytesPerPixel(outBuffer.format);
    android_memset16((uint16_t*)outBuffer.bits, 0xF800, bpr*outBuffer.height);//往surface缓冲区塞要显示的RGB内容
    surface->unlockAndPost();
	sleep(3);
 
//用skia画图
/*******************************第三张图***************************************************/
    SurfaceComposerClient::openGlobalTransaction();
    surfaceControl->setSize(320, 420);
	surfaceControl->setPosition(100, 100);
    SurfaceComposerClient::closeGlobalTransaction();
	surfaceControl->show();//感觉没有这部图片也能显示
	
    SkPaint paint;
    paint.setColor(SK_ColorBLUE);
    Rect rect(0, 0, 320, 240);
    Region dirtyRegion(rect);
    
    surface->lock(&outBuffer, &rect);
    bpr = outBuffer.stride * bytesPerPixel(outBuffer.format);
//    printf("w=%d,h=%d,bpr=%d,fmt=%d,bits=%p\n", outBuffer.w, outBuffer.h, bpr, outBuffer.format, outBuffer.bits);
    SkBitmap bitmap;
    bitmap.setConfig(convertPixelFormat(outBuffer.format), 320, 240, bpr);
    bitmap.setPixels(outBuffer.bits);
    SkCanvas canvas;
    SkRegion clipReg;
    const Rect b(dirtyRegion.getBounds());
    clipReg.setRect(b.left, b.top, b.right, b.bottom);
    canvas.clipRegion(clipReg);
    canvas.drawARGB(0, 0xFF, 0x00, 0xFF);
    canvas.drawCircle(200, 200, 100, paint);
    bitmap.notifyPixelsChanged();
    surface->unlockAndPost();
    sleep(3);
 
 
/**********************************************************************************/
    SkFILEStream stream("/sdcard/test.jpg");
    SkImageDecoder* codec = SkImageDecoder::Factory(&stream);
    if(codec){
    	SkBitmap bmp;
    	stream.rewind();
    	codec->decode(&stream, &bmp, SkBitmap::kRGB_565_Config, SkImageDecoder::kDecodePixels_Mode);
    	surface->lock(&outBuffer,NULL);
    	bpr = outBuffer.stride * bytesPerPixel(outBuffer.format);
    	bitmap.setConfig(convertPixelFormat(outBuffer.format), 320, 240, bpr);
    	bitmap.setPixels(outBuffer.bits);
    	//dev = new SkDevice(bitmap);
    	//canvas.setDevice(dev);
    	canvas.drawBitmap(bmp, SkIntToScalar(200), SkIntToScalar(300));
    	surface->unlockAndPost();
		sleep(3);
		//delete dev;
    }
    
    IPCThreadState::self()->joinThreadPool();
    
    IPCThreadState::self()->stopProcess();
 
    return 0;
}
