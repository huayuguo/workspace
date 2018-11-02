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
 *  da_cmd.h
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
#ifndef  _DA_CMD_H_
#define  _DA_CMD_H_

#include "flashtool.h"

//------------------------------------------------------------------------------
// DA Command Object                                                            
//------------------------------------------------------------------------------

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
int WriteData(GPS_Download_Arg arg, const void *write_buf, unsigned int write_len);

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
int ReadData(GPS_Download_Arg arg, void *read_buf, unsigned int read_len);


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
 int CMD_ChangeUartSpeed(GPS_Download_Arg arg, unsigned char max_full_sync_count) ;
int CMD_SetMemBlock(GPS_Download_Arg arg, ROM_HANDLE_T  *dl_handle);


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
int CMD_Finish(GPS_Download_Arg arg);

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
               CALLBACK_DOWNLOAD_PROGRESS  cb_download_flash,  void *cb_download_flash_arg);

#endif
