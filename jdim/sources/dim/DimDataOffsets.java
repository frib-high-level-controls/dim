package dim;
import java.util.Vector;

public class DimDataOffsets
{
	Vector offsetList;
	int nOffsets;
	
	public DimDataOffsets()
	{
		offsetList = new Vector();
		nOffsets = 0;
	}
	
	public int findOffset(int offset)
	{
		int i;
		DataOffset item;
		
		for(i = 0; i < nOffsets; i++)
		{
			item = (DataOffset)offsetList.elementAt(i);
			if(item.getOffset() == offset)
				return i;
		}
		return -1;
	}

	public void addOffset(int offset, char type, int size)
	{
		DataOffset item = new DataOffset(offset, type, size);		
		offsetList.add(item);
		nOffsets++;
	}
	
	public int getSize(int index)
	{
		DataOffset item;
		
		item = (DataOffset)offsetList.elementAt(index);
		return item.getSize();	
	}
	public char getType(int index)
	{
		DataOffset item;
		
		item = (DataOffset)offsetList.elementAt(index);
		return item.getType();	
	}
}	

class DataOffset
{
	char itsType;
	int itsOffset;
	int itsSize;
	
	public DataOffset(int offset, char type, int size)
	{
		itsOffset = offset;
		itsSize = size;
		itsType = type;
	}
	public char getType()
	{
		return itsType;
	}
	public int getOffset()
	{
		return itsOffset;
	}
	public int getSize()
	{
		return itsSize;
	}
}
