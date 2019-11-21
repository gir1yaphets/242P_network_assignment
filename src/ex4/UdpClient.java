package ex4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

public class UdpClient {
    public static void main(String[] args) {
        UdpClient client = new UdpClient();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String cmd = scanner.nextLine();
            client.createClient(cmd);
        }
    }

    private static final String HOST_NAME = "127.0.0.1";
    private static final int PORT = 8080;

    private DatagramSocket datagramSocket;

    public void createClient(String request) {
        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(15000);

            sendRequest(request);
            proceedResponse();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }


    private void proceedResponse() {
        byte[] responseBuff = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(responseBuff, responseBuff.length);

        try {
            datagramSocket.receive(responsePacket);
            String response = new String(responsePacket.getData(), 0 , responsePacket.getLength());
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(String request) {
        byte[] sendBuff = request.getBytes();
        InetAddress address;
        DatagramPacket datagramPacket;
        try {
            address = InetAddress.getByName(HOST_NAME);
            datagramPacket = new DatagramPacket(sendBuff, sendBuff.length, address, PORT);
            datagramSocket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
