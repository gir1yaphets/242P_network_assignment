package ex4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpServer {
    private DatagramSocket datagramSocket;

    private static final int PORT = 8080;
    private static final int MAX_LENGTH = 1024;

    private static final String COMMAND_INDEX = "index";
    private static final String RESPONSE_OK = "OK";
    private static final String RESPONSE_ERROR = "ERROR";

    private static final String SERVER_PATH = "src/ex3";

    public static void main(String[] args) {
        UdpServer udpServer = new UdpServer();
        udpServer.serverStart();
    }

    public void serverStart() {
        try {
            datagramSocket = new DatagramSocket(PORT);
            byte[] buffer = new byte[MAX_LENGTH];

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                receiveRequest(packet);

                proceedRequest(packet);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void proceedRequest(DatagramPacket packet) {
        String respond = "";
        String request = new String(packet.getData(), 0 , packet.getLength());

        if (COMMAND_INDEX.equals(request)) {
            File dir = new File(SERVER_PATH);

            if (dir.exists()) {
                StringBuilder stringBuilder = new StringBuilder();

                for (String file : dir.list()) {
                    stringBuilder.append(file).append(" ");
                }

                respond = stringBuilder.toString();
            }
        } else {
            String fileName = "./" + request;
            File file = new File(fileName);

            if (file.exists()) {
                StringBuilder sb = new StringBuilder();
                sb.append(RESPONSE_OK).append("\n");
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(fileName));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }

                    respond = sb.toString();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    respond = RESPONSE_ERROR;
                } catch (IOException e) {
                    e.printStackTrace();
                    respond = RESPONSE_ERROR;
                }
            } else {
                respond = RESPONSE_ERROR;
            }
        }

        packet.setData(respond.getBytes());
        packetData(respond);
        sendResponse(packet);
    }


    private void receiveRequest(DatagramPacket packet) {
        try {
            datagramSocket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendResponse(DatagramPacket packet) {
        try {
            datagramSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] packetData(String data) {
        byte[] before = data.getBytes();

        int originalPacket = getPacketNum(before.length);

        int finalLength = before.length + originalPacket * 8;
        int finalPacket = getPacketNum(finalLength);

        byte[] after = new byte[finalPacket];

        int len = 0, index = 0;
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : data.toCharArray()) {
            if (len == 0) {
                stringBuilder.append(finalPacket);
                stringBuilder.append(index);
                stringBuilder.append(c);
                len += 10;
            } else if (len ==  MAX_LENGTH) {
                index++;
                len = 0;
            } else {
                stringBuilder.append(c);
                len += 2;
            }
        }

        System.out.println(stringBuilder.toString());

        return after;
    }

    private int getPacketNum(int length) {
        return (length % MAX_LENGTH == 0) ? length / MAX_LENGTH : length / MAX_LENGTH + 1;
    }
}
