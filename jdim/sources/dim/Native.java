package dim;
import java.util.Vector;


/**
 * Defines the native methods that loads the native library into memory,
 * this class is loaded automatically when any of the other classes in this package are loaded.
 *
 * Class to loads and initialize the dim native library. The shareable library is search for on the following locations:
 * <ul>
 * <li> If the System Property <code>"dim.dll.path"</code> is defined then the value returned by
 * <code>System.getProperty("dim.dll.path")</code> will be tried first by dim.Native.
 * <li> The URL location of the dim.Native.class is located on the class path. The name dim/Native.class will be
 * stripped from the URL. If URL specifies a JAR file, the jar file name will also be stripped from the URL.
 * If the resulting URL terminates with either <code>"/classes/"</code> or <code>"/lib/"</code> the dim.Native class
 * will attempt to load the library from the path <code>URL+"../bin/dim.dll"</code>.
 * <li> Next the dim.Native class will attempt to load the library from the path <code>URL+"/dim.dll"</code>.
 * <li> Finally the dim.Native class will attempt to load the native library using the path <code>"dim.dll"</code>.
 * In this case the system will resolve the location of the shareable library possibly based on a path environment variable.
 * </ul>
 * <br>
 * The user may modifies the Default Search Path for the shareable native library by invoking the method
 * {@link #setDllSearchPath <code>setDllSearchPath()</code>}.
 * @author M.Jonker Cern
 * @version v1.2
 * @todo add a general explanation of the DIM phylosophy (property model)
 */
public class Native
{
    private Native(){} // We do not allow instantiation of this class


    /** Subscribtion method mode option for requesting a once only subscription (a singe get).
     * Used as an argument to {@link Client#infoService <code>infoService()</code>}.
     */
    static final int  ONCE_ONLY            = 0x01;
    /** Subscribtion method mode option for requesting a timed subscription.
     * Used as an argument to {@link Client#infoService <code>infoService()</code>}.
     * Indicates that the subscription is timed and the information should be refreshed periodically.
     */
    static final int  TIMED                = 0x02;
    /** Subscribtion method mode option for requesting a monitored subscription.
     * Used as an argument to {@link Client#infoService <code>infoService()</code>}.
     * Indicates that the subscription is monitored and information should be refreshed when the server signales a change.
     */
    static final int  MONITORED            = 0x04;
	
    static final int  MONIT_ONLY           = 0x20;
	
    static final int  UPDATE               = 0x40;

	static final int  TIMED_ONLY           = 0x80;
	static final int  MONIT_FIRST          = 0x100;

	/** Subscribtion method mode flag to request time stamped subscriptions. */
    static final int  F_STAMPED            = 0x00001000;
    /** Subscribtion and Send method mode flag to request the method to block until completion. */
    static final int  F_WAIT               = 0x10000000;

    static int dim_version=0; // we use this variable also to see if loading is (recursivly) active or not

	// DIM Error Severities
	static final int  DIM_INFO 		= 0;
	static final int  DIM_WARNING 	= 1;
	static final int  DIM_ERROR 	= 2;
	static final int  DIM_FATAL 	= 3;
	// DIM Error codes
	static final int  DIMDNSUNDEF = 0x1;	/* DIM_DNS_NODE undefined			FATAL */
	static final int  DIMDNSREFUS = 0x2;	/* DIM_DNS refuses connection		FATAL */
	static final int  DIMDNSDUPLC = 0x3;	/* Service already exists in DNS	FATAL */
	static final int  DIMDNSEXIT  = 0x4;	/* DNS requests server to EXIT		FATAL */
	static final int  DIMDNSTMOUT = 0x5;	/* Server failed sending Watchdog	WARNING */

	static final int  DIMSVCDUPLC = 0x10;	/* Service already exists in Server	ERROR */
	static final int  DIMSVCFORMT = 0x11;	/* Bat format string for service	ERROR */
	static final int  DIMSVCINVAL = 0x12;	/* Service ID invalid				ERROR */
	static final int  DIMSVCTOOLG = 0x13;	/* Service name too long			ERROR */

