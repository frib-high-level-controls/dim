#include <stdio.h>                   
#include <ctype.h>
#include <time.h>
#include <dim.h>
#include <dic.h>
#include <dis.h>

extern int WebDID_Debug;

typedef struct item{
    struct item *next;
	DNS_SERVER_INFO server;
	DNS_SERVICE_INFO *service_ptr;
    char name[MAX_NAME];
	int match;
	int busy;
	int isSMI;
}SERVER;

typedef struct nitem{
    struct nitem *next;
	SERVER *server_head;
    char name[MAX_NAME];
	int match;
	int busy;
	int hasSMI;
}NODE;
NODE *Node_head = (NODE *)0;

typedef struct sitem{
    struct sitem *next;
	char name[MAX_NAME];
	int sid;
	void *buffer;
	int buffer_size;
	int size;
	time_t timestamp;
	void *buffer_str;
	int buffer_str_size;
	int str_size;
	int conn_id;
	time_t last_subscribed;
	time_t last_updated;
	int n_browsers;
}CURR_SERVICE;
CURR_SERVICE *Curr_service_head = (CURR_SERVICE *)0;

typedef struct objstate{
	char name[MAX_NAME];
	char state[512];
	int sid;
	int mode_index;
	void *browserp;
}OBJSTATE;
/*
typedef struct domainitem{
	char name[MAX_NAME];
	OBJSTATE objs[1];
}CURR_SMIDOMAIN;
*/
typedef struct bitem{
    struct bitem *next;
	int id;
	int subscribe;
	time_t last_subscribed;
	time_t last_updated;
	time_t last_polled;
	time_t last_changed;
	int conn_id;
	int n_services;
	int n_servers;
	int n_nodes;
	CURR_SERVICE *servicep;
	char *JSONBuffer;
	int JSONBufferSize;
	char *JSONSmiBuffer;
	int JSONSmiBufferSize;
	char pattern[256];
	char curr_command[MAX_NAME];
	char *service_format_ptr;
	int isSMI;
	int n_domains;
	char curr_smidomain[MAX_NAME];
	int curr_smidomain_size;
	int curr_smidomain_nobjs;
	OBJSTATE *smidomainp;
}BROWSER;
BROWSER *Browser_head = (BROWSER *)0;

char *JSONBuffer = 0;
int JSONBufferSize = 0;
char JSONHeader[256] = {'\0'};
char JSONSmiHeader[256] = {'\0'};

char *JSONSmiBuffer = 0;
int JSONSmiBufferSize = 0;

int First_time = 1;
int Curr_view_opt = -1;	
char Curr_view_opt_par[80];	
char Curr_service_name[132];
char Curr_service_format[256];
int Curr_service_print_type = 0;
int N_nodes = 0;
int N_servers = 0;	
int N_services = 0;	
static char no_link = -1;
static char no_link_str[5] = "DEAD";
int no_link_int = -1;
FILE	*fptr;

char *Service_content_str;
char *Curr_service_list = 0;
char *Curr_client_list = 0;
int Curr_service_id = 0;
SERVER *Got_Service_List = 0;
SERVER *Got_Client_List = 0;

int Timer_q;
char Title[128];

int did_init(char *local_node, int dns_port)
{
	void update_servers();
	char icon_title[128];
	char dns_node[128];
	int ret;
       
	dim_init();
	dic_disable_padding();
	dis_disable_padding();
	
	ret = dim_get_dns_node(dns_node);
	if(!ret)
	{
		strcpy(dns_node, local_node);
		dim_set_dns_node(dns_node);
	}
    dns_port = dic_get_dns_port();
	if(dns_port != DNS_PORT)
	{
		sprintf(Title,"DIM DNS: %s:%d",dns_node,dns_port);
	}
	else
	{
		sprintf(Title,"DIM DNS: %s",dns_node);
	}
	sprintf(icon_title,"DID %s",dns_node);
dim_print_date_time();
printf("webDid Starting up on %s\n\t serving %s\n", local_node, Title);
	Timer_q = dtq_create();
	dic_info_service("DIS_DNS/SERVER_INFO",MONITORED,0,0,0,update_servers,0,
						&no_link,1);
	return 1;
}

SERVER *find_server(NODE *nodep, int pid)
{
  SERVER *servp;
  DNS_SERVER_INFO *ptr;

  servp = nodep->server_head;
  while( (servp = (SERVER *)sll_get_next((SLL *)servp)) )
  {
      ptr = &servp->server;
      if(ptr->pid == pid)
	  {
		return(servp);
	  }
  }
  return ((SERVER *)0);
}

NODE *find_node(char *node)
{
  NODE *nodep;

  nodep = Node_head;
  while( (nodep = (NODE *)sll_get_next((SLL *)nodep)) )
  {
      if(!strcmp(nodep->name,node))
	  {
			return(nodep);
	  }
  }
  return ((NODE *)0);
}

int find_server_service_pattern(SERVER *servp, char *pattern)
{
	DNS_SERVICE_INFO *servicep;
	int n_services, i;
	int n_found = 0;

	servicep = servp->service_ptr;
	n_services = servp->server.n_services;
	for(i = 0; i < n_services; i++)
	{
		if(strstr(servicep->name, pattern))
		{
			n_found++;
		}
		servicep++;
	}
	return(n_found);
}

int find_service_pattern(NODE *nodep, SERVER *servpp, char *pattern, int *n_servers)
{
  SERVER *servp;
  int ret, n_found = 0;
  int n_servers_found = 0;

  if(!servpp)
  {
	servp = nodep->server_head;
	while( (servp = (SERVER *)sll_get_next((SLL *)servp)) )
	{
		if((ret = find_server_service_pattern(servp, pattern)))
		{
			n_found += ret;
			n_servers_found++;
		}
	}
  }
  else
  {
	if((ret = find_server_service_pattern(servpp, pattern)))
	{
		n_found += ret;
	}
  }
  if(n_servers != 0)
	  *n_servers = n_servers_found;
  return(n_found);
}

CURR_SERVICE *find_curr_service(char *service)
{
  CURR_SERVICE *servicep;

  servicep = Curr_service_head ;
  while( (servicep = (CURR_SERVICE *)sll_get_next((SLL *)servicep)) )
  {
      if(!strcmp(servicep->name,service))
	  {
			return(servicep);
	  }
  }
  return ((CURR_SERVICE *)0);
}

