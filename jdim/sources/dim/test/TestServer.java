package dim.test;

import dim.*;
import java.util.Date;

/**
 * This class serves both as a complete test of the Dim package, as well as a show case for the capabilities of DIM.
 * This class is still under construction.
 * @author M.Jonker Cern
 * @version v1.2
 */
public class TestServer
{

  public static void main(String[] args)
  {
//	DimServer.setDnsNode("lxplus053.cern.ch");
//	System.out.println(DimServer.getDnsNode()+" "+DimServer.getDnsPort());

	DimErrorHandler eid = new DimErrorHandler()
	{
		public void errorHandler(int severity, int code, String msg)
		{
			if(code == DIMSVCDUPLC)
				System.out.println("Service already declared");
			System.out.println("Error: "+msg+" sev: "+severity);
		}
	};
	DimServer.addErrorHandler(eid);

	DimExitHandler exid = new DimExitHandler()
	{
		public void exitHandler(int code)
		{
			System.out.println("Exit: "+code);
		}
	};
	DimServer.addExitHandler(exid);

	DimCommand cid = new DimCommand("TestCommand","I")
	{
		public void commandHandler()
		{
			int i;
			System.out.println("Received "+getInt());
			String[] list = DimServer.getClientServices();
			System.out.println("Services: ");
			for(i = 0; i < list.length; i++)
				System.out.println(list[i]);
		}
	};
				
	int i = 0;
	DimService sid1 = new DimService("TEST_IT_INT",i);
//	DimService sid2 = new DimService("TEST_IT_INT",i);
	DimService cid1 = new DimService("TEST_IT_STR","hello hello");
	
	int myArr[] = {0,1,2,3,4,5};
	DimService aa = new DimService("TEST_IT_ARRAY",myArr);
/*	
	DimService testMix = new DimService("TEST_MIX");
	testMix.setInt(123);
	testMix.setString("hello",20);
	testMix.setDouble(3.5);
	testMix.setIntArray(new int[] {2,3,4});
	testMix.updateService();
*/
	DimService testMix = new DimService();
//	testMix.setInt("a", 123);
//	testMix.setString("b", "hello",20);
//	testMix.setDouble("c", 3.5);
//	testMix.setIntArray("d", new int[] {2,3,4});
	testMix.setInt(123);
	testMix.setString(20, "hello");
	testMix.setDouble(3.5);
	testMix.setIntArray(new int[] {2,3,4});
	testMix.setLong((long)123456);
//	testMix.setString("first string");
//	testMix.setString("this is the next one");
	testMix.setStringArray(new String[] {"aaa","bbb","ccc"});
	testMix.setName("TEST_MIX");
	testMix.updateService();
	DimServer.start("TestJDim");
	while(true)
	{
		DimTimer.sleep(10);
		sid1.setQuality(i);
		sid1.setTimestamp(new Date());
		sid1.updateService(++i);
		int myArr1[] = {0,1,2,3,4,5,6,7,8};
		aa.updateService(myArr1);
//		testMix.setInt("a",456);
//		testMix.setString("b","hello world");
//		testMix.setDouble("c", 6.5);
//		testMix.setIntArray("d",new int[] {6,7,8,9,10});
		testMix.setInt(456);
		testMix.setString(20, "hello world");
		testMix.setDouble(6.5);
		testMix.setIntArray(new int[] {6,7,8});
		testMix.setLong((long)12345678);
//		testMix.setString("second string");
//		testMix.setString("following string is a very long string");
		testMix.setStringArray(new String[] {"aaa1","bbb1","ccc1"});
		testMix.updateService();
	}
  }
}
