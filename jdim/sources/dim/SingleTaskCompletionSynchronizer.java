package dim;

/**
 * Provides a mechanisme for two threads to synchronize on task completion.
 * A scheduling thread that launches a task that will be completed by an execution thread, can synchronise with
 * this completion by passing the task an instance of this class. When the task completes it should invoke the
 * setCompletionCode method of this object.
 * A scheduling thread that wants to wait for the task completion, invokes the getCompletionCode method.
 * If the task has completed, the method will return immediately. Otherwise the method will block until the task
 * running in the execution thread invokes the setCompletionCode method.
 * The scheduling thread may check the completion status by invoking the non blocking checkCompletionCode
 * method.
 * The user can specify the code that should be returned in case no data is available (due to a timeout or exception),
 * through an argument of the constructor.
 * <p>
 * This class can be further subclassed to add additional information to the completion code. The execution
 * thread can safely set the additional information before invoking the setCompletionCode. The scheduling thread
 * can safely retrieve the information after invoking the getCompletionCode method.
 * @author M.Jonker Cern
 * @version v1.2
 */
public class SingleTaskCompletionSynchronizer implements CompletionHandler
{
    protected int state = IDLE;
    protected int completionCode;
    protected int theExceptionCC;

    /**
     * Creates a new task completion synchronizer object. The NoCompletionCode that will be used by this object
     * is zero.
     */
    public SingleTaskCompletionSynchronizer()
    {
      theExceptionCC = 0;
    }


    /**
     * Creates a new task completion synchronizer object with a predefined CompletionCode for timeout and exceptions.
     * @param anExceptionCC The value to be returned when the setCompletionCode has not been invoked.
     */
    public SingleTaskCompletionSynchronizer(int anExceptionCC)
    {
      theExceptionCC = anExceptionCC;
    }

    /**
     * test if the completion handler matches a given state;
     * @param aState The state to be tested against.
     */
    protected final boolean inState(int aState)
    {
      return (state&aState)==aState;
    }

    /**
     * Sets the completion code and wakes up any task waiting to get the completion code.
     * @param theCompletionCode The code to be returned by the getCompletionCode
     * @return The state of the TaskCompletionSynchronizer at the momement the method was executed.
     * The state can either be
     * <ul>
     * <li> {@link #IDLE}      : No request was made.
     * <li> {@link #REQUESTED} : Request was pending.
     * <li> {@link #TIMEOUT}   : A request was issued but has timed out.
     * </li></ul>
     */
    public synchronized int setCompletionCode(int theCompletionCode)
    {
      // usage consistency check:
      if(inState(COMPLETED)) throw new CompletionHandler.ObjectInUse("Completion code has already been set.");

      int observedState = state;

      if(state==REQUESTED) notify(); // wake them up
      state = COMPLETED;
      completionCode = theCompletionCode;
      return observedState;
    }

    /**
     * gets the completion code if it has been set. Otherwise, this method will block and the
     * requestor will be suspended until the setCompletionCode method is invoked by the exection thread,
     * or the timeout expires.
     * If the wait times out, the NoCompletionCode will be returned.
     * @param timeout Specifies the maximum time this method may block in milli seconds. A zero value specifies
     * no timeout.
     */
    public synchronized int  getCompletionCode(int timeout)
    {
      // usage consistency check:
      if(inState(REQUESTED))
        if(inState(COMPLETED)) throw new CompletionHandler.ObjectInUse("The completion code has been retrieved already.");
        else                   throw new CompletionHandler.ObjectInUse("A task is already waiting for the completion.");

      if(state!=COMPLETED)
      {
        state = REQUESTED;
        try
        {
          wait(timeout);
        }
        catch (Exception e) {}
        if(state!=COMPLETED)
        {
          state=TIMEOUT;
          return theExceptionCC;
        }
      }
      state = CONSUMED;
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
     * Exception CompletionCode of this object otherwise.
     */
    public synchronized int  checkCompletionCode()
    {
      if(inState(COMPLETED)) return completionCode;
      else                   return theExceptionCC;
    }

    public int  checkState()
    {
      return state;
    }

    /**
     * recycles an object in the CONSUMED state such that it can be re-used.
     */
    public synchronized void recycle()
    {
      if(! inState(CONSUMED)) throw new CompletionHandler.ObjectInUse("The object is still in use.");
      state = IDLE;
      return;
    }
}