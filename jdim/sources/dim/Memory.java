package dim;

/**
 * An object of this class holds a reference to a block of memory that is allocated and managed
 * by the native code. The user object has no means to alter the contents
 * of the memory blocks that are referenced by objects of this class.
 * Objects of this class are used by the native code to communicate incomming data to
 * a non native object (see the {@link DataDecoder#decodeData <code>decodeData()</code>}
 * method of the {@link DataDecoder} interface).
 * <br>
 * The information can be extracted from native memory using the <code>get<i>Primitive</i>()</code> and
 * <code>copyInto<i>Primitive</i>Array()</code> methods of this class.
 * <br>
 * To avoid <i>pirate references</i> to a Memory object that is passed by parameter to the decodeData()
 * method, the internal data address in Memory objects will be invalidated upon return from a
 * decodeData method. Hence, accessing a Memory object through a <i>pirate reference</i> outside the
 * calling scope of the decodeData method will lead to a run time null pointer exception.
 * We are forced to do this since we have no control over the memory that is used to store the incomming native data.
 * <p>
 * The {@link MutableMemory } class extends the <code>Memory</code> class and provides
 * a reference to a block of memory that is allocated and managed on behalve of a
 * user object. It also provides <code>set</code> methods to alter the block of memory .
 * @see MutableMemory
 * @author M.Jonker Cern; Adjustments for 64bit platforms Joern Adamczewski, gsi, 27-Oct-2007
 
 * @version v1.2
 */

public class Memory
{
    static // force loading the native library
    {
        Native.loadNativeLibrary();
    }

    /**
     * This class cannot be instantiated
     */
    Memory()
    {
    }

    /**
     * The address of the native memory.
     */
    long dataAddress;
    /**
     * The total amount of native memory allocated.
     */
    int allocatedSize;
    /**
     * Points to the end of the last data fetch operation and where the next data fetch operation will take place.
     * The current read offset will not exceed the current high water mark.
     */
    int dataFetchOffset;
    /**
     * Points to the end of the last store operation and to where the next store operation will take place.
     * The write offset can only be modified through methods in the {@link MutableMemory} subclass and it will not
     * exceed the allocated data size.
     */
    int dataStoreOffset;
    /**
     * The highest point to where memory has been writen in the past. If the dataStoreOffset is changed to a value
     * higher then the current value of the highWaterMark, all the memory from max(highWaterMark, writeOffset) till
     * newWriteOffset will be cleared. After this the highWaterMark value will be updated with the new value of
     * dataStoreOffset.
     * @since some version in the future (i.e. this feature is not currently implemented).
     */
    int highWaterMark;


    /**
     * This exception is thrown by the methods of Memory when a user attempts to read outside the boundaries
     * of the written memory area. This error refects a condition that is normally not expected to happen.
     * For this reason the Exception inherits from {@link IncorrectUsageException} (which inherits from
     * java.lang.RuntimeException RuntimeException} and hence does not have to be caught by the user.
     */
    static class ReadingOutOfBoundException extends IncorrectUsageException
    {
        public String toString()
        {
            return "Attempt to read outside the written area.";
        }
    }

    /**
     * This exception is thrown when a user attempts to set the read offset outside the boundaries
     * of the initialized memory area. This error refects a condition that is normally not expected to happen.
     * For this reason the Exception inherits from {@link IncorrectUsageException} (which inherits from
     * java.lang.RuntimeException RuntimeException} and hence does not have to be caught by the user.
     */
    class IllegalOffsetException extends IncorrectUsageException
    {
        public String toString()
        {
            return "The specified offset is outside the written area.";
        }
    }


    /**
     * Private method used by the native code to initialize the data pointers and call a DataDecoder.
     */
    private void decodeData(long newDataAddress, int newDataSize, DataDecoder theDecoder)
    {
        dataAddress     = newDataAddress;
        highWaterMark   = newDataSize;
        dataFetchOffset = 0;
        theDecoder.decodeData(this);
        /* clear the dataAddress, so no one will be tempted to abuse this information through pirate copies of the
           MemoryObject. We have to do this because the memory pointed to by data is not under our control */
        dataAddress     = 0;
        highWaterMark   = 0;
    }


    /**
     * Returns the address of the native memory.
     */
    public long getDataAddress()
    {
        return dataAddress;
    }

    /**
     * Returns the total amount of native memory allocated.
     */
    public int getAllocatedSize()
    {
        return allocatedSize;
    }

