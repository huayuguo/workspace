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
 *  flashtool.cpp
 *
 * Project:
 * --------
 *  Flash Download Library.
 *
 * Description:
 * ------------
 *  Main function of MCU download.
 *
 *******************************************************************************/
#include "flashtool.h"
#include "da_cmd.h"
#include "brom_base.h"
#include "DOWNLOAD.H"
#include "GPS_DL_api.h"
#include <stdio.h>
#include <string.h>
#include "sw_types.h"
#include "brom.h"

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
#include "gps_controller.h"
#include "mnl_common.h"



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


/*******************************************************************************
 * Function name:
 * ---------
 *  SyncWithDA
 *
 * Description:
 * ------------
 *  This funciton is used to recevied the information from target.
 *  The communication flow please refer to the BromDLL document.
 *  'BL_PRINT' is a sample to output debug log.
 *
 * Input:
 * --------
 *  arg: download arguments structure includes callback function and parameter. 
 *
 * Output:
 * --------
 *  p_da_report: information that DA will report. 
 *  p_FlashDeviceKey:  information that DA will report.
 *
 *******************************************************************************/
static int SyncWithDA(GPS_Download_Arg arg, s_DA_REPORT_T *p_da_report, s_FlashDeviceKey *p_FlashDeviceKey)
{
   unsigned char   buf[32];
   s_FlashDeviceKey   flash_id;

   if( NULL == p_da_report )
   {
      return FT_INVALID_ARGUMENTS;
   }

   // initialize first 
   memset(p_da_report, 0, sizeof(s_DA_REPORT_T));
   p_da_report->expected_da_major_ver = DA_MAJOR_VER;
   p_da_report->expected_da_minor_ver = DA_MINOR_VER;
   p_da_report->flash_device_id = DEVICE_UNKNOWN;

   // get SYNC_CHAR 
   LOGE("[FUNET]SyncWithDA(): wait SYNC_CHAR");
   if(ReadData(arg, buf, 1))
   {
	   LOGE( "[FUNET]SyncWithDA(): SYNC_CHAR no response.");
	  return FT_DA_NO_RESPONSE;
   }
   LOGE("[FUNET]SyncWithDA(): SYNC_CHAR Get:%x.", buf[0]);
   
   if( SOC_FAIL == buf[0] )
   {
	  LOGE("[FUNET]SyncWithDA(): SOC_FAIL(%x) received from DA.", buf[0]);
      return FT_DA_SOC_CHECK_FAIL;
   }
   else if( HW_ERROR == buf[0] )
   {
      return FT_DA_HW_ERROR;
   }
   else if( SYNC_CHAR != buf[0] )
   {
	  LOGE("[FUNET]SyncWithDA(): non-SYNC_CHAR(%x) received from DA.", buf[0]);
      return FT_DA_SYNC_INCORRECT;
   }

   // get DA_MAJOR_VER, DA_MINOR_VER    
   LOGE("[FUNET]SyncWithDA(): wait DA_MAJOR_VER, DA_MINOR_VER.", buf[0]);
   if(ReadData(arg, buf, 2))
   {
      return FT_DA_NO_RESPONSE;
   }

   p_da_report->da_major_ver = buf[0];
   p_da_report->da_minor_ver = buf[1];
   if( DA_MAJOR_VER!=p_da_report->da_major_ver || DA_MINOR_VER!=p_da_report->da_minor_ver )
   {
	  LOGE( "[FUNET]SyncWithDA(): DA_v%d.%d was expired, expect DA_v%d.%d .",  p_da_report->da_major_ver, p_da_report->da_minor_ver, DA_MAJOR_VER, DA_MINOR_VER);
	  return FT_DA_VERSION_INCORRECT;
   }

   LOGE( "[FUNET]SyncWithDA(): DA_v%d.%d.",  p_da_report->da_major_ver, p_da_report->da_minor_ver);
   LOGE( "[FUNET]SyncWithDA(): wait DEVICE_INFO.");
   if(ReadData(arg, buf, 1))
   {
      return FT_DA_NO_RESPONSE;
   }
   p_da_report->flash_device_id = (DEVICE_INFO)buf[0];
   if( DEVICE_UNKNOWN == p_da_report->flash_device_id )
   {
      return FT_DA_UNKNOWN_FLASH_DEVICE;
   }

   // get flash size, manufacture id and device code and ext sram size 
   if(ReadData(arg, buf, 16))
   {
      return FT_DA_NO_RESPONSE;
   }
   // get flash size 
   p_da_report->flash_size = ((buf[0]<<24)&0xFF000000)|((buf[1]<<16)&0x00FF0000)|((buf[2]<<8)&0x0000FF00)|((buf[3])&0x000000FF);
   // get flash manufacture id and device code 
   flash_id.m_ManufactureId   = ((buf[4]<<8)&0xFF00)|((buf[5])&0x00FF);
   flash_id.m_DeviceCode      = ((buf[6]<<8)&0xFF00)|((buf[7])&0x00FF);
   flash_id.m_ExtDeviceCode1   = ((buf[8]<<8)&0xFF00)|((buf[9])&0x00FF);
   flash_id.m_ExtDeviceCode2   = ((buf[10]<<8)&0xFF00)|((buf[11])&0x00FF);
   if( NULL != p_FlashDeviceKey )
   {
      *p_FlashDeviceKey = flash_id;
   }
   // get external sram size 
   p_da_report->ext_sram_size = ((buf[12]<<24)&0xFF000000)|((buf[13]<<16)&0x00FF0000)|((buf[14]<<8)&0x0000FF00)|((buf[15])&0x000000FF);

   LOGE("[FUNET]SyncWithDA(): SYNC ok.");
   return FT_OK;
}


