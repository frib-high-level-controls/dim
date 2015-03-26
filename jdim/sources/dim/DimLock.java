/*
 * Created on Jan 26, 2009
 *
 */
package dim;

/**
 * @author clara
 *
 */
public class DimLock extends DimTimer{
	int n = 0;
	public DimLock()
	{
		n = 0;
	}
	public synchronized void reset()
	{
		n = 0;
	}
	public synchronized void dimWait()
	{
		if(n == 0)
		{
			try{
				wait();
			}catch (InterruptedException e){}
		}			
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
			if((secs > 0) && (n > 0))
				stop();
			return n;
		}
		return n;	
	}
	public synchronized void dimWakeUp()
	{
//		System.out.println("woke up");
		n++;
		notify();
	}
	public synchronized void timerHandler()
	{
//		System.out.println("time out");
		n = -1;
		notify();
	}
}
