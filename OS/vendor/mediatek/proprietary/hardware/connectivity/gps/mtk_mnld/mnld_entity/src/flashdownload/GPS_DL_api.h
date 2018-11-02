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
*  GPS_DL_api.h
*
* Project:
* --------
*  GPS Download Library.
*
*******************************************************************************/
#ifndef _GPS_DL_API_H_
#define _GPS_DL_API_H_


#define MAX_GPS_IMAGE_NUM 16

#define GPS_FW_DOWNLOAD_OK 0
#define GPS_FW_DOWNLOAD_UPLOAD_ERR -1  

typedef int (*CALLBACK_DOWNLOAD_PROGRESS_INIT)(void *usr_arg);
typedef int (*CALLBACK_DOWNLOAD_PROGRESS)(unsigned char finished_percentage,
										   unsigned int finished_bytes,
										   unsigned int total_bytes, void *usr_arg);

typedef int (*CALLBACK_CONN_BROM_WRITE_BUF_INIT)(void *usr_arg);
typedef int (*CALLBACK_CONN_BROM_WRITE_BUF)(unsigned char finished_percentage,
                                           unsigned int sent_bytes,
                                           unsigned int total_bytes, void *usr_arg);

typedef struct{
    const unsigned char     *m_image;  //buffer to store DA image
    unsigned int            m_size;    //DA image size
}GPS_DA;

typedef struct{
    const unsigned char     *m_image; // buffer to store firmware image 
    unsigned int            m_size;   // firmware image size
}GPS_Image;

typedef struct{
    unsigned int    m_num;
    GPS_Image       m_image_list[MAX_GPS_IMAGE_NUM];
}GPS_Image_List;

typedef struct{
   unsigned int      m_packet_length;
   unsigned char     m_num_of_unchanged_data_blocks;
   const unsigned char     *m_buf;
   unsigned int      m_len;   
   unsigned int      m_begin_addr; 
   unsigned int      m_end_addr;
}ROM_HANDLE_T;

typedef struct{
    int                                 m_bEnableLog;                   //can be used to enable or disable debug log
    int *                               m_p_bootstop;                   //can be used to stop download process
    CALLBACK_CONN_BROM_WRITE_BUF_INIT   m_cb_download_conn_da_init;     //callback function which is invoked at the beginning of DA transimitting.
    void *                              m_cb_download_conn_da_init_arg; // parameter of the callback function upper
    CALLBACK_CONN_BROM_WRITE_BUF        m_cb_download_conn_da;          //callback function which is invoked during the DA transimitting.
    void *                              m_cb_download_conn_da_arg;      // parameter of the callback function upper
}GPS_DA_Arg;

typedef struct{
    CALLBACK_DOWNLOAD_PROGRESS_INIT     m_cb_download_conn_init;        //callback function which is invoked at the beginning of firmware transimitting.
    void *                              m_cb_download_conn_init_arg;    // parameter of the callback function upper
    CALLBACK_DOWNLOAD_PROGRESS          m_cb_download_conn;             //callback function which is invoked during the firmware transimitting.
    void *                              m_cb_download_conn_arg;         // parameter of the callback function upper
    CALLBACK_CONN_BROM_WRITE_BUF_INIT   m_cb_download_conn_da_init;     //callback function which is invoked at the beginning of DA transimitting.
    void *                              m_cb_download_conn_da_init_arg; // parameter of the callback function upper
    CALLBACK_CONN_BROM_WRITE_BUF        m_cb_download_conn_da;          //callback function which is invoked during the DA transimitting.
    void *                              m_cb_download_conn_da_arg;
    int                                 m_bEnableLog;                   //can be used to enable or disable debug log
    int *                               m_p_bootstop;                   //can be used to stop download process
}GPS_Download_Arg;



#endif
