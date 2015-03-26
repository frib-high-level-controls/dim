package dim;

/** The class IncorrectUsageException of this package extends the RuntimeException of the
  * package java.lang.
  * All exceptions that inherit from this subclass represents exeptions that the user code
  * should avoid. Hence these exceptions are intended to signal coding errors and
  * the user is not expected to cath these errors (since by coding properly, this catagory
  * of error should be avoided). A user may of course use the exception mechnism as a way
  * of lazy checking as in the following example:
  * <p>
  * <pre>
  * MutableMemory reportContainer = new MutableMemory(2048);
  * reportContainer.setString(Status.getReport);
  * // add a joke if there is still room
  * String jokeOfTheDay = Joke.getJokeOfTheDay();
  * try
  * {
  *   reportContainer.setString(jokeOfTheDay);
  * }
  * catch (exception e)
  * {
  *   // never mind, there is no space for the joke of the day.
  * }
  * Server.send("/someone/to/cheer/up", reportContainer);
  *
  * </pre>
  * <br>
  * All exceptions in this package extend from the IncorrectUsageException class and not
  * directly from the RuntimeException class in the java.lang package because:
  * <ul>
  * <li>
  * This class, and hence any derived subclass, add some more specific information to the report in order to give the
  * user a strong hint that the origin of the exception resides in the user code and does not reflect a failure in the
  * implementation of this package.
  * <li>
  * This package further defines the method <code>report(}</code>. On certain non critical places, we may decide to
  * report an exception rather then throwing the exception.
  * Depending on the value of the global flag reportControl one of the following actions may be taken:
  * <ul>
  * <li>{@link #SILENT} <tab>the execution continues without error report.
  * <li>{@link #WARNING} <tab>the execution continues after a simple error report.
  * <li>{@link #TRACEBACK} <tab>the execution continues after a traceback error report.
  * <li>{@link #PEDANTIC} <tab>the exception is thrown, unless the exception is caught, the execution will terminated.
  * </ul>
  * </ul>
 * @author M.Jonker Cern
 * @version v1.2
  */
public class IncorrectUsageException extends java.lang.RuntimeException
{
    /** option flag to indicate that reports should be silent */
    public final static int SILENT = 0;
    /** option flag to indicate that a simple warning should be generated */
    public final static int WARNING = 1;
    /** option flag to indicate that a traceback report should be generated */
    public final static int TRACEBACK = 2;
    /** option flag to indicate that an exception should be thrown */
    public final static int PEDANTIC = 3;

    /**
     * Static field that indicated whether exceptions of this type should be thrown or reported only.
     * We define it private so a subclass cannot overwrite this method.
     */
    private static int reportControl = PEDANTIC;

    public IncorrectUsageException()
    {
        super();
    }

    public IncorrectUsageException(String anError)
    {
        super(anError);
    }

    /**
     * Create a more appropriate output which should orient the user in locating the origin of the problem.
     */
    public String toString()
    {
        return "The following exception is caused by incorrect usage of the dim package:\n" + super.toString();
    }

    /**
     * Sets the class specific flag whether any exceptions derived from this class should be reported or not.
     */
    public static void setReportControl(int flag)
    {
        reportControl = flag;
    }

    /**
     * Returns the report control value.
     * Subclasses may overwrite this method to control the reporting on a per subclass basis, and possibly to implement
     * a more sophisticated reporting policy, which addapt the report level as a function of the number of reports.
     * (Example: set the control level to pendantic after 5 reports).
     */
    public int getReportControl()
    {
        return reportControl;
    }

    /**
     * Dependening on the value of the reportControl value this method will report or throw this (the exception).
     */
    void report() throws IncorrectUsageException
    {
        int control = getReportControl();
        switch (control)
        {
            case SILENT:
            {
                return;
            }
            case WARNING:
            {
                System.out.println("Warning: "+this);
                return;
            }
            case TRACEBACK:
            {
                System.out.println("Warning: "+this);
                this.printStackTrace();
                return;
            }
        }
        throw this;
    }
}
