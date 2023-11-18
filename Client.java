import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int  SERVER_PORT = 4444;
    private  SocketChannel socketChannel;
    private ByteBuffer buffer;
    private String gameType;
    private Viewer viewer;


    public Client(Viewer viewer, String gameType) {
        this.viewer = viewer;
        this.gameType = gameType;
        buffer = ByteBuffer.allocate(1024);
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT));
            System.out.println("Successfully connected to server ...");
            sendDataToServer(gameType);
            String serverResponse = getDataFromServer();
            System.out.println(serverResponse);

        } catch (IOException e) {
            System.out.println("ERROR occurred while trying to make connection");
        }
    }

    public String getGameType() {
        return gameType;
    }
    public boolean hasConnectionToServer() {
        return socketChannel.isOpen();
    }
    public void closeClient() {
        if (socketChannel != null && socketChannel.isOpen()) {
            try {
                socketChannel.close();
                System.out.println("Close client Socket");
            } catch (IOException exc) {
                System.out.println(exc);
            }

        }
    }
    public  String loadLevelFromServer(String level) {
        String levelContent = null;
        if(socketChannel != null) {
            sendDataToServer(level);
            levelContent = getDataFromServer();
        }
        return levelContent;
    }

    public  String loadEnemyLevelFromServer() {
        String levelContent = null;
        if(socketChannel != null) {
            levelContent = getDataFromServer();
        }
        return levelContent;
    }

    public  void sendDataToServer(String data) {
        while(true) {
            try {
                buffer.clear();
                buffer.put(data.getBytes());
                buffer.flip();

                socketChannel.write(buffer);
                buffer.clear();
                System.out.println("Successfully send data to server [~]");
                break;

            } catch(IOException exc) {
                System.out.println("exception in method sendDataToServer " + exc);
                exc.printStackTrace();
                System.out.println("has connection to server = " + hasConnectionToServer());
                viewer.showMenu();
                break;

            }
        }
    }

    public  String getDataFromServer() {
        String data = null;
        while(true) {
            try {
                buffer.clear();
                int bytesRead = socketChannel.read(buffer);
                if (bytesRead > 0) {
                    buffer.flip();
                     data = new String(buffer.array(), 0, bytesRead);
                }
                buffer.clear();
                System.out.println("Successfully get data from server [~]");
                break;

            } catch(IOException exc) {
                System.out.println("exception in method getDataFromServer " + exc);
                exc.printStackTrace();
                System.out.println("has connection to server = " + hasConnectionToServer());
                viewer.showMenu();
                break;

            }
        }
        return data;
    }




}
