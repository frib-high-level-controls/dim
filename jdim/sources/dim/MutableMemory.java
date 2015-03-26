package dim;

/**
 * An object of this class holds a reference to a block of native memory that is
 * allocated and managed by the native code on behalve of a user object.
 * Objects of this class can be instantiated by user objects and are used
 * to communicate outgoing data to the native code (see the
 * {@link DataEncoder#encodeData <code>encodeData()</code>} method of the {@link DataEncoder} interface).
 * The referenced native memory can be altered by the user using the
 * <code>set<i>Primitive</i>()</code> and <code>copyFrom<i>Primitive</i>Array()</code> methods of this class.
 * @author M.Jonker Cern; Adjustments for 64bit platforms Joern Adamczewski, gsi, 27-Oct-2007
 * @version v1.2
 * @todo add mechanism to extend the data container size when needed.
 */
public class MutableMemory extends Memory
{
    /**
     * This exception is thrown by the methods of MutableMemory when a user attempts to write outside the boundaries
     * of the allocated memory area. This error refects a condition that is normally not expected to happen.
     * For this reason the Exception inherits from {@link IncorrectUsageException} (which inherits from
     * java.lang.RuntimeException RuntimeException} and hence does not have to be caught by the user.
     */
    static class NoRoomException extends IncorrectUsageException
    {
        public String toString()
        {
            return "There is no more room to accomodate the requested store.";
        }
    }


    /**
     * This exception is thrown when a user attempts to set the write offset outside the boundaries
     * of the allocated memory area. This error refects a condition that is normally not expected to happen.
     * For this reason the Exception inherits from {@link IncorrectUsageException} (which inherits from
     * java.lang.RuntimeException RuntimeException} and hence does not have to be caught by the user.
     */
    class IllegalOffsetException extends IncorrectUsageException
    {
        public String toString()
        {
            return "The specified offset exceeds the allocated size of " +allocatedSize;
        }
    }

    public MutableMemory()
    {
        dataAddress = 0;
        allocatedSize = 0;
    }

	/**
     * Creates a new MutableMemory object with preallocated initial size.
     * @param size The allocated size of the object.
     */
    public MutableMemory(int size)
    {
        dataAddress = allocateNativeDataBlock(size);
        if(dataAddress!=0) allocatedSize = size;
        else               throw new OutOfMemoryError("Native malloc failure");
    }

    /**
     * Creates a new MutableMemory object intialized with a copy of a Memory object.
     * @param source The object that will be used as a source to initialize the created object.
     */
    public MutableMemory(Memory source)
    {
        dataAddress = allocateNativeDataBlock(source.highWaterMark);
        if(dataAddress==0) throw new OutOfMemoryError("Native malloc failure");

        allocatedSize = source.highWaterMark;
        copyFromMemory(source);
    }

    protected void finalize()
    {
        if(allocatedSize!=0) releaseNativeDataBlock(dataAddress);
    }

    public void setSize(int size) throws IllegalOffsetException
    {
    	int copyIt = 0;
		if(size > allocatedSize)
		{
			MutableMemory tmpData = new MutableMemory(size);
//			System.out.println("********** Memory New, allocating "+size);
			if(allocatedSize!=0)
			{
		        copyNativeDataBlock(tmpData.dataAddress, dataAddress, highWaterMark);
				releaseNativeDataBlock(dataAddress);
				copyIt = 1;
			}
			dataAddress = allocateNativeDataBlock(size);
			if(dataAddress!=0) allocatedSize = size;
			else               throw new OutOfMemoryError("Native malloc failure");
			if(copyIt == 1)
		    	copyNativeDataBlock(dataAddress, tmpData.dataAddress, highWaterMark);
		}
		dataStoreOffset = 0;
		dataFetchOffset = 0;
		return;
    }

