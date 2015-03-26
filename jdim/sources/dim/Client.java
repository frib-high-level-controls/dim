package dim;

/**
 * Defines the native methods that make the functionaly of the DIM C-library for clients available to the Java code.
 * <p>The following shows a simple example of a client object that makes use of the
 * <code>infoService()</code> and <code>sendString()</code> class methods.
 * <blockquote><pre>
 *
 * import dim.*;
 * public class ExpertDeviceHandler
 * {
 *   String deviceName;
 *   public ExpertDeviceHandler(String device)
 *   {
 *     deviceName = device;
 *   }
 *
 *   String getExpertStatus()
 *   {
 *
 *     DataDecoder theStringDecoder = new DataDecoder()
 *       {
 *         String theString;
 *         decodeData(Memory theData)
 *         {
 *           theString = theData.getString(0);
 *         }
 *       };
 *
 *     // note: since we use once_only with wait,
 *     //       we do not need to remember the service_id
 *     Client.infoService(deviceName+"/ExpertStatus",
 *                         theStringDecoder,
 *                         ONCE_ONLY | F_WAIT, 0);
 *     return theStringDecoder.theString;
 *   }
 *
 *   setExpertAction(String theAction)
 *   {
 *     Client.send(deviceName+"/ExpertStatus",
 *                 null, F_WAIT, 0, theAction);
 *   }
 * }
 * </blockquote></pre>
 * @author M.Jonker Cern ; Adjustments for 64bit platforms Joern Adamczewski, gsi, 31-Oct-2007
 * @version v1.2
 * @todo define a send class. The send class subscribes to the send property so we can check for the availability.
 */

public class Client
{
    static // force loading the native library
    {
      Native.loadNativeLibrary();
	  noPadding();
    }

    private Client(){} // we do not allow instantiation of this class


    /** Subscribtion method mode option for requesting a once only subscription (a singe get).
     * Used as an argument to {@link #infoService <code>infoService()</code>}.
     */
    public static final int  ONCE_ONLY            = Native.ONCE_ONLY;
    /** Subscribtion method mode option for requesting a timed subscription.
     * Used as an argument to {@link #infoService <code>infoService()</code>}.
     * Indicates that the subscription is timed and the information should be refreshed periodically.
     */
    public static final int  TIMED                = Native.TIMED;
    /**
     * Subscribtion method mode option for requesting a monitored subscription.
     * Used as an argument to {@link #infoService <code>infoService()</code>}.
     * Indicates that the subscription is monitored and information should be refreshed when the server signales a change.
     */
    public static final int  MONITORED            = Native.MONITORED;

	public static final int  MONIT_ONLY           = Native.MONIT_ONLY;
	
    public static final int  UPDATE               = Native.UPDATE;

	public static final int  TIMED_ONLY           = Native.TIMED_ONLY;
	public static final int  MONIT_FIRST          = Native.MONIT_FIRST;

	/** Subscribtion method mode flag to request time stamped subscriptions. */
    public static final int  F_STAMPED            = Native.F_STAMPED;
    /** Subscribtion and Send method mode flag to request the method to block until completion. */
    public static final int  F_WAIT               = Native.F_WAIT;

    /**
     * Internal class to Synchronize sending of data.
     * The SendSynchronizer class is an extention of the SingleTaskCompletionSynchronizer. On object of this class
     * provides a handle for a thread that initiates an asynchronous transaction to resynchronise with the completion
     * of this asynchronous request. The extented functionality provided by the SendSynchronizer class alows a user
     * of this object to specify an additional CompletionHandler that will be called when the asynchronous task
     * completes.
     * Objects of this class are instantiated directly by the Native code when needed, i.e. in case a
     * send() method is invoked with the wait option.
     */
    private static class SendSynchronizer extends SingleTaskCompletionSynchronizer
    {
      /**
       * The user specified object that implements the CompletionHandler interface. If not null the
       * setCompletionCode method of this object will be invoked upon completion of the send.
       */
      CompletionHandler theCompletionHandler;
      /**
       * @param aCompletionHandler Specifies the object that implements the CompletionHandler interface
       * whos setCompletionCode method is to be invoked upon completion of the send.
       */
      SendSynchronizer(CompletionHandler aCompletionHandler)
      {
        theCompletionHandler=aCompletionHandler;
      }
      /**
       * This method overwrites the setCompletionCode method of SingleTaskCompletionSynchronizer in order to invoke
       * the setCompletionCode method the CompletionHandler interface that was specified when this object was created.
       */
      public int setCompletionCode(int theCompletionCode)
      {
        if(theCompletionHandler!=null) theCompletionHandler.setCompletionCode(theCompletionCode);
        return super.setCompletionCode(theCompletionCode);
      }
    }


