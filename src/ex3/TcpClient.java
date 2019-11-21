package ex3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TcpClient {
    private static final String HOST_NAME = "127.0.0.1";
    private static final int PORT = 8888;

    public static void main(String[] args) {
        Socket socket;
        TcpClient client = new TcpClient();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Please input a command :");


        while (true) {
            String cmd = scanner.nextLine();

            try {
                socket = new Socket(HOST_NAME, PORT);
                socket.setSoTimeout(15000);

                //send request to server
                client.sendRequest(socket, cmd);

                //proceed response from server
                client.proceedResponse(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void proceedResponse(Socket socket) {
        InputStream in;
        try {
            in = socket.getInputStream();
            StringBuilder sb = new StringBuilder();
            String line;

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            System.out.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(Socket socket, String request) {
        PrintWriter printWriter;
        try {
            printWriter = new PrintWriter(socket.getOutputStream());
            printWriter.println(request);
            printWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
