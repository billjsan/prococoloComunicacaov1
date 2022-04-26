package client;

public class Logger {
    public static final boolean  ISLOGABLE = true;
    private static final String APPNAME = "STTP v1 client";

    public static void d(String TAG, String msg){
        System.out.println("[" + APPNAME + "]" + " ["+ TAG+"] " + msg);
    }
}
