package dim.test;

import dim.*;

/**
 * This class serves both as a complete test of the Dim package, as well as a show case for the capabilities of DIM.
 * This class is still under construction.
 * @author M.Jonker Cern
 * @version v1.2
 */

public class TestClient
{

  public static void start_timer(int secs)
  {
	DimTimer atimer = new DimTimer(secs)
	{
		public void timerHandler()
		{
			System.out.println("tick");
			start_timer(10);
		}
	};
  }

  public static void main(String[] args)
  {
//	DimClient.setDnsNode("pclhcb155.cern.ch");
	System.out.println(DimClient.getDnsNode()+" "+DimClient.getDnsPort());
/*
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
*/
//  	DimBrowser.getServices("EP/AAA/*");

	System.out.println("asking for "+"*/ITBOARD1/*");
	String[] srvcs = DimBrowser.getServices("*/ITBOARD1/*");
	System.out.println("got "+srvcs.length);
	
	for(int i = 0; i <srvcs.length; i++)
	{
		System.out.println("i: "+i+" - "+srvcs[i]);
		System.out.println(DimBrowser.getFormat(srvcs[i]));			
		System.out.println(DimBrowser.isCommand(srvcs[i]));			
	}

	System.out.println("asking for "+"*_RUNINFO/*");
	srvcs = DimBrowser.getServices("*_RUNINFO/*");
	System.out.println("got "+srvcs.length);

	for(int i = 0; i <srvcs.length; i++)
	{
		System.out.println("i: "+i+" - "+srvcs[i]);
		System.out.println(DimBrowser.getFormat(srvcs[i]));			
		System.out.println(DimBrowser.isCommand(srvcs[i]));			
	}

//	System.out.println("asking for "+"EP/*");
//	srvcs = DimBrowser.getServices("EP/*");
//	System.out.println("got "+srvcs.length);
/*
	for(int i = 0; i <srvcs.length; i++)
	{
		System.out.println("i: "+i+" - "+srvcs[i]);
		System.out.println(DimBrowser.getFormat(srvcs[i]));			
		System.out.println(DimBrowser.isCommand(srvcs[i]));			
	}
*/

	String[] servers = DimBrowser.getServers();
	for(int i = 0; i <servers.length; i++)
	{
		System.out.println("i: "+i+" - "+servers[i]);
		System.out.println(DimBrowser.getServerNode(servers[i]));			
		System.out.println(DimBrowser.getServerPid(servers[i]));			
	}

/*
	DimBrowser.getServices("EP/COOLING/*");
	DimInfo tt2 = new DimInfo("TEST_IT_INT", -1)
	{
		public void infoHandler()
		{
			int i;
			System.out.println("Received "+getInt());
			System.out.println("Quality "+getQuality());
			System.out.println("Timestamp "+getTimestamp());
			String[] list = DimClient.getServerServices();
			System.out.println("Services: ");
			for(i = 0; i < list.length; i++)
				System.out.println(list[i]);
		}
	};
	TestSrvc tt;
	DimInfo tt1 = new DimCurrentInfo("xx/Service_000", "Not there");
	System.out.println(tt1.getString());
	tt = new TestSrvc("TEST_IT_ARRAY", -1);
	TestSrvcStr ttstr = new TestSrvcStr("TEST_IT_STR","none");
	TestSrvcMix ttmix = new TestSrvcMix("TEST_MIX",-1);
	
//	System.out.println("starting timer");  
//	start_timer(10);

	int i = 0;
	
	TestRPC testRpc = new TestRPC("RPC", -1);
	
	testRpc.setData(i, 5);
*/
//	TestUpdSrvc testUpd = new TestUpdSrvc("TEST/INTVAL", -1);
//	System.out.println("Subscribed");  
			
//	while(true)
//	{
		DimTimer.sleep(10);	
//		i++;
//		testRpc.setData(i, 5);
//		System.out.println("Sending command: "+ i);  
//		DimClient.sendCommand("TestCommand",i++);
//		if(i == 3)
//			break;
//	}
	
  }
}

class TestUpdSrvc extends DimUpdatedInfo
{
	
	TestUpdSrvc(String name, int noLink)
	{
		super(name, noLink);
	}
	public void infoHandler()
	{
		System.out.print("Received int, value: ");
		int val = getInt();
		System.out.println(val);
	}
}

