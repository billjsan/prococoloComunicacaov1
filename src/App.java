import client.Client;
import client.MyLogger;

public class App {
    private static String TAG = App.class.getSimpleName();

    public static void main(String[] args) {
        if (MyLogger.ISLOGABLE) MyLogger.d(TAG, "starting client program");

        Client client = new Client();
        client.startClient();
    }
}
