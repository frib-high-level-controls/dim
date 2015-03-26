package dim;


/**
 * This interface should be implemented by any class that needs to handle and decode incoming data.
 * An instance of this interface is required for the methods
 * {@link Client#infoService <code>infoService()</code>}
 * and {@link Server#addCommand <code>addCommand()</code>}.
 * @author M.Jonker Cern
 * @version v1.2
 */
public interface DataDecoder
{
    /**
     * This method is invoked when the native code needs to communicate incomming data to the
     * non native code.
     * @param theData An object holding a reference to the native memory where the incomming data
     * resides. In case a connection is lost, this method will be invoked with a null argument.
     * <br>
     * The {@link Memory} methods:
     * <code>get'<i>Primitive</i>'()</code>,
     * <code>get'<i>Primitive</i>'Array()</code>and
     * <code>copyInto'<i>Primitive</i>'Array()</code>
     * can be used to extract the information from a memory object.
     */
    void decodeData(Memory theData);
}
