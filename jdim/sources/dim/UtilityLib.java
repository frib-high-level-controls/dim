package dim;


/**
 * This class provides a library of higher level utility methods to dim services.
 * @depricated
 * @author M.Jonker Cern
 * @version v1.2
 */
public class UtilityLib
{
  protected UtilityLib()
  {
  }

  /**
   * This inner class extends a SingleTaskCompletionSynchronizer to synchronize the infoService
   * actions. The decodeData method of the Native.DataDecoder interface that is passed as an argument to the
   * infoService method, is invoked by the dim Input Stream reading thread each time new data for the service
   * has becomes available.
   * Here the invokation of this method is used to synchronise with the scheduling thread (or any other thread).
   * The class has been enriched by a mechanisme that retains the last decoded result string such that it can be
   * retrieved after synchronisation.
   */
  public static class StringSynchronizer extends SingleTaskCompletionSynchronizer implements DataDecoder
  {
    String theString;
    public void decodeData(Memory theData)
    {
      if(inState(COMPLETED)) return; // we silently leave the scene
      if(theData==null) theString = "";
      else              theString = theData.getString();
      setCompletionCode(1); // wake up the client thread
    }
    public synchronized String getString()
    {
      return getString(0);
    }
    public synchronized String getString(int timeout)
    {
      if(!inState(COMPLETED)) getCompletionCode(timeout); // so we will wait
      if(!inState(COMPLETED)) return "";
      return theString;
    }
  }

  /**
   * Get the String information from a Named Service. This method will make a synchronous (blocking) request
   * for the value of the named service.
   * @param name The name of the service.
   * @return The value of the named service
   */
  public static String getStringInfo(String name)
  {
    StringSynchronizer theStringGetter = new StringSynchronizer();
    Client.infoService(name, theStringGetter, Client.ONCE_ONLY | Native.F_WAIT, 0);
    return theStringGetter.getString();
  }
}