BROWSER *find_browser(int id)
{
  BROWSER *browserp;

  browserp = Browser_head;
  while( (browserp = (BROWSER *)sll_get_next((SLL *)browserp)) )
  {
      if(browserp->id == id)
	  {
			return(browserp);
	  }
  }
  return ((BROWSER *)0);
}
/*
void set_browser_changes(int n_services, int n_servers, int n_nodes)
{
  BROWSER *browserp;

  browserp = Browser_head;
  while( (browserp = (BROWSER *)sll_get_next((SLL *)browserp)) )
  {
	  if(browserp->n_services != n_services)
		browserp->n_services_changed = 1;
	  if(browserp->n_servers != n_servers)
		browserp->n_servers_changed = 1;
	  if(browserp->n_nodes != n_nodes)
		browserp->n_nodes_changed = 1;
  }
}
*/
void prepare_browser_tree()
{
  BROWSER *browserp;
  int prepareJSONTree();

  browserp = Browser_head;
  while( (browserp = (BROWSER *)sll_get_next((SLL *)browserp)) )
  {
	  if(browserp->pattern[0] != '\0')
		  prepareJSONTree(browserp);
  }
}
/*
void clear_browser_changes(BROWSER *browserp)
{
	browserp->n_services_changed = 0;
    browserp->n_servers_changed = 0;
    browserp->n_nodes_changed = 0;
}
*/
void update_servers(int *tag, DNS_DID *buffer, int *size)
{
int n_services, service_size;
SERVER *servp;
NODE *nodep;
int j;
char str[MAX_NAME], sname[MAX_NAME], *ptr;
int prepareJSONTree();
int prepareJSONHeader();

	if(tag){}
	if(!Node_head)
	{
		Node_head = (NODE *)malloc(sizeof(NODE));
		sll_init((SLL *)Node_head);
	}
	if(First_time)
	{
		First_time = 0;
	}

	if(!*size)
		return;
	if(*(char *)buffer == -1)
	{
		N_servers = 0;
		N_services = 0;
		return;
	}
	buffer->server.n_services = vtohl(buffer->server.n_services);
	buffer->server.pid = vtohl(buffer->server.pid);
	n_services = buffer->server.n_services;

	if(n_services == 1)
	  return;
	strcpy(sname, buffer->server.task);
	if(n_services > 1)
	{
		for(j = 0; j < n_services; j++)
		{
			buffer->services[j].type = vtohl(
				buffer->services[j].type);
			buffer->services[j].status = vtohl(
				buffer->services[j].status);
			buffer->services[j].n_clients = vtohl(
				buffer->services[j].n_clients);
			if((int)strlen(sname) == MAX_TASK_NAME-4-1)
			{
				strcpy(str,buffer->services[j].name);
				if( (ptr = strstr(str,"/CLIENT_LIST")) )
				{
					*ptr = '\0';
					strcpy(sname,str);
				}
			}
		}
	}
	if (!(nodep = find_node(buffer->server.node)))
	{
		if(n_services)
		{
			N_nodes++;
			nodep = (NODE *)malloc(sizeof(NODE));
			strcpy(nodep->name,buffer->server.node);
			nodep->hasSMI = 0;
			nodep->server_head = (SERVER *)malloc(sizeof(SERVER));
			sll_init((SLL *)nodep->server_head);
			sll_insert_queue((SLL *)Node_head,(SLL *)nodep);
		}
	}
	if (!(servp = find_server(nodep,buffer->server.pid)))
	{
		if(n_services)
		{
			servp = (SERVER *)malloc(sizeof(SERVER));
			strcpy(servp->name,sname);
			servp->next = 0;
			servp->busy = 0;
			servp->server.n_services = 0;
			servp->service_ptr = 0;
			servp->isSMI = 0;
			if(strstr(sname,"_SMI"))
			{
				servp->isSMI = 1;
				nodep->hasSMI = 1;
			}
			sll_insert_queue((SLL *)nodep->server_head,(SLL *)servp);
		}
	}
	if(n_services != 0)
	{
		if(n_services == servp->server.n_services)
		{
			return;
		}
		if(servp->server.n_services == 0)
			N_servers++;
		if(servp->server.n_services != -1)
			N_services -= servp->server.n_services;
		memcpy(&servp->server,&buffer->server,sizeof(DNS_SERVER_INFO));
		if(servp->service_ptr)
		{
			free(servp->service_ptr);
			servp->service_ptr = 0;
		}
		if(n_services != -1)
		{
			service_size = n_services*(int)sizeof(DNS_SERVICE_INFO);
			servp->service_ptr = (DNS_SERVICE_INFO *)malloc((size_t)service_size);
			memcpy(servp->service_ptr, buffer->services, (size_t)service_size);
			N_services += n_services;
		}
		servp->busy = 1;
	}
	else
	{
	  if(servp)
	    {
		N_servers--;
		if(servp->server.n_services != -1)
		  {
			N_services -= servp->server.n_services;
		  }
		servp->server.n_services = 0;
		servp->busy = -1;
		servp->isSMI = 0;
	    }
	}
	if(JSONHeader[0])
	  prepareJSONHeader();
}

void got_update_services(BROWSER **tag, char *buffer, int *size)
{
	BROWSER *browserp;

	if(size){}
	browserp = (BROWSER *)*tag;
	if(browserp->service_format_ptr)
		free(browserp->service_format_ptr);
	browserp->service_format_ptr = (char *)malloc(strlen(buffer)+1);
	strcpy(browserp->service_format_ptr, buffer);
}

char *update_services(char *node, char *server, int pid, int browser)
{
	char str[MAX_NAME];
	NODE *nodep;
	SERVER *servp;
	char *ptr = 0;
	BROWSER *browserp;
	char *prepareJSONServiceList();
	BROWSER *create_browser();

	if(!(browserp = find_browser(browser)))
		browserp = create_browser(browser);

	if(server){}
	sprintf(str,"%s/SERVICE_LIST",server);
	dic_info_service(str,ONCE_ONLY,20,0,0,
		got_update_services,(dim_long)browserp,"None",5);
	if((nodep = find_node(node)))
	{
	    if((servp = find_server(nodep, pid)))
		{
			ptr = prepareJSONServiceList(servp, node, pid, browserp);
		}
	}
	return ptr;
}

void got_update_smi_objects(BROWSER **tag, char *buffer, int *size)
{
	BROWSER *browserp;

	if(size){}
	browserp = (BROWSER *)*tag;
	if(browserp->service_format_ptr)
		free(browserp->service_format_ptr);
	browserp->service_format_ptr = (char *)malloc(strlen(buffer)+1);
	strcpy(browserp->service_format_ptr, buffer);
}

char *update_smi_objects(char *node, char *server, int pid, int browser)
{
	char str[MAX_NAME];
	NODE *nodep;
	SERVER *servp;
	char *ptr = 0;
	BROWSER *browserp;
	char *prepareJSONSmiObjectList();
	BROWSER *create_browser();

	if(!(browserp = find_browser(browser)))
	{
		browserp = create_browser(browser);
		browserp->isSMI = 1;
	}
	if(server){}
	sprintf(str,"%s/SERVICE_LIST",server);
	dic_info_service(str,ONCE_ONLY,20,0,0,
		got_update_smi_objects,(dim_long)browserp,"None",5);
	if((nodep = find_node(node)))
	{
	    if((servp = find_server(nodep, pid)))
		{
			ptr = prepareJSONSmiObjectList(servp, node, pid, browserp);
		}
	}
	return ptr;
}

void get_curr_service_format()
{
	char *format;
	char *dic_get_format();
/*
	char str[256], *ptr, *ptr1;
	int rpc_flag;

	strcpy(str,Curr_service_name);
	rpc_flag = 0;
	if( (ptr = strstr(str,"/RpcIn")) )
	{
		*ptr = '\0';
		rpc_flag = 1;
	}
	if( (ptr = strstr(str,"/RpcOut")) )
	{
		*ptr = '\0';
		rpc_flag = 2;
	}
	strcat(str,"|");
*/
	format = dic_get_format(0);
/*
	if( (ptr = strstr(Curr_service_list,str)) )
	{
		if(!rpc_flag)
		{
		    ptr += (int)strlen(str);
		    ptr1 = strchr(ptr,'|');
		}
		else if(rpc_flag == 1)
		{
		    ptr += (int)strlen(str);
		    ptr1 = strchr(ptr,',');
		}
		else
		{
		    ptr += (int)strlen(str);
		    ptr = strchr(ptr,',');
		    ptr++;
		    ptr1 = strchr(ptr,'|');
		}
	    strncpy(Curr_service_format,ptr,(int)(ptr1 - ptr));
	    Curr_service_format[(int)(ptr1-ptr)] = '\0';
	}
*/
	if(format)
		strcpy(Curr_service_format,format);
	else
		Curr_service_format[0] = '\0';
}

void get_service_format(char *buffer, char *service, char *format)
{
	char str[256], *ptr, *ptr1;
	int rpc_flag;

	strcpy(str, service);
	rpc_flag = 0;
	*format = '\0';
	if( (ptr = strstr(str,"/RpcIn")) )
	{
		*ptr = '\0';
		rpc_flag = 1;
	}
	if( (ptr = strstr(str,"/RpcOut")) )
	{
		*ptr = '\0';
		rpc_flag = 2;
	}
	strcat(str,"|");
	if( (ptr = strstr(buffer, str)) )
	{
		if(!rpc_flag)
		{
		    ptr += (int)strlen(str);
		    ptr1 = strchr(ptr,'|');
		}
		else if(rpc_flag == 1)
		{
		    ptr += (int)strlen(str);
		    ptr1 = strchr(ptr,',');
		}
		else
		{
		    ptr += (int)strlen(str);
		    ptr = strchr(ptr,',');
		    ptr++;
		    ptr1 = strchr(ptr,'|');
		}
	    strncpy(format,ptr,(size_t)(ptr1 - ptr));
	    format[(int)(ptr1-ptr)] = '\0';
	}
}

int delete_curr_service(CURR_SERVICE *servicep)
{

if(WebDID_Debug)
printf("\nUnsubscribing %s\n\n",servicep->name); 
	dic_release_service(servicep->sid);
	if(servicep->buffer_size)
		free(servicep->buffer);
	if(servicep->buffer_str_size)
		free(servicep->buffer_str);
	sll_remove((SLL *)Curr_service_head, (SLL *)servicep);
	free(servicep);
	return(1);
}

int delete_browser(BROWSER *browserp)
{
	CURR_SERVICE *servicep;

	if((servicep = browserp->servicep))
	{
		servicep->n_browsers--;
		if(!servicep->n_browsers)
			delete_curr_service(servicep);
	}
	if(browserp->service_format_ptr)
		free(browserp->service_format_ptr);
	sll_remove((SLL *)Browser_head, (SLL *)browserp);
	free(browserp);
	return(1);
}

