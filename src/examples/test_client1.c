#include <dic.h>
#include <time.h>
#include <string.h>
#include <stdio.h>

int Data[4000];
int no_link = -1;

void rout( tag, buf, size )
int *buf;
int *tag, *size;
{

	printf("Received beam%d Data : %d\n",*tag, buf[0]);
}

int main(int argc, char **argv)
{
	int i;
	char aux[80];
	int id = 123;

	dic_info_service( "Beam1/Data", MONITORED, 0, 0, 0, rout, 1,
			  &no_link, 4 );
	dic_info_service( "Beam1/Data", MONITORED, 0, 0, 0, rout, 1,
			  &no_link, 4 );
	dic_info_service( "Beam1/Data", MONITORED, 0, 0, 0, rout, 1,
			  &no_link, 4 );
	dic_info_service( "Beam2/Data", MONITORED, 0, 0, 0, rout, 2,
			  &no_link, 4 );
	dic_info_service( "Beam2/Data", MONITORED, 0, 0, 0, rout, 2,
			  &no_link, 4 );
	dic_info_service( "Beam2/Data", MONITORED, 0, 0, 0, rout, 2,
			  &no_link, 4 );
	
	while(1)
	{
		usleep(1000);
		dic_cmnd_service("Beam1/Cmd","Update",7);
		dic_cmnd_service("Beam2/Cmd","Update",7);
	}
	return 1;
}
