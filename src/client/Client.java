package client;


import client.view.MainWindow;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {


    private  int PORT = 8198;
    private  String IP = "192.186.0.0";
    private Socket socket = null;

    public Client() {
    }


    public void startClient() {
        Scanner input;
        PrintWriter output;
        MainWindow window = new MainWindow();

        while (!window.hasUserSetAddress()){

            System.out.println("waiting user input");
        }
        IP = window.getIP();
        PORT = Integer.parseInt(window.getPORT());


        try {
            socket = new Socket(IP, PORT);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            input = new Scanner(inputStream);
            output = new PrintWriter(outputStream, true);

            output.println("_get");

            String msg = "";
            while(input.hasNextLine()){
                msg += input.nextLine();
                //System.out.println(msg);
            }
            window.setPageContent(msg);
            window.showPageContent();

        } catch (IOException e) {
            Logger.d("err", "erro");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                Logger.d("erro", "erro");
            }
        }
    }
}



