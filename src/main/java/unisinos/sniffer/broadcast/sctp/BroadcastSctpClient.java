package unisinos.sniffer.broadcast.sctp;

import com.sun.nio.sctp.AbstractNotificationHandler;
import com.sun.nio.sctp.AssociationChangeNotification;
import static com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent.COMM_UP;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.ShutdownNotification;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import unisinos.sniffer.SnifferConstants;
import unisinos.sniffer.broadcast.BroadcastClient;
import unisinos.sniffer.handler.BufferHandler;

public class BroadcastSctpClient implements BroadcastClient {
    
    private final List<SctpChannel> hostChannels;
    private BufferHandler onPacketReceived;

    public BroadcastSctpClient() {
        this.hostChannels = new ArrayList<>();
        this.onPacketReceived = (packetBytes) -> System.out.println("Received " + packetBytes.length + " bytes");
    }

    @Override
    public Thread start() throws IOException {
        // TODO: Refatorar estrutura da interface de cliente para não obrigar uma thread aqui
        Thread clientThread = new Thread(() -> {
            while(true) { }
        });
        clientThread.start();
        return clientThread;
    }

    @Override
    public void addHost(InetAddress host, int port) throws IOException {
        SctpChannel hostChannel = SctpChannel.open(new InetSocketAddress(host, port), 0, 0);
        hostChannels.add(hostChannel);
        new Thread(() -> {
            AssociationHandler assocHandler = new AssociationHandler();
            ByteBuffer buffer = ByteBuffer.allocate(SnifferConstants.MAX_BUFFER_SIZE);
            MessageInfo messageInfo;
            while (true) {
                try {
                    buffer.clear();
                    messageInfo = hostChannel.receive(buffer, System.out, assocHandler);
                    onPacketReceived.handle(Arrays.copyOf(buffer.array(), messageInfo.bytes()));
                } catch (IOException ex) {
                    Logger.getLogger(BroadcastSctpClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    @Override
    public void onDataReceived(BufferHandler handler) {
        this.onPacketReceived = handler;
    }

    @Override
    public void close() throws IOException {
        for (SctpChannel hostChannel : hostChannels) {
            hostChannel.close();
        }
    }
    
    static class AssociationHandler extends AbstractNotificationHandler<PrintStream> {

      @Override
      public HandlerResult handleNotification(AssociationChangeNotification not, PrintStream stream) {
          if (not.event().equals(COMM_UP)) {
              int outbound = not.association().maxOutboundStreams();
              int inbound = not.association().maxInboundStreams();
              stream.printf("New association setup with %d outbound streams, and %d inbound streams.\n", outbound, inbound);
          }
          return HandlerResult.CONTINUE;
      }

      @Override
      public HandlerResult handleNotification(ShutdownNotification not, PrintStream stream) {
          stream.printf("The association has been shutdown.\n");
          return HandlerResult.RETURN;
      }
  }
    
}