void check_browser( BROWSER *tag)
{
	BROWSER *browserp;
	time_t tsecs;

	browserp = (BROWSER *)tag;
if(WebDID_Debug)
printf("\nCheck_browser %d\n",browserp->id); 
	tsecs = time((time_t *)0);
	if((tsecs - browserp->last_polled) > 20)
	{
if(WebDID_Debug)
printf("\nDeleting browser %d\n\n",browserp->id); 
		delete_browser(browserp);
	}
	else
		dtq_start_timer(10, check_browser, browserp);
}

BROWSER *create_browser(int id)
{
	BROWSER *browserp;

	if(!Browser_head)
	{
		Browser_head = (BROWSER *)malloc(sizeof(BROWSER));
		sll_init((SLL *)Browser_head);
	}
	browserp = (BROWSER *)malloc(sizeof(BROWSER));
	browserp->id = id;
    browserp->last_subscribed = 0;
	browserp->last_updated = 0;
	browserp->last_polled = 0;
    browserp->last_changed = 0;
	browserp->n_nodes = 0;
	browserp->n_servers = 0;
	browserp->n_services = 0;
	browserp->servicep = 0;
	browserp->JSONBuffer = 0;
	browserp->JSONBufferSize = 0;
	browserp->JSONSmiBuffer = 0;
	browserp->JSONSmiBufferSize = 0;
	browserp->pattern[0] = '\0';
	browserp->service_format_ptr = 0;
	browserp->curr_command[0] = '\0';
	browserp->curr_smidomain[0] = '\0';
	browserp->smidomainp = 0;
	sll_insert_queue((SLL *)Browser_head,(SLL *)browserp);
	dtq_start_timer(10, check_browser, browserp);
	return browserp;
}

int update_command_data(char *service, int conn_id, BROWSER *browserp)
{
	char format[MAX_NAME];
	char answer[MAX_NAME*3];
	extern void sendData();

	if(browserp->service_format_ptr)
	{
		get_service_format(browserp->service_format_ptr, service, format);
if(WebDID_Debug)
printf("CMD data %s %s\n",service,format);
	}
	else
	{
		strcpy(format,"?");
	}
	strcpy(browserp->curr_command, service);
	sprintf(answer,"To %s (%s)",service, format);
	sendData(conn_id, answer, 4);
	return 1;
}

int update_service_data(char *service, int conn_id, int subscribe, int req, int browser, int force)
{
	CURR_SERVICE *servicep;
	time_t tsecs;
	void recv_service_info();
	extern void sendData();
	BROWSER *browserp;

	if(req){}
	if(!Curr_service_head)
	{
		Curr_service_head = (CURR_SERVICE *)malloc(sizeof(CURR_SERVICE));
		sll_init((SLL *)Curr_service_head);
	}
	if(!(browserp = find_browser(browser)))
		browserp = create_browser(browser);
	if(force == -1)
	{
		update_command_data(service, conn_id, browserp);
		return 1;
	}
	if((servicep = browserp->servicep))
	{
		servicep->n_browsers--;
		if(!servicep->n_browsers)
			delete_curr_service(servicep);
	}
	if(!(servicep = find_curr_service(service)))
	{
		servicep = (CURR_SERVICE *)malloc(sizeof(CURR_SERVICE));
		strcpy(servicep->name,service);
		servicep->conn_id = conn_id;
		servicep->buffer = 0;
		servicep->buffer_size = 0;
		servicep->size = 0;
		servicep->buffer_str = 0;
		servicep->buffer_str_size = 0;
		servicep->str_size = 0;
		servicep->last_updated = 0;
		tsecs = time((time_t *)0);
		browserp->last_subscribed = tsecs;
		browserp->last_updated = tsecs;
		servicep->last_subscribed = tsecs;
		servicep->n_browsers = 0;
		sll_insert_queue((SLL *)Curr_service_head,(SLL *)servicep);
		servicep->sid = (int)dic_info_service_stamped( service, MONITORED, subscribe, 0, 0,
			recv_service_info, servicep, &no_link_int, 4);
	}
	else
	{
		if(servicep->size)
		{
			if((servicep->timestamp > browserp->last_updated) || (force))
			{
				sendData(conn_id, servicep->buffer_str, 4);
			}
			else
			{
				sendData(conn_id, "", 4);
			}
			browserp->last_updated = servicep->timestamp;
		}
	}
	if(force)
	{
		browserp->servicep = servicep;
		servicep->n_browsers++;
	}
	return 1;
}

int check_browser_changes(char *service, int conn_id, int subscribe, int req, int browser, int force)
{
	CURR_SERVICE *servicep;
	time_t tsecs;
	void recv_service_info();
	extern void sendData();
	BROWSER *browserp;
	char answer[256];
	int service_changed = 0;

	if(req){}
	if(subscribe){}
	if(!(browserp = find_browser(browser)))
		browserp = create_browser(browser);
	if(!Curr_service_head)
	{
		Curr_service_head = (CURR_SERVICE *)malloc(sizeof(CURR_SERVICE));
		sll_init((SLL *)Curr_service_head);
	}
	if(service[0] != '\0')
	{
	    if((servicep = find_curr_service(service)))
		{
			if(servicep->size)
			{
				if((servicep->timestamp > browserp->last_updated) || (force))
				{
					service_changed = 1;
				}
			}
		}
	}
	if(browserp->isSMI)
	{
		if((browserp->last_changed >= browserp->last_polled) || (force))
		{
				service_changed = 1;
		}
	}
/*
	sprintf(answer,"%d %d %d %d\n",
		browserp->n_services_changed, browserp->n_servers_changed, 
		browserp->n_nodes_changed, service_changed);
*/
	sprintf(answer,"%d %d %d %d %d %d %d\n",
		N_services, N_servers, N_nodes, service_changed,
		browserp->n_services, browserp->n_servers, browserp->n_nodes);
	sendData(conn_id, answer, 4);
	tsecs = time((time_t *)0);
	browserp->last_polled = tsecs;
	return 1;
}

int find_services(char *pattern, int conn_id, int browser, int force)
{
	void recv_service_info();
	extern void sendData();
	BROWSER *browserp;
	char format[MAX_NAME];
	int prepareJSONTree();
	void did_prepare_command();

	if(conn_id){}
	if(!(browserp = find_browser(browser)))
		browserp = create_browser(browser);
	if(force == -1)
	{
		if(browserp->service_format_ptr)
		{
			get_service_format(browserp->service_format_ptr, browserp->curr_command, format);
			did_prepare_command(pattern, browserp->curr_command, format);
		}
		return 1;
	}
	if(conn_id){}
	if(!(browserp = find_browser(browser)))
		browserp = create_browser(browser);
	strcpy(browserp->pattern, pattern);
	return 1;
}

void recv_service_info(void **tag, int *buffer, int *size)
{
	int conn_id;
	void print_service_formatted();
	extern void sendData();
	CURR_SERVICE *servicep;
	time_t tsecs;

	servicep = *tag;
	conn_id = servicep->conn_id;
	if (servicep->buffer_size < *size)
	{
		if(servicep->buffer_size)
			free(servicep->buffer);
		servicep->buffer = malloc((size_t)*size);
		servicep->buffer_size = *size;
	}
	memcpy(servicep->buffer, (char *)buffer, (size_t)*size);
	servicep->size = *size;
	if (servicep->buffer_str_size < (1024 + (*size)*16))
	{
		if(servicep->buffer_str_size)
			free(servicep->buffer_str);
		servicep->buffer_str = malloc((size_t)(1024 + (*size)*16));
		servicep->buffer_str_size = 1024 + (*size)*16;
	}
	Service_content_str = servicep->buffer_str;
	strcpy(Curr_service_name, servicep->name);
	get_curr_service_format();
	if((*size == 4 ) && (*buffer == -1))
	{
		sprintf(Service_content_str,
			"Service %s Not Available", Curr_service_name);
	}
	else
	{
		print_service_formatted(servicep, buffer, *size);
	}
	if(servicep->last_updated == 0)
	{
		sendData(conn_id, Service_content_str, 4);
		tsecs = time((time_t *)0);
		servicep->last_updated = tsecs;
	}
}

