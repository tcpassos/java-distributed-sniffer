package unisinos.sniffer.broadcast.sctp;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public Thread start() throws IOException {
        Thread serverThread = new Thread(() -> {
            while (true) {
                try {
                    SctpChannel clientChannel = serverChannel.accept();
                    clientChannels.add(clientChannel);
                    System.out.println("New listener " + clientChannel.getAllLocalAddresses());
                } catch (IOException ex) {
                    Logger.getLogger(BroadcastSctpServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        serverThread.start();
        return serverThread;
    }

    @Override
    public void send(byte[] data, int len) {
        for (SctpChannel clientChannel : clientChannels) {
            MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0);
            try {
                clientChannel.send(ByteBuffer.wrap(data, 0, len), messageInfo);
            } catch (IOException ex) {
                // TODO: Tratar desconexão
            }
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
