
                    DIM version 14.07 Release Notes

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

NOTE 3: The Version Number service provided by servers is now set to 1407
	(version 14.07).

07/04/2004
Changes for version 14.0:
      - Spring Cleanup
	Cleaned up inline methods out of include files.
	Fixed some virtual destructors
	Removed some unnecessary system include files
	Fixed ctime_r to work on all platforms (Solaris, LynxOS too)

21/04/2004
Changes for version 14.1:
      - Now if a server declares an exit handler (DimServer::addExitHandler
	or dis_add_exit_handler) the user is responsible for exiting.
	DIM will not exit by itself anymore.
      - The windows version now also distributes MSVCRTD.DLL.
      - Fixed a bug where servers could get confused if similar longish
	service names were used in different servers.   

18/05/2004
Changes for version 14.2:
      - Cleaned up some "C" warnings
      - Linux Did didn't display RPC formats properly - fixed.
      - RPC structures were not transfered correctly back to the client,
	the format of the structure was currupted - fixed.
      - Added a New constructor and updateService method for the Java class 
	DimService. These allow the creation and update of a DimService by
	passing another DimService. The data and format of the DimService
	will be copied to the new one. Usefull for structured data.

02/06/2004
Changes for version 14.2-1:
      - Fixed a bug in the java version that would make DIP crash when 
	extracting data items (file format.java)

