package unisinos.java.sniffer.broadcast;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import unisinos.java.sniffer.constants.SnifferConstants;

public class BroadcastGenericServer implements BroadcastServer, BroadcastConstants {
    
    private final DatagramSocket serverSocket;
    private final Set<SocketAddress> listeners;
    private InputStream inputStream;

    public BroadcastGenericServer(int serverPort) throws SocketException {
        this.listeners = new HashSet<>();
        serverSocket = new DatagramSocket(serverPort);
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
            startInputStreamHandler();
            byte[] receiveData = new byte[SnifferConstants.MAX_BUFFER_SIZE];
            while (true) {
                Arrays.fill(receiveData, (byte) 0);
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    serverSocket.receive(receivePacket);
                    String action = new String(receivePacket.getData()).trim();
                    SocketAddress listenerAddr = receivePacket.getSocketAddress();
                    if (action.equals(ACTION_ADD_LISTENER)) {
                        if (listeners.add(listenerAddr)) {
                            onListenerConnection(listenerAddr);
                        }
                    } else if (action.equals(ACTION_REMOVE_LISTENER)) {
                        if (listeners.remove(listenerAddr)) {
                            onListenerRemove(listenerAddr);
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(BroadcastGenericServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        serverThread.start();
        return serverThread;
    }
    
    /**
     * Callback called when a new listener connects
     *
     * @param listenerAddr Listener address
     * @throws IOException
     */
    protected void onListenerConnection(SocketAddress listenerAddr) throws IOException {
        System.out.println("New listener [" + listenerAddr + "]");
    }

    /**
     * Callback called when a new listener connects
     *
     * @param listenerAddr Listener address
     * @throws IOException
     */
    protected void onListenerRemove(SocketAddress listenerAddr) throws IOException {
        System.out.println("Listener removed [" + listenerAddr + "]");
    }
    
    /**
     * Send a packet to all listening addresses
     *
     * @param data Data that will be sent to listeners
     * @param len Data length
     * @throws IOException
     */
    @Override
    public void send(byte[] data, int len) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, len);
        for (SocketAddress listener: listeners) {
            packet.setSocketAddress(listener);
            serverSocket.send(packet);
        }
    }

    /**
     * Send a packet to a specific listener
     *
     * @param data Data that will be sent
     * @param len Data length
     * @param listenerAddr Listener
     * @throws IOException
     */
    @Override
    public void send(byte[] data, int len, SocketAddress listenerAddr) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, len, listenerAddr);
        serverSocket.send(packet);
    }
    
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    
    private void startInputStreamHandler() {
        new Thread(() -> {
            byte[] data = new byte[SnifferConstants.MAX_BUFFER_SIZE];
            while(true) {
                try {
                    if(Objects.nonNull(inputStream) && inputStream.available() > 0) {
                        int dataLength = inputStream.read(data);
                        send(data, dataLength);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(BroadcastGenericServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    @Override
    public void close() throws IOException {
        // TODO
    }

}