/*
class TestSrvc extends DimInfo
{
	
	TestSrvc(String name, int noLink)
	{
		super(name, noLink);
	}
	public void infoHandler()
	{
		System.out.print("Received Array, size: ");
		int myArr[] = getIntArray();
        System.out.println(myArr.length);
	}
}

class TestSrvcStr extends DimInfo
{
	
	TestSrvcStr(String name, String noLink)
	{
		super(name, noLink);
	}
	public void infoHandler()
	{
		System.out.print("Received String: ");
        System.out.println(getString());
	}
}

class TestSrvcMix extends DimInfo
{
	
	TestSrvcMix(String name, int noLink)
	{
		super(name, noLink);
	}
	public void infoHandler()
	{
		System.out.println("Received Mixed Service :");

//		int myArr[] = getIntArray(3);
//		double dd = getDouble();
//		short ss = getShort();
//		char cc = getChar();
//		short ss1 = getShort();
//		float ff = getFloat();
//		String str = getString(); 
//      System.out.println(myArr[0]+" "+myArr[1]+" "+
//			myArr[2]+" "+dd+" "+ss+" "+(char)cc+" "+ss1+" "+
//						   ff+" "+str);

		int ii = getInt();
		if(ii != -1)
		{
			int i;
			String str = getString();
			double dd = getDouble();
			int[] arr=getIntArray();
			long ll = getLong();
			System.out.println(ii+" "+str+" "+dd+" "+ll);
			System.out.println(arr[0]+" "+arr.length);
//			String str1 = getString();
//			String str2 = getString();
//			System.out.println(str1+" "+str2);
			String[] strs = getStringArray();
			for(i = 0; i < strs.length; i++)
			{
				System.out.println("strs"+i+" "+strs[i]);				
			}

			int ii1 = getInt();
			String str1 = getString();
			double dd1 = getDouble();
			int[] arr1=getIntArray();
			long ll1 = getLong();
			System.out.println("***** "+ii1+" "+str1+" "+dd1+" "+ll1);
			System.out.println(arr1[0]+" "+arr1.length);


		}
		else
        	System.out.println(ii);
	}
}
*/
/*
public class TestClient
{
  public static void main(String[] args)
  {
	int timeout = 5;
//	int sleepTime = 5000;
	  
	int totalNumberOfLoops = 30; 
	int loopNumber = 0;
	
	int numberOfSuccesses = 0;
	int numberOfTimeouts = 0;
	int numberOfZeroes = 0;
	int numberOfOthers = 0;
	int numberOfAttempts = 0;
	
	int retValue = 0;
	
	TestRPC testRpc = new TestRPC("TestTobiasRPC", -1);
	//TestRPC testRpc = new TestRPC("DimServerRPC", -1);
	
	for(int a = 1; a <= totalNumberOfLoops; a++)
	{
//		try {
//			Thread.sleep(sleepTime);
//		}
//		catch (InterruptedException e) {
//			System.out.println("Interupted while waiting 1 second for join()!");
//		}
		DimTimer.sleep(1);	
		loopNumber++;
		
		
//		do {
			
			//Sending rpc parameters:
			//The "TEST" command will advice the PVSS system on the other side to return it's 
			//current system time once the trigger is set.
			if(DimClient.sendCommand("TestTobiasRPC/Cmd", "TEST") != 1) {
				System.out.println("ERROR: Couldn't connect to ExpertCommand.parameters. Command was " + "TEST");
				System.exit(0);
			}
			
			retValue = testRpc.setData(1, timeout);
						
			
			switch(retValue) {
				case -1:
					numberOfTimeouts++;
					System.out.println("Timeout.");
					break;
				case 0:
					numberOfZeroes++;
					System.out.println("Zero.");
					break;
				case 1:
					numberOfSuccesses++;
					break;
				default:
					numberOfOthers++;
					System.out.println("Other.");
			}
			numberOfAttempts++;
			
			
//		} while (retValue < 1);
		
		retValue = 0;
		
		 //As the answer has been received now (otherwise we'd still be in "while()"),
		//we can safely retrieve the String value associated with the answer.
		//If no value can be got, "DIM-UNKNOWN" will be used as a default:
		String rpcStringAnswer = (new DimCurrentInfo("TestTobiasRPC/data", "DIM-UNKNOWN")).getString();
		
		System.out.println("(Loop " + loopNumber + ") DDC-Server test successful. Connection to DIM established: " + rpcStringAnswer);
	}//end for(a)
	
	
	System.out.println("-----------------STATISTICS-----------------");
	System.out.println("Number of loops: " + totalNumberOfLoops);
	System.out.println("Number of successes: " + numberOfSuccesses);
	System.out.println("Number of timeouts: " + numberOfTimeouts);
	System.out.println("Number of zeroes: " + numberOfZeroes);
	System.out.println("Number of others: " + numberOfOthers);
	System.out.println("Number of attempts: " + numberOfAttempts);
	
	System.exit(0);
  }
  
}



class TestRPC extends DimInfo
{
	String rpcName;
	static DimLock rpcLock;
	int itsWaiting;
	
	TestRPC(String name, int noLink)
	{
		super(name+"/RpcOut", noLink);
		
		rpcName = name;	
		rpcLock = null;
		itsWaiting = 0;
	}
	public int setData(int data, int tout)
	{
		int ret;
		if(rpcLock == null)
			rpcLock = new DimLock();
		rpcLock.reset();		
		itsWaiting = 1;
		DimClient.sendCommandNB(rpcName+"/RpcIn",data);
		System.out.println("Sending RPC : "+data);
		ret = rpcLock.dimWait(tout);
		itsWaiting = 0;
		System.out.println("Done "+ret);
		return ret;
	}
	public void infoHandler()
	{
		if(itsWaiting == 0)
			return;
		rpcLock.dimWakeUp();
		System.out.println("Received RPC : " + getInt());
	}
};


class DimLock extends DimTimer
{
	int n = 0;
	public DimLock()
	{
		n = 0;
	}
	public synchronized void reset()
	{
		n = 0;
	}
	public synchronized int dimWait(int secs)
	{
		if(n == 0)
		{
			if(secs > 0)
				start(secs);
			try{
				wait();
			}catch (InterruptedException e){}
			if(n > 0)
				stop();
			return n;
		}
		return 0;	
	}
	public synchronized void dimWakeUp()
	{
		System.out.println("woke up");
		n++;
		notify();
	}
	public synchronized void timerHandler()
	{
		System.out.println("time out");
		n = -1;
		notify();
	}
}
*/