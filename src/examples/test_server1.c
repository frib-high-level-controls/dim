#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <dis.h>

int Data1[4000];
int Data2[4000];
int Id1, Id2;

void cmnd_rout(int *tag, char *buf, int *size)
{
	int cid[2];
	
	cid[0] = dis_get_conn_id();
	cid[1] = 0;
	if(*tag == 1)
		dis_selective_update_service(Id1, cid);
	else if(*tag == 2)
		dis_selective_update_service(Id2, cid);
}

int main(int argc, char **argv)
{
	int i, id, *ptr;
	char aux[80];
	char name[84], name1[132];
	int on = 0;
	long dnsid = 0;
	char extra_dns[128];
	int new_dns = 0;
/*
	int buf_sz, buf_sz1;
*/

dis_set_debug_on();

	i = 0;
	Data1[0] = i;	
	Id1 = dis_add_service( "Beam1/Data", "C", Data1, 4000, (void *)0, 0 );
	dis_add_cmnd("Beam1/Cmd","C",cmnd_rout, 1);
	Data2[0] = i;	
	Id2 = dis_add_service( "Beam2/Data", "C", Data2, 4000, (void *)0, 0 );
	dis_add_cmnd("Beam2/Cmd","C",cmnd_rout, 2);

	dis_start_serving( argv[1] );

	while(1)
	{
		usleep(1000);
		i++;
		Data1[0] = i;	
		dis_update_service(Id1);
		Data2[0] = i;	
		dis_update_service(Id2);
	}
	return 1;
}