    /**
     * Returns the current data size. The current data size is defined by the maximum memory location in the native
     * data memory ever written to.
     */
    public int getDataSize()
    {
        return highWaterMark;
    }

    /**
     * Returns the current value of dataStoreOffset. The dataStoreOffset points to the end of the last store operation
     * and to where the next store operation will take place.
     */
    public int getDataStoreOffset()
    {
        return dataStoreOffset;
    }

    /**
     * Returns the current value of dataFetchOffset. The dataFetchOffset points to the end of the last fetch operation
     * and to where the next fetch operation will take place.
     */
    public int getDataFetchOffset()
    {
        return dataFetchOffset;
    }


    /**
     * Modifies the value of the dataFetchOffset. The dataFetchOffset points to the end of the last fetch operation
     * and to where the next fetch operation will take place.
     * @param newDataFetchOffset The new value for the dataFetchOffset.
     * @throws IllegalOffsetException This exception is thrown if an attempts made to set dataFetchOffset to an area
     * that has not been written to.
     */
    public void setDataFetchOffset(int newDataFetchOffset) throws IllegalOffsetException
    {
        if(newDataFetchOffset <0 || newDataFetchOffset> highWaterMark) throw new IllegalOffsetException();
        dataFetchOffset = newDataFetchOffset;
    }


    /**
     * Changes the dataFetchOffset with a relative amount.
     * @param delta The amount by which the offset has to be increased with (or decreased with
     * in case of a negative value).
     * @see #setDataFetchOffset
     */
    public void moveDataFetchOffset(int delta) throws IllegalOffsetException
    {
        setDataFetchOffset(dataFetchOffset+delta);
    }



    /**
     * Dumps internal data to the native output console.
     * <p>
     * <b>Note:</b> the debug messages are written to the native console and  not to the
     * System.out stream. On certain IDE's (e.g. JBuilder) these messages will not be
     * visible on the IDE console.
     * @param internalDataAddress The address of the internal data store to be dumped.
     * @param internalDataSize The size of the internal data store to be dumped.
     * @param dumpOptions Reserved for future use. Should be zero for future compatibility.
     */
    static native void dumpInternalData(long internalDataAddress, int internalDataSize, int dumpOptions);

    /**
     * Dumps internal data to the native output console.
     * <p>
     * <b>Note:</b> the debug messages are written to the native console and  not to the
     * System.out stream. On certain IDE's (e.g. JBuilder) these messages will not be
     * visible on the IDE console.
     * @param dumpOptions Reserved for future use. Should be zero for future compatibility.
     */
    public void dumpInternalData(int dumpOptions)
    {
        dumpInternalData(dataAddress, highWaterMark, dumpOptions);
    }

    // private native unpacking methods ================================================================================
    /**
     * Extracts a boolean data item from an internal data store.
     * @param nativeDataAddress The address of the internal data from where the data item should be extrated.
     * @return The extracted data.
     */
    private static native boolean getBoolean(long nativeDataAddress);

    /**
     * Extracts a char data item from an internal data store.
     * @see #getBoolean(int)
     */
    private static native    char getChar   (long nativeDataAddress);

    /**
     * Extracts a byte data item from an internal data store.
     * @see #getBoolean(int)
     */
    private static native    byte getByte   (long nativeDataAddress);

    /**
     * Extracts a short data item from an internal data store.
     * @see #getBoolean(int)
     */
    private static native   short getShort  (long nativeDataAddress);

    /**
     * Extracts a int data item from an internal data store.
     * @see #getBoolean(int)
     */
    private static native     int getInt    (long nativeDataAddress);

    /**
     * Extracts a long data item from an internal data store.
     * @see #getBoolean(int)
     */
    private static native    long getLong   (long nativeDataAddress);

    /**
     * Extracts a float data item from an internal data store.
     * @see #getBoolean(int)
     */
    private static native   float getFloat  (long nativeDataAddress);

    /**
     * Extracts a double data item from an internal data store.
     * @see #getBoolean(int)
     */
    private static native  double getDouble (long nativeDataAddress);

    /**
     * Extracts a String data item from an internal data store.
     * @see #getBoolean(int)
     */
    private static native  String getString (long nativeDataAddress, int maxSize);


    /**
     * Fetches data from an internal data store into an existing boolean array.
     * @param nativeDataAddress The address of the internal data from where the data should be extracted.
     * @param array Target array of the extracted data.
     * @param arrayOffset The offset into the target array.
     * @param length Number of array elements to be extracted.
     */
    private static native void copyIntoBooleanArray(long nativeDataAddress, boolean[] array, int arrayOffset, int length);

