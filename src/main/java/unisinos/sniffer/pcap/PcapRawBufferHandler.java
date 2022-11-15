package unisinos.sniffer.pcap;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import unisinos.sniffer.handler.BufferHandler;

public class PcapRawBufferHandler implements BufferHandler {
    
    private final OutputStream rawOutputStream;
    private boolean initialized;
    
    public PcapRawBufferHandler(OutputStream rawOutputStream) throws IOException {
        this.rawOutputStream = rawOutputStream;
    }

    @Override
    public void handle(byte[] buffer) {
        try {
            if (!initialized) {
                PcapHeader.writeDefaultPcapHeader(rawOutputStream);
                initialized = true;
            }
            rawOutputStream.write(buffer);
        } catch (IOException ex) {
            Logger.getLogger(PcapRawBufferHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
