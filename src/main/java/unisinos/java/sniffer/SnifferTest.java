package unisinos.java.sniffer;

import io.pkts.Pcap;
import unisinos.java.sniffer.process.ProcessExecutor;
import unisinos.java.sniffer.constants.SnifferConstants;
import unisinos.java.sniffer.broadcast.BroadcastClient;
import unisinos.java.sniffer.broadcast.BroadcastServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "Sniffer", mixinStandardHelpOptions = true)
public class SnifferTest implements Runnable {
    
    // Commandline parameters
    @Option(names = { "-s", "--host" }, description = "Host to sniff") 
    String host = "";
    @Option(names = { "-p", "--port" }, description = "Server port (default=" + SnifferConstants.SERVER_DEFAULT_PORT + ")")
    int severPort = SnifferConstants.SERVER_DEFAULT_PORT;
    @Option(names = { "--no-serve" }, description = "Does not act as a server visible to other hosts") 
    boolean noServe = false;
    @Option(names = { "-c", "--command" }, description = "Command to perform packet capture (default=tcpdump)") 
    String captureCommand = "sudo tcpdump -w - -U";
    
    List<Thread> threads;

    public SnifferTest() {
        threads = new ArrayList<>();
    }

    @Override
    public void run() {
        try {            
            startServer(); // Starts the server and simulates server messages
            startClient(); // Put the client to listen to the host broadcast
            for (Thread thread: threads) thread.join(); // Waits for threads to finish
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(SnifferTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void startClient() throws IOException {
        if (host.isEmpty()) {
            return;
        }
        BroadcastClient client = new BroadcastClient();
        client.addHost(InetAddress.getByName(host), severPort);
        threads.add(client.start());
               
        Pcap pcap = Pcap.openStream(client.getInputStream());
        pcap.loop((packet) -> {
            System.out.println(packet);
            return true;
        });
    }

    private void startServer() throws IOException {
        if (noServe) {
            return;
        }
        BroadcastServer server = new BroadcastServer(severPort);
        server.setInputStream(getPacketCaptureStream());
        threads.add(server.start());
    }
    
    private InputStream getPacketCaptureStream() throws IOException {
        Process captureProcess = ProcessExecutor.getShellProcess(captureCommand);
        return captureProcess.getInputStream();
    }
    
    public static void main(String[] args) throws IOException {        
        int exitCode = new CommandLine(new SnifferTest()).execute(args); 
        System.exit(exitCode);
    }

}
