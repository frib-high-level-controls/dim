package dim;
import java.util.Date;

public class DimInfo extends MutableMemory implements DataDecoder, DimInfoHandler
{
    int service_id;
	MutableMemory noLinkData;
	Format itsFormat;
	String service_name;

	public DimInfo(String theServiceName, String noLink)
	{
		noLinkData = new MutableMemory(noLink.length()+1);
		noLinkData.copyString(noLink);
		subscribe(theServiceName, 0);
	}
	public DimInfo(String theServiceName, boolean noLink)
	{
		noLinkData = new MutableMemory(1);
		noLinkData.copyBoolean(noLink);
		subscribe(theServiceName, 0);
	}
	public DimInfo(String theServiceName, byte noLink)
	{
		noLinkData = new MutableMemory(1);
		noLinkData.copyByte(noLink);
		subscribe(theServiceName, 0);
	}
	public DimInfo(String theServiceName, short noLink)
	{
		noLinkData = new MutableMemory(2);
		noLinkData.copyShort(noLink);
		subscribe(theServiceName, 0);
	}
	public DimInfo(String theServiceName, int noLink)
	{
		noLinkData = new MutableMemory(4);
		noLinkData.copyInt(noLink);
		subscribe(theServiceName, 0);
	}
	public DimInfo(String theServiceName, float noLink)
	{
		noLinkData = new MutableMemory(4);
		noLinkData.copyFloat(noLink);
		subscribe(theServiceName, 0);
	}
	public DimInfo(String theServiceName, double noLink)
	{
		noLinkData = new MutableMemory(8);
		noLinkData.copyDouble(noLink);
		subscribe(theServiceName, 0);
	}
	public DimInfo(String theServiceName, long noLink)
	{
		noLinkData = new MutableMemory(8);
		noLinkData.copyLong(noLink);
		subscribe(theServiceName, 0);
	}
	public DimInfo(String theServiceName, int time, String noLink)
	{
		noLinkData = new MutableMemory(noLink.length()+1);
		noLinkData.copyString(noLink);
		subscribe(theServiceName, time);
	}
	public DimInfo(String theServiceName, int time, boolean noLink)
	{
		noLinkData = new MutableMemory(1);
		noLinkData.copyBoolean(noLink);
		subscribe(theServiceName, time);
	}
	public DimInfo(String theServiceName, int time, byte noLink)
	{
		noLinkData = new MutableMemory(1);
		noLinkData.copyByte(noLink);
		subscribe(theServiceName, time);
	}
	public DimInfo(String theServiceName, int time, short noLink)
	{
		noLinkData = new MutableMemory(2);
		noLinkData.copyShort(noLink);
		subscribe(theServiceName, time);
	}
	public DimInfo(String theServiceName, int time, int noLink)
	{
		noLinkData = new MutableMemory(4);
		noLinkData.copyInt(noLink);
		subscribe(theServiceName, time);
	}
	public DimInfo(String theServiceName, int time, float noLink)
	{
		noLinkData = new MutableMemory(4);
		noLinkData.copyFloat(noLink);
		subscribe(theServiceName, time);
	}
	public DimInfo(String theServiceName, int time, double noLink)
	{
		noLinkData = new MutableMemory(8);
		noLinkData.copyDouble(noLink);
		subscribe(theServiceName, time);
	}
	public DimInfo(String theServiceName, int time, long noLink)
	{
		noLinkData = new MutableMemory(8);
		noLinkData.copyLong(noLink);
		subscribe(theServiceName, time);
	}
	
	protected void finalize()
	{
		releaseService();
	}
	
	public void releaseService()
	{
		Client.releaseService(service_id);
	}
	
