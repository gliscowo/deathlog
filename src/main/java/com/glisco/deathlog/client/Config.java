package com.glisco.deathlog.client;

import com.glisco.deathlog.storage.BaseDeathLogStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {

    private static Config INSTANCE = new Config();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String configFileName = "deathlog.json";

    public boolean screenshotsEnabled = false;

    public static void save() {
        try (var writer = new FileWriter(FabricLoader.getInstance().getConfigDir().resolve(configFileName).toFile())) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            BaseDeathLogStorage.LOGGER.warn("Could not save config", e);
        }
    }

    public static void load() {
        try (var reader = new FileReader(FabricLoader.getInstance().getConfigDir().resolve(configFileName).toFile())) {
            INSTANCE = GSON.fromJson(reader, Config.class);
        } catch (IOException e) {
            if (e instanceof FileNotFoundException) {
                save();
            } else {
                BaseDeathLogStorage.LOGGER.warn("Could not load config", e);
            }
        }
    }

    public static Config instance() {
        return INSTANCE;
    }
}
