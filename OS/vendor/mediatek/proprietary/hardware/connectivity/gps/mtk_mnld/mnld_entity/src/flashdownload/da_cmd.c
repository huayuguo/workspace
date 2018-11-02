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
 *  da_cmd.cpp
 *
 * Project:
 * --------
 *  Flash Download Library.
 *
 * Description:
 * ------------
 *  DA(Download Agent) handshake command.
 *
 *******************************************************************************/
#include "brom_base.h"
#include "da_cmd.h"
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
#include "mt3333_controller.h"

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


#define UART_BAUDRATE_SYNC_RETRY      50
#define PACKET_RE_TRANSMISSION_TIMES   3
#define DA_FLASH_ERASE_WAITING_TIME      1000

const unsigned int g_BaudrateTable[] = {
   921600,  // 0x01 
   460800,  // 0x02 
   230400,  // 0x03 
   115200,  // 0x04 
   57600,   // 0x05 
   38400,   // 0x06 
   19200,   // 0x07 
   9600,    // 0x08 
   4800,    // 0x09 
   2400,    // 0x0a 
   1200,    // 0x0b 
   300,     // 0x0c
   110,     // 0x0d
   14400    // 0x0e
};
extern int g_fd_mt3333_data;

/*******************************************************************************
 * Function name:
 * ---------
 *  WriteData
 *
 * Description:
 * ------------
 *  This funciton is used to send firmware data to target.
 *  'BL_PRINT' is a sample to output debug log.
 *
 * Input:
 * --------
 *  arg: download arguments structure includes callback function and parameter. 
 *  write_buf: data buffer want to be sent.
 *  write_len: data length.
 *
 *******************************************************************************/
int WriteData(GPS_Download_Arg arg, const void *write_buf, unsigned int write_len) {

   unsigned int BytesOfWriiten = 0;
   unsigned int rest_of_bytes = 0;
   unsigned long wbytes = 0;
   unsigned short RetryCount = 0;

   if( NULL == write_buf || 0 >= write_len ) {
      LOGE("DA_cmd::WriteData(): invalid arguments, write_buf(0x%08X), write_len(%lu).", write_buf, write_len);
      return 1;
   }

   RetryCount = 0;
   BytesOfWriiten = 0;
   while( BytesOfWriiten < write_len ) 
   	{
   
      // check stop flag 
      if( NULL!=arg.m_p_bootstop && BOOT_STOP==*(arg.m_p_bootstop) ) {
         LOGE( "DA_cmd::WriteData(): m_stopflag(0x%08X)=%lu, force to stop!", arg.m_p_bootstop, *(arg.m_p_bootstop));
         return 2;
      }
   
      wbytes = 0;
      rest_of_bytes = write_len-BytesOfWriiten;

	  GPS_UART_PutByte_Buffer((uint32 *) write_buf, rest_of_bytes);

      // update write bytes
         BytesOfWriiten += rest_of_bytes;

   }

   LOGE("DA_cmd::WriteData(): OK. total=(%lu/%lu).", BytesOfWriiten, write_len);
   return 0;
}

/*******************************************************************************
 * Function name:
 * ---------
 *  ReadData
 *
 * Description:
 * ------------
 *  This funciton is used to read ack from target.
 *  'BL_PRINT' is a sample to output debug log.
 *
 * Input:
 * --------
 *  arg: download arguments structure includes callback function and parameter. 
 *  read_buf: data buffer want to be read.
 *  read_len: data length.
 *
 *******************************************************************************/
int ReadData(GPS_Download_Arg arg, void *read_buf, unsigned int read_len)
{
   unsigned int BytesOfRead = 0;
   unsigned int rest_of_bytes = 0;
   unsigned long rbytes = 0;
   unsigned short RetryCount = 0;

   if( NULL == read_buf || 0 >= read_len )
   {
      LOGE("DA_cmd::ReadData(): invalid arguments, read_buf(0x%08X), read_len(%lu).", read_buf, read_len);
      return 1;
   }

   // initialize read buffer
   memset(read_buf, '\0' , read_len);

   RetryCount = 0;
   BytesOfRead = 0;
   while( BytesOfRead < read_len )
   {
      // check stop flag 
      if( NULL!=arg.m_p_bootstop && BOOT_STOP==*(arg.m_p_bootstop) )
      {
         LOGE("DA_cmd::ReadData(): m_stopflag(0x%08X)=%lu, force to stop!", arg.m_p_bootstop, *(arg.m_p_bootstop));
         return 2;
      }
   
      rbytes = 0;
      rest_of_bytes = read_len-BytesOfRead;
	  
	  GPS_UART_GetByte_Buffer((uint32 *) read_buf, rest_of_bytes);

	  BytesOfRead += rest_of_bytes;

   }

   LOGE( "DA_cmd::ReadData(): OK. total=(%lu/%lu).", BytesOfRead, read_len);
   return 0;
}


