package dim;


/**
 * Defines the native methods that make the functionaly of the DIM C-library for servers available to the Java code.
 * @author M.Jonker Cern
 * @version v1.2
 */


public class Server
{
    static // force loading the native library
    {
      Native.loadNativeLibrary();
	  noPadding();
    }

    private Server(){} // we do not allow instantiation of this class


    /** Subscribtion method mode flag to request time stamped subscriptions. */
    public static final int  F_STAMPED            = Native.F_STAMPED;
    /** Subscribtion and Send method mode flag to request the method to block until completion. */
    public static final int  F_WAIT               = Native.F_WAIT;


    /**
     * Internal class to Synchronise receiving of data.
     * The ReceiveSynchronizer class is an extention of the SingleTaskCompletionSynchronizer. An object of this class
     * provides a handle to a thread that initiates an asynchronous transaction to resynchronise with the completion
     * of the asynchronous request. Objects of this class use the decodeNativeData method of the DataDecoder
     * interface to handle data reception. The responsability to decode NativeData is forwarded to the DataDecoder
     * object that was declared during the object instantiation. After this method invokation, the setCompletionCode
     * method of the super class (SingleTaskCompletionSynchronizer) will be invoked, but only upon the first invokation
     * of this method.
     * Objects of this class are instantiated directly by the Native code when needed, i.e. in case a
     * info_service() method is invoked with the wait option.
     */
    private static class ReceiveSynchronizer extends SingleTaskCompletionSynchronizer implements DataDecoder
    {
      /**
       * The user specified object that implements the DataDecoder interface. The responsability to
       * decode native data is delegated to the decodeNativeData method of this object.
       */
       DataDecoder theNativeDataDecoder;
      /**
       * @param aNativeDataDecoder Specifies the object that implements the DataDecoder interface. The
       * responsability to decode native data will be delegated to the decodeNativeData method of this object.
       */
      ReceiveSynchronizer(DataDecoder aNativeDataDecoder)
      {
        theNativeDataDecoder = aNativeDataDecoder;
      }
      /**
       * decodes native data. This method is invoked when native data arrives. The method delegates the responsability
       * to decode native data to the decodeNativeData method of the DataDecoder interface that was specified
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


//  not implemented
//	public static void change_address(int service_id, int new_address, int new_size);
//	public static native int find_service(string name);		// not implemented ?
//	public static native string get_id();					// name@node of client
//	public static native int get_quality(int service_id);
//	public static native int get_timestamp(int service_id, int *secs, int *milisecs);
//	public static native string get_format(int service_id);
//	public static native void disable_padding();			// not implemented ?

  /**
   * This exception is thrown a when user attempts to redefine the servername.
   * This error refects a condition that is normally not expected to happen.
   * For this reason the Exception inherits from {@link IncorrectUsageException} (which inherits from
   * java.lang.RuntimeException RuntimeException} and hence does not have to be caught by the user.
   */
    public static class NameRedefined extends IncorrectUsageException
    {
        public String toString() { return "The server name is redefined after the server started serving";}
    }
  /**
   * This exception is thrown when a user calls request registration of its services witout having
   * given a servername.
   * This error refects a condition that is normally not expected to happen.
   * For this reason the Exception inherits from {@link IncorrectUsageException} (which inherits from
   * java.lang.RuntimeException RuntimeException} and hence does not have to be caught by the user.
   */
    public static class NameUndefined extends IncorrectUsageException
    {
        public String toString() { return "The server name is unknown while start services";}
    }

    private static String theServerName = null;

    /**
      * Instruct DIM that all declared services are to be registered and activated.
      * @param task_name The name by which this server will be known.
      * Note that the task name should be unique in the dim name space (DNS) registry.
      */
    private static native int    startServing(String task_name);

    /**
      * Sets the name of the server by wich the server will be announced on the middleware. The name is
      * associated with all declared services and commands. The name should be set once before the first invokation
      * of the {@ling #startServing()} method. Atempst to set the name a second time call will cause the
      * method to throw the Server.NameRedefined runtime exception.
      * @param task_name The name by which this server will be known.
      * <p>
      * <b>Remark:</b><br>The task name should be unique in the dim name space (DNS) registry.
      */
    public static void setServerName(String serverName) throws NameRedefined
    {
        if(theServerName!=null) new NameRedefined().report();
        theServerName = serverName;
    }

