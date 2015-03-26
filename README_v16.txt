
                    DIM version 16.14 Release Notes

Notes 1 and 2 for Unix Users only
NOTE 1: In order to "make" DIM two environment variables should be set:
	OS = one of {HP-UX, AIX, OSF1, Solaris, SunOS, LynxOS, Linux}
	DIMDIR = the path name of DIM's top level directory
	The user should then go to DIM's top level directory and do:
	> source .setup
	> gmake all
	Or, if there is no support for C++ on the machine:
	> gmake CPP=no all

NOTE 2: The Name Server (Dns), DID, servers and clients (if running in 
	background) should be started whith the output redirected to a 
	logfile ex:
		Dns </dev/null >& dns.log &

NOTE 3: The Version Number service provided by servers is now set to 1614.

07/12/2007
Changes for version 16.14:
    - Now by default All DIM processes are ready to accept up to 8192 connections, both
      in Linux and Windows. Although in Linux for this to be effective the machine system 
      limits must allow more than 1024 descriptors/open files per process.
    - Fixed a little memory leak in tokenstring.cxx
    - And a little compilation bug for some platforms in tcpip.c 


15/05/2007
Changes for version 16.13:
    - If DIM_HOST_NODE is defined when starting up a server, a DIM client will now try 
      two network interfaces in order to talk to that server and only give up if they both 
      fail. First it will try the ip name or ip address specified by the server using 
      DIM_HOST_NODE, if that fails it will try the ip address of the default interface
      retrieved by the server using gethostname (and gethostbyname).
      The changes basically affect the case in which the DIM_HOST_NODE given to the servers
      is specified as IP address instad of an IP name. Otherwise this mechanism was already 
      working.


3/05/2007
Changes for version 16.12:
    - The Java version did not exit properly when main() terminated - fixed.


25/04/2007
Changes for version 16.11:
    - On Linux the timeout to detect a lost connections (unplugged ethernet cable
      or machine reboot) was too long, around 15 minutes - Fixed.
      On Linux the KEEPALIVE feature is now used instead of a regular socket write,
      all other platforms should work as before.


21/02/2007
Changes for version 16.10:
    - Found a bug in dis_stop_serving: one socket connection was not closed - fixed.
    - Implemented a new environment variable for the DNS: DIM_DNS_ACCEPTED_NODES
      Can receive a list on nodes or domains separated by commas.
      If the DNS receives a connection from a node not in this list, it will
      reject it and kill the server or client requesting it.
    - Fixed some C++ warnings.


19/01/2007
Changes for version 16.9:
    - The modifications done in version 16.8 have introduced a bug:
	- DIM servers would not behave properly (exit) when receiving a kill command
          from the DNS (for duplicated services, not allowed host names or manual "kill")
	  This is now fixed.


30/10/2006
Changes for version 16.8:
    - Modified dis_stop_serving() and DimServer::stop() to completely stop DIM:
	- Stop also the DIM threads.
	- Release all allocated memory
	- Allow a different port number when re-starting.


11/07/2006
Changes for version 16.7:
    - Prepared for increasing the number of open connections per process
      (On Linux still requires changing some parameters and recompiling the Dns)
    - Fixed one error and several warnings for gcc 4.


11/05/2006
Changes for version 16.6:
    - Sometimes a server or client would crash while exiting if the DNS was not running.
      Fixed.
    - Fixed the reporting of some ERROR messages on Windows (used to report error "0")
    - Allowed dim_send_command to receive instead of -dns <node_name>
	-dns <node_name>[:<port_number>]


01/05/2006
Changes for version 16.5:
    - Big Spring Cleanup. Removed most warnings. Can now be compiled on
      Windows with Warning Level 3 and on Linux with -Wall
      (still not working for -ansi -pedantic...)
    - When trying to access a server in a different network (i.e. not reacheable)
      a client (for example DID) would take very very long to timeout - fixed.
    - Added two new sets of functions that allow setting the DIM_DNS_NODE separately
      for a server and a client in the same process:
	- int dis_set_dns_node(char *node)
	- int dis_get_dns_node(char *node)
	- int dis_set_dns_port(int port)
	- int dis_get_dns_port()

	- int dic_set_dns_node(char *node)
	- int dic_get_dns_node(char *node)
	- int dic_set_dns_port(int port)
	- int dic_get_dns_port()
      These routines should be used instead of the equivalent ones starting with "dim_"
      since these set the same DIM_DNS_NODE/port for both Server and client parts of a 
      process.
    - Adapted the C++ equivalents (DimClient::setDnsNode, etc. and DimServer::setDnsNode, 
      etc.) to use the new routines, so they are now independent.
      Adapted also the Java equivalents.
    - Fixed DimBridge to use the new routines.
    - Fixed a bug in DID that made it crash sometimes at startup (and also when the DNS 
      restarted)!
    - Found some very interesting features of DIM:
        - In a node with two ethernet interfaces (so connected to two networks):
	    - The DNS will answer to servers and client on both networks, only its server
              part - DIS_DNS (the one that answers to DID and DimBrowser requests) would
              in principle answer only to one of the networks (in principle the default
              interface* but can be changed by setting the environment variable "DIM_HOST_NODE").    
	    - But, in fact, if the DNS or any server is started with the environment variable 
              DIM_HOST_NODE set to the interface that is not the default* one. Than both the 
              DNS (including the server part) and the DIM servers will be accessible from both 
              networks. For example DID will work fine on both networks.
            * The command "hostname" will return the name of the default network interface.   

    Note: As a result of inserting new functions the DIM shared library entry points have
          changed, so all DIM Servers/Clients should be relinked (in particular in Linux).


20/04/2006
Changes for version 16.4:
    - Optimized the DNS for providing the list or running servers dynamically
      by subscribing to the service "DIS_DNS/SERVER_LIST"


07/04/2006
Changes for version 16.3:
    - Upgraded to work on LynxOS Version 4. 
      - Updated makefile for INTEL platform
      - Updated some ifdefs based on the existence of __Lynx__


10/03/2006
Changes for version 16.2:
    - Increased the listen queue. To avoid "Connection Refused" messages from servers
      or from the DNS.


28/02/2006
Changes for version 16.1:
    - Fixed the NO_THREADS option for LINUX, it had stopped working.
    - DimInfo::getData() could return an invalid pointer if called before connecting
      to the server (or discovering the server did not exist). Fixed 
      (it now returns 0 in this case).


09/11/2005
Changes for version 16.0:
    - Consolidated the new timer handling mechanism, should be much more precise.
    - Fixed the RPC handling. Used to be based on timming assumptions.
      Now uses a safe protocol to make sure the server is connected before sending 
      an RPC request.
    - Included in the distribution some performance measurements and a benchmark 
      server and client. Sources in src/benchmark executables in /bin for windows
      and /linux for linux.
      Usage:
	benchServer <message_size_in_bytes> <number_of_services>
	benchClient
      benchClient will run for a while and print the measurement results. 

Please check the Manual for more information at:
    http://www.cern.ch/dim
