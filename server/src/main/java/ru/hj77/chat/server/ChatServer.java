package ru.hj77.chat.server;

import lombok.extern.log4j.Log4j2;
import ru.hj77.network.TCPConnection;
import ru.hj77.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;


public class ChatServer implements TCPConnectionListener {

    public static void main(String[] args) {
        new ChatServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    private ChatServer() {
        System.out.println("Server running...");
        try(ServerSocket serverSocket = new ServerSocket(8888)){
            while(true) {
                try{
                    new TCPConnection(serverSocket.accept(), this);
                }
                catch (IOException exception){
                    System.out.println("TCPConnection exception " + exception);
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        sendToAllConnections("Client connected: " + tcpConnection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String message) {
        sendToAllConnections(message);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        sendToAllConnections("Client disconnected: " + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception " + e);
    }

    private void sendToAllConnections(String value) {

        System.out.println(value);

        final int cnt = connections.size();
        for (int i = 0; i < cnt; i++) {
            connections.get(i).sendMessage(value);
        }
    }
}
