#include <errno.h>
#include <dim.h>
#include <ctype.h>
#ifndef WIN32
#include <netdb.h>
#endif

int Tcpip_max_io_data_write = TCP_SND_BUF_SIZE - 16;
int Tcpip_max_io_data_read = TCP_RCV_BUF_SIZE - 16;

static void ast_conn_h(int handle, int svr_conn_id, int protocol)
{
	register DNA_CONNECTION *dna_connp;
	register int tcpip_code;
	register int conn_id;
	int web_start_read();

	if(protocol){}
	conn_id = conn_get();
/*
	if(!conn_id)
		dim_panic("In ast_conn_h: No more connections\n");
*/
	dna_connp = &Dna_conns[conn_id] ;
	dna_connp->error_ast = Dna_conns[svr_conn_id].error_ast;
	tcpip_code = tcpip_open_connection( conn_id, handle );

	if(tcpip_failure(tcpip_code))
	{
		dna_report_error(conn_id, tcpip_code,
			"Connecting to", DIM_ERROR, DIMTCPCNERR);
		conn_free(conn_id);
	} else {
		dna_connp->state = RD_HDR;
		dna_connp->buffer = (int *)malloc(TCP_RCV_BUF_SIZE);
		memset(dna_connp->buffer, 0, TCP_RCV_BUF_SIZE);
/*
		if(!dna_connp->buffer)
		{
			printf("Error in DNA - handle_connection malloc returned 0\n");
			fflush(stdout);
		}
*/
		dna_connp->buffer_size = TCP_RCV_BUF_SIZE;
		dna_connp->read_ast = Dna_conns[svr_conn_id].read_ast;
		dna_connp->saw_init = FALSE;
		web_start_read(conn_id, TCP_RCV_BUF_SIZE); /* sizeof(DNA_NET) */
		/* Connection arrived. Signal upper layer ? */
		dna_connp->read_ast(conn_id, NULL, 0, STA_CONN);
	}
	tcpip_code = tcpip_start_listen(svr_conn_id, ast_conn_h);
	if(tcpip_failure(tcpip_code))
	{
		dna_report_error(svr_conn_id, tcpip_code,
			"Listening at", DIM_ERROR, DIMTCPLNERR);
	}
}

static void read_data( int conn_id)
{
	register DNA_CONNECTION *dna_connp = &Dna_conns[conn_id];

/*
printf("passing up %d bytes, conn_id %d\n",dna_connp->full_size, conn_id); 
*/
		dna_connp->read_ast(conn_id, dna_connp->buffer,
			dna_connp->full_size, STA_DATA);
}

static void ast_read_h( int conn_id, int status, int size )
{
	register DNA_CONNECTION *dna_connp = &Dna_conns[conn_id];

	if(!dna_connp->buffer) /* The connection has already been closed */
	{
		return;
	}
	if(status == 1)
	{
/*
		next_size = dna_connp->curr_size;
		buff = (char *) dna_connp->curr_buffer;
  		if(size < next_size) 
		{
			max_io_data = Tcpip_max_io_data_read;
			read_size = ((next_size - size) > max_io_data) ?
				max_io_data : next_size - size;
			dna_connp->curr_size -= size;
			dna_connp->curr_buffer += size;
			tcpip_code = tcpip_start_read(conn_id, buff + size, 
				read_size, ast_read_h);
			if(tcpip_failure(tcpip_code)) 
			{
#ifndef WIN32
			  if(errno == ENOTSOCK)
			  {
				  if(dna_connp->read_ast)
					dna_connp->read_ast(conn_id, NULL, 0, STA_DISC);
			  }
			  else
#endif
			  {
				dna_report_error(conn_id, tcpip_code,
					"Reading from", DIM_ERROR, DIMTCPRDERR);
			  }
			}
			return;
		}
		switch(dna_connp->state)
		{
			case RD_HDR :
				if(is_header(conn_id))
				{
					if( dna_connp->state == RD_DATA )
					{
						next_size = vtohl(dna_connp->buffer[1]);
						dna_start_read(conn_id, next_size);
					}
					else
					{
						dna_connp->state = RD_HDR;
						dna_start_read(conn_id, READ_HEADER_SIZE);
					}
				}
				break;
			case RD_DATA :
				read_data(conn_id);
				dna_connp->state = RD_HDR;
				dna_start_read(conn_id, READ_HEADER_SIZE);
				break;
			default:
				break;
		}
*/
		dna_connp->full_size = size;
		read_data(conn_id);
		dna_start_read(conn_id, TCP_RCV_BUF_SIZE);
	} 
	else 
	{
	  /*
	  printf("Connection lost. Signal upper layer\n");
	  */
		if(dna_connp->read_ast)
			dna_connp->read_ast(conn_id, NULL, 0, STA_DISC);
	}
}