void print_service_formatted(CURR_SERVICE *servicep, void *buff, int size)
{
char type;
int num, ret;
char str[256];
char *ptr;
void *buffer_ptr;
char timestr[256], aux[64], sizestr[64];
int quality = 0, secs = 0, mili = 0; 
int did_write_string(char, int, void **, int);
time_t tsecs;

	if(size < 1024)
		sprintf(sizestr,"%d bytes",size);
	else if (size < 1024*1024)
		sprintf(sizestr,"%2.2f Kb",(float)size/1024);
	else
		sprintf(sizestr,"%2.2f Mb",(float)size/(1024*1024));

	sprintf(Service_content_str,
	  "<FONT FACE=\"consolas\">Service %s (%s) Contents :<br />  <br />", Curr_service_name,
	  Curr_service_format);
    dic_get_timestamp(0, &secs, &mili);
    quality = dic_get_quality(0);
	tsecs = secs;
	servicep->timestamp = tsecs;
	my_ctime(&tsecs, timestr, 128);
    ptr = strrchr(timestr,' ');
    strcpy(aux, ptr);
    sprintf(ptr,".%03d",mili);
    strcat(timestr, aux);
    timestr[(int)strlen(timestr)-1] = '\0';
   
    sprintf(str," Timestamp: %s&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp Quality: %d&nbsp&nbsp&nbsp Size: %s<br /><br />",
	  timestr, quality, sizestr);

   strcat(Service_content_str,str);
   ptr = Curr_service_format;
   buffer_ptr = buff;
   while(*ptr)
   { 
     type = *ptr++;
     if(*ptr == ':')
     {
		ptr++;
		sscanf(ptr, "%d", &num);
		ret = did_write_string(type, num, &buffer_ptr, size);
		size -= ret;
		if( (ptr = strchr(ptr,';')) )
			ptr++;
		else
			break;
	 }
     else
     {
		ret = did_write_string(type, 0, &buffer_ptr, size);
		size -= ret;
		break;
     }
   }
   strcat(Service_content_str,"</FONT>");
}


int did_write_string(char type, int num, void **buffer_ptr, int ssize)
{
void *ptr;
int size, psize;

  void print_service_standard();
  void print_service_char();
  void print_service_short();
  void print_service_float();
  void print_service_double();

  ptr = *buffer_ptr;
  switch(type)
    {
    case 'L':
    case 'l':
      strcat(Service_content_str," L");
      if(!num)
		size = ssize/(int)sizeof(int);
      else
		size = num;
      psize = size * (int)sizeof(int);
      print_service_standard(ptr, size);
      break;
    case 'I':
    case 'i':
      strcat(Service_content_str," I");
      if(!num)
		size = ssize/(int)sizeof(int);
      else
		size = num;
      psize = size * (int)sizeof(int);
      print_service_standard(ptr, size);
      break;
    case 'S':
    case 's':
      strcat(Service_content_str," S");
      if(!num)
		size = ssize/(int)sizeof(short);
      else
		size = num;
      psize = size * (int)sizeof(short);
      print_service_short(ptr, size);
      break;
    case 'F':
    case 'f':
      strcat(Service_content_str," F");
      if(!num)
		size = ssize/(int)sizeof(float);
      else
		size = num;
      psize = size * (int)sizeof(float);
      print_service_float(ptr, size);
      break;
    case 'D':
    case 'd':
      strcat(Service_content_str," D");
      if(!num)
		size = ssize/(int)sizeof(double);
      else
		size = num;
      psize = size * (int)sizeof(double);
      print_service_double(ptr, size);
      break;
    case 'X':
    case 'x':
      strcat(Service_content_str," X");
      if(!num)
		size = ssize/(int)sizeof(longlong);
      else
		size = num;
      psize = size * (int)sizeof(longlong);
      print_service_standard(ptr, size*2);
      break;
    case 'C':
    case 'c':
    default:
      strcat(Service_content_str," C");
      if(!num)
		size = ssize;
      else
		size = num;
      psize = size;
      print_service_char(ptr, size);
    }
  ptr = (char *)ptr + psize;
  *buffer_ptr = ptr;
  return psize;
}

void sprintf_html(char *str, int n, int value)
{
	char tmp[80];
	int min, i;

	str[0] = '\0';
	min = sprintf(tmp,"%d",value);
	for(i = 0; i < (n-min); i++)
	{
		strcat(str,"&nbsp");
	}
	strcat(str, tmp);
}

void print_service_standard(int *buff, int size)
{
int i,j;
char *ptr, str[80], tmp[256];
int last[4];

	ptr = Service_content_str;
	ptr += (int)strlen(Service_content_str);
	for( i = 0; i < size; i++)
	{
	  strcpy(tmp,"");
		if(i%4 == 0)
		{
		  if(i != 0)
		    {
			strcat(tmp,"&nbsp");
		    }
			sprintf_html(str, 7, i);
			strcat(tmp,str);
		}
		if(!(i%4))
			strcat(tmp,"H: ");
		sprintf(str,"&nbsp&nbsp&nbsp %08X",buff[i]);
		strcat(tmp,str);
		last[i%4] = buff[i];
		if((i%4 == 3) || (i == (size-1)))
		{
			strcat(tmp,"<br />");
			for(j = 0; j <= (i%4); j++)
			{
				if(j == 0)
					strcat(tmp,"&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp D: ");
				sprintf_html(str, 12, last[j]);
				strcat(tmp,str);
			}
			strcat(tmp,"<br />");
		}
		strcpy(ptr, tmp);
		ptr += (int)strlen(tmp);
	}
	strcpy(tmp,"<br />");
	strcpy(ptr, tmp);
}

void print_service_longlong(longlong *buff, int size)
{
int i,j;
char *ptr, str[80], tmp[256];
longlong last[4];

	ptr = Service_content_str;
	ptr += (int)strlen(Service_content_str);
	for( i = 0; i < size; i++)
	{
	  strcpy(tmp,"");
		if(i%4 == 0)
		{
		  if(i != 0)
		    {
			strcat(tmp,"&nbsp");
		    }
			sprintf_html(str, 7, i);
			strcat(tmp,str);
		}
		if(!(i%4))
			strcat(tmp,"H: ");
		sprintf(str,"&nbsp&nbsp&nbsp %08X",(unsigned)buff[i]);
		strcat(tmp,str);
		last[i%4] = buff[i];
		if((i%4 == 3) || (i == (size-1)))
		{
			strcat(tmp,"<br />");
			for(j = 0; j <= (i%4); j++)
			{
				if(j == 0)
					strcat(tmp,"&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp D: ");
				sprintf_html(str, 12, (int)last[j]);
				strcat(tmp,str);
			}
			strcat(tmp,"<br />");
		}
		strcpy(ptr, tmp);
		ptr += (int)strlen(tmp);
	}
	strcpy(tmp,"<br />");
	strcpy(ptr, tmp);
}

void print_service_short(short *buff, int size)
{
int i,j;
char *ptr, str[80], tmp[256];
short last[8];

	ptr = Service_content_str;
	ptr += (int)strlen(Service_content_str);
	for( i = 0; i < size; i++)
	{
	  strcpy(tmp,"");
		if(i%8 == 0)
		{
		  if(i != 0)
		    {
			strcat(tmp,"&nbsp");
		    }
			sprintf_html(str, 7, i);
			strcat(tmp,str);
		}
		if(!(i%8))
			strcat(tmp,"H: ");
		sprintf(str,"&nbsp %04X",buff[i]);
		strcat(tmp,str);
		last[i%8] = buff[i];
		if((i%8 == 7) || (i == (size-1)))
		{
			strcat(tmp,"<br />");
			for(j = 0; j <= (i%8); j++)
			{
				if(j == 0)
					strcat(tmp,"&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp D: ");
				sprintf_html(str, 6, last[j]);
				strcat(tmp,str);
			}
			strcat(tmp,"<br />");
		}
		strcpy(ptr, tmp);
		ptr += (int)strlen(tmp);
	}
	strcpy(tmp,"<br />");
	strcpy(ptr, tmp);
}

void print_service_char(char *buff, int size)
{
int i,j;
char *asc, *ptr, str[80], tmp[256];

	asc = (char *)buff;
	ptr = Service_content_str;
	ptr += (int)strlen(Service_content_str);
	for( i = 0; i < size; i++)
	{
	  strcpy(tmp,"");
		if(i%16 == 0)
		{
		  if(i != 0)
		    {
			strcat(tmp,"&nbsp");
		    }
			sprintf_html(str, 7, i);
			strcat(tmp,str);
		}
		if(!(i%16))
			strcat(tmp,"H: ");
		sprintf(str,"%02X",buff[i]);
/*		strcat(tmp,str);
*/
		strcat(tmp," ");
		strcat(tmp,&str[(int)strlen(str)-2]);
		/*
		if(i%4 == 3)
		  strcat(tmp," ");
		*/
		if((i%16 == 15) || (i == (size-1)))
		{
			if(i%16 != 15)
			{
			    for(j = 1; j < 16 - (i%16); j++)
				strcat(tmp,"&nbsp&nbsp ");
			}
			strcat(tmp,"&nbsp&nbsp&nbsp '");
			for(j = 0; j <= (i%16) ; j++)
			{
				if(isprint(asc[j]))
				{
					if(asc[j] == ' ')
						sprintf(str,"&nbsp");
					else if(asc[j] == '<')
						sprintf(str,"&lt");
					else if(asc[j] == '>')
						sprintf(str,"&gt");
					else if(asc[j] == '&')
						sprintf(str,"&amp");
					else
						sprintf(str,"%c",asc[j]);
					strcat(tmp,str);
				}
				else
				{
					sprintf(str,".");
					strcat(tmp,str);
				}
			}
			strcat(tmp,"'<br />");
			asc = (char *)&buff[i+1];
		}
		strcpy(ptr, tmp);
		ptr += (int)strlen(tmp);
	}
	strcpy(tmp,"<br />");
	strcpy(ptr, tmp);
}

