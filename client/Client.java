package client;

import Utils.Utils;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Scanner;

public class Client {
    private final String CLIENT_DATA_DIR;
    private final String SERVER_ADDRESS;
    private final int SERVER_PORT;
    private static final String REQUEST_SENT = "The request was sent.";
    private static final String RESPONSE_SAYS = "The response says that";

    private final Scanner scanner = new Scanner(System.in);

    public Client(String SERVER_ADDRESS, int SERVER_PORT, String CLIENT_DATA_DIR) {
        this.SERVER_ADDRESS = SERVER_ADDRESS;
        this.SERVER_PORT = SERVER_PORT;
        this.CLIENT_DATA_DIR = CLIENT_DATA_DIR;
    }

    public void run() {

        try (Socket socket = new Socket(InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

            File file = new File(CLIENT_DATA_DIR);

            file.mkdirs();

            System.out.print("Enter action (1 - get a file, 2 - create a file, 3 - delete a file): ");
            String action = scanner.nextLine();

            switch (action) {
                case "1" -> get(input, output);
                case "2" -> create(input, output);
                case "3" -> delete(input, output);
                case "exit" -> exit(input, output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void exit(DataInputStream input, DataOutputStream output) throws IOException {
        System.out.println(REQUEST_SENT);
        output.writeUTF("exit");
    }

    private void create(DataInputStream input, DataOutputStream output) throws IOException {
        System.out.print("Enter name of the file: ");
        String filename = scanner.nextLine();
        System.out.print("Enter name of the file to be saved on server: ");
        String filenameToBeSaved = scanner.nextLine();

        try (var file = new FileInputStream(CLIENT_DATA_DIR + filename)) {
            byte[] data = file.readAllBytes();
            output.writeUTF(String.format("PUT %s %s", Utils.formatRequestParam(Paths.get(CLIENT_DATA_DIR + filename).toString()), filenameToBeSaved));
            output.writeInt(data.length);
            output.write(data);
            System.out.println(REQUEST_SENT);

            String res = input.readUTF();

            if (res.startsWith("200")) {
                String id = res.split(" ")[1];
                System.out.printf("Response says that file is saved! ID = %s\n", id);
            } else if (res.equals("403")) {
                System.out.printf("%s creating the file was forbidden!\n", RESPONSE_SAYS);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
    }

    private void getOrDelete(String type, DataInputStream input, DataOutputStream output) throws IOException {
        System.out.printf("Do you want to %s the file by name or by id (1 - name, 2 - id): ", type);
        int getBy = scanner.nextInt();
        scanner.nextLine();

        String by = "";
        String paramName = "";

        if (getBy == 1) {
            by = "BY_NAME ";
            paramName = "name";
        } else if (getBy == 2) {
            by = "BY_ID ";
            paramName = "id";
        }

        System.out.printf("Enter %s: ", paramName);
        String paramValue = scanner.nextLine();

        output.writeUTF(String.format("%s %s%s", type.toUpperCase(), by, paramValue));
        System.out.println(REQUEST_SENT);
        String res = input.readUTF();

        if (res.equals("404")) {
            System.out.printf("%s this file is not found!", RESPONSE_SAYS);
        }

        if (type.equals("get")) {
            if (res.startsWith("200")) {
                System.out.print("The file was downloaded! Specify a name for it: ");
                int len = input.readInt();
                byte[] data = new byte[len];
                input.readFully(data, 0, len);

                String filename = scanner.nextLine();
                Utils.save(Paths.get(CLIENT_DATA_DIR + filename), data);
                System.out.println("File saved on the hard drive!");
            }
        } else if (type.equals("delete")) {
            if (res.startsWith("200")) {
                System.out.printf("%s this file was successfully deleted!\n", RESPONSE_SAYS);
            }
        }
    }

    private void get(DataInputStream input, DataOutputStream output) throws IOException {
        getOrDelete("get", input, output);
    }

    public void delete(DataInputStream input, DataOutputStream output) throws IOException {
        getOrDelete("delete", input, output);
    }
}
