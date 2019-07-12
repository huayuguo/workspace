#include <termios.h>
#include <unistd.h>
#include <stdlib.h>
#include <fcntl.h> 
#include <linux/kernel.h>
#include <stdio.h>
#include <pthread.h>
#include <string.h>
#include <sys/select.h>
#include <sys/time.h>
 
char rev_buf[256];
char databuf[] = "Hello World!\n";
int uart_fd;
 
#define DEV_NAME "/dev/ttyMT1"
 
int Set_Com_Config(int fd, int baud_rate, int data_bits, char parity, int stop_bits)
{
   struct termios new_cfg, old_cfg;
   
   int speed;
   
   /*保存并测试现有串口参数设置，在这里如果串口号等出错，会有相关出错信息*/
   if(tcgetattr(fd, &old_cfg) != 0)       /*该函数得到fd指向的终端配置参数，并将它们保存到old_cfg变量中，成功返回0，否则-1*/
	{
		perror("tcgetttr");
		return -1;		  
	}

	/*设置字符大小*/
	new_cfg = old_cfg;		
	cfmakeraw(&new_cfg); /*配置为原始模式*/	   
	new_cfg.c_cflag &= ~CSIZE; /*用位掩码清空数据位的设置*/ 
	/*设置波特率*/
	switch(baud_rate)	
	{
		case 2400:
		speed = B2400;			
		break;

		case 4800:			
			speed = B4800;			
			break;

		case 9600:
			speed = B9600;			
			break;

		case 115200:			
			speed = B115200;			
			break;

		case 19200:			
			speed = B19200;			
			break;

		case 38400:			
			speed = B38400;			
			break;

		default:					
			speed = B115200;			
			break;
	}
 
	cfsetispeed(&new_cfg, speed); //设置输入波特率
	cfsetospeed(&new_cfg, speed); //设置输出波特率
 
	/*设置数据长度*/
	switch(data_bits)	
	{
		case 5:
			new_cfg.c_cflag &= ~CSIZE;//屏蔽其它标志位
			new_cfg.c_cflag |= CS5;
			break;

		case 6:
			new_cfg.c_cflag &= ~CSIZE;//屏蔽其它标志位
			new_cfg.c_cflag |= CS6;
			break;

		case 7:
			new_cfg.c_cflag &= ~CSIZE;//屏蔽其它标志位
			new_cfg.c_cflag |= CS7;
			break;

		case 8:
			new_cfg.c_cflag &= ~CSIZE;//屏蔽其它标志位
			new_cfg.c_cflag |= CS8;
			break;

		default:			
			new_cfg.c_cflag &= ~CSIZE;//屏蔽其它标志位
			new_cfg.c_cflag |= CS8;
			break;
	}
 
	/*设置奇偶校验位*/
	switch(parity)
	{
		default:		
		case 'n':	
		case 'N': //无校验
		{
			new_cfg.c_cflag &= ~PARENB;	
			new_cfg.c_iflag &= ~INPCK;
		}	
		break;

		case 'o': //奇校验	
		case 'O':	
		{
			new_cfg.c_cflag |= (PARODD | PARENB);	
			new_cfg.c_iflag |= INPCK;
		}		
		break;

		case 'e': //偶校验
		case 'E':
		{
			new_cfg.c_cflag |=  PARENB;
			new_cfg.c_cflag &= ~PARODD;
			new_cfg.c_iflag |= INPCK;
		}
		break;
	}
 
	/*设置停止位*/
	switch(stop_bits)
	{
		default:
		case 1:
			new_cfg.c_cflag &= ~CSTOPB;
			break;

		case 2:
			new_cfg.c_cflag |= CSTOPB;
			break;
	}

	/*设置等待时间和最小接收字符*/
	new_cfg.c_cc[VTIME] = 1; /* 读取一个字符等待1*(1/10)s */
	new_cfg.c_cc[VMIN] = 1; /* 读取字符的最少个数为1 */

	/*处理未接收字符*/
	tcflush(fd, TCIFLUSH); //溢出数据可以接收，但不读

	/* 激活配置 (将修改后的termios数据设置到串口中)
	 * TCSANOW：所有改变立即生效
	 */
	if((tcsetattr(fd, TCSANOW, &new_cfg))!= 0)
	{
		perror("tcsetattr");
		return -1;
	}

	return 0;   
}
 