    public static native int getQuality(int serviceId);
    public static native int getTimestamp(int serviceId);
    public static native int getTimestampMillisecs(int serviceId);
	public int getQuality()
	{
		return getQuality(service_id);
	}
	public Date getTimestamp()
	{
		int secs, millisecs;
		long total;
		secs =  getTimestamp(service_id);
		millisecs = getTimestampMillisecs(service_id);
		total = secs;
		total = total*1000+millisecs;
		Date dd = new Date(total);
		return dd;
	}
	public void decodeData(Memory theData)
    {
		int size = theData.getDataSize();
		if(size != 0)
		{
			setSize(size);
			copyFromMemory(theData);
			getFormat();
		}
		else
		{
			setSize(noLinkData.getDataSize());
			copyFromMemory(noLinkData);
		}	
		infoHandler();
    }
	public void infoHandler() {};
	void subscribe(String theServiceName, int time)
	{
		do_subscribe();
		service_name = theServiceName;
		service_id = Client.infoService(theServiceName, 
										this, 
										Client.MONITORED|Client.F_STAMPED,
										time);
	}
	void do_subscribe()
	{
		itsFormat = new Format("");
	}
	public String getName()
	{
		return service_name;
	}
	public String getFormat()
	{
		String format;
		
		format = Client.getFormat(service_id);
		itsFormat = new Format(format); 
		return format;
	}
	
	public boolean getBoolean()
	{
		char type = itsFormat.getType();
		int num = itsFormat.getNum();
		return super.getBoolean();
	}
	public byte getByte()
	{
		char type = itsFormat.getType();
		int num = itsFormat.getNum();
		return super.getByte();
	}
	public short getShort()
	{
		char type = itsFormat.getType();
		int num = itsFormat.getNum();
		return super.getShort();
	}
	public int getInt()
	{
		char type = itsFormat.getType();
		int num = itsFormat.getNum();
		return super.getInt();
	}
	public float getFloat()
	{
		char type = itsFormat.getType();
		int num = itsFormat.getNum();
		return super.getFloat();
	}
	public double getDouble()
	{
		char type = itsFormat.getType();
		int num = itsFormat.getNum();
		return super.getDouble();
	}
	public long getLong()
	{
		char type = itsFormat.getType();
		int num = itsFormat.getNum();
		return super.getLong();
	}
	public String getString()
	{
		char type = itsFormat.getType();
		int num = itsFormat.getNum();
		if(num != 0)
			return super.getString(num);
		return super.getString();
	}
	public boolean[] getBooleanArray()
	{
		char type = itsFormat.getType();
		int num = itsFormat.getNum();
		if(num != 0)
			return super.getBooleanArray(num);
		return super.getBooleanArray();
	}
	public byte[] getByteArray()
	{
		char type = itsFormat.getType();
		int num = itsFormat.getNum();
		if(num != 0)
			return super.getByteArray(num);
		return super.getByteArray();
	}
	public short[] getShortArray()
	{
		char type = itsFormat.getType();
		int num = itsFormat.getNum();
		if(num != 0)
			return super.getShortArray(num);
		return super.getShortArray();
	}
	public int[] getIntArray()
	{
		char type = itsFormat.getType();
		int num = itsFormat.getNum();
		if(num != 0)
			return super.getIntArray(num);
		return super.getIntArray();
	}
	public float[] getFloatArray()
	{
		char type = itsFormat.getType();
		int num = itsFormat.getNum();
		if(num != 0)
			return super.getFloatArray(num);
		return super.getFloatArray();
	}
	public double[] getDoubleArray()
	{
		char type = itsFormat.getType();
		int num = itsFormat.getNum();
		if(num != 0)
			return super.getDoubleArray(num);
		return super.getDoubleArray();
	}
	public long[] getLongArray()
	{
		char type = itsFormat.getType();
		int num = itsFormat.getNum();
		if(num != 0)
			return super.getLongArray(num);
		return super.getLongArray();
	}
	public String[] getStringArray()
	{
		char type = itsFormat.getType();
		int num = itsFormat.getNum();
		if(num != 0)
			return super.getStringArray(num);
		return super.getStringArray();
	}
}

interface DimInfoHandler
{
	void infoHandler();
}