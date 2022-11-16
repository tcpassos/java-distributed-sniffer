package unisinos.sniffer.broadcast.sctp;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import unisinos.sniffer.broadcast.BroadcastConstants;
import unisinos.sniffer.broadcast.BroadcastServer;

public class BroadcastSctpServer implements BroadcastServer, BroadcastConstants {
    
    private final SctpServerChannel serverChannel;
    private final List<SctpChannel> clientChannels;

    public BroadcastSctpServer(int serverPort) throws IOException {
        this.clientChannels = new ArrayList<>();
        serverChannel = SctpServerChannel.open();
        InetSocketAddress serverAddr = new InetSocketAddress(serverPort);
        serverChannel.bind(serverAddr);
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
            while (true) {
                try {
                    SctpChannel clientChannel = serverChannel.accept();
                    clientChannels.add(clientChannel);
                    System.out.println("New listener " + clientChannel.getRemoteAddresses());
                } catch (IOException ex) {
                    Logger.getLogger(BroadcastSctpServer.class.getName()).log(Level.SEVERE, null, ex);
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
        List<SctpChannel> disconnectedClients = new ArrayList<>();
        for (SctpChannel clientChannel : clientChannels) {
            MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0);
            Set<SocketAddress> remoteAddresses = null;
            try {
                remoteAddresses = clientChannel.getRemoteAddresses();
                clientChannel.send(ByteBuffer.wrap(data, 0, len), messageInfo);
            } catch (IOException ex) {
                disconnectedClients.add(clientChannel);
                System.out.println("Listener disconnected " + remoteAddresses);
            }
        }
        for (SctpChannel disconnectedClient : disconnectedClients) {
            clientChannels.remove(disconnectedClient);
        }
    }

    @Override
    public void close() throws IOException {
        try (serverChannel) {
            for (SctpChannel clientChannel : clientChannels) {
                clientChannel.close();
            }
        }
    }
    
}
