#ifndef EXT_IMG_PROC_IMP_H
#define EXT_IMG_PROC_IMP_H
#include "utilities.h"
//-----------------------------------------------------------------------------
class ExtImgProcImp : public ExtImgProc
{
    protected:
        ExtImgProcImp();
        virtual ~ExtImgProcImp();
    //
    public:
        static ExtImgProc*  getInstance(void);
        virtual void        destroyInstance(void);
        //
        virtual MBOOL       init(void);
        virtual MBOOL       uninit(void);
        virtual MUINT32     getImgMask(void);
        virtual MBOOL       doImgProc(ImgInfo& img);
		virtual MBOOL		setImgMask(MUINT32 u4ImgMask);
   //
   private:
        mutable Mutex   mLock;
        volatile MINT32 mUser;
        MUINT32         mImgMask;
		static YuvVideoFrame yuv_video_frame;
		struct NumColorInfo num_color_info;
		struct LineInfo line_info;
		int mark_x, mark_y;
};
//-----------------------------------------------------------------------------
#endif