/*******************************************************************************
 * Function name:
 * ---------
 *  FlashDownload_Internal
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
 *  p_arg: Callback function pointer.
 *
 *******************************************************************************/
static int FlashDownload_Internal(
	GPS_DA* GPS_da,
	GPS_Image_List* GPS_image_list,
	FLASHTOOL_ARG  *p_arg,
	GPS_Download_Arg arg)
{
	int ret;
	int *p_stopflag = arg.m_p_bootstop;
	s_BOOT_FLASHTOOL_ARG boot_flashtool_arg;
	ROM_HANDLE_T dl_handle,*p_dl_handle;
	s_DA_REPORT_T      da_report;
	s_DA_REPORT_T      *pDA_Report;
	s_FlashDeviceKey   flash_id;
	p_dl_handle = &dl_handle;


	p_dl_handle->m_buf = GPS_image_list->m_image_list[0].m_image;
	p_dl_handle->m_len = GPS_image_list->m_image_list[0].m_size;
	p_dl_handle->m_begin_addr = 0x00000000;
	p_dl_handle->m_end_addr = p_dl_handle->m_begin_addr + p_dl_handle->m_len - 1;
	p_dl_handle->m_packet_length = 1024; 


	// check arguments
	if( NULL==GPS_image_list || NULL==p_arg || NULL==GPS_da) 
	{
    	LOGE("[FUNET]Input parameters error");
	   return FT_INVALID_ARGUMENTS;
	}

	// fill boot flashtool arg 
	boot_flashtool_arg.m_da_start_addr           = 0x00000C00;
	boot_flashtool_arg.m_da_buf                  = GPS_da->m_image;
	boot_flashtool_arg.m_da_len                  = GPS_da->m_size;

	 
	 LOGE("[FUNET]Enter Boot_FlashTool");

	 // boot target to flashtool mode 
	 ret = Boot_FlashTool(&boot_flashtool_arg, arg);


	 // check return value 
	 if( BROM_OK != ret )
	 {
	    // boot up failed!
		LOGE( "[FUNET]FlashDownload_Internal(): BRom_AutoBoot::Boot_FlashTool() fail! , Err(%x).", ret);
	    return (FT_BROM_ERROR|ret);
	 }

	LOGE( "[FUNET]Sync With DA");

	// sync with DA 
	pDA_Report=&da_report;
	if( FT_OK != (ret=SyncWithDA(arg, pDA_Report, &flash_id)) )
	{
	  return ret;
	}

	// Doesn't format 
	pDA_Report->fat_begin_addr = 0;
	pDA_Report->fat_length = 0;
	// change UART speed for high speed(921600bps)
	LOGE( "Change to high speed baudrate(921600) ");

	int status = CMD_ChangeUartSpeed(arg, 1);
	if(status) 
	{
	  LOGE( "CMD_ChangeUartSpeed fail status %d", status);
	  return FT_DA_CHANGE_BAUDRATE_FAIL;
	}
	else
	{
	  LOGE("CMD_ChangeUartSpeed OK");				 
	}

	LOGE("[FUNET]CMD_SetMemBlock.");
	// set memory block
	if(CMD_SetMemBlock(arg, p_dl_handle))
	{
	  return FT_DA_SET_DOWNLOAD_BLOCK_FAIL;
	}
	LOGE("[FUNET]da_cmd.CMD_WriteData.");
	// write to flash by memory block
	if(CMD_WriteData(arg, p_dl_handle, p_arg->m_cb_download_conn_init, p_arg->m_cb_download_conn_init_arg, p_arg->m_cb_download_conn, p_arg->m_cb_download_conn_arg))
	{
	  return FT_DA_DOWNLOAD_FAIL;
	}

	// set ret to FT_OK 
	ret = FT_OK;

	LOGE("[FUNET]FlashDownload_Internal(): Success!");

	// Finish Cmd
	CMD_Finish(arg);

	return ret;
}



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
int  FlashDownload(
	GPS_DA* GPS_da,
	GPS_Image_List* GPS_image_list,
	GPS_Download_Arg arg)
{
   FLASHTOOL_ARG  flashtool_arg,*p_arg;
   p_arg = &flashtool_arg;

   // check arguments
   if( NULL == GPS_image_list || NULL == GPS_da) 
   {
	  LOGE("[FUNET]Input parameters error"); //Show debug log, please use your own logger API to replace 'BL_PRINT' function.
      return FT_INVALID_ARGUMENTS;
   }

   p_arg->m_cb_download_conn_init = arg.m_cb_download_conn_init;
   p_arg->m_cb_download_conn_init_arg = arg.m_cb_download_conn_init_arg;
   p_arg->m_cb_download_conn = arg.m_cb_download_conn;
   p_arg->m_cb_download_conn_arg = arg.m_cb_download_conn_arg;
   p_arg->m_cb_download_conn_da_init = arg.m_cb_download_conn_da_init;
   p_arg->m_cb_download_conn_da_init_arg = arg.m_cb_download_conn_da_init_arg;
   p_arg->m_cb_download_conn_da = arg.m_cb_download_conn_da;
   p_arg->m_cb_download_conn_da_arg = arg.m_cb_download_conn_da_arg;
   
   LOGE("[FUNET]Enter FlashDownload_Internal");
   return FlashDownload_Internal(GPS_da, GPS_image_list, p_arg, arg);
}


