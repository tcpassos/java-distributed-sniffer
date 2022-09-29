package unisinos.sniffer.shell;

import java.io.IOException;

public class ProcessExecutor {
    
    public static Process getShellProcess(String command) throws IOException {
        String operatingSystem = System.getProperty("os.name");
        if (operatingSystem.toLowerCase().contains("window")) {
            return new ProcessBuilder("cmd", "/c", command).start();
        } else {
            return  new ProcessBuilder("/bin/bash", "-c", command).start();
        }
    }
    
}
