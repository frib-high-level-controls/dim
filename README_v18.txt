
                    DIM version 18.05 Release Notes

Notes 1 and 2 for Unix Users only
NOTE 1: In order to "make" DIM two environment variables should be set:
	OS = one of {HP-UX, AIX, OSF1, Solaris, SunOS, LynxOS, Linux, Darwin}
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

NOTE 3: The Version Number service provided by servers is now set to 1805.

26/02/2009
Changes for version 18.05:
    - Made the callback for "DIS_DNS/SERVER_LIST" uninterruptible, so that two clients subscribing
      would not get mixed up answers.
    - The same for "<server>/SERVICE_LIST"
    - Tryied to fix a DNS crash, introduced in v18r4 by releasing the connection when "informing clients".
    - removed some "//" comments in "C"


20/02/2009
Changes for version 18.04:
    - Changed the dim_wait() mechanism, so that it works for several threads in parallel:
	- On Linux it was based on POSIX semaphores now it is based on POSIX "condition 
          variables"
	- On Windows it was based on "Auto Reset Events" now it uses "Manual Resel "Events"
    - The DNS should now correctly update the "DIS_DNS/SERVER_LIST" service. It used to report
      a new server, even when the services already existed and the server was killed by the DNS.
      (And never report it killed). It also didn't report correctly when a server went out of "ERROR"
      (this is reported as a "+" as for a new server). 


05/02/2009
Changes for version 18.03:
    - The list of registered services in a server could get corrupted in some rare cases
      making the server crash - fixed.
    - If the DNS couldn't talk to a client it could sometimes hang - fixed.
    - Java client modifications:
	- DimUpdatedInfo was not working correctly - fixed in dim_jni.c.
	- Implemented DimRpcInfo
	- Changed the DimBroser class to use DimRpcInfo.
	- Added a jdim.jar file in the jdim/classes directory of the DIM distribution 


15/01/2009
Changes for version 18.02:
    - Added the following functions:
	- C++ Client
		- int DimClient.getServerPid()
	- Java Client
		- int DimClient.getServerPid()
		- String[] DimBrowser.getServers()
		- String DimBrowser.getServerNode(String server)
		- int DimBrowser.getServerPid(String server)


09/01/2009
Changes for version 18.01:
    - Added in the distribution the Visual Studio 8 dlls and manifest. Otherwise
      it would not work on most PCs.


03/12/2008
Changes for version 18.00:
    - The Windows execulables and libraries are now built using Visual Studio 8
    - Some changes added by GSI mainly in the Java Native Interface


06/11/2008
Changes for version 17.12:
    - Client functionality:
    	- Added a new function dic_stop(), to close anything related to DIM 
          for a client
    	- Added the function dic_get_server_pid(). Similar to dic_get_server(). 
          Can be executed in a callback to retrieve the pid of the current server
    - DimBrowser Class:
        - DimBrowser::getServices() used to create and destroy the DimRpc connection
          to the Dns every time it was called. This was heavy if called in a loop.
          Now the connection is maintained until the DimBrowser itself is destroyed.
	- A new method DimBrowser::getNextServer(char *&server, char *&node, int *pid)
          has been created. similar to the previous one but returns also the server pid.
    - DNS
	- The DNS was still doing some blocking write calls to servers or clients.
          Now all write calls have a timeout and can not block forever.
    - Linux DID
	- The "Subscribe" button was subscribing to services with update rate of 10 seconds.
	  This was misleading, the users could think the server was calling update_service
          when it wasn't.
          Now there are two Subscribe buttons ("on change" or "Update rate of 10 seconds").
    - DimDridge
	- Accepts an extra flag "-copy" which provokes an internal copy of the data.


08/09/2008
Changes for version 17.11:
    - Some DIM Processes, servers or clients could enter a loop taking 100 % CPU 
      time in some rare occasions, fixed.
    - Added some protections when removing services in the DimBridge.


30/08/2008
Changes for version 17.10:
    - Some DIM Processes, servers or clients would not reconnect when the DNS was
      restarted. Fixed two cause:
	- Some processes in Linux were stuck reading from the DNS socket
	- Some others "forgot" to set a timer under very special conditions
    - Changed some of the DNS debug messages to be more explicit.


21/07/2008
Changes for version 17.09:
    - DIM error messages were not being flushed when the output was redirected 
      to a logfile, fixed.


18/07/2008
Changes for version 17.08:
    - Sometimes a server or a client could do a read on a sockect that had just
      been closed which left them hanging forever - fixed.


01/07/2008
Changes for version 17.07:
    - The DimTimer was sometimes not started when the constructor was called
      with a time argument.
    - Clients could not connect to more than 1024 servers - fixed.
      (if the machine allows more than 1024 connections)


30/06/2008
Changes for version 17.06:
    - Corrected the makefile for Darwin, now the number of accepted connections is 
      increased to 8192 only for Linux.
    - Fixed a bug in the DimTimer, it used to accept to be re-started, but then crashed
      at destruction time if not stopped the same number of times. Now it can not be
      re-started.
    - The Dns used to ask servers to re-register at regular intervals when they were not 
      sending their watchdog messages (i.e. they were in "ERROR", red in DID). Now the
      DNS only asks once (unless they answer). This could cause the DNS to hang if
      servers were in ERROR for a long time.
    - The Dns now accepts a command line parameter: -d to print debug messages.
    - The clients were not handling properly the case when they could contact the DNS
      but then they could not contact the server that the DNS gave them (either because
      of a firewall or because the server run on an inaccessible network). In this case
      the clients would timeout trying to contact the server for each service and kept
      asking the DNS the server coordinates over and over again. Now the clients keep
      a list on unreacheable servers, so they don't try to contact the server for each 
      service and only ask the DNS again with an increasing interval that goes from 10 
      seconds to 2 minutes maximum.
    - The server now issues an error message if the format string is too long.
    - Linux DID
        - Removed the command "Kill ALL Servers", it was too dangerous
	- Now the list of nodes in "View Servers by Node" is in alphabetical order and
	  in lowercase.


30/04/2008
Changes for version 17.05:
    - In Linux in some cases a SIGPIPE was generated. Normally the DIM library sets
      the behaviour of SIGPIPE to ignored, but if another library or main program
      changes the SIGPIPE behaviour, then the application could exit when the SIGPIPE
      was generated. Fixed - on Linux now the function send with flag MSG_NOSIGNAL
      is used in oder to avoid generating SIGPIPE.



4/04/2008
Changes for version 17.04:
    - Sometimes processes (servers or clients) would hang when the DNS was restarted.
      This was due to a strange (Windows?) feature, by which a connect could succeed
      after a connection was closed (and reported) on the other side. Fixed.



27/03/2008
Changes for version 17.03:
    - Can now make DID for 64 bits by making DIM using:
	gmake X64=yes all
    - Increased the size of the Hash tables for the servers and the DNS.



20/02/2008
Changes for version 17.02:
    - Fixed the Java DimTimer - stop() didn't work
      Required changing dim_jni.c as well as the java part
    - Fixed DIM for Darwin - had stopped working



20/01/2008
Changes for version 17.01:
    - The Java API now works on 64 bit machines, Thanks to Joern Adamczewski.
      Please use:
	gmake JDIM=yes all
    - Linux executables are now compiled/linked on slc4 (32 bits).
    - Big changes in the DimRpcs both client and server part. Tere were bugs
      related to the handling of timeouts.
      Unfortunatelly all applications using RPCs need to be re-linked.



-----------------------------------------------------------------------------------------
Previous version history:

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
