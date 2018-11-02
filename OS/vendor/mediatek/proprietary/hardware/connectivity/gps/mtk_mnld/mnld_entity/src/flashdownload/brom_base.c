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
 *  brom_base.cpp
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
#include "DOWNLOAD.H"
#include "brom_base.h"
#include "GPS_DL_api.h"
#include "flashtool.h"
#include "sw_types.h"
#include "gps_uart.h"
#include <string.h>

#include <fcntl.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <sys/epoll.h>
#include <sys/stat.h>
#include <unistd.h>

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


// BootRom Command 
#define BOOT_ROM_WRITE_CMD    0xA1
#define BOOT_ROM_CHECKSUM_CMD 0xA4
#define BOOT_ROM_JUMP_CMD     0xA8


//------------------------------------------------------------------------------
// bootrom command                                                              
//------------------------------------------------------------------------------


/*******************************************************************************
 * Function name:
 * ---------
 *  BRom_WriteBuf
 *
 * Description:
 * ------------
 *  This funciton is used to download 'download agent' file(DA) to target.
 *  The communication flow please refer to the BromDLL document.
 *  'BL_PRINT' is a sample to output debug log.
 *
 * Input:
 * --------
 *  arg: download arguments structure includes callback function and parameter. 
 *  u1BaseAddr: the start address of DA in internal SRAM.
 *  buf_in: DA file buffer.
 *  num_of_byte: DA file size.
 *
 *******************************************************************************/
