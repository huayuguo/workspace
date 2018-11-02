#ifndef __MT3333_CONTROLLER_H__
#define __MT3333_CONTROLLER_H__


#ifdef __cplusplus
extern "C" {
#endif
#include "mtk_gps.h"

#define  MT3333_CONTROLLER_TTY_NAME       "/dev/ttyMT1"


typedef enum {
	MAIN_MT3333_CONTROLLER_EVENT_GPSREBOOT,
    MAIN_MT3333_CONTROLLER_EVENT_GPSENABLE,
	MAIN_MT3333_CONTROLLER_EVENT_GPSSTART,
	MAIN_MT3333_CONTROLLER_EVENT_GPSSTOP,
	MAIN_MT3333_CONTROLLER_EVENT_GPSDISABLE,
	MAIN_MT3333_CONTROLLER_EVENT_GPSDELETEAIDING,
	MAIN_MT3333_CONTROLLER_EVENT_REQUES1STNMEA,
	MAIN_MT3333_CONTROLLER_EVENT_REQUESTNTP,
	MAIN_MT3333_CONTROLLER_EVENT_REQUESTNLP,
	MAIN_MT3333_CONTROLLER_EVENT_REQUESTEPO,
	MAIN_MT3333_CONTROLLER_EVENT_REQUESTQEPO,
	MAIN_MT3333_CONTROLLER_EVENT_ENABLEDEBUGLOG,
	MAIN_MT3333_CONTROLLER_EVENT_DISABLEDEBUGLOG,
	MAIN_MT3333_CONTROLLER_EVENT_ONCEPERSECOND,
	MAIN_MT3333_CONTROLLER_EVENT_FACTORYMETA,
	MAIN_MT3333_CONTROLLER_EVENT_ENABLEDEASYMODE,
	MAIN_MT3333_CONTROLLER_EVENT_DISABLEDEASYMODE,
	MAIN_MT3333_CONTROLLER_EVENT_NMEA_ONOFF,
	MAIN_MT3333_CONTROLLER_EVENT_GNSS_SYSTEM,
	MAIN_MT3333_CONTROLLER_EVENT_NMEA_OFF,
} main_mt3333_controller_event;

int mt3333_controller_Utc2GpsTime(unsigned short* pi2Wn, unsigned int* pdfTow, unsigned int* sys_time);

int mt3333_controller_init() ;
int mt3333_controller_delete_aiding_data(int flags);

int mt3333_controller_inject_location(double latitude, double longitude, float accuracy);

int mt3333_controller_inject_time(int64_t time, int64_t timeReference, int uncertainty);
int mt3333_controller_inject_epo(int qepo);

int mt3333_controller_socket_send_cmd(main_mt3333_controller_event cmd);

void* mt3333_thread_control(void *arg);
void* mt3333_thread_data(void *arg);
void* mt3333_thread_data_debug( void*  arg );

int mt3333_controller_set_baudrate_length_parity_stopbits(int fd, unsigned int new_baudrate, int length, char parity_c, int stopbits);
int mt3333_controller_set_gnss_working_satellite_system(MTK_GNSS_CONFIGURATION gnssmode);

int mt3333_controller_check_uart_baudrate(void);
int mt3333_controller_check_if_gpsfunctionok(void);


#ifdef __cplusplus
}
#endif

#endif



