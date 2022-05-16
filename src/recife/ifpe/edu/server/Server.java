package recife.ifpe.edu.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    public final int PORT;
    static List<Socket> clients = new ArrayList<>();

    public Server(int port) {
        this.PORT = port;
    }

    int clientsCount = 1;
    public void startServer() {

        try {
            ServerSocket socket = new ServerSocket(PORT);

            while (clientsCount <= 1000) {
                Socket incoming = socket.accept();
                System.out.println("Spawning " + clientsCount);

                clients.add(incoming);
                new EcoHandler(incoming, clientsCount, clients).start();
                clientsCount++;
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
