package dim;
import java.util.Vector;
/*
public class DimTags
{
	String[] tags;
	int[] tagTypes;
	int[] tagOffsets;
	int nTags;
	
	public DimTags()
	{
		nTags = 0;
		tags = new String[100];
		tagOffsets = new int[100];
		tags[0] = "";
	}
	
	public int findTag(String name)
	{
		int i;
		
		for(i = 0; i < nTags; i++)
		{
			if(tags[i] == name)
				return i;
		}
		return -1;
	}

	public void addTag(String name, int offset)
	{
		int i;
		
		tags[nTags] = name;
		tagOffsets[nTags] = offset;
		nTags++;
	}
	
	public int getOffset(int index)
	{
		return tagOffsets[index];	
	}
}	
*/
public class DimTags
{
	Vector tagList;
	int nTags;
	
	public DimTags()
	{
		tagList = new Vector();
		nTags = 0;
	}
	
	public int findTag(String name)
	{
		int i;
		Tag tag;
		
		for(i = 0; i < nTags; i++)
		{
			tag = (Tag)tagList.elementAt(i);
			if(tag.getName() == name)
				return i;
		}
		return -1;
	}

	public void addTag(String name, int offset, int size)
	{
		Tag tag = new Tag(name, offset, size);		
		tagList.add(tag);
		nTags++;
	}
	
	public int getOffset(int index)
	{
		Tag tag;
		
		tag = (Tag)tagList.elementAt(index);
		return tag.getOffset();	
	}
	public int getSize(int index)
	{
		Tag tag;
		
		tag = (Tag)tagList.elementAt(index);
		return tag.getSize();	
	}
}	

class Tag
{
	String tagName;
	int tagType;
	int tagOffset;
	int tagSize;
	
	public Tag(String name, int offset, int size)
	{
		tagName = name;
		tagOffset = offset;
		tagSize = size;
	}
	public String getName()
	{
		return tagName;
	}
	public int getOffset()
	{
		return tagOffset;
	}
	public int getSize()
	{
		return tagSize;
	}
}
