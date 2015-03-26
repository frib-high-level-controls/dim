package dim;
import java.util.Date;

public class DimService extends MutableMemory implements DataEncoder, DimServiceUpdateHandler
{
    int service_id;
	String service_name;
	int published, curr_size;
	String format;
	Format itsFormat;
	DimDataOffsets items;
	
	public DimService(String theServiceName)
	{
		published = 0;
		service_name = theServiceName;
		curr_size = 0;
		format = "";
		items = new DimDataOffsets();
	}
	public DimService()
	{
		published = 0;
		service_name = "";
		curr_size = 0;
		format = "";
		items = new DimDataOffsets();
	}
	
	public String getFormatStr()
	{
		return format;
	}
	public void setName(String theServiceName)
	{
		service_name = theServiceName;
	}

	public DimService(String theServiceName, boolean theData)
	{
		service_name = theServiceName;
		setSize(1);
		this.copyBoolean(theData);
		service_id = Server.addService(theServiceName, "C", this);
		published = 1;
		itsFormat = null;
//		theDataStore = null;
	}
	public DimService(String theServiceName, byte theData)
	{
		service_name = theServiceName;
		setSize(1);
		this.copyByte(theData);
		service_id = Server.addService(theServiceName, "C", this);
		published = 1;
		itsFormat = null;
//		theDataStore = null;
	}
	public DimService(String theServiceName, short theData)
	{
		service_name = theServiceName;
		setSize(2);
		this.copyShort(theData);
		service_id = Server.addService(theServiceName, "S", this);
		published = 1;
		itsFormat = null;
//		theDataStore = null;
	}
	public DimService(String theServiceName, int theData)
	{
		service_name = theServiceName;
		setSize(4);
		this.copyInt(theData);
        service_id = Server.addService(theServiceName, "I", this);
		published = 1;
		itsFormat = null;
//		theDataStore = null;
	}
	public DimService(String theServiceName, float theData)
	{
		service_name = theServiceName;
		setSize(4);
		this.copyFloat(theData);
        service_id = Server.addService(theServiceName, "F", this);
		published = 1;
		itsFormat = null;
//		theDataStore = null;
	}
	public DimService(String theServiceName, double theData)
	{
		service_name = theServiceName;
		setSize(8);
		this.copyDouble(theData);
        service_id = Server.addService(theServiceName, "D", this);
		published = 1;
		itsFormat = null;
//		theDataStore = null;
	}
	public DimService(String theServiceName, long theData)
	{
		service_name = theServiceName;
		setSize(8);
		this.copyLong(theData);
		service_id = Server.addService(theServiceName, "X", this);
		published = 1;
		itsFormat = null;
//		theDataStore = null;
	}
    public DimService(String theServiceName, String theData)
    {
 		service_name = theServiceName;
		setSize(theData.length()+1);
		this.copyString(theData);
        service_id = Server.addService(theServiceName, "C", this);
		published = 1;
		itsFormat = null;
//		theDataStore = null;
   }
	public DimService(String theServiceName, boolean[] theData)
	{
		service_name = theServiceName;
		int size = Sizeof.sizeof(theData);
		setSize(size);
		this.copyFromBooleanArray(theData, 0, size);
		service_id = Server.addService(theServiceName, "C", this);
		published = 1;
		itsFormat = null;
//		theDataStore = null;
	}
	public DimService(String theServiceName, byte[] theData)
	{
		service_name = theServiceName;
		int size = Sizeof.sizeof(theData);
		setSize(size);
		this.copyFromByteArray(theData, 0, size);
		service_id = Server.addService(theServiceName, "C", this);
		published = 1;
		itsFormat = null;
//		theDataStore = null;
	}
	public DimService(String theServiceName, short[] theData)
	{
		service_name = theServiceName;
		int size = Sizeof.sizeof(theData);
		setSize(size);
		this.copyFromShortArray(theData, 0, size/2);
		service_id = Server.addService(theServiceName, "S", this);
		published = 1;
		itsFormat = null;
//		theDataStore = null;
	}
	public DimService(String theServiceName, int[] theData)
	{
		service_name = theServiceName;
		int size = Sizeof.sizeof(theData);
		setSize(size);
		this.copyFromIntArray(theData, 0, size/4);
        service_id = Server.addService(theServiceName, "I", this);
		published = 1;
		itsFormat = null;
//		theDataStore = null;
	}
	public DimService(String theServiceName, float[] theData)
	{
		service_name = theServiceName;
		int size = Sizeof.sizeof(theData);
		setSize(size);
		this.copyFromFloatArray(theData, 0, size/4);
        service_id = Server.addService(theServiceName, "F", this);
		published = 1;
		itsFormat = null;
//		theDataStore = null;
	}
	public DimService(String theServiceName, double[] theData)
	{
		service_name = theServiceName;
		int size = Sizeof.sizeof(theData);
		setSize(size);
		this.copyFromDoubleArray(theData, 0, size/8);
        service_id = Server.addService(theServiceName, "D", this);
		published = 1;
		itsFormat = null;
//		theDataStore = null;
	}
	public DimService(String theServiceName, long[] theData)
	{
		service_name = theServiceName;
		int size = Sizeof.sizeof(theData);
		setSize(size);
		this.copyFromLongArray(theData, 0, size/8);
		service_id = Server.addService(theServiceName, "X", this);
		published = 1;
		itsFormat = null;
//		theDataStore = null;
	}
	public DimService(String theServiceName, DimService src)
	{
		service_name = theServiceName;
		int size = src.getDataSize();
		setSize(size);
		this.copyFromMemory(src);
		service_id = Server.addService(theServiceName, src.getFormatStr(), this);
		published = 1;
		itsFormat = null;
//		theDataStore = null;
	}
     public void finalize()
    {
        	removeService();
    }
	
