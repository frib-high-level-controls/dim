package dim;

public class DimErrorHandler implements ErrorHandler
{
		
	static // force loading the native library
	{
	  Native.loadNativeLibrary();
	}
/*
	public DimErrorHandler()
	{
		DimError.setHandler(this);
		addErrorHandler();		
	}
	public static void addErrorHandler(DimErrorHandler handler)
	{
		DimError.setHandler(handler);
		addErrorHandler();
	}
*/
	public static native void  addSrvErrorHandler();
	public static native void  addCltErrorHandler();

	public static void setSrvHandler(DimErrorHandler handler)
	{
		DimSrvError.setHandler(handler);
		addSrvErrorHandler();		
	}
	
	public static void setCltHandler(DimErrorHandler handler)
	{
		DimCltError.setHandler(handler);
		addCltErrorHandler();		
	}

	public void errorHandler(int severity, int code, String msg) 
	{
	}
	
	private static class DimSrvError implements ErrorHandler
	{
		static DimErrorHandler theErrorHandler;
	
		DimSrvError() {}
		
		public static void setHandler(DimErrorHandler handler)
		{
			theErrorHandler = handler;
		}
	
		public void errorHandler(int severity, int code, String msg) 
		{
//			System.out.println("Server: "+severity);
//			System.out.println("Server: "+msg);
			theErrorHandler.errorHandler(severity, code, msg);
		};
	}
	private static class DimCltError implements ErrorHandler
	{
		static DimErrorHandler theErrorHandler;
	
		DimCltError() {}
		
		public static void setHandler(DimErrorHandler handler)
		{
			theErrorHandler = handler;
		}
	
		public void errorHandler(int severity, int code, String msg) 
		{
//			System.out.println("Server: "+severity);
//			System.out.println("Server: "+msg);
			theErrorHandler.errorHandler(severity, code, msg);
		};
	}
}

interface ErrorHandler
{
	// DIM Error Severities
	public static final int  DIM_INFO 	= Native.DIM_INFO;
	public static final int  DIM_WARNING = Native.DIM_WARNING;
	public static final int  DIM_ERROR 	= Native.DIM_ERROR;
	public static final int  DIM_FATAL 	= Native.DIM_FATAL;
	// DIM Error codes
	public static final int  DIMDNSUNDEF = Native.DIMDNSUNDEF;	/* DIM_DNS_NODE undefined			FATAL */
	public static final int  DIMDNSREFUS = Native.DIMDNSREFUS;	/* DIM_DNS refuses connection		FATAL */
	public static final int  DIMDNSDUPLC = Native.DIMDNSDUPLC;	/* Service already exists in DNS	FATAL */
	public static final int  DIMDNSEXIT  = Native.DIMDNSEXIT;	/* Server failed sending Watchdog	WARNING */
	public static final int  DIMDNSTMOUT = Native.DIMDNSTMOUT;	/* Server failed sending Watchdog	WARNING */

	public static final int  DIMSVCDUPLC = Native.DIMSVCDUPLC;	/* Service already exists in Server	ERROR */
	public static final int  DIMSVCFORMT = Native.DIMSVCFORMT;	/* Bat format string for service	ERROR */
	public static final int  DIMSVCINVAL = Native.DIMSVCINVAL;	/* Service ID invalid				ERROR */
	public static final int  DIMSVCTOOLG = Native.DIMSVCTOOLG;	/* Service ID invalid				ERROR */

	public static final int  DIMTCPRDERR = Native.DIMTCPRDERR;	/* TCP/IP read error				ERROR */
	public static final int  DIMTCPWRRTY = Native.DIMTCPWRRTY;	/* TCP/IP write	error - Retrying	WARNING */
	public static final int  DIMTCPWRTMO = Native.DIMTCPWRTMO;	/* TCP/IP write error - Disconnect	ERROR */
	public static final int  DIMTCPLNERR = Native.DIMTCPLNERR;	/* TCP/IP listen error				ERROR */
	public static final int  DIMTCPOPERR = Native.DIMTCPOPERR;	/* TCP/IP open server error			ERROR */
	public static final int  DIMTCPCNERR = Native.DIMTCPCNERR;	/* TCP/IP connection error			ERROR */
	public static final int  DIMTCPCNEST = Native.DIMTCPCNEST;	/* TCP/IP connection established	INFO */
	public static final int  DIMDNSCNERR = Native.DIMDNSCNERR;	/* Connection to DNS failed			ERROR */
	public static final int  DIMDNSCNEST = Native.DIMDNSCNEST;	/* Connection to DNS established	INFO */

	void errorHandler(int severity, int code, String msg);
}