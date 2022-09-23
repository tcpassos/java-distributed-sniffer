package unisinos.java.sniffer;

import io.pkts.Pcap;
import io.pkts.packet.IPPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.TransportPacket;
import io.pkts.protocol.Protocol;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class SnifferTest {

    public static void main(String[] args) throws IOException {
        try (InputStream processStream = getShellProcess("C:\\WinDump.exe -w - -U").getInputStream()) {
            final Pcap pcap = Pcap.openStream(processStream);
            pcap.loop((Packet packet) -> {
                if (packet.hasProtocol(Protocol.TCP)) {
                    (Optional.of((TransportPacket) packet.getPacket(Protocol.TCP))).ifPresent(transportPacket -> {
                        final int sourcePort = transportPacket.getSourcePort();
                        final int destPort = transportPacket.getDestinationPort();
                        final String payload = transportPacket.getPayload().toString();

                        final IPPacket ip = (IPPacket) transportPacket.getParentPacket();
                        final String destIp = ip.getDestinationIP();
                        final String sourceIp = ip.getSourceIP();

                        System.out.println(destIp + ":" + sourcePort + " -> " + sourceIp + ":" + sourcePort);
                        System.out.println(payload);
                    });
                }
                return true;
            });
        }
    }

    private static Process getShellProcess(String command) throws IOException {
        String operatingSystem = System.getProperty("os.name");
        if (operatingSystem.toLowerCase().contains("window")) {
            return new ProcessBuilder("cmd", "/c", command).start();
        } else {
            return  new ProcessBuilder("/bin/bash", "-c", command).start();
        }
    }

}
