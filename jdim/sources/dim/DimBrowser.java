package dim;

import java.util.*;

public class DimBrowser
{
	static DimRpcInfo dnsRpc = null;
	static String serverAnswer;
	static Vector allServers = new Vector();
	static String[] allNodes;
	static String[] allPids;
		
	static DimCurrentInfo serverSrvc = null;
	static String serviceAnswer;
	static Vector allServices = new Vector();
	static String[] allQuals;
	static String[] allFormats;

    private DimBrowser()// we do not allow instantiation of this class
    {
    } 

	public static synchronized String[] getServers()
	{
		serverSrvc = new DimCurrentInfo("DIS_DNS/SERVER_LIST","__DIM_NO_LINK__")
		{
			public void infoHandler()
			{
				serverAnswer = getString();
				allPids = getString().split("\\|");
				if(serverAnswer.compareTo("__DIM_NO_LINK__") == 0)
				{
					serverAnswer = "";
				}
			}
		};

		String[] serverInfo;
		
		if(serverAnswer.length() != 0)
			serverInfo = serverAnswer.split("\\|");
		else
			serverInfo = new String[0];
		allServers.clear();
		allNodes = new String[serverInfo.length];
		for (int i=0 ; i<serverInfo.length ; i++)
		{
			String[] tmp = serverInfo[i].split("@");
			if (tmp.length==2)
			{
				allServers.add(tmp[0]);
				allNodes[i] = tmp[1];
			}
		}
		return (String[])allServers.toArray(new String[0]);
	}

	public static String getServerNode(String server)
	{
		int index = allServers.indexOf(server);
		if(index >= 0)
			return allNodes[index];
		else
			return "";
	}
	public static int getServerPid(String server)
	{
		String pidstr;
		int pid = 0;
		int index = allServers.indexOf(server);
		if(index >= 0)
		{
			pidstr = allPids[index];
			pid = Integer.parseInt(pidstr.trim());
		}
		return pid;
	}
    public static synchronized String[] getServices(String pattern)
    {  
		String[] services;
  		if(dnsRpc == null)
			dnsRpc = new DimRpcInfo("DIS_DNS/SERVICE_INFO","");
    	dnsRpc.setData(pattern);
    	serviceAnswer = dnsRpc.getString();
		if(serviceAnswer.length() != 0)
			services = serviceAnswer.split("\n");
		else
			services = new String[0];
		allQuals = new String[services.length];
		allFormats = new String[services.length];
		allServices.clear();
		for(int i = 0; i < services.length;i++)
		{
			String[] tmp = (services[i]+" ").split("\\|");
			if (tmp.length == 3)
			{
				allServices.add(tmp[0]);
				allFormats[i] = tmp[1];
				allQuals[i] = tmp[2].trim();
//System.out.println(i+" "+tmp[0]+" "+tmp[1]+" "+tmp[2]);
				services[i] = tmp[0];
			}
		}
		return services;
    }
    public static synchronized boolean isCommand(String service)
    {
		int index = allServices.indexOf(service);
//		System.out.println("getQual"+" "+index);
		if(index == -1)
			return false;
		return allQuals[index].equals("CMD");
    }
	public static synchronized String getFormat(String service)
	{
		int index = allServices.indexOf(service);
//System.out.println("getFormat"+" "+index);
		if(index == -1)
			return null;
		return allFormats[index];
	}
 	public static void stopBrowsing()
	{
	}
}

