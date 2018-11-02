/*******************************************************************************
 *  Copyright Statement:
 *  --------------------
 *  This software is protected by Copyright and the information contained
 *  herein is confidential. The software may not be copied and the information
 *  contained herein may not be used or disclosed except with the written
 *  permission of MediaTek Inc. (C) 2016
 *
 ******************************************************************************/

/*******************************************************************************
 * Filename:
 * ---------
 *  brom_mt3301.cpp
 *
 * Project:
 * --------
 *  BootRom Library
 *
 * Description:
 * ------------
 *  BootRom communication functions implementation which are used to sync with BootRom.
 *
 *******************************************************************************/
#include <stdio.h>
#include "flashtool.h"
#include "gps_uart.h"
#include "brom_base.h"
#include "sw_types.h"


#include <fcntl.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <sys/epoll.h>
#include <sys/stat.h>
#include <unistd.h>
#include "mt3333_controller.h"
#include "mnl_common.h"
#include "gps_controller.h"


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
#include <cutils/sockets.h>
#include <cutils/log.h>     /*logging in logcat*/
#define LOGD(fmt, arg ...) ALOGD("%s: " fmt, __FUNCTION__ , ##arg)
#define LOGW(fmt, arg ...) ALOGW("%s: " fmt, __FUNCTION__ , ##arg)
#define LOGE(fmt, arg ...) ALOGE("%s: " fmt, __FUNCTION__ , ##arg)
#endif



// MT3301 BootRom Start Command
#define MT3301_BOOT_ROM_START_CMD1      0xA0
#define MT3301_BOOT_ROM_START_CMD2      0x0A
#define MT3301_BOOT_ROM_START_CMD3      0x50
#define MT3301_BOOT_ROM_START_CMD4      0x05

#define NMEA_START_CMD1         '$'
#define NMEA_START_CMD2         'P'
#define NMEA_START_CMD3         'M'
#define NMEA_START_CMD4         'T'
#define NMEA_START_CMD5         'K'
#define NMEA_START_CMD6         '1'
#define NMEA_START_CMD7         '8'
#define NMEA_START_CMD8         '0'
#define NMEA_START_CMD9         '*'
#define NMEA_START_CMD10        '3'
#define NMEA_START_CMD11        'B'
#define NMEA_START_CMD12      0x0D
#define NMEA_START_CMD13      0x0A


static const unsigned char MT3301_BOOT_ROM_START_CMD[] =
{
   MT3301_BOOT_ROM_START_CMD1,
   MT3301_BOOT_ROM_START_CMD2,
   MT3301_BOOT_ROM_START_CMD3,
   MT3301_BOOT_ROM_START_CMD4
};                              

static unsigned char NMEA_START_CMD[] =
{
   NMEA_START_CMD1,
   NMEA_START_CMD2,
   NMEA_START_CMD3,
   NMEA_START_CMD4,
   NMEA_START_CMD5,
   NMEA_START_CMD6,
   NMEA_START_CMD7,
   NMEA_START_CMD8,
   NMEA_START_CMD9,
   NMEA_START_CMD10, 
   NMEA_START_CMD11, 
   NMEA_START_CMD12, 
   NMEA_START_CMD13 
};                              
extern int g_fd_mt3333_data;    
extern MNL_CONFIG_T mnld_cfg;


/*******************************************************************************
 * Function name:
 * ---------
 *  BRom_StartCmd
 *
 * Description:
 * ------------
 *  This funciton is used to send SYNC_CHAR to build connection with bootRom.
 *  The communication flow please refer to the BromDLL document.
 *  'BL_PRINT' is a sample to output debug log.
 *
 * Input:
 * --------
 *  arg: download arguments structure includes callback function and parameter. 
 *
 *******************************************************************************/
