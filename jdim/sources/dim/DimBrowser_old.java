package dim;

public class DimBrowser
{
	static String answer;
	static String[] allServices;
	static DimInfo serverSrvc;
	static String serverAnswer;
	static String pids;
	static String[] allServers;
	static String[] allNodes;
	static String[] allPids;
	static DimRpcInfo dnsRpc;
		
    private DimBrowser()// we do not allow instantiation of this class
    {
     	allServices = null;
    	serverSrvc = null;
    	allServers = null;
		allNodes = null;
		allPids = null;
//		dnsRpc = null;
    } 

	public static synchronized String[] getServers()
	{
		serverSrvc = new DimCurrentInfo("DIS_DNS/SERVER_LIST","__DIM_NO_LINK__")
		{
			public void infoHandler()
			{
				serverAnswer = getString();
				pids = getString();
				if(serverAnswer.compareTo("__DIM_NO_LINK__") == 0)
				{
					serverAnswer = "";
				}
			}
		};

		String[] servers;
		int len;
		if(serverAnswer.length() != 0)
		{
			servers = serverAnswer.split("@");
			len = servers.length - 1;
		}
		else
		{
			servers = new String[0];
			return servers;
		}

		String curr;
		allServers = new String [len];
		allNodes = new String[len];
		allPids = new String[len];
		
		curr = serverAnswer;
		int i = 0;
		while(true)
		{
			int index = curr.indexOf('|');
			if(index < 0)
				break;
			allServers[i] = curr.substring(0,index);
			i++;
			curr = curr.substring(index+1);
		}
		if(curr.length()> 0)
			allServers[i] = curr;
			
		for(i = 0; i < allServers.length; i++)
		{
			int index = allServers[i].indexOf('@');
			allNodes[i] = allServers[i].substring(index+1);			
			allServers[i] = allServers[i].substring(0,index);			
		}
		curr = pids;
		i = 0;
		while(true)
		{
			int index = curr.indexOf('|');
			if(index < 0)
				break;
			allPids[i] = curr.substring(0,index);
			i++;
			curr = curr.substring(index+1);
		}
		if(curr.length()> 0)
			allPids[i] = curr;
			
		return allServers;
	}
	private static int findServer(String server)
	{
		for(int i = 0; i < allServers.length;i++)
		{
			if(server.compareTo(allServers[i]) == 0)
				return i;
		}
		return -1;    	
	}
	public static String getServerNode(String server)
	{
		int index = findServer(server);
		if(index >= 0)
			return allNodes[index];
		else
			return "";
	}
	public static int getServerPid(String server)
	{
		String pidstr;
		int pid = 0;
		int index = findServer(server);
		if(index >= 0)
		{
			pidstr = allPids[index];
			pid = Integer.parseInt(pidstr.trim());
		}
		return pid;
	}
    public static synchronized String[] getServices(String pattern)
    {  
  		if(dnsRpc == null)
			dnsRpc = new DimRpcInfo("DIS_DNS/SERVICE_INFO","");
    	dnsRpc.setData(pattern);
    	answer = dnsRpc.getString();
		if(answer.length() != 0)
		{
			allServices = answer.split("\n");
		}
		else
		{
			allServices = new String[0];
		}
		String[] services = new String [allServices.length];
		for(int i = 0; i < allServices.length;i++)
		{
			int index = allServices[i].indexOf('|');
			services[i] = allServices[i].substring(0,index);
		}
		return services;
    }
    public static synchronized boolean isCommand(String service)
    {
		int index = findService(service);
		if(index == -1)
			return false;
		int i = allServices[index].lastIndexOf('|');
		if(allServices[index].substring(i+1).compareTo("CMD") == 0)
			return true;
		return false;
    }
	public static synchronized String getFormat(String service)
	{
		int index = findService(service);
		if(index == -1)
			return null;
		int i = allServices[index].indexOf('|');
		int j = allServices[index].lastIndexOf('|');
		String format = allServices[index].substring(i+1,j);
		return format;
	}
    private static int findService(String service)
    {
		for(int i = 0; i < allServices.length;i++)
		{
			int index = allServices[i].indexOf('|');
			if(service.compareTo(allServices[i].substring(0,index)) == 0)
				return i;
		}
		return -1;    	
    }
	public static void stopBrowsing()
	{
	}
}

