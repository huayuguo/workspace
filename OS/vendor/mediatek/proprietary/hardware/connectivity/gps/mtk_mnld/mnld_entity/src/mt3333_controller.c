
#ifdef CONFIG_GPS_MT3333

#include <stdio.h>
#include <stdarg.h>
#include <sys/time.h>
#include <string.h>
#include <sys/epoll.h>
#include <errno.h>
#include <pthread.h>
#include <termios.h>
#include <time.h>
#include "mtk_lbs_utility.h"
#include "data_coder.h"
#include "mt3333_controller.h"
#include "mnld.h"
#include "qepo.h"
#include "epo.h"
#include "mnl_common.h"
#include "mtk_gps_sys_fp.h"
#include <netinet/in.h>
#include <arpa/inet.h>
#include "flashtool.h"






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
#define LOGD(...) tag_log(1, "[op01]", __VA_ARGS__);
#define LOGW(...) tag_log(1, "[op01] WARNING: ", __VA_ARGS__);
#define LOGE(...) tag_log(1, "[op01] ERR: ", __VA_ARGS__);
#else
#define LOG_TAG "mt3333_controller"
#include <cutils/sockets.h>
#include <cutils/log.h>     /*logging in logcat*/
#define LOGD(fmt, arg ...) ALOGD("%s: " fmt, __FUNCTION__ , ##arg)
#define LOGW(fmt, arg ...) ALOGW("%s: " fmt, __FUNCTION__ , ##arg)
#define LOGE(fmt, arg ...) ALOGE("%s: " fmt, __FUNCTION__ , ##arg)
#endif



static int g_fd_mt3333_controller;
int g_fd_mt3333_data;
static struct tm g_mt3333_time_st;
static int nmea_1st_after_powerongps=1;
int factory_meta_exist=0;
int is_ygps_delete_data = 0;

extern int gps_epo_type; 
extern int gps_epo_download_days;
extern MTK_GPS_SYS_FUNCTION_PTR_T porting_layer_callback;
extern MNL_CONFIG_T mnld_cfg;
extern MNL_CONFIG_T mnl_config;

/**
 * Utc2GpsTime converts UTC time to GPS time
 *
 * @param u2Yr, u2Mo, u2Day, u2Hr, u2Min, dfSec: UTC time
 * @param pi2Wn GPS week
 * @param pdfTow GPS time

 *
 * @return None
 */
void __mt3333_controller_Utc2GpsTime(unsigned int u2Yr, unsigned int u2Mo, unsigned int u2Day,
                 unsigned int u2Hr, unsigned int u2Min, unsigned int dfSec,
                 unsigned short* pi2Wn, unsigned int* pdfTow)
{
  int iYearsElapsed;     // Years since 1980.
  int iDaysElapsed;      // Days elapsed since Jan 5/Jan 6, 1980.
  int iLeapDays;         // Leap days since Jan 5/Jan 6, 1980.
  int i;
  // Number of days into the year at the start of each month (ignoring leap
  // years).
  unsigned short  doy[12] = {0,31,59,90,120,151,181,212,243,273,304,334};

  iYearsElapsed = u2Yr - 1980;


  i = 0;
  iLeapDays = 0;
  while(i <= iYearsElapsed)
  {
    if((i % 100) == 20)
    {
      if((i % 400) == 20)
      {
        iLeapDays++;
      }
    }
    else if((i % 4) == 0)
    {
      iLeapDays++;
    }
    i++;
  }

/*  iLeapDays = iYearsElapsed / 4 + 1; */
  if((iYearsElapsed % 100) == 20)
  {
    if(((iYearsElapsed % 400) == 20) && (u2Mo <= 2))
    {
      iLeapDays--;
    }
  }
  else if(((iYearsElapsed % 4) == 0) && (u2Mo <= 2))
  {
    iLeapDays--;
  }
  iDaysElapsed = iYearsElapsed * 365 + doy[u2Mo - 1] + u2Day + iLeapDays - 6;

  // Convert time to GPS weeks and seconds
  *pi2Wn = iDaysElapsed / 7;
  *pdfTow = (iDaysElapsed % 7) * 86400
            + u2Hr * 3600 + u2Min * 60 + dfSec;
}

int mt3333_controller_Utc2GpsTime(unsigned short* pi2Wn, unsigned int* pdfTow, unsigned int* sys_time){
	
	struct tm *time_st = NULL;
	long long time_s = 0;

#if 0
	time_s = time/1000;
	time_st = localtime(&time_s);
	g_mt3333_time_st = *time_st;
#else
	time_t         now;
	extern time_t epo_get_now_time();
	//now = time(NULL);
	now = epo_get_now_time();
	gmtime_r(&now, &g_mt3333_time_st);
	time_st = &g_mt3333_time_st;
#endif
	
	LOGD("%d-%d-%d %d:%d:%d\n", 
		time_st->tm_year + 1900, time_st->tm_mon + 1, time_st->tm_mday, 
		time_st->tm_hour, time_st->tm_min, time_st->tm_sec);
	__mt3333_controller_Utc2GpsTime(time_st->tm_year + 1900, time_st->tm_mon + 1, time_st->tm_mday, 
		time_st->tm_hour, time_st->tm_min, time_st->tm_sec,
		pi2Wn,pdfTow);
	*sys_time = now;
	return 0;
}


#if 1

typedef struct __baudrate_mpping{
	unsigned int		ul_baud_rate;
	speed_t			linux_baud_rate;
}BAUD_RATE_SETTING;

static BAUD_RATE_SETTING speeds_mapping[] = {
    {0		,B0		},
    {50		,B50		},
    {75		,B75		},
    {110	,B110		},
    {134	,B134,		},
    {150	,B150		},
    {200	,B200		},
    {300	,B300		},
    {600	,B600		},
    {1200	,B1200		},
    {1800	,B1800		},
    {2400	,B2400		},
    {4800	,B4800		},
    {9600	,B9600		},
    {19200	,B19200		},
    {38400	,B38400		},
    {57600	,B57600		},
    {115200	,B115200	},
    {230400	,B230400	},
    {460800	,B460800	},
    {500000	,B500000	},
    {576000	,B576000	},
    {921600	,B921600	},
    {1000000	,B1000000	}, 
    {1152000	,B1152000	}, 
    {1500000	,B1500000	}, 
    {2000000	,B2000000	}, 
    {2500000	,B2500000	}, 
    {3000000	,B3000000	}, 
    {3500000	,B3500000	}, 
    {4000000	,B4000000	},
};