    /**
     * Extracts data from an internal data store into an existing char array.
     * @see #copyIntoBooleanArray(int, boolean[], int, int)
     */
    private static native void copyIntoCharArray   (long nativeDataAddress, char[] array, int arrayOffset, int length);

    /**
     * Extracts data from an internal data store into an existing byte array.
     * @see #copyIntoBooleanArray(int, boolean[], int, int)
     */
    private static native void copyIntoByteArray   (long nativeDataAddress, byte[] array, int arrayOffset, int length);

    /**
     * Extracts data from an internal data store into an existing short array.
     * @see #copyIntoBooleanArray(int, boolean[], int, int)
     */
    private static native void copyIntoShortArray  (long nativeDataAddress, short[] array, int arrayOffset, int length);

    /**
     * Extracts data from an internal data store into an existing int array.
     * @see #copyIntoBooleanArray(int, boolean[], int, int)
     */
    private static native void copyIntoIntArray    (long nativeDataAddress, int[] array, int arrayOffset, int length);

    /**
     * Extracts data from an internal data store into an existing long array.
     * @see #copyIntoBooleanArray(int, boolean[], int, int)
     */
    private static native void copyIntoLongArray   (long nativeDataAddress, long[] array, int arrayOffset, int length);

    /**
     * Extracts data from an internal data store into an existing float array.
     * @see #copyIntoBooleanArray(int, boolean[], int, int)
     */
    private static native void copyIntoFloatArray  (long nativeDataAddress, float[] array, int arrayOffset, int length);

    /**
     * Extracts data from an internal data store into an existing double array.
     * @see #copyIntoBooleanArray(int, boolean[], int, int)
     */
    private static native void copyIntoDoubleArray (long nativeDataAddress, double[] array, int arrayOffset, int length);


    // public unpacking methods ========================================================================================

    /**
     * Extracts a boolean data item.
     * The data is extracted starting at the current value of the dataFetchOffset.
     * The dataFetchOffset is updated after the operation.
     * @return The extracted data.
     * @throws ReadingOutOfBoundException This exception is thrown if an attempt is made to read an area that has
     * not been written to.
     * @see #setDataFetchOffset
     * @see #getDataFetchOffset
     */
    public boolean getBoolean() throws ReadingOutOfBoundException
    {
        if(dataFetchOffset+ 1 > highWaterMark)
			dataFetchOffset = 0;

        boolean theData = getBoolean(dataAddress+dataFetchOffset);
        dataFetchOffset += 1;
        return theData;
    }

    /**
     * Extracts a char data item.
     * @see #getBoolean()
     */
    public char getChar() throws ReadingOutOfBoundException
    {
        if(dataFetchOffset+ 1 > highWaterMark) 			
			dataFetchOffset = 0;

        char theData = getChar(dataAddress+dataFetchOffset);
        dataFetchOffset += 1;
        return theData;
    }

    /**
     * Extracts a byte data item.
     * @see #getBoolean()
     */
    public byte getByte() throws ReadingOutOfBoundException
    {
        if(dataFetchOffset+ 1 > highWaterMark) 
			dataFetchOffset = 0;

        byte theData = getByte(dataAddress+dataFetchOffset);
        dataFetchOffset += 1;
        return theData;
    }

    /**
     * Extracts a short data item.
     * @see #getBoolean()
     */
    public short getShort() throws ReadingOutOfBoundException
    {
        if(dataFetchOffset+ 2 > highWaterMark) dataFetchOffset = 0;

        short theData = getShort(dataAddress+dataFetchOffset);
        dataFetchOffset += 2;
        return theData;
    }

    /**
     * Extracts an int data item.
     * @see #getBoolean()
     */
    public int getInt() throws ReadingOutOfBoundException
    {
        if(dataFetchOffset+ 4 > highWaterMark) dataFetchOffset = 0;

        int theData = getInt(dataAddress+dataFetchOffset);
        dataFetchOffset += 4;
        return theData;
    }

    /**
     * Extracts a long data item.
     * @see #getBoolean()
     */
    public long getLong() throws ReadingOutOfBoundException
    {
        if(dataFetchOffset+ 8 > highWaterMark) dataFetchOffset = 0;

        long theData = getLong(dataAddress+dataFetchOffset);
        dataFetchOffset += 8;
        return theData;
    }

