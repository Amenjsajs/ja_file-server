package client;

import java.io.File;

public class Main {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 23456;

    public static final String CLIENT_DATA_DIR = System.getProperty("user.dir") +
            File.separator + "src" + File.separator + "client" + File.separator + "data" + File.separator;

    public static void main(String[] args) {
        Client client = new Client(SERVER_ADDRESS, SERVER_PORT, CLIENT_DATA_DIR);
        client.run();
    }
}
