package client;


import client.view.ContentWindow;
import client.view.MainWindow;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private String TAG = Client.class.getSimpleName();
    private Socket socket = null;

    public Client() {
    }

    public void startClient() {
        if (MyLogger.ISLOGABLE) MyLogger.d(TAG, "startClient");
        Scanner input;
        PrintWriter output;
        MainWindow window = new MainWindow();

        while (!window.hasUserSetAddress()) {
            if (MyLogger.ISLOGABLE) MyLogger.d(TAG, "waiting user input");
        }

        String IP = window.getIP();
        if (MyLogger.ISLOGABLE) MyLogger.d(TAG, "IP: " + IP);

        int PORT = Integer.parseInt(window.getPORT());
        if (MyLogger.ISLOGABLE) MyLogger.d(TAG, "PORT: " + PORT);

        try {
            socket = new Socket(IP, PORT);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            input = new Scanner(inputStream);
            output = new PrintWriter(outputStream, true);

            output.println("_get");
            if (MyLogger.ISLOGABLE) MyLogger.d(TAG, "requesting server content");

            boolean isReceiving = true;
            StringBuilder receivedText = new StringBuilder();

            while (isReceiving) {
                if (input.hasNextLine()) {
                    receivedText.append(input.nextLine());
                    if (receivedText.toString().contains("<:end>")) {
                        isReceiving = false;
                        if (MyLogger.ISLOGABLE) MyLogger.d(TAG, "stopping content download");
                    }
                    receivedText.replace(receivedText.length() - 6, receivedText.length(), "");
                }
            }

            String[] utilText = receivedText.toString().split("<.start>");

            ContentWindow contentWindow = new ContentWindow(utilText[1]);
            window.close();
            contentWindow.show();

        } catch (IOException e) {
            MyLogger.d(TAG, "erro");
        } finally {
            try {
                socket.close();
                if (MyLogger.ISLOGABLE) MyLogger.d(TAG, "closing connection");
            } catch (IOException e) {
                MyLogger.d(TAG, "erro");
            }
        }
    }
}