void print_service_float(float *buff, int size)
{
int i;
char *ptr, str[80], tmp[256];

	ptr = Service_content_str;
	ptr += (int)strlen(Service_content_str);
	for( i = 0; i < size; i++)
	{
	  strcpy(tmp,"");
		if(i%4 == 0)
		{
		  if(i != 0)
		    {
			strcat(tmp,"&nbsp");
		    }
			sprintf_html(str, 7, i);
			strcat(tmp,str);
		}
		sprintf(str,"%12.3G",*(buff++));
		strcat(tmp,str);
		if((i%4 == 3) || (i == size-1))
		{
			strcat(tmp,"<br />");
		}
		strcpy(ptr, tmp);
		ptr += (int)strlen(tmp);
	}
	strcpy(tmp,"<br />");
	strcpy(ptr, tmp);
	ptr += (int)strlen(tmp);
}

void print_service_double(double *buff, int size)
{
int i;
char *ptr, str[80], tmp[256];

    ptr = Service_content_str;
	ptr += (int)strlen(Service_content_str);
	for( i = 0; i < size; i++)
	{
	  strcpy(tmp,"");
		if(i%4 == 0)
		{
		  if(i != 0)
		    {
			strcat(tmp,"&nbsp");
		    }
			sprintf_html(str, 7, i);
			strcat(tmp,str);
		}
		sprintf(str,"%12.3G",*(buff++));
		strcat(tmp,str);
		if((i%4 == 3) || (i == size-1))
		{
			strcat(tmp,"<br />");
		}
		strcpy(ptr, tmp);
		ptr += (int)strlen(tmp);
	}
	strcpy(tmp,"<br />");
	strcpy(ptr, tmp);
	ptr += (int)strlen(tmp);
}

char *addJSONStart(char *ptr)
{
	char *ptro;

	strcat(ptr,"{\n");
	ptro = ptr + (int)strlen(ptr);
	return ptro;
}

char *addJSONEnd(char *ptr)
{
	char *ptro;

	strcat(ptr,"}\n");
	ptro = ptr + (int)strlen(ptr);
	return ptro;
}

char *addJSONNodeStart(char *ptr, char *node)
{
	char *ptro;

	sprintf(ptr,"%s: [\n", node);
	ptro = ptr + (int)strlen(ptr);
	return ptro;
}

char *addJSONNodeEnd(char *ptr)
{
	char *ptro;

	strcat(ptr,"]\n");
	ptro = ptr + (int)strlen(ptr);
	return ptro;
}

char *addJSONChildStart(char *ptr, char *child, int sep)
{
	char *ptro;

	if(sep)
		sprintf(ptr,"{ %s, ", child);
	else
		sprintf(ptr,"{ %s", child);
	ptro = ptr + (int)strlen(ptr);
	return ptro;
}

char *addJSONChildEnd(char *ptr, int sep)
{
	char *ptro;

	if(sep)
		strcat(ptr," },\n");
	else
		strcat(ptr," }\n");
	ptro = ptr + (int)strlen(ptr);
	return ptro;
}

char *getJSONBuffer(char *node, int browser)
{
	BROWSER *browserp;
	int prepareJSONTree();

	if(browser)
	{
	    if((browserp = find_browser(browser)))
		{
			if(browserp->pattern[0] != '\0')
			{
			  prepareJSONTree(node, browserp);
			  return(browserp->JSONBuffer);
			}
			browserp->n_services = 0;
			browserp->n_servers = 0;
			browserp->n_nodes = 0;
		}
	}
	prepareJSONTree(node, 0);
	return(JSONBuffer);
}

char *getJSONSmiBuffer(char *node, int browser)
{
	BROWSER *browserp;
	int prepareJSONSmiTree();

	if(!(browserp = find_browser(browser)))
	{
		browserp = create_browser(browser);
		browserp->isSMI = 1;
		strcpy(browserp->pattern,"SMI/");
	}
	if(browser)
	{
	    if((browserp = find_browser(browser)))
		{
			if(browserp->pattern[0] != '\0')
			{
			  prepareJSONSmiTree(node, browserp);
			  return(browserp->JSONSmiBuffer);
			}
//			browserp->n_services = 0;
//			browserp->n_servers = 0;
//			browserp->n_nodes = 0;
		}
	}
	prepareJSONSmiTree(node, 0);
	return(JSONSmiBuffer);
}

char *getJSONHeader(int isSMI)
{
  int prepareJSONHeader();

  if(isSMI){}
  if(JSONHeader[0] == '\0')
    prepareJSONHeader();
  return(JSONHeader);
}

int getNodeLabel(char *name, char *label)
{
	int i;
	extern int web_get_node_name();

	web_get_node_name(name, label);
	for(i = 0; i < ((int)strlen(label) + 1); i++)
	{
		label[i] = (char)tolower((int)label[i]);
		if(label[i] == '.')
		{
		    label[i] = '\0';
		    break;
		}
	}
	return 1;
}

int prepareJSONTree(char *node, BROWSER *browserp)
{
	char *ptr;
	NODE *nodep;
	SERVER *servp;
	char str[256], aux[128];
	int selective = 0;
	int n_nodes, tot_n_nodes;
	int n_servers, tot_n_servers;
	int ret, n_found = 0;

	if(browserp)
	{
		if(browserp->pattern[0] != '\0')
			selective = 1;
		else
			return(0);
	}
	if(!selective)
	{
		if(JSONBufferSize == 0)
		{
			JSONBuffer = malloc((size_t)(N_nodes*128+N_servers*128));
		}
		else if (JSONBufferSize < N_nodes*128+N_servers*128)
		{
			free(JSONBuffer);
			JSONBuffer = malloc((size_t)(N_nodes*128+N_servers*128));
		}
		ptr = JSONBuffer;
	}
	else
	{
		if(browserp->JSONBufferSize == 0)
		{
			browserp->JSONBuffer = malloc((size_t)(N_nodes*128+N_servers*128));
		}
		else if (browserp->JSONBufferSize < N_nodes*128+N_servers*128)
		{
			free(browserp->JSONBuffer);
			browserp->JSONBuffer = malloc((size_t)(N_nodes*128+N_servers*128));
		}
		ptr = browserp->JSONBuffer;
	}
	*ptr = '\0';
	if(!strcmp(node, "src"))
	{
	ptr = addJSONStart(ptr);
	ptr = addJSONNodeStart(ptr,"children");
		sprintf(str,"text: \"%s\", id: \"Nodes\", expanded: false", Title);
			ptr = addJSONChildStart(ptr,str,1);
		ptr = addJSONNodeStart(ptr,"children");
	nodep = Node_head;
	tot_n_nodes = 0;
	while( (nodep = (NODE *)sll_get_next((SLL *)nodep)) )
	{
		nodep->match = 1;
		if(selective)
		{
			if(!(ret = find_service_pattern(nodep, 0, browserp->pattern, &tot_n_servers)))
			{
				nodep->match = 0;
				continue;
			}
			else
			{
				n_found += ret;
			}
		}
		tot_n_nodes++;
	}
	n_nodes = 0;
	nodep = Node_head;
	while( (nodep = (NODE *)sll_get_next((SLL *)nodep)) )
	{
		if(!nodep->match)
			continue;
		getNodeLabel(nodep->name, aux);
		sprintf(str,"text: \"%s\", id: \"%s\", qtip: \"%s\"",
			aux, nodep->name, nodep->name);
		ptr = addJSONChildStart(ptr,str,0);
		n_nodes++;
if(WebDID_Debug)
		printf("adding %s %d %d\n",nodep->name, n_nodes, tot_n_nodes);
		if(n_nodes < tot_n_nodes)
			ptr = addJSONChildEnd(ptr,1);
		else
			ptr = addJSONChildEnd(ptr,0);
	}
		ptr = addJSONNodeEnd(ptr);
		ptr = addJSONChildEnd(ptr,0);
	ptr = addJSONNodeEnd(ptr);
	ptr = addJSONEnd(ptr);
	if(selective)
	{
		browserp->n_services = n_found;
		browserp->n_servers = tot_n_servers;
		browserp->n_nodes = tot_n_nodes;
	}
	}
	else
	{
	  if((nodep = find_node(node)))
	  {
	ptr = addJSONStart(ptr);
	ptr = addJSONNodeStart(ptr,"children");
		servp = nodep->server_head;
		tot_n_servers = 0;
		while( (servp = (SERVER *)sll_get_next((SLL *)servp)) )
		{
			servp->match = 1;
			if(servp->busy != 1)
			{
				servp->match = 0;
				continue;
			}
			if(selective)
			{
				if(!(ret = find_service_pattern(nodep, servp, browserp->pattern, 0)))
				{
					servp->match = 0;
					continue;
				}
				else
				{
					n_found += ret;
				}
			}
			tot_n_servers++;
		}
		n_servers = 0;
		servp = nodep->server_head;
		while( (servp = (SERVER *)sll_get_next((SLL *)servp)) )
		{
			if(!servp->match)
				continue;
			sprintf(str,"text: \"%s\", id: \"%d\", leaf: true, icon: \"server.png\"",servp->name, servp->server.pid);
			ptr = addJSONChildStart(ptr,str,0);
			n_servers++;
			if(n_servers < tot_n_servers)
				ptr = addJSONChildEnd(ptr,1);
			else
				ptr = addJSONChildEnd(ptr,0);
		}
	ptr = addJSONNodeEnd(ptr);
	ptr = addJSONEnd(ptr);
	  }
	}
	/*
if(!selective)
printf(" Nodes&Servers %s\n",JSONBuffer);
else
printf(" Nodes&Servers %s\n",browserp->JSONBuffer);
	*/
	return(1);
}

