package dim;

public class DimCurrentInfo extends DimInfo
{		
	public DimCurrentInfo(String theServiceName, String noLink)
	{
		super(theServiceName,noLink);
	}
	public DimCurrentInfo(String theServiceName, int noLink)
	{
		super(theServiceName,noLink);
	}
	public DimCurrentInfo(String theServiceName, float noLink)
	{
		super(theServiceName,noLink);
	}
	public DimCurrentInfo(String theServiceName, double noLink)
	{
		super(theServiceName,noLink);
	}
	public DimCurrentInfo(String theServiceName, int time, String noLink)
	{
		super(theServiceName,time,noLink);
	}
	public DimCurrentInfo(String theServiceName, int time, int noLink)
	{
		super(theServiceName,time,noLink);
	}
	public DimCurrentInfo(String theServiceName, int time, float noLink)
	{
		super(theServiceName,time,noLink);
	}
	public DimCurrentInfo(String theServiceName, int time, double noLink)
	{
		super(theServiceName,time,noLink);
	}
	
	void subscribe(String theServiceName, int time)
	{
		do_subscribe();
		service_id = Client.infoService(theServiceName, 
										this, 
										Client.ONCE_ONLY|Client.F_WAIT|Client.F_STAMPED,
										time);
	}
	
}
