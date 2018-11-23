#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>
#include <linux/ioctl.h>
#include <sys/ioctl.h>
#include <errno.h>

#include "SerialPort.h"


#include "utils/Log.h"
#define LOG_TAG "serial_port"

#define IR_IOC	'R'

#define IR_IOCTRL_POWER_ON  _IO(IR_IOC, 0x00)
#define IR_IOCTRL_POWER_OFF _IO(IR_IOC, 0x01)

const char *IR_DEV_FILE = "/dev/ir";

int gIRFd = -1;
int mTtyFd = -1;


static speed_t getBaudrate(jint baudrate)
{
        switch(baudrate) {
        case 0: return B0;
        case 50: return B50;
        case 75: return B75;
        case 110: return B110;
        case 134: return B134;
        case 150: return B150;
        case 200: return B200;
        case 300: return B300;
        case 600: return B600;
        case 1200: return B1200;
        case 1800: return B1800;
        case 2400: return B2400;
        case 4800: return B4800;
        case 9600: return B9600;
        case 19200: return B19200;
        case 38400: return B38400;
        case 57600: return B57600;
        case 115200: return B115200;
        case 230400: return B230400;
        case 460800: return B460800;
        case 500000: return B500000;
        case 576000: return B576000;
        case 921600: return B921600;
        case 1000000: return B1000000;
        case 1152000: return B1152000;
        case 1500000: return B1500000;
        case 2000000: return B2000000;
        case 2500000: return B2500000;
        case 3000000: return B3000000;
        case 3500000: return B3500000;
        case 4000000: return B4000000;
        default: return -1;
        }
}

int ir_power_on_off(int poweron)
{
        int res;

        gIRFd = open(IR_DEV_FILE, O_RDONLY);
        if (gIRFd <= 0)
        {
                ALOGE("open ir file error , errno:%d", errno);
                return errno;
        }
        if (poweron != 0)
        {
                ALOGD("ir power on");
                res = ioctl(gIRFd, IR_IOCTRL_POWER_ON, 0);
                if (res < 0) {
                        ALOGE("power on: ioctl failed (%s)\n", strerror(errno));
                }
        }
        else
        {
                ALOGD("ir power off");
                res = ioctl(gIRFd, IR_IOCTRL_POWER_OFF, 0);
                if (res < 0) {
                        ALOGE("power off: ioctl failed (%s)\n", strerror(errno));
                }
        }
        close(gIRFd);
        return 0;
}
/*
 * Class:     android_serialport_SerialPort
 * Method:    open
 * Signature: (Ljava/lang/String;II)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL Java_com_yjzn_SerialPort_open
  (JNIEnv *env, jclass thiz, jstring path, jint baudrate, jint flags)
{
        speed_t speed;
        jobject mFileDescriptor;

        /* Check arguments */
        {
                speed = getBaudrate(baudrate);
                if (speed == -1) {
                        /* TODO: throw an exception */
                        ALOGE("Invalid baudrate");
                        return NULL;
                }
        }

        /* Opening device */
        {
                jboolean iscopy;
                const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
                ALOGD("Opening serial port %s with flags 0x%x", path_utf, O_RDWR | flags);
                mTtyFd = open(path_utf, O_RDWR | flags);
                ALOGD("open() fd = %d", mTtyFd);
                (*env)->ReleaseStringUTFChars(env, path, path_utf);
                if (mTtyFd == -1)
                {
                        /* Throw an exception */
                        ALOGE("Cannot open port");
                        /* TODO: throw an exception */
                        return NULL;
                }
        }

        /* Configure device */
        {
                struct termios cfg;
                ALOGD("Configuring serial port");
                if (tcgetattr(mTtyFd, &cfg))
                {
                        ALOGE("tcgetattr() failed");
                        close(mTtyFd);
                        /* TODO: throw an exception */
                        return NULL;
                }

                cfmakeraw(&cfg);
                cfsetispeed(&cfg, speed);
                cfsetospeed(&cfg, speed);

                if (tcsetattr(mTtyFd, TCSANOW, &cfg))
                {
                        ALOGE("tcsetattr() failed");
                        close(mTtyFd);
                        /* TODO: throw an exception */
                        return NULL;
                }
        }

        /* Create a corresponding file descriptor */
        {
                jclass cFileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
                jmethodID iFileDescriptor = (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
                jfieldID descriptorID = (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
                mFileDescriptor = (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);
                (*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint)mTtyFd);
        }

        //ir_power_on_off(1);

        return mFileDescriptor;
}

