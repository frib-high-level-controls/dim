#include <dim.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

int WebDID_Debug = 0;

#define BUFSIZE 8096
#define WERROR 42
#define SORRY 43
#define LOG   44

#ifndef WIN32
#define O_BINARY 0
#endif

struct {
        char *ext;
        char *filetype;
} extensions [] = {
        {"gif", "image/gif" },  
        {"jpg", "image/jpeg"}, 
        {"jpeg","image/jpeg"},
        {"png", "image/png" },  
        {"zip", "image/zip" },  
        {"gz",  "image/gz"  },  
        {"tar", "image/tar" },  
        {"htm", "text/html" },  
        {"html","text/html" },  
        {"js","text/javascript" },  
        {"css","text/css" },  
        {"php","text/php" },  
        {"json","application/json" },  
        {"ico","image/x-icon" },  
        {0,0} };

extern int web_open_server();
extern void web_write();
extern int web_close();
extern int did_init();
extern int check_browser_changes();
extern int find_services();

void getTime(char *buffer)
{
	time_t nowtime; 
	struct tm *nowtm; 

	nowtime = time((time_t *)0);
	nowtm = (struct tm *)gmtime(&nowtime); 
	strftime(buffer, 128, "%a, %d %b %Y %H:%M:%S GMT", nowtm);
}
	
void log_it(int type, char *s1, char *s2, int conn_id)
{
        char logbuffer[BUFSIZE*2];
	static char date_buffer[128];
	static char snd_buffer[BUFSIZE+1]; /* static so zero filled */

        switch (type) {
        case WERROR: (void)printf("ERROR: %s:%s exiting pid=%d\n",s1, s2, getpid()); break;
        case SORRY: 
                (void)sprintf(logbuffer, "<HTML><BODY><H1>webDid: %s %s</H1></BODY></HTML>\r\n", s1, s2);
				dim_print_date_time();
                (void)printf("webDid: %s %s\n",s1, s2); 
	getTime(date_buffer);
	(void)sprintf(snd_buffer,"HTTP/1.1 200 OK\r\nDate: %s\r\nServer: DID/19.7\r\nContent-Length: %d\r\nContent-Type: %s\r\nConnection: close\r\n\r\n",
		      date_buffer, (int)strlen(logbuffer), "text/html");
    (void)web_write(conn_id,snd_buffer,(int)strlen(snd_buffer));
                (void)web_write(conn_id,logbuffer,(int)strlen(logbuffer));
                break;
        case LOG: (void)printf("INFO: %s:%s:%d\n",s1, s2,conn_id); 
           break;
        }
/*
        if(type == WERROR || type == SORRY)
		{
			sleep(60);
			exit(3);
		}
*/
}

int getParameters(char *buffer, char (*pars)[], char *ptrs[])
{
	char *ptr, *parptr;
	int i, j, n = 0, found = 0;
	int code;

	if(!strchr(buffer,'?'))
		return 0;
	parptr = (char *)pars;
	for(i = 0; *parptr; i++)
	{
		n++;
		ptrs[i] = 0;
		if((ptr = strstr(buffer, parptr)))
		{
			ptrs[i] = ptr+(int)strlen(parptr);
			found++;
		}
		parptr += 32;
	}
	ptrs[i] = 0;
	for(i = 0; ptrs[i]; i++)
	{
	        if((ptr = strchr(ptrs[i],'&')))
			*ptr = '\0';
		while((ptr = strchr(ptrs[i],'%')))
		{
			sscanf((ptr + 1),"%2X",&code);
			sprintf(ptr,"%c",(char)code);
			ptr++;
			for(j = 0; *(ptr + 2); j++)
			{
				*ptr = *(ptr+2);
				ptr++;
			}
			*ptr = '\0';
		}
	}
	if(found == n)
		return 1;
	else
		return 0;
}

int getNodeParameters(char *buffer, char *node, int *browser)
{
	char pars[4][32];
	char *ptrs[4];
	int ret;

	strcpy(pars[0],"node=");
	strcpy(pars[1],"browser=");
	pars[2][0] = '\0';
	ret = getParameters(buffer, pars, ptrs);
	if(!ret)
		return 0;
	strcpy(node, ptrs[0]);
	sscanf(ptrs[1],"%d",browser);
if(WebDID_Debug)
	printf("parse pars - node %s,  browser id %d\n", node, *browser);
	return 1;
}


