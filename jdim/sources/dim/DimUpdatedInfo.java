package dim;

public class DimUpdatedInfo extends DimInfo
{		
	public DimUpdatedInfo(String theServiceName, String noLink)
	{
		super(theServiceName,noLink);
	}
	public DimUpdatedInfo(String theServiceName, int noLink)
	{
		super(theServiceName,noLink);
	}
	public DimUpdatedInfo(String theServiceName, float noLink)
	{
		super(theServiceName,noLink);
	}
	public DimUpdatedInfo(String theServiceName, double noLink)
	{
		super(theServiceName,noLink);
	}
	public DimUpdatedInfo(String theServiceName, int time, String noLink)
	{
		super(theServiceName,time,noLink);
	}
	public DimUpdatedInfo(String theServiceName, int time, int noLink)
	{
		super(theServiceName,time,noLink);
	}
	public DimUpdatedInfo(String theServiceName, int time, float noLink)
	{
		super(theServiceName,time,noLink);
	}
	public DimUpdatedInfo(String theServiceName, int time, double noLink)
	{
		super(theServiceName,time,noLink);
	}
	
	void subscribe(String theServiceName, int time)
	{
		do_subscribe();
		service_id = Client.infoService(theServiceName, 
										this, 
										Client.UPDATE|Client.F_STAMPED,
										time);
	}
	
}
