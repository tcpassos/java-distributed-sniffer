package unisinos.sniffer.broadcast.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import unisinos.sniffer.SnifferConstants;
import unisinos.sniffer.broadcast.BroadcastClient;
import unisinos.sniffer.broadcast.BroadcastConstants;
import unisinos.sniffer.buffer.BufferHandler;

public class BroadcastUdpClient implements BroadcastClient, BroadcastConstants {
    
    private final DatagramSocket clientSocket;
    private final Set<InetSocketAddress> hosts;
    private BufferHandler onPacketReceived;

    public BroadcastUdpClient() throws IOException {
        this.clientSocket = new DatagramSocket();
        this.hosts = new HashSet<>();
        this.onPacketReceived = (packetBytes) -> System.out.println("Received " + packetBytes.length + " bytes");
    }

    /**
     * Start listening to the broadcast
     *
     * @return {@code Thread}
     * @throws IOException
     */
    @Override
    public Thread start() throws IOException {
        Thread clientThread = new Thread(() -> {
            byte[] receivedData = new byte[SnifferConstants.MAX_BUFFER_SIZE];
            while (true) {
                Arrays.fill(receivedData, (byte) 0); // Clear the buffer
                DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);
                try {
                    clientSocket.receive(receivePacket);
                    // Creates a copy of the received packet data because the returned array will always have a fixed value
                    byte[] data = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                    onPacketReceived.handle(data);
                } catch (IOException ex) {
                    Logger.getLogger(BroadcastUdpClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        clientThread.start();
        return clientThread;
    }
    
    /**
     * Add a host to receive messages from
     *
     * @param host Host
     * @param port Port
     * @throws IOException
     */
    @Override
    public void addHost(InetAddress host, int port) throws IOException {
        byte[] sendData = ACTION_ADD_LISTENER.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, host, port);
        clientSocket.send(sendPacket);
        hosts.add(new InetSocketAddress(host, port));
    }

    /**
     * Sets the handler that will process incoming packets
     *
     * @param handler Handler
     */
    @Override
    public void onDataReceived(BufferHandler handler) {
        this.onPacketReceived = handler;
    }

    /**
     * Send a message to all connected hosts to remove this listener
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        try (clientSocket) {
            byte[] sendData = ACTION_REMOVE_LISTENER.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);
            for (InetSocketAddress host: hosts) {
                sendPacket.setAddress(host.getAddress());
                sendPacket.setPort(host.getPort());
                clientSocket.send(sendPacket);
            }
        }
    }

}