    /**
     * Internal class to Synchronise receiving of data.
     * The ReceiveSynchronizer class is an extention of the SingleTaskCompletionSynchronizer. An object of this class
     * provides a handle to a thread that initiates an asynchronous transaction to resynchronise with the completion
     * of the asynchronous request. Objects of this class use the {@link DataDecoder#decodeData <code>decodeData()</code>}
     * method of the {@link DataDecoder <code>DataDecoder</code>} interface to handle data reception. The responsability to
     * decode the native data is forwarded to the <code>DataDecoder</code> interface specified when the object was instantiated.
     * After this method invokation, the setCompletionCode
     * method of the super class (SingleTaskCompletionSynchronizer) will be invoked, but only upon the first invokation
     * of this method. This implies that when the <code>F_WAIT</code> option was specified with one of the subscription modes
     * (i.e. <code>MONITORED</code> or <code>TIMED</code>),
     * the system will unblock on the first reply, but the subscription will stay active.
     * Objects of this class are instantiated directly by the Native code when needed, i.e. in case a
     * <code>infoService()</code> method is invoked with the <code>F_WAIT</code> option.
     */
    private static class ReceiveSynchronizer extends SingleTaskCompletionSynchronizer implements DataDecoder
    {
      /**
       * The user specified object that implements the <code>DataDecoder</code> interface. The responsability to
       * decode native data is delegated to the <code>decodeData</code> method of this object.
       */
       DataDecoder theNativeDataDecoder;
      /**
       * @param aNativeDataDecoder Specifies the object that implements the <code>DataDecoder</code> interface. The
       * responsability to decode native data will be delegated to the <code>decodeData</code> method of this object.
       */
      ReceiveSynchronizer(DataDecoder aNativeDataDecoder)
      {
        theNativeDataDecoder = aNativeDataDecoder;
      }
      /**
       * decodes native data. This method is invoked when native data arrives. The method delegates the responsability
       * to decode native data to the <code>decodeData</code> method of the <code>DataDecoder</code> interface that was specified
       * when this object was instantiated. Finally this method will invoke the setCompletionCode() of the super class,
       * if this has not yet been done before, in order to wake up any client.
       */
      public void decodeData(Memory theData)
      {
        theNativeDataDecoder.decodeData(theData);
        // Test the state, so we set the completion state only once:
        int state = super.checkState();
        if( (state & CompletionHandler.COMPLETED)==0 ) setCompletionCode(1);
      }
    }

