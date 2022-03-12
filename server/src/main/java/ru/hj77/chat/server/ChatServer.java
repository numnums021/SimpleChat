package ru.hj77.chat.server;

import ru.hj77.network.TCPConnection;
import ru.hj77.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import lombok.extern.java.Log;

@Log
public class ChatServer implements TCPConnectionListener {

    public static void main(String[] args) {
        new ChatServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();


    private ChatServer() {
        log.info("Server running...");
        try(ServerSocket serverSocket = new ServerSocket(8888)){
            while(true) {
                try{
                    new TCPConnection(serverSocket.accept(), this);
                }
                catch (IOException exception){
                    log.info("TCPConnection exception " + exception);
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
        log.info("TCPConnection exception " + e);
    }

    private void sendToAllConnections(String value) {
        log.info(value);

        final int cnt = connections.size();
        for (TCPConnection connection : connections) {
            connection.sendMessage(value);
        }
    }
}