    public void removeService()
    {
		if(service_id != 0)
        	Server.removeService(service_id);
    }
    public Memory encodeData()
    {
		serviceUpdateHandler();
        return this;
    }
    public void updateService(String theData)
    {
		this.setSize(theData.length()+1);
        this.copyString(theData);
        Server.updateService(service_id);
    }
	public void updateService(boolean theData)
	{
		this.setSize(1);
		this.copyBoolean(theData);
		Server.updateService(service_id);
	}
	public void updateService(byte theData)
	{
		this.setSize(1);
		this.copyByte(theData);
		Server.updateService(service_id);
	}
	public void updateService(short theData)
	{
		this.setSize(2);
		this.copyShort(theData);
		Server.updateService(service_id);
	}
    public void updateService(int theData)
    {
		this.setSize(4);
        this.copyInt(theData);
        Server.updateService(service_id);
    }
	public void updateService(float theData)
    {
		this.setSize(4);
        this.copyFloat(theData);
        Server.updateService(service_id);
    }
    public void updateService(double theData)
    {
		this.setSize(8);
        this.copyDouble(theData);
        Server.updateService(service_id);
    }
	public void updateService(long theData)
	{
		this.setSize(8);
		this.copyLong(theData);
		Server.updateService(service_id);
	}
	public void updateService(boolean[] theData)
	{
		int size = Sizeof.sizeof(theData);
		setSize(size);
		this.copyFromBooleanArray(theData, 0, size);
		Server.updateService(service_id);
	}
	public void updateService(byte[] theData)
	{
		int size = Sizeof.sizeof(theData);
		setSize(size);
		this.copyFromByteArray(theData, 0, size);
		Server.updateService(service_id);
	}
	public void updateService(short[] theData)
	{
		int size = Sizeof.sizeof(theData);
		setSize(size);
		this.copyFromShortArray(theData, 0, size/2);
		Server.updateService(service_id);
	}
    public void updateService(int[] theData)
    {
		int size = Sizeof.sizeof(theData);
		setSize(size);
		this.copyFromIntArray(theData, 0, size/4);
        Server.updateService(service_id);
    }
    public void updateService(float[] theData)
    {
		int size = Sizeof.sizeof(theData);
		setSize(size);
		this.copyFromFloatArray(theData, 0, size/4);
        Server.updateService(service_id);
    }
    public void updateService(double[] theData)
    {
		int size = Sizeof.sizeof(theData);
		setSize(size);
		this.copyFromDoubleArray(theData, 0, size/8);
        Server.updateService(service_id);
    }
	public void updateService(long[] theData)
	{
		int size = Sizeof.sizeof(theData);
		setSize(size);
		this.copyFromLongArray(theData, 0, size/8);
		Server.updateService(service_id);
	}
	public void updateService(DimService src)
	{
		int size = src.getDataSize();
		setSize(size);
		this.copyFromMemory(src);
		Server.updateService(service_id);
	}
	
