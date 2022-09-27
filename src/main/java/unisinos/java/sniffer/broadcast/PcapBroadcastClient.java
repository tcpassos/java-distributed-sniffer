package unisinos.java.sniffer.broadcast;

import io.pkts.Pcap;
import io.pkts.packet.IPPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.TransportPacket;
import io.pkts.protocol.Protocol;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PcapBroadcastClient extends BroadcastGenericClient {

    public PcapBroadcastClient() throws IOException {
        super();
        handlePackets();
    }

    private void handlePackets() throws IOException {
        new Thread(() -> {
            try {
                Pcap pcap = Pcap.openStream(super.getInputStream());
                pcap.loop((packet) -> {
                    printPacket(packet);
                    return true;
                });
            } catch (IOException ex) {
                Logger.getLogger(PcapBroadcastClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();
    }
    
    private void printPacket(Packet packet) throws IOException {
        Optional<TransportPacket> transportPacketOpt = getTransportPacket(packet);
        if (transportPacketOpt.isEmpty()) {
            return;
        }
        TransportPacket transportPacket = transportPacketOpt.get();
        IPPacket ipPacket = transportPacket.getParentPacket();
        System.out.println("\n--------------------------------------");
        System.out.printf("(%s) [%s] --> [%s]\n",
                          transportPacket.getProtocol().getName().toUpperCase(),
                          ipPacket.getSourceIP(),
                          ipPacket.getDestinationIP());
        System.out.println("--------------------------------------");
        Optional.ofNullable(transportPacket.getPayload()).ifPresent(payload -> System.out.println(payload));
    }
    
    private Optional<TransportPacket> getTransportPacket(Packet packet) throws IOException {
        if (packet.hasProtocol(Protocol.TCP))
            return Optional.of((TransportPacket) packet.getPacket(Protocol.TCP));
        if (packet.hasProtocol(Protocol.UDP))
            return Optional.of((TransportPacket) packet.getPacket(Protocol.UDP));
        return Optional.empty();
    }
    
}
