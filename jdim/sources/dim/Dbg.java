package dim;

/**
 * This class defines all methods and options to control the Debug output of
 * the native code. This class cannot be instantiated.
 * <p>
 * <b>Note:</b> the debug messages are written to the native console and  not to the
 * <code>System.out</code> PrintStream. On certain IDE's (e.g. JBuilder) these messages will not be
 * visible on the IDE console.
 * @author M.Jonker Cern
 * @version v1.2
 */
public final class Dbg
{

    static // force loading the native library
    {
      Native.loadNativeLibrary();
    }

    private Dbg(){} // we do not allow instantiation of this class

    /** Activates debug message for dll load and unload. */
    public static final int MODULE            = 0x00000001;
    /** Activates debug message when changing the debug mask. */
    public static final int TRANSACTIONS      = 0x00000002;
    /** Activates send callback tracing */
    public static final int SEND_CALLBACK     = 0x00000004;
    /** Activates sendNative tracing */
    public static final int SEND_NATIVE       = 0x00000008;
    /** Activates sendNativeArray tracing */
    public static final int INFO_CALLBACK     = 0x00000010;
    /** Activates info_service tracing */
    public static final int INFO_SERVICE      = 0x00000020;
    /** Activates server tracing */
    public static final int SERVER            = 0x00000100;
    /** Activates addService service callback tracing */
    public static final int SERVICE_CALLBACK  = 0x00000200;
    /** Activates addService tracing */
    public static final int ADD_SERVICE       = 0x00000400;
    /** Activates releaseService tracing */
    public static final int RELEASE_SERVICE   = 0x00000800;
    /** Activates command callback tracing */
    public static final int CMND_CALLBACK     = 0x00001000;
    /** Activates addCmnd tracing */
    public static final int ADD_CMND          = 0x00002000;
    /** Activates updateService tracing */
    public static final int UPDATE_SERVICE    = 0x00004000;
    /** Activates getClient tracing */
    public static final int GETCLIENT         = 0x00008000;
    /** Activates serializer tracing */
    public static final int SERIALIZER        = 0x00010000;
    /** Activates descriptor handling tracing */
    public static final int DESCRIPTORS       = 0x00020000;
    /** Activates all tracing */
    public static final int FULL              = 0xFFFFFFFF;

    /**
     * Set the debug mask to control the various debug trace modes of the native library.
     * <p>
     * <b>Note:</b> the debug messages are written to the native console and  not to the
     * <code>System.out</code> PrintStream. On certain IDE's (e.g. JBuilder) these messages will
     * not be visible on the IDE console.
     * @param debugMask The debugMask to control the debug trace mode of the native library.
     */
    public static native void setMask(int debugMask);

    /**
     * Get the debug mask which controls the various trace options of the native library.
     * @return The current DebugMask.
     */
    public static native int  getMask();

    /**
     * Switches the selected set of debug options on.
     * @param options The options to be switched on. Any other option is left unaffected;
     * @see #setMask
     */
    public static void setOptions(int options)
    {
        setMask(getMask() | options);
    }

    /**
     * Switches the selected set of debug options off.
     * @param options The options to be switched off. Any other option is left unaffected;
     * @see #setMask
     */
    public static void clrOptions(int options)
    {
        setMask(getMask() & ~options);
    }
}
