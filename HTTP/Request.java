package HTTP;

import Utils.Utils;
import model.FileIdMap;
import server.Server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Request {
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_DELETE = "DELETE";

    private static final List<String> allowMethods = List.of(METHOD_PUT, METHOD_GET, METHOD_DELETE);

    public static final String FILENAME_FORMAT_REGEX = "[^/\\\\:*?\"<>|\\s]+";

    public static final String REQUEST_FORMAT_REGEX = "(PUT\\s" + FILENAME_FORMAT_REGEX + "\\s.*)|(GET|DELETE)\\s" + FILENAME_FORMAT_REGEX;

    private static FileIdMap fileIdMap;

    public static Response PUT(String filename, String content) {
        if (Files.exists(Path.of(filename))) {
            return new Response(Response.HTTP_FORBIDDEN, "");
        }

        if (Utils.writeTextToFile(filename, content)) {
            return new Response();
        }

        return new Response(Response.HTTP_FORBIDDEN, "");
    }

    public static Response PUT(Path path, String filename, byte[] data) {
        Path serverPath = Paths.get(Server.SERVER_DATA_DIR + filename);
        if (Files.exists(serverPath)) {
            return new Response(Response.HTTP_FORBIDDEN, "");
        }

        final int id = Utils.saveOnServer(path, filename, data);
        if (id == -1) {
            return new Response(Response.HTTP_ERROR, "");
        }

        return new Response(Response.HTTP_OK, String.valueOf(id));
    }

    private static synchronized FileIdMap getFileIdMap() {
        if (fileIdMap == null) {
            fileIdMap = Utils.getFileIdsMap();
            return fileIdMap;
        }
        return fileIdMap;
    }


    public static Response GET(String by, String paramValue) {
        String filename = paramValue;

        if (by.equals("BY_ID")) {
            FileIdMap fileIdMap = getFileIdMap();
            if (fileIdMap == null) {
                return new Response(Response.HTTP_NOT_FOUND, "");
            }
            filename = getFileIdMap().getMap().get(Integer.parseInt(paramValue));
        }

        final byte[] bytes = Utils.loadFromServer(filename);
        if (bytes == null) {
            return new Response(Response.HTTP_NOT_FOUND, "");
        }

        return new Response(Response.HTTP_OK, bytes);
    }

    public static Response DELETE(String by, String paramValue) {
        String filename = paramValue;
        FileIdMap fileIdMap = getFileIdMap();

        if (fileIdMap == null) {
            return new Response(Response.HTTP_NOT_FOUND, "");
        }

        if (by.equals("BY_ID")) {
            filename = fileIdMap.getMap().get(Integer.parseInt(paramValue));
        }

        Path path = Paths.get(Server.SERVER_DATA_DIR + filename);
        if(filename == null ){
            return new Response(Response.HTTP_NOT_FOUND, "");
        }

        if(!Files.exists(path)){
            return new Response(Response.HTTP_NOT_FOUND, "");
        }

        try {
            Files.deleteIfExists(path);
            for (var entry : fileIdMap.getMap().entrySet()) {
                if (filename.equals(entry.getValue())) {
                    fileIdMap.getMap().remove(entry.getKey());
                    break;
                }
            }
            Utils.serialize(fileIdMap, Server.SERVER_DATA_DIR + Server.ID_NAMES_MAP_FILE);
            return new Response();
        } catch (IOException e) {
            return new Response(Response.HTTP_NOT_FOUND, "");
        }
    }

    public static Response DELETE(String filename) {
        try {
            if (Files.deleteIfExists(Path.of(filename))) {
                return new Response();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return new Response(Response.HTTP_NOT_FOUND, "");
    }
}