static speed_t mt3333_controller_get_speed(unsigned int baudrate) 
{
	unsigned int idx;
	for (idx = 0; idx < sizeof(speeds_mapping)/sizeof(speeds_mapping[0]); idx++){
		if (baudrate == (unsigned int)speeds_mapping[idx].ul_baud_rate){
			return speeds_mapping[idx].linux_baud_rate;
		}
	}
	return CBAUDEX;        
}
#if 0
int mt3333_controller_set_baudrate_length_parity_stopbits(int fd, unsigned int new_baudrate, int length, char parity_c, int stopbits)
{
    struct termios uart_cfg_opt;
	speed_t speed;
	char  using_custom_speed = 0;
	
	if(-1==fd)
		return -1;

	/* Get current uart configure option */
	if(-1 == tcgetattr(fd, &uart_cfg_opt))
		return -1;

	tcflush(fd, TCIOFLUSH);

	/* Baud rate setting section */
	speed = mt3333_controller_get_speed(new_baudrate);
	if(CBAUDEX != speed){
		/*set standard buadrate setting*/
		cfsetospeed(&uart_cfg_opt, speed);
		cfsetispeed(&uart_cfg_opt, speed);
		LOGE("Standard baud=%d",new_baudrate);
	}else{
		LOGE("Custom baud=%d",new_baudrate);
		using_custom_speed = 1;
	}
	/* Apply baudrate settings */
	if(-1==tcsetattr(fd, TCSANOW, &uart_cfg_opt))
		return -1;
    
	/* Set time out */
	uart_cfg_opt.c_cc[VTIME] = 1;
	uart_cfg_opt.c_cc[VMIN] = 0;

	/*if((ioctl(fd,TIOCGSERIAL,&ss)) < 0)
		return -1;

	if(using_custom_speed){
		ss.flags |= ASYNC_SPD_CUST;  
        	ss.custom_divisor = 1<<31|new_baudrate;
        }else
        	ss.flags &= ~ASYNC_SPD_CUST;    

	if((ioctl(fd, TIOCSSERIAL, &ss)) < 0)
		return -1;//*/

	/* Data length setting section */
	uart_cfg_opt.c_cflag &= ~CSIZE;
	switch(length)
	{
	default:
	case 8:
		uart_cfg_opt.c_cflag |= CS8;
		break;
	case 5:
		uart_cfg_opt.c_cflag |= CS5;
		break;
	case 6:
		uart_cfg_opt.c_cflag |= CS6;
		break;
	case 7:
		uart_cfg_opt.c_cflag |= CS7;
		break;
	}

	/* Parity setting section */
	uart_cfg_opt.c_cflag &= ~(PARENB|PARODD);
	switch(parity_c)
	{
	default:
	case 'N':
	case 'n':
		uart_cfg_opt.c_iflag &= ~INPCK;
		break;
	case 'O':
	case 'o':
		uart_cfg_opt.c_cflag |= (PARENB|PARODD);
		uart_cfg_opt.c_iflag |= INPCK;
		break;
	case 'E':
	case 'e':
		uart_cfg_opt.c_cflag |= PARENB;
		uart_cfg_opt.c_iflag |= INPCK;
		break;
	}

	/* Stop bits setting section */
	if(2==stopbits)
		uart_cfg_opt.c_cflag |= CSTOPB;
	else
		uart_cfg_opt.c_cflag &= ~CSTOPB;

	/* Using raw data mode */
	uart_cfg_opt.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
	uart_cfg_opt.c_iflag &= ~(INLCR | IGNCR | ICRNL | IXON | IXOFF);
	uart_cfg_opt.c_oflag &=~(INLCR|IGNCR|ICRNL);
	uart_cfg_opt.c_oflag &=~(ONLCR|OCRNL);

	/* Apply new settings */
	if(-1==tcsetattr(fd, TCSANOW, &uart_cfg_opt))
		return -1;

	tcflush(fd,TCIOFLUSH);

	/* All setting applied successful */
	LOGE("setting apply done\r\n");
	return 0;
}
#else
int mt3333_controller_set_baudrate_length_parity_stopbits(int fd, unsigned int new_baudrate, int length, char parity_c, int stopbits)
{

    struct termios termOptions;
    // fcntl(fd, F_SETFL, 0);

    // Get the current options:
    tcgetattr(fd, &termOptions);

    // Set 8bit data, No parity, stop 1 bit (8N1):
    termOptions.c_cflag &= ~PARENB;
    termOptions.c_cflag &= ~CSTOPB;
    termOptions.c_cflag &= ~CSIZE;
    termOptions.c_cflag |= CS8 | CLOCAL | CREAD;

    
    // termOptions.c_lflag

    // Raw mode
    termOptions.c_iflag &= ~(INLCR | ICRNL | IXON | IXOFF | IXANY);
    termOptions.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);  /*raw input*/
    termOptions.c_oflag &= ~OPOST;  /*raw output*/

    tcflush(fd, TCIFLUSH);  // clear input buffer
    termOptions.c_cc[VTIME] = 10;  /* inter-character timer unused, wait 1s, if no data, return */
    termOptions.c_cc[VMIN] = 0;  /* blocking read until 0 character arrives */

     // Set baudrate to 38400 bps
    cfsetispeed(&termOptions, mt3333_controller_get_speed(new_baudrate));  /*set baudrate to 115200, which is 3332 default bd*/
    cfsetospeed(&termOptions, mt3333_controller_get_speed(new_baudrate));

    tcsetattr(fd, TCSANOW, &termOptions);
	LOGE("Custom baud=%d",new_baudrate);
    return 0;
}

#endif


#endif
#if 1
int mt3333_thread_pmtk_input_fd[5]={C_INVALID_SOCKET,C_INVALID_SOCKET,C_INVALID_SOCKET,C_INVALID_SOCKET,C_INVALID_SOCKET};

