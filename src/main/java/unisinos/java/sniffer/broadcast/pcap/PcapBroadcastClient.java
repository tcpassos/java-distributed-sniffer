package unisinos.java.sniffer.broadcast.pcap;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import io.pkts.Pcap;
import io.pkts.PcapOutputStream;
import io.pkts.packet.IPPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.TransportPacket;
import io.pkts.protocol.Protocol;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import unisinos.java.sniffer.broadcast.BroadcastGenericClient;

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
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date arrivalDate = new Date(packet.getArrivalTime() / 1000);
        System.out.printf("(%s %s) [%s:%s] --> [%s:%s]\n",
                          formatter.format(arrivalDate),
                          transportPacket.getProtocol().getName().toUpperCase(),
                          ipPacket.getSourceIP(),
                          Ansi.colorize(String.valueOf(transportPacket.getSourcePort()), getPortTextColors(transportPacket.getSourcePort())),
                          ipPacket.getDestinationIP(),
                          Ansi.colorize(String.valueOf(transportPacket.getDestinationPort()), getPortTextColors(transportPacket.getDestinationPort())));
//        ApplicationPacket applicationPacket = (ApplicationPacket) transportPacket.getNextPacket();
//        if (Objects.nonNull(applicationPacket)) {
//            Optional.ofNullable(applicationPacket.getPayload())
//                    .ifPresent(payload -> System.out.println(Ansi.colorize(payload.toString(), Attribute.CYAN_TEXT())));
//        }
    }
    
    private Optional<TransportPacket> getTransportPacket(Packet packet) throws IOException {
        if (packet.hasProtocol(Protocol.TCP))
            return Optional.of((TransportPacket) packet.getPacket(Protocol.TCP));
        if (packet.hasProtocol(Protocol.UDP))
            return Optional.of((TransportPacket) packet.getPacket(Protocol.UDP));
        return Optional.empty();
    }
    
    private Attribute[] getPortTextColors(int port) {
        Attribute[] attributes = new Attribute[2];
        int fgColor = port % 255;
        if (Arrays.asList(1, 16, 17, 18, 234, 235, 236).contains(fgColor)) { // To dark
            fgColor = 15;
        }
        attributes[0] = Attribute.TEXT_COLOR(fgColor);
        attributes[1] = Attribute.BLACK_BACK();
        return attributes;
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (Objects.nonNull(outputFileStream)) {
            outputFileStream.close();
        }
    }

}
