package Utils;

import model.FileIdMap;
import server.Server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static String formatRequestParam(String str) {
        return str.replaceAll("\\s", "...");
    }

    public static String inverseFormatRequestParam(String str) {
        return str.replaceAll("\\.\\.\\.", " ");
    }

    private static synchronized int generateId(String filename) {
        String mapFileName = Server.SERVER_DATA_DIR + Server.ID_NAMES_MAP_FILE;
        FileIdMap fileIdMap;

        try {
            if (Files.exists(Paths.get(mapFileName))) {
                fileIdMap = (FileIdMap) deserialize(mapFileName);
            } else {
                fileIdMap = new FileIdMap();
            }

            fileIdMap.add(filename);
            serialize(fileIdMap, mapFileName);

            return fileIdMap.getLastId();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static synchronized FileIdMap getFileIdsMap() {
        String mapFileName = Server.SERVER_DATA_DIR + Server.ID_NAMES_MAP_FILE;
        FileIdMap fileIdMap;

        try {
            if (Files.exists(Paths.get(mapFileName))) {
                fileIdMap = (FileIdMap) deserialize(mapFileName);
                return fileIdMap;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int saveOnServer(Path path, String filename, byte[] data) {
        String filenameToBeSaved = filename;
        if (filenameToBeSaved == null) {
            String name = path.getFileName().toString();
            int pos = name.lastIndexOf(".");

            filenameToBeSaved = name.substring(0, pos) + System.currentTimeMillis() + name.substring(pos);
        }

        if (save(Paths.get(Server.SERVER_DATA_DIR + filenameToBeSaved), data)) {
            return generateId(filenameToBeSaved);
        }
        return -1;
    }

    public static byte[] loadFromServer(String filename) {
        Path path = Paths.get(Server.SERVER_DATA_DIR + filename);
        try (var file = new FileInputStream(path.toString())) {
            return file.readAllBytes();
        } catch (IOException ignored) {
        }
        return null;
    }

    public static boolean save(Path path, byte[] data) {
        var ref = new Object() {
            boolean isSaved = false;
        };

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(() -> {
            try {
                Files.write(path, data);
                ref.isSaved = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        executor.shutdown();
        try {
            if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        return ref.isSaved;
    }

    public static boolean writeTextToFile(String filename, String text) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            bw.write(text);
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public static String readFileTextFromFile(String filename) {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return resultStringBuilder.toString();
    }

    /**
     * Serialize the given object to the file
     */
    public static void serialize(Object obj, String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
    }

    /**
     * Deserialize to an object from the file
     */
    public static Object deserialize(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }

}
