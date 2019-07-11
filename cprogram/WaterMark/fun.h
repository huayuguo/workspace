

enum VideoFormat{YUV444, YUV420, YUV422};  // 视频格式
enum NumColor{BLACK, WHITE, RED, GREEN, BLUE}; // 数字颜色
enum NumPosition{LeftTop, RightTop, LeftBottom, RightBottom}; // 数字的位置
typedef unsigned char uchar;
typedef struct YuvFrame *YuvVideoFrame;


#define MAX(a, b)  ((a) > (b) ? (a) : (b))

// YUV视频帧
struct YuvFrame
{
    int width; // 视频帧宽
    int height; // 视频帧高
    enum VideoFormat format; // 视频的格式
    uchar *y_component; // y分量
    uchar *u_component; // u分量
    uchar *v_component; // v分量
};

// 数字颜色信息
struct NumColorInfo
{
    uchar y_value;
    uchar u_value;
    uchar v_value;
};

// 数字线条信息
struct LineInfo
{
    int length; // 线条长
    int width; // 线条宽
};




// 将字符串转换为数字
//int atoi(char *s);

/****************************************************************************
InitializeFrame：
    初始化视频帧
input：
    int width 视频帧的宽
    int height 视频帧的高
    enum VideoFormat format 视频的格式
output：
    YuvVideoFrame yuv_video_frame
return：
****************************************************************************/
void InitializeFrame(int width, int height, enum VideoFormat format, YuvVideoFrame yuv_video_frame);

/****************************************************************************
DisposeFrame：
    释放视频帧
input：
    YuvVideoFrame yuv_video_frame
output：
return：
****************************************************************************/
void DisposeFrame(YuvVideoFrame yuv_video_frame);

/****************************************************************************
AddLine：
    在YUV视频帧上添加线条
input：
    int x 位置坐标
    int y 
    int width 添加线条的宽
    int height 添加线条的高 
    struct NumColorInfo num_color_info  数字的颜色
    YuvVideoFrame yuv_video_frame
output：
return：
****************************************************************************/
void AddLine(int x, int y, 
             int width, int height, 
             struct NumColorInfo num_color_info,
             YuvVideoFrame yuv_video_frame);

/****************************************************************************
AddDigit：
    在YUV视频上添加数字
input：
    int digit 添加数字
    int x 位置坐标
    int y 
    struct LineInfo info_line 数字线条信息
    struct NumColorInfo num_color_info 数字颜色
    YuvVideoFrame yuv_video_frame
output：
return：
****************************************************************************/
void AddDigit(int digit,
              int x, int y, 
              struct LineInfo info_line,
              struct NumColorInfo num_color_info,
              YuvVideoFrame yuv_video_frame);

/****************************************************************************
AddNumInYuvVideo：
    在YUV视频上添加帧序号
input：
    int frame_num  帧序号
    int x 数字起始位置坐标
    int y  
    struct LineInfo line_info  数字线条信息
    struct NumColorInfo num_color_info  颜色信息
    YuvVideoFrame yuv_video_frame
output：
return：
****************************************************************************/
void AddNumInYuvVideo(int frame_num, int x, int y, struct LineInfo line_info, struct NumColorInfo num_color_info, YuvVideoFrame yuv_video_frame);

/****************************************************************************
InitializeNumColorInfo：
    初始化数字的颜色信息
input：
    enum NumColor color
output:
    struct NumColorInfo &num_color_info
return：
****************************************************************************/
void InitializeNumColorInfo(enum NumColor color, struct NumColorInfo &num_color_info);

/****************************************************************************
InitializeLineAndPositionInfo：
    初始化线条和位置信息
input：
    int width  视频帧宽
    int height  视频帧高
    enum NumPosition position
output:
    struct LineInfo &line_info 线条信息
    int &x 数字起始坐标
    int &y
return：
****************************************************************************/
void InitializeLineAndPositionInfo(int width, int height, enum NumPosition position, struct LineInfo &line_info, int &x, int &y);