void * mt3333_thread_pmtk_input_func(void * arg)
{
    int exit = 0;
    ssize_t bytes = 0, j = 0, i;
    char buf[MNLD_INTERNAL_BUFF_SIZE]={0};
    char cmd[MNLD_INTERNAL_BUFF_SIZE]={0};
	int fd = (int)arg;
    
	LOGD("thread pmtk create: %.8X,fd = %d", (unsigned int)pthread_self(),fd);

    while(!exit)
    {
		bytes = read(fd, buf, MNLD_INTERNAL_BUFF_SIZE);

        if (bytes > 0) {
            LOGD("len=%d,%s", bytes, buf);

			if(bytes != write(g_fd_mt3333_data , buf, bytes)){
				LOGD("3333 not receive data");
				break;
			}

        } else {
            if (errno == EINTR) {
                LOGD("thread pmtk input exit");
                break;
            } else if (bytes == 0) { /*client close*/    
                LOGD("client %d close", fd);
				close(fd);
				for(i=0;i<5; i++){
					if(mt3333_thread_pmtk_input_fd[i] == fd){
						mt3333_thread_pmtk_input_fd[i] = C_INVALID_SOCKET;
						break;
					}
				}
                break;
            } else {
                LOGD("pmtk sleep 200ms. bytes=%d", (int)bytes);
                usleep(200000);  // sleep 200 ms
            }
        }
    }

	pthread_exit(NULL);
	return NULL;
}

void* mt3333_thread_data_debug( void*  arg ) 
{
    int server_fd = C_INVALID_SOCKET, conn_fd = C_INVALID_SOCKET, on;
    struct sockaddr_in server_addr;
    struct sockaddr_in client_addr;
    socklen_t size;
    char buf[128];
	int i=0;

	
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == -1) 
    {
        LOGE("socket error = %d (%s)", errno, strerror(errno));
        pthread_exit(NULL);
        return NULL;
    }

    /* Enable address reuse */
    on = 1;
    if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on))) 
    {
        close(server_fd);
        LOGE("setsockopt error = %d (%s)", errno, strerror(errno));
        pthread_exit(NULL);
        return NULL;
    }

    server_addr.sin_family = AF_INET;   /*host byte order*/
    server_addr.sin_port = htons(7000); /*short, network byte order*/
    server_addr.sin_addr.s_addr = INADDR_ANY; /*automatically fill with my IP*/
    memset(server_addr.sin_zero, 0x00, sizeof(server_addr.sin_zero));

    if (bind(server_fd, (struct sockaddr*)&server_addr, sizeof(server_addr)) == -1) 
    {
        close(server_fd);
        LOGE("bind error = %d (%s)", errno, strerror(errno));
        pthread_exit(NULL);
        return NULL;
    }

    if (listen(server_fd, 5) == -1) 
    {
        close(server_fd);
        LOGE("listen error = %d (%s)", errno, strerror(errno));
        pthread_exit(NULL);
        return NULL;
    }

    LOGD("listening debug port: %d", 7000);

    while (1) 
    {
        size = sizeof(client_addr);
        conn_fd = accept(server_fd, (struct sockaddr*)&client_addr, &size);
        if (conn_fd <= 0) 
        {
            LOGE("accept error: %d (%s)", errno, strerror(errno));
            continue;
        } 
        else 
        {
            LOGD("accept connection [%d] %s:%d", conn_fd, 
                inet_ntoa(client_addr.sin_addr), ntohs(client_addr.sin_port));     
            /*loop until being interrupted or client close*/ 
			pthread_t pthread_mt3333_controller_accept;
			for(i=0;i<5; i++){
				if(mt3333_thread_pmtk_input_fd[i] == C_INVALID_SOCKET){
					mt3333_thread_pmtk_input_fd[i] = conn_fd;
					break;
				}
			}
			pthread_create(&pthread_mt3333_controller_accept, NULL, mt3333_thread_pmtk_input_func, (void*)conn_fd);
            
        }        
    }    
    pthread_exit(NULL);
    return NULL;
}

#endif

unsigned char gps_nmea_cal_checksum(unsigned char *ptr, unsigned char len){
	unsigned char i=0;
	unsigned char checksum=0;

	checksum=ptr[1];
	for(i=2; i<len; i++){
		checksum ^= ptr[i];
	}
	return checksum;
}

#if 1

#define MT3333_CONTROLLER_EPO_RECORD_SIZE (72)
#define MT3333_CONTROLLER_EPO_SV_NUMBER (32+24)
#define MT3333_CONTROLLER_EPO_SEG_BUF_SIZE ((MT3333_CONTROLLER_EPO_RECORD_SIZE * MT3333_CONTROLLER_EPO_SV_NUMBER) >> 2)

int mt3333_controller_u4EPOWORD[MT3333_CONTROLLER_EPO_SEG_BUF_SIZE];



int mtk_gps_epo_UtcToGpsHour (struct tm* time_st)
{
	int iYearsElapsed; // Years since 1980
	int iDaysElapsed; // Days elapsed since Jan 6, 1980
	int iLeapDays; // Leap days since Jan 6, 1980
	int i;
	int iYr =time_st->tm_year + 1900;
	int iMo =time_st->tm_mon + 1;
	int iDay =time_st->tm_mday;
	int iHr = time_st->tm_hour;
	// Number of days into the year at the start of each month (ignoring leap years)
	const int doy[12] = {0,31,59,90,120,151,181,212,243,273,304,334};

    
    
	iYearsElapsed = iYr - 1980;
	i = 0;
	iLeapDays = 0;
	while (i <= iYearsElapsed)
	{
		if ((i % 100) == 20)
		{
			if ((i % 400) == 20)
			{
				iLeapDays++;
			}
		}
		else if ((i % 4) == 0)
		{
			iLeapDays++;
		}
		i++;
	}
	if ((iYearsElapsed % 100) == 20)
	{
		if (((iYearsElapsed % 400) == 20) && (iMo <= 2))
		{
			iLeapDays--;
		}
	}
	else if (((iYearsElapsed % 4) == 0) && (iMo <= 2))
	{
		iLeapDays--;
	}
	iDaysElapsed = iYearsElapsed * 365 + doy[iMo - 1] + iDay + iLeapDays - 6;
	
    
	return (iDaysElapsed * 24 + iHr);
}

int gps_data_convert(int dat){
	char *ptr = (char *)(&dat);
	
	return (ptr[0]<<24) + (ptr[1]<<16) + (ptr[2]<<8) + (ptr[3]<<0);
}

