package model;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class FileIdMap implements Serializable {
    @Serial
    private static final long serialVersionUID = -3876142896056425171L;
    private final Map<Integer, String> map = new LinkedHashMap<>();
    private int lastId = 0;

    public synchronized int getLastId() {
        return lastId;
    }

    public Map<Integer, String> getMap() {
        return map;
    }

    public synchronized void add(String filename) {
        map.put(++lastId, filename);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        for (var entry : map.entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append(";\n");
        }

        return "FileIdMap{" +
                "map=" + sb +
                ", lastId=" + lastId +
                '}';
    }
}
