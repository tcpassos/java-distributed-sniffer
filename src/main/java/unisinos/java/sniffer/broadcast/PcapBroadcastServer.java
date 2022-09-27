package unisinos.java.sniffer.broadcast;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import unisinos.java.sniffer.process.ProcessExecutor;

public class PcapBroadcastServer extends BroadcastGenericServer {
    
    private final byte[] pcapHeader;
    
    public PcapBroadcastServer(int serverPort, String captureCommand) throws IOException {
        super(serverPort);        
        // Opens a input stream with the packet capture process output
        Process captureProcess = ProcessExecutor.getShellProcess(captureCommand);
        InputStream captureInputStream = captureProcess.getInputStream();
        super.setInputStream(captureInputStream);
        // Save the pcap global header to send as the first message to the listeners
        Buffer pcapBuffer = Buffers.wrap(captureInputStream);
        this.pcapHeader = pcapBuffer.readBytes(24).getArray();
    }

    @Override
    protected void onListenerConnection(SocketAddress listenerAddr) throws IOException {
        super.onListenerConnection(listenerAddr);
        super.send(pcapHeader, pcapHeader.length, listenerAddr);
    }

}