int BRom_WriteBuf(
      GPS_Download_Arg arg,
      unsigned int ulBaseAddr,
      const unsigned char *buf_in, unsigned int num_of_byte)
{
	const unsigned char *buf = (const unsigned char *)buf_in;
	unsigned short data;
	unsigned int num_of_word = (num_of_byte+1)/2;
	int err;
	unsigned char rate;
	unsigned int finish_rate = 0;
	unsigned int i = 0;
	unsigned short checksum = 0;
	unsigned short brom_checksum = 0;
	unsigned int accuracy;
	unsigned char write_buf[10];
	unsigned char read_buf[10];
	int j;

	if( NULL==buf || 0>=num_of_byte )
	{
	  return 1;
	}

	//Callback function of DA download initialize
	if (arg.m_cb_download_conn_da_init != NULL)
	{
		arg.m_cb_download_conn_da_init(arg.m_cb_download_conn_da_init_arg);
	}
	
   if(BRom_CmdSend(arg, BOOT_ROM_WRITE_CMD ))
      return 2;

   LOGE("[FUNET]BRom_Base::BRom_WriteBuf(): Send BaseAddr ");
   
   if((err=BRom_OutData(arg, ulBaseAddr)))
   {
	   LOGE( "[FUNET]BRom_Base::BRom_WriteBuf(): BRom_OutData(%x): send base address fail!, Err(%d).", ulBaseAddr, err );
      return 3;
   }
   
   if((err=BRom_OutData(arg, num_of_word)))
   {
	   LOGE( "[FUNET]BRom_Base::BRom_WriteBuf(): BRom_OutData(%d): send number of word fail!, num_of_byte(%d), Err(%d).", num_of_word, num_of_byte, err );
      return 4;
   }

   // Set finish rate of callback function to 100(%)
   accuracy = 100;
   
   LOGE( "[FUNET]BRom_Base::BRom_WriteBuf(): DA download Start ");

   while( i < num_of_byte )
   {
	   if( NULL!=arg.m_p_bootstop && BOOT_STOP==(*(arg.m_p_bootstop)) )
	   {
		   LOGE("[FUNET]BRom_Base::WriteData(): BOOT_STOP!, m_p_bootstop(0x%08X)=%u. ", arg.m_p_bootstop, *(arg.m_p_bootstop));
		  return 1;
	   }

         // copy from buf to write_buf by swap order
         for(j=0; j<10; j+=2)
         {
            write_buf[j] = buf[i+j+1];
            write_buf[j+1] = buf[i+j];

            data = (((unsigned short)write_buf[j])<<8)&0xFF00;
            data |= ((unsigned short)write_buf[j+1])&0x00FF;

            // update checksum
            checksum ^= data;
         }

         // write 
         // This function should be implemented by user.
		 GPS_UART_PutByte_Buffer((uint32 *) write_buf, 10);
		 

         // read bootrom echo to verify
         // This function should be implemented by user.
		 GPS_UART_GetByte_Buffer((uint32 *) read_buf, 10);

		 LOGE("[FUNET]BRom_Base::BRom_WriteBuf(): Progress(%d%),already:%d, to_do:%d ", (int)((float)i/(float)num_of_byte*100.0f),i,num_of_byte);
		 
         if(memcmp(write_buf, read_buf, 10))
         {
			 LOGE( "[FUNET]BRom_Base::BRom_WriteBuf(): write_buf={ %x, %x, %x, %x, %x, %x, %x, %x, %x, %x }. ", write_buf[0], write_buf[1], write_buf[2], write_buf[3], write_buf[4], write_buf[5], write_buf[6], write_buf[7], write_buf[8], write_buf[9]);
			 LOGE( "[FUNET]BRom_Base::BRom_WriteBuf():  read_buf={ %x, %x, %x, %x, %x, %x, %x, %x, %x, %x }. ", read_buf[0], read_buf[1], read_buf[2], read_buf[3], read_buf[4], read_buf[5], read_buf[6], read_buf[7], read_buf[8], read_buf[9]);
			 LOGE("[FUNET]BRom_Base::BRom_WriteBuf(): write_buf != read_buf  ");
            return 8;
         }

         // increase by 10, because we send 5 WORDs each time 
         i += 10;
		 
		//Callback function of DA download
		if (arg.m_cb_download_conn_da!= NULL)
		{
	         if( accuracy < (rate=(unsigned int)(((float)i/num_of_byte)*accuracy)) )
	         {
	            rate = accuracy;
	         }

	         if( 0 < (rate-finish_rate) )
	         {
	            finish_rate = rate;
	            // progress callback 
	            arg.m_cb_download_conn_da((unsigned char)finish_rate, i, num_of_byte, arg.m_cb_download_conn_da_arg);
	         }
		}

   }

   // perform checksum verification
   if((err=BRom_CheckSumCmd(arg, ulBaseAddr, num_of_word, &brom_checksum)))
   {
	  LOGE( "[FUNET]BRom_Base::BRom_WriteBuf(): BRom_CheckSumCmd() fail!, Err(%d). ", err);
      return 9;
   }

   // compare checksum
   if( checksum != brom_checksum )
   {
	   LOGE("[FUNET]BRom_Base::BRom_WriteBuf(): checksum error!, checksum(%x) != brom_checksum(%x) ", checksum, brom_checksum);
      return 10;
   }
   else
   {
	   LOGE("[FUNET]BRom_Base::BRom_WriteBuf(): checksum ok!, checksum(%x) == brom_checksum(%x). ", checksum, brom_checksum);
   }

   return 0;
}


/*******************************************************************************
 * Function name:
 * ---------
 *  BRom_CheckSumCmd
 *
 * Description:
 * ------------
 *  This funciton is used to request checksum which be calculated on target side.
 *  'BL_PRINT' is a sample to output debug log.
 *
 * Input:
 * --------
 *  arg: download arguments structure includes callback function and parameter. 
 *  u1BaseAddr: the start address of DA in internal SRAM.
 *  num_of_byte: DA file size.
 *
 * Output:
 * --------
 *  result: the received checksum. 
 *
 *******************************************************************************/
