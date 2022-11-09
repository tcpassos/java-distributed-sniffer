package unisinos.sniffer.broadcast.pcap;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import io.pkts.Pcap;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.frame.PcapGlobalHeader;
import io.pkts.packet.IPPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.TransportPacket;
import io.pkts.protocol.Protocol;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import unisinos.sniffer.broadcast.BroadcastClient;

public class PcapBroadcastClient implements Closeable {
    
    private final BroadcastClient client;
    
    public PcapBroadcastClient(BroadcastClient client) throws IOException {
        this.client = client;
        // Connect the OutputStream used to write incoming packets with the InputStream used to display packets in the console
        PipedOutputStream pcapOutputStream = new PipedOutputStream();
        PipedInputStream pcapInputStream = new PipedInputStream();
        pcapInputStream.connect(pcapOutputStream);
        writeDefaultPcapHeader(pcapOutputStream);
        // For each received packet, write to the stream that will be read in the waitTodisplayPackets() method
        client.onDataReceived((packetBytes) -> {
            try {
                pcapOutputStream.write(packetBytes);
            } catch (IOException ex) {
                Logger.getLogger(PcapBroadcastClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        waitTodisplayPackets(pcapInputStream);
    }

    public PcapBroadcastClient(BroadcastClient client, OutputStream rawOutputStream) throws IOException {
        this.client = client;
        writeDefaultPcapHeader(rawOutputStream);
        // For each packet received, write to OutputStream received in constructor
        client.onDataReceived((packetBytes) -> {
            try {
                rawOutputStream.write(packetBytes);
            } catch (IOException ex) {
                Logger.getLogger(PcapBroadcastClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    public Thread start() throws IOException {
        return client.start();
    }

    public void addHost(InetAddress host, int port) throws IOException {
        client.addHost(host, port);
    }

    @Override
    public void close() throws IOException {
        client.close();
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
    
    private void writeDefaultPcapHeader(OutputStream outputStream) throws IOException {
        Buffer body = Buffers.createBuffer(20);
        body.setUnsignedByte(0, (short) 2);
        body.setUnsignedByte(2, (short) 4);
        body.setUnsignedInt(4, 0);
        body.setUnsignedInt(8, 0);
        body.setUnsignedInt(12, 65535);
        Protocol protocol = Protocol.ETHERNET_II;
        Long linkType = protocol.getLinkType();
        body.setUnsignedInt(16, linkType);
        Buffer header = Buffers.createBuffer(24);
        header.write(PcapGlobalHeader.MAGIC_LITTLE_ENDIAN);
        header.write(body.getRawArray());
        outputStream.write(header.getRawArray());
    }

}
