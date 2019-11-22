package ex3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {
    private static final int PORT = 8888;

    private static final String COMMAND_INDEX = "index";
    private static final String RESPONSE_OK = "OK";
    private static final String RESPONSE_ERROR = "ERROR";

    private static String serverPath;

    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            serverPath = args[0];
        }

        ServerSocket serverSocket;
        TcpServer server = new TcpServer();

        try {
            serverSocket = new ServerSocket(PORT);
            while (true) {
                Socket client = serverSocket.accept();

                System.out.println("client:" + client.getInetAddress().getLocalHost() + " has connected!");

                //proceed message from client
                server.proceedRequest(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void proceedRequest(Socket client) {
        String request = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            request = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Server received command : " + request);

        if (COMMAND_INDEX.equals(request)) {
            File dir = new File(serverPath);

            if (dir.exists()) {
                StringBuilder stringBuilder = new StringBuilder();
                for (String file : dir.list()) {
                    stringBuilder.append(file).append(" ");
                }

                sendResponse(client, stringBuilder.toString());
            }
        } else {
            String fileName = serverPath + request;
            File file = new File(fileName);

            if (file.exists()) {
                try {
                    BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
                    sendResponse(client, fileReader);
                } catch (FileNotFoundException e) {
                    sendResponse(client, RESPONSE_ERROR);
                }
            } else {
                sendResponse(client, RESPONSE_ERROR);
            }
        }
    }

    private void sendResponse(Socket client, String result) {
        PrintWriter printWriter;

        try {
            printWriter = new PrintWriter(client.getOutputStream());
            printWriter.println(result);
            printWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendResponse(Socket client, BufferedReader reader) {
        PrintWriter printWriter;

        try {
            printWriter = new PrintWriter(client.getOutputStream());

            printWriter.println(RESPONSE_OK + "\r\n");
            String line;

            while ((line = reader.readLine()) != null) {
                printWriter.println(line);
                printWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