int prepareJSONSmiTree(char *node, BROWSER *browserp)
{
	char *ptr;
	NODE *nodep;
	SERVER *servp;
	char str[256], aux[128];
	int selective = 0;
	int n_nodes, tot_n_nodes;
	int n_servers, tot_n_servers;
	int ret, n_found = 0;
	char pattern[256] = {'\0'};
	char *sptr;

	if(browserp)
	{
		if(browserp->pattern[0] != '\0')
		{
			selective = 1;
			strcpy(pattern, browserp->pattern);
		}
//		else
//			return(0);
	}
//	selective = 1;
//	strcpy(pattern,"SMI/*");
	if(!selective)
	{
		if(JSONSmiBufferSize == 0)
		{
			JSONSmiBuffer = malloc((size_t)(N_nodes*128+N_servers*128));
		}
		else if (JSONSmiBufferSize < N_nodes*128+N_servers*128)
		{
			free(JSONSmiBuffer);
			JSONSmiBuffer = malloc((size_t)(N_nodes*128+N_servers*128));
		}
		ptr = JSONSmiBuffer;
	}
	else
	{
		if(browserp->JSONSmiBufferSize == 0)
		{
			browserp->JSONSmiBuffer = malloc((size_t)(N_nodes*128+N_servers*128));
		}
		else if (browserp->JSONSmiBufferSize < N_nodes*128+N_servers*128)
		{
			free(browserp->JSONSmiBuffer);
			browserp->JSONSmiBuffer = malloc((size_t)(N_nodes*128+N_servers*128));
		}
		ptr = browserp->JSONSmiBuffer;
	}
	*ptr = '\0';
	if(!strcmp(node, "src"))
	{
	ptr = addJSONStart(ptr);
	ptr = addJSONNodeStart(ptr,"children");
		sprintf(str,"text: \"%s\", id: \"Nodes\", expanded: false", Title);
			ptr = addJSONChildStart(ptr,str,1);
		ptr = addJSONNodeStart(ptr,"children");
	nodep = Node_head;
	tot_n_nodes = 0;
	while( (nodep = (NODE *)sll_get_next((SLL *)nodep)) )
	{
		nodep->match = 1;
		if(selective)
		{
			if(!(ret = find_service_pattern(nodep, 0, pattern, &tot_n_servers)))
			{
				nodep->match = 0;
				continue;
			}
			else
			{
				n_found += ret;
			}
		}
		tot_n_nodes++;
	}
	n_nodes = 0;
	nodep = Node_head;
	while( (nodep = (NODE *)sll_get_next((SLL *)nodep)) )
	{
		if(!nodep->match)
			continue;
		getNodeLabel(nodep->name, aux);
		sprintf(str,"text: \"%s\", id: \"%s\", qtip: \"%s\"",
			aux, nodep->name, nodep->name);
		ptr = addJSONChildStart(ptr,str,0);
		n_nodes++;
if(WebDID_Debug)
		printf("adding %s %d %d\n",nodep->name, n_nodes, tot_n_nodes);
		if(n_nodes < tot_n_nodes)
			ptr = addJSONChildEnd(ptr,1);
		else
			ptr = addJSONChildEnd(ptr,0);
	}
		ptr = addJSONNodeEnd(ptr);
		ptr = addJSONChildEnd(ptr,0);
	ptr = addJSONNodeEnd(ptr);
	ptr = addJSONEnd(ptr);
	if(selective)
	{
		browserp->n_services = n_found;
		browserp->n_servers = tot_n_servers;
		browserp->n_nodes = tot_n_nodes;
	}
	}
	else
	{
	  if((nodep = find_node(node)))
	  {
	ptr = addJSONStart(ptr);
	ptr = addJSONNodeStart(ptr,"children");
		servp = nodep->server_head;
		tot_n_servers = 0;
		while( (servp = (SERVER *)sll_get_next((SLL *)servp)) )
		{
			servp->match = 1;
			if(servp->busy != 1)
			{
				servp->match = 0;
				continue;
			}
			if(selective)
			{
				if(!(ret = find_service_pattern(nodep, servp, pattern, 0)))
				{
					servp->match = 0;
					continue;
				}
				else
				{
					n_found += ret;
				}
			}
			tot_n_servers++;
		}
		n_servers = 0;
		servp = nodep->server_head;
		while( (servp = (SERVER *)sll_get_next((SLL *)servp)) )
		{
			if(!servp->match)
				continue;
			strcpy(aux, servp->name);
			sptr = strstr(aux,"_SMI");
			if(sptr)
				*sptr = '\0';
			sprintf(str,"text: \"%s\", id: \"%d\", leaf: true, icon: \"server.png\", name: \"%s\"",aux, servp->server.pid, servp->name);
			ptr = addJSONChildStart(ptr,str,0);
			n_servers++;
			if(n_servers < tot_n_servers)
				ptr = addJSONChildEnd(ptr,1);
			else
				ptr = addJSONChildEnd(ptr,0);
		}
	ptr = addJSONNodeEnd(ptr);
	ptr = addJSONEnd(ptr);
	  }
	}
	/*
if(!selective)
printf(" Nodes&Servers %s\n",JSONBuffer);
else
printf(" Nodes&Servers %s\n",browserp->JSONBuffer);
	*/
printf("%s\n",browserp->JSONSmiBuffer);
	return(1);
}

int prepareJSONHeader()
{
	char *ptr;
	char str[128];

	ptr = JSONHeader;
	*ptr = '\0';
	ptr = addJSONStart(ptr);
	ptr = addJSONNodeStart(ptr,"items");
	sprintf(str,"text: \"%s\"",Title);
	ptr = addJSONChildStart(ptr,str,0);
	ptr = addJSONChildEnd(ptr,1);
	sprintf(str,"text: \"%d Servers Known - %d Services Available\"",N_servers, N_services);
	ptr = addJSONChildStart(ptr,str,0);
	ptr = addJSONChildEnd(ptr,0);
	ptr = addJSONNodeEnd(ptr);
	ptr = addJSONEnd(ptr);
if(WebDID_Debug)
printf(" Header %s\n",JSONHeader);
	return(1);
}

