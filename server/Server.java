package server;

import HTTP.Request;
import HTTP.Response;
import Utils.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {
    private final String SERVER_ADDRESS;
    private final int SERVER_PORT;
    public static String SERVER_DATA_DIR;

    public static final String ID_NAMES_MAP_FILE = "id_map.map";

    public Server(String SERVER_ADDRESS, int SERVER_PORT, String SERVER_DATA_DIR) {
        this.SERVER_ADDRESS = SERVER_ADDRESS;
        this.SERVER_PORT = SERVER_PORT;
        Server.SERVER_DATA_DIR = SERVER_DATA_DIR;
    }

    public void run() {
        try (ServerSocket server = new ServerSocket(SERVER_PORT, 50, InetAddress.getByName(SERVER_ADDRESS))) {
            System.out.println("Server started!");

            File file = new File(SERVER_DATA_DIR);

            file.mkdirs();

            if (!file.exists()) {
                System.out.println("Directory '" + SERVER_DATA_DIR + "' does not exit");
            } else {
                while (true) {
                    try (
                            Socket socket = server.accept(); // accept a new client
                            DataInputStream input = new DataInputStream(socket.getInputStream());
                            DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                    ) {
                        String request = input.readUTF(); // read a message from the client
                        if (request.equals("exit")) {
                            break;
                        } else {
                            String[] params = request.split(" ");
                            if (request.startsWith("PUT")) {
                                Path path = Paths.get(Utils.inverseFormatRequestParam(params[1]));
                                String filename = null;
                                if (params.length == 3) {
                                    filename = params[2];
                                }

                                int length = input.readInt();
                                if (length > 0) {
                                    byte[] data = new byte[length];
                                    input.readFully(data, 0, data.length);

                                    Response response = Request.PUT(path, filename, data);

                                    output.writeUTF(String.format("%s %s", response.getStatus(), response.getContent()));
                                }
                            } else if (request.startsWith("GET") || request.startsWith("DELETE")) {
                                String by = params[1];
                                String paramValue = params[2];
                                Response response;

                                if (request.startsWith("GET")) {

                                    response = Request.GET(by, paramValue);

                                    if (response.getBytes() == null) {
                                        output.writeUTF(String.valueOf(response.getStatus()));
                                    } else {
                                        output.writeUTF(String.valueOf(response.getStatus()));
                                        output.writeInt(response.getBytes().length);
                                        output.write(response.getBytes());
                                    }
                                } else {
                                    response = Request.DELETE(by, paramValue);
                                    output.writeUTF(String.valueOf(response.getStatus()));
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
