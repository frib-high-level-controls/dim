
                    DIM version 15.23 Release Notes

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

NOTE 3: The Version Number service provided by servers is now set to 1523
	(version 15.23).

17/09/2004
Changes for version 15.0:
    - Changes for 64 bit machine (LP64 architecture) support
	- All DIM "tags" are now longs instead of ints this affects:
	    - Client callback parameters
	    - Server callback parameters
	    - Timer callback parameters
	  (The reason is: tags were very often user to pass pointers) 
	- DIM is now compiled with -fPIC by default on Linux
	- The byte swapping and structure padding was fixed.

14/10/2004
Changes for version 15.1:
    - Big Bug Fixed affecting the DIM_DNS for windows!!!!
	- Windows has an hidden default limit of the number of sockets
	  per process set to 64, only partially though, more sockets can
	  be created with no problem but they are silently masked out
          by the select call!
	- Anyway this limit is now set to 1024.
    - removed a few print statements.

28/10/2004
Changes for version 15.2:
    - Removed some C++ style comments which did not compile on Solaris.

02/11/2004
Changes for version 15.3:
    - Byte swapping was missing in one place when asking for stampped 
      services (noticed on Solaris)

11/11/2004
Changes for version 15.4:
    - Added two command line options to dim_send_command:
	dim_send_command <cmnd_name> [<data>] [-dns <dim_dns_node>] [-s]
        -dns allows setting the dim_dns_node, and -s means silent.      

03/12/2004
Changes for version 15.5:
    - Changed the bahaviour of the DNS "KILL_SERVERS" command. Now if the
      user declares a server exit_handler, the server will not exit (unless
      the user code explicilty exits) and will continue running fine, 
      otherwise the server exits as before.
    - The exit_handler now gets as parameter the error code that caused the
      exit request (so that the user can decide wether to exit or not) or 
      the code sent by the client, if the EXIT request came from a client. 
      As a result clients should not send exit codes lower than 0x100 in order 
      not to be confused with the internal error codes.
    - IMPORTANT NOTES:
      - The behaviour of the user error_handler() changed. Now the
	error_handler only gets called to report an error. If the user wants
	to modify the automatic exit behaviour he/she has to also declare an
	exit_handler. In previous versions if an error_handler was declared,
	the exit_handler was not necessary.
      - Also the Java version has changed since the ERROR codes changed.
   
07/12/2004
Changes for version 15.5-1:
    - Corrected a bug in include file dis.hxx introduced in v15r5

08/12/2004
Changes for version 15.6:
    - Included support for Scheduling policies and priorities between DIM
      threads by creating the calls:
	- dim_set_scheduler_class(int sched_class)
	- dim_get_scheduler_calss(int *sched_class)
	- dim_set_priority(int dim_thread, int priority)
	- dim_get_priority(int dim_thread, int priority)

      These calls are only implemented on Linux and Windows and they have 
      different behaviour on the two platforms:
	- dim_set_scheduler_class(int sched_class)
	On Windows:
	  sched_class is the process's priority class:
		-1 = IDLE_PRIORITY_CLASS
		 0 = NORMAL_PRIORITY_CLASS
		 1 = HIGH_PRIORITY_CLASS
		 2 = REALTIME_PRIORITY_CLASS
	On Linux:
	  sched_class is the process's schedule policy:
		 0 = SCHED_OTHER
		 1 = SCHED_FIFO
		 2 = SCHED_RR
	  All threads in the process will be set to this sched_class
	
	- dim_set_priority(int dim_thread, int priority)
	  where dim_thread : 1 - Main thread, 2 - IO thread, 3 - Timer thread
	On Windows:
	  priority is the thread's relative priority:
		-3 = THREAD_PRIORITY_IDLE
		-2 = THREAD_PRIORITY_LOWEST
		-1 = THREAD_PRIORITY_BELOW_NORMAL
		 0 = THREAD_PRIORITY_NORMAL
		 1 = THREAD_PRIORITY_ABOVE_NORMAL
		 2 = THREAD_PRIORITY_HIGHEST
		 3 = THREAD_PRIORITY_TIME_CRITICAL

	On Linux:
	  priority is the thread's absolute priority:
		 0 	for SCHED_OTHER
		 1 - 99 for SCHED_FIFO or SCHED_RR