int BRom_StartCmd(GPS_Download_Arg arg)
{
	unsigned char data8;
	unsigned long i;
	unsigned char tmp8;
	unsigned long cnt = 0;
	int retry_max=5;
#if 0
	LOGE("[FUNET]Send PMTK180 to force MT3333/39 into boot rom mode");
	//Send PMTK180 to force 3333 into boot rom
	GPS_UART_PutByte_Buffer((unsigned long *)NMEA_START_CMD, sizeof(NMEA_START_CMD));
	if(0 != mt3333_controller_set_baudrate_length_parity_stopbits(g_fd_mt3333_data ,115200, 8, 'N', 1)){
		LOGE("configure uart baudrate failed");
		return 1;
	}
	//delay 500ms. 
	usleep(500 * 1000);
	GPS_UART_IgnoreData();    

#else
	//usleep(3000 * 1000);
	if(0 != mt3333_controller_set_baudrate_length_parity_stopbits(g_fd_mt3333_data ,115200, 8, 'N', 1)){
		LOGE("configure uart baudrate failed");
		return 1;
	}
	
	LOGE("[FUNET]Send first sync char111");
	
	
	//mnl_get_rdelay();
	//mnl_set_rdelay(1500);
	//mnl_get_rdelay();
	pthread_mutex_t mutx = PTHREAD_MUTEX_INITIALIZER;

HADK_AWAKE_BEGIN:	
	cnt = 0;
	pthread_mutex_lock(&mutx);
	//mnl_set_pwrctl(1);
	//for(int j=0; j<2048 ;j++){GPS_UART_PutByte(MT3301_BOOT_ROM_START_CMD[0]);}
	mnl_set_pwrctl(1);
	//GPS_UART_PutByte(MT3301_BOOT_ROM_START_CMD[0]);
	//GPS_UART_PutByte(MT3301_BOOT_ROM_START_CMD[0]);
	pthread_mutex_unlock(&mutx);
#endif	
	while(1)
	{
		GPS_UART_PutByte(MT3301_BOOT_ROM_START_CMD[0]); //First start command sync char
		if(1 == GPS_UART_GetByte_NO_TIMEOUT(&data8))
		{
			 tmp8 = 0x5F;
			 LOGE("[FUNET]Received data:%x", data8);
			 if(tmp8 == data8)
			 {
				 LOGE("[FUNET] First char sync ok");
				 goto SECOND_CHAR;	
			 }
		}
		//else
		//{
		//	LOGE("[FUNET]No data received");
		//}
		
		cnt++;
		if (cnt > 20000)
		{
			cnt = 0;
			retry_max--;
			LOGE("[FUNET] First char sync timeout,retry=%d",retry_max);
			if(retry_max > 0){
				mnl_set_pwrctl(0);
				//usleep(1500 * 1000);
				msleep(1500);
				goto HADK_AWAKE_BEGIN;
			}
			return 1;
		}
	}

	
	
SECOND_CHAR:
	cnt=0;
	GPS_UART_IgnoreData();
	LOGE("[FUNET]Sync second char");

	i = 1;
	GPS_UART_PutByte(MT3301_BOOT_ROM_START_CMD[i]); //2nd sync char
	tmp8 = 0xF5; // ~MT3301_BOOT_ROM_START_CMD[i]
SECOND_CHAR_1:	
	data8 = GPS_UART_GetByte();
	if(tmp8 != data8)
	{
		LOGE("[FUNET]Wrong ack data received:%x", data8);
		if(data8 == 0x5F){
			goto SECOND_CHAR_1;
		}
		retry_max--;
		LOGE("[FUNET] Second char sync timeout,retry=%d",retry_max);
		if(retry_max > 0){
			mnl_set_pwrctl(0);
			//usleep(1500 * 1000);
			msleep(1500);
			goto HADK_AWAKE_BEGIN;
		}
		return 1;
	}

	LOGE("[FUNET]Sync third char");
	i = 2;
	GPS_UART_PutByte(MT3301_BOOT_ROM_START_CMD[i]); //3rd sync char
	tmp8 = 0xAF;
	data8 = GPS_UART_GetByte();
	if(tmp8 != data8)
	{
		LOGE( "[FUNET]Wrong ack data received:%x", data8);
		
		retry_max--;
		LOGE("[FUNET] Third char sync timeout,retry=%d",retry_max);
		if(retry_max > 0){
			mnl_set_pwrctl(0);
			//usleep(1500 * 1000);
			msleep(1500);
			goto HADK_AWAKE_BEGIN;
		}
		return 1;
	}

	LOGE("[FUNET]Sync forth char");
	i = 3;
	GPS_UART_PutByte(MT3301_BOOT_ROM_START_CMD[i]); //4th sync char
	tmp8 = 0xFA;
	data8 = GPS_UART_GetByte();
	if(tmp8 != data8)
	{
		LOGE("[FUNET]Wrong ack data received:%x", data8);
		
		retry_max--;
		LOGE("[FUNET] Four char sync timeout,retry=%d",retry_max);
		if(retry_max > 0){
			mnl_set_pwrctl(0);
			//usleep(1500 * 1000);
			msleep(1500);
			goto HADK_AWAKE_BEGIN;
		}
		return 1;
	}

    return 0;
}


/*******************************************************************************
 * Function name:
 * ---------
 *  Boot_FlashTool
 *
 * Description:
 * ------------
 *  This funciton is used to build connection with bootRom and download DA to target.
 *  The communication flow please refer to the BromDLL document.
 *  'BL_PRINT' is a sample to output debug log.
 *
 * Input:
 * --------
 *  arg: download arguments structure includes callback function and parameter. 
 *  p_arg: download arguments structure includes start address, DA buffer and DA length.
 *
 *******************************************************************************/
int Boot_FlashTool(const s_BOOT_FLASHTOOL_ARG  *p_arg, GPS_Download_Arg arg)
{   
   // check data
   if( NULL == p_arg )
      return BROM_INVALID_ARGUMENTS;

   LOGE("[FUNET]Enter BRom_StartCmd");

   // send start command 
   if( BRom_StartCmd(arg) )
   {
       return BROM_CMD_START_FAIL;
   }
   LOGE( "[FUNET]Start Command Sync Done");

   if(BRom_WriteBuf(arg, p_arg->m_da_start_addr, p_arg->m_da_buf, p_arg->m_da_len))
   {
      return BROM_DOWNLOAD_DA_FAIL;
   }

   LOGE("[FUNET]Boot_FlashTool: BRom_WriteBuf() Pass!");

   // jump to m_da_start_addr to execute DA code on Internal SRAM 
   if(BRom_JumpCmd( arg, p_arg->m_da_start_addr ))
      return BROM_CMD_JUMP_FAIL;
   LOGE("[FUNET]Boot_FlashTool: BRom_JumpCmd() Pass!");
   return BROM_OK;
}
//------------------------------------------------------------------------------
