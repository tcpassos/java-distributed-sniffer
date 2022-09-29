package unisinos.sniffer.handler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import unisinos.sniffer.SnifferConstants;

public class DatagramPacketHandler {
    
    private final DatagramSocket socket;
    private final Consumer<DatagramPacket> handler;
    private Consumer<Exception> errorHandler;

    public DatagramPacketHandler(DatagramSocket socket, Consumer<DatagramPacket> handler) {
        this.socket = socket;
        this.handler = handler;
        this.errorHandler = (ex -> Logger.getLogger(DatagramPacketHandler.class.getName()).log(Level.SEVERE, null, ex));
    }

    public void onError(Consumer<Exception> errorHandler) {
        this.errorHandler = errorHandler;
    }

    public Thread handle() throws IOException {
        Thread handlerThread = new Thread(() -> {
            byte[] receivedData = new byte[SnifferConstants.MAX_BUFFER_SIZE];
            while (true) {
                Arrays.fill(receivedData, (byte) 0); // Clear the buffer
                DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);
                try {
                    socket.receive(receivePacket);
                    handler.accept(receivePacket);
                } catch (IOException ex) {
                    errorHandler.accept(ex);
                }
            }
        });
        handlerThread.start();
        return handlerThread;
    }

}
