package dim;


public class DimClient
{
	static // force loading the native library
	{
	  Native.loadNativeLibrary();
	}

    private DimClient(){} // we do not allow instantiation of this class

    public static int sendCommand(String name, String theData)
    {
        return Client.send(name, null, Client.F_WAIT, 0, theData);
    }
    public static int sendCommand(String name, int theData)
    {
        return Client.send(name, null, Client.F_WAIT, 0, theData);
    }
    public static int sendCommand(String name, float theData)
    {
        return Client.send(name, null, Client.F_WAIT, 0, theData);
    }
    public static int sendCommand(String name, double theData)
    {
        return Client.send(name, null, Client.F_WAIT, 0, theData);
    }
    public static int sendCommand(String name, int[] theData)
    {
        return Client.send(name, null, Client.F_WAIT, 0, theData);
    }
    public static int sendCommand(String name, float[] theData)
    {
        return Client.send(name, null, Client.F_WAIT, 0, theData);
    }
    public static int sendCommand(String name, double[] theData)
    {
        return Client.send(name, null, Client.F_WAIT, 0, theData);
    }
    public static int sendCommandNB(String name, String theData)
    {
        return Client.send(name, null, 0, 0, theData);
    }
    public static int sendCommandNB(String name, int theData)
    {
        return Client.send(name, null, 0, 0, theData);
    }
    public static int sendCommandNB(String name, float theData)
    {
        return Client.send(name, null, 0, 0, theData);
    }
    public static int sendCommandNB(String name, double theData)
    {
        return Client.send(name, null, 0, 0, theData);
    }
    public static int sendCommandNB(String name, int[] theData)
    {
        return Client.send(name, null, 0, 0, theData);
    }
    public static int sendCommandNB(String name, float[] theData)
    {
        return Client.send(name, null, 0, 0, theData);
    }
    public static int sendCommandNB(String name, double[] theData)
    {
        return Client.send(name, null, 0, 0, theData);
    }
    public static int setExitHandler ( String serverName)
    {
        return Client.send(serverName + "/SET_EXIT_HANDLER", null, 0, 0, (int) 1);
    }
    public static int killServer ( String serverName)
    {
        return Client.send(serverName + "/EXIT", null, 0, 0, (int) 1);
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
		DimErrorHandler.setCltHandler(handler);
	}

	public static String getServerName()
	{
	   return Client.getServer();
	}

	public static int getServerId()
	{
	   return Client.getServerConnID();
	}

	public static int getServerPid()
	{
	   return Client.getServerPID();
	}

	public static String[] getServerServices()
	{
		String list;
		String[] services;
		list = Client.getServices();
		if(list != null)
		{
			list = list.replace('\n',',');
			services = list.split(",");
		}
		else
			services = new String[0];
		return services;
	}
	public static void stop()
	{
		Client.stop();
		Native.stop();
	}
    /**  @todo look at these not implemented client functions
     *   public static void change_address(int service_id, int new_address, int new_size);
     *   public static native int find_service(string name);		// not implemented ?
     *   public static native string get_id();				// name@node of client ??
     *   public static native int get_quality(int service_id);
     *   public static native int get_timestamp(int service_id, int *secs, int *milisecs);
     *   public static native string get_format(int service_id);
     *   public static native void disable_padding();			// not implemented ?
     */

}
