package unisinos.sniffer.handler;

public interface BufferHandler {
    
    public void handle(byte[] buffer);

    public default BufferHandler andThen(BufferHandler anotherHandler) {
        return buffer -> {
            handle(buffer);
            anotherHandler.handle(buffer);
        };
    }
    
}