//Example code of callback function
int gps_download_progress_callback(unsigned char finished_percentage,
										   unsigned int finished_bytes,
										   unsigned int total_bytes, void *usr_arg)
{
	int percentage[] = {5, 15, 100};
	int stage = (int) usr_arg;
    LOGE( "[FUNET]current update progress: %d, stage:%d ",finished_percentage, stage);

	if (stage >= sizeof(percentage)/sizeof(int) - 1)
	{
		LOGE( "[FUNET]display error, stage:%d ", stage);
		return 1;
	}
	finished_percentage = (percentage[stage + 1] - percentage[stage]) * finished_percentage/100 + percentage[stage];
    LOGE("[FUNET]real update progress: %d ",finished_percentage);
	return 0;
}

#define DA_FILE_NADA "/data/misc/gps/3333_da.bin"
#define FW_FILE_NADA "/data/misc/gps/3333_fw.bin"
#define FW_FILE_NADA_OLD "/data/misc/gps/3333_fw_old.bin"


uint8 *fw_buf=NULL;
int32 fw_size=0;
uint8 *da_buf=NULL;
int32 da_size=0;
int fw_downloading=0;
extern int g_fd_mt3333_data;
extern MNL_CONFIG_T mnld_cfg;

int flashtool_file_isvalid(void){
	int result_da=0;
	int result_fw=0;

	result_da=access(DA_FILE_NADA, F_OK);
	result_fw=access(FW_FILE_NADA, F_OK);

	if(result_da>=0 && result_fw>=0){
		//LOGE("result=ok");
		return 1;
	}
	LOGE("da=%d, fw=%d, result=wrong", result_da,result_fw);
	return 0;
}

