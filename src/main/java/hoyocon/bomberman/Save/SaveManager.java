package hoyocon.bomberman.Save;

import java.io.*;

public class SaveManager {
    private static final String SAVE_FILE = "savegame.dat";

    public static void save(GameState state) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SAVE_FILE)))
        {
            oos.writeObject(state);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GameState load() {
        File f = new File(SAVE_FILE);
        if (!f.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(f)))
        {
            return (GameState) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