int CMD_ChangeUartSpeed(GPS_Download_Arg arg, unsigned char max_full_sync_count) 
{

   unsigned char buf[3];
   volatile unsigned char data8;
   int ret;
   int i;
   unsigned char BaudrateId = 0x01; //921600, refer to g_BaudrateTable
   volatile unsigned char full_sync_char;
	unsigned char count;

   // send speed change command 
   buf[0] = DA_SPEED_CMD;
   buf[1] = BaudrateId;
   buf[2] = max_full_sync_count;
   LOGE("send DA_SPEED_CMD(0x%02X) + BaudrateId(0x%02X) + FULL_SYNC_CNT(0x%02X).", buf[0], buf[1], buf[2]);
   if(WriteData( arg, buf, 3))
   {
      return 2;
   }

   // read ack 
   LOGE( "wait for ACK.");
   LOGE( " before: buf 0x%x", buf[0]);
   if(ReadData( arg, buf, 1))
   {
      return 3;
   }
   if( ACK != buf[0] ) {
      LOGE( "non-ACK(0x%02X) return.", buf[0]);
      return 4;
   }
   // wait 2523 side UART baudrate change to 921600bps
   LOGE( "Wait 2523 side baudrate change to BaudrateId(%u)=%lu successfully.", BaudrateId, g_BaudrateTable[BaudrateId-1]);
	
   // sleep awhile for target uart state machine is ready 
   if(0 != mt3333_controller_set_baudrate_length_parity_stopbits(g_fd_mt3333_data ,921600, 8, 'N', 1)){
		LOGE("configure uart baudrate failed");
		return 1;
	}
   usleep(100*1000);

   // sync mechanism: wait for baudrate change is done on both Target and PC sides 
      for(i=0; i<UART_BAUDRATE_SYNC_RETRY; i++) 
      {
         // send sync char 
         buf[0] = SYNC_CHAR;
		 if(WriteData( arg, buf, 1))
         {
			LOGE(" Write SYNC_CHAR failed");
            continue;
         }

         // wait for sync char
         if( !ReadData( arg, buf, 1) ) 
         {
            if( SYNC_CHAR == buf[0] ) 
            {
               // receive correct SYNC_CHAR 
               LOGE( "SYNC(%lu): SYNC_CHAR(0x%02X) received!", g_BaudrateTable[BaudrateId-1], buf[0]);
               break;
            }
            else 
            {
               // receive non-SYNC_CHAR
               LOGE( "SYNC(%lu): non-SYNC_CHAR(0x%02X) received, keep on sync.", g_BaudrateTable[BaudrateId-1], buf[0]);
            }
         }
         else 
         {
            // wait for SYNC_CHAR timeout
            LOGE( "SYNC(%lu): sync timeout, keep on sync.", g_BaudrateTable[BaudrateId-1]);
         }
      }

      // if fail over UART_BAUDRATE_SYNC_RETRY times, return error 
      if( UART_BAUDRATE_SYNC_RETRY <= i ) 
      {
         LOGE( "SYNC(%lu): retry %d times and sync fail!", g_BaudrateTable[BaudrateId-1], i);
         return 6;
      }

      // send pc side purge ok ack 
      //buf[0] = ACK;
      LOGE( "SYNC(%lu): send PC side TX & RX purge ok ACK. ", g_BaudrateTable[BaudrateId-1]);
	  do
	  {
		  buf[0] = ACK;
		  if(WriteData(arg, buf, 1)) 
		  {
			 return 8;
		  }

		  // wait for target side TX & RX purge ok ACK 
		  LOGE( "SYNC(%lu): wait for target side TX & RX purge ok ACK.", g_BaudrateTable[BaudrateId-1]);
		  if( !ReadData(arg, buf, 1) ) 
		  {
			 if( ACK != buf[0] ) 
			 {
				LOGE( "SYNC(%lu): non-ACK(0x%02X) received!", g_BaudrateTable[BaudrateId-1], buf[0]);
				//return 9;
			 }
		  }
		  else 
		  {
			 LOGE("SYNC(%lu): ReadData(): fail, Err(%d). ", g_BaudrateTable[BaudrateId-1], ret);
			 return 10;
		  }
	  }while (ACK != buf[0]);

	  
	  /*if(WriteData(com_driver, arg, buf, 1)) {
         return 8;
      }

      // wait for target side TX & RX purge ok ACK 
      MTRACE(g_hBROM_DEBUG, "DA_cmd::CMD_ChangeUartSpeed(): SYNC(%lu): wait for target side TX & RX purge ok ACK.", g_BaudrateTable[BaudrateId-1]);
      if( 0 == (ret=ReadData(com_driver, arg, buf, 1)) ) 
      {
         if( ACK != buf[0] ) 
         {
            MTRACE_ERR(g_hBROM_DEBUG, "DA_cmd::CMD_ChangeUartSpeed(): SYNC(%lu): non-ACK(0x%02X) received!", g_BaudrateTable[BaudrateId-1], buf[0]);
            return 9;
         }
      }
      else 
      {
         MTRACE_ERR(g_hBROM_DEBUG, "DA_cmd::CMD_ChangeUartSpeed(): SYNC(%lu): ReadData(): fail, Err(%d). ", g_BaudrateTable[BaudrateId-1], ret);
         return 10;
      }*/
      LOGE( "SYNC(%lu): ACK(0x%02X) received, sync ok!", g_BaudrateTable[BaudrateId-1], buf[0]);

      if( 0 < max_full_sync_count ) {
         LOGE( "FULL_SYNC(%lu): full sync all char to observe if baudrate is stable.", g_BaudrateTable[BaudrateId-1]);
         full_sync_char = 0;
         while(1) {

            for(i=0; i<max_full_sync_count; i++) {

               // fill sync char 
               data8 = full_sync_char;

               // send sync char 
               if(WriteData(arg, &data8, 1)) {
                  // write fail 
                  LOGE("FULL_SYNC(%lu)[%u]: can't write KEEP_SYNC_CHAR(0x%02X) to target, full sync fail, baudrate is not stable!!", g_BaudrateTable[BaudrateId-1], i, full_sync_char);
                  return 11;
               }

               // wait for sync char 
               if(!ReadData(arg, buf, 1)) {
                  if( full_sync_char != buf[0] ) {
                     // receive non-SYNC_CHAR 
                     LOGE( "FULL_SYNC(%lu)[%u]: expect KEEP_SYNC_CHAR(0x%02X), but receive 0x%02X, full sync fail, baudrate is not stable!!", g_BaudrateTable[BaudrateId-1], i, full_sync_char, buf[0]);
                     return 12;
                  }
               }
               else {
                  // wait for SYNC_CHAR timeout 
                  LOGE( "FULL_SYNC(%lu)[%u]: wait for KEEP_SYNC_CHAR(0x%02X) timeout, full sync fail, baudrate is not stable!!", g_BaudrateTable[BaudrateId-1], i, full_sync_char);
                  return 13;
               }
			   //MTRACE(g_hBROM_DEBUG, "DA_cmd::CMD_ChangeUartSpeed(): FULL_SYNC(%lu)[%u]: expect KEEP_SYNC_CHAR(0x%02X), receive 0x%02X, That's right!!", g_BaudrateTable[BaudrateId-1], i, full_sync_char, buf[0]);
            }
            
            if( 0xFF > full_sync_char ) {
               //MTRACE(g_hBROM_DEBUG, "DA_cmd::CMD_ChangeUartSpeed(): FULL_SYNC(%lu)[%u]: KEEP_SYNC_CHAR(0x%02X) done.", g_BaudrateTable[BaudrateId-1], i, full_sync_char);
               full_sync_char++;
            }
            else {
               break;
            }
         }
         LOGE("FULL_SYNC(%lu): successful!", g_BaudrateTable[BaudrateId-1]);
      }
      else {
         LOGE( "skip FULL_SYNC(%lu) procedure after baudrate changed.", g_BaudrateTable[BaudrateId-1]);
      }

   LOGE( " OK!");
   return 0;
}


