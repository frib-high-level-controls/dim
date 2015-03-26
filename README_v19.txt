
                    DIM version 19.39 Release Notes

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

NOTE 3: The Version Number service provided by servers is now set to 1939.

04/10/2012
Changes for version 19.39:
    - Unfortunately Windows, unlike Linux, does not use LP64 convention for 64 bits, 
      i.e. the type long is a 32 bit variable (?!?!?!)
      So created a dim_long type which can always hold a pointer
 

09/08/2012
Changes for version 19.38:
    - The timeout used by clients and servers to try to reconnect to a DNS was supposed to be random,
      to avoid all processes retrying at the same time, but it wasn't - fixed.


27/06/2012
Changes for version 19.37:
    - DIM DNS crashed for servers with a task name bigger than 80 characters - Fixed.


22/06/2012
Changes for version 19.36:
    - The internal "DIS_DNS/KILL_SERVERS" command can now be used to pass a user defined exit_code to
      the servers. The servers will get this exit_code in their exit_handler.
      Although the exit_code passed to the "DIS_DNS/KILL_SERVERS" command is an integer, only the lower
      16 bits can be used, i.e. only these bits are passed to the servers.


24/05/2012
Changes for version 19.35:
    - Fixed the DimInfo() default constructor, now if the default constructor is called, it doesn't cause the 
      destructor to crash anymore.
    - Made available to DimInfo, DimStampedInfo, DimUpdatedInfo and DimCurrentInfo the method: 
      	- void subscribe(char *name, void *nolink, int nolinksize, int time, DimInfoHandler *handler)
      Like this the default constructor can be called and then this method called later to subscribe
      whenever needed.
    - The behaviour of a giving a null pointer and size 0 as "no link" parameters was not completely
      defined. The user could get back either a null pointer or an "invalid" pointer in the callbacks.
      This is now well-defined: 
        - If null pointer and size 0 is used at subscribe, the user will get null pointer and size 0 in the callback.
        - If a negative size is passed at subscribe the callback is not called at all.