    /**
     * Sends a boolean data item to the named service.
     * If the aCompletionHandler parameter is present, the setCompletionCode method of the CompletionHandler
     * interface will be invoked when the Send action has completed. A send action completes either when the data has
     * been delivered to the remote service, or when the name of the service cannot be resolved with the name server.
     * @param name The name of the named service.
     * @param aCompletionHandler An object that implements the CompletionHandler interface.
     * @param mode Bit mask to control to mode of the send operation.
     * <BR> The following option flag is recognized:
     * <UL>
     * <LI> {@link #F_WAIT} wait for send delivery completion.
     * </LI></UL>
     * @param reserved Reserved for future (timeout?) usage. Should be zero for future compatibility.
     * @param data The data to be send to the remote service.
     * @return The completionCode of the send action. In case the WAIT option flag was present in the mode
     * parameter, the completionCode will be the completion code of the send operation.
     * Otherwise, the completion code will indicate whether or not the request was queued successfully.
     */
    public static native int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved, boolean data);

    /**
     * Sends a char data item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    public static native int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved,    char data);
    /**
     * Sends a byte data item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    public static native int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved,    byte data);
    /**
     * Sends a short data item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    public static native int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved,   short data);
    /** 
     * Sends a int data item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    public static native int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved,     int data);
    /**
     * Sends a long data item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    public static native int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved,    long data);
    /**
     * Sends a float data item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    public static native int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved,   float data);
    /**
     * Sends a double data item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    public static native int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved,  double data);
    /**
     * Sends a String data item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    public static native int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved,  String data);

    /**
     * Sends a boolean array data item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    public static native int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved, boolean[] dataArray);
    /**
     * Sends a char data array item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    public static native int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved,    char[] dataArray);
    /**
     * Sends a byte array data item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    public static native int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved,    byte[] dataArray);
    /**
     * Sends a short array data item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    public static native int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved,   short[] dataArray);
    /**
     * Sends a int array data item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    public static native int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved,     int[] dataArray);
    /**
     * Sends a long array data item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    public static native int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved,    long[] dataArray);
    /**
     * Sends a float array data item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    public static native int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved,   float[] dataArray);
    /**
     * Sends a double array data item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    public static native int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved,  double[] dataArray);

    /**
     * Sends a nativeDataBlock data item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    private static native int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved,  long nativeDataBlock, int nativeDataSize);

    /**
     * Sends a nativeDataBlock data item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    public static int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved,  Memory theData)
    {
        return send(name, aCompletionHandler, mode, reserved, theData.dataAddress, theData.highWaterMark);
    }
    /**
     * Sends a nativeDataBlock data item to the named service.
     * @see #send(java.lang.String, CompletionHandler, int, int, boolean)
     */
    public static int send(String name, CompletionHandler aCompletionHandler, int mode, int reserved,  DataEncoder aNativeDataEncoder)
    {
        Memory theData = aNativeDataEncoder.encodeData();
        return send(name, aCompletionHandler, mode, reserved, theData.dataAddress, theData.highWaterMark);
    }


    /**
     * Subscribe to a named service.
     * @param name The name of the named service.
     * @param data An object that implements the <code>DataDecoder</code> interface that will receive the data.
     * @param mode A parameter to control to mode of the send operation.
     * <BR> The following options are recognized:
     * <UL>
     * <LI> {@link #ONCE_ONLY} Make a once only subscription.
     * The timeout parameter specifies the time interval by which the reply should arrive.
     * <LI> {@link #TIMED} Make a timed subscription.
     * The timeout parameter specifies the time interval by which the data will be refreshed.
     * <LI> {@link #MONITORED} Make a monitored subscription.
     * The data will be refreshed when signalled by the server.
     * If a non zero timeout value is given, the data will also be refreshed periodically.
     * </LI></UL>
     * In addition, the following modification flags can be added to the mode:
     * <UL>
     * <LI> {@link #F_STAMPED} Make the data reception time stamped.
     * <LI> {@link #F_WAIT} Wait for receive delivery completion.
     * In case the wait option is used with any subscription mode (i.e. {@link #TIMED} or {@link #MONITORED}), the method will block until the
     * first data delivery.
     * </LI></UL>
     * @param timeout Defines the timeout or the refresh rate of the data.
     * @return The service identification of the subscription. The service identification is required to
     * unsubscribe from the service.
     * <p> <b>Remark:</b><br> The responsability to release a service which is not {@link #ONCE_ONLY} is with the client.
     * To unsubscribe to a service the client should use the service_id which is returned by this method.
     *
     */
    public static native int infoService(String name, DataDecoder data, int mode, int timeout);
    /**
     * Releases a subscribtion to a service.
     * @param serviceId The service id that was returned when the subscribtion was made.
     */
    public static native void releaseService(int serviceId);
    /**
     * No Padding on received structures
     */
    public static native void noPadding();

	public static native String getFormat(int serviceId);
    /**
     * Inform the Server that this client would like the Server to execute an ExitHandler when it dies.
     */
    public static int setExitHandler ( String serverName)
    {
        return send(serverName + "/EXIT", null, 0, 0, (int) 1);
    }

	/**
	 * get the name of the server providing the current service;
	 * This method can be invoked during the execution of the encodeData or decodeData methods.
	 * Outside the scope of these methods, the return will be meaningless.
	 * @return The name of the current server.
	 */
	public static native String getServer();

	/**
	 * get the list of services subscribed by the current server;
	 * This method can be invoked during the execution of the encodeData or decodeData methods.
	 * or during the error_handling method.
	 * Outside the scope of these methods, the return will be meaningless.
	 * @return The list of services separated by '\n'.
	 */
	public static native String getServices();
	
	public static native void stop();
	/**
	 * get the internal connection number of the current server;
	 * This method can be invoked during the execution of the encodeData or decodeData methods.
	 * Outside the scope of these methods, the return will be meaningless.
	 * @return The internal connection number of the client, an index in the range from 0 to the maximum
	 * number of subscribing clients to the server.
	 */
	public static native int    getServerConnID();

	/**
	 * get the Process ID of the current server;
	 * This method can be invoked during the execution of the encodeData or decodeData methods.
	 * Outside the scope of these methods, the return will be meaningless.
	 * @return The server's PID.
	 */
	public static native int    getServerPID();

    /**  @todo look at these not implemented client functions
     *   public static void change_address(int service_id, int new_address, int new_size);
     *   public static native int find_service(string name);		// not implemented ?
     *   public static native string get_id();				// name@node of client ??
     *   public static native int get_quality(int service_id);
     *   public static native int get_timestamp(int service_id, int *secs, int *milisecs);
     *   public static native string get_format(int service_id);
     *   public static native void disable_padding();			// not implemented ?
     */

    /**
     * A simple class that provides an Object Oriented interface wrapper around the infoService function.
     * <p>
     * This class may in a future release be extended with convenience methods to inquire on subscription
     * properties such as name, name and network address of the implementing server, availability, format etc.
     * <p>
     * In a future version this class will be extended with an autmatic call to release service once there
     * are no more references to this class. To ensure continuation of the subscription, the user should keep
     * a reference to this class as long as its services are required.
     * <br>For performance and stability reasons, however, the user is encouraged to use the release method
     * explicitly and not to rely on the automatic clean up facility (which is not actually implemented).
     * @see #infoService
     * @todo implement a getStatus method
     */
    public static class Subscription
    {
        /**
         * The service_id off the server.
         */
        private int serviceId;

        /**
         * Creates a new instance of a subscription.
         * @see #infoService
         */
        public Subscription(String name, DataDecoder data, int mode, int timeout)
        {
            serviceId = infoService(name, data, mode, timeout);
        }

        /**
         * Releases the subscribtion.
         */
        public void release()
        {
            releaseService(serviceId);
        }
    }
}
