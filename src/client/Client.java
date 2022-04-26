package client;

import java.io.IOException;
import java.net.Socket;

public class Client {


    private final int PORT ;
    private final String IP;

    public Client(int PORT, String IP) {
        this.PORT = PORT;
        this.IP = IP;
    }

    public void startClient(){
        try {
            Socket socket = new Socket(IP, PORT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
