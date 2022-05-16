package recife.ifpe.edu.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class EcoHandler extends Thread {

    private final Socket incoming;
    private final int counter;
    private final List<Socket> clients;

    public EcoHandler(Socket i, int c, List<Socket> clients) {
        this.incoming = i;
        this.counter = c;
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader
                    (new InputStreamReader(incoming.getInputStream()));
            PrintWriter out = new PrintWriter
                    (incoming.getOutputStream(), true);
            out.println("Hello! Enter BYE to exit.");

            boolean done = false;
            while (!done) {
                String clientInput = in.readLine();

                if (clientInput == null) done = true;
                else {
                    for (Socket client : this.clients) {

                        PrintWriter clientOutput = new PrintWriter(client.getOutputStream(), true);
                        clientOutput.println("Message from client: (" + counter + "): " + clientInput);
                    }

                    if (clientInput.trim().equals("BYE"))
                        done = true;
                }
            }
            incoming.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