	/**
     * Changes the dataStoreOffset. The dataStoreOffset normally points to the end of the last data data store
     * operation. Every data store operation will store the data at the offset given by the value of dataStoreOffset.
     * After a data store operation, the dataStoreOffset is incremented with the size of the data store operation.
     * This method alows the user to change the value of the data store and to set to an lower, or higher location.
     * In case the dataStoreOffset is pushed beyon the high water mark (the highest location ever written), the
     * unwritten area will be initialized with zero.
     * @param newOffset The value to which the offset should be changed.
     * @throws IllegalOffsetException This exception is thrown if an attempt is made to set the dataStoreOffset outside
     * the allocated area.
     */
    public MutableMemory setDataStoreOffset(int newOffset) throws IllegalOffsetException
    {
        if(newOffset< 0 || newOffset > allocatedSize)  throw new IllegalOffsetException();
        if(newOffset > highWaterMark)
        {
            // memset(dataAddress+ highWaterMark, 0, newOffset - highWaterMark);
            highWaterMark = newOffset;
        }
        dataStoreOffset = newOffset;
        return this;
    }

    /**
     * Changes the dataStoreOffset with a relative amount.
     * @param delta The amount by which the offset has to be increased with (or decreased with
     * in case of a negative value).
     * @see #setDataStoreOffset
     */
    public MutableMemory moveDataStoreOffset(int delta) throws IllegalOffsetException
    {
        return setDataStoreOffset(dataStoreOffset+delta);
    }

    /**
     * Restores the dataStoreOffset to highest location ever written.
     * @see #setDataStoreOffset
     */
    public MutableMemory restoreDataStoreOffset(int delta)
    {
        dataStoreOffset = highWaterMark;
        return this;
    }


    /**
     * Changes the dataSize. The dataSize points to the end of the highest offset written and is used to
     * determines the amount of data that is transmitted by dim.
     * Normally the dataSize pointer never decreases, even when the dataStoreOffset is decreased.
     * This methods alows the dataSize to be changed, for example to reset the dataSize to zero such that a
     * MutableMemory object can be reused. The method will also set the dataStoreOffset to the same value.
     * <p><b>Remark: </b><br>
     * This method will have as a side effect to reset the dataStoreOffset to the same location as the dataSize.
     * <br>
     * In case the dataSize is lowered, any previous data in the memory block is discarded.
     * <br>
     * In a future release, when the case the dataSize is increased, the increased data area will be initialized with zero.
     * <p>
     * <b>Example:</b>
     * <blockindent><pre>
     * // Define a MutableMemory object and initialize it with a fixed header.
     * MutableMemory myMessage = new MutableMemory(1024).setString("This is a message from me\n");
     * int dataOffset = myMessage.getDataStoreOffset()-1; // -1 to remove the terminator
     *
     * public void tell(String them, String it)
     * {
     *     Client.Send(them, myMessage.setDataSize(dataOffset).setString(it));
     * }
     * </pre></blockindent>
     * @param newSize The value to new datasize.
     * @throws IllegalOffsetException This exception is thrown if an attempt is made to set the dataSize outside
     * the allocated area.
     */
    public MutableMemory setDataSize(int newSize) throws IllegalOffsetException
    {
        if(newSize< 0 || newSize > allocatedSize)  throw new IllegalOffsetException();
        if(newSize > highWaterMark)
        {
            // memset(dataAddress+ highWaterMark, 0, newOffset - highWaterMark);
        }
        highWaterMark   = newSize;
        dataStoreOffset = newSize;
        return this;
    }

    private static native long  allocateNativeDataBlock(int size);
    private static native void releaseNativeDataBlock(long nativeDataAddress);
    private static native void copyNativeDataBlock(long destinationDataAddress, long sourceDataAddress, int dataSize);


    // private native packing methods ==================================================================================

    /**
     * Copies a boolean data item into an internal data store.
     * @param nativeDataAddress The address of the NativeDataBlock where the data should be copied into.
     * @param dataOffset The data offset (in bytes) into the NativeDataBlock to where the data should be copied.
     * @param data The data item to be copied.
     */
    private static native void setBoolean(long nativeDataAddress, boolean data);

