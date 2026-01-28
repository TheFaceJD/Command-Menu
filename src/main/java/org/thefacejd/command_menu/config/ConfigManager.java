package org.thefacejd.command_menu.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.thefacejd.command_menu.CommandMenuConfig;
import org.thefacejd.command_menu.Command_menu;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(Command_menu.MOD_ID + ".json");

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static CommandMenuConfig config = new CommandMenuConfig();

    public static void load() {
        if (!CONFIG_PATH.toFile().exists()) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            CommandMenuConfig loaded = GSON.fromJson(reader, CommandMenuConfig.class);
            if (loaded != null) {
                config = loaded;
            } else {
                config = CommandMenuConfig.createDefault();
                save();
            }
        } catch (Exception e) {
            System.err.println("Failed to load config, using defaults.");
            e.printStackTrace();
            config = CommandMenuConfig.createDefault();
        }

        config.validate();
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save config!");
            e.printStackTrace();
        }
    }
}
