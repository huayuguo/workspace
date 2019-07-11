#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include "fun.h"


// 将字符串转换为数字
/*int atoi(char *s)
{
    int sign;
    int n;

    while(*s == ' ')
        s++;
    sign = (*s == '-') ? -1 : 1;
    if('-' == *s || '+' == *s)
        s++;
    for(n = 0; isdigit(*s); s++)
    {
        n = n * 10 + (*s - '0');
    }
    return n * sign;
}*/

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
void InitializeFrame(int width, int height, enum VideoFormat format, YuvVideoFrame yuv_video_frame)
{
    int y_pixel_num, u_pixel_num, v_pixel_num;

    yuv_video_frame->width = width;
    yuv_video_frame->height = height;
    yuv_video_frame->format = format;

    y_pixel_num = width * height;
    switch(format)
    {
    case YUV444:
        u_pixel_num = width * height;
        v_pixel_num = width * height;
        break;
    case YUV420:
        u_pixel_num = width * height / 4;
        v_pixel_num = width * height / 4;
        break;
    case YUV422:
        u_pixel_num = width * height / 2;
        v_pixel_num = width * height / 2;
        break;
    default:
        break;
    }
    yuv_video_frame->y_component = (uchar *)malloc(y_pixel_num * sizeof(uchar));
    if(NULL == yuv_video_frame->y_component)
    {
        printf("run out of memory\n");
        exit(0);
    }
    yuv_video_frame->u_component = (uchar *)malloc(u_pixel_num * sizeof(uchar));
    if(NULL == yuv_video_frame->u_component)
    {
        printf("run out of memory\n");
        exit(0);
    }
    yuv_video_frame->v_component = (uchar *)malloc(v_pixel_num * sizeof(uchar));
    if(NULL == yuv_video_frame->v_component)
    {
        printf("run out of memory\n");
        exit(0);
    }
}

/****************************************************************************
DisposeFrame：
    释放视频帧
input：
    YuvVideoFrame yuv_video_frame
output：
return：
****************************************************************************/
void DisposeFrame(YuvVideoFrame yuv_video_frame)
{
    free(yuv_video_frame->y_component);
    free(yuv_video_frame->u_component);
    free(yuv_video_frame->v_component);
    free(yuv_video_frame);
}

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
             YuvVideoFrame yuv_video_frame)
{
    int i, j;

    if(YUV444 == yuv_video_frame->format)
    {
        for(i = 0; i < height; i++)
        {
            for(j = 0; j < width; j++)
            {
                *(yuv_video_frame->y_component + (y + i) * yuv_video_frame->width + (x + j)) = num_color_info.y_value;
                *(yuv_video_frame->u_component + (y + i) * yuv_video_frame->width + (x + j)) = num_color_info.u_value;
                *(yuv_video_frame->v_component + (y + i) * yuv_video_frame->width + (x + j)) = num_color_info.v_value;
            }
        }
    }

    if(YUV420 == yuv_video_frame->format)
    {
        for(i = 0; i < height; i++)
        {
            for(j = 0; j < width; j++)
            {
                *(yuv_video_frame->y_component + (y + i) * yuv_video_frame->width + (x + j)) = num_color_info.y_value;
                *(yuv_video_frame->u_component + (y + i) / 2 * yuv_video_frame->width / 2 + (x + j) / 2) = num_color_info.u_value;
                *(yuv_video_frame->v_component + (y + i) / 2 * yuv_video_frame->width / 2 + (x + j) / 2) = num_color_info.v_value;
            }
        }
    }

    if(YUV422 == yuv_video_frame->format)
    {
        for(i = 0; i < height; i++)
        {
            for(j = 0; j < width; j++)
            {
                *(yuv_video_frame->y_component + (y + i) * yuv_video_frame->width + (x + j)) = num_color_info.y_value;
                *(yuv_video_frame->u_component + (y + i) * yuv_video_frame->width / 2 + (x + j) / 2) = num_color_info.u_value;
                *(yuv_video_frame->v_component + (y + i) * yuv_video_frame->width / 2 + (x + j) / 2) = num_color_info.v_value;
            }
        }
    }
}

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
              YuvVideoFrame yuv_video_frame)
{
    int line_width = info_line.width;
    int line_length = info_line.length;

    if(0 == digit || 4 == digit || 5 == digit || 6 == digit || 8 == digit || 9 == digit)
    {
        AddLine(x, y + line_width, line_width, line_length, num_color_info, yuv_video_frame);
    }
    if(0 == digit || 2 == digit || 6 == digit || 8 == digit)
    {
        AddLine(x, y + line_width * 2 + line_length, line_width, line_length, num_color_info, yuv_video_frame);
    }
    if(0 == digit || 1 == digit || 2 == digit || 3 == digit || 4 == digit || 7 == digit || 8 == digit || 9 == digit)
    {
        AddLine(x + line_width + line_length, y + line_width, line_width, line_length, num_color_info, yuv_video_frame);
    }
    if(0 == digit || 1 == digit || 3 == digit || 4 == digit || 5 == digit || 6 == digit || 7 == digit || 8 == digit || 9 == digit)
    {
        AddLine(x + line_width + line_length, y + line_width * 2 + line_length, line_width, line_length, num_color_info, yuv_video_frame);
    }
    if(0 == digit || 2 == digit || 3 == digit || 5 == digit || 6 == digit || 7 == digit || 8 == digit || 9 == digit)
    {
        AddLine(x + line_width, y, line_length, line_width, num_color_info, yuv_video_frame);
    }
    if(2 == digit || 3 == digit || 4 == digit || 5 == digit || 6 == digit || 8 == digit || 9 == digit)
    {
        AddLine(x + line_width, y + line_width + line_length, line_length, line_width, num_color_info, yuv_video_frame);
    }
    if(0 == digit || 2 == digit || 3 == digit || 5 == digit || 6 == digit || 8 == digit)
    {
        AddLine(x + line_width, y + line_width * 2 + line_length * 2, line_length, line_width, num_color_info, yuv_video_frame);
    }
}


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
void AddNumInYuvVideo(int frame_num, int x, int y, struct LineInfo line_info, struct NumColorInfo num_color_info, YuvVideoFrame yuv_video_frame)
{
    int distance;
    int i;
    int digit;

    distance = line_info.width * 6 + line_info.length;
    i = 0;
    do{
        digit = frame_num % 10;
        AddDigit(digit, x - i * distance, y, line_info, num_color_info, yuv_video_frame);
        i++;
    }while(frame_num /= 10);
}


