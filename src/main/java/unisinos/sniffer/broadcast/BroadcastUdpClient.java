package unisinos.sniffer.broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import unisinos.sniffer.handler.DatagramPacketHandler;

public class BroadcastUdpClient implements BroadcastClient, BroadcastConstants {
    
    private final DatagramSocket clientSocket;
    private final Set<InetSocketAddress> hosts;
    private Consumer<DatagramPacket> onPacketReceivedConsumer;

    public BroadcastUdpClient() throws IOException {
        this.clientSocket = new DatagramSocket();
        this.hosts = new HashSet<>();
        this.onPacketReceivedConsumer = (packet) -> System.out.println("Received a packet from " + packet.getAddress());
    }

    /**
     * Start listening to the broadcast
     *
     * @return {@code Thread}
     * @throws IOException
     */
    @Override
    public Thread start() throws IOException {
        return new DatagramPacketHandler(clientSocket, onPacketReceivedConsumer).handle();
    }
    
    /**
     * Add a host to receive messages from
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
     * @param onPacketReceivedConsumer Handler
     */
    public void onPacketReceived(Consumer<DatagramPacket> onPacketReceivedConsumer) {
        this.onPacketReceivedConsumer = onPacketReceivedConsumer;
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
