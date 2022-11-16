package unisinos.sniffer.broadcast;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import unisinos.sniffer.buffer.BufferHandler;

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
    
    /**
     * Assigns an event that will consume the information received from the server
     *
     * @param handler Data handler
     */
    public void onDataReceived(BufferHandler handler);

}