int BRom_CheckSumCmd(GPS_Download_Arg arg, unsigned int ulBaseAddr, unsigned int num_of_word, unsigned short *result)
{
   int ret;

   if(BRom_CmdSend(arg, BOOT_ROM_CHECKSUM_CMD ))
      return 1;

   if((ret=BRom_OutData(arg, ulBaseAddr)))
   {
	  LOGE("[FUNET]BRom_Base::BRom_CheckSumCmd(): BRom_OutData(%x): send base address fail!, Err(%d). ", ulBaseAddr, ret);
      return 2;
   }
   
   if((ret=BRom_OutData(arg, num_of_word)))
   {
	  LOGE("[FUNET]BRom_Base::BRom_CheckSumCmd(): BRom_OutData(%d): send number of word fail!, Err(%d). ", num_of_word, ret);
      return 3;
   }

   *result = GPS_UART_GetData16();

   return 0;
}


/*******************************************************************************
 * Function name:
 * ---------
 *  BRom_JumpCmd
 *
 * Description:
 * ------------
 *  This funciton is used to jump to DA start address to execute DA in internal SRAM.
 *  The communication flow please refer to the BromDLL document.
 *  'BL_PRINT' is a sample to output debug log.
 *
 * Input:
 * --------
 *  arg: download arguments structure includes callback function and parameter. 
 *  addr: the start address of DA in internal SRAM.
 *
 *******************************************************************************/
int BRom_JumpCmd(GPS_Download_Arg arg, unsigned int addr)
{
   int ret;
   
   if(BRom_CmdSend(arg, BOOT_ROM_JUMP_CMD ))
      return 1;

   if((ret=BRom_OutData(arg, addr)))
   {
	   LOGE("[FUNET]BRom_Base::BRom_JumpCmd(): BRom_OutData(%x): send jump address fail!, Err(%d). ", addr, ret);
      return 2;
   }
   return 0;
}


/*******************************************************************************
 * Function name:
 * ---------
 *  BRom_CmdSend
 *
 * Description:
 * ------------
 *  This funciton is used to send one byte data to UART and check received data.
 *  'BL_PRINT' is a sample to output debug log.
 *
 * Input:
 * --------
 *  arg: download arguments structure includes callback function and parameter. 
 *  cmd: the ont byte data want to be sent.
 *
 *******************************************************************************/

int BRom_CmdSend(GPS_Download_Arg arg, unsigned char cmd)
{
   unsigned char result;
   unsigned char *p_result;

   p_result = &result;
   
   if( NULL!=arg.m_p_bootstop && BOOT_STOP==(*(arg.m_p_bootstop)) ) 
   {
	   LOGE("BRom_StartCmd: BOOT_STOP!, m_p_bootstop(0x%08X)=%u.", arg.m_p_bootstop, *(arg.m_p_bootstop));
	   return (BROM_CMD_START_FAIL+6);
   }

   GPS_UART_PutByte(cmd); 
   
   *p_result = GPS_UART_GetByte();

   if( cmd != *p_result )
   {
	 LOGE("BRom_Base::BRom_CmdSend(0x%02X): bootrom response is incorrect!, cmd(0x%02X) != result(0x%02X).", cmd, cmd, *p_result);
      return 4;
   }
   else
      return 0;
}


/*******************************************************************************
 * Function name:
 * ---------
 *  BRom_OutData
 *
 * Description:
 * ------------
 *  This funciton is used to send 32-bits data to UART and check received data.
 *  'BL_PRINT' is a sample to output debug log.
 *  GPS_UART_PutData32/GPS_UART_GetData32 should be implemented by user.
 *
 * Input:
 * --------
 *  arg: download arguments structure includes callback function and parameter. 
 *  data: 32-bits data want to be sent.
 *
 *******************************************************************************/
int BRom_OutData(GPS_Download_Arg arg, unsigned int data)
{
   unsigned int tmp32;

   if( NULL!=arg.m_p_bootstop && BOOT_STOP==(*(arg.m_p_bootstop)) ) 
   {
	   LOGE("BRom_StartCmd: BOOT_STOP!, m_p_bootstop(0x%08X)=%u.", arg.m_p_bootstop, *(arg.m_p_bootstop));
	   return (BROM_CMD_START_FAIL+6);
   }

   GPS_UART_PutData32(data);
   
	tmp32 = GPS_UART_GetData32();

	if (tmp32 != data)
	 return 4;

   return 0;
}