/*******************************************************************************
 * Function name:
 * ---------
 *  CMD_SetMemBlock
 *
 * Description:
 * ------------
 *  This funciton is used to set download memory block.
 *  The communication flow please refer to the BromDLL document.
 *  'BL_PRINT' is a sample to output debug log.
 *
 * Input:
 * --------
 *  arg: download arguments structure includes callback function and parameter. 
 *  dl_handle: firmware packet information.
 *
 *******************************************************************************/
int CMD_SetMemBlock(GPS_Download_Arg arg, ROM_HANDLE_T  *dl_handle)
{
   unsigned char buf[4];
   unsigned int begin_addr;
   unsigned int end_addr;

   // check arguments
   if( NULL==dl_handle )
   {
	  LOGE( "[FUNET]DA_cmd::CMD_SetMemBlock(): invalid arguments! dl_handle(%x).", dl_handle);
      return 1;
   }

   // send mem block command
   buf[0] = DA_MEM_CMD;
   buf[1] = 0x01;
   LOGE("[FUNET]DA_cmd::CMD_SetMemBlock(): send DA_MEM_CMD(%x) + MEM_BLOCK_COUNT(%x).",  buf[0], buf[1]);
   if(WriteData(arg, buf, 2))
   {
      return 2;
   }

   // send MEM begin addr, end addr
	// MOD with 0x08000000(bank1 start address), because our maui_sw remap flash to bank1 on MT6218B series project. 
	begin_addr = dl_handle->m_begin_addr%0x08000000;
	end_addr = dl_handle->m_end_addr%0x08000000;

	// send begin addr, high byte first
	buf[0] = (unsigned char)((begin_addr>>24)&0x000000FF);
	buf[1] = (unsigned char)((begin_addr>>16)&0x000000FF);
	buf[2] = (unsigned char)((begin_addr>> 8)&0x000000FF);
	buf[3] = (unsigned char)((begin_addr)    &0x000000FF);
	LOGE( "[FUNET]DA_cmd::CMD_SetMemBlock(): send MEM_BEGIN_ADDR( %x%x%x%x).", buf[0], buf[1], buf[2], buf[3]);
	if(WriteData(arg, buf, 4))
	{
	 return 3;
	}

	// send end addr, high byte first
	buf[0] = (unsigned char)((end_addr>>24)&0x000000FF);
	buf[1] = (unsigned char)((end_addr>>16)&0x000000FF);
	buf[2] = (unsigned char)((end_addr>> 8)&0x000000FF);
	buf[3] = (unsigned char)((end_addr)    &0x000000FF);
	LOGE( "[FUNET]DA_cmd::CMD_SetMemBlock(): send MEM_END_ADDR(%x%x%x%x).", buf[0], buf[1], buf[2], buf[3]);
	if(WriteData(arg, buf, 4))
	{
	 return 4;
	}

   // read ack 
	LOGE("[FUNET]DA_cmd::CMD_SetMemBlock(): wait for ACK..");
   if(ReadData(arg, buf, 1))
   {
      return 5;
   }

   if( ACK != buf[0] )
   {
	  LOGE("[FUNET]DA_cmd::CMD_SetMemBlock(): non-ACK(%x) return.", buf[0]);
	  return 6;
   }
   LOGE("[FUNET]DA_cmd::CMD_SetMemBlock(): ACK(%x) OK!", buf[0]);
   if(ReadData(arg, buf, 1))
   {
      return 7;
   }
   dl_handle->m_num_of_unchanged_data_blocks = buf[0];
   LOGE( "[FUNET]DA_cmd::CMD_SetMemBlock(): UNCHANED_DATA_BLOCKS=( %x)", dl_handle->m_num_of_unchanged_data_blocks);
   return 0;   
}