	int do_setup_format(int offset, char type, int num)
	{
		char last;
		if(published == 0)
		{
			if(items.findOffset(offset) != -1)
				return 1;
			items.addOffset(offset, type, num);
			if(format != "")
			{
				if(format.lastIndexOf(':') < format.lastIndexOf(';'))
				{
					last = format.charAt(format.length()-1);
					if(last != type)
					{
						System.out.println(
							"JDIM: Dynamic Item must be at the end");
						return 0;
					}
					else
						return 1;
				}
				format += ";";
			}
			format += type;
			if(num != 0)
				format += ":"+num;
			return 1;
		}
		else
		{
			int index;
			if((index = items.findOffset(offset)) == -1)
			{
				if((type == 'C') && (num == 0))
					return 1;
				else
				{
					System.out.println("JDIM: Offset "+offset+" not found ");
					return 0;
				}
			}
//			char ntype = itsFormat.getType();
//			int nnum = itsFormat.getNum();
			char ntype = items.getType(index);
			int nnum = items.getSize(index);
			if(ntype != type)
			{
				System.out.println("JDIM: Expected "+ntype+" found "+type);
				return 0;
			}
			if((nnum != num) && (nnum != 0) && (num != 0))
			{
//				if(nnum)
					System.out.println("JDIM: Expected "+nnum+" items, found "+num);
//				else
//					System.out.println("JDIM: Expected "+nnum+" items, found "+num);
//				return 0;
			}
			if(nnum == 0)
				return 1;
			return nnum;
		}
	}
/*		
	public int do_set_data_size(char type, int len, int size)
	{
		int index, offset, old_size;
		
		if(do_setup_format(type,len) == 0)
			return -1;
		offset = curr_size;
		curr_size += size;
		setSize(curr_size);
		setDataStoreOffset(offset);
		return offset;
	}
*/
	
	public int do_setup_data(char type, int len, int size, int offset)
	{
		int index, new_offset, old_size;

		new_offset = offset;
		if(offset == -1)
		{
			new_offset = curr_size;
			curr_size += size;
			setSize(curr_size);
		}
		else if((type == 'C') && (len == 0))
		{
			curr_size = getAllocatedSize();
			if(offset + size > curr_size)
				setSize(offset+size);
		}
		if(do_setup_format(new_offset, type,len) == 0)
			return -1;
		setDataStoreOffset(new_offset);
		return new_offset;
	}

	public int setBoolean(boolean theData)
	{
		int offset;		
		offset = do_setup_data('C', 1, 1, -1);
		if(offset != -1)
			copyBoolean(theData);
		return offset;
	}

	public int setBoolean(boolean theData, int theOffset)
	{
		int offset;		
		offset = do_setup_data('C', 1, 1, theOffset);
		if(offset != -1)
			copyBoolean(theData);
		return offset;
	}

	public int setByte(byte theData)
	{
		int offset;		
		offset = do_setup_data('C', 1, 1, -1);
		if(offset != -1)
			copyByte(theData);
		return offset;
	}

	public int setByte(byte theData, int theOffset)
	{
		int offset;		
		offset = do_setup_data('C', 1, 1, theOffset);
		if(offset != -1)
			copyByte(theData);
		return offset;
	}

	public int setShort(short theData)
	{
		int offset;		
		offset = do_setup_data('S', 1, 2, -1);
		if(offset != -1)
			copyShort(theData);
		return offset;
	}

	public int setShort(short theData, int theOffset)
	{
		int offset;		
		offset = do_setup_data('S', 1, 2, theOffset);
		if(offset != -1)
			copyShort(theData);
		return offset;
	}

	public int setInt(int theData)
	{
		int offset;		
		offset = do_setup_data('I', 1, 4, -1);
		if(offset != -1)
			copyInt(theData);
		return offset;
	}

	public int setInt(int theData, int theOffset)
	{
		int offset;		
		offset = do_setup_data('I', 1, 4, theOffset);
		if(offset != -1)
			copyInt(theData);
		return offset;
	}

