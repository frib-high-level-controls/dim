package dim;

public class Format
{
  String itsFormat;
  int itsIndex, itsLen;
  
  public Format(String format)
  {
	  int index;
	  
	  itsFormat = format.toUpperCase();
//	  if((index = itsFormat.lastIndexOf(':')) != -1)
//	  {
//		  itsFormat = itsFormat.substring(0,index);
//	  }
	  itsLen = itsFormat.length();
	  itsIndex = 0;
  }
  public Format(String format, int patch)
  {
	  int index, index1;;
	  
	  itsFormat = format.toUpperCase();
	  index = itsFormat.lastIndexOf(':');
	  index1 = itsFormat.lastIndexOf(';');
	  if(index > index1)
	  {
		  itsFormat = itsFormat.substring(0,index);
	  }
	  itsLen = itsFormat.length();
	  itsIndex = 0;
  }
  
  public String getFormat()
  {
	  return itsFormat;
  }
  
  public void reset()
  {
  	  itsIndex = 0;
  }
  public char getType()
  {
	  char type = '\0';
	  
	  if(itsIndex >= itsLen)
	  {
		  itsIndex = 0;
//		  return type;
	  }

	  if(itsFormat.length()>0)
	  	type = itsFormat.charAt(itsIndex);
	  return type;
  }
  
  public int getNum()
  {
	  int endIndex;
	  String numstr;
	  Integer inum;
	  
	  itsIndex++;
	  if((itsIndex) >= itsLen)
	  {
		  return 0;
	  }
	  if(itsFormat.charAt(itsIndex) == ':')
	  {
		  itsIndex++;
		  endIndex = itsFormat.indexOf(";",itsIndex);
		  if(endIndex >= 0)
		  {
			  numstr = itsFormat.substring(itsIndex, endIndex);
			  itsIndex = endIndex+1;
		  }
		  else
		  {
			  numstr = itsFormat.substring(itsIndex);
			  itsIndex = itsLen;
		  }
		  inum = new Integer(numstr);
		  return inum.intValue();
	  }
	  return 0;
  }
  /*
  public int getItem(Character type, Integer num)
  {
	  int endIndex;
	  String numstr;
	  
	  if(itsIndex >= itsLen)
	  {
		  itsIndex = 0;
		  return 0;
	  }
	  type = new Character(itsFormat.charAt(itsIndex));
	  itsIndex++;
	  if(itsIndex >= itsLen)
	  {
		  num = 0;
		  return 1;
	  }
	  if(itsFormat.charAt(itsIndex) == ':')
	  {
		  itsIndex++;
		  endIndex = itsFormat.indexOf(";",itsIndex);
		  if(endIndex >= 0)
		  {
			  numstr = itsFormat.substring(itsIndex, endIndex);
			  itsIndex = endIndex+1;
		  }
		  else
		  {
			  numstr = itsFormat.substring(itsIndex);
			  itsIndex = itsLen;
		  }
		  num = new Integer(numstr);
		  return 1;
	  }
	  return 1;
  }
  public int getItem(Character type, Integer num)
  {
	  int endIndex;
	  String numstr;
	  
	  if(itsIndex >= itsLen)
	  {
		  itsIndex = 0;
		  return 0;
	  }
	  type = new Character(itsFormat.charAt(itsIndex));
	  itsIndex++;
	  if(itsIndex >= itsLen)
	  {
		  num = 0;
		  return 1;
	  }
	  if(itsFormat.charAt(itsIndex) == ':')
	  {
		  itsIndex++;
		  endIndex = itsFormat.indexOf(";",itsIndex);
		  if(endIndex >= 0)
		  {
			  numstr = itsFormat.substring(itsIndex, endIndex);
			  itsIndex = endIndex+1;
		  }
		  else
		  {
			  numstr = itsFormat.substring(itsIndex);
			  itsIndex = itsLen;
		  }
		  num = new Integer(numstr);
		  return 1;
	  }
	  return 1;
  }
*/
}
