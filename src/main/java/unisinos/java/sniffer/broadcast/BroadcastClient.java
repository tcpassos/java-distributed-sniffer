package unisinos.java.sniffer.broadcast;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import unisinos.java.sniffer.constants.SnifferConstants;

public class BroadcastClient {
    
    private final DatagramSocket clientSocket;
    private final PipedOutputStream outputStream;
    private final PipedInputStream inputStream;

    public BroadcastClient() throws IOException {
        this.clientSocket = new DatagramSocket();
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
    public void addHost(InetAddress host, int port) throws IOException {
        byte[] sendData = "add".getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, host, port);
        clientSocket.send(sendPacket);
    }

    /**
     * Start listening to the broadcast
     *
     * @return {@code Thread}
     * @throws IOException
     */
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
                    Logger.getLogger(BroadcastClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        clientThread.start();
        return clientThread;
    }
    
    public InputStream getInputStream() {
        return inputStream;
    }

}