int getServerParameters(char *buffer, char *node, char *server, int *pid, int *browser)
{
	char pars[10][32];
	char *ptrs[10];
	int ret;

	strcpy(pars[0],"dimnode=");
	strcpy(pars[1],"dimserver=");
	strcpy(pars[2],"dimserverid=");
	strcpy(pars[3],"browser=");
	pars[4][0] = '\0';
	ret = getParameters(buffer, pars, ptrs);
	if(!ret)
		return 0;
	strcpy(node, ptrs[0]);
	strcpy(server, ptrs[1]);
	sscanf(ptrs[2],"%d",pid);
	sscanf(ptrs[3],"%d",browser);
if(WebDID_Debug)
printf("parse pars - node %s, server %s, pid %d, browser %d\n",node, server, *pid, *browser);
	return 1;
}

int getServiceParameters(char *buffer, char *service, int *req, int* browser, int *force)
{
	char pars[10][32];
	char *ptrs[10];
	int ret;

	strcpy(pars[0],"dimservice=");
	strcpy(pars[1],"reqNr=");
	strcpy(pars[2],"reqId=");
	strcpy(pars[3],"force=");
	pars[4][0] = '\0';
	ret = getParameters(buffer, pars, ptrs);
	if(!ret)
		return 0;
	strcpy(service, ptrs[0]);
	sscanf(ptrs[1],"%d",req);
	sscanf(ptrs[2],"%d",browser);
	sscanf(ptrs[3],"%d",force);
if(WebDID_Debug)
printf("\nparse service pars - service %s %d %d %d\n\n",service, *req, *browser, *force);
	return 1;
}


extern char JSONHeader[];
extern char *JSONBuffer;

static char *conv_buffer = 0;
static int conv_buffer_size = 0;

char *unescape(char *buffer)
{
  int buffer_size;
  char *ptr, *ptr1;
  int code;

  buffer_size = (int)strlen(buffer) + 1;
  if(buffer_size > conv_buffer_size )
  {
    if(conv_buffer_size)
      free(conv_buffer);
    conv_buffer = malloc((size_t)buffer_size);
    conv_buffer_size = buffer_size;
  }
  ptr = buffer;
  ptr1 = conv_buffer;
  while(*ptr)
  {
    if(*ptr != '%')
    {
      *ptr1 = *ptr;
      ptr++;
      ptr1++;
    }
    else
    {
      ptr++;
      sscanf(ptr,"%2X",&code);
      sprintf(ptr1,"%c",code);
      ptr += 2;
      ptr1++;
      *ptr1 = '\0';
    }
  }
  *ptr1 = '\0';
  return conv_buffer;
}

void sendData(int conn_id, char *buffer, int type)
{
	static char date_buffer[128];
	static char snd_buffer[BUFSIZE+1]; /* static so zero filled */
	static char snd_data_buffer[BUFSIZE+1]; /* static so zero filled */
	char *ptr = 0;
	char node[128], server[256], service[256];
	int pid, ret, req, browser, force;
	extern char *update_services();
	extern char *update_service_data();
	extern char *getJSONHeader();
	extern char *getJSONBuffer();
	char datatype[128];
	char *conv_buffer;

	conv_buffer = buffer;
	strcpy(datatype,"application/json");
	if(type == 0)
	{
	  ptr = getJSONHeader(0);
	}
	else if(type == 1)
	{
	    ret = getNodeParameters(conv_buffer, node, &browser);
		ptr = getJSONBuffer(node, browser);
	}
	else if(type == 2)
	{
		ret = getServerParameters(conv_buffer, node, server, &pid, &browser);
		if(!ret)
		{
			strcpy(snd_data_buffer,"{}");
			ptr = snd_data_buffer;
		}
		else
		{
			ptr = update_services(node, server, pid, browser);
		}
	}
	else if(type == 3)
	{
		ret = getServiceParameters(conv_buffer, service, &req, &browser, &force);
		if(!ret)
		{
			strcpy(snd_data_buffer,"{}");
			ptr = snd_data_buffer;
		}
		else
		{
			update_service_data(service, conn_id, 0, req, browser, force);
			return;
		}
	}
	else if(type == 4)
	{
		ptr = conv_buffer;
if(WebDID_Debug)
		printf("%s\n",ptr);
		strcpy(datatype,"text/html");
	}
	else if(type == 5)
	{
		ret = getServiceParameters(conv_buffer, service, &req, &browser, &force);
		if(!ret)
		{
			strcpy(snd_data_buffer,"{}");
			ptr = snd_data_buffer;
		}
		else
		{
			check_browser_changes(service, conn_id, 0, req, browser, force);
			return;
		}
	}
	else if(type == 6)
	{
		ret = getServiceParameters(conv_buffer, service, &req, &browser, &force);
		if(!ret)
		{
			strcpy(snd_data_buffer,"{}");
			ptr = snd_data_buffer;
		}
		else
		{
			find_services(service, conn_id, browser, force);
			if(force == -1)
				strcpy(snd_data_buffer,"");
			else
				strcpy(snd_data_buffer,"load");
			ptr = snd_data_buffer;
		}
	}
	getTime(date_buffer);
	(void)sprintf(snd_buffer,"HTTP/1.1 200 OK\r\nDate: %s\r\nServer: DID/19.7\r\nContent-Length: %d\r\nContent-Type: %s\r\nConnection: close\r\n\r\n",
		      date_buffer, (int)strlen(ptr), datatype);
    (void)web_write(conn_id,snd_buffer,(int)strlen(snd_buffer));
if(WebDID_Debug)
	printf("SENDING DATA to conn %d:\n%s\n",conn_id, snd_buffer);
    (void)web_write(conn_id,ptr,(int)strlen(ptr));
if(WebDID_Debug == 2)
	printf("SENDING DATA to conn %d:\n%s\n",conn_id, ptr);
}


