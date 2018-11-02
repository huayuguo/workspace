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
 *  flashtool.h
 *
 * Project:
 * --------
 *  Flash Download Library.
 *
 * Description:
 * ------------
 *  APIs for FlashTool Library.
 *
 *******************************************************************************/
#ifndef _FLASHTOOL_H_
#define _FLASHTOOL_H_

#include "brom.h"
#include "DOWNLOAD.H"
#include "GPS_DL_api.h"
#include <stdio.h>

#ifdef   __cplusplus
extern "C" {
#endif

//------------------------------------------------------------------------------
// return code                                                                  
//------------------------------------------------------------------------------

#define FT_RET(ret)  (ret&0x000000FF)

#define FT_OK                          0x000000
#define FT_ERROR                       0x000001
#define FT_INVALID_ARGUMENTS           0x000002
#define FT_COM_PORT_OPEN_ERR           0x000003
#define FT_DA_HANDLE_ERROR             0x000004
#define FT_DL_HANDLE_ERROR             0x000005
#define FT_BROM_ERROR                  0x000007
#define FT_COM_PORT_SET_TIMEOUT_ERR    0x000008
#define FT_DA_NO_RESPONSE              0x000009
#define FT_DA_SYNC_INCORRECT           0x00000A
#define FT_DA_VERSION_INCORRECT        0x00000B
#define FT_DA_UNKNOWN_FLASH_DEVICE     0x00000C
#define FT_DA_SET_EXT_CLOCK_FAIL       0x00000D
#define FT_DA_SET_BBCHIP_TYPE_FAIL     0x00000E
#define FT_DA_CHANGE_BAUDRATE_FAIL     0x00000F
#define FT_DA_SET_DOWNLOAD_BLOCK_FAIL  0x000010
#define FT_DA_DOWNLOAD_FAIL            0x000011
#define FT_DA_FORMAT_FAIL              0x000013
#define FT_DA_FINISH_CMD_FAIL          0x000014
#define FT_DA_SOC_CHECK_FAIL           0x000015
#define FT_DA_BBCHIP_DSP_VER_INCORRECT 0x000016
#define FT_SKIP_AUTO_FORMAT_FAT        0x000017
#define FT_DA_HW_ERROR                 0x000018
#define FT_DA_ENABLE_WATCHDOG_FAIL     0x000019
#define FT_CALLBACK_ERROR              0x000020
             

//------------------------------------------------------------------------------
// DA report structure                                                          
//------------------------------------------------------------------------------
typedef  struct DA_REPORT_T{
   unsigned char  expected_da_major_ver;
   unsigned char  expected_da_minor_ver;
   unsigned char  da_major_ver;
   unsigned char  da_minor_ver;
   DEVICE_INFO    flash_device_id;
   unsigned int   flash_size;
   unsigned int   fat_begin_addr;
   unsigned int   fat_length;
   unsigned int   ext_sram_size;
}s_DA_REPORT_T;


//------------------------------------------------------------------------------
// FLASHTOOL_ARG structure                                                      
//------------------------------------------------------------------------------

typedef  struct {
   CALLBACK_DOWNLOAD_PROGRESS_INIT m_cb_download_conn_init; //callback function which is invoked at the beginning of firmware transimitting.
   void  *m_cb_download_conn_init_arg; // parameter of the callback function upper

   CALLBACK_CONN_BROM_WRITE_BUF_INIT m_cb_download_conn_da_init;//callback function which is invoked at the beginning of DA transimitting.
   void *m_cb_download_conn_da_init_arg; // parameter of the callback function upper

   CALLBACK_DOWNLOAD_PROGRESS  m_cb_download_conn; //callback function which is invoked during the firmware transimitting.
   void  *m_cb_download_conn_arg; // parameter of the callback function upper

   CALLBACK_CONN_BROM_WRITE_BUF  m_cb_download_conn_da; //callback function which is invoked during the DA transimitting.
   void *m_cb_download_conn_da_arg; // parameter of the callback function upper
   
}FLASHTOOL_ARG;


/*******************************************************************************
 * Function name:
 * ---------
 *  FlashDownload
 *
 * Description:
 * ------------
 *  This funciton is used to manage the download flow.
 *  The communication flow please refer to the BromDLL document.
 *  'BL_PRINT' is a sample to output debug log.
 *
 * Input:
 * --------
 *  arg: download arguments structure includes callback function and parameter. 
 *  GPS_da: import Download Agent(DA) buffer and size. 
 *  GPS_image_list:  import firmware buffer and size.
 *
 *******************************************************************************/
extern int  FlashDownload(
   GPS_DA* GPS_da,
   GPS_Image_List* GPS_image_list,
   GPS_Download_Arg arg);

void flashtool_download_thread(void *arg);
int flashtool_file_isvalid(void);


#ifdef   __cplusplus
}
#endif

#endif



