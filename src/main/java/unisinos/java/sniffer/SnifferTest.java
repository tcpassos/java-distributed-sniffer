package unisinos.java.sniffer;

import static com.diogonunes.jcolor.Ansi.colorize;
import com.diogonunes.jcolor.Attribute;
import java.io.Closeable;
import java.io.File;
import unisinos.java.sniffer.constants.SnifferConstants;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import unisinos.java.sniffer.broadcast.BroadcastClient;
import unisinos.java.sniffer.broadcast.BroadcastServer;
import unisinos.java.sniffer.broadcast.pcap.PcapBroadcastClient;
import unisinos.java.sniffer.broadcast.pcap.PcapBroadcastServer;

@Command(name = "Sniffer", mixinStandardHelpOptions = true)
public class SnifferTest implements Runnable {
    
    // Commandline parameters
    @Option(names = { "-s", "--host" }, description = "Host to sniff") 
    String host = "";
    @Option(names = { "-S", "--host-file" }, description = "File with hosts to sniff") 
    String hostFile = "";
    @Option(names = { "-p", "--port" }, description = "Server port (default=" + SnifferConstants.SERVER_DEFAULT_PORT + ")")
    int serverPort = SnifferConstants.SERVER_DEFAULT_PORT;
    @Option(names = { "--no-serve" }, description = "Does not act as a server visible to other hosts") 
    boolean noServe = false;
    @Option(names = { "-o", "--output" }, description = "Output file") 
    String output = "";
    @Option(names = { "-c", "--command" }, description = "Command to perform packet capture (default=tcpdump)") 
    String captureCommand = "tcpdump -w - -U host $(hostname -I | awk '{print $1}') and port not " + serverPort;
    
    List<Thread> threads;

    public SnifferTest() {
        threads = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            startServer();
            startClient();
            for (Thread thread: threads) thread.join(); // Waits for threads to finish
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(SnifferTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Starts a server that will send the packets captured on the local machine to all connected listeners
     *
     * @throws IOException 
     */
    private void startServer() throws IOException {
        if (noServe) {
            return;
        }
        BroadcastServer server = new PcapBroadcastServer(serverPort, captureCommand);
        handleCloseOnShutdown(server);
        threads.add(server.start());
        // Print the local IP used to serve
        System.out.println(colorize("***********************************", Attribute.YELLOW_TEXT()));
        System.out.printf (colorize("Sending packets at %s:%d\n", Attribute.YELLOW_TEXT()), getLocalIp(), serverPort);
        System.out.println(colorize("***********************************", Attribute.YELLOW_TEXT()));
    }
    
    /**
     * Set the client to listen on all hosts specified as parameters
     *
     * @throws IOException 
     */
    private void startClient() throws IOException {
        if (host.isEmpty() && hostFile.isEmpty()) {
            return;
        }
        BroadcastClient client;
        if (output.isEmpty()) {
            client = new PcapBroadcastClient();
        } else {
            client = new PcapBroadcastClient(new File(output));
        }
        handleCloseOnShutdown(client);
        threads.add(client.start());
        Set<String> hostsToSniff = getHostsToSniff();
        // Print hosts banner
        System.out.println(colorize("***********************************", Attribute.GREEN_TEXT()));
        System.out.println(colorize("Listening for packets from:", Attribute.GREEN_TEXT()));
        System.out.println(colorize("***********************************", Attribute.GREEN_TEXT()));
        hostsToSniff.forEach(hostToSniff -> System.out.println(colorize(host, Attribute.BOLD(), Attribute.GREEN_TEXT())));
        System.out.println(colorize("***********************************", Attribute.GREEN_TEXT()));
        // Start to listening for packets from hosts
        for (String hostName: hostsToSniff)
            client.addHost(InetAddress.getByName(hostName), serverPort);
    }
    
    private String getLocalIp() {
        try (final DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.connect(InetAddress.getByName("8.8.8.8"), 12345);
            return datagramSocket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException ex) {
            return "<error>";
        }
    }
    
    private Set<String> getHostsToSniff() {
        Set<String> hostsToSniff = new HashSet<>();
        if (!host.isEmpty()) {
            hostsToSniff.add(host);
        }
        if (!hostFile.isEmpty()) {
            Path hostsPath = Path.of(hostFile);
            if (Files.exists(hostsPath)) {
                try {
                    hostsToSniff.addAll(Files.readAllLines(hostsPath));
                } catch (IOException ex) { }
            }
        }
        return hostsToSniff;
    }
    
    private void handleCloseOnShutdown(Closeable closeable) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    closeable.close();
                } catch (IOException ex) {
                    Logger.getLogger(SnifferTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    public static void main(String[] args) throws IOException {        
        int exitCode = new CommandLine(new SnifferTest()).execute(args); 
        System.exit(exitCode);
    }

}
