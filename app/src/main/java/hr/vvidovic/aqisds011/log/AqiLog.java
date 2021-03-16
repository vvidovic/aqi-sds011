package hr.vvidovic.aqisds011.log;

import android.util.Log;

public class AqiLog {
    // Disable all logging by setting this to Log.ASSERT.
    private static final int AQI_PRIORITY = Log.VERBOSE;

    public static void d(String tag, String msgFormat, Object ...msgParams) {
        log(Log.DEBUG, tag, msgFormat, msgParams);
    }
    public static void i(String tag, String msgFormat, Object ...msgParams) {
        log(Log.INFO, tag, msgFormat, msgParams);
    }
    public static void w(String tag, String msgFormat, Object ...msgParams) {
        log(Log.WARN, tag, msgFormat, msgParams);
    }
    public static void e(String tag, String msgFormat, Object ...msgParams) {
        log(Log.ERROR, tag, msgFormat, msgParams);
    }

    private static void log(int priority, String tag, String msgFormat, Object ...msgParams) {
        if(priority >= AQI_PRIORITY) {
            String msg = String.format(msgFormat, msgParams);
            Log.println(priority, tag, msg);
        }
    }
}
