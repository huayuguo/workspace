#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>

int main(int argc, char *argv[]) {
	int fd;

	fd = open("/sys/devices/platform/my_led/status", O_RDWR);
	if(fd < 0) {
		printf("Can't access my_led file node.\n");
	}

	write(fd, "0", 1);
	close(fd);
	
	return 0;
}
