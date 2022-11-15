package unisinos.sniffer.broadcast.sctp;

import com.sun.nio.sctp.AbstractNotificationHandler;
import com.sun.nio.sctp.AssociationChangeNotification;
import static com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent.COMM_UP;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.ShutdownNotification;
import java.io.PrintStream;
import java.net.InetAddress;

public class SctpAssociationHandler extends AbstractNotificationHandler<PrintStream> {
    
    private final InetAddress host;
    private final int port;

    public SctpAssociationHandler(InetAddress host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public HandlerResult handleNotification(AssociationChangeNotification not, PrintStream stream) {
        if (not.event().equals(COMM_UP)) {
            int outbound = not.association().maxOutboundStreams();
            int inbound = not.association().maxInboundStreams();
            stream.printf("Host association for %s:%d with %d outbound streams, and %d inbound streams.\n", host.getHostAddress(), port, outbound, inbound);
        }
        return HandlerResult.CONTINUE;
    }

    @Override
    public HandlerResult handleNotification(ShutdownNotification not, PrintStream stream) {
        stream.printf("The association for %s:%d has been shutdown.\n", host.getHostAddress(), port);
        return HandlerResult.RETURN;
    }
    
}