int flashtool_read_da_file(void){
	int result=0;
	int fd=0;
	int file_size=0;
	int ret=0;

	fd = open(DA_FILE_NADA, O_RDONLY);
	if(fd < 0){
		LOGE("open %s fail",DA_FILE_NADA);
		ret=-1;
		goto DEINIT;
	}
	
	file_size=lseek(fd, 0, SEEK_END);
	if(file_size < 0){
		LOGE("point file tail error-1");
		ret=-2;
		goto DEINIT;
	}
	da_size=file_size;
	LOGE("file size=%d",da_size);
	da_buf=malloc(da_size);
	if(NULL == da_buf){
		LOGE("malloc error");
		ret=-3;
		goto DEINIT;
	}
	

	result=lseek(fd, 0, SEEK_SET);
	if(result < 0){
		LOGE("point file tail error-2");
		ret=-4;
		goto DEINIT;
	}

	if(da_size != (result=read(fd, da_buf, da_size))){
		LOGE("read file error,len=%d,orilen=%d",result,da_size);
		ret=-5;
		goto DEINIT;
	}

	close(fd);
	return 0;

DEINIT:
	if(fd>=0){
		close(fd);
	}
	
	if(NULL != da_buf){
		free(da_buf);
		da_buf=NULL;
	}
	return ret;
}

int flashtool_read_fw_file(void){
	int result=0;
	int fd=0;
	int file_size=0;
	int ret=0;

	fd = open(FW_FILE_NADA, O_RDONLY);
	if(fd < 0){
		LOGE("open %s fail",FW_FILE_NADA);
		ret=-1;
		goto DEINIT;
	}
	
	file_size=lseek(fd, 0, SEEK_END);
	if(file_size < 0){
		LOGE("point file tail error-1");
		ret=-2;
		goto DEINIT;
		
	}
	fw_size=file_size;
	LOGE("file size=%d",fw_size);
	fw_buf=malloc(fw_size);
	if(NULL == fw_buf){
		LOGE("malloc error");
		ret=-3;
		goto DEINIT;
	}
	

	result=lseek(fd, 0, SEEK_SET);
	if(result < 0){
		LOGE("point file tail error-2");
		ret=-4;
		goto DEINIT;
	}

	if(fw_size != (result=read(fd, fw_buf, fw_size))){
		LOGE("read file error,len=%d,orilen=%d",result,fw_size);
		ret=-5;
		goto DEINIT;
	}

	close(fd);
	return 0;

DEINIT:
	if(fd>=0){
		close(fd);
	}
	
	if(NULL != fw_buf){
		free(fw_buf);
		fw_buf=NULL;
	}
	return ret;
	return 0;
}


/*******************************************************************************
 * Function name:
 * ---------
 *  main
 *
 * Description:
 * ------------
 *  Main funciton. 
 *  Please invoke this funciton to start download process.
 *  User must load DA and firmware file at MCU side and provide buffer pointer to the parameters in the sample
 *  code below.
 *  The communication flow please refer to the BromDLL document.
 *  'BL_PRINT' is a sample to output debug log.
 *
 *******************************************************************************/
