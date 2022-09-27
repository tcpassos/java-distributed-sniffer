package unisinos.java.sniffer.broadcast;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import unisinos.java.sniffer.constants.SnifferConstants;

public class BroadcastGenericClient implements BroadcastClient {
    
    private final DatagramSocket clientSocket;
    private final Set<InetSocketAddress> hosts;
    private final PipedOutputStream outputStream;
    private final PipedInputStream inputStream;

    public BroadcastGenericClient() throws IOException {
        this.clientSocket = new DatagramSocket();
        this.hosts = new HashSet<>();
        this.outputStream = new PipedOutputStream();
        this.inputStream = new PipedInputStream();
        inputStream.connect(outputStream);
    }
    
    /**
     * Add a host to receive messages from
     * @param host Host
     * @param port Port
     * @throws IOException
     */
    @Override
    public void addHost(InetAddress host, int port) throws IOException {
        byte[] sendData = "add".getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, host, port);
        clientSocket.send(sendPacket);
        hosts.add(new InetSocketAddress(host, port));
    }

    /**
     * Remove a host
     * @param host Host
     * @param port Port
     * @throws IOException
     */
    @Override
    public void removeHost(InetAddress host, int port) throws IOException {
        byte[] sendData = "remove".getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, host, port);
        clientSocket.send(sendPacket);
        hosts.remove(new InetSocketAddress(host, port));
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
                Arrays.fill(receivedData, (byte) 0);
                DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);
                try {
                    clientSocket.receive(receivePacket);          
                    outputStream.write(receivedData, receivePacket.getOffset(), receivePacket.getLength());
                } catch (IOException ex) {
                    Logger.getLogger(BroadcastGenericClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        clientThread.start();
        return clientThread;
    }
    
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public void close() throws IOException {
        for (InetSocketAddress host: hosts) {
            removeHost(host.getAddress(), host.getPort());
        }
    }

}