void gps_timer_epo_send_assistance_data(void)
{
    int i=0, SatID;
    int *pBuf;
    unsigned char nmea_cmd[300], temp[50];
	int err=0;
    int count=0;

	if(g_fd_mt3333_data < 0){
		return;
	}
	
	

    if (gps_epo_type == 0)// G+G
    {
        // read binary EPO data and sent it to MT3333
        for (i =0; i < (MT3333_CONTROLLER_EPO_SV_NUMBER); i++)
        {
            pBuf = mt3333_controller_u4EPOWORD + (i * 18);
            // assume host system is little-endian
            SatID = (pBuf[0] & 0xFF000000) >> 24;
            if (SatID == 0){
                continue;
            }
            
            // assume host system is little-endian
            sprintf(nmea_cmd,
            "$PMTK721,%X,%X,%X,%X,%X,%X,%X,%X,%X,%X,%X,%X,%X,%X,%X,%X,%X,%X,%X",
            SatID,
#if 0            
            gps_data_convert(pBuf[0]), gps_data_convert(pBuf[1]), gps_data_convert(pBuf[2]), gps_data_convert(pBuf[3]), 
            gps_data_convert(pBuf[4]), gps_data_convert(pBuf[5]), gps_data_convert(pBuf[6]), gps_data_convert(pBuf[7]), 
            gps_data_convert(pBuf[8]), gps_data_convert(pBuf[9]), gps_data_convert(pBuf[10]),gps_data_convert(pBuf[11]), 
            gps_data_convert(pBuf[12]), gps_data_convert(pBuf[13]), gps_data_convert(pBuf[14]), gps_data_convert(pBuf[15]), 
            gps_data_convert(pBuf[16]), gps_data_convert(pBuf[17]));
#else 
			(pBuf[0]), (pBuf[1]), (pBuf[2]), (pBuf[3]), 
            (pBuf[4]), (pBuf[5]), (pBuf[6]), (pBuf[7]), 
            (pBuf[8]), (pBuf[9]), (pBuf[10]),(pBuf[11]), 
            (pBuf[12]), (pBuf[13]), (pBuf[14]), (pBuf[15]), 
            (pBuf[16]), (pBuf[17]));

#endif
			sprintf(temp ,"*%02X\x0d\x0a", gps_nmea_cal_checksum(nmea_cmd, strlen(nmea_cmd)));
			strcat(nmea_cmd , temp);
			count = write(g_fd_mt3333_data , nmea_cmd, strlen(nmea_cmd)+1);
			if((strlen(nmea_cmd)+1) != count){
				err=-1;
			}

			LOGD("err=%d,cmd=%s" ,err, nmea_cmd);

            //UART DMA buffer is 2K, so GPS can send max 2K bytes once
            //one satellite EPO sentence contain max 174 bytes, every 12 satellites contain max 2088 bytes,
            //send 2088 bytes need cost 180ms(baudrate 115200) ,so when send 12 satellites EPO data, wait 180ms
            if (( (i + 1) % 12 ) == 0){
                usleep(360*1000);
            }
        }
    }
    
}



int mt3333_controller_inject_epo(int qepo){

    int fd = 0;
    int addLock, res_epo, res_epo_hal;
    unsigned int u4GpsSecs_start;    // GPS seconds
    unsigned int u4GpsSecs_expire;
    //char *epo_file = EPO_FILE;
    char *epo_file_hal = qepo?QEPO_UPDATE_HAL:EPO_UPDATE_HAL;
    char epofile[32] = {0};
    time_t uSecond_start;      // UTC seconds
    time_t uSecond_expire;
    int ret = 0;
    pthread_mutex_t mutx = PTHREAD_MUTEX_INITIALIZER;
	int epofile_begin_utc_hour=0;

#if 1
		time_t		   now;
		extern time_t epo_get_now_time();
		//now = time(NULL);
		now = epo_get_now_time();
		gmtime_r(&now, &g_mt3333_time_st);
#endif
	int current_gps_hour = mtk_gps_epo_UtcToGpsHour(&g_mt3333_time_st);
	
	
	ret = pthread_mutex_lock(&mutx);
	if (access(epo_file_hal, 0) == -1) {
		LOGE("EPOHAL.DAT is not exist\n");
		ret = pthread_mutex_unlock(&mutx);
		return -1;
	}

	fd = open(epo_file_hal, O_RDONLY);
	if (fd < 0) {
        LOGE("Open EPO fail, return\n");
        ret = pthread_mutex_unlock(&mutx);
        return -1;
    }

	// Add file lock
    if (mtk_gps_sys_read_lock(fd, 0, SEEK_SET, 0) < 0) {
	    LOGE("Add read lock failed, return\n");
	    close(fd);
	    ret = pthread_mutex_unlock(&mutx);
	    return -1;
   }

	if(qepo == 0){
		// EPO start time
	    if (sizeof(epofile_begin_utc_hour) != read(fd, &epofile_begin_utc_hour, sizeof(epofile_begin_utc_hour))) {
	        LOGE("Get EPO file start time error, return\n");
	        close(fd);
	        ret = pthread_mutex_unlock(&mutx);
	        return -1;
	    }else{
			epofile_begin_utc_hour = epofile_begin_utc_hour & 0x00FFFFFF;
		}

		

		LOGD("current_gps_hour - epofile_begin_utc_hour =%d\n" , current_gps_hour - epofile_begin_utc_hour);
		if (current_gps_hour - epofile_begin_utc_hour > gps_epo_download_days*24){
			close(fd);
	        ret = pthread_mutex_unlock(&mutx);
			LOGE("EPOHAL.DAT is expired, return\n");
			return -1;
		}

		if (lseek(fd, ((current_gps_hour - epofile_begin_utc_hour) / 6)*(MT3333_CONTROLLER_EPO_RECORD_SIZE)*(MT3333_CONTROLLER_EPO_SV_NUMBER), SEEK_SET) == -1){
			close(fd);
	        ret = pthread_mutex_unlock(&mutx);
			LOGE("lseek error: %s\n", strerror(errno));
			return -1;
		}
	}
	
        
	
    if ((MT3333_CONTROLLER_EPO_SEG_BUF_SIZE*sizeof(int)) != read(fd, mt3333_controller_u4EPOWORD, MT3333_CONTROLLER_EPO_SEG_BUF_SIZE*sizeof(int))) {
        close(fd);
        ret = pthread_mutex_unlock(&mutx);
		LOGE("lseek error: %s\n", strerror(errno));
		return -1;
    }

	close(fd);
	ret = pthread_mutex_unlock(&mutx);

	gps_timer_epo_send_assistance_data();
	return 0;
}

#endif