    /**
     * Copies a char data item into an internal data store.
     * @see #setBoolean(int, boolean)
     */
    private static native void setChar   (long nativeDataAddress,    char data);

    /**
     * Copies a byte data item into an internal data store.
     * @see #setBoolean(int, boolean)
     */
    private static native void setByte   (long nativeDataAddress,    byte data);

    /**
     * Copies a short data item into an internal data store.
     * @see #setBoolean(int, boolean)
     */
    private static native void setShort  (long nativeDataAddress,   short data);

    /**
     * Copies a int data item into an internal data store.
     * @see #setBoolean(int, boolean)
     */
    private static native void setInt    (long nativeDataAddress,     int data);

    /**
     * Copies a long data item into an internal data store.
     * @see #setBoolean(int, boolean)
     */
    private static native void setLong   (long nativeDataAddress,    long data);

    /**
     * Copies a float data item into an internal data store.
     * @see #setBoolean(int, boolean)
     */
    private static native void setFloat  (long nativeDataAddress,   float data);

    /**
     * Copies a double data item into an internal data store.
     * @see #setBoolean(int, boolean)
     */
    private static native void setDouble (long nativeDataAddress,  double data);

    /**
     * Copies a String data item into an internal data store.
     * @see #setBoolean(int, boolean)
     */
    private static native void setString (long nativeDataAddress,  String data);


    /**
     * Copies a boolean data array into an internal data store.
     * @param nativeDataAddress The address of the internal data store where the data should be copied into.
     * @param dataOffset The data offset (in bytes) into the internal data store to where the data should be copied.
     * @param array The data array to be copied.
     * @param arrayOffset The offset (in elements) in the data array from where the copy should start.
     * @param length The number of elements from the data array to be copied.
     */
    private static native void copyFromBooleanArray(long nativeDataAddress, boolean[] array, int arrayOffset, int length);

    /**
     * Copies a char data array into an internal data store.
     * @see #copyFromBooleanArray(int, boolean[], int, int)
     */
    private static native void copyFromCharArray   (long nativeDataAddress,    char[] array, int arrayOffset, int length);

    /**
     * Copies a byte data array into an internal data store.
     * @see #copyFromBooleanArray(int, boolean[], int, int)
     */
    private static native void copyFromByteArray   (long nativeDataAddress,    byte[] array, int arrayOffset, int length);

    /**
     * Copies a short data array into an internal data store.
     * @see #copyFromBooleanArray(int, boolean[], int, int)
     */
    private static native void copyFromShortArray  (long nativeDataAddress,   short[] array, int arrayOffset, int length);

    /**
     * Copies a int data array into an internal data store.
     * @see #copyFromBooleanArray(int, boolean[], int, int)
     */
    private static native void copyFromIntArray    (long nativeDataAddress,     int[] array, int arrayOffset, int length);

    /**
     * Copies a long data array into an internal data store.
     * @see #copyFromBooleanArray(int, boolean[], int, int)
     */
    private static native void copyFromLongArray   (long nativeDataAddress,    long[] array, int arrayOffset, int length);

    /**
     * Copies a float data array into an internal data store.
     * @see #copyFromBooleanArray(int, boolean[], int, int)
     */
    private static native void copyFromFloatArray  (long nativeDataAddress,   float[] array, int arrayOffset, int length);

    /**
     * Copies a double data array into an internal data store.
     * @see #copyFromBooleanArray(int, boolean[], int, int)
     */
    private static native void copyFromDoubleArray (long nativeDataAddress,  double[] array, int arrayOffset, int length);


    // public packing methods ==========================================================================================

