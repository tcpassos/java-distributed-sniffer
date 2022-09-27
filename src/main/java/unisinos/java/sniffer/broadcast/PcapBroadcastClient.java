package unisinos.java.sniffer.broadcast;

import io.pkts.Pcap;
import io.pkts.PcapOutputStream;
import io.pkts.packet.IPPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.TransportPacket;
import io.pkts.protocol.Protocol;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PcapBroadcastClient extends BroadcastGenericClient {
    
    private File outputFile;
    private PcapOutputStream outputFileStream;

    public PcapBroadcastClient(File outputFile) throws IOException {
        super();
        this.outputFile = outputFile;
        handlePackets();
    }

    public PcapBroadcastClient() throws IOException {
        super();
        handlePackets();
    }

    private void handlePackets() throws IOException {
        new Thread(() -> {
            try {
                Pcap pcap = Pcap.openStream(super.getInputStream());
                if (Objects.nonNull(outputFile)) {
                    outputFileStream = pcap.createOutputStream(new FileOutputStream(outputFile));
                }
                pcap.loop((packet) -> {
                    if (Objects.nonNull(outputFileStream)) {
                        outputFileStream.write(packet);
                    }
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

    @Override
    public void close() throws IOException {
        super.close();
        if (Objects.nonNull(outputFileStream)) {
            outputFileStream.close();
        }
    }

}