	public int setFloat(float theData)
	{
		int offset;		
		offset = do_setup_data('F', 1, 4, -1);
		if(offset != -1)
			copyFloat(theData);
		return offset;
	}

	public int setFloat(float theData, int theOffset)
	{
		int offset;		
		offset = do_setup_data('F', 1, 4, theOffset);
		if(offset != -1)
			copyFloat(theData);
		return offset;
	}

	public int setDouble(double theData)
	{
		int offset;		
		offset = do_setup_data('D', 1, 8, -1);
		if(offset != -1)
			copyDouble(theData);
		return offset;
	}

	public int setDouble(double theData, int theOffset)
	{
		int offset;		
		offset = do_setup_data('D', 1, 8, theOffset);
		if(offset != -1)
			copyDouble(theData);
		return offset;
	}

	public int setLong(long theData)
	{
		int offset;		
		offset = do_setup_data('X', 1, 8, -1);
		if(offset != -1)
			copyLong(theData);
		return offset;
	}

	public int setLong(long theData, int theOffset)
	{
		int offset;		
		offset = do_setup_data('X', 1, 8, theOffset);
		if(offset != -1)
			copyLong(theData);
		return offset;
	}

	public int setString(int max_size, String theData)
	{		
		int offset;		
		offset = do_setup_data('C', max_size, max_size, -1);
		if(offset != -1)
			copyString(theData);
		return offset;
	}

	public int setString(String theData)
	{		
		int offset;		
		offset = do_setup_data('C', 0, theData.length()+1, -1);
		if(offset != -1)
			copyString(theData);
		return offset;
	}

	public int setString(String theData, int theOffset)
	{		
		int offset;		
		offset = do_setup_data('C', 0, theData.length()+1, theOffset);
		if(offset != -1)
			copyString(theData);
		return offset;
	}

/*
	public int setString(String theData)
	{				
//		return theDataStore.setString(theData);

		int max_size;
		if(published == 0)
			max_size = do_setup_format('C',theData.length());
		else
			max_size = do_setup_format('C',0);
		if(max_size == 0)
			return 0;
		itsLast = 1;


		int offset = curr_size;
		curr_size += max_size;
		setSize(curr_size);
		setDataStoreOffset(offset);

//		if(do_setup_data('I', 1, 4, "") == 0)
//			return 0;
		this.copyString(theData);
		return 1;

	}
*/	
	public int setBooleanArray(boolean theData[])
	{		
		int size = Sizeof.sizeof(theData);
		int len = size;
		int offset;		
		offset = do_setup_data('C', len, size, -1);
		if(offset != -1)
			copyFromBooleanArray(theData);
		return offset;
	}

	public int setBooleanArray(boolean theData[], int theOffset)
	{		
		int size = Sizeof.sizeof(theData);
		int len = size;

		int offset;		
		offset = do_setup_data('C', len, size, theOffset);
		if(offset != -1)
			copyFromBooleanArray(theData);
		return offset;
	}

	public int setByteArray(byte theData[])
	{		
		int size = Sizeof.sizeof(theData);
		int len = size;
		int offset;		
		offset = do_setup_data('C', len, size, -1);
		if(offset != -1)
			copyFromByteArray(theData);
		return offset;
	}

	public int setByteArray(byte theData[], int theOffset)
	{		
		int size = Sizeof.sizeof(theData);
		int len = size;

		int offset;		
		offset = do_setup_data('C', len, size, theOffset);
		if(offset != -1)
			copyFromByteArray(theData);
		return offset;
	}

	public int setShortArray(short theData[])
	{		
		int size = Sizeof.sizeof(theData);
		int len = size/2;
		int offset;		
		offset = do_setup_data('S', len, size, -1);
		if(offset != -1)
			copyFromShortArray(theData);
		return offset;
	}

	public int setShortArray(short theData[], int theOffset)
	{		
		int size = Sizeof.sizeof(theData);
		int len = size/2;

		int offset;		
		offset = do_setup_data('S', len, size, theOffset);
		if(offset != -1)
			copyFromShortArray(theData);
		return offset;
	}

	public int setIntArray(int theData[])
	{		
		int size = Sizeof.sizeof(theData);
		int len = size/4;
		int offset;		
		offset = do_setup_data('I', len, size, -1);
		if(offset != -1)
			copyFromIntArray(theData);
		return offset;
	}

