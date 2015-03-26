package dim;

public class DimData extends MutableMemory
{
	int published, curr_size;
	String format;
	Format itsFormat;
	int itsLast;
	
	public DimData()
	{
		curr_size = 0;
		format = "";
		itsLast = 0;
	}
	
	public void do_publish()
	{
		itsFormat = new Format(format, 1);
		published = 1;
	}
	
	int do_setup(char type, int num)
	{
		if(published == 0)
		{
			if(itsLast == 1)
			{
				System.out.println(
					"JDIM: Dynamic String Item must be at the end");
				return 0;
			}
			if(format != "")
				format += ";";
			format += type+":"+num;
			return 1;
		}
		else
		{
			char ntype = itsFormat.getType();
			int nnum = itsFormat.getNum();
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
		
	public int setInt(int theData)
	{
		if(do_setup('I',1) == 0)
			return 0;
		int offset = curr_size;
		curr_size += 4;
		setSize(curr_size);
		setDataStoreOffset(offset);
		copyInt(theData);
		return 1;
	}
	public int setFloat(float theData)
	{
		if(do_setup('F',1) == 0)
			return 0;
		int offset = curr_size;
		curr_size += 4;
		setSize(curr_size);
		setDataStoreOffset(offset);
		copyFloat(theData);
		return 1;
	}
	public int setDouble(double theData)
	{
		if(do_setup('D',1) == 0)
			return 0;
		int offset = curr_size;
		curr_size += 8;
		setSize(curr_size);
		setDataStoreOffset(offset);
		copyDouble(theData);
		return 1;
	}
	public int setString(String theData, int max_size)
	{		
		if(do_setup('C',max_size) == 0)
			return 0;
		int offset = curr_size;
		curr_size += max_size;
		setSize(curr_size);
		setDataStoreOffset(offset);
		this.copyString(theData);
		return 1;
	}
	public int setString(String theData)
	{				
		int max_size;
		if(published == 0)
			max_size = do_setup('C',theData.length());
		else
			max_size = do_setup('C',0);
		if(max_size == 0)
			return 0;
		itsLast = 1;
/*
		if(published == 0)
		{
			return 0;
		}
		else
		{
			char type = itsFormat.getType();
			int num = itsFormat.getNum();
			if(type != 'C')
				return 0;
			max_size = num;
		}
*/
		int offset = curr_size;
		curr_size += max_size;
		setSize(curr_size);
		setDataStoreOffset(offset);
		this.copyString(theData);
		return 1;
	}
	public int setIntArray(int theData[])
	{		
		int size = Sizeof.sizeof(theData);
		int len = size/4;
		if(do_setup('I',len) == 0)
			return 0;
		int offset = curr_size;
		curr_size += size;
		setSize(curr_size);
		setDataStoreOffset(offset);
		this.copyFromIntArray(theData, 0, len);
		return 1;
	}
	public int setFloatArray(float theData[])
	{		
		int size = Sizeof.sizeof(theData);
		int len = size/4;
		if(do_setup('F',len) == 0)
			return 0;
		int offset = curr_size;
		curr_size += size;
		setSize(curr_size);
		setDataStoreOffset(offset);
		this.copyFromFloatArray(theData, 0, len);
		return 1;
	}
	public int setDoubleArray(double theData[])
	{		
		int size = Sizeof.sizeof(theData);
		int len = size/8;
		if(do_setup('D',len) == 0)
			return 0;
		int offset = curr_size;
		curr_size += size;
		setSize(curr_size);
		setDataStoreOffset(offset);
		this.copyFromDoubleArray(theData, 0, len);
		return 1;
	}
}