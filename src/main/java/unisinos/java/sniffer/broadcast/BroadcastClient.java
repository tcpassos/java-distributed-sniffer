package unisinos.java.sniffer.broadcast;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;

public interface BroadcastClient extends Closeable {
    
    /**
     * Start listening to the broadcast
     *
     * @return {@code Thread}
     * @throws IOException
     */
    public Thread start() throws IOException;
    
    /**
     * Add a host to receive messages from
     * @param host Host
     * @param port Port
     * @throws IOException
     */
    public void addHost(InetAddress host, int port) throws IOException;

}
