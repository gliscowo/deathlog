package com.glisco.deathlog.client;

import com.glisco.deathlog.storage.BaseDeathLogStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    private static Config INSTANCE = new Config();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean screenshotsEnabled = false;

    public static void load() {
        if (!Files.exists(configPath())) {
            save();
            return;
        }

        try (var input = Files.newInputStream(configPath())) {
            INSTANCE = GSON.fromJson(new InputStreamReader(input, StandardCharsets.UTF_8), Config.class);
        } catch (IOException e) {
            BaseDeathLogStorage.LOGGER.warn("Could not load config", e);
        }
    }

    public static void save() {
        try (var output = Files.newOutputStream(configPath()); var writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            BaseDeathLogStorage.LOGGER.warn("Could not save config", e);
        }
    }

    public static Config instance() {
        if (INSTANCE == null) {
            INSTANCE = new Config();
        }

        return INSTANCE;
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("deathlog.json");
    }
}
