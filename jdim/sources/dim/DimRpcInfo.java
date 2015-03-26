/*
 * Created on Jan 26, 2009
 *
 */
package dim;

/**
 * @author clara
 *
 */
public class DimRpcInfo extends DimInfo{
	String rpcName;
	DimLock rpcLock;
	int itsWaiting;
	
	void subscribe(String theServiceName, int time)
	{
		itsWaiting = 1;
		rpcLock = new DimLock();
		rpcLock.reset();		
		super.subscribe(theServiceName, time);
	}
	public DimRpcInfo(String theServiceName, String noLink)
	{
		super(theServiceName+"/RpcOut", noLink);
		initRpc(theServiceName);
	}
	public DimRpcInfo(String theServiceName, int noLink)
	{
		super(theServiceName+"/RpcOut", noLink);
		initRpc(theServiceName);
	}
	public DimRpcInfo(String theServiceName, float noLink)
	{
		super(theServiceName+"/RpcOut", noLink);
		initRpc(theServiceName);
	}
	public DimRpcInfo(String theServiceName, double noLink)
	{
		super(theServiceName+"/RpcOut", noLink);
		initRpc(theServiceName);
	}
	void initRpc(String name)
	{
		rpcName = name;	
		wait(0);
	}
	public void setData(String data)
	{
		setup();
		DimClient.sendCommandNB(rpcName+"/RpcIn",data);
		wait(0);
	}
	public void setData(int data)
	{
		setup();
		DimClient.sendCommandNB(rpcName+"/RpcIn",data);
		wait(0);
	}
	public void setData(float data)
	{
		setup();
		DimClient.sendCommandNB(rpcName+"/RpcIn",data);
		wait(0);
	}
	public void setData(double data)
	{
		setup();
		DimClient.sendCommandNB(rpcName+"/RpcIn",data);
		wait(0);
	}
	public void setData(int[] data)
	{
		setup();
		DimClient.sendCommandNB(rpcName+"/RpcIn",data);
		wait(0);
	}
	public void setData(float[] data)
	{
		setup();
		DimClient.sendCommandNB(rpcName+"/RpcIn",data);
		wait(0);
	}
	public void setData(double[] data)
	{
		setup();
		DimClient.sendCommandNB(rpcName+"/RpcIn",data);
		wait(0);
	}
	
	
	public void setData(String data, int tout)
	{
		setup();
		DimClient.sendCommandNB(rpcName+"/RpcIn",data);
		wait(tout);
	}
	public void setData(int data, int tout)
	{
		setup();
		DimClient.sendCommandNB(rpcName+"/RpcIn",data);
		wait(tout);
	}
	public void setData(float data, int tout)
	{
		setup();
		DimClient.sendCommandNB(rpcName+"/RpcIn",data);
		wait(tout);
	}
	public void setData(double data, int tout)
	{
		setup();
		DimClient.sendCommandNB(rpcName+"/RpcIn",data);
		wait(tout);
	}
	public void setData(int[] data, int tout)
	{
		setup();
		DimClient.sendCommandNB(rpcName+"/RpcIn",data);
		wait(tout);
	}
	public void setData(float[] data, int tout)
	{
		setup();
		DimClient.sendCommandNB(rpcName+"/RpcIn",data);
		wait(tout);
	}
	public void setData(double[] data, int tout)
	{
		setup();
		DimClient.sendCommandNB(rpcName+"/RpcIn",data);
		wait(tout);
	}
	void setup()
	{
		rpcLock.reset();		
		itsWaiting = 1;
	}
	int wait(int tout)
	{
		int ret = 1;
		ret = rpcLock.dimWait(tout);
		itsWaiting = 0;
		return ret;
	}
	public void infoHandler()
	{
		if(itsWaiting == 0)
			return;
		rpcLock.dimWakeUp();
	}
};

