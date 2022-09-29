package unisinos.sniffer.broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import unisinos.sniffer.SnifferConstants;

public class BroadcastUdpServer implements BroadcastServer, BroadcastConstants {
    
    private final DatagramSocket serverSocket;
    private final Set<SocketAddress> listeners;

    public BroadcastUdpServer(int serverPort) throws SocketException {
        this.serverSocket = new DatagramSocket(serverPort);
        this.listeners = new HashSet<>();
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
            byte[] receiveData = new byte[SnifferConstants.MAX_BUFFER_SIZE];
            while (true) {
                Arrays.fill(receiveData, (byte) 0); // Clear the buffer
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    serverSocket.receive(receivePacket);
                    SocketAddress listenerAddr = receivePacket.getSocketAddress();
                    String action = new String(receivePacket.getData()).trim();
                    switch (action) {
                        case ACTION_ADD_LISTENER:
                            if (listeners.add(listenerAddr))
                                System.out.println("New listener [" + listenerAddr + "]");
                            break;
                        case ACTION_REMOVE_LISTENER:
                            if (listeners.remove(listenerAddr))
                                System.out.println("Listener removed [" + listenerAddr + "]");
                            break;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(BroadcastUdpServer.class.getName()).log(Level.SEVERE, null, ex);
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
        DatagramPacket packet = new DatagramPacket(data, len);
        for (SocketAddress listener: listeners) {
            packet.setSocketAddress(listener);
            try {
                serverSocket.send(packet);
            } catch (IOException ex) {
                System.out.println("Packet with size " + len + " not sent:");
                System.out.println(ex.getMessage());
            }
        }
    }

    @Override
    public void close() throws IOException {
        // TODO
    }

}
