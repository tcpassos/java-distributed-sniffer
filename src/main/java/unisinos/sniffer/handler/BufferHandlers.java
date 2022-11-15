package unisinos.sniffer.handler;

public class BufferHandlers {
    
    public static final BufferHandler RECEIVED_BYTES_MESSAGE = (packetBytes) -> {
        System.out.println("Received " + packetBytes.length + " bytes");
    };
    
}