    /**
     * Copies the contence of a source Memory object.
     * The data is copied starting at the current value of the dataStoreOffset.
     * The dataStoreOffset is updated after the operation.
     * @param source The Memory object to be copied.
     * @throws NoRoomException This exception is thrown if an attempt is made to write outside
     * the allocated area.
     */
    public MutableMemory copyFromMemory(Memory source) throws NoRoomException
    {
        if(dataStoreOffset+source.highWaterMark > allocatedSize) throw new NoRoomException();
        copyNativeDataBlock(dataAddress+dataStoreOffset, source.dataAddress, source.highWaterMark);
        dataStoreOffset += source.highWaterMark;
//		if(dataStoreOffset > highWaterMark)
	        highWaterMark = dataStoreOffset;
        return this;
    }

    /**
     * Copies from a boolean.
     * The data is copied starting at the current value of the dataStoreOffset.
     * The dataStoreOffset is updated after the operation.
     * @param data The data item to be copied.
     * @throws NoRoomException This exception is thrown if an attempt is made to write outside
     * the allocated area.
     */
    public MutableMemory copyBoolean(boolean data) throws NoRoomException
    {
        if(dataStoreOffset+1 > allocatedSize) throw new NoRoomException();
        setBoolean(dataAddress+dataStoreOffset, data);
        dataStoreOffset += 1;
		if(dataStoreOffset > highWaterMark)
	        highWaterMark = dataStoreOffset;
        return this;
    }

    /**
     * Copies from a char.
     * @see #setBoolean(boolean)
     */
    public MutableMemory copyChar(char data) throws NoRoomException
    {
        if(dataStoreOffset+2 > allocatedSize) throw new NoRoomException();
        setChar(dataAddress+dataStoreOffset, data);
        dataStoreOffset +=  2;
		if(dataStoreOffset > highWaterMark)
 	       highWaterMark = dataStoreOffset;
        return this;
    }

    /**
     * Copies from a byte.
     * @see #setBoolean(boolean)
     */
    public MutableMemory copyByte(byte data) throws NoRoomException
    {
        if(dataStoreOffset+1 > allocatedSize) throw new NoRoomException();
        setByte(dataAddress+dataStoreOffset, data);
        dataStoreOffset +=  1;
		if(dataStoreOffset > highWaterMark)
 	       highWaterMark = dataStoreOffset;
        return this;
    }

    /**
     * Copies from a short.
     * @see #setBoolean(boolean)
     */
    public MutableMemory copyShort(short data) throws NoRoomException
    {
        if(dataStoreOffset+2 > allocatedSize) throw new NoRoomException();
        setShort(dataAddress+dataStoreOffset, data);
        dataStoreOffset +=  2;
		if(dataStoreOffset > highWaterMark)
	        highWaterMark = dataStoreOffset;
      return this;
    }

    /**
     * Copies from an int.
     * @see #setBoolean(boolean)
     */
    public MutableMemory copyInt(int data) throws NoRoomException
    {
        if(dataStoreOffset+4 > allocatedSize) throw new NoRoomException();
        setInt(dataAddress+dataStoreOffset, data);
        dataStoreOffset +=  4;
		if(dataStoreOffset > highWaterMark)
	        highWaterMark = dataStoreOffset;
        return this;
    }

    /**
     * Copies from a long.
     * @see #setBoolean(boolean)
     */
    public MutableMemory copyLong(long data) throws NoRoomException
    {
        if(dataStoreOffset+8 > allocatedSize) throw new NoRoomException();
        setLong(dataAddress+dataStoreOffset, data);
        dataStoreOffset +=  8;
		if(dataStoreOffset > highWaterMark)
	        highWaterMark = dataStoreOffset;
        return this;
    }

    /**
     * Copies from a long.
     * @see #setBoolean(boolean)
     */
    public MutableMemory copyFloat(float data) throws NoRoomException
    {
        if(dataStoreOffset+4 > allocatedSize) throw new NoRoomException();
        setFloat(dataAddress+dataStoreOffset, data);
        dataStoreOffset +=  4;
		if(dataStoreOffset > highWaterMark)
	        highWaterMark = dataStoreOffset;
        return this;
    }