    /**
     * Extracts a float data item.
     * @see #getBoolean()
     */
    public float getFloat() throws ReadingOutOfBoundException
    {
        if(dataFetchOffset+ 4 > highWaterMark) dataFetchOffset = 0;

        float theData = getFloat(dataAddress+dataFetchOffset);
        dataFetchOffset += 4;
        return theData;
    }

    /**
     * Extracts a double data item.
     * @see #getBoolean()
     */
    public double getDouble() throws ReadingOutOfBoundException
    {
        if(dataFetchOffset+ 8 > highWaterMark) dataFetchOffset = 0;

        double theData = getDouble(dataAddress+dataFetchOffset);
        dataFetchOffset += 8;
        return theData;
    }

    /**
     * Extracts a String data item.
     * @see #getBoolean()
     * @todo, add a maximum size parameter to the native getString method.
     */
    public String getString() throws ReadingOutOfBoundException
    {
        if(dataFetchOffset >= highWaterMark)
			dataFetchOffset = 0;
        String theData = getString(dataAddress+dataFetchOffset, 0);
        dataFetchOffset += theData.length()+1;
        return theData;
    }

	public String getString(int max_size) throws ReadingOutOfBoundException
    {
        if(dataFetchOffset >= highWaterMark)
			dataFetchOffset = 0;
        String theData = getString(dataAddress+dataFetchOffset, 0);
        dataFetchOffset += max_size;
        return theData;
    }


    /**
     * Extracts a boolean array.
     * The data is extracted starting at the current value of the dataFetchOffset.
     * The dataFetchOffset is updated after the operation.
     * @param length Number of array elements to be extracted.
     * @return The extracted array.
     * @throws ReadingOutOfBoundException This exception is thrown if an attempts made to read an area that has
     * not been written to.
     * @see #setDataFetchOffset
     * @see #getDataFetchOffset
     */
    public boolean[] getBooleanArray(int length) throws ReadingOutOfBoundException
    {
        int size = length*1;
        if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();

        boolean[] array = new boolean[length];
        copyIntoBooleanArray(dataAddress+dataFetchOffset, array, 0, length);
        dataFetchOffset = dataFetchOffset + size;
        return array;
    }


	public boolean[] getBooleanArray() throws ReadingOutOfBoundException
	{
		int size = getDataSize();
		size -= dataFetchOffset;
		if(size<0) throw new ReadingOutOfBoundException();
		if(dataFetchOffset+size > highWaterMark) highWaterMark = 0;

		boolean[] array = new boolean[size];
		copyIntoBooleanArray(dataAddress+dataFetchOffset, array, 0, size);
		dataFetchOffset = dataFetchOffset + size;
		return array;
	}
     /**
     * Extracts a char array.
     * @see #getBooleanArray(int)
     */
    public char[] getCharArray(int length) throws ReadingOutOfBoundException
    {
        int size = length*2;
        if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();

        char[] array = new char[length];
        copyIntoCharArray(dataAddress+dataFetchOffset, array, 0, length);
        dataFetchOffset = dataFetchOffset + size;
        return array;
    }

    /**
     * Extracts a byte array.
     * @see #getBooleanArray(int)
     */
    public byte[] getByteArray(int length) throws ReadingOutOfBoundException
    {
        int size = length*1;
        if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();

        byte[] array = new byte[length];
        copyIntoByteArray(dataAddress+dataFetchOffset, array, 0, length);
        dataFetchOffset = dataFetchOffset + size;
        return array;
    }

	public byte[] getByteArray() throws ReadingOutOfBoundException
	{
		int size = getDataSize();
		size -= dataFetchOffset;
		if(size<0) throw new ReadingOutOfBoundException();
		if(dataFetchOffset+size > highWaterMark) highWaterMark = 0;

		byte[] array = new byte[size];
		copyIntoByteArray(dataAddress+dataFetchOffset, array, 0, size);
		dataFetchOffset = dataFetchOffset + size;
		return array;
	}
 
     /**
     * Extracts a short array.
     * @see #getBooleanArray(int)
     */
    public short[] getShortArray(int length) throws ReadingOutOfBoundException
    {
        int size = length*2;
        if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();

        short[] array = new short[length];
        copyIntoShortArray(dataAddress+dataFetchOffset, array, 0, length);
        dataFetchOffset = dataFetchOffset + size;
        return array;
    }

