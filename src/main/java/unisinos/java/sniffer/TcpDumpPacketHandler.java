package unisinos.java.sniffer;

import io.pkts.PacketHandler;
import io.pkts.packet.IPPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.TransportPacket;
import io.pkts.protocol.Protocol;
import java.io.IOException;
import java.util.Optional;

public class TcpDumpPacketHandler implements PacketHandler {

    @Override
    public boolean nextPacket(Packet packet) throws IOException {
        if (packet.hasProtocol(Protocol.TCP)) {
            Optional<TransportPacket> transportPacketOpt = Optional.of((TransportPacket) packet.getPacket(Protocol.TCP));
            if (transportPacketOpt.isPresent()) {
                TransportPacket transportPacket = transportPacketOpt.get();
                int sourcePort = transportPacket.getSourcePort();
                int destPort = transportPacket.getDestinationPort();

                IPPacket ip = (IPPacket) transportPacket.getParentPacket();
                String destIp = ip.getDestinationIP();
                String sourceIp = ip.getSourceIP();

                System.out.printf("[%s:%s] -> [%s:%s]\n", sourceIp, sourcePort, destIp, destPort);
                Optional.ofNullable(transportPacket.getPayload()).ifPresent(payload -> System.out.println(payload));
            }
        }
        return true;
    }

}
