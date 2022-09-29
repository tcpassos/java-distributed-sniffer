package unisinos.sniffer.broadcast.pcap;

import java.io.IOException;
import java.io.InputStream;
import unisinos.sniffer.SnifferConstants;
import unisinos.sniffer.broadcast.BroadcastUdpServer;
import unisinos.sniffer.handler.InputStreamHandler;
import unisinos.sniffer.shell.ProcessExecutor;

public class PcapBroadcastServer extends BroadcastUdpServer {
    
    public PcapBroadcastServer(int serverPort, String captureCommand) throws IOException {
        super(serverPort);
        // Opens a input stream with the packet capture process output
        InputStream captureInputStream = ProcessExecutor.getShellProcess(captureCommand)
                                                        .getInputStream();
        // Prepare to send packets to listeners when it arrives
        InputStreamHandler pcapHandler = new InputStreamHandler(captureInputStream,
                                                                buffer -> super.send(buffer, buffer.length),
                                                                SnifferConstants.MAX_BUFFER_SIZE);
        pcapHandler.handle();
    }

}