JNIEXPORT int JNICALL Java_com_yjzn_SerialPort_configUart
        (JNIEnv *env, jobject thiz, jint nBits,jchar nEvent,jint baudrate,jint nStop)
{
        ALOGW("configUart:nBits=%d,nEvent=%c,nSpeed=%d,nStop=%d",nBits,nEvent,baudrate,nStop);
        struct termios newtio,oldtio;
        speed_t speed;

        if (mTtyFd == -1)
        {
                ALOGE("Cannot open port");
                return -1;
        }
        if(tcgetattr(mTtyFd, &oldtio) != 0){
                ALOGE("setup serial failure");
                return -1;
        }
        memset(&newtio,0,sizeof(newtio));
        newtio.c_cflag |=CLOCAL|CREAD;
        switch(nBits){//设置数据位数
                case 7:
                        newtio.c_cflag &=~CSIZE;
                        newtio.c_cflag |=CS7;
                break;
                case 8:
                        newtio.c_cflag &=~CSIZE;
                        newtio.c_cflag |=CS8;
                break;
                default:
                        ALOGW("nBits:%d,invalid param",nBits);
                break;
        }
        switch(nEvent){//设置校验位
                case 'O':
                        newtio.c_cflag |=PARENB;//enable parity checking
                        newtio.c_cflag |=PARODD;//奇校验位
                        newtio.c_iflag |=(INPCK|ISTRIP);
                        //options.c_iflag |= INPCK;//Disable parity checking
                break;
                case 'E':
                        newtio.c_cflag|=PARENB;//
                        newtio.c_cflag&=~PARODD;//偶校验位
                        newtio.c_iflag|=(INPCK|ISTRIP);
                        //options.c_iflag |= INPCK;//Disable parity checking
                break;
                case 'N':
                        newtio.c_cflag &=~PARENB;//清除校验位
                        //options.c_iflag &=~INPCK;//Enable parity checking
                break;
                //case 'S':
                // options.c_cflag &= ~PARENB;//清除校验位
                // options.c_cflag &=~CSTOPB;
                // options.c_iflag |=INPCK;//Disable parity checking
                // break;
                default:
                        newtio.c_cflag &=~PARENB;
                        ALOGW("nEvent:%c,invalid param",nEvent);
                break;
        }
        speed = getBaudrate(baudrate);
        cfsetispeed(&newtio,speed);
        cfsetospeed(&newtio,speed);

        switch(nStop){//设置停止位
                case 1:
                        newtio.c_cflag &= ~CSTOPB;
                break;
                case 2:
                        newtio.c_cflag |= CSTOPB;
                break;
                default:
                        ALOGW("nStop:%d,invalid param",nStop);
                break;
        }
        newtio.c_cc[VTIME] = 0;//设置等待时间
        newtio.c_cc[VMIN] = 0;//设置最小接收字符
        tcflush(mTtyFd, TCIFLUSH);
        if(tcsetattr(mTtyFd,TCSANOW,&newtio) != 0){
                ALOGE("options set error");
                return -1;
        }
        return 1;
}

/*
 * Class:     cedric_serial_SerialPort
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_yjzn_SerialPort_close
  (JNIEnv *env, jobject thiz)
{
        //ir_power_on_off(0);

        close(mTtyFd);
        mTtyFd=-1;
}