/*******************************************************************************
 * Function name:
 * ---------
 *  CMD_Finish
 *
 * Description:
 * ------------
 *  This funciton is used to send finish command to notify target download process is over.
 *  The communication flow please refer to the BromDLL document.
 *  'BL_PRINT' is a sample to output debug log.
 *
 * Input:
 * --------
 *  arg: download arguments structure includes callback function and parameter. 
 *
 *******************************************************************************/
int CMD_Finish(GPS_Download_Arg arg)
{
   unsigned char buf[2];

   // send DA_FINISH_CMD command
   buf[0] = DA_FINISH_CMD;
   GPS_UART_PutByte(buf[0]); 
   LOGE( "[FUNET]DA_cmd::CMD_Finish(): OK!");
   return 0;
}

/*******************************************************************************
 * Function name:
 * ---------
 *  CMD_WriteData
 *
 * Description:
 * ------------
 *  This funciton is used to send firmware data to target.
 *  The communication flow please refer to the BromDLL document.
 *  'BL_PRINT' is a sample to output debug log.
 *
 * Input:
 * --------
 *  arg: download arguments structure includes callback function and parameter. 
 *  dl_handle: firmware packet information.
 *  cb_download_flash_init: callback function which is invoked at the beginning of transimitting.
 *  cb_download_flash_init_arg: parameter of initial callback function.
 *  cb_download_flash: callback function which is invoked during the transimitting.
 *  cb_download_flash_arg: parameter of  callback function upper.
 *
 *******************************************************************************/