12/06/2004
Changes for version 14.3:

      - DIM Server Exit_handler: A server can specify an exit handler
	by using:
		dis_add_exit_handler(exitHandler) or 
		DimServer::addExitHandler()/virtual void exitHandler()
	The exitHandler will be called in the following conditions:
		- DNS node undefined
		- Services already declared in DNS
		- DNS doesn't accept connections from this machine
		- An EXIT command from a client
	If the user doesn't declare an exit handler, the DIM server will
	exit, otherwise it is up to the user.

      - DIM Error_handler: A server or a client can specify an error_handler
	by using:
		dis_add_error_handler(errorHandler)
		dic_add_error_handler(errorHandler) 
		DimServer::addErrorHandler()/virtual void errorHandler(...)
		DimClient::addErrorHandler()/virtual void errorHandler(...) 
	The error_handler will be called whenever DIM wants to report an 
	error (in this case all stdout/stderr prints will be suppressed).
	The errorHandler is called with the following parameters:
		int severity:
			0: info
			1: warning
			2: error
			3: fatal
		int error_code
			possible codes listed in dim_common.h
		char *msg
	If the user declared an error handler for a server and an error with
	severity = fatal is received the exit handler will also be called.
	For a client if an error with severity = fatal is received and there
	is no error handler declared the process will exit.

      - Java DIM: the same functionality is available in the form of a 
	DimErrorHandler class and a DimExitHandler class which can be used as 
	in the example:
	public static void main(String[] args) {
		DimErrorHandler erid = new DimErrorHandler()
		{
			public void errorHandler(int severity, int code, String msg)
			{
				System.out.println("Error: "+msg+" sev: "+
					severity);
			}
		};
		DimExitHandler exid = new DimExitHandler()
		{
			public void exitHandler(int code)
			{
				System.out.println("Exit: "+code);
			}
		};
		...

      - Getting and Setting the DIM_DNS_NODE from a program:
	The calls:
		dim_set_dns_node
		dim_get_dns_node 
        and the corresponding C++ calls:
		DimServer::setDnsNode and DimServer::getDnsNode, 
		DimClient::setDnsNode and DimClient::getDnsNode
 	already existed, 
	they have now been complemented with:
		dim_set_dns_port
		dim_get_dns_port
	and the static C++ calls: 
		DimServer::setDnsNode(node, port),DimServer::getDnsPort(), 
		DimClient::setDnsNode(node, port), DimClient::getDnsPort()

      - In Java the folowing methods have been added to the classes DimServer
	and DimClient:	
		public static void setDnsNode(String nodes);
		public static void setDnsNode(String nodes, int port);
		public static String getDnsNode();
		public static int getDnsPort();

      - the Java method DimService.getName() has been added.

      - In C++ the creation of a new DimService could fail, for example, if
	the service already existed in this server, this was not reported, since
	a constructor can't return a value.
	With the new error handling mechanisms, a user can declare an error_handler,
	check if the error code is DIMSVCDUPLC and generate an exception which
	will be thrown withing the DimService creation.
      - Dim used to pass timestamps between servers and clients as an integer
	for seconds since January 1970 and a short for milliseconds.
	Now it will be one integer also for the milliseconds. By default DIM
	still uses milliseconds, but if a server passes nanoseconds to 
	dis_set_timestamp the client will receive nanoseconds when doing 
	dic_get_timestamp.

08/07/2004
Changes for version 14.4:

      - Java DIM: In order to allow a different error handler to be called for the
	server and the client (if both in the same process) The calls:
		DimServer.addErrorHandler(DimErrorHandler handler) and 
		DimClient.addErrorHandler(DimErrorHandler handler)
	Should be called respectively by the server or the client in order to install
	the Error Handler. For compatibitlity, the call:
		DimServer.addExitHandler(DimErrorHandler handler)
	was also added. 
 
	They can now be used as in the example (for a server):
	public static void main(String[] args) {
		DimErrorHandler erid = new DimErrorHandler()
		{
			public void errorHandler(int severity, int code, String msg)
			{
				System.out.println("Error: "+msg+" sev: "+
					severity);
			}
		};
		DimServer.addErrorHandler(erid);
		DimExitHandler exid = new DimExitHandler()
		{
			public void exitHandler(int code)
			{
				System.out.println("Exit: "+code);
			}
		};
		DimServer.addExitHandler(exit);
		...

      - In a server it was already possible to find out inside a callback (for example
	when a command was received):
	    - from which client id the message came:
		int dis_get_conn_id()
            - The name of this client in the form <pid>@<node_name>
		int dis_get_client(char *name)
	The equivalent C++ calls:
	    	int DimServer::getClientId();
		char *DimServer::getClientName();
	And Java calls:
	    	int DimServer.getClientId();
		String DimServer.getClientName();
	Where also available.

	These calls were now complemented in 3 ways:
	1 - They can be done also by clients to find out which server is providing a service
	    inside a callback (for example when a service is received).
	    For this purpose the following "C" calls where added:
	    - from which server id the message came:
		int dic_get_conn_id()
            - The name of this server in the form <server_name>@<node_name>
		int dic_get_server(char *name)
	      And The equivalent C++ calls:
	    	int DimClient::getServerId();
		char *DimClient::getServerName();
	      And Java calls:
	    	int DimClient.getServerId();
		String DimClient.getServerName();
	2 - These calls are also available inside the errorHandler callbacks.
	    Can be used to find out if the error originated from a specific connection, 
            in which case conn_id (or clientID or ServerId) != 0. 
	    Or if it is a generic error, afecting all connections in which case conn_id = 0.
	3 - A new type of calls has been added which allows to find out:
	    For a server - which services are being used by the current client (i.e. the
	    		   client that triggered the execution of this callback)
	    For a client - Which services are being provided by the current server (i.e
			   the server that triggered the execution of this callback)

	    This calls are the following in "C":
		- char *dis_get_client_services(conn_id)
		- char *dic_get_server_services(conn_id)
		They return a list of services separated by '\n'
	    In C++:
		- char **DimServer::getClientServices();
		- char **DimClient::getServerServices();
		They return an array of pointers to service names. The array is terminated by
		a null pointer.
	    In Java:
		- String[] DimServer.getClientServices();
		- String[] DimClient.getServerServices();

	An example in C++ of the usage of the new calls in an ErrorHandler:

	class ErrorHandler : public DimErrorHandler
	{
		void errorHandler(int severity, int code, char *msg)
		{
			int index = 0;
			char **services;
			cout << severity << " " << msg << endl;
			services = DimClient::getServerServices();
			cout<< "from "<< DimClient::getServerName() << " services:" << endl;
			while(services[index])
			{
				cout << services[index] << endl;
				index++;
			}
		}
	public:
		ErrorHandler() {DimClient::addErrorHandler(this);}
	};

	And in Java:

	...
	DimErrorHandler eid = new DimErrorHandler()
	{
		public void errorHandler(int severity, int code, String msg)
		{
			System.out.println("Error: "+msg+" sev: "+severity);
			String[] list = DimClient.getServerServices();
			System.out.println("Services: ");
			for(int i = 0; i < list.length; i++)
				System.out.println(list[i]);
		}
	};
	DimClient.addErrorHandler(eid);

02/08/2004
Changes for version 14.5:
    - Fixed a bug in dic.c - related to commands terminating after a connection was closed,
      affected in particular DimBrowser "RPC" calls. 

02/08/2004
Changes for version 14.6:
    - Noticed that since the changes of version v14r4 the Dns was not printing any error
      messages anymore - Fixed.
    - In Windows SO_REUSEADDR doesn't work properly, so two DNSs could be running at the 
      same time using the same port number on the same PC - Fixed, now like in Linux, the
      second one exits (printing an error message).

10/08/2004
Changes for version 14.7:
    - The TCPIP error "Host Unknown" was not treated or reported properly. A client could
      report DNS found when the DNS node was set to be an unexisting machine - fixed.

Please check the Manual for more information at:
    http://www.cern.ch/dim
