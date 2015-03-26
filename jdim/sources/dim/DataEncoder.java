package dim;

/**
  * This interface should be implemented by any class that produces outgoing data.
  * An instance of this interface can be passed to the methods
  * {@link Server#addService <code>addService()</code>}
  * and {@link Client#send(java.lang.String, CompletionHandler, int, int, boolean) <code>send()</code>}.
  * <P>Example:
  * <blockquote><pre>
  * class MyDataEncoder implements DataEncoder
  * {
  *   MutableMemory theData = new MutableMemory(32); //allocate 32 bytes
  *   public Memory encodeData()
  *   {
  *     return theData();
  *   }
  *   private void update(String aString)
  *   {
  *     // if the String length exceed the memory allocated you should take some action
  *     theData.setString(aString);
  *   }
  * }
  * </pre></blockquote>
  * @author M.Jonker Cern
  * @version v1.2
  */
public interface DataEncoder
{
    /**
      * This method is invoked when the native code needs the outgoing data from the
      * non native code.
      * @return theData An oject holding a reference to the native memory where the outgoing data is stored.
      * The implementing object is free to reuse the returned object in subsequent invokations.
      * The {@link MutableMemory} methods <code>set'<i>Primitive</i>'()</code> and
      * <code>copyFrom'<i>Primitive</i>'Array()</code>
      * can be used to fill the information in a mutable native memory object.
      */
    Memory encodeData();		        // invoked by native client requests, update_service(), send()
}