int CMD_WriteData(
      GPS_Download_Arg arg,
      ROM_HANDLE_T  *dl_handle,
      CALLBACK_DOWNLOAD_PROGRESS_INIT  cb_download_flash_init,  void *cb_download_flash_init_arg,
      CALLBACK_DOWNLOAD_PROGRESS  cb_download_flash,  void *cb_download_flash_arg)
{
   static const char ErrAckTable[][64]= {
      "TIMEOUT_DATA",
      "CKSUM_ERROR",
      "RX_BUFFER_FULL",
      "TIMEOUT_CKSUM_LSB",
      "TIMEOUT_CKSUM_MSB",
      "ERASE_TIMEOUT",
      "PROGRAM_TIMEOUT",
      "RECOVERY_BUFFER_FULL",
      "UNKNOWN_ERROR"
   };
   unsigned char buf[5];
   unsigned int finish_rate = 0;
   unsigned int total_bytes = 0;
   unsigned int total_sent_bytes = 0;
   unsigned int accuracy;
   int ret;
   unsigned int sent_bytes;
   unsigned int retry_count=0;
   unsigned int j;
   unsigned int rate;
   unsigned short checksum;
   unsigned int frame_bytes;

   // check arguments
   if( NULL==dl_handle )
   {
	  LOGE("[FUNET]DA_cmd::CMD_WriteData(): invalid arguments! dl_handle( %x)..", dl_handle);
      return 1;
   }

   total_bytes = dl_handle->m_len;

   // send write command + packet length
   buf[0] = DA_WRITE_CMD;
   buf[1] = (unsigned char)((dl_handle->m_packet_length>>24) &0x000000FF);
   buf[2] = (unsigned char)((dl_handle->m_packet_length>>16) &0x000000FF);
   buf[3] = (unsigned char)((dl_handle->m_packet_length>>8)  &0x000000FF);
   buf[4] = (unsigned char)((dl_handle->m_packet_length)     &0x000000FF);
   LOGE("[FUNET]DA_cmd::CMD_WriteData(): send DA_WRITE_CMD( %x), PACKET_LENGTH( %x%x%x%x)=%u..", buf[0], buf[1], buf[2], buf[3], buf[4], dl_handle->m_packet_length);
   if(WriteData(arg, buf, 5))
   {
      return 3;
   }

   // initialization callback
   if( NULL != cb_download_flash_init )
   {
	  LOGE( "[FUNET]DA_cmd::CMD_WriteData(): CALLBACK: cb_download_flash_init().");
	  cb_download_flash_init(cb_download_flash_init_arg);
   }

   // Set finish rate of callback function to 100(%)
   accuracy = 100;

   if( 0 != (ret=ReadData(arg, buf, 1)) )
   {
	  LOGE( "[FUNET]DA_cmd::CMD_WriteData(): ReadData(): fail, Err(%d).", ret);
      return 4;
   }

   LOGE( "[FUNET]DA_cmd::CMD_WriteData(): read ack( %x).", buf[0]);
   
   if( ACK != buf[0] )
   {
	  LOGE( "[FUNET]DA_cmd::CMD_WriteData(): %s( %x): fail to save all the unchanged data from flash!", buf[0]>UNKNOWN_ERROR?"UNKNOWN_ACK":ErrAckTable[buf[0]], buf[0]);
	  return 5;
   }

   // wait for 1st sector erase done 
   LOGE( "[FUNET]DA_cmd::CMD_WriteData(): wait for 1st sector erase done.");
   if( 0 != (ret=ReadData(arg, buf, 1)) )
   {
	  LOGE( "[FUNET]DA_cmd::CMD_WriteData(): ReadData(): fail, Err(%d)", ret);
      return 6;
   }

   LOGE("[FUNET]DA_cmd::CMD_WriteData(): read ack( %x).", buf[0]);
   
   if( ACK != buf[0] )
   {
	  LOGE( "[FUNET]DA_cmd::CMD_WriteData(): %s( %x): fail to erase the 1st sector!.", buf[0]>UNKNOWN_ERROR?"UNKNOWN_ACK":ErrAckTable[buf[0]], buf[0]);
	  return 7;
   }

   // send all rom files
   finish_rate = 0;
   total_sent_bytes = 0;
	// send each rom file
	sent_bytes = 0;
	retry_count = 0;
	LOGE( "[FUNET]DA_cmd::CMD_WriteData(): %d bytes, total_sent_bytes=%d/%d..", dl_handle->m_len, total_sent_bytes, total_bytes);
	while( sent_bytes < dl_handle->m_len )
	{

	re_transmission:

	 // reset the frame checksum
	 checksum = 0;

	 // if the last frame is less than PACKET_LENGTH bytes
	 if( dl_handle->m_packet_length > (dl_handle->m_len-sent_bytes) )
	 {
	    frame_bytes = dl_handle->m_len - sent_bytes;
	 }
	 else
	 {
	    // the normal frame
	    frame_bytes = dl_handle->m_packet_length;
	 }

	  GPS_UART_PutByte_Buffer((uint32 *) (dl_handle->m_buf+sent_bytes), frame_bytes);

	 // calculate checksum
	 for(j=0; j<frame_bytes; j++)
	 {
	    // WARNING: MUST make sure it unsigned value to do checksum
	    checksum += dl_handle->m_buf[sent_bytes+j];
	 }

	 // send 2 bytes checksum, high byte first
	 buf[0] = (unsigned char)((checksum>> 8)&0x000000FF);
	 buf[1] = (unsigned char)((checksum)    &0x000000FF);
	 
	 if(WriteData(arg, buf, 2))
	 {
	    goto read_cont_char;
	 }

	read_cont_char:
	 // read CONT_CHAR
	 buf[0] = 0xEE;
	 if( 0 != (ret=ReadData(arg, buf, 1)) )
	 {
		LOGE( "[FUNET]DA_cmd::CMD_WriteData(): ReadData(): fail, Err(%d).",ret);
		return 8;
	 }
	 LOGE("[FUNET]CONT_CHART Received( %x).", buf[0]);

	 switch(buf[0])
	 {
	    case CONT_CHAR:
	       // sent ok!, reset retry_count			   
		   LOGE( "[FUNET]CONT_CHART Ok( %x).", buf[0]);
	       retry_count = 0;
	       break;
	    case ERASE_TIMEOUT:
		   return 9;
	    case PROGRAM_TIMEOUT:
		   return 10;
	    case RECOVERY_BUFFER_FULL:
		   return 11;
	    case RX_BUFFER_FULL:
	    case CKSUM_ERROR:
	    case TIMEOUT_DATA:
	    case TIMEOUT_CKSUM_LSB:
	    case TIMEOUT_CKSUM_MSB:
	    default:
	       // check retry times
	       if( PACKET_RE_TRANSMISSION_TIMES > retry_count )
	       {
	          retry_count++;
	       }
	       else
	       {
	          // fail to re-transmission
	          // send NACK to wakeup DA to stop
	          buf[0] = NACK;
			  if(WriteData(arg, buf, 1))
	          {
				 LOGE( "DA_cmd::CMD_WriteData(): Retry(%u): (%u%%): %lu bytes sent, total_bytes=%lu/%lu.", retry_count, (unsigned short)(((float)finish_rate/accuracy)*100), sent_bytes, total_sent_bytes, total_bytes);
	          }
	          return 12;
	       }

	       // wait for DA clean RX buffer
		   if( 0 != (ret=ReadData(arg, buf, 1)) )
	       {
	          LOGE( "DA_cmd::CMD_WriteData(): ReadData(): fail, Err(%d)", ret);
	          LOGE( "DA_cmd::CMD_WriteData(): Retry(%u): (%u%%): %lu bytes sent, total_bytes=%lu/%lu.", retry_count, (unsigned short)(((float)finish_rate/accuracy)*100), sent_bytes, total_sent_bytes, total_bytes);
			  return 13;
	       }
		   
	       if( ACK != buf[0] )
	       {
	          LOGE("DA_cmd::CMD_WriteData(): Retry(%u): wrong ack(0x%02X) return!", retry_count, buf[0]);
	          LOGE("DA_cmd::CMD_WriteData(): Retry(%u): (%u%%): %lu bytes sent, total_bytes=%lu/%lu.", retry_count, (unsigned short)(((float)finish_rate/accuracy)*100), sent_bytes, total_sent_bytes, total_bytes);
			  return 14;
	       }

	       // send CONT_CHAR to wakeup DA to start recieving again
	       buf[0] = CONT_CHAR;
	       LOGE( "DA_cmd::CMD_WriteData(): Retry(%u): send CONT_CHAR to wakeup DA to start recieving again.", retry_count);
		   if(WriteData(arg, buf, 1))
	       {
	          LOGE("DA_cmd::CMD_WriteData(): Retry(%u): (%u%%): %lu bytes sent, total_bytes=%lu/%lu.", retry_count, (unsigned short)(((float)finish_rate/accuracy)*100), sent_bytes, total_sent_bytes, total_bytes);
	          return 15;
	       }

	       // re-transmission this frame
	       LOGE( "DA_cmd::CMD_WriteData(): Retry(%u): re-transmission this frame, offset(%lu).", retry_count, sent_bytes);
		   goto re_transmission;

	       break;
	 }

	 // update progress state
	 sent_bytes += frame_bytes;
	 total_sent_bytes += frame_bytes;

	 // calculate finish rate
	 if( accuracy < (rate=(unsigned int)(((float)total_sent_bytes/total_bytes)*accuracy)) )
	 {
	    rate = accuracy;
	 }

	 if( 0 < (rate-finish_rate) )
	 {
	    finish_rate = rate;
	    // calling callback 
	    LOGE( "[FUNET]DA_cmd::CMD_WriteData(): (%d%%): %d bytes sent, total_bytes=%d/%d.",(unsigned char)finish_rate, sent_bytes, total_sent_bytes, total_bytes);
	    if( NULL != cb_download_flash )
	    {
		   LOGE( "[FUNET]DA_cmd::CMD_WriteData(): CALLBACK: cb_download_flash().");
	       cb_download_flash((unsigned char)finish_rate, total_sent_bytes, total_bytes, cb_download_flash_arg);
	    }
	 }
	}
	LOGE( "[FUNET]DA_cmd::CMD_WriteData(): (%d%%): %d bytes sent, total_bytes=%d/%d.",(unsigned char)finish_rate, sent_bytes, total_sent_bytes, total_bytes);

   // wait for recovery done ack 
   LOGE( "[FUNET]DA_cmd::CMD_WriteData(): wait for DA to perform unchanged data recovery.");
   if( 0 != (ret=ReadData(arg, buf, 1)) )
   {
	  LOGE("[FUNET]DA_cmd::CMD_WriteData(): ReadData(): fail, Err(%d)", ret);
	  return 16;
   }

   if( ACK != buf[0] )
   {
	  LOGE( "[FUNET]DA_cmd::CMD_WriteData(): %s( %x): fail to recover all the unchanged data to flash!", buf[0]>UNKNOWN_ERROR?"UNKNOWN_ACK":ErrAckTable[buf[0]], buf[0]);
	  return 17;
   }

   // wait for checksum ack 
   LOGE( "[FUNET]DA_cmd::CMD_WriteData(): wait for DA to perform flash checksum.!");
   if( 0 != (ret=ReadData(arg, buf, 1)) )
   {
	  LOGE( "[FUNET]DA_cmd::CMD_WriteData(): ReadData(): fail, Err(%d).!", ret);
	  return 18;
   }

   if( NACK == buf[0] )
   {
	  LOGE("[FUNET]DA_cmd::CMD_WriteData(): NACK( %x) return, flash checksum error!", buf[0]);
	  return 19;
   }
   else if( ACK != buf[0] )
   {
	  LOGE( "[FUNET]DA_cmd::CMD_WriteData(): non-ACK( %x) return.", buf[0]);
      return 20;
   }

   LOGE( "[FUNET]DA_cmd::CMD_WriteData(): ACK( %x): checksum OK!", buf[0]);
   return 0;
}