/****************************************************************************
InitializeNumColorInfo：
    初始化数字的颜色信息
input：
    enum NumColor color
output:
    struct NumColorInfo &num_color_info
return：
****************************************************************************/
void InitializeNumColorInfo(enum NumColor color, struct NumColorInfo &num_color_info)
{
    switch(color)
    {
    case BLACK: // 黑色
        num_color_info.y_value = 16;
        num_color_info.u_value = 128;
        num_color_info.v_value = 128;
        break;
    case WHITE: // 白色
        num_color_info.y_value = 235;
        num_color_info.u_value = 128;
        num_color_info.v_value = 128;
        break;
    case RED:  // 红色
        num_color_info.y_value = 65;
        num_color_info.u_value = 100;
        num_color_info.v_value = 212;
        break;
    case GREEN: // 绿色
        num_color_info.y_value = 112;
        num_color_info.u_value = 72;
        num_color_info.v_value = 58;
        break;
    case BLUE: // 蓝色
        num_color_info.y_value = 35;
        num_color_info.u_value = 212;
        num_color_info.v_value = 114;
        break;
    default:  // 其他情况黑色
        num_color_info.y_value = 16;
        num_color_info.u_value = 128;
        num_color_info.v_value = 128;
        break;
    }
}

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
void InitializeLineAndPositionInfo(int width, int height, enum NumPosition position, struct LineInfo &line_info, int &x, int &y)
{
    line_info.length = width / 100;  // 线条的长度
    line_info.width = MAX(width / 400, 1);  // 线条的宽度

    // 数字的起始位置
    switch(position)
    {
    case LeftTop: // 左上角
        x = line_info.width * 22 + line_info.length * 3;
        y = line_info.width * 4;
        break;
    case RightTop: // 右上角
        x = width - line_info.width * 6 - line_info.length;
        y = line_info.width * 4;
        break;
    case LeftBottom: // 左下角
        x = line_info.width * 22 + line_info.length * 3;
        y = height - line_info.width * 7 - line_info.length * 2;
        break;
    case RightBottom: // 右下角
        x = width - line_info.width * 6 - line_info.length;
        y = height - line_info.width * 7 - line_info.length * 2;
        break;
    default:
        break;
    }
}