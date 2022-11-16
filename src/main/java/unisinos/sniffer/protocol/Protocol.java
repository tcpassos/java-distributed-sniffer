package unisinos.sniffer.protocol;

import java.util.Arrays;

public enum Protocol {
    
    UDP("UDP"),
    TCP("TCP"),
    SCTP("SCTP");
    
    private final String name;
    
    Protocol(String name) {
        this.name = name;
    }
    
    public static Protocol parse(String protocolName) {
        switch (protocolName) {
            case "UDP":
                return Protocol.UDP;
            case "TCP":
                return Protocol.TCP;
            case "SCTP":
                return Protocol.SCTP;
            default:
                throw new IllegalArgumentException("Protocol not implemented: " + protocolName + ". Valid protocols are " + Arrays.toString(Protocol.values()));
        }
    }

    public String getName() {
        return name;
    }
    
}
