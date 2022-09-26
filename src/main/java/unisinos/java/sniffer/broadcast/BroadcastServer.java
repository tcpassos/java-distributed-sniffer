package unisinos.java.sniffer.broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BroadcastServer {
    
    private final DatagramSocket serverSocket;
    private final Set<SocketAddress> listeners;
    private final int serverPort;

    public BroadcastServer(int serverPort) throws SocketException {
        this.serverPort = serverPort;
        this.listeners = new HashSet<>();
        serverSocket = new DatagramSocket(serverPort);
    }

    /**
     * Starts a thread that will wait for listeners that will be used for data broadcast
     *
     * @return {@code Thread}
     * @throws IOException
     */
    public Thread start() throws IOException {
        Thread serverThread = new Thread(() -> {
            System.out.println("Escutando na porta " + serverPort);
            byte[] receiveData = new byte[1024];
            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    serverSocket.receive(receivePacket);
                    if (listeners.add(receivePacket.getSocketAddress())) {
                        System.out.println("Listener adicionado [" + receivePacket.getAddress() + ":" + receivePacket.getPort() + "]");
                    }
                } catch (IOException ex) {
                    Logger.getLogger(BroadcastServer.class.getName()).log(Level.SEVERE, null, ex);
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
     * @throws IOException
     */
    public void send(byte[] data) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, data.length);
        for (SocketAddress listener: listeners) {
            packet.setSocketAddress(listener);
            serverSocket.send(packet);
        }
    }

}
