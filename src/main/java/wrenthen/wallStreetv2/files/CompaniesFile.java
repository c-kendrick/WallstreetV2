package wrenthen.wallStreetv2.files;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CompaniesFile {

    private static File file;
    private static FileConfiguration customFile;

    //Finds or generates the custom file
    public static void setup() {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("WallStreet").getDataFolder(), "companies.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Couldn't generate file");
            }
        }
        customFile = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get() {
        return customFile;
    }

    public static void setString(String first, String second) {
        customFile.set(first, second);
    }
    public static void setList(String first, List<String> second) {
        customFile.set(first, second);
    }

    public static void save() {
        try {
            customFile.save(file);
        } catch (IOException e) {
            System.out.println("Couldn't save file");
        }
    }

    public static void reload() {
        customFile = YamlConfiguration.loadConfiguration(file);
    }
}
