package dim;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class DimTimer implements DimTimerHandler
{

	long id;
	public DimTimer(int secs)
	{
		start(secs);
	}
	public DimTimer()
	{
	}

	public static native long start(DimTimer theTimer, int secs);
	public static native int stop(long id);
	
	public void start(int secs)
	{
		id = start(this, secs);
	}
	
	public int stop()
	{
		return stop(id);
	}
	
	public static void sleep(int secs)
	{
  		try
        {
			Thread/*.currentThread()*/.sleep(secs*1000);
        }
        catch (Exception e) {}
	}
	public void timerHandler() {};
	
	static String now(String dateFormat) {
	  Calendar cal = Calendar.getInstance();
	  SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
	  return sdf.format(cal.getTime());
	}
	public static void printCurrentTime()
	{
	  System.out.println(now("yyyy.MM.dd hh:mm:ss"));
	}
}

interface DimTimerHandler
{
	void timerHandler();
}