	public int setIntArray(int theData[], int theOffset)
	{		
		int size = Sizeof.sizeof(theData);
		int len = size/4;

		int offset;		
		offset = do_setup_data('I', len, size, theOffset);
		if(offset != -1)
			copyFromIntArray(theData);
		return offset;
	}

	public int setFloatArray(float theData[])
	{		
		int size = Sizeof.sizeof(theData);
		int len = size/4;
		int offset;		
		offset = do_setup_data('F', len, size, -1);
		if(offset != -1)
			copyFromFloatArray(theData);
		return offset;
	}

	public int setFloatArray(float theData[], int theOffset)
	{		
		int size = Sizeof.sizeof(theData);
		int len = size/4;

		int offset;		
		offset = do_setup_data('F', len, size, theOffset);
		if(offset != -1)
			copyFromFloatArray(theData);
		return offset;
	}

	public int setDoubleArray(double theData[])
	{		
		int size = Sizeof.sizeof(theData);
		int len = size/8;

		int offset;		
		offset = do_setup_data('D', len, size, -1);
		if(offset != -1)
			copyFromDoubleArray(theData);
		return offset;
	}

	public int setDoubleArray(double theData[], int theOffset)
	{		
		int size = Sizeof.sizeof(theData);
		int len = size/8;

		int offset;		
		offset = do_setup_data('D', len, size, theOffset);
		if(offset != -1)
			copyFromDoubleArray(theData);
		return offset;
	}

	public int setLongArray(long theData[])
	{		
		int size = Sizeof.sizeof(theData);
		int len = size/8;
		int offset;		
		offset = do_setup_data('X', len, size, -1);
		if(offset != -1)
			copyFromLongArray(theData);
		return offset;
	}

	public int setLongArray(long theData[], int theOffset)
	{		
		int size = Sizeof.sizeof(theData);
		int len = size/8;

		int offset;		
		offset = do_setup_data('X', len, size, theOffset);
		if(offset != -1)
			copyFromLongArray(theData);
		return offset;
	}

	public int setStringArray(String theData[])
	{		
		int size = 0;
		int len = theData.length;
		int offset, i;

		for(i = 0; i < len; i++)
		{
			size += theData[i].length()+1;
		}	
		offset = do_setup_data('C', 0, size, -1);
		if(offset != -1)
		{
			for(i = 0; i < len; i++)
				copyString(theData[i]);
		}
		return offset;
	}

	public int setStringArray(String theData[], int theOffset)
	{		
		int size = 0;
		int len = theData.length;
		int offset, i;

		for(i = 0; i < len; i++)
		{
			size += theData[i].length()+1;
		}	
		offset = do_setup_data('C', 0, size, theOffset);
		if(offset != -1)
		{
			for(i = 0; i < len; i++)
				copyString(theData[i]);
		}
		return offset;
	}

	public void updateService()
	{
		if(published == 0)
		{
			itsFormat = new Format(format, 1);
			service_id = Server.addService(service_name, itsFormat.getFormat(), this);
			published = 1;
		}
		else
		{
			if(itsFormat != null)
			{
				itsFormat.reset();
			}
			Server.updateService(service_id);
		}
		curr_size = 0;
	}
	
	public void selectiveUpdateService()
	{
		int client_id = DimServer.getClientId();
		Server.selectiveUpdateService(service_id, client_id);
	}
	
	public void selectiveUpdateService(int[] client_ids)
	{
		Server.selectiveUpdateService(service_id, client_ids);
	}

	public void selectiveUpdateService(int client_id)
	{
		Server.selectiveUpdateService(service_id, client_id);
	}

    public static native void setQuality(int serviceId, int quality);
    public static native void setTimestamp(int serviceId, int secs, int millisecs);
	public void setQuality(int quality)
	{
		setQuality(service_id, quality);
	}
	public void setTimestamp(Date tstamp)
	{
		int secs, millisecs;
		long total, aux;
		
		total = tstamp.getTime();
		aux = total % 1000;
		millisecs = (int)aux;
		aux = total / 1000;
		secs = (int)aux;
		setTimestamp(service_id, secs, millisecs);
	}
	public void serviceUpdateHandler() {}
}

interface DimServiceUpdateHandler
{
	void serviceUpdateHandler();
}
