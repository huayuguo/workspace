/*******************************************************************************
 *  Copyright Statement:
 *  --------------------
 *  This software is protected by Copyright and the information contained
 *  herein is confidential. The software may not be copied and the information
 *  contained herein may not be used or disclosed except with the written
 *  permission of MediaTek Inc. (C) 2016
 *
 *******************************************************************************/
//Sample code of gps_uart.h
//User must implement this file based on your MCU.

#include "sw_types.h"


#define UART2_base       (0x00000000)

//UART2 MMP address
#define   UART2_RBR		(UART2_base+0x0000)		/* Read only */
#define   UART2_THR		(UART2_base+0x0000)		/* Write only */
#define   UART2_LSR		(UART2_base+0x0014)

//LSR
#define   UART_LSR_DR         	0x0001
#define   UART_LSR_OE         	0x0002
#define   UART_LSR_PE         	0x0004
#define   UART_LSR_THRE     	  0x0020
#define   UART_LSR_FIFOERR    	0x0080

#define UART_ReadReg(_addr)   		(uint16)(*(volatile uint8 *)_addr)
#define UART_WriteReg(_addr,_data)  *(volatile uint8 *)_addr = (uint8)_data


void GPS_UART_IgnoreData(void);

void GPS_UART_Init(void);
uint8 GPS_UART_GetByte(void);
bool GPS_UART_GetByteStatus(uint8 *data);
bool GPS_UART_GetByte_NO_TIMEOUT(uint8 *data);
bool GPS_UART_GetByte_Buffer(uint32* buf, uint32 length);
void GPS_UART_PutByte(uint8 data);
void GPS_UART_PutByte_Buffer(uint32* buf, uint32 length);
uint16 GPS_UART_GetData16(void);
void GPS_UART_PutData16(uint16 data);
uint32 GPS_UART_GetData32(void);
void GPS_UART_PutData32(uint32 data);
void GPS_UART_Flush(void);