int Uart_Send(int fd, char *data, int data_len)  
{  
	int len = 0;  
	len = write(fd, data, data_len);

	if(len == data_len) {  
		return len;  
	} else {  
		tcflush(fd, TCOFLUSH); //TCOFLUSH刷新写入的数据但不传送  
		return -1;  
	}  
} 
 
int Uart_Recv(int fd, char *rev_buf, int data_len)
{
	int len, fs_sel;  
	fd_set fs_read;  

	struct timeval tv_timeout;  

	FD_ZERO(&fs_read); //清空集合
	FD_SET(fd,&fs_read); // 将一个给定的文件描述符加入集合之中 

	tv_timeout.tv_sec = 5;  
	tv_timeout.tv_usec = 0;  

	//使用select实现串口的多路通信  
	fs_sel = select(fd + 1, &fs_read, NULL, NULL, &tv_timeout); //如果select返回值大于0，说明文件描述符发生了变化
	//printf("fs_sel = %d\n",fs_sel);  //如果返回0，代表在描述符状态改变前已超过timeout时间,错误返回-1  

	if(fs_sel)  
	{  
		len = read(fd, rev_buf, data_len);  
		printf("len = %d, fs_sel = %d\n", len, fs_sel);  
		return len;  
	}  
	else  
	{  
		return 0;  
	}       
}  
 
int Uart_Init(int fd, int speed, int databits, int parity, int stopbits)
{
	//设置串口数据帧格式  
	if (Set_Com_Config(fd, speed, databits, parity, stopbits) == 0)  
	{                                                           
		return 0;  
	}  
	else  
	{  
		return  -1;  
	}  
}
 
int Uart_Receive_thread(void)
{
	int r_count;
	while(1)
	{
		r_count = Uart_Recv(uart_fd, rev_buf, sizeof(rev_buf));	
		if(r_count)
		{
			for(int i = 0; i < r_count; i++)
			{
				printf("rev_buf[%d] = 0x%x\n", i, rev_buf[i]);		
			}
		}
	}
}
 
void Uart_Send_thread(void)
{
	int s_count;

	while(1)
	{
		sleep(1);
		printf("This is Uart_Send_thread.\n");
		s_count = Uart_Send(uart_fd, databuf, strlen(databuf));
		if(s_count == -1)	
			printf("Uart_Send Error!\n");
		else
			printf("Send %d data successfully!\n", s_count);
	}	
}
 
int main(int argc, char *argv[]) 
{  
	pthread_t id_Uart_Receive, id_Uart_Send;
	int ret1, ret2;

	uart_fd = open(DEV_NAME, O_RDWR | O_NOCTTY);
	if(uart_fd == -1)
	{
		printf("Uart Open Failed!\n");
		exit(EXIT_FAILURE);
	}

	if(Uart_Init(uart_fd, 115200, 8, 'N', 1) == -1)
	{
		printf("Uart_Init Failed!\n");
		exit(EXIT_FAILURE);
	}

	ret1 = pthread_create(&id_Uart_Receive, NULL, (void *) Uart_Receive_thread, NULL);
	ret2 = pthread_create(&id_Uart_Send, NULL, (void *) Uart_Send_thread, NULL);

	if(ret1!=0){
		printf ("Create Uart_Receive_thread error!\n");
		close(uart_fd);
		exit (1);
	}
	if(ret2!=0){
		printf ("Create Uart_Send_thread error!\n");
		close(uart_fd);
		exit (1);
	}
	while(1)
	{
		printf("This is the main process.\n");
		usleep(1000000);
	}
	pthread_join(id_Uart_Receive, NULL);
	pthread_join(id_Uart_Send, NULL);

	return 0;
}
