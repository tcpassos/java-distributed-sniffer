package unisinos.sniffer.pcap;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.frame.PcapGlobalHeader;
import io.pkts.protocol.Protocol;
import java.io.IOException;
import java.io.OutputStream;

public class PcapHeader {
    
    /**
     * Writes the standard header needed to create a pcap file
     *
     * @param outputStream Stream in which the header will be written
     * @throws IOException
     */
    public static void writeDefaultPcapHeader(OutputStream outputStream) throws IOException {
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
