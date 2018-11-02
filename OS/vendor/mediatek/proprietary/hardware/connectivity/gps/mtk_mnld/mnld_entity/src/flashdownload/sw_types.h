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
 * Filename:
 * ---------
 *   sw_types.h
 *
 * Project:
 * --------
 *  Flash Download Library.
 *
 * Description:
 * ------------
 *   Common type and structure definition
 *
 *******************************************************************************/

#ifndef SW_TYPES_H
#define SW_TYPES_H

#define COMPILE_ASSERT(condition) ((void)sizeof(char[1 - 2*!!!(condition)]))

/*
 * general definitions
 */

typedef signed char    int8;
typedef signed short   int16;
typedef signed long    int32;
typedef signed int     intx;
typedef unsigned char  uint8;
typedef unsigned short uint16;
typedef unsigned long  uint32;
#ifdef WIN32
	typedef unsigned long uint64;
#else
	typedef unsigned long long uint64;
#endif
typedef unsigned int   uintx;
typedef unsigned char  U8;
typedef unsigned short U16;
typedef unsigned long  U32;
typedef int32          S32;
typedef unsigned int   Ux;
typedef unsigned char*  P_U8;
typedef unsigned short* P_U16;
typedef unsigned long*  P_U32;
typedef unsigned int*   P_Ux;

typedef int8    int8_t;
typedef int16   int16_t;
//typedef int32   int32_t;
typedef uint8   uint8_t;
typedef uint16  uint16_t;
//typedef uint32  uint32_t;

#ifndef __cplusplus
typedef unsigned char  bool;
#endif

/*
 * Definitions for BOOLEAN
 */
/*
#define FALSE          0
#define TRUE           1
*/
/*
 * Definitions for BOOLEAN
 */
typedef enum BOOL
{
    FALSE = 0,
    TRUE = 1
} BOOL;


/*
 * Definitions for NULL
 */
#ifndef NULL
#define NULL           0
#endif

/*
 * For GFH library
 */
#if defined(WIN32)
#define __WIN32_STDCALL   __stdcall
#else
#define __WIN32_STDCALL
#endif

#endif  /* SW_TYPES_H */
