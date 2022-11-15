package unisinos.sniffer.broadcast.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import unisinos.sniffer.broadcast.BroadcastServer;

public class BroadcastTcpServer implements BroadcastServer {
    
    private final ServerSocket serverSocket;
    private final List<Socket> clientSockets;

    public BroadcastTcpServer(int serverPort) throws IOException {
        this.serverSocket = new ServerSocket(serverPort);
        this.clientSockets = new ArrayList<>();
    }

    /**
     * Starts a thread that will wait for listeners that will be used for data broadcast
     *
     * @return {@code Thread}
     * @throws IOException
     */
    @Override
    public Thread start() throws IOException {
        Thread serverThread = new Thread(() -> {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSockets.add(clientSocket);
                    System.out.println("New listener [" + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + "]");
                } catch (IOException ex) {
                    Logger.getLogger(BroadcastTcpServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        serverThread.start();
        return serverThread;
    }

    /**
     * Send a packet to all listening addresses
     *
     * @param data Data that will be sent to listeners
     * @param len Data length
     */
    @Override
    public void send(byte[] data, int len) {
        List<Socket> disconnectedClients = new ArrayList<>();
        for (Socket clientSocket : clientSockets) {
            try {
                clientSocket.getOutputStream().write(data, 0, len);
            } catch (IOException ex) {
                System.out.println("Listener disconnected [" + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() +  "]");
                disconnectedClients.add(clientSocket);
            }
        }
        for (Socket disconnectedClient : disconnectedClients) {
            clientSockets.remove(disconnectedClient);
        }
    }

    @Override
    public void close() throws IOException {
        try (serverSocket) {
            for (Socket clientSocket : clientSockets) {
                clientSocket.close();
            }
        }
    }
    
}
