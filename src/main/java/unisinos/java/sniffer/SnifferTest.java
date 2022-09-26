package unisinos.java.sniffer;

import unisinos.java.sniffer.broadcast.BroadcastClient;
import unisinos.java.sniffer.broadcast.BroadcastServer;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "Sniffer", mixinStandardHelpOptions = true)
public class SnifferTest implements Runnable {
    
    @Option(names = { "-s", "--host" }, description = "Host to sniff") 
    String host = "";
    @Option(names = { "--no-serve" }, description = "Does not act as a server visible to other hosts") 
    boolean noServe = false;

    @Override
    public void run() {
        try {
            List<Thread> threads = new ArrayList<>();
            // Put the client to listen to the host broadcast
            if (!host.isEmpty()) {
                BroadcastClient client = new BroadcastClient();
                client.addHost(InetAddress.getByName(host), SnifferConstants.SERVER_PORT);
                Thread clientThread = client.listen(packet -> {
                    System.out.println("----------------------------------------");
                    System.out.println("Pacote recebido de [" + packet.getSocketAddress() + "]");
                    System.out.println(new String (packet.getData()));
                });
                threads.add(clientThread);
            }
            // Starts the server
            if (!noServe) {
                BroadcastServer server = new BroadcastServer(SnifferConstants.SERVER_PORT);
                threads.add(server.start());
                // Simulates server messages
                Scanner in = new Scanner(System.in);
                String msg;
                do {
                    msg = in.nextLine();
                    server.send(msg.getBytes());
                } while (!msg.equals("exit"));
            }
            // Waits for threads to finish preventing the program from terminating
            for (Thread thread: threads) thread.join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(SnifferTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) throws IOException {        
        int exitCode = new CommandLine(new SnifferTest()).execute(args); 
        System.exit(exitCode);
    }

}
