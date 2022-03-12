package ru.hj77.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPConnection {

    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;
    private final Thread rxThread;
    private final TCPConnectionListener tcpConnectionListener;

    public TCPConnection(TCPConnectionListener tcpConnectionListener, String ip, int port) throws IOException {
        this(new Socket(ip, port), tcpConnectionListener);
    }

    public TCPConnection(Socket socket, TCPConnectionListener tcpConnectionListener) throws IOException {
        this.socket = socket;
        this.tcpConnectionListener = tcpConnectionListener;

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        this.rxThread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            tcpConnectionListener.onConnectionReady(TCPConnection.this);
                            while (!rxThread.isInterrupted()) {
                                tcpConnectionListener.onReceiveString(TCPConnection.this, in.readLine());
                            }

                        } catch (IOException ex) {
                            tcpConnectionListener.onException(TCPConnection.this, ex);
                        } finally {
                            tcpConnectionListener.onDisconnect(TCPConnection.this);
                        }
                    }
                }
        );
        this.rxThread.start();
    }

    public synchronized void sendMessage(String message) {
        try {
            out.write(message + "\r\n");
            out.flush();
        } catch (IOException ex) {
            tcpConnectionListener.onException(TCPConnection.this, ex);
            disconnect();
        }
    }

    public synchronized void disconnect() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException ex) {
            tcpConnectionListener.onException(TCPConnection.this, ex);
        }
    }

    @Override
    public String toString() {
        return "TCPConnection{" +
                "socket=" + socket +
                ", in=" + in +
                ", out=" + out +
                ", rxThread=" + rxThread +
                ", tcpConnectionListener=" + tcpConnectionListener +
                '}';
    }
}
