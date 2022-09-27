package unisinos.java.sniffer.broadcast;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;

public interface BroadcastServer extends Closeable {

    /**
     * Starts a thread that will wait for listeners that will be used for data broadcast
     *
     * @return {@code Thread}
     * @throws IOException
     */
    public Thread start() throws IOException;

    /**
     * Send a packet to all listening addresses
     *
     * @param data Data that will be sent to listeners
     * @param len Data length
     * @throws IOException
     */
    public void send(byte[] data, int len) throws IOException;

    /**
     * Send a packet to a specific listener
     *
     * @param data Data that will be sent
     * @param len Data length
     * @param listenerAddr Listener
     * @throws IOException
     */
    public void send(byte[] data, int len, SocketAddress listenerAddr) throws IOException;
    
}
