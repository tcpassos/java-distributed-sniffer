package unisinos.java.sniffer;

import io.pkts.Pcap;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "Sniffer", mixinStandardHelpOptions = true)
public class SnifferTest implements Runnable {
    
    @Option(names = { "-s", "--host" }, description = "Host to sniff") 
    String host = "";

    @Override
    public void run() {
        String command = "sudo tcpdump -w - -U";
        try (InputStream processStream = ProcessExecutor.getShellProcess(command).getInputStream()) {
            final Pcap pcap = Pcap.openStream(processStream);
            pcap.loop(new TcpDumpPacketHandler());
        } catch (IOException ex) {
            Logger.getLogger(SnifferTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) throws IOException {        
        int exitCode = new CommandLine(new SnifferTest()).execute(args); 
        System.exit(exitCode);
    }

}