	public short[] getShortArray() throws ReadingOutOfBoundException
	{
		int size = getDataSize();
		size -= dataFetchOffset;
		if(size<0) throw new ReadingOutOfBoundException();
		if(dataFetchOffset+size > highWaterMark) highWaterMark = 0;

		short[] array = new short[size/2];
		copyIntoShortArray(dataAddress+dataFetchOffset, array, 0, size/2);
		dataFetchOffset = dataFetchOffset + size;
		return array;
	}
 
    /**
     * Extracts a int array.
     * @see #getBooleanArray(int)
     */
    public int[] getIntArray(int length) throws ReadingOutOfBoundException
    {
        int size = length*4;
        if(size<0) throw new ReadingOutOfBoundException();
		if(dataFetchOffset+size > highWaterMark) highWaterMark = 0;

        int[] array = new int[length];
        copyIntoIntArray(dataAddress+dataFetchOffset, array, 0, length);
        dataFetchOffset = dataFetchOffset + size;
        return array;
    }

    public int[] getIntArray() throws ReadingOutOfBoundException
    {
		int size = getDataSize();
		size -= dataFetchOffset;
		if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();

        int[] array = new int[size/4];
        copyIntoIntArray(dataAddress+dataFetchOffset, array, 0, size/4);
        dataFetchOffset = dataFetchOffset + size;
        return array;
    }
    /**
     * Extracts a long array.
     * @see #getBooleanArray(int)
     */
    public long[] getLongArray(int length) throws ReadingOutOfBoundException
    {
        int size = length*8;
        if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();

        long[] array = new long[length];
        copyIntoLongArray(dataAddress+dataFetchOffset, array, 0, length);
        dataFetchOffset = dataFetchOffset + size;
        return array;
    }

	public long[] getLongArray() throws ReadingOutOfBoundException
	{
		int size = getDataSize();
		size -= dataFetchOffset;
		if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();
		
		long[] array = new  long[size/8];
		copyIntoLongArray (dataAddress+dataFetchOffset, array, 0, size/8);
		dataFetchOffset = dataFetchOffset + size;
		return array;
	}

    /**
     * Extracts a float array.
     * @see #getBooleanArray(int)
     */
    public float[] getFloatArray(int length) throws ReadingOutOfBoundException
    {
        int size = length*4;
        if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();

        float[] array = new   float[length];
        copyIntoFloatArray  (dataAddress+dataFetchOffset, array, 0, length);
        dataFetchOffset = dataFetchOffset + size;
        return array;
    }

    public float[] getFloatArray() throws ReadingOutOfBoundException
    {
        int size = getDataSize();
		size -= dataFetchOffset;
        if(size<0) throw new ReadingOutOfBoundException();
		if(dataFetchOffset+size > highWaterMark) highWaterMark = 0;

		float[] array = new float[size/4];
        copyIntoFloatArray(dataAddress+dataFetchOffset, array, 0, size/4);
        dataFetchOffset = dataFetchOffset + size;
        return array;
    }

	/**
     * Extracts a double array.
     * @see #getBooleanArray(int)
     */
    public double[] getDoubleArray(int length) throws ReadingOutOfBoundException
    {
        int size = length*8;
        if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();

        double[] array = new  double[length];
        copyIntoDoubleArray (dataAddress+dataFetchOffset, array, 0, length);
        dataFetchOffset = dataFetchOffset + size;
        return array;
    }

    public double[] getDoubleArray() throws ReadingOutOfBoundException
    {
        int size = getDataSize();
		size -= dataFetchOffset;
		if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();
		
        double[] array = new  double[size/8];
        copyIntoDoubleArray (dataAddress+dataFetchOffset, array, 0, size/8);
        dataFetchOffset = dataFetchOffset + size;
        return array;
    }

public String[] getStringArray() throws ReadingOutOfBoundException
{
	String str;
	String array[];
	int len = 0, i, offset;
	int size = getDataSize();
	size -= dataFetchOffset;
	if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();
	
	offset = dataFetchOffset;
	while(size > 0)
	{	
		str = getString(dataAddress+dataFetchOffset, 0);
		dataFetchOffset += str.length()+1;
		size -= str.length()+1;
		len++; 	
	}
	array = new String[len];
	dataFetchOffset = offset;
	for(i = 0; i < len; i++)
	{
		array[i] = getString(dataAddress+dataFetchOffset, 0);
		dataFetchOffset += array[i].length()+1;
	}
	return array;
}

public String[] getStringArray(int num) throws ReadingOutOfBoundException
{
	String array[];
	int i;
	int size = getDataSize();
	size -= dataFetchOffset;
	if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();
	
	array = new String[num];
	for(i = 0; i < num; i++)
	{
		array[i] = getString(dataAddress+dataFetchOffset, 0);
		dataFetchOffset += array[i].length()+1;
		if(dataFetchOffset > highWaterMark) throw new ReadingOutOfBoundException();
	}
	return array;
}