    /**
      * Instruct DIM that all newly declared services are to be registered and activated.
      * Newly declared services are not activated unless the user invokes the startServing() method.
      * This method may be invoked more than once. Before the first time this method is invoked,
      * however, the user should have made a call to the setServerName method to declare the name of the server.
      * Failure to do so will throw the serverNameUndefined runtime exception.
      * <p>
      * <b>Remark:</b><br>All added services and commands, as well as the server name itself must be unique
      * unique in the dim name space (DNS) registry. The current implementation will force the server to exit
      * if any of the names are already in use.
      */
    public static int registerServices() throws Server.NameUndefined
    {
        if(theServerName==null) new NameUndefined().report();
        return startServing(theServerName);
    }

    /**
     * Suspend serving of all services.
     */
    public static native void   stopServing();

    /**
     * get the name of the calling client;
     * This method can be invoked during the execution of the encodeData or decodeData methods.
     * Outside the scope of these methods, the return will be meaningless.
     * @return The name of the calling client.
     */
    public static native String getClient();

	/**
	 * get the list of services subscribed by the calling client;
	 * This method can be invoked during the execution of the encodeData or decodeData methods.
	 * or during the error_handling method.
	 * Outside the scope of these methods, the return will be meaningless.
	 * @return The list of services separated by '\n'.
	 */
	public static native String getServices();
	
    /**
     * get the internal connection number of the calling client;
     * This method can be invoked during the execution of the encodeData or decodeData methods.
     * Outside the scope of these methods, the return will be meaningless.
     * @return The internal connection number of the client, an index in the range from 0 to the maximum
     * number of subscribing clients to the server.
     */
    public static native int    getClientConnID();

    /**
     * adds a named service to the server.
     * @param serviceName The name by which this service will be registered in the Dim Name Space (DNS) registry.
     * @param serviceType The type of the service, specifying the data format.
     * @param theNativeDataEncoder An object that implements the DataEncoder interface and that
     * will cook the Native data on request for a client.
     * @return The service registration ID.
     * <p>An example of usage for the addService is given by:
     * <blockquote><pre>
     * class StringService extends MutableMemory implements DataEncoder
     * {
     *     int service_id;
     *     StringService(String theServiceName)
     *     {
     *         super(28); // preallocate 28 bytes of MutableMemory
     *         service_id = addService(theServiceName, "C", this);
     *     }
     *     protected void finalize()
     *     {
     *         removeService(service_id);
     *     }
     *
     *     public Memory encodeData()
     *     {
     *         return this;
     *     }
     *
     *     public void Update(String aString)
     *     {
     *         this.setString(0, aString);
     *         updateService(service_id);
     *     }
     * }
     * </pre></blockquote>
     *
     */

    public static native int    addService(String serviceName, String serviceType, DataEncoder theNativeDataEncoder);

    // Note: we do not define an addService() method with a Memory parameter.
    // Specifying only a method with a Memory type parameter, would not alow us to be informed when data is
    // required. (Alowing the data to be tailored to the requesting user).
    // If a Memory type parameter is beneficial for efficiency reasons, then the responsability for maintaining
    // such a permanent reference to the Memory object can be given to the class implementing the DataEncoder.
    // Example: see the class dim.test.StringService

    /**
     * adds a named command service to the server.
     * @param commandName The name by which this command will be registered in the Dim Name Space (DNS) registry.
     * @param commandType The type of the service, specifying the data format.
     * @param theNativeDataDecoder An object that implements the DataDecoder interface and that
     * will digest the Native data from a client command.
     * @return The service registration ID.
     */
    public static native int    addCommand(String commandName, String commandType, DataDecoder theNativeDataDecoder);

//  void add_client_exit_handler(void (*usr_routine)(...));
//  void set_client_exit_handler(int conn_id, int tag);
//  void add_exit_handler(void (*usr_routine)(...));
//  void report_service(char *service_name);

    /**
     * Signals a specific client.
     * @param service_id The service identification that was returned when the service was registered.
     * @param client_id The internal connection number (as returned
     * by get_client_connID() ) of the client that is to be notified.
     */
    public static int  selectiveUpdateService(int serviceId, int clientId) { return selectiveUpdateService(serviceId, new int[] {clientId, 0});}

    /**
     * Signals a selective list of clients.
     * @param service_id The service identification that was returned when the service was registered.
     * @param client_id_list The list that contains the internal connection number (as returned
     * by get_client_connID() ) of the clients that are to be notified. Note: the last element of the list should
     * contain zero.
     */
    public static native int  selectiveUpdateService(int serviceId, int[] clientIdList);

