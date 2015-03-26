
                    DIM version 13.10 Release Notes

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

NOTE 3: The Version Number service provided by servers is now set to 1310
	(version 13.10).

06/10/2003
Changes for version 13.0:
      - Fixed all know bugs resulting from:
	- A modification done since v12 which allowed tcpip writes to 
	  proceed in parallel with reads (in a different thread). This 
	  created a few problems with the timming of connections and 
	  disconnections.
	- Extensive tests of the Java interface to DIM due to the DIP
	  implementation tests.

14/10/2003
Changes for version 13.1:
      - Fixed an extra bug related to having servers and clients within
	the same process.

13/11/2003
Changes for version 13.2:
      - Fixed a bug in the RPC client - the size of the message was 
	sometimes wrong.
      - When the number of DIM services declared by a server was a
        multiple of 100 the clients would not get updated on server
	restart - Fixed.
      - If a client exited immediately after a command with callback
        The server would sometimes not get the command - hopefully
	fixed. 
	Note: This is still the case for a command without callback 
	(this is a feature of the asynchronous method). 

2/12/2003
Changes for version 13.3:
      - Fixed the linux makefile (realclean).
      - the C++ version of stop timer now returns the number of seconds 
	left to sleep (used to be void).
 
13/01/2004
Changes for version 13.4:
      - The DNS now accepts an environment variable "DIM_DNS_ACCEPTED_DOMAINS".
	It will refuse connections from servers running outside these domains
	(actually, at the moment it will kill the servers -> to be modified).
	Ex.: DIM_DNS_NODE=cern.ch,slac.stanford.edu
      - the Java version now implements a DimBrowser class, similar to the C++ 
	one. And the complex data types are better handled.
 
27/01/2004
Changes for version 13.5:
      - A socket close modification since v12r8 for Linux was causing problems,
	put back as it was.
      - dim_send_command had a limitation to 80 characters for a DIM service
	name for no reason. DIM service names are limited to 128 characters.

28/01/2004
Changes for version 13.6:
      - When sending an EXIT command to a server the client wouldn't behave
	properly on Linux - fixed 

30/01/2004
Changes for version 13.7:
      - Commands would sometimes be remembered by a client and sent later when
	the server started up - fixed!

06/02/2004
Changes for version 13.8
      - The Name server would only remember server names up to 40 characters,
	so when asked for the list of servers known it would truncate the names.
	Fixed.
      - The handling of disconnections/reconnections for very "fast" servers was
	causing problems to the client: callback not called or reconnection failed.
	fixed.

27/02/2004
Changes for version 13.9
      - Replaced "ctime()" in dim_print_date_time() by its reentrant version ctime_r
	for the Linux version.
      - Added selectiveUpdateService() to the methods in the Java class DimService.
      - The user set server timestamp was being reset (and replaced by the current
	time) if the service was sent to several clients - Fixed.
      - Sometimes DIM commands could go out of order if a client was sending the same
	command (different data) out very fast - fixed.

16/03/2004
Changes for version 13.10
      - Fixed the DimBrowser Java class
      - The timer thread was sometimes not counting time properly when interrupted
	every second -> fixed.
      - The Client sometimes forgot to call the command callback when the DNS died
	while sending a command with callback -> fixed. 

Please check the Manual for more information at:
    http://www.cern.ch/dim
