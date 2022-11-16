package unisinos.sniffer.broadcast.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import unisinos.sniffer.SnifferConstants;
import unisinos.sniffer.broadcast.BroadcastClient;
import unisinos.sniffer.buffer.BufferHandler;

public class BroadcastTcpClient implements BroadcastClient {
    
    private final List<Socket> hostSockets;
    private BufferHandler onPacketReceived;
    private boolean isClosed;

    public BroadcastTcpClient() {
        this.hostSockets = new ArrayList<>();
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
            while(!isClosed) { }
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
        Socket clientSocket = new Socket(host, port);
        byte[] data = new byte[SnifferConstants.MAX_BUFFER_SIZE];
        InputStream clientInputStream = clientSocket.getInputStream();
        Thread hostThread = new Thread(() -> {
            while (true) {
                try {
                    if (clientInputStream.available() > 0) {
                        int dataLength = clientInputStream.read(data);
                        onPacketReceived.handle(Arrays.copyOf(data, dataLength));
                    }
                } catch (IOException ex) {
                    Logger.getLogger(BroadcastTcpClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        hostThread.start();
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
     * Closes TCP Sockets of all connected hosts
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        for (Socket hostSocket : hostSockets) {
            hostSocket.close();
        }
        isClosed = true;
    }
    
}