    /**
     * Extracts data into an existing boolean array.
     * The data is extracted starting at the current value of the dataFetchOffset.
     * The dataFetchOffset is updated after the operation.
     * @param array Target array of the extracted data.
     * @param arrayOffset The offset into the target array.
     * @param length Number of array elements to be extracted.
     * @throws ReadingOutOfBoundException This exception is thrown if an attempts made to read an area that has
     * not been written to.
     * @see #setDataFetchOffset
     * @see #getDataFetchOffset
     */
    public void copyIntoBooleanArray(boolean[] array, int arrayOffset, int length) throws ReadingOutOfBoundException
    {
        int size = length*1;
        if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();
        copyIntoBooleanArray(dataAddress+dataFetchOffset, array, arrayOffset, length);
        dataFetchOffset = dataFetchOffset + size;
        return;
    }

    /**
     * Extracts data into an existing char array.
     * @see #copyIntoBooleanArray(boolean[], int, int)
     */
    public void copyIntoCharArray(char[] array, int arrayOffset, int length) throws ReadingOutOfBoundException
    {
        int size = length*2;
        if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();
        copyIntoCharArray   (dataAddress+dataFetchOffset, array, arrayOffset, length);
        dataFetchOffset = dataFetchOffset + size;
        return;
    }

    /**
     * Extracts data into an existing byte array.
     * @see #copyIntoBooleanArray(boolean[], int, int)
     */
    public void copyIntoByteArray(byte[] array, int arrayOffset, int length) throws ReadingOutOfBoundException
    {
        int size = length*1;
        if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();
        copyIntoByteArray   (dataAddress+dataFetchOffset, array, arrayOffset, length);
        dataFetchOffset = dataFetchOffset + size;
        return;
    }

    /**
     * Extracts data into an existing short array.
     * @see #copyIntoBooleanArray(boolean[], int, int)
     */
    public void copyIntoShortArray(short[] array, int arrayOffset, int length) throws ReadingOutOfBoundException
    {
        int size = length*2;
        if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();
        copyIntoShortArray  (dataAddress+dataFetchOffset, array, arrayOffset, length);
        dataFetchOffset = dataFetchOffset + size;
        return;
    }

    /**
     * Extracts data into an existing int array.
     * @see #copyIntoBooleanArray(boolean[], int, int)
     */
    public void copyIntoIntArray(int[] array, int arrayOffset, int length) throws ReadingOutOfBoundException
    {
        int size = length*4;
        if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();
        copyIntoIntArray(dataAddress+dataFetchOffset, array, arrayOffset, length);
        dataFetchOffset = dataFetchOffset + size;
        return;
    }

    /**
     * Extracts data into an existing long array.
     * @see #copyIntoBooleanArray(boolean[], int, int)
     */
    public void copyIntoLongArray(long[] array, int arrayOffset, int length) throws ReadingOutOfBoundException
    {
        int size = length*8;
        if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();
        copyIntoLongArray(dataAddress+dataFetchOffset, array, arrayOffset, length);
        dataFetchOffset = dataFetchOffset + size;
        return;
    }

    /**
     * Extracts data into an existing float array.
     * @see #copyIntoBooleanArray(boolean[], int, int)
     */
    public void copyIntoFloatArray(float[] array, int arrayOffset, int length) throws ReadingOutOfBoundException
    {
        int size = length*4;
        if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();
        copyIntoFloatArray(dataAddress+dataFetchOffset, array, arrayOffset, length);
        dataFetchOffset = dataFetchOffset + size;
        return;
    }
    /**
     * Extracts data into an existing double array.
     * @see #copyIntoBooleanArray(boolean[], int, int)
     */
    public void copyIntoDoubleArray(double[] array, int arrayOffset, int length) throws ReadingOutOfBoundException
    {
        int size = length*8;
        if(size<0 || dataFetchOffset+size > highWaterMark) throw new ReadingOutOfBoundException();
        copyIntoDoubleArray(dataAddress+dataFetchOffset, array, arrayOffset, length);
        dataFetchOffset = dataFetchOffset + size;
        return;
    }
    
	public int getSize()
	{
		int size = getDataSize();
		size -= dataFetchOffset;
		return size;
	}
}
