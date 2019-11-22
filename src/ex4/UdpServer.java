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

    public static final String COMMAND_INDEX = "index";
    public static final String COMMAND_ACK = "ack";
    public static final String COMMAND_SYNC = "sync";
    private static final String RESPONSE_OK = "OK";
    private static final String RESPONSE_ERROR = "ERROR";

    private static String serverPath;
    private byte[] data;
    private int index = 0;
    private int packetNum = 0;

    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            serverPath = args[0];
        }

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
        String respond;
        String request = new String(packet.getData(), 0 , packet.getLength());

        System.out.println("request = " + request);

        if (COMMAND_INDEX.equals(request)) {
            File dir = new File(serverPath);

            if (dir.exists()) {
                StringBuilder stringBuilder = new StringBuilder();

                for (String file : dir.list()) {
                    stringBuilder.append(file).append(" ");
                }

                respond = stringBuilder.toString();
                packet.setData(respond.getBytes());
                sendResponse(packet);
            }
        } else if (request.startsWith(COMMAND_ACK)) {
            String[] ack = request.split(",");
            int requestIndex = Integer.valueOf(ack[1]);

            System.out.println("ACK index = " + index);
            if (requestIndex == index) {
                index += 1;

                sendSync(packet);
                sendPacket(packet);
            }
        } else {
            String fileName = serverPath + request;
            File file = new File(fileName);

            if (file.exists() && !file.isDirectory()) {
                StringBuilder sb = new StringBuilder();
                sb.append(RESPONSE_OK).append("\n");
                try {
                    String content = getFileContent(fileName);
                    setDataPacketNum(content);
                    index = -1;
                    sendSync(packet);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void receiveRequest(DatagramPacket packet) {
        try {
            datagramSocket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileContent(String fileName) throws FileNotFoundException, IOException {
        System.out.println("getFileContent fileName = " + fileName);
        StringBuilder sb = new StringBuilder();
        BufferedReader reader;
        String content = "" ;
        try {
            reader = new BufferedReader(new FileReader(fileName));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            content = sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    private void setDataPacketNum(String content) {
        data = content.getBytes();
        packetNum = (data.length % MAX_LENGTH == 0) ? data.length / MAX_LENGTH : data.length / MAX_LENGTH + 1;
    }

    private void sendSync(DatagramPacket packet) {
        String sync;

        if (index == packetNum - 1) {
            sync = COMMAND_SYNC + "," + Integer.MIN_VALUE;
        } else {
            sync = COMMAND_SYNC + "," + index;
        }
        byte[] syncBytes = sync.getBytes();
        packet.setData(syncBytes);
        sendResponse(packet);
    }

    private void sendPacket(DatagramPacket packet) {
        if (index < packetNum && index != Integer.MIN_VALUE) {
            int start = index * MAX_LENGTH;
            byte[] sendBuff;

            int remain = data.length - index * MAX_LENGTH;

            if (remain > MAX_LENGTH) {
                sendBuff = new byte[MAX_LENGTH];
                System.arraycopy(data, start, sendBuff, 0, MAX_LENGTH);
            } else {
                sendBuff = new byte[remain];
                System.arraycopy(data, start, sendBuff, 0, remain);
            }

            packet.setData(sendBuff);
            sendResponse(packet);
        }
    }

    private void sendResponse(DatagramPacket packet) {
        try {
            datagramSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