03/05/2012
Changes for version 19.34:
    - Changed back to dna_write_nowait() the message that the server sends to the client when removing
      a service. This was causing clients not to reconnect ever again when the server removed services.
      (because the client would get the info much before the DNS, so it would keep trying to reconnect
      and failing even though the service or even the server didn't exist anymore, without asking the DNS)
    - Changed the client in order to avoid the behaviour above, i.e. if sending the service request fails it
      asks the DNS again.


23/04/2012
Changes for version 19.33:
    - A bug was introduced in v19r30. When trying to retry immediately, a dtq_start_timer(0)
      was used (like for dna_write). This is not possible because the callback is not protected 
      by a DIM lock. Fixed. (in v19r32)
    - Small tidy up in dic.c and protecting the move_to_xxx functions.
    - When a server received an unsubscribe from the last subscribed service of a client it was closing the
      connection to the client, this is not good because there could be commands being sent. In any case
      it should be up to the client to close the connection - Fixed.
    - dim_send_command now accepts a "-i" argument to send integer data (default is string)


30/03/2012
Changes for version 19.31:
    - changed dna_write to dna_write_nowait for servers when removing a service and for clients
      when releasing and subscribing to a service. Dna_write cannot be used for the same connection
      as dna_write_nowait as it will mingle the packets.
    - Removed more compiler warnings.


14/03/2012
Changes for version 19.30:
    - Sometimes when trying to open a connection to a server the client could backoff for too
      long (10 seconds), now it will retry immediately and then at increasing intervals.
    - dim_stop() would sometimes not properly stop all threads if a new connection was received
      in the mean time.
    - The DimServerDns destructor could crash, fixed.
    - Removed some compiler warnings about variables set but not used.
    - Two new functions available:
	- dim_set_listen_backlog(int size)
	- int dim_get_listen_backlog()
    - The DNS will set the listen_backlog to 1024 at startup, but the OS will truncate it to
      a maximum limit (available in /proc/sys/net/core/somaxconn, linux default=128), 
      while for servers the constant SOMAXCONN is used.


06/02/2012
Changes for version 19.28:
    - Added more log messages when a "Write Timeout" occurs to know where it originated.
    - A server could sometimes not release the connection in case of a "Write Timeout", and
      then keep on timing out for each message on this connection - fixed.


19/01/2012
Changes for version 19.27:
    - Updated the DIM Makefiles for MacOSX (Darwin)
    - Added New possibilities to change DIM timeouts:
	- 2 New Environment variables:
	    - DIM_WRITE_TMOUT				(default: 5)
	    - DIM_KEEPALIVE_TMOUT			(default: 15)
	- Same functionality as the functions:
	    - dim_set_write_timeout(int secs)
	    - int dim_get_write_timeout()
	    - dim_set_keepalive_timeout(int secs)	//new
	    - int dim_get_keepalive_timeout()		//new
	- The functions have precedence over the environment variables.
    - The server per client exit_handler functionality 
      (provided by dis_add_client_exit_handler()/dis_set_client_exit_handler())
      wan not always working correctly (in case of write timeouts) - fixed.
    - Changed the reporting of "Write Tiemout":
	- Used to report only: 
	    - "ERROR: Write Timeout, disconnecting from..."
	- Now reports:
	    - "WARNING: Write Timeout, writing to ..."
	    - If it disconnects: "ERROR: Write Timeout, disconnecting from ..."
	    - If it reconnects later: "INFO: Re-connected to ..." 


14/09/2011
Changes for version 19.26:
    - In the notes for version 19.08 there is the following:
    	- Since version v18r4 where dim_wait() was modified, dim_wait could hang in windows if
      	  the wake_up event was triggered before dim_wait was called. Could affect smi++. Fixed.
    - Amasingly enough this seems also to be the case for Linux, dim_wait could hang until there
      was some new DIM activity, normally some timer firing... Fixed.
    - The Linux DID now accepts an extra parameter: -dns=<dns_node_name>

01/09/2011
Changes for version 19.25:
    - When a Client was releasing a service "at the same time" as the server was deleting the service,
      The Connection could be released by mistake - fixed.
    - The SERVICE_LIST service could be updated with an empty string if there were two consecutive 
      dis_start_serving() - fixed.


08/08/2011
Changes for version 19.24:
    - The funtion DimInfo::getFormat() never return the correct format of a service, if the first time
      it was called, the service was not available, i.e. when called inside a "no_link" callback - Fixed.
    - Deleting the last service of a server or stopping a server could generate "Invalid Service Id"
      messages from the service that updates DID - Fixed.


15/07/2011
Changes for version 19.23:
    - The new functions:
	int DimClient::inCallback()
	int DimServer::inCallback()
      Can be used to find out if the function is being called in the context of a DIM callback
      handler (they return 1 if yes, 0 if no).
    - There was no way to "remove" an errorHandler or exitHandler in C++.
      Now the following functions accept 0 as parameter:
	DimClient::addErrorHandler(0)
	DimServer::addErrorHandler(0)
	DimServer::addExitHandler(0)
	DimServer::addClientExitHandler(0)
      In order to remove them.
    - The Windows Visual Studio Manifest file distributed since version v19r19 was wrong, so the latest
      DIM versions did not work on Windows machines without Visual Studio 8 installed - Fixed.


21/06/2011
Changes for version 19.22:
    - DIM servers would hang when tring to exit due to "Services already declared", if the user
      exitHandler() didn't directly call exit (instead tried to exit later in the main program).
      Fixed.
    - Servers accept now also 'B' or 'V' as format, they are both equivalent to 'C'.
    - In Linux when a server printed "Write timeout, disconecting from XXX", it didn't always
      disconnect properly, so the client would not always reconnect afterwards. Fixed. 


31/05/2011
Changes for version 19.21:
    - Fixed a bug in DimRpcInfo: the timer for the timeout was started too late and sometimes the
      RPC data was received in the meantime, so the timer was never stopped. 


04/05/2011
Changes for version 19.20:
    - Fixed a bug added in v19r18: The <server_name>/SERVICE_LIST was no longer reporting correctly
      the disappearence of services (by a "-<service_name>" ). Fixed.


27/04/2011
Changes for version 19.19:
    - Fixed a very very old (horrible) bug in dis.c and dns.c: there was a hardwired malloc(8), 
      which was only ok for 32 bit machines. It's amazing this didn't bring more trouble...


07/04/2011
Changes for version 19.18:
    - The standard server service <server_name>/SERVICE_LIST had problems reporting the correct
      information when used by several clients in paralel - fixed.


11/03/2011
Changes for version 19.17:
    - The TCPIP "listen" backlog for a server was increased for all servers (including the DNS)
      from 16 to the constant SOMAXCONN (128 on Linux at the moment, 200 on Windows ?)
    - After a "fork" the DIM initialization sequence guided by semaphores was not correctly
      handled, this made the forked children hang and not respond to DIM anymore - fixed.


23/02/2011
Changes for version 19.16:
    - DimServer::stop() did not correctly clear the ServerName - fixed.
    - The Linux DID now prints the DNS it is connected to in the title bar.


20/12/2010
Changes for version 19.15:
    - Still fixing dis_stop_serving():
        - The DNS sometimes gets a remove service message from a server, after the server has
          closed the connection, this was not handled properly - fixed.
        - Fixed yet another detail (variable not cleared) in the thread handling at dis_stop_serving().
	- dtq.c now clears all timer_queues at dis_stop_serving()
	- Added some protections in case of closed connections.


10/12/2010
Changes for version 19.14:
    - Still fixing dis_stop_serving():
	- Adedd pthread_join in linux to wait for threads to die
	- the following dis_start_serving() would not reconnect to the DNS if the DNS connection
          was pending (i.e. the DNS was stopped or restarted)


06/12/2010
Changes for version 19.13:
    - Fixed a few compiler warnings in dis.c
    - Added #ifndef PXI around some Windows setPriority calls
    - dis_stop_serving() did not completely clean-up DIM so that another dis_start_serving()
      could be done properly after for example a "fork()". Fixed.


20/09/2010
Changes for version 19.12:
    - Fixed a bug added when removing warnings in v19r10 (dis.c and dns.c).


07/06/2010
Changes for version 19.11:
    - Added some protections in update_service() in order to try to solve a DIP issue.
      (related to very frequent updates of the same service in different threads) 
    - Added the possibility of defining timeouts for:
	- DimBrowser::getServices 
	- DimBrowser::getServers 
	- DimBrowser::getServerServices 
	- DimBrowser::getServerClients
    - Added the possibility of retrieving the time a command arrived:
	- int dis_get_timestamp(int service_id, int *secs, int *millisecs) in C
	- int DimCommand::getTimestamp() and int DimCommand::getTimestampMillisecs() in C++
    - Added a "const" keyword to the "char *format" parameter in the constructors of
	- DimService and DimCommand
    - Added a call DimCommand::hasNext(), can be used when commands are queued.
    - Fixed a memory leak when using DimService::setData and then dynamically deleting the
      DimService 


17/02/2010
Changes for version 19.10:
    - Fixed a bug in the DNS related to the latest change (browsing for a single service name)
      The DNS could crash when killing a server.
    - Removed some compilation warnings 


04/01/2010
Changes for version 19.09:
    - Created two new functions: dis_set_debug_on() and dis_set_debug_off(), these
      enable or disable printing a message per service update
    - Tried to protect against:
	- a service being deleted from the server while it is being updated
        - a client unsubscribing from a service while it is being updated.
    - Optimized the DNS when browsing for a service search pattern without wildcards
      (i.e. browsing for a single service name) 


13/11/2009
Changes for version 19.08:
    - Since version v18r4 where dim_wait() was modified, dim_wait could hang in windows if
      the wake_up event was triggered before dim_wait was called. Could affect smi++.
      Fixed.
    - Fixed a compilation bug in dis.c that affected some platforms.


30/10/2009
Changes for version 19.07:
    - Some more bugs related to being able to publish to more that one DNS fixed.


28/10/2009
Changes for version 19.06:
    - When opening DNS connections, when the DNS is not there, from a process that is at the 
      same time a client and a server only one pending connection was used now two separate 
      ones are created.
    - Tried to fix a few more problems related to dis_stop_serving...


26/10/2009
Changes for version 19.05:
    - dis_stop_serving had stopped working in version 19.4. So all servers that undeclared
      all services and then tried to re-declare new ones would fail (corrupted server name).
      Affected in particular the DimBridge


27/08/2009
Changes for version 19.04:
    - Added the following functions:
	Server part:
		C - dis_get_n_clients(int service_id)
		C++ - int DimService::getNClients()
	Client part (C++ only):
		DimClient::setNoDataCopy()
		This will prevent any data copy in the client and the user should make 
                sure that the data received from DIM is not used outside the callback
                in order to benefir from this feature.
    - Fixed the Java DIM Jar file, it was wrong in the previous version.


31/07/2009
Changes for version 19.03:
    - Removed some more compilation warnings.
    - Fixed a bug in the DNS. The mechanism for retrieving the "SERVER_LIST" when 
      some server names were longer that 35 characters was very slow.


06/07/2009
Changes for version 19.02:
    - Fixed a bug in the server part handling of RPCs, it created a memory leak.
      It was using a separate thread to handle timeouts and there is no safe way to 
      kill a thread from outside. Fixed.
    	- the function dim_stop_thread() is now obsolete.
    - Added the possibility to change the send and receive buffer sizes:
	- int dim_set_write_buffer_size(int size)
	- int dim_get_write_buffer_size()
	- int dim_set_read_buffer_size(int size)
	- int dim_get_read_buffer_size()
      The default (and minimum) is 16384 bytes.
      These calls should be done before any other DIM calls.
    - Fixed a bug in the Java DimBrowser class (the format was not returned correctly)

04/05/2009
Changes for version 19.01:
    - A server can now publish to more than one DNS.
      To use an extra DNS:
	- in "C":
		long dnsid;
		char extra_dns[128];
		...
		dim_get_env_var("EXTRA_DNS_NODE", extra_dns, sizeof(extra_dns));
		dnsid = dis_add_dns(extra_dns,0);
		sprintf(name1,"NewService%d",i);
		dis_add_service_dns(dnsid, name1, "I", &NewData, sizeof(NewData), 
					(void *)0, 0 );
		dis_start_serving_dns(dnsid, "xx_new");

	- in C++:
		DimServerDns *newDns;
		char *extraDns = 0;
		DimService *new_servint;
		...
		extraDns = DimUtil::getEnvVar("EXTRA_DNS_NODE");
		if(extraDns)
			newDns = new DimServerDns(extraDns, 0, "new_TEST");
		...
		if(extraDns)
			new_servint = new DimService(newDns, "new_TEST/INTVAL",ival);

    - Removed all warnings from DIM sources so that it can be compiled with -Wall -Wextra on Linux
    - Changed the makefiles so that the default on Linux is now 64 bits.
	- The flag 32BITS=yes can be added in order to generate 32 bit code


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
