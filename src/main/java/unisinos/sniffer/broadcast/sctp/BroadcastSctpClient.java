package unisinos.sniffer.broadcast.sctp;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.NotificationHandler;
import com.sun.nio.sctp.SctpChannel;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import unisinos.sniffer.SnifferConstants;
import unisinos.sniffer.broadcast.BroadcastClient;
import unisinos.sniffer.buffer.BufferHandler;
import unisinos.sniffer.buffer.BufferHandlers;

public class BroadcastSctpClient implements BroadcastClient {
    
    private final List<SctpChannel> hostChannels;
    private BufferHandler onPacketReceived;
    private boolean isClosed;

    public BroadcastSctpClient() {
        this.hostChannels = new ArrayList<>();
        this.onPacketReceived = BufferHandlers.RECEIVED_BYTES_MESSAGE;
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
        // Opens an SCTP channel with the host address
        SctpChannel hostChannel = SctpChannel.open(new InetSocketAddress(host, port), 0, 0);
        hostChannels.add(hostChannel);
        new Thread(() -> {
            // Object responsible for handling host association connection and disconnection events
            NotificationHandler assocHandler = new SctpAssociationHandler(host, port);
            ByteBuffer buffer = ByteBuffer.allocate(SnifferConstants.MAX_BUFFER_SIZE);
            MessageInfo messageInfo;
            while (true) {
                try {
                    // Clears the buffer and waits for the next message from the host
                    buffer.clear();
                    messageInfo = hostChannel.receive(buffer, System.out, assocHandler);
                    // If the message is null it could mean that the host is no longer live
                    if (Objects.isNull(messageInfo)) {
                        break;
                    }
                    onPacketReceived.handle(Arrays.copyOf(buffer.array(), messageInfo.bytes()));
                } catch (IOException ex) {
                    Logger.getLogger(BroadcastSctpClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
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
     * Closes SCTP channels of all connected hosts
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        for (SctpChannel hostChannel : hostChannels) {
            hostChannel.close();
        }
        isClosed = true;
    }
    
}

