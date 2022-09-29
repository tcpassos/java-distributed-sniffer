package unisinos.sniffer.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InputStreamHandler {
    
    private final InputStream inputStream;
    private final BufferHandler handler;
    private final int bufferSize;
    private Consumer<Exception> errorHandler;

    public InputStreamHandler(InputStream inputStream, BufferHandler handler, int bufferSize) {
        this.inputStream = inputStream;
        this.handler = handler;
        this.bufferSize = bufferSize;
        this.errorHandler = (ex -> Logger.getLogger(InputStreamHandler.class.getName()).log(Level.SEVERE, null, ex));
    }
    
    public void onError(Consumer<Exception> errorHandler) {
        this.errorHandler = errorHandler;
    }
    
    public Thread handle() {
        Thread handlerThread = new Thread(() -> {
            byte[] data = new byte[bufferSize];
            while (true) {
                try {
                    if (inputStream.available() > 0) {
                        int dataLength = inputStream.read(data);
                        handler.handle(Arrays.copyOf(data, dataLength));
                    }
                } catch (IOException ex) {
                    errorHandler.accept(ex);
                }
            }
        });
        handlerThread.start();
        return handlerThread;
    }
    
}
