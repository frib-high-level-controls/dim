package dim;

/**
 * This interface specifies a method to assist threads in the synchronization of task completion.
 * A scheduling thread that launches one or more tasks that run concurrently in one or more execution
 * threads, can synchronise with the task completions by passing every task an instance of this interface. When the
 * execution threads completes a task, this task should invoke the setCompletionCode method of this interface.
 * Depending on the implementation of this interface, a scheduling thread can check the completion state, or
 * synchronise with one or more asynchronous tasks.
 * @see dim.SingleTaskCompletionSynchronizer
 * @see dim.MultipleTaskCompletionSynchronizer
 * @author M.Jonker Cern
 * @version v1.2
 */
public interface CompletionHandler
{

  /** The task is neither completed, neither requested. */
  public static final int IDLE = 0;
  /** The task is not yet completed but the completion status is requested */
  public static final int REQUESTED = 1;
  /** The task is completed, but the completion status has not yet been retrieved. */
  public static final int COMPLETED = 2;
  /** The task is completed and the completion status has been retrieved. */
  public static final int CONSUMED  =  REQUESTED + COMPLETED;
  /** The task is not yet completed but the completion status has been requested and timed out */
  public static final int TIMEOUT   = 4;


  /**
   * Exception class to signal a user code error. This exception should be thrown by an implementation
   * of the CompletionHandler interface when the user calls more than once the setCompletionCode
   * or the getCompletionCode method. This error refects a condition that is normally not expected to happen.
   * For this reason the Exception inherits from {@link IncorrectUsageException} (which inherits from
   * java.lang.RuntimeException RuntimeException} and hence does not have to be caught by the user.
   */
  static public class ObjectInUse extends IncorrectUsageException
  {
      public ObjectInUse(String reason)
      {
          super(reason);
      }
  }

  /**
   * Sets the completion code of the completed task and takes any needed actions to wake up a waiting thread.
   * @param theCompletionCode A completion status that can be made available to other threads.
   * @return The state of the CompletionHandler at the momement the method was invoked.
   * The state can either be
   * <ul>
   * <li> {@link #IDLE}      : No request was made.
   * <li> {@link #REQUESTED} : Request was pending.
   * <li> {@link #TIMEOUT}   : A request was issued but has timed out.
   * </li></ul>
   */
  public int setCompletionCode(int theCompletionCode);

  /**
   * checks the state of a CompletionHandler.
   * @return The state. The possible return states are given by:
   * <ul>
   * <li> {@link #IDLE}      : no completion request is pending, no completion code has been reported.
   * <li> {@link #REQUESTED} : a completionCode get request is pending.
   * <li> {@link #COMPLETED} : the completionCode was set but not yet retrieved by a consumer.
   * <li> {@link #CONSUMED}  : the completionCode was set and retrieved by a consumer.
   * <li> {@link #TIMEOUT}   : a request was issued but timed out.
   * </li></ul>
   */
  public int  checkState();
}