int mt3333_controller_delete_aiding_data(int flags)
{
    
	int err=0;
    int count=0;
	int count1=10*5;

	const char hot_cmd[]="$PMTK101";
	const char warm_cmd[]="$PMTK102";
	const char cold_cmd[]="$PMTK103";
	const char full_cmd[]="$PMTK104";
	const char agps_cmd[]="$PMTK106";

	char nmea_cmd[256]={0};
	char temp[50]={0};

    LOGD("%s:0x%X\n", __FUNCTION__, flags);

	if(g_fd_mt3333_data < 0){
		return -1;
	}
	if(is_ygps_delete_data == 0){
		return -1;
	}
	

    if ((flags&(GPS_DELETE_EPHEMERIS|GPS_DELETE_POSITION|GPS_DELETE_RTI)) == (GPS_DELETE_EPHEMERIS|GPS_DELETE_POSITION|GPS_DELETE_RTI)) {
        LOGD("Send MNL_CMD_RESTART_FULL in HAL\n");
		sprintf(nmea_cmd ,"%s",full_cmd);
    } else if ((flags&(GPS_DELETE_EPHEMERIS|GPS_DELETE_POSITION)) == (GPS_DELETE_EPHEMERIS|GPS_DELETE_POSITION)) {
        LOGD("Send MNL_CMD_RESTART_COLD in HAL\n");
		sprintf(nmea_cmd ,"%s",cold_cmd);
    } else if ((flags&(GPS_DELETE_EPHEMERIS)) == (GPS_DELETE_EPHEMERIS)) {
        LOGD("Send MNL_CMD_RESTART_WARM in HAL\n");
		sprintf(nmea_cmd ,"%s",warm_cmd);
    } else if ((flags&GPS_DELETE_RTI) == GPS_DELETE_RTI) {
        LOGD("Send MNL_CMD_RESTART_HOT in HAL\n");
		sprintf(nmea_cmd ,"%s",hot_cmd);
    } else{
        LOGD("Send MNL_CMD_RESTART_HOT in HAL\n");
		sprintf(nmea_cmd ,"%s",hot_cmd);
    }

	sprintf(temp ,"*%02X\x0d\x0a", gps_nmea_cal_checksum(nmea_cmd, strlen(nmea_cmd)));
	strcat(nmea_cmd , temp);
	count = write(g_fd_mt3333_data , nmea_cmd, strlen(nmea_cmd)+1);
	if((strlen(nmea_cmd)+1) != count){
		err=-1;
	}
	LOGD("err=%d,cmd=%s\n" ,err, nmea_cmd);
	
    return 0;

}

int mt3333_controller_inject_location(double latitude, double longitude, float accuracy)
{

	const char time_aiding_cmd[]="$PMTK741";
	
	char nmea_cmd[256]={0};
	char temp[50]={0};

	int err=0;
    int count=0;
	struct tm *time_st = NULL;
	long long time_s = 0;

	if(g_fd_mt3333_data < 0){
		return -1;
	}

	sprintf(nmea_cmd ,"%s",time_aiding_cmd);

	LOGD("inject location lati= %f, longi = %f, accuracy =%f\n", latitude, longitude, accuracy);
	sprintf(temp ,",%.6f,%.6f,0", latitude , longitude);
	strcat(nmea_cmd , temp);
	
	
#if 0
		time_st = &g_mt3333_time_st;
#else
		time_t		   now;
		extern time_t epo_get_now_time();
		//now = time(NULL);
		now = epo_get_now_time();
		gmtime_r(&now, &g_mt3333_time_st);
		time_st = &g_mt3333_time_st;
#endif

	
	LOGD("%d-%d-%d %d:%d:%d\n", 
		time_st->tm_year + 1900, time_st->tm_mon + 1, time_st->tm_mday, 
		time_st->tm_hour, time_st->tm_min, time_st->tm_sec);
	sprintf(temp ,",%4d,%02d,%02d,%02d,%02d,%02d", 
		time_st->tm_year + 1900, time_st->tm_mon + 1, time_st->tm_mday, 
		time_st->tm_hour, time_st->tm_min, time_st->tm_sec);
	strcat(nmea_cmd , temp);
	
	sprintf(temp ,"*%02X\x0d\x0a", gps_nmea_cal_checksum(nmea_cmd, strlen(nmea_cmd)));
	strcat(nmea_cmd , temp);
	count = write(g_fd_mt3333_data , nmea_cmd, strlen(nmea_cmd)+1);
	if((strlen(nmea_cmd)+1) != count){
		err=-1;
	}

	LOGD("err=%d,cmd=%s" ,err, nmea_cmd);
	
    return 0;
}

int mt3333_controller_inject_time(int64_t time, int64_t timeReference, int uncertainty)
{
    
	
	const char time_aiding_cmd[]="$PMTK740";
	
	char nmea_cmd[256]={0};
	char temp[50]={0};

	int err=0;
    int count=0;
	struct tm *time_st = NULL;
	long long time_s = 0;
	time_t         now;
	extern time_t epo_get_now_time();
	
	if(g_fd_mt3333_data < 0){
		return -1;
	}


	sprintf(nmea_cmd ,"%s",time_aiding_cmd);

	if(time==0 && timeReference == 0){
		LOGD("local time\n");
		//now = time(NULL);
		now = epo_get_now_time();
		gmtime_r(&now, &g_mt3333_time_st);
		time_st = &g_mt3333_time_st;
	}else{// network time is wrong ,diffrect about 20s
		return 0;
		now = epo_get_now_time();
		LOGD("network time %d, ref=%d,local=%d\n",time,timeReference,now);
		time_s = time/1000;
		//time_st = localtime(&time_s);
		//g_mt3333_time_st = *time_st;
		gmtime_r(&time_s, &g_mt3333_time_st);
		time_st = &g_mt3333_time_st;
	}

	
	LOGD("%d-%d-%d %d:%d:%d\n", 
		time_st->tm_year + 1900, time_st->tm_mon + 1, time_st->tm_mday, 
		time_st->tm_hour, time_st->tm_min, time_st->tm_sec);
	
	sprintf(temp ,",%4d,%02d,%02d,%02d,%02d,%02d", 
		time_st->tm_year + 1900, time_st->tm_mon + 1, time_st->tm_mday, 
		time_st->tm_hour, time_st->tm_min, time_st->tm_sec);
	strcat(nmea_cmd , temp);
	
	sprintf(temp ,"*%02X\x0d\x0a", gps_nmea_cal_checksum(nmea_cmd, strlen(nmea_cmd)));
	strcat(nmea_cmd , temp);
	count = write(g_fd_mt3333_data , nmea_cmd, strlen(nmea_cmd)+1);
	if((strlen(nmea_cmd)+1) != count){
		err=-1;
	}
	LOGD("err=%d,cmd=%s" ,err, nmea_cmd);

    return 0;
}

int mt3333_controller_enable_debuglog(int enable)
{
    
	
	const char time_aiding_cmd[]="$PMTK299";
	
	char nmea_cmd[256]={0};
	char temp[50]={0};

	int err=0;
    int count=0;
	struct tm *time_st = NULL;
	long long time_s = 0;

	
	if(g_fd_mt3333_data < 0){
		return -1;
	}

	sprintf(nmea_cmd ,"%s",time_aiding_cmd);
	
	
	
	sprintf(temp ,",%d", enable);
	strcat(nmea_cmd , temp);
	
	sprintf(temp ,"*%02X\x0d\x0a", gps_nmea_cal_checksum(nmea_cmd, strlen(nmea_cmd)));
	strcat(nmea_cmd , temp);
	count = write(g_fd_mt3333_data , nmea_cmd, strlen(nmea_cmd)+1);
	if((strlen(nmea_cmd)+1) != count){
		err=-1;
	}
	LOGD("err=%d,cmd=%s" ,err, nmea_cmd);

    return 0;
}

int mt3333_controller_enable_easymode(int enable)
{
    
	
	const char time_aiding_cmd[]="$PMTK869,1";
	
	char nmea_cmd[256]={0};
	char temp[50]={0};

	int err=0;
    int count=0;
	struct tm *time_st = NULL;
	long long time_s = 0;

	
	if(g_fd_mt3333_data < 0){
		return -1;
	}

	sprintf(nmea_cmd ,"%s",time_aiding_cmd);
	
	
	
	sprintf(temp ,",%d", enable);
	strcat(nmea_cmd , temp);
	
	sprintf(temp ,"*%02X\x0d\x0a", gps_nmea_cal_checksum(nmea_cmd, strlen(nmea_cmd)));
	strcat(nmea_cmd , temp);
	count = write(g_fd_mt3333_data , nmea_cmd, strlen(nmea_cmd)+1);
	if((strlen(nmea_cmd)+1) != count){
		err=-1;
	}
	LOGD("err=%d,cmd=%s" ,err, nmea_cmd);

    return 0;
}
int mt3333_controller_set_nmea_onoff()
{
    int gll =0;
	int rmc =1;
	int vtg =1;
	int gga =1;
	int gsa =1;
	int gsv =1;
	int accuracy =1;
	const char time_aiding_cmd[]="$PMTK314";
	
	char nmea_cmd[256]={0};
	char temp[50]={0};

	int err=0;
    int count=0;
	struct tm *time_st = NULL;
	long long time_s = 0;

	
	if(g_fd_mt3333_data < 0){
		return -1;
	}

	sprintf(nmea_cmd ,"%s",time_aiding_cmd);
	
	
	
	sprintf(temp ,",%d,%d,%d,%d,%d,%d", gll,rmc,vtg,gga,gsa,gsv);
	strcat(nmea_cmd , temp);
	sprintf(temp ,",0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
	strcat(nmea_cmd , temp);
	sprintf(temp ,",%d", accuracy);
	strcat(nmea_cmd , temp);
	
	sprintf(temp ,"*%02X\x0d\x0a", gps_nmea_cal_checksum(nmea_cmd, strlen(nmea_cmd)));
	strcat(nmea_cmd , temp);
	count = write(g_fd_mt3333_data , nmea_cmd, strlen(nmea_cmd)+1);
	if((strlen(nmea_cmd)+1) != count){
		err=-1;
	}
	LOGD("err=%d,cmd=%s" ,err, nmea_cmd);

    return 0;
}


int mt3333_controller_set_gnss_working_satellite_system(MTK_GNSS_CONFIGURATION gnssmode)
{
    int gps_enable=0;
	int glonass_enable=0;
	int galileo_enable=0;
	int galileo_full_enable=0;
	int beidou_enable=0;
	
	const char time_aiding_cmd[]="$PMTK353";
	
	char nmea_cmd[256]={0};
	char temp[50]={0};

	int err=0;
    int count=0;

	switch(gnssmode){
	case MTK_CONFIG_GPS_GLONASS:
	case MTK_CONFIG_GPS_GLONASS_BEIDOU:
		gps_enable=1;
		glonass_enable=1;
		galileo_enable=0;
		galileo_full_enable=0;
		beidou_enable=0;
		break;
	case MTK_CONFIG_GPS_BEIDOU:
		gps_enable=1;
		glonass_enable=0;
		galileo_enable=0;
		galileo_full_enable=0;
		beidou_enable=1;
		break;	
	case MTK_CONFIG_GPS_ONLY:
		gps_enable=1;
		glonass_enable=0;
		galileo_enable=0;
		galileo_full_enable=0;
		beidou_enable=0;
		break;	
	case MTK_CONFIG_BEIDOU_ONLY:
		gps_enable=0;
		glonass_enable=0;
		galileo_enable=0;
		galileo_full_enable=0;
		beidou_enable=1;
		break;	
	case MTK_CONFIG_GLONASS_ONLY:
		gps_enable=0;
		glonass_enable=1;
		galileo_enable=0;
		galileo_full_enable=0;
		beidou_enable=0;
		break;	
	case MTK_CONFIG_GPS_GLONASS_BEIDOU_GALILEO:
		gps_enable=1;
		glonass_enable=1;
		galileo_enable=1;
		galileo_full_enable=0;
		beidou_enable=0;
		break;	
	}
	
	if(g_fd_mt3333_data < 0){
		return -1;
	}

	sprintf(nmea_cmd ,"%s",time_aiding_cmd);
	
	
	
	sprintf(temp ,",%d,%d,%d,%d,%d", gps_enable,glonass_enable,galileo_enable,galileo_full_enable,beidou_enable);
	strcat(nmea_cmd , temp);
	
	sprintf(temp ,"*%02X\x0d\x0a", gps_nmea_cal_checksum(nmea_cmd, strlen(nmea_cmd)));
	strcat(nmea_cmd , temp);
	count = write(g_fd_mt3333_data , nmea_cmd, strlen(nmea_cmd)+1);
	if((strlen(nmea_cmd)+1) != count){
		err=-1;
	}
	LOGD("err=%d,cmd=%s" ,err, nmea_cmd);

    return 0;
}


static int mt3333_controller_event_hdlr(int fd) {
    char buff[MNLD_INTERNAL_BUFF_SIZE] = {0};
    int offset = 0;
    main_mt3333_controller_event cmd;
    int read_len;
	char assist_data_req=0; // show gps icon

    read_len = safe_recvfrom(fd, buff, sizeof(buff));
    if (read_len <= 0) {
        LOGE("mt3333_controller_event_hdlr() safe_recvfrom() failed read_len=%d", read_len);
        return -1;
    }

	while(offset < read_len){
		
	    cmd = get_int(buff, &offset);
		LOGD("cmd =%d",cmd);
	    switch (cmd) {
	    case MAIN_MT3333_CONTROLLER_EVENT_GPSREBOOT:
			break;
		case MAIN_MT3333_CONTROLLER_EVENT_GPSSTART:
			nmea_1st_after_powerongps = 1;
			break;
		case MAIN_MT3333_CONTROLLER_EVENT_GPSSTOP:
			break;
		case MAIN_MT3333_CONTROLLER_EVENT_REQUES1STNMEA:
			assist_data_req=0; // show gps icon
			porting_layer_callback.sys_agps_disaptcher_callback(MTK_AGPS_CB_START_REQ, 0, &assist_data_req);
			//usleep(800*1000);
			//mt3333_controller_socket_send_cmd(MAIN_MT3333_CONTROLLER_EVENT_NMEA_ONOFF);
			break;
		case MAIN_MT3333_CONTROLLER_EVENT_REQUESTNTP:
			//assist_data_req=1;
			//porting_layer_callback.sys_agps_disaptcher_callback(MTK_AGPS_CB_BITMAP_UPDATE, 0, &assist_data_req);
			mt3333_controller_inject_time(0,0,0);
			break;	
		case MAIN_MT3333_CONTROLLER_EVENT_REQUESTNLP:
			assist_data_req=2;
			porting_layer_callback.sys_agps_disaptcher_callback(MTK_AGPS_CB_BITMAP_UPDATE, 0, &assist_data_req);
			break;	
		case MAIN_MT3333_CONTROLLER_EVENT_REQUESTEPO:
			if(epo_downloader_is_file_invalid()){
				// download QEPO , but now we can't get wk + tow, so need to add.
				porting_layer_callback.sys_agps_disaptcher_callback(MTK_AGPS_CB_QEPO_DOWNLOAD_REQ, 0, NULL);
			}else{
				mt3333_controller_inject_epo(0);
			}
			break;
		case MAIN_MT3333_CONTROLLER_EVENT_REQUESTQEPO:
			mt3333_controller_inject_epo(1);
			break;	
		case MAIN_MT3333_CONTROLLER_EVENT_ENABLEDEBUGLOG:
			mt3333_controller_enable_debuglog(1);
			break;	
		case MAIN_MT3333_CONTROLLER_EVENT_DISABLEDEBUGLOG:
			mt3333_controller_enable_debuglog(0);
			break;	
		case MAIN_MT3333_CONTROLLER_EVENT_ONCEPERSECOND:
			// tell mnld nmea monitor that data is comming normally.
			porting_layer_callback.sys_gps_mnl_callback(MTK_GPS_MSG_FIX_READY);
			break;	
		case MAIN_MT3333_CONTROLLER_EVENT_FACTORYMETA:
			factory_meta_exist = 1;
			break;	
		case MAIN_MT3333_CONTROLLER_EVENT_ENABLEDEASYMODE:
			mt3333_controller_enable_easymode(1);
			break;	
		case MAIN_MT3333_CONTROLLER_EVENT_DISABLEDEASYMODE:
			mt3333_controller_enable_easymode(0);
			break;		
		case MAIN_MT3333_CONTROLLER_EVENT_NMEA_ONOFF:
			mt3333_controller_set_nmea_onoff();
			break;		
		case MAIN_MT3333_CONTROLLER_EVENT_GNSS_SYSTEM:
			mt3333_controller_set_gnss_working_satellite_system(mnl_config.GNSSOPMode);
			break;			
	    }
	}

	return 0;
}

static int mt3333_data_event_hdlr(int fd) {
    char buff[MNLD_INTERNAL_BUFF_SIZE] = {0};
    int offset = 0;
    char assist_data_req=0;
    int read_len;
	int i;
	
	
	do{
		read_len = read(fd, buff, sizeof(buff));
		LOGE("read len=%d", read_len);
	    if (read_len <= 0) {
	        break;
	    }else{
			if((mnld_is_gps_started()) || (factory_meta_exist == 1)){
			}else{
				continue;
			}

			// bypass data to app.
			porting_layer_callback.sys_nmea_output_to_mnld(buff, read_len);
			// debug log when issue occur.
			porting_layer_callback.sys_nmea_output_to_app(buff, read_len);

			//save log to debug log file.
			porting_layer_callback.sys_alps_gps_dbg2file_mnld(buff, read_len);

			for(i=0;i<5; i++){
				if(mt3333_thread_pmtk_input_fd[i] != C_INVALID_SOCKET){
					write(mt3333_thread_pmtk_input_fd[i] , buff, read_len);
				}
			}
		}
	}while(1);
    

    return 0;
}

#if 0
static void mt3333_controller_thread_timeout() {
    LOGE("mt3333_controller_thread_timeout() crash here for debugging");
    CRASH_TO_DEBUG();
}
#endif

void* mt3333_thread_control(void *arg) {
    #define MAX_EPOLL_EVENT 1
    //timer_t hdlr_timer = init_timer(mt3333_controller_thread_timeout);
    struct epoll_event events[MAX_EPOLL_EVENT];
    UNUSED(arg);

    int epfd = epoll_create(MAX_EPOLL_EVENT);
    if (epfd == -1) {
        LOGE("epoll_create failure reason=[%s]%d",
            strerror(errno), errno);
        return 0;
    }

    if (epoll_add_fd(epfd, g_fd_mt3333_controller) == -1) {
        LOGE("epoll_add_fd() failed for g_fd_mt3333_controller failed");
        return 0;
    }

    while (1) {
        int i;
        int n;
        LOGD("wait");
        n = epoll_wait(epfd, events, MAX_EPOLL_EVENT , -1);
        if (n == -1) {
            if (errno == EINTR) {
                continue;
            } else {
                LOGE("epoll_wait failure reason=[%s]%d",
                    strerror(errno), errno);
                return 0;
            }
        }
        
        for (i = 0; i < n; i++) {
            if (events[i].data.fd == g_fd_mt3333_controller) {
                if (events[i].events & EPOLLIN) {
                    mt3333_controller_event_hdlr(g_fd_mt3333_controller);
                }
            } else {
                LOGE("unknown fd=%d",
                    events[i].data.fd);
            }
        }
        
    }
	pthread_exit(NULL);
    LOGE("exit");
    return 0;
}
int mt3333_controller_check_if_gpsfunctionok(void) {
	char buff[32]={0};
	int read_len = 0;
	int temp=0;
	int result=1;
	int i=0;
	
    mnl_set_pwrctl(1);

	int cnt_1s=0;
	int retry_cnt=0;
	do{
		temp = read(g_fd_mt3333_data, buff, sizeof(buff)-1-read_len);
		if(temp<=0){
			temp=0;
		}
		read_len += temp;
		usleep(100 * 1000);
		if((++cnt_1s) > 30){// wait 3s
			
			LOGE("read summary len=%d", read_len);
			cnt_1s = 0;
			read_len =0;
			retry_cnt++;
			mnl_set_pwrctl(4);// reset 3333 power
		}
		if(retry_cnt > 1){
			LOGE("retry max count=5, maybe chip is broken");
			mnl_set_pwrctl(0);
			if (property_set("3333.gps", "fail") != 0) {
				LOGE("set 3333.gps %s\n", strerror(errno));
			}
			return result;
		}
	}while(read_len<31);
	
	LOGE("read len=%d,content=%s", read_len,buff);
	for(i=0; i<sizeof(buff); i++){
		if(buff[i] >= 0x80){
			result=0;
			break;
		}
	}
	//mnl_set_pwrctl(0);
	LOGE("result= %d",result); 
	if(1 == result){
		if (property_set("3333.gps", "ok") != 0) {
			LOGE("set 3333.gps %s\n", strerror(errno));
		}
	}else{
		if (property_set("3333.gps", "fail") != 0) {
			LOGE("set 3333.gps %s\n", strerror(errno));
		}
	}
	
	return result;
}


int mt3333_controller_check_uart_baudrate(void) {
	char buff[32]={0};
	int read_len = 0;
	int temp=0;
	int result=1;
	int i;
	
    mnl_set_pwrctl(1);

	int cnt_1s=0;
	int retry_cnt=0;
	do{
		temp = read(g_fd_mt3333_data, buff, sizeof(buff)-1-read_len);
		if(temp<=0){
			temp=0;
		}
		read_len += temp;
		usleep(100 * 1000);
		if((++cnt_1s) > 30){// wait 3s
			
			LOGE("read summary len=%d", read_len);
			cnt_1s = 0;
			read_len =0;
			retry_cnt++;
			mnl_set_pwrctl(4);// reset 3333 power
		}
		if(retry_cnt > 1){
			LOGE("retry max count=5, maybe chip is broken");
			mnl_set_pwrctl(0);
			return result;
		}
	}while(read_len<31);
	
	LOGE("read len=%d,content=%s", read_len,buff);
	for(i=0; i<sizeof(buff); i++){
		if(buff[i] >= 0x80){
			result=0;
			break;
		}
	}
	mnl_set_pwrctl(0);
	LOGE("result= %d",result); 
	return result;
}


void* mt3333_thread_data(void *arg) {
    #define MAX_EPOLL_EVENT 1
    //timer_t hdlr_timer = init_timer(mt3333_controller_thread_timeout);
    struct epoll_event events[MAX_EPOLL_EVENT];
    UNUSED(arg);

    int epfd = epoll_create(MAX_EPOLL_EVENT);
    if (epfd == -1) {
        LOGE("epoll_create failure reason=[%s]%d",
            strerror(errno), errno);
        return 0;
    }

	if (epoll_add_fd(epfd, g_fd_mt3333_data) == -1) {
        LOGE("epoll_add_fd() failed for g_fd_mt3333_data failed");
        return 0;
    }

    while (1) {
        int i;
        int n;
        LOGD("wait");
        n = epoll_wait(epfd, events, MAX_EPOLL_EVENT , -1);
        if (n == -1) {
            if (errno == EINTR) {
                continue;
            } else {
                LOGE("epoll_wait failure reason=[%s]%d",
                    strerror(errno), errno);
                return 0;
            }
        }
        
        for (i = 0; i < n; i++) {
            if (events[i].data.fd == g_fd_mt3333_data) {
                if (events[i].events & EPOLLIN) {
                    mt3333_data_event_hdlr(g_fd_mt3333_data);
                }
            } else {
                LOGE("unknown fd=%d",
                    events[i].data.fd);
            }
        }
        
    }
	pthread_exit(NULL);
    LOGE("exit");
    return 0;
}



int mt3333_controller_init() {
    pthread_t pthread_mt3333_controller;
	pthread_t pthread_mt3333_data;
	pthread_t pthread_mt3333_data_debug;
	pthread_t pthread_flashtool;

    g_fd_mt3333_controller = socket_bind_udp(MNLD_MT3333_CONTROLLER_SOCKET);
    if (g_fd_mt3333_controller < 0) {
        LOGE("socket_bind_udp(MNLD_MT3333_CONTROLLER_SOCKET) failed");
        return -1;
    }

	g_fd_mt3333_data= open(MT3333_CONTROLLER_TTY_NAME, O_RDWR|O_NOCTTY|O_NONBLOCK);
    if (g_fd_mt3333_data < 0) {
		LOGE("no gps hardware detected: %s:%d, %s", MT3333_CONTROLLER_TTY_NAME, g_fd_mt3333_data, strerror(errno));
        return -1;
    }
	
	
	if(flashtool_file_isvalid()){
		
		pthread_create(&pthread_flashtool, NULL, flashtool_download_thread, NULL);
	}else{
#if 1
		LOGE("uart baudrate:%d", mnld_cfg.init_speed);
		if(0 != mt3333_controller_set_baudrate_length_parity_stopbits(g_fd_mt3333_data ,mnld_cfg.init_speed, 8, 'N', 1)){
			LOGE("configure uart baudrate failed");
			return -1;
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
	}
	
    return 0;
}


int mt3333_controller_socket_send_cmd(main_mt3333_controller_event cmd) {
    char buff[MNLD_INTERNAL_BUFF_SIZE] = {0};
    int offset = 0;
    put_int(buff, &offset, cmd);
    //put_int(buff, &offset, delete_aiding_data_flags);
    return safe_sendto(MNLD_MT3333_CONTROLLER_SOCKET, buff, offset);
}

#endif