char *JSONServices = 0;
int JSONServicesSize = 0;
char *prepareJSONServiceList(SERVER *servp, char *node, int pid, BROWSER *browserp)
{
	DNS_SERVICE_INFO *servicep;
	char *ptr;
	int n_services, i;
	char str[256], type_str[256];
	int selective = 0;
	int n_found = 0, n;

	servicep = servp->service_ptr;
	n_services = servp->server.n_services;
	if(JSONServicesSize == 0)
	{
		JSONServicesSize = n_services*256;
		JSONServices = malloc((size_t)JSONServicesSize);
	}
	else if (JSONServicesSize < n_services*256)
	{
		free(JSONServices);
		JSONServicesSize = n_services*256;
		JSONServices = malloc((size_t)JSONServicesSize);
	}
	if(browserp)
	{
		if(browserp->pattern[0] != '\0')
			selective = 1;
	}
	n_found = n_services;
	if(selective)
	{
		n_found = find_server_service_pattern(servp, browserp->pattern);
	}
	ptr = JSONServices;
	*ptr = '\0';
	ptr = addJSONStart(ptr);
	ptr = addJSONNodeStart(ptr,"children");
	if(selective)
		sprintf(str,"text: \"%s (%d/%d services, pid %d)\"",servp->name, n_found, n_services, servp->server.pid);
	else
		sprintf(str,"text: \"%s (%d services, pid %d)\"",servp->name, n_services, servp->server.pid);
    ptr = addJSONChildStart(ptr,str,1);
	ptr = addJSONNodeStart(ptr,"children");
	servicep = servp->service_ptr;
	n = 0;
	for(i = 0; i < n_services; i++)
	{
/*
printf("Service type = %d\n",servicep->type);
*/
		if((!selective) || (strstr(servicep->name, browserp->pattern)))
		{
			if(servicep->type == 1)
			{
				sprintf(type_str,"%d@%s|%s|CMD", pid, node, servicep->name);
				sprintf(str,"text: \"%s\", id: \"%s\", leaf: true, icon: \"leaf_cmd.gif\"",servicep->name, type_str);
			}
			else
			{
				sprintf(type_str,"%d@%s|%s", pid, node, servicep->name);
				sprintf(str,"text: \"%s\", id: \"%s\", leaf: true",servicep->name, type_str);
			}
			ptr = addJSONChildStart(ptr,str,0);
			n++;
			if(n < n_found)
				ptr = addJSONChildEnd(ptr,1);
			else
				ptr = addJSONChildEnd(ptr,0);
		}
		servicep++;
	}
	ptr = addJSONNodeEnd(ptr);
	ptr = addJSONChildEnd(ptr,0);
	ptr = addJSONNodeEnd(ptr);
	ptr = addJSONEnd(ptr);
	return JSONServices;
}

char *JSONSmiServices = 0;
int JSONSmiServicesSize = 0;

char *prepareJSONSmiObjectList(SERVER *servp, char *node, int pid, BROWSER *browserp)
{
	DNS_SERVICE_INFO *servicep;
	char *ptr;
	int n_services, i;
	char str[512], type_str[512];
	int selective = 0;
	int n_found = 0, n, mode_index;
	char aux[512], *sptr, state[512], *stptr;
	OBJSTATE *smidomainp;
	int findSmiServices();

printf("prepareJSONSmiObjectList name %s\n", servp->name);
	servicep = servp->service_ptr;
	n_services = servp->server.n_services;
	if(JSONSmiServicesSize == 0)
	{
		JSONSmiServicesSize = n_services*512;
		JSONSmiServices = malloc((size_t)JSONSmiServicesSize);
	}
	else if (JSONSmiServicesSize < n_services*512)
	{
		free(JSONSmiServices);
		JSONSmiServicesSize = n_services*512;
		JSONSmiServices = malloc((size_t)JSONSmiServicesSize);
	}
	if(browserp)
	{
		if(browserp->pattern[0] != '\0')
			selective = 1;
	}
	n_found = n_services;
	/*
	if(selective)
	{
		n_found = find_server_service_pattern(servp, browserp->pattern);
	}
	*/

	n_found = findSmiServices(browserp, servp);
	smidomainp = browserp->smidomainp;

printf("prepareJSONSmiObjectList1 name %s\n", servp->name);


	ptr = JSONSmiServices;
	*ptr = '\0';
	ptr = addJSONStart(ptr);
	ptr = addJSONNodeStart(ptr,"children");
	/*
	if(selective)
		sprintf(str,"name: \"%s (%d/%d services, pid %d)\"",servp->name, n_found, n_services, servp->server.pid);
	else
		sprintf(str,"name: \"%s (%d services, pid %d)\"",servp->name, n_services, servp->server.pid);
	*/
	sprintf(str,"name: \"%s (%d objects, pid %d)\"",servp->name, n_found, servp->server.pid);
    ptr = addJSONChildStart(ptr,str,1);
	ptr = addJSONNodeStart(ptr,"children");
	servicep = servp->service_ptr;
	n = 0;
	for(i = 0; i < n_services; i++)
	{
/*
printf("Service type = %d\n",servicep->type);
*/
printf("prepareJSONSmiObjectList2 obj name %s\n", servicep->name);
		if((!selective) || (strstr(servicep->name, browserp->pattern)))
		{
/*
			if(servicep->type == 1)
			{
				sprintf(type_str,"%d@%s|%s|CMD", pid, node, servicep->name);
				sprintf(str,"name: \"%s\", id: \"%s\", leaf: true, icon: \"leaf_cmd.gif\"",servicep->name, type_str);
			}
			else
			{
				sprintf(type_str,"%d@%s|%s", pid, node, servicep->name);
				sprintf(str,"name: \"%s\", state: \"RUNNING\", id: \"%s\", leaf: true",servicep->name, type_str);
			}
*/
			if(servicep->status == 2)
			{
				sprintf(type_str,"%d@%s|%s", pid, node, servicep->name);
				strcpy(aux, servicep->name);
				sptr = strchr(aux,'/');
				if(sptr)
				{
					sptr++;
					sptr = strchr(sptr,'/');
					if(sptr)
						sptr++;
				}
				strcpy(state, smidomainp[i].state);
				stptr = strchr(state,'/');
				if(stptr)
				{
					*stptr = '\0';
				}
				mode_index = smidomainp[i].mode_index;
//				sprintf(str,"name: \"%s\", state: \"%s\", id: \"%s\", leaf: true, fname: \"%s\"",sptr, state, type_str, servicep->name);
				sprintf(str,"name: \"%s\", state: \"%s\", mode: \"%s\",id: \"%s\", leaf: true, fname: \"%s\"",
					sptr, state, smidomainp[mode_index].state, type_str, servicep->name);
			
				ptr = addJSONChildStart(ptr,str,0);
				n++;
				if(n < n_found)
					ptr = addJSONChildEnd(ptr,1);
				else
					ptr = addJSONChildEnd(ptr,0);
			}
		}
		servicep++;
	}
	ptr = addJSONNodeEnd(ptr);
	ptr = addJSONChildEnd(ptr,0);
	ptr = addJSONNodeEnd(ptr);
	ptr = addJSONEnd(ptr);
printf("%s\n",JSONSmiServices);
	return JSONSmiServices;
}

void update_smi_state(OBJSTATE **tag, char *data, int *size)
{
	OBJSTATE *servicep;
	time_t tsecs;

	if(*size){}
	servicep = *tag;

	if(strcmp(servicep->state, data))
	{
		strcpy(servicep->state, data); 
		tsecs = time((time_t *)0);
		((BROWSER *)(servicep->browserp))->last_changed = tsecs;
	}
//printf("SMI State %s %s %08x\n", servicep->name, servicep->state, (unsigned int)servicep);
}

int findSmiServices(BROWSER *browserp, SERVER *servp)
{
	DNS_SERVICE_INFO *servicep;
	int n_services, i, index;
	int n_found = 0, sid;
	int checkSmiObjName();
	int findSmiModeObj();

	n_services = servp->server.n_services;
	if(strcmp(browserp->curr_smidomain,servp->name))
	{
		if(browserp->curr_smidomain[0] != '\0')
		{
// unsubscribe; free
			for(i = 0; i < browserp->curr_smidomain_size; i++)
			{
				if(browserp->smidomainp[i].sid)
					dic_release_service(browserp->smidomainp[i].sid);
			}
			free(browserp->smidomainp);
			browserp->curr_smidomain[0] = '\0';
			browserp->curr_smidomain_size = 0;
		}
		strcpy(browserp->curr_smidomain, servp->name);
		browserp->smidomainp = malloc(n_services * sizeof(OBJSTATE));
		browserp->curr_smidomain_size = n_services;
	}
	else
		return browserp->curr_smidomain_nobjs;
	servicep = servp->service_ptr;
	for(i = 0; i < n_services; i++)
	{
		browserp->smidomainp[i].sid = 0;
		browserp->smidomainp[i].state[0] = '\0';
		if(checkSmiObjName(servicep))
		{
			strcpy(browserp->smidomainp[i].name, servicep->name);
//			strcpy(browserp->smidomainp[i].state, "");
			browserp->smidomainp[i].browserp = browserp;
//printf("address %s %08x\n",browserp->smidomainp[i].name, (unsigned int)&(browserp->smidomainp[i]));
			sid = dic_info_service(servicep->name,MONITORED,0,0,0,update_smi_state, &(browserp->smidomainp[i]),
						no_link_str, 5);
			browserp->smidomainp[i].sid = sid;
			if(servicep->status == 2)
				n_found++;
		}
		servicep++;
	}
	servicep = servp->service_ptr;
	for(i = 0; i < n_services; i++)
	{
		if(servicep->status == 2)
		{
			index = findSmiModeObj(servp->service_ptr, n_services, servicep->name);
			browserp->smidomainp[i].mode_index = index;
		}
		servicep++;
	}
	browserp->curr_smidomain_nobjs = n_found;
	return n_found;
}