    /**
     * Copies from a double.
     * @see #setBoolean(boolean)
     */
    public MutableMemory copyDouble(double data) throws NoRoomException
    {
        if(dataStoreOffset+8 > allocatedSize) throw new NoRoomException();
        setDouble(dataAddress+dataStoreOffset, data);
        dataStoreOffset +=  8;
		if(dataStoreOffset > highWaterMark)
	        highWaterMark = dataStoreOffset;
        return this;
    }

    /**
     * Copies from a String.
     * @see #setBoolean(boolean)
     */
    public MutableMemory copyString(String data) throws NoRoomException
    {
        int length = data.length()+1;
        if(dataStoreOffset+length > allocatedSize) throw new NoRoomException();
        setString (dataAddress+dataStoreOffset, data);
        dataStoreOffset +=  length;
		if(dataStoreOffset > highWaterMark)
	        highWaterMark = dataStoreOffset;
        return this;
    }


    /**
     * Copies from a boolean data array.
     * The data is copied starting at the current value of the dataStoreOffset.
     * The dataStoreOffset is updated after the operation.
     * @param array The data array to be copied.
     * @param arrayOffset The offset (in elements) in the data array from where the copy should start.
     * @param length The number of elements from the data array to be copied.
     * @return A copy of reference to this.
     */
    public MutableMemory copyFromBooleanArray(boolean[] array, int arrayOffset, int length) throws NoRoomException
    {
        int size = length*1;
        if(dataStoreOffset+size > allocatedSize) throw new NoRoomException();
        copyFromBooleanArray(dataAddress+dataStoreOffset, array, arrayOffset, length);
        dataStoreOffset +=  size;
		if(dataStoreOffset > highWaterMark)
	        highWaterMark = dataStoreOffset;
        return this;
    }

    /**
     * Copies from a char data array.
     * @see #copyFromBooleanArray(boolean[], int, int)
     */
    public MutableMemory copyFromCharArray   (char[] array, int arrayOffset, int length) throws NoRoomException
    {
        int size = length*2;
        if(dataStoreOffset+size > allocatedSize) throw new NoRoomException();
        copyFromCharArray(dataAddress+dataStoreOffset, array, arrayOffset, length);
        dataStoreOffset +=  size;
		if(dataStoreOffset > highWaterMark)
	        highWaterMark = dataStoreOffset;
        return this;
    }

    /**
     * Copies from a byte data array.
     * @see #copyFromBooleanArray(boolean[], int, int)
     */
    public MutableMemory copyFromByteArray   (byte[] array, int arrayOffset, int length) throws NoRoomException
    {
        int size = length*1;
        if(dataStoreOffset+size > allocatedSize) throw new NoRoomException();
        copyFromByteArray(dataAddress+dataStoreOffset, array, arrayOffset, length);
        dataStoreOffset +=  size;
		if(dataStoreOffset > highWaterMark)
	        highWaterMark = dataStoreOffset;
        return this;
    }

    /**
     * Copies from a short data array.
     * @see #copyFromBooleanArray(boolean[], int, int)
     */
    public MutableMemory copyFromShortArray  (short[] array, int arrayOffset, int length) throws NoRoomException
    {
        int size = length*2;
        if(dataStoreOffset+size > allocatedSize) throw new NoRoomException();
        copyFromShortArray(dataAddress+dataStoreOffset, array, arrayOffset, length);
        dataStoreOffset +=  size;
		if(dataStoreOffset > highWaterMark)
	        highWaterMark = dataStoreOffset;
        return this;
    }

    /**
     * Copies from a int data array.
     * @see #copyFromBooleanArray(boolean[], int, int)
     */
    public MutableMemory copyFromIntArray    (int[] array, int arrayOffset, int length) throws NoRoomException
    {
        int size = length*4;
        if(dataStoreOffset+size > allocatedSize) throw new NoRoomException();
        copyFromIntArray(dataAddress+dataStoreOffset, array, arrayOffset, length);
        dataStoreOffset +=  size;
		if(dataStoreOffset > highWaterMark)
	        highWaterMark = dataStoreOffset;
        return this;
    }

