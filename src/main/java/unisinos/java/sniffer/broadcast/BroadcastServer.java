package unisinos.java.sniffer.broadcast;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import unisinos.java.sniffer.constants.SnifferConstants;

public class BroadcastServer {
    
    private final DatagramSocket serverSocket;
    private final Set<SocketAddress> listeners;
    private InputStream inputStream;

    public BroadcastServer(int serverPort) throws SocketException {
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
            startInputStreamHandler();
            byte[] receiveData = new byte[SnifferConstants.MAX_BUFFER_SIZE];
            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    serverSocket.receive(receivePacket);
                    if (listeners.add(receivePacket.getSocketAddress())) {
                        System.out.println("New listener [" + receivePacket.getAddress() + ":" + receivePacket.getPort() + "]");
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
     * @param len Data length
     * @throws IOException
     */
    public void send(byte[] data, int len) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, len);
        for (SocketAddress listener: listeners) {
            packet.setSocketAddress(listener);
            serverSocket.send(packet);
        }
    }
    
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    
    private void startInputStreamHandler() {
        new Thread(() -> {
            byte[] data = new byte[SnifferConstants.MAX_BUFFER_SIZE];
            while(true) {
                try {
                    if(Objects.nonNull(inputStream) && inputStream.available() > 100) {
                        int dataLength = inputStream.read(data);
                        send(data, dataLength);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(BroadcastServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

}