void sendSmiData(int conn_id, char *buffer, int type)
{
	static char date_buffer[128];
	static char snd_buffer[BUFSIZE+1]; /* static so zero filled */
	static char snd_data_buffer[BUFSIZE+1]; /* static so zero filled */
	char *ptr = 0;
	char node[128], server[256], service[256];
	int pid, ret, req, browser, force;
	extern char *update_services(), *update_smi_objects();
	extern char *update_service_data();
	extern char *getJSONHeader();
	extern char *getJSONBuffer(), *getJSONSmiBuffer();
	char datatype[128];
	char *conv_buffer;

	conv_buffer = buffer;
	strcpy(datatype,"application/json");
	if(type == 0)
	{
	  ptr = getJSONHeader(1);
	}
	else if(type == 1)
	{
	    ret = getNodeParameters(conv_buffer, node, &browser);
		ptr = getJSONSmiBuffer(node, browser);
	}
	else if(type == 2)
	{
		ret = getServerParameters(conv_buffer, node, server, &pid, &browser);
		if(!ret)
		{
			strcpy(snd_data_buffer,"{}");
			ptr = snd_data_buffer;
		}
		else
		{
			ptr = update_smi_objects(node, server, pid, browser);
		}
	}
	else if(type == 3)
	{
		ret = getServiceParameters(conv_buffer, service, &req, &browser, &force);
		if(!ret)
		{
			strcpy(snd_data_buffer,"{}");
			ptr = snd_data_buffer;
		}
		else
		{
			update_service_data(service, conn_id, 0, req, browser, force);
			return;
		}
	}
	else if(type == 4)
	{
		ptr = conv_buffer;
if(WebDID_Debug)
		printf("%s\n",ptr);
		strcpy(datatype,"text/html");
	}
	else if(type == 5)
	{
		ret = getServiceParameters(conv_buffer, service, &req, &browser, &force);
		if(!ret)
		{
			strcpy(snd_data_buffer,"{}");
			ptr = snd_data_buffer;
		}
		else
		{
			check_browser_changes(service, conn_id, 0, req, browser, force);
			return;
		}
	}
	else if(type == 6)
	{
		ret = getServiceParameters(conv_buffer, service, &req, &browser, &force);
		if(!ret)
		{
			strcpy(snd_data_buffer,"{}");
			ptr = snd_data_buffer;
		}
		else
		{
			find_services(service, conn_id, browser, force);
			if(force == -1)
				strcpy(snd_data_buffer,"");
			else
				strcpy(snd_data_buffer,"load");
			ptr = snd_data_buffer;
		}
	}
	getTime(date_buffer);
	(void)sprintf(snd_buffer,"HTTP/1.1 200 OK\r\nDate: %s\r\nServer: DID/19.7\r\nContent-Length: %d\r\nContent-Type: %s\r\nConnection: close\r\n\r\n",
		      date_buffer, (int)strlen(ptr), datatype);
    (void)web_write(conn_id,snd_buffer,(int)strlen(snd_buffer));
if(WebDID_Debug)
	printf("SENDING DATA to conn %d:\n%s\n",conn_id, snd_buffer);
    (void)web_write(conn_id,ptr,(int)strlen(ptr));
if(WebDID_Debug == 2)
	printf("SENDING DATA to conn %d:\n%s\n",conn_id, ptr);
}