    /**
     * Signals all subscribing clients of the service.
     * @param service_id The service identification that was returned when the service was registered.
     */
    public static        int  updateService(int serviceId) { return selectiveUpdateService(serviceId, null); }

    /**
     * removes a previously registered information or command service.
     * @param service_id The service identification that was returned when the service was registered.
     */

	public static native void  noPadding();
	
	public static native int  removeService(int serviceId);

    /** @todo look at these not implemented server functions
      * int    get_next_cmnd(int *tag, int *buffer, int *size );
      *        public static native int find_service(string name);
      * void add_client_exit_handler(void (*usr_routine)(...));
      * void set_client_exit_handler(int conn_id, int tag);
      * void add_exit_handler(void (*usr_routine)(...));
      * void report_service(char *service_name);
      * void send_service(int service_id, int *buffer, int size);	// where is this used?
      * int  set_buffer_size(int size);
      * void set_quality(int service_id, int quality);
      * void set_timestamp(int service_id, int secs, int millisecs);
      * void disable_padding();
     */

    /**
     * A simple class that provides an Object Oriented wrapper around the add_service function.
     * <p>
     * This class may in a future release be extended with convenience methods to inquire on
     * properties such as name, connected clients, buffers etc.
     * <p>
     * In a future version this class will be extended with an autmatic call to remove the service once there
     * are no more references to this class. To ensure continuation of the serivce, the user should keep
     * a reference to this class as long as its services are required.
     * <br>For performance and stability reasons, however, the user is encouraged to use the remove method
     * explicitly and not to rely on the automatic clean up facility (which is not actually implemented).
     * @see #addService
     * @todo implement a getStatus method  etc.
     */
    public static class Info
    {
        /**
         * The service_id off the service.
         */
        private int serviceId;

        /**
         * Creates a new instance of a info service.
         * @param serviceName The name by which this service will be registered in the Dim Name Space (DNS) registry.
         * @param serviceType The type of the service, specifying the data format.
         * @param theNativeDataEncoder An object that implements the DataEncoder interface and that
         * will cook the Native data on request for a client.
         * @see #addService addService
         */
        public Info(String serviceName, String serviceType, DataEncoder theNativeDataEncoder)
        {
            serviceId = addService(serviceName, serviceType, theNativeDataEncoder);
        }


        /**
         * Signals all subscribing clients of the service.
         */
        public void update()
        {
            updateService(serviceId);
        }


        /**
         * Signals a selected client only.
         * @param client_id The internal connection number (as returned
         * by get_client_connID() ) of the client that should be notified.
         */
        public void selectiveUpdate(int clientId)
        {
            selectiveUpdateService(serviceId, new int[] {clientId, 0});
        }

        /**
         * Signals a selective list of clients.
         * @param client_id_list The list that contains the internal connection number (as returned
         * by get_client_connID() ) of the clients that are to be notified.
         */
        public void selectiveUpdate(int[] clientIdList)
        {
            selectiveUpdateService(serviceId, clientIdList);
        }

        /**
         * Removes the registered service.
         */
        public void remove()
        {
            removeService(serviceId);
        }
    }


    /**
     * A simple class that provides an Object Oriented wrapper around the add_cmnd function.
     * <p>
     * This class may in a future release be extended with convenience methods to inquire on
     * properties such as name, connected clients, buffers etc.
     * <p>
     * In a future version this class will be extended with an autmatic call to remove the service once there
     * are no more references to this class. To ensure continuation of the serivce, the user should keep
     * a reference to this class as long as its services are required.
     * <br>For performance and stability reasons, however, the user is encouraged to use the remove method
     * explicitly and not to rely on the automatic clean up facility (which is not actually implemented).
     * @see #addCommand
     * @todo implement a getStatus method  etc.
     */
    public static class Command
    {
        /**
         * The service_id off the service.
         */
        private int serviceId;

        /**
         * Creates a new instance of command service to the server.
         * @param commandName The name by which this command will be registered in the Dim Name Space (DNS) registry.
         * @param serviceType The type of the service, specifying the data format.
         * @param theNativeDataDecoder An object that implements the DataDecoder interface and that
         * will digest the Native data from a client command.
         * @see #addCommand addCommand
         */
        public Command(String commandName, String commandType, DataDecoder theNativeDataDecoder)
        {
            serviceId = addCommand(commandName, commandType, theNativeDataDecoder);
        }

        /**
         * Removes the registered service.
         */
        public void remove()
        {
            removeService(serviceId);
        }
    }

}