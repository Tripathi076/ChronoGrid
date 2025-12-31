package util;

import java.io.*;

public class SaveLoad {

    private SaveLoad() {
        // Utility class
    }

    public static void save(Object obj) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("save.dat"))) {
            out.writeObject(obj);
        }
    }

    public static Object load() throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("save.dat"))) {
            return in.readObject();
        }
    }
}
