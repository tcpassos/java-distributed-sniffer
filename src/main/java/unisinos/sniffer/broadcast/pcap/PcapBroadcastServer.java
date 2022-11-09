package unisinos.sniffer.broadcast.pcap;

import java.io.IOException;
import java.io.InputStream;
import unisinos.sniffer.SnifferConstants;
import unisinos.sniffer.broadcast.BroadcastServer;
import unisinos.sniffer.handler.InputStreamHandler;
import unisinos.sniffer.shell.ProcessExecutor;

public class PcapBroadcastServer implements BroadcastServer {
    
    private final BroadcastServer server;
    private final String captureCommand;
    
    public PcapBroadcastServer(BroadcastServer server, String captureCommand) throws IOException {
        this.server = server;
        this.captureCommand = captureCommand;
    }

    @Override
    public Thread start() throws IOException {
        // Opens a input stream with the packet capture process output
        InputStream captureInputStream = ProcessExecutor.getShellProcess(captureCommand)
                                                        .getInputStream();
        // Prepare to send packets to listeners when it arrives
        InputStreamHandler pcapHandler = new InputStreamHandler(captureInputStream,
                                                                buffer -> server.send(buffer, buffer.length),
                                                                SnifferConstants.MAX_BUFFER_SIZE);
        pcapHandler.handle();
        // Starts the server
        return server.start();
    }

    @Override
    public void send(byte[] data, int len) {
        server.send(data, len);
    }

    @Override
    public void close() throws IOException {
        server.close();
    }

}
