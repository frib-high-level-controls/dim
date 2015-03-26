#ifdef WIN32
#define ioctl ioctlsocket

#define closesock myclosesocket
#define readsock recv
#define writesock send

#define EINTR WSAEINTR
#define EADDRNOTAVAIL WSAEADDRNOTAVAIL
#define EWOULDBLOCK WSAEWOULDBLOCK
#define ECONNREFUSED WSAECONNREFUSED
#define HOST_NOT_FOUND	WSAHOST_NOT_FOUND
#define NO_DATA	WSANO_DATA

#include <windows.h>
#include <process.h>
#include <io.h>
#include <fcntl.h>
#include <Winsock.h>
#include <stddef.h>
#include <stdlib.h>
#include <stdio.h>
#else
#define closesock close
#define readsock(a,b,c,d) read(a,b,c)

#if defined(__linux__) && !defined (darwin)
#define writesock(a,b,c,d) send(a,b,c,MSG_NOSIGNAL)
#else
#define writesock(a,b,c,d) write(a,b,c)
#endif
#include <ctype.h>
#include <sys/socket.h>
#include <fcntl.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <signal.h>
#include <sys/ioctl.h>
#include <errno.h>
#include <netdb.h>
#include <unistd.h>
#include <sys/time.h>
#include <sys/types.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#endif

#define ushort unsigned short
#define TCP_RCV_BUF_SIZE	16384/*32768*//*65536*/
#define TCP_SND_BUF_SIZE	16384/*32768*//*65536*/


#ifdef WIN32
int init_sock()
{
	WORD wVersionRequested;
	WSADATA wsaData;
	int err;
	static int sock_init_done = 0;

	if(sock_init_done) return(1);
 	wVersionRequested = MAKEWORD( 2, 0 );
	err = WSAStartup( wVersionRequested, &wsaData );

	if ( err != 0 ) 
	{
    	return(0);
	}

	/* Confirm that the WinSock DLL supports 2.0.*/
	/* Note that if the DLL supports versions greater    */
	/* than 2.0 in addition to 2.0, it will still return */
	/* 2.0 in wVersion since that is the version we      */
	/* requested.                                        */

	if ( LOBYTE( wsaData.wVersion ) != 2 ||
        HIBYTE( wsaData.wVersion ) != 0 ) 
	{
	    WSACleanup( );
    	return(0); 
	}
	sock_init_done = 1;
	return(1);
}

int myclosesocket(int path)
{
	int code, ret;
	code = WSAGetLastError();
	ret = closesocket(path);
	WSASetLastError(code);
	return ret;
}
#endif

int tcp_open_client( char *node, int port )
{
	/* Create connection: create and initialize socket stuff. Try
	 * and make a connection with the server.
	 */
	struct sockaddr_in sockname;
	struct hostent *host;
	int path, val, ret_code, ret;

#ifdef WIN32
	init_sock();
#endif
	if( (host = gethostbyname(node)) == (struct hostent *)0 ) 
	{
		return(0);
	}

	if( (path = socket(AF_INET, SOCK_STREAM, 0)) == -1 ) 
	{
		perror("socket");
		return(0);
	}

	val = 1;
      
	if ((ret_code = setsockopt(path, IPPROTO_TCP, TCP_NODELAY, 
			(char*)&val, sizeof(val))) == -1 ) 
	{
#ifdef DEBUG
		printf("Couln't set TCP_NODELAY\n");
#endif
		closesock(path); 
		return(0);
	}

	val = TCP_SND_BUF_SIZE;      
	if ((ret_code = setsockopt(path, SOL_SOCKET, SO_SNDBUF, 
			(char*)&val, sizeof(val))) == -1 ) 
	{
#ifdef DEBUG
		printf("Couln't set SO_SNDBUF\n");
#endif
		closesock(path); 
		return(0);
	}

	val = TCP_RCV_BUF_SIZE;
	if ((ret_code = setsockopt(path, SOL_SOCKET, SO_RCVBUF, 
			(char*)&val, sizeof(val))) == -1 ) 
	{
#ifdef DEBUG
		printf("Couln't set SO_RCVBUF\n");
#endif
		closesock(path); 
		return(0);
	}

#if defined(__linux__) && !defined (darwin)
	val = 2;
	if ((ret_code = setsockopt(path, IPPROTO_TCP, TCP_SYNCNT, 
			(char*)&val, sizeof(val))) == -1 ) 
	{
#ifdef DEBUG
		printf("Couln't set TCP_SYNCNT\n");
#endif
	}
#endif

	sockname.sin_family = PF_INET;
	sockname.sin_addr = *((struct in_addr *) host->h_addr);
	sockname.sin_port = htons((ushort) port); /* port number to send to */
	while((ret = connect(path, (struct sockaddr*)&sockname, sizeof(sockname))) == -1 )
	{
		if(errno != EINTR)
		{
			closesock(path);
			return(0);
		}
	}
	return(path);
}

int tcp_write( int path, char *buffer, int size )
{
	/* Do a (synchronous) write to conn_id.
	 */
	int	wrote;

	wrote = writesock( path, buffer, size, 0 );
	if( wrote == -1 ) {
		return(0);
	}
	return(wrote);
}
