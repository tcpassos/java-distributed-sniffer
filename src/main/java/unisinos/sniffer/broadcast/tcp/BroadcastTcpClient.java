package unisinos.sniffer.broadcast.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import unisinos.sniffer.SnifferConstants;
import unisinos.sniffer.broadcast.BroadcastClient;
import unisinos.sniffer.handler.BufferHandler;
import unisinos.sniffer.handler.InputStreamHandler;

public class BroadcastTcpClient implements BroadcastClient {
    
    private final List<Socket> hostSockets;
    private BufferHandler onPacketReceived;

    public BroadcastTcpClient() {
        this.hostSockets = new ArrayList<>();
        this.onPacketReceived = (packetBytes) -> System.out.println("Received " + packetBytes.length + " bytes");
    }

    @Override
    public Thread start() throws IOException {
        // TODO: Refatorar estrutura da interface de cliente para não obrigar uma thread aqui
        Thread clientThread = new Thread(() -> {
            while(true) { }
        });
        clientThread.start();
        return clientThread;
    }

    @Override
    public void addHost(InetAddress host, int port) throws IOException {
        Socket clientSocket = new Socket(host, port);
        new InputStreamHandler(clientSocket.getInputStream(), onPacketReceived, SnifferConstants.MAX_BUFFER_SIZE).handle();
    }

    @Override
    public void onDataReceived(BufferHandler handler) {
        this.onPacketReceived = handler;
    }

    @Override
    public void close() throws IOException {
        for (Socket hostSocket : hostSockets) {
            hostSocket.close();
        }
    }
    
}