void flashtool_download_thread(void *arg1)
{
	int ret;
	GPS_DA gps_da;
	GPS_Image_List gps_image;
	GPS_Download_Arg arg;
	pthread_t pthread_mt3333_controller;
	pthread_t pthread_mt3333_data;
	pthread_t pthread_mt3333_data_debug;
	unsigned int retry_max=3;
	int i;

	
	//mnl_set_pwrctl(1);
	LOGE("everything is begin");
	
	for(i=0;i<1; i++){
		usleep(1000 * 1000);
	}
	

	//User must provide the pointer of download agent file(DA) buffer and image size to the below parameters.
	//gps_da.m_image = ......;
	//gps_da.m_size = ......;
	
	//MT3333/3339 only has one image file, so set image number as 1.
	gps_image.m_num = 1;
	//User must provide the pointer of firmware buffer and image size to the below parameters.
	//gps_image.m_image_list[0].m_image = ......;
	//gps_image.m_image_list[0].m_size = ......;
	if(flashtool_file_isvalid()){
		if(0 == flashtool_read_da_file()){
			if(0 == flashtool_read_fw_file()){
				gps_image.m_image_list[0].m_image = fw_buf;
				gps_image.m_image_list[0].m_size = fw_size;
				gps_da.m_image = da_buf;
				gps_da.m_size = da_size;
			}else{
				goto DEINIT;
			}
		}else{
			goto DEINIT;
		}
		
	}else{
		goto DEINIT;
	}

	LOGE("file is ready");
	fw_downloading=1;
	
	memset(&arg, 0, sizeof(arg));
	//Callback functions which are invoked while downloading image to GPS chip help to show the rate of progress. 
	//Callback functions are implemented by user.
	//If do not want to use callback function, set them as 'NULL'.
	//If callback functions do not need input parameter, set argument as 'NULL'. 
  arg.m_cb_download_conn = gps_download_progress_callback;
	arg.m_cb_download_conn_arg = (void*) 1;
	arg.m_cb_download_conn_da = gps_download_progress_callback;
	arg.m_cb_download_conn_da_arg = (void*) 0;

	//Note: before downloading, user must confirm:
	//1. MCU UART which connects to GPS chip UART has been initialized.
	//2. UART baud rate has been set correctly.
	//3. GPS has been power on correctly.
	//4. GPS 32KHz clock has been input correctly.
	
	do{
		ret = FlashDownload(&gps_da, &gps_image, arg);
		retry_max--;
		mnl_set_pwrctl(0);
		usleep(100 * 1000);
		LOGE("ret=0x%x, retry=%d", ret,retry_max);
		if(ret == FT_OK ){
			break;
		}
	}while(retry_max != 0);
	
	fw_downloading=0;
	if(ret == FT_OK){
		LOGE("rename result=%d",rename(FW_FILE_NADA, FW_FILE_NADA_OLD));
	}

DEINIT:
	if(fw_buf){
		free(fw_buf);
		fw_buf = NULL;
	}	
	if(da_buf){
		free(da_buf);
		da_buf = NULL;
	}
#if 0	
	if(0 != mt3333_controller_set_baudrate_length_parity_stopbits(g_fd_mt3333_data ,mnld_cfg.link_speed, 8, 'N', 1)){
		LOGE("configure uart baudrate failed");
		return;
	}
#endif

	gps_driver_state_init();

#if 1
		//LOGE("uart baudrate:%d", mnld_cfg.init_speed);
		if(0 != mt3333_controller_set_baudrate_length_parity_stopbits(g_fd_mt3333_data ,mnld_cfg.init_speed, 8, 'N', 1)){
			LOGE("configure uart baudrate failed");
			return;
		}
		if(1 != mt3333_controller_check_if_gpsfunctionok()){
		#if 0	
			if(0 != mt3333_controller_set_baudrate_length_parity_stopbits(
				g_fd_mt3333_data ,
				mnld_cfg.init_speed == 115200?921600:115200, 
				8, 'N', 1)){
				LOGE("configure uart baudrate failed");
				return -1;
			}
		#endif	
		}
#endif	

	
	pthread_create(&pthread_mt3333_controller, NULL, mt3333_thread_control, NULL);
	pthread_create(&pthread_mt3333_data, NULL, mt3333_thread_data, NULL);
	pthread_create(&pthread_mt3333_data_debug, NULL, mt3333_thread_data_debug, NULL);

	pthread_exit(NULL);
	return ;
}

