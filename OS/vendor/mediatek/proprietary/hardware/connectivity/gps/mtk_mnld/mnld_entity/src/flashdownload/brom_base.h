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
 *  brom_base.h
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
#ifndef   _BROM_BASE_H_
#define   _BROM_BASE_H_

#include "brom.h"
#include "bbchip_id.h"
#include "GPS_DL_api.h"
#include <stdio.h>
   

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
int BRom_StartCmd(GPS_Download_Arg arg);


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
int Boot_FlashTool(const s_BOOT_FLASHTOOL_ARG  *p_arg, GPS_Download_Arg arg);


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
int BRom_WriteBuf(GPS_Download_Arg arg,
                        unsigned int ulBaseAddr,
                        const unsigned char *buf_in, 
                        unsigned int num_of_byte);

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
int BRom_CheckSumCmd(GPS_Download_Arg arg, unsigned int baseaddr,
                        unsigned int num_of_word, unsigned short *result);

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
int BRom_JumpCmd(GPS_Download_Arg arg, unsigned int addr);

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
int BRom_CmdSend(GPS_Download_Arg arg, unsigned char cmd);

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
int BRom_OutData(GPS_Download_Arg arg, unsigned int data);

#endif
//-----------------------------------------------------------------------------