    /**
     * Copies from a long data array.
     * @see #copyFromBooleanArray(boolean[], int, int)
     */
    public MutableMemory copyFromLongArray   (long[] array, int arrayOffset, int length) throws NoRoomException
    {
        int size = length*8;
        if(dataStoreOffset+size > allocatedSize) throw new NoRoomException();
        copyFromLongArray(dataAddress+dataStoreOffset, array, arrayOffset, length);
        dataStoreOffset +=  size;
		if(dataStoreOffset > highWaterMark)
	        highWaterMark = dataStoreOffset;
        return this;
    }

    /**
     * Copies from a float data array.
     * @see #copyFromBooleanArray(boolean[], int, int)
     */
    public MutableMemory copyFromFloatArray  (float[] array, int arrayOffset, int length) throws NoRoomException
    {
        int size = length*4;
        if(dataStoreOffset+size > allocatedSize) throw new NoRoomException();
        copyFromFloatArray(dataAddress+dataStoreOffset, array, arrayOffset, length);
        dataStoreOffset +=  size;
		if(dataStoreOffset > highWaterMark)
	        highWaterMark = dataStoreOffset;
        return this;
    }

    /**
     * Copies from a double data array.
     * @see #copyFromBooleanArray(boolean[], int, int)
     */
    public MutableMemory copyFromDoubleArray (double[] array, int arrayOffset, int length) throws NoRoomException
    {
        int size = length*8;
        if(dataStoreOffset+size > allocatedSize) throw new NoRoomException();
        copyFromDoubleArray(dataAddress+dataStoreOffset, array, arrayOffset, length);
        dataStoreOffset +=  size;
		if(dataStoreOffset > highWaterMark)
	        highWaterMark = dataStoreOffset;
        return this;
    }


    /**
     * Copies from a boolean data array.
     * The data is copied starting at the current value of the dataStoreOffset.
     * The dataStoreOffset is updated after the operation.
     * @param array The data array to be copied.
     * @return A copy of reference to this.
     */
    public MutableMemory copyFromBooleanArray(boolean[] array) throws NoRoomException { return copyFromBooleanArray(array, 0, array.length); }

    /**
     * Copies from a byte data array.
     * @see #copyFromBooleanArray(boolean[])
     */
    public MutableMemory copyFromByteArray(byte[] array) throws NoRoomException { return copyFromByteArray(array, 0, array.length); }

    /**
     * Copies from a byte data array.
     * @see #copyFromBooleanArray(boolean[])
     */
    public MutableMemory copyFromCharArray(char[] array) throws NoRoomException { return copyFromCharArray(array, 0, array.length); }

    /**
     * Copies from a byte data array.
     * @see #copyFromBooleanArray(boolean[])
     */
    public MutableMemory copyFromShortArray(short[] array) throws NoRoomException { return copyFromShortArray(array, 0, array.length); }

    /**
     * Copies from a byte data array.
     * @see #copyFromBooleanArray(boolean[])
     */
    public MutableMemory copyFromIntArray(int[] array) throws NoRoomException { return copyFromIntArray(array, 0, array.length); }

    /**
     * Copies from a byte data array.
     * @see #copyFromBooleanArray(boolean[])
     */
    public MutableMemory copyFromLongArray(long[] array) throws NoRoomException { return copyFromLongArray(array, 0, array.length); }

    /**
     * Copies from a byte data array.
     * @see #copyFromBooleanArray(boolean[])
     */
    public MutableMemory copyFromFloatArray(float[] array) throws NoRoomException { return copyFromFloatArray(array, 0, array.length); }

    /**
     * Copies from a byte data array.
     * @see #copyFromBooleanArray(boolean[])
     */
    public MutableMemory copyFromDoubleArray(double[] array) throws NoRoomException { return copyFromDoubleArray(array, 0, array.length); }

}