void sendFile(int conn_id, char *buffer, int size)
{
        int j, file_fd, buflen, len;
        int i, ret;
        char * fstr;
		int flen;
        static char snd_buffer[BUFSIZE+1]; /* static so zero filled */
		static char date_buffer[128];


		ret = size;
        if(ret > 0 && ret < BUFSIZE)    /* return code is valid chars */
                buffer[ret]=0;          /* terminate the buffer */
        else buffer[0]=0;

if(WebDID_Debug)
printf("Got %s\n", buffer);
        if( strncmp(buffer,"GET ",4) && strncmp(buffer,"get ",4) )
		{
                log_it(SORRY,"Only simple GET operation supported",buffer,conn_id);
				return;
		}

        for(i=4;i<BUFSIZE;i++) 
		{ /* null terminate after the second space to ignore extra stuff */
                if(buffer[i] == ' ') 
				{ /* string is "GET URL " +lots of other stuff */
                        buffer[i] = 0;
                        break;
                }
        }

if(WebDID_Debug)
printf("Got 1 %s\n", buffer);
       for(j=0;j<i-1;j++)      /* check for illegal parent directory use .. */
		{
                if(buffer[j] == '.' && buffer[j+1] == '.')
				{
                        log_it(SORRY,"Parent directory (..) path names not supported",buffer,conn_id);
						return;
				}
		}
		if((int)strlen(buffer) == 5)
		{
			if( !strncmp(&buffer[0],"GET /",5) || !strncmp(&buffer[0],"get /",5) ) 
			/* convert no filename to index file */
                (void)strcpy(buffer,"GET /index.html");
		}
		if((int)strlen(buffer) == 8)
		{
			if( !strncmp(&buffer[0],"GET /smi",8) || !strncmp(&buffer[0],"get /smi",8) ) 
			/* convert no filename to index file */
                (void)strcpy(buffer,"GET /smi/index.html");
		}
        /* work out the file type and check we support it */
        buflen=(int)strlen(buffer);
        fstr = (char *)0;
        for(i=0;extensions[i].ext != 0;i++) 
		{
              len = (int)strlen(extensions[i].ext);
              if( !strncmp(&buffer[buflen-len], extensions[i].ext, (size_t)len)) 
			  {
                        fstr =extensions[i].filetype;
                        break;
                }
        }
/*
		(void)sprintf(snd_buffer,"HTTP/1.1 100 Continue\r\n\r\n");
        (void)web_write(conn_id,snd_buffer,(int)strlen(snd_buffer));
		printf("SENDING to conn %d:\n%s\n",conn_id, snd_buffer);
*/
		if(fstr == 0)
		{
if(WebDID_Debug)
printf("Got %s\n", buffer);
			if(!strncmp(&buffer[5],"didHeader",9))
			{
				sendData(conn_id, &buffer[5], 0);
				return;
			}
			else if(!strncmp(&buffer[5],"didData",7))
			{
				sendData(conn_id, &buffer[5], 1);
				return;
			}
			else if(!strncmp(&buffer[5],"didServices",11))
			{
				sendData(conn_id, &buffer[5], 2);
				return;
			}
			else if(!strncmp(&buffer[5],"didServiceData",14))
			{
				sendData(conn_id, &buffer[5], 3);
				return;
			}
			else if(!strncmp(&buffer[5],"didPoll",7))
			{
				sendData(conn_id, &buffer[5], 5);
				return;
			}
			else if(!strncmp(&buffer[5],"didQuery",8))
			{
				sendData(conn_id, &buffer[5], 6);
				return;
			}
			else if(!strncmp(&buffer[5],"smiData",7))
			{
				sendSmiData(conn_id, &buffer[5], 1);
				return;
			}
			else if(!strncmp(&buffer[5],"smiObjects",10))
			{
				sendSmiData(conn_id, &buffer[5], 2);
				return;
			}
/*
			if((!strncmp(&buffer[5],"didData",7)) || (!strncmp(&buffer[5],"didServices",11)))
			{
				if(ptr = strchr(&buffer[5],'/'))
				{
					*ptr = '\0';
				}
				buflen=(int)strlen(buffer);
				for(i=0;extensions[i].ext != 0;i++) 
				{
					len = (int)strlen(extensions[i].ext);
					if( !strncmp(&buffer[buflen-len], extensions[i].ext, len)) 
					{
					    fstr =extensions[i].filetype;
						printf("fstr %s", fstr);
                        break;
					}
				}
				if(!strncmp(&buffer[5],"didData",7))
				{
					sendData(conn_id, &buffer[5]);
				}
				else if(!strncmp(&buffer[5],"didServices",11))
				{
					sendData(conn_id, &buffer[5]);
				}
			}
*/
			else
			{
				log_it(SORRY,"file extension type not supported",buffer,conn_id);
				return;
			}
		}

		if(( file_fd = open(&buffer[5],O_RDONLY | O_BINARY)) == -1) /* open the file for reading */
		{
                log_it(SORRY, "failed to open file",&buffer[5],conn_id);
				return;
		}

		flen = 0;
        while ( (ret = (int)read(file_fd, snd_buffer, BUFSIZE)) > 0 ) 
		{
			flen += ret;
		}
		close(file_fd);

		if(( file_fd = open(&buffer[5],O_RDONLY | O_BINARY)) == -1) /* open the file for reading */
		{
                log_it(SORRY, "failed to open file",&buffer[5],conn_id);
				return;
		}

		getTime(date_buffer);
		(void)sprintf(snd_buffer,"HTTP/1.1 200 OK\r\nDate: %s\r\nServer: DID/19.7\r\nContent-Length: %d\r\nContent-Type: %s\r\nConnection: close\r\n\r\n",
            date_buffer, flen, fstr);
        (void)web_write(conn_id,snd_buffer,(int)strlen(snd_buffer));
if(WebDID_Debug)
		printf("SENDING to conn %d:\n%s\n",conn_id, snd_buffer);

        /* send file in 8KB block - last block may be smaller */
        while ( (ret = (int)read(file_fd, snd_buffer, BUFSIZE)) > 0 ) {
                (void)web_write(conn_id,snd_buffer,ret);
if(WebDID_Debug == 2)
			printf("SENDING data to conn %d: %d bytes\n",conn_id, ret);
        }
		close(file_fd);
#ifdef LINUX
        sleep(1);       /* to allow socket to drain */
#endif
}