int web_start_read(int conn_id, int size)
{
	register DNA_CONNECTION *dna_connp = &Dna_conns[conn_id];
	register int tcpip_code, read_size;
	int max_io_data;
	
	if(!dna_connp->busy)
	{
		return(0);
	}

	dna_connp->curr_size = size;
	dna_connp->full_size = size;
	if(size > dna_connp->buffer_size) 
	{
		dna_connp->buffer =
				(int *) realloc(dna_connp->buffer, (size_t)size);
		memset(dna_connp->buffer, 0, (size_t)size);
		dna_connp->buffer_size = size;
	}
	dna_connp->curr_buffer = (char *) dna_connp->buffer;
	max_io_data = Tcpip_max_io_data_read;
	read_size = (size > max_io_data) ? max_io_data : size ;

	tcpip_code = tcpip_start_read(conn_id, dna_connp->curr_buffer,
				  read_size, ast_read_h);
	if(tcpip_failure(tcpip_code)) {
		dna_report_error(conn_id, tcpip_code,
			"Reading from", DIM_ERROR, DIMTCPRDERR);

		return(0);
	}

	return(1);
}								

int web_get_port()
{
	int ret;
	char ports[64];
	int port = 2500;

	ret = dim_get_env_var("DIM_DID_PORT", ports, 64);
	if(ret)
	{
		sscanf(ports,"%d",&port);
	}
	return port;
}

int web_open_server(char *task, void (*read_ast)(), int *protocol, int *port, void (*error_ast)())
{
	register DNA_CONNECTION *dna_connp;
	register int tcpip_code;
	register int conn_id;

	conn_id = dna_open_client("", "", 0, 0, 
					0, 0, 0);
	dna_close(conn_id);
/*
	if(!DNA_Initialized)
	{
		conn_arr_create(SRC_DNA);
		DNA_Initialized = TRUE;
	}
*/
	*protocol = PROTOCOL;
	conn_id = conn_get();
	dna_connp = &Dna_conns[conn_id];
/*
	if(!conn_id)
		dim_panic("In dna_open_server: No more connections\n");
*/
	dna_connp->protocol = TCPIP;
	dna_connp->error_ast = error_ast;
	tcpip_code = tcpip_open_server(conn_id, task, port);
	if(tcpip_failure(tcpip_code))
	{
		dna_report_error(conn_id, tcpip_code,
			"Opening server port", DIM_ERROR, DIMTCPOPERR);
		conn_free(conn_id);
		return(0);
	}
	dna_connp->writing = FALSE;
	dna_connp->read_ast = read_ast;
	tcpip_code = tcpip_start_listen(conn_id, ast_conn_h);
	if(tcpip_failure(tcpip_code))
	{
		dna_report_error(conn_id, tcpip_code, "Listening at", DIM_ERROR, DIMTCPLNERR);
		return(0);
	}
	return(conn_id);
}

void web_write(int conn_id, char *buffer, int size)
{
	tcpip_write( conn_id, buffer, size );
}

static void release_conn(int conn_id)
{
	register DNA_CONNECTION *dna_connp = &Dna_conns[conn_id] ;

	DISABLE_AST
	if(dna_connp->busy)
	{ 
		tcpip_close(conn_id);
		if(dna_connp->buffer)
		{
			free(dna_connp->buffer);
			dna_connp->buffer = 0;
			dna_connp->buffer_size = 0;
		}
		dna_connp->read_ast = NULL;
		dna_connp->error_ast = NULL;
		conn_free(conn_id);
	}
	ENABLE_AST
}

int web_close(int conn_id)
{
	if(conn_id > 0)
	{
		release_conn(conn_id);
	}
	return(1);
}

int web_get_node_name(char *node, char *name)
{
	int a,b,c,d;
/* Fix for gcc 4.6 "dereferencing type-punned pointer will break strict-aliasing rules"?!*/
	unsigned char ipaddr_buff[4];
	unsigned char *ipaddr = ipaddr_buff;
	struct hostent *host;

	strcpy(name, node);
	if(isdigit(node[0]))
	{
		sscanf(node,"%d.%d.%d.%d",&a, &b, &c, &d);
	    ipaddr[0] = (unsigned char)a;
	    ipaddr[1] = (unsigned char)b;
	    ipaddr[2] = (unsigned char)c;
	    ipaddr[3] = (unsigned char)d;
		if( (host = gethostbyaddr(ipaddr, sizeof(ipaddr), AF_INET)) == (struct hostent *)0 )
		{
			return(0);
		}
		else
		{
			strcpy(name,host->h_name);
			return(1);
		}
	}
	return(0);
}
