package unisinos.java.sniffer.broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BroadcastClient {
    
    private final DatagramSocket clientSocket;

    public BroadcastClient() throws SocketException {
        this.clientSocket = new DatagramSocket();
    }
    
    /**
     * Add a host to receive messages from
     * @param host Host
     * @param port Port
     * @throws IOException
     */
    public void addHost(InetAddress host, int port) throws IOException {
        byte[] sendData = "add".getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, host, port);
        clientSocket.send(sendPacket);
    }

    /**
     * Start listening to the broadcast
     *
     * @param packetConsumer Consumer that will handle packets received from broadcast hosts
     * @return {@code Thread}
     * @throws IOException
     */
    public Thread listen(Consumer<DatagramPacket> packetConsumer) throws IOException {
        Thread clientThread = new Thread(() -> {
            byte[] receiveData = new byte[1024];
            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    clientSocket.receive(receivePacket);
                    packetConsumer.accept(receivePacket);
                } catch (IOException ex) {
                    Logger.getLogger(BroadcastClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        clientThread.start();
        return clientThread;
    }

}
