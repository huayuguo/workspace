/*******************************************************************************
*  Copyright Statement:
*  --------------------
*  This software is protected by Copyright and the information contained
*  herein is confidential. The software may not be copied and the information
*  contained herein may not be used or disclosed except with the written
*  permission of MediaTek Inc. (C) 2016
*
*******************************************************************************/

/*******************************************************************************
 *
 * Filename:
 * ---------
 *	   gps_uart.c
 *
 * Description:
 * ------------
 *    This file defines the sample code of uart driver.
 *    User must implement all the functions in this file based on your MCU.
 *
 *******************************************************************************/
#include "gps_uart.h"
#include "sw_types.h"
#include <stdio.h>
#include <stdarg.h>
#include <sys/time.h>
#include <string.h>
#include <sys/epoll.h>
#include <errno.h>
#include <pthread.h>
#include <termios.h>
#include <time.h>


#ifdef LOGD
#undef LOGD
#endif
#ifdef LOGW
#undef LOGW
#endif
#ifdef LOGE
#undef LOGE
#endif
#if 0
//#define LOGD(...) tag_log(1, "[gpshal]", __VA_ARGS__);
//#define LOGW(...) tag_log(1, "[gpshal] WARNING: ", __VA_ARGS__);
//#define LOGE(...) tag_log(1, "[gpshal] ERR: ", __VA_ARGS__);
#define LOGD(...)
#define LOGW(...)
#define LOGE(...)
#else
#define LOG_TAG "flashtool"
//#include <cutils/sockets.h>
#include <cutils/log.h>     /*logging in logcat*/
#define LOGD(fmt, arg ...) ALOGD("%s: " fmt, __FUNCTION__ , ##arg)
#define LOGW(fmt, arg ...) ALOGW("%s: " fmt, __FUNCTION__ , ##arg)
#define LOGE(fmt, arg ...) ALOGE("%s: " fmt, __FUNCTION__ , ##arg)
#endif



extern int g_fd_mt3333_data;
static uint32 err_threshold = 0xFFFFF; //Timeout flag, can be changed.

void GPS_UART_IgnoreData(void)
{
	uint8 buf[1024];
	int count=0;
	while(1)
    {
    	count = read(g_fd_mt3333_data, &buf, 1024);
		if(count>=0){
			LOGE("count=%d",count);
			 continue;
		}
		break;
	}

	//usleep(500 * 1000);

	while(1)
    {
    	count = read(g_fd_mt3333_data, &buf, 1024);
		if(count>=0){
			LOGE("count1=%d",count);
			 continue;
		}
		break;
	}
}

void GPS_UART_Init(void)	
{

    /* uart pinmux configure */

    /* enable uart clock */
	
	//Set Baudrate

}

//Get one byte data from UART and return this byte.
//This function should wait in a while loop until received a data.
uint8 GPS_UART_GetByte(void)
{
    uint8	g_uart_last_rx_data = 0;

	while(1)
    {
		if(1 == read(g_fd_mt3333_data, &g_uart_last_rx_data, 1)){
			return g_uart_last_rx_data;
		}
	}
}

//Get one byte data from UART and output this byte by formal parameter.
//This function will return finally if received a data or timeout.
bool GPS_UART_GetByteStatus(uint8 *data)
{
	uint8	g_uart_last_rx_data = 0;
	uint32  err_count=0;

	while((err_count++)<err_threshold)
    {
		if(1 == read(g_fd_mt3333_data, data, 1)){
			return TRUE;
		}
	}
	return FALSE;
}

//Get one byte data from UART and output this byte by formal parameter.
//This function will return immediatelly after trying reading data from UART no matter any data is read.
bool GPS_UART_GetByte_NO_TIMEOUT(uint8 *data)
{	
	if(1 == read(g_fd_mt3333_data, data, 1)){
		return TRUE;
	}
	return FALSE;
}

//Read data from UART, read length is defined by input parameter, and output the string by formal parameter.
bool GPS_UART_GetByte_Buffer(uint32* buf, uint32 length)
{
	bool ret;
	uint32 i;
	uint8 * buf8 = (uint8*)buf;

	for(i=0; i<length; i++)
	{
		ret = GPS_UART_GetByteStatus(buf8+i);
		if(!ret){ return FALSE;}
	}

	return TRUE;
}
void GPS_UART_Flush(void)
{
	LOGE("result:%d",fsync(g_fd_mt3333_data));
	
}

//Put one byte data to UART.
void GPS_UART_PutByte(uint8 data)
{
	while(1)
    {
		if(1 == write(g_fd_mt3333_data, &data, 1)){
			return;
		}
	}
    
}

//Put data to UART, write buffer and length are defined by input parameter.
void GPS_UART_PutByte_Buffer(uint32* buf, uint32 length)
{
	uint32 i;
	uint8* tmp_buf = (uint8*)buf;

	for(i=0; i < length; i++)
	{
		GPS_UART_PutByte(*(tmp_buf+i));
	}
}

//Get 16 bits data from UART.
uint16 GPS_UART_GetData16(void)			//ok, high byte is first
{
	uint8	tmp,index;
	uint16 	tmp16;
	uint16  result =0;
	for (index =0;index < 2;index++)
	{
		tmp = GPS_UART_GetByte();
		tmp16 = (uint16)tmp;
		result |= (tmp16 << (8-8*index));
	}
	return result;
}

//Put 16 bits data from UART.
void GPS_UART_PutData16(uint16 data)		//ok, high byte is first
{
	uint8	tmp,index;
	uint16 	tmp16;

	for (index =0;index < 2;index++)
	{
		tmp16 = (data >> (8-8*index));
		tmp = (uint8)tmp16;
		GPS_UART_PutByte(tmp);
	}
}

//Get 32 bits data from UART.
uint32 GPS_UART_GetData32(void)			//ok, high byte is first
{
	uint8	tmp,index;
	uint32 	tmp32;
	uint32  result =0;
	for (index =0;index < 4;index++)
	{
		tmp = GPS_UART_GetByte();
		tmp32 = (uint32)tmp;
		result |= (tmp32 << (24-8*index));
	}
	return result;
}

//Put 32 bits data from UART.
void GPS_UART_PutData32(uint32 data)		//ok, high byte is first
{
	uint8	tmp,index;
	uint32 	tmp32;

	for (index =0;index < 4;index++)
	{
		tmp32 = (data >> (24-8*index));
		tmp = (uint8)tmp32;
		GPS_UART_PutByte(tmp);
	}
}

