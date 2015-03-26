package dim;


public class DimServer
{

	static // force loading the native library
	{
	  Native.loadNativeLibrary();
	}

    private DimServer(){} // we do not allow instantiation of this class

	public static void start(String serverName)
	{
		Server.setServerName(serverName);
		Server.registerServices();
	}
	public static void start()
	{
		Server.registerServices();
	}
	public static void stop()
	{
	   Server.stopServing();
	}
	
	public static String getClientName()
	{
	   return Server.getClient();
	}

	public static int getClientId()
	{
	   return Server.getClientConnID();
	}

	public static String[] getClientServices()
	{
		String list;
		String[] services;
		list = Server.getServices();
		if(list != null)
		{
			list = list.replace('\n',',');
			services = list.split(",");
		}
		else
			services = new String[0];
	   	return services;
	}

	public static native void disableAST();
	public static native void enableAST();

	public static void setDnsNode(String nodes, int port)
	{
		setDnsPort(port);
		setDnsNode(nodes);
	}	
	public static native void setDnsNode(String nodes);
	static native void setDnsPort(int port);
	public static native String getDnsNode();
	public static native int getDnsPort();
	
	public static void addErrorHandler(DimErrorHandler handler)
	{
		DimErrorHandler.setSrvHandler(handler);
	}

	public static void addExitHandler(DimExitHandler handler)
	{
		DimExitHandler.setHandler(handler);
	}

   /** @todo look at these not implemented server functions
      * int    get_next_cmnd(int *tag, int *buffer, int *size );
      *        public static native int find_service(string name);
      * void add_client_exit_handler(void (*usr_routine)(...));
      * void set_client_exit_handler(int conn_id, int tag);
      * void add_exit_handler(void (*usr_routine)(...));
      * void report_service(char *service_name);
      * void send_service(int service_id, int *buffer, int size);	// where is this used?
      * int  set_buffer_size(int size);
      * void set_quality(int service_id, int quality);
      * void set_timestamp(int service_id, int secs, int millisecs);
      * void disable_padding();
     */

}
