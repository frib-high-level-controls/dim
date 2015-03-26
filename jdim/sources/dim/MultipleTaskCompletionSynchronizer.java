package dim;
import dim.CompletionHandler;

/**
 * Provides a mechanisme for multiple threads to synchronize task completions.
 * A scheduling thread that launches several tasks that will be completed by independent execution threads,
 * can synchronise with the task completion by instantiating an object of this class. For every task to be executed
 * asynchronously, it should obtain a CompletionHandler from this object and pass this to the excution thread.
 * When the execution threads completes the task it should invoke the setCompletionCode method of this interface.
 * A scheduling thread that wants to wait for the completion of one or more tasks, invokes the getCompletionCode method.
 * If any task have completed, the method will return immediately. Otherwise the method will block until one of the
 * execution thread invokes the setCompletionCode method.
 * The scheduling thread may check the completion status by invoking the non blocking checkCompletionCode
 * method.
 * The user can pass the code that should be returned when no data is available, by an argument of the
 * constructor when the class is instantiated.
 * <p>
 * This class can be further subclassed to add additional information to the completion code. The execution
 * thread can safely set the additional information before invoking the setCompletionCode. The scheduling thread
 * can safely retrieve the information after invoking the getCompletioCode method.
 * @author M.Jonker Cern
 * @version v1.2
 * @todo work in progress: finish specification and implementation (needs to have some realistic use cases).
 * I have to see how such an object of this class can be used. Would one wait for a single task completion (or) or any
 * task completion. (masks).
 * Task completion can be identified in two ways (or both). Every created CompletionHandler object is given an
 * identifier and/or a general task related object pointer.
 * @author M.Jonker Cern
 * @version v1.2
 */
public class MultipleTaskCompletionSynchronizer
{
    protected int state = CompletionHandler.IDLE;
    protected int completionCode;
    protected int theExceptionCC;
    // protected Vector theCompletionHandlers = new Vector();
    protected CompletionHandler[] theCompletionHandlers = new CompletionHandler[32];

    /**
     * Creates a new task completion synchronizer object. The NoCompletionCode that will be used by this object
     * is zero.
     */
    public MultipleTaskCompletionSynchronizer()
    {
      theExceptionCC = 0;
    }

    /**
     * Creates a new task completion synchronizer object with a predefined NoCompletionCode.
     * @param aNotCompletedCode The value to be returned when the setCompletionCode has not yet
     * been invoked.
     */
    public MultipleTaskCompletionSynchronizer(int aNotCompletedCode)
    {
      theExceptionCC = aNotCompletedCode;
    }

    /**
     * Gets a new completion handler to be passed to a task that will complete asynchronously in an independend
     * execution thread. When this task finished completion, it should call the setCompletionCode method of this
     * interface.
     * @param identifier
     * @param taskPointer. A pointer to an object (possibly the asynchronous task itself) that the
     * user may later retrieve from a completed ...
     */
    CompletionHandler getCompletionHandler(int identifier, Object taskPointer)
    {
        SingleTaskCompletionSynchronizer aCompletionHandler;
        aCompletionHandler = new SingleTaskCompletionSynchronizer();
        theCompletionHandlers[identifier]=aCompletionHandler;
        return aCompletionHandler;
    }

    /**
     * Wait for the completion of one or more completed tasks.
     */
    public int wait(int mask, int count)
    {
        /** @toto complete this method
         */
        return 0;
    }

    /**
     * Sets the completion code and wakes up any task waiting to get the completion code.
     * @param theCompletionCode The code to be returned by the getCompletionCode
     * @return The state of the TaskCompletionSynchronizer at the momement the method was executed.
     * The state can either be
     * <le> IDLE: No request was made)
     * <le> REQUESTED: request was pending),
     * <le> EXPIRED: a request was issued but timed out).
     */
    public synchronized int setCompletionCode(int theCompletionCode)
    {
      int observedState = state;
      // usage consistency check:
      if((state&CompletionHandler.COMPLETED)!=0) new CompletionHandler.ObjectInUse("Completion Code has been set already.").report();
      if(state==CompletionHandler.REQUESTED) notify(); // wake them up
      state = CompletionHandler.COMPLETED;
      completionCode = theCompletionCode;
      return observedState;
    }

    /**
     * gets the completion code if it has been set. Otherwise, this method will block and the
     * requestor will be suspended until the setCompletionCode method is invoked by the exection thread,
     * or the timeout expires.
     * If the wait times out, the NoCompletionCode will be returned.
     * @param timeout Specifies the maximum time this method may block. A zero value specifies
     * no timeout.
     */
    public synchronized int  getCompletionCode(int timeout)
    {
        // usage consistency check:
        if(state==CompletionHandler.REQUESTED) new CompletionHandler.ObjectInUse("A task is already waiting for the completion.").report();
        if(state==CompletionHandler.CONSUMED)  new CompletionHandler.ObjectInUse("The completion code has already been retrieved.").report();
        if(state!=CompletionHandler.COMPLETED)
        {
          state = CompletionHandler.REQUESTED;
          try
          {
            wait(timeout);
          }
          catch (Exception e) {}

          if(state!=CompletionHandler.COMPLETED)
          {
            state=CompletionHandler.TIMEOUT;
            return theExceptionCC;
          }
        }
        state = CompletionHandler.CONSUMED;
        return completionCode;
    }

    /**
     * gets the completion code, without timout.
     * @see #getCompletionCode()
     */
    public int  getCompletionCode()
    {
      return getCompletionCode(0);
    }

    /**
     * checks the completion code, without blocking.
     * @return The completion code if the TaskCompletionSynchronizer has completed or the
     * NoCompletionCode of this object otherwise.
     */
    public synchronized int  checkCompletionCode()
    {
      if( (state&CompletionHandler.COMPLETED)!=0) return completionCode;
      else                                        return theExceptionCC;
    }

    /**
     * checks the state of the TaskCompletionSynchronizer object.
     * @return The state, which can either be
     * <ul>
     * <li> IDLE      : no completion request is pending, no completion code has been reported
     * <li> REQUESTED : a completion request is pending.
     * <li> TIMEOUT   : a request was issued but timed out.
     * <li> COMPLETED : a completion was reported, but not yet taken by a consumer
     * <li> CONSUMED  : a reported completion request was consumed.
     * </li></ul>
     */
    public int  checkState()
    {
      return state;
    }

    /**
     * recycles an object in the CONSUMED state such that it can be used again.
     */
    public synchronized void recycle()
    {
      if(state!=CompletionHandler.CONSUMED) new CompletionHandler.ObjectInUse("The object has not been CONSUMED yet.").report();
      state = CompletionHandler.IDLE;
      return;
    }
}