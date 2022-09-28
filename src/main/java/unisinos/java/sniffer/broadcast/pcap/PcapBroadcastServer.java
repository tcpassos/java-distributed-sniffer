package unisinos.java.sniffer.broadcast.pcap;

import java.io.IOException;
import java.io.InputStream;
import unisinos.java.sniffer.broadcast.BroadcastGenericServer;
import unisinos.java.sniffer.shell.ProcessExecutor;

public class PcapBroadcastServer extends BroadcastGenericServer {
    
    public PcapBroadcastServer(int serverPort, String captureCommand) throws IOException {
        super(serverPort);
        // Opens a input stream with the packet capture process output
        Process captureProcess = ProcessExecutor.getShellProcess(captureCommand);
        InputStream captureInputStream = captureProcess.getInputStream();
        super.setInputStream(captureInputStream);
    }

}