	static final int  DIMTCPRDERR = 0x20;	/* TCP/IP read error				ERROR */
	static final int  DIMTCPWRRTY = 0x21;	/* TCP/IP write	error - Retrying	WARNING */
	static final int  DIMTCPWRTMO = 0x22;	/* TCP/IP write error - Disconnect	ERROR */
	static final int  DIMTCPLNERR = 0x23;	/* TCP/IP listen error				ERROR */
	static final int  DIMTCPOPERR = 0x24;	/* TCP/IP open server error			ERROR */
	static final int  DIMTCPCNERR = 0x25;	/* TCP/IP connection error			ERROR */
	static final int  DIMTCPCNEST = 0x26;	/* TCP/IP connection established	INFO */

	static final int  DIMDNSCNERR = 0x30;	/* Connection to DNS failed			ERROR */
	static final int  DIMDNSCNEST = 0x31;	/* Connection to DNS established	INFO */
	
	static private String[] dll_locations = null;

    /**
     * Modifies the Default Search Path for the shareable native library.
     * @param theLocations A vector of Strings each one containing a fully qualified path name from where the
     * system should attempt to load the shareable library.
     * @throws IncorrectUsageException This exception is thrown when this method is invoked after the loading of the library.
     */
    static public void setDllSearchPath(String[] theLocations) throws IncorrectUsageException
    {
        if (dim_version!=0) (new dim.IncorrectUsageException("The native library has been loaded already. DllSearchPath is ignored.")).report();
        dll_locations=theLocations;
        return;
    }

    /**
     * Internal method to retrieve the DllSearchPath array. See also the general description.
     */
    static private String[] getDllSearchPath()
    {
        if(dll_locations!=null) return dll_locations; // de klant is de koning

        Vector results = new Vector();
		String[] resArray;
        final String className = "dim/Native.class";   // for jar test use "java/lang/Object.class";
/*
        String property = System.getProperty("dim.dll.path");
        if(property!=null) results.addElement(property);

        URL url = ClassLoader.getSystemResource(className);
        if(url!=null)
        {
            String result = url.getFile();
            int lastIndex=-1;
            int firstIndex=0;
            if(url.getProtocol().equals("jar"))
            {
                if(result.startsWith("file:")) firstIndex=5;
                lastIndex = result.indexOf("!");
                lastIndex = result.lastIndexOf("/",lastIndex)+1;
            }
            else if(url.getProtocol().equals("file"))
            {
                lastIndex = result.length() - className.length();
            }
            if(lastIndex > 0)
            {
                int rootIndex=-1;
                if     (result.regionMatches(true, lastIndex-9, "/classes/", 0, 9)) rootIndex = lastIndex-8;
                else if(result.regionMatches(true, lastIndex-5, "/lib/",     0, 5)) rootIndex = lastIndex-4;
                if(rootIndex!=-1) results.addElement(result.substring(firstIndex,rootIndex)+"bin/dim.dll");
                results.addElement(result.substring(firstIndex,lastIndex)+"dim.dll");
            }
        }
*/
		results.addElement("jdim");
//		results.addElement("D:\\dim\\bin\\jdim.dll");
/*
        results.addElement("libdim.so");
        results.addElement("//Srv2_home/DIV_SL/Co/SPS2001/MACSy/implementation/shared_libraries/dimJNI_v2.dll");
*/
//        return (String[]) results.toArray(new String[results.size()]);
		resArray = new String[results.size()];
		results.copyInto(resArray);
		return resArray;
    }

    /**
     * Loads the shareable library. This procedure will search a predefined set of file path.
     */
    static int loadNativeLibrary()// This static code will load the native dim library.
    {
      // return if we are already loaded

      if(dim_version!=0) return dim_version;
      dim_version = -1;
      // the JNI needs information from some other classes and hence those are loaded as well.

      Error loadError=null;
      String[] loadPaths = getDllSearchPath();
      for(int i=0; i < loadPaths.length; i++)
      {
        try
        {
          System.out.println("Tring to load DIM from " + loadPaths[i] );
          System.loadLibrary(loadPaths[i]);
//		  System.load(loadPaths[i]);
//         System.out.println("Loaded jdim.dll from " + loadPaths[i] );
          loadError = null;
          break;
        }
        catch (Error e)
        {
          loadError=e;
        }
      }
      // if there is still an error, throw it.
      if(loadError!=null) throw(loadError);
      dim_version = init();	// in case we use JNI_version < 1.2
      if (dim_version==0) dim_version = -1;

      return dim_version;
    }

    /**
     * Initialize the dim native library;
     * @return The JNI version.
     */
    static private native int init();

	static native int stop();
}
