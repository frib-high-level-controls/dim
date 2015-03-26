package dim;

/**
 * The ObjectDescriptor class defines helper objects that provides a mapping between simple java objects and a internal data store.
 * Objects of this class may be used to assist in the packing and unpacking of native data into java objects.
 * @todo expand Object descriptor context (specially array handling, string handling etc.
 * @todo implement a method to unpack a String array based on a list of seperators (e.g. \n characters);
 *
 * @todo see if we can define a dimSerializable interface that returns the object descriptor.
 * copyIntoObject(native, DimSerializable) or will get too much coupling between dim and regular objects in such a case?
 * i.e. DeviceState should not implement DimSerializable, but can I extend DeviceState into a class that
 * implements DimSerializible? Only if no-one else wants to extend device state.
 * @author M.Jonker Cern ; Adjustments for 64bit platforms Joern Adamczewski, gsi, 27-Oct-2007
 * @version v1.2
 */
public class ObjectDescriptor
{
    static // force loading the native library
    {
        Native.loadNativeLibrary();
    }

    /** The internal address of the object descriptor object. */
    long objectDescriptorAddress;
    /** The class described by this object descriptor. */
    Class theClass;
    /** The offset where the next field will be copied. */
    int offset;

    /**
     * Creates a new instance of the object descriptor with an initial number of allocated field descriptors.
     * If later in the usage of this object more field entries are added than the initial number of allocated
     * entries, the object will automatically allocate more space for the field entries.
     * @param aClass The class for which the objectDescriptor will be contracted.
     * @param initialEntries The number of initial field descriptors.
     */
    public ObjectDescriptor(Class aClass, int initialEntries)
    {
        theClass = aClass;
        objectDescriptorAddress = newObjectDescriptor(theClass, initialEntries);
        offset=0;
    }
    /**
     * Creates a new instance of the object descriptor with an initial 10 allocated field descriptors. If later in
     * the usage of this object more field entries are added than the initial number of allocated entries, the object will
     * automatically allocate more space for the field entries.
     * @param aClass The class for which the objectDescriptor will be contracted.
     */
    public ObjectDescriptor(Class theClass)
    {
        objectDescriptorAddress = newObjectDescriptor(theClass, 10);
    }

    /**
     * Adds a field to the object descriptor.
     * @param fieldname The name of the field in the object.
     * @param fieldType The type of the field in the object.
     * @param offset The offset within the internal data store where the field is located.
     */
    public void addField(String fieldName, String fieldType)
    {
        offset+=
        addFieldToObjectDescriptor(objectDescriptorAddress, fieldName, fieldType, offset);
    }

    /**
     * Creates a new object and initializes it with information from native data according to this object descriptor.
     * @param nativeData The object holding the native data.
     * @todo create a dedicated exception for for class Instantiation exceptions
     */
    public Object newObject(Memory nativeData) throws Memory.ReadingOutOfBoundException
    {
        int size = offset;
        if(size<0 || nativeData.dataFetchOffset+size > nativeData.highWaterMark) throw new Memory.ReadingOutOfBoundException();

        Object target;
        try
        {
            target = theClass.newInstance();
        }
        catch (InstantiationException e)
        {
            throw(new RuntimeException(e.toString()));
        }
        catch (IllegalAccessException e)
        {
            throw(new RuntimeException(e.toString()));
        }
        copyIntoObject(nativeData.dataAddress+nativeData.dataFetchOffset, target, objectDescriptorAddress);
        nativeData.dataFetchOffset += size;
        return target;
    }

    /**
     * Extracts information from a Memory object into an object according to this object descriptor.
     * The data is extracted from the Memory object starting at the current value of its dataFetchOffset.
     * The dataFetchOffset is updated after the operation.
     * @param target The target object where the data should be copied into. The object should be castable to the same
     * class that was specified when the objectDescriptor was instantiated.
     * @param nativeData The object holding the native data.
     */
    public void copyIntoObject(Object target, Memory nativeData) throws Memory.ReadingOutOfBoundException
    {
        int size = offset;
        if(size<0 || nativeData.dataFetchOffset+size > nativeData.highWaterMark) throw new Memory.ReadingOutOfBoundException();
        copyIntoObject(nativeData.dataAddress+nativeData.dataFetchOffset, target, objectDescriptorAddress);
        nativeData.dataFetchOffset += size;
    }


    /**
     * Copies information from an object into a MutableMemory object according to this object descriptor.
     * The data is copied into the MutableMemory object at the current value of its dataStoreOffset.
     * The dataStoreOffset is updated after the operation.
     * @param source The source object where the data should be copied from. The object should be castable to the same
     * class that was specified when the objectDescriptor was instantiated.
     * @param nativeData The MutableMemory object holding the native data.
     */
    public void copyFromObject(Object source, MutableMemory nativeData) throws MutableMemory.NoRoomException
    {
        int size = offset;
        if(nativeData.dataStoreOffset+size > nativeData.allocatedSize) throw new MutableMemory.NoRoomException();
        // nativeData.dataSize += copyFromObject(nativeData.dataAddress, source, objectDescriptorAddress);
        copyFromObject(nativeData.dataAddress + nativeData.dataStoreOffset, source, objectDescriptorAddress);
        nativeData.dataStoreOffset += size;
        if( nativeData.highWaterMark < nativeData.dataStoreOffset) nativeData.highWaterMark = nativeData.dataStoreOffset;
    }


    /**
     * Deallocates the internal resources used by the object descriptor.
     */
    protected void finalize()
    {
        deleteObjectDescriptor(objectDescriptorAddress);
    }
    /**
     * Dumps internal data to the native output console.
     * <p>
     * <b>Note:</b> the debug messages are written to the native console and  not to the
     * <code>System.out</code> PrintStream. On certain IDE's (e.g. JBuilder) these messages
     * will not be visible on the IDE console.
     * @param dumpOptions Reserved for future use. Should be zero for future compatibility.
     */
    public void dumpInternalData(int dumpOptions)
    {
        Memory.dumpInternalData(objectDescriptorAddress, 128, dumpOptions);
    }

    //	Methods to manage object descriptors, which are used to define packing and unpacking descriptions of data objects.

    private static native long  newObjectDescriptor(Class theClass, int maxEntries);
    private static native int  addFieldToObjectDescriptor(long theObjectDescriptor, String fieldName, String fieldType, int offset);
    private static native void deleteObjectDescriptor(long theObjectDescriptor);

    private static native void copyIntoObject(long nativeDataAddress, Object theObject, long theObjectDescriptor);
    private static native void copyFromObject(long nativeDataAddress, Object theObject, long theObjectDescriptor);

}