int findSmiModeObj(DNS_SERVICE_INFO *serviceptr, int n_services, char *name)
{
	int i;
	DNS_SERVICE_INFO *servicep;
	char mode_name[256], *ptr, *ptr1, *ptr2;

	servicep = serviceptr;
	strcpy(mode_name, name);
	ptr1 = mode_name;
	if((ptr = strstr(mode_name,"::")))
	{
		*ptr = '\0';
		ptr2 = ptr1;
		while((ptr1 = strchr(ptr1,'/')))
		{
			ptr1++;
			ptr2 = ptr1;
		}
		if(strcmp(ptr2, ptr+2))
			*ptr = ':';
	}
	strcat(mode_name,"_FWM");
printf("Find SMI Mode %s %s\n",name, mode_name);
	for(i = 0; i < n_services; i++)
	{
		if(servicep->status == 3)
		{
			if(!strcmp(servicep->name, mode_name))
			{
printf("Find SMI Mode index %s %s %d\n",mode_name, servicep->name, i);
				return i;
			}
		}
		servicep++;
	}
	return 0;
}

int checkSmiObjName(DNS_SERVICE_INFO *servicep)
{
	int ismode = 0, ret = 0;
	char *name;
	int matchString();

	name = servicep->name;
	if(matchString(name,"SMI/*"))
	{
		ret = 1;
		if(matchString(name,"*&ALLOC*"))
			ret = 0;
		else if(matchString(name,"*/ACTIONS&PARS"))
			ret = 0;
		else if(matchString(name,"*/BUSY"))
			ret = 0;
		else if(matchString(name,"*/CMD"))
			ret = 0;
		else if(matchString(name,"*/OBJECTSET_LIST"))
			ret = 0;
		else if(matchString(name,"*/OBJECT_LIST"))
			ret = 0;
		else if(matchString(name,"*/SMI_VERSION_NUMBER"))
			ret = 0;
		else if(matchString(name,"*/SET/*"))
			ret = 0;
// If JCOP framework
		else if(matchString(name,"*_FWDM"))
			ret = 0;
		else if(matchString(name,"*_FWCNM"))
			ret = 0;
		else if(matchString(name,"*_FWM"))
		{
			ismode = 1;
			if(matchString(name,"*::*")) 
				ret = 0;
		}
	}
	if(ret)
	{
		if(ismode)
			servicep->status = 3;
		else
			servicep->status = 2;
	}
	return ret;
}

int matchString( char *wzString, char *wzPattern )
{
  switch (*wzPattern){
    case '\0':
      return !*wzString;
    case '*':
      return matchString(wzString, wzPattern+1) ||
             ( *wzString && matchString(wzString+1, wzPattern) );
    case '?':
      return *wzString &&
             matchString(wzString+1, wzPattern+1);
    default:
      return (*wzPattern == *wzString) &&
             matchString(wzString+1, wzPattern+1);
  }
}

int get_type_size(char type)
{
  int size;

  switch(type)
    {
    case 'L':
    case 'l':
      size = sizeof(long);
      break;
    case 'I':
    case 'i':
      size = sizeof(int);
      break;
    case 'S':
    case 's':
      size = sizeof(short);
      break;
    case 'F':
    case 'f':
      size = sizeof(float);
      break;
    case 'D':
    case 'd':
      size = sizeof(double);
      break;
    case 'C':
    case 'c':
    default:
      size = 1;
    }
  return(size);
}

void did_prepare_command(char *str, char *service, char *format)
{
char type;
int num;
int size, full_size = 0;
char *ptr;
static int last_size = 0;
static void *last_buffer = 0;
void *buffer_ptr;
char *str_ptr;
void did_read_string(char, int, void **, char **);

   str_ptr = str; 
   ptr = format; 
   while(*ptr)
   { 
     type = *ptr++;
     if(*ptr == ':')
     {
		ptr++;
		size = get_type_size(type);
		sscanf(ptr, "%d", &num);
		full_size += size * num;
		if( (ptr = strchr(ptr,';')) )
			ptr++;
		else
			break;
     }
   }

   full_size += 256;
   if(full_size > last_size)
   {
      if(last_size)
		free(last_buffer);
      last_buffer = malloc((size_t)full_size);
      last_size = full_size;
   }
   memset(last_buffer, 0, (size_t)last_size);
   buffer_ptr = last_buffer;
   ptr = format; 
   while(*ptr)
   { 
     type = *ptr++;
     if(*ptr == ':')
     {
		ptr++;
		sscanf(ptr, "%d", &num);
		did_read_string(type, num, &buffer_ptr, &str_ptr);  
		if(!str_ptr)
			break;
		if( (ptr = strchr(ptr,';')) )
			ptr++;
		else
			break;
     }
     else
     {
		did_read_string(type, 0, &buffer_ptr, &str_ptr);
		break;
     }
   }
   full_size = (int) ((char *)buffer_ptr - (char *)last_buffer);
   dic_cmnd_service(service,last_buffer,full_size);
}

int read_str_int(char *str)
{
  int i;
  if((str[0] == '0') && (str[1] == 'x'))
    sscanf(str+2,"%x",&i);
  else
    sscanf(str,"%d",&i);
  return(i);
}

int read_str_char(char *str, char *cc)
{
  int num;

  if(str[0] == '\'')
    *cc = str[1];
  else if(str[0] == '\"')
    return(0);
  else if((str[0] == '0') && (str[1] == 'x'))
  {
    sscanf(str+2,"%x",&num);
	if(num <= 0xff)
		*cc = (char)num;
	else
		return(-1);
  }
  else if(isalpha(str[0]))
    return(-1);
  else
  {
    sscanf(str,"%d",&num);
	if(num <= 0xff)
		*cc = (char)num;
	else
		return(-1);
  }
  return(1);
}

void did_read_string(char type, int num, void **buffer_ptr, char **str_ptr)
{
int i, ret = 0;
float ff;
double dd;
void *ptr;
char *strp, *ptr1;
char cc;
 short s;

  strp = *str_ptr; 
  ptr = *buffer_ptr;
  if(!num)
    num = 1000000;
  switch(type)
  {
    case 'L':
    case 'l':
    case 'I':
    case 'i':
      for(i = 0; i<num; i++)
      {
		*(int *)ptr = read_str_int(strp);
		ptr = (int *)ptr +1;
		if( (strp = strchr(strp,' ')) )
			strp++;
		else
			break;
      }
      break;
    case 'S':
    case 's':
      for(i = 0; i<num; i++)
      {
		s = (short)read_str_int(strp);
		*((short *)ptr) = s;
		ptr = (short *)ptr +1;
		if( (strp = strchr(strp,' ')) )
			strp++;
		else
			break;
      }
      break;
    case 'F':
    case 'f':
      for(i = 0; i<num; i++)
      {
		sscanf(strp,"%f",&ff);
		*(float *)ptr = ff;
		ptr = (float *)ptr +1;
		if( (strp = strchr(strp,' ')) )
			strp++;
		else
			break;
      }
      break;
    case 'D':
    case 'd':
      for(i = 0; i<num; i++)
      {
		sscanf(strp,"%f",&ff);
		dd = (double)ff;
		*(double *)ptr = dd;
		ptr = (double *)ptr +1;
		if( (strp = strchr(strp,' ')) )
			strp++;
		else
			break;
      }
      break;
    case 'C':
    case 'c':
    default:
      for(i = 0; i<num; i++)
      {
		if((ret = read_str_char(strp, &cc)) <= 0)
			break;
		*(char *)ptr = cc;
		ptr = (char *)ptr +1;
		if( (strp = strchr(strp,' ')) )
			strp++;
		else
			break;
	  }
      if(ret <= 0)
      {
		if(!ret)
		{
			strp++;
		}
		num = (int)strlen(strp)+1;
		strncpy((char *)ptr,strp,(size_t)num);
		if( (ptr1 = (char *)strchr((char *)ptr,'\"')) )
		{
			num--;
			*ptr1 = '\0';
		}
		ptr = (char *)ptr + num;
		if( (strp = strchr(strp,' ')) )
			strp++;
		else
			break;
      }
  }
  *buffer_ptr = ptr;
  *str_ptr = strp;
}
