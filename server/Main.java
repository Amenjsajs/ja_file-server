package server;

import java.io.File;

public class Main {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 23456;

    public static final String SERVER_DATA_DIR = System.getProperty("user.dir") +
            File.separator + "src" + File.separator + "server" + File.separator + "data" + File.separator;

    public static void main(String[] args) {
        Server server = new Server(SERVER_ADDRESS, SERVER_PORT, SERVER_DATA_DIR);
        server.run();
    }

}
