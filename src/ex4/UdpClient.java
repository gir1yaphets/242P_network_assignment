package ex4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

import static ex4.UdpServer.COMMAND_ACK;
import static ex4.UdpServer.COMMAND_INDEX;
import static ex4.UdpServer.RESPONSE_ERROR;
import static ex4.UdpServer.RESPONSE_OK;

public class UdpClient {
    public static void main(String[] args) {
        UdpClient client = new UdpClient();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please input a command :");

        while (true) {
            String cmd = scanner.nextLine();
            client.createClient(cmd);
        }
    }

    private static final String HOST_NAME = "127.0.0.1";
    private static final int PORT = 8080;

    private String command;
    private int index;

    private StringBuilder stringBuilder = new StringBuilder();
    private DatagramSocket datagramSocket;

    private static final int RETRY_TIMES = 3;
    private int retry = 0;

    public void createClient(String request) {
        command = request;
        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(15000);

            sendRequest(request);
            while (true) {
                if (!proceedResponse()) {
                    break;
                }

            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }


    private boolean proceedResponse() {
        byte[] responseBuff = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(responseBuff, responseBuff.length);

        try {
            datagramSocket.receive(responsePacket);
            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());

            if (command.equals(COMMAND_INDEX)) {
                System.out.println("response = " + response);
                return false;
            }

            if (response.startsWith(UdpServer.COMMAND_SYNC)) {
                String[] body = response.split(",");
                index = Integer.valueOf(body[1]);

                if (index == -1) {
                    stringBuilder = new StringBuilder();
                    sendAck(index);
                }
            } else if (response.equals(RESPONSE_OK)) {
                System.out.println("OK!");
            } else if (response.equals(RESPONSE_ERROR)) {
                System.out.println("Sorry, the file you input does not exist!");
                return false;
            } else {
                stringBuilder.append(response);

                if (index == Integer.MIN_VALUE) {
                    System.out.println("final result = " + stringBuilder.toString());
                    return false;
                }

                sendAck(index);
            }
        } catch (IOException e) {
            if (retry++ < RETRY_TIMES) {
                System.out.println("retry " + retry + " time" + ", command is " + command);
                sendRequest(command);
                return true;
            } else {
                System.out.println("Sorry, time out!");
                return false;
            }
        }

        return true;
    }

    private void sendAck(int index) {
        String request = COMMAND_ACK + "," + index;
        sendRequest(request);
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