03/02/2005
Changes for version 15.7:
    - Fixed a bug that made DIM servers crash when unplugging for a short time the
      network cable.
    - Linux executables and libraries are now compiled on Linux SLC3 with gcc 3.2.3
    - Contains a new version of the DIM Tree Browser for WIndows. 

02/03/2005
Changes for version 15.8:
    - Contains again a new version of the DIM Tree Browser for WIndows, now allows
      to display structures correctly (Thanks to Serguei Sergueev). 
    - DIM used old style predefined macros, for example linux instead of __linux__.
      So it didn't compile when users used gcc/g++ -ansi -pedantic. Fixed.
    - A check is now made on the length of a DIM service name. The service is 
      discarded if the name is longer then 131 characters.

04/04/2005
Changes for version 15.9:
    - Ported to MacOSX (Darwin). "OS" has to be defined as "Darwin":
      	- Replaced ftime() by gettimeofday()
	- Used sem_open instead of sem_init on Darwin. (sem_init not implemented).
    - Made some order in the macro definition, so that it works whether for example 
      unix or __unix__ are defined.
    - Sometimes when a server received a command, the connection to the client wasn't
      completely setup yet, so DimServer::getClientName() would not return the correct
      result - Fixed.

11/04/2005
Changes for version 15.10:
    - Fixed a memory leak that happened in clients when sending commands to a 
      non-existing server.

15/04/2005
Changes for version 15.11:
    - Optimized DIM for servers with many services (in particular the server library).
    - Uses a better Hash function in Dns and servers.

20/04/2005
Changes for version 15.12:
    - DIM did not update a service if it contained no data - fixed.

22/04/2005
Changes for version 15.13:
    - Fixed a bug introduced in version 15.11, servers did not reconnect anymore
      when the DNS restarted.

02/05/2005
Changes for version 15.14:
    - Fixed several features or bugs related to the optimizations for many services:
	- Sometimes a server declaring the same services would not exit properly
	- If a browser was open it would slow down enourmously the start up of the 
	  server
	- A server sometimes crashed while declaring the services

24/05/2005
Changes for version 15.15:
    - Fixed some warnings reported by "valgrind" mostly related to delete[]

30/05/2005
Changes for version 15.16:
    - Fixed a bug related to a server name longer than 40 characters.

16/06/2005
Changes for version 15.17:
    - Included the call keepWaiting() in DimRpcInfo. To allow multiple client
      RPCs to use the same Server RPC (the user still has to provide an id).

20/06/2005
Changes for version 15.18:
    - Included support for creating user threads:
	- From "C":
		int dim_start_thread(void (*thread_ast)(), long tag)
	- From C++:
		class DimThread
		{
		public:
			DimThread();
			virtual ~DimThread();
			int start();
			virtual void threadHandler() { };
		};

27/06/2005
Changes for version 15.19:
    - Added the possibility to decide not to update a service in a server callback
      by returning a negative size (to be used with care, since the client will
      timeout if the server stops responding for too long).

15/08/2005
Changes for version 15.20:
    - Fixed a bug which could make a DIM server loop forever (happened to tmSrv)

19/08/2005
Changes for version 15.21:
    - Fixed a bug in the DNS introduced in version v15r7: would not report and react
      properly, in some ocasions, to previously declared services.

05/10/2005
Changes for version 15.22:
    - Fixed some error messages reported by the DNS, they were not correct.

04/11/2005
Changes for version 15.23:
    - Fixed several bugs in the timer handling mechanisms. Could provoke fake timeouts.

Please check the Manual for more information at:
    http://www.cern.ch/dim