static void handler( int conn_id, char *packet, int size, int status )
{
	switch(status)
	{
	case STA_DISC:     /* connection broken */
if(WebDID_Debug)
{
			dim_print_date_time();
			printf(" Disconnect received - conn: %d to %s@%s\n", conn_id,
				Net_conns[conn_id].task,Net_conns[conn_id].node );
}
			web_close(conn_id);
		break;
	case STA_CONN:     /* connection received */
if(WebDID_Debug)
{
			dim_print_date_time();
			printf(" Connection request received - conn: %d\n", conn_id);
}
		break;
	case STA_DATA:     /* normal packet */
/*
			dim_print_date_time();
			printf(" conn %d packet received:\n", conn_id);
			printf("packet size = %d\n", size);
			printf("%s\n",packet);
			fflush(stdout);
*/
			sendFile(conn_id, packet, size);
		break;
	default:	
		dim_print_date_time();
		printf( " - DIM panic: recv_rout(): Bad switch, exiting...\n");
		abort();
	}
}

static void error_handler(int conn_id, int severity, int errcode, char *reason)
{
	if(conn_id){}
	if(errcode){}
	dim_print_msg(reason, severity);
/*
	if(severity == 3)
	{
			printf("Exiting!\n");
			exit(2);
	}
*/
}

int main(int argc, char **argv)
{
    int port;
	int proto;
	char dns_node[128];
	int web_get_port();
	char *ptr;
	char currwd[256];

	if(argc){}

	strcpy(currwd, argv[0]);
	printf("arg %s\n",currwd);
	ptr = strrchr(currwd,'/');
	if(ptr)
	{
		*ptr = '\0';
	}
	ptr = strrchr(currwd,'\\');
	if(ptr)
	{
		*ptr = '\0';
	}
	chdir(currwd);
        log_it(LOG,"webDid starting",argv[1],getpid());
        /* setup the network socket */
		proto = 1;
		port = web_get_port();
		get_node_name(dns_node);
		did_init(dns_node, DNS_PORT);
		if(!web_open_server("DID",handler, &proto, &port, error_handler))
			return(0);
/*
		ret = matchString("hello world","*ll*");
		printf("%s %s %d\n", "hello world","*ll*",ret);
		ret = matchString("hello world","ll*");
		printf("%s %s %d\n", "hello world","ll*",ret);
*/
		while(1)
			sleep(10);
		return(0);
}
