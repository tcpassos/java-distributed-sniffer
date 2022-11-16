package unisinos.sniffer.pcap;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import io.pkts.Pcap;
import io.pkts.packet.IPPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.TransportPacket;
import io.pkts.protocol.Protocol;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import unisinos.sniffer.buffer.BufferHandler;

public class PcapPromptBufferHandler implements BufferHandler {
    
    private boolean initialized;
    private final PipedOutputStream pcapOutputStream;
   
    public PcapPromptBufferHandler() throws IOException {
        // Connect the OutputStream used to write incoming packets with the InputStream used to display packets in the console
        pcapOutputStream = new PipedOutputStream();
        PipedInputStream pcapInputStream = new PipedInputStream();
        pcapInputStream.connect(pcapOutputStream);
        waitTodisplayPackets(pcapInputStream);
    }
    
    /**
     * For each received packet, write to the stream that will be read in the waitTodisplayPackets() method
     *
     * @param buffer Packet bytes
     */
    @Override
    public void handle(byte[] buffer) {
        try {
            if (!initialized) {
                PcapHeader.writeDefaultPcapHeader(pcapOutputStream);
                initialized = true;
            }
            pcapOutputStream.write(buffer);
        } catch (IOException ex) {
            Logger.getLogger(PcapPromptBufferHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void waitTodisplayPackets(InputStream pcapInputStream) throws IOException {
        new Thread(() -> {
            try {
                Pcap pcap = Pcap.openStream(pcapInputStream);
                pcap.loop((packet) -> {
                    printPacket(packet);
                    return true;
                });
            } catch (IOException ex) {
                Logger.getLogger(PcapPromptBufferHandler.class.getName()).log(Level.SEVERE, null, ex);
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
        if (Arrays.asList(0, 16, 17, 18, 232, 233, 234, 235, 236).contains(fgColor)) { // Too dark
            fgColor = 15;
        }
        attributes[0] = Attribute.TEXT_COLOR(fgColor);
        attributes[1] = Attribute.BLACK_BACK();
        return attributes;
    }

}
