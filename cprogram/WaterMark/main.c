#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include "fun.h"


int main(int argc, char *argv[])
{
    char InputVideoName[100];
    char OutputVideoName[100];
    int width;
    int height;
    int FrameNum;
    enum VideoFormat format = YUV444;
    enum NumColor color = BLACK;
    enum NumPosition position = LeftBottom;
    YuvVideoFrame yuv_video_frame;
    time_t start_time, end_time;
    FILE *ifp, *ofp;
    int y_pixel_num, u_pixel_num, v_pixel_num;
    struct NumColorInfo num_color_info;
    struct LineInfo line_info;
    int x, y; // 数字的位置坐标

    if(argc != 9)
    {
        printf("Error!\n");
        return -1;
    }
    strcpy(InputVideoName, argv[1]);
    strcpy(OutputVideoName, argv[2]);
    width = atoi(argv[3]);
    height = atoi(argv[4]);
    FrameNum = atoi(argv[5]); // 帧数
    format = (enum VideoFormat)atoi(argv[6]); // 格式 0表示yuv444，1表示yuv420 planar, 2表示yuv422 planar
    color = (enum NumColor)atoi(argv[7]); // 字体颜色 0表示黑色，1表示白色，2表示红色，3表示绿色，4表示蓝色
    position = (enum NumPosition)atoi(argv[8]); // 帧序号的位置 0表示左上角，1表示右上角，2表示左下角，3表示右下角

    start_time = time(NULL);

    // 初始化帧
    yuv_video_frame = (YuvVideoFrame)malloc(sizeof(struct YuvFrame));
    if(NULL == yuv_video_frame)
    {
        printf("run out of memory\n");
        exit(0);
    }
    InitializeFrame(width, height, format, yuv_video_frame);

    // 初始化颜色信息
    InitializeNumColorInfo(color, num_color_info);

    // 初始化数字线条和起始位置信息
    InitializeLineAndPositionInfo(width, height, position, line_info, x, y);

    // 打开输入输出文件
    if((ifp = fopen(InputVideoName, "rb")) == NULL)
    {
        printf("cannot open %s\n", InputVideoName);
        return -1;
    }
    if((ofp = fopen(OutputVideoName, "ab")) == NULL)
    {
        printf("cannot open %s\n", OutputVideoName);
        return -1;
    }

    y_pixel_num = width * height;
    switch(yuv_video_frame->format)
    {
        case YUV444: 
            u_pixel_num = y_pixel_num;
            v_pixel_num = y_pixel_num;
            break;
        case YUV420:
            u_pixel_num = y_pixel_num / 4;
            v_pixel_num = y_pixel_num / 4;
            break;
        case YUV422:
            u_pixel_num = y_pixel_num / 2;
            v_pixel_num = y_pixel_num / 2;
            break;
        default:
            break;
    }

    for(int frame_num = 0; frame_num < FrameNum; frame_num++)
    {
        fread(yuv_video_frame->y_component, sizeof(uchar), y_pixel_num, ifp);
        fread(yuv_video_frame->u_component, sizeof(uchar), u_pixel_num, ifp);
        fread(yuv_video_frame->v_component, sizeof(uchar), v_pixel_num, ifp);

        AddNumInYuvVideo(frame_num, x, y, line_info, num_color_info, yuv_video_frame);

        fwrite(yuv_video_frame->y_component, sizeof(uchar), y_pixel_num, ofp);
        fwrite(yuv_video_frame->u_component, sizeof(uchar), u_pixel_num, ofp);
        fwrite(yuv_video_frame->v_component, sizeof(uchar), v_pixel_num, ofp);
    }
    fclose(ifp);
    fclose(ofp);
    DisposeFrame(yuv_video_frame);

    end_time = time(NULL);
    //printf("\ntime = %d\n", end_time - start_time);

    return 0;
}