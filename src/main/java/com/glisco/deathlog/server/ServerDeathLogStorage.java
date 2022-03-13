package com.glisco.deathlog.server;

import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.death_info.SpecialPropertyProvider;
import com.glisco.deathlog.death_info.properties.*;
import com.glisco.deathlog.storage.BaseDeathLogStorage;
import com.glisco.deathlog.storage.DeathInfoCreatedCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ServerDeathLogStorage extends BaseDeathLogStorage {

    private final Map<UUID, List<DeathInfo>> deathInfos;
    private final Path deathLogDir;

    public ServerDeathLogStorage() {
        this.deathInfos = new HashMap<>();
        this.deathLogDir = FabricLoader.getInstance().getGameDir().resolve("deaths").toAbsolutePath();

        if (!Files.exists(deathLogDir) && !deathLogDir.toFile().mkdir()) {
            raiseError("Failed to create directory");

            LOGGER.error("Failed to create DeathLog storage directory, further disk operations have been disabled");
            return;
        }

        try {
            Files.list(deathLogDir).forEach(path -> {
                if (isErrored()) return;

                if (!Files.exists(path)) return;
                if (path.endsWith(".dat")) return;

                UUID uuid;

                try {
                    uuid = UUID.fromString(FilenameUtils.getBaseName(path.toString()));
                } catch (IllegalArgumentException e) {
                    raiseError("Invalid filename");

                    e.printStackTrace();
                    LOGGER.error("Failed to parse UUID from filename '{}', further disk operations have been disabled", FilenameUtils.removeExtension(path.toString()));
                    return;
                }

                var list = new ArrayList<DeathInfo>();
                load(path.toFile(), list).join();
                deathInfos.put(uuid, list);
            });
        } catch (IOException | IllegalArgumentException e) {
            raiseError("Unknown problem");

            e.printStackTrace();
            LOGGER.error("Failed to load DeathLog database, further disk operations have been disabled");
        }
    }

    @Override
    public List<DeathInfo> getDeathInfoList(UUID profile) {
        return deathInfos.getOrDefault(profile, new ArrayList<>());
    }

    @Override
    public void delete(DeathInfo info, UUID profile) {
        deathInfos.get(profile).remove(info);
        save(deathLogDir.resolve(profile.toString() + ".dat").toFile(), deathInfos.get(profile));
    }

    @Override
    public void store(Text deathMessage, PlayerEntity player) {
        final DeathInfo deathInfo = new DeathInfo();

        deathInfo.setProperty(DeathInfo.INVENTORY_KEY, new InventoryProperty(player.getInventory()));

        deathInfo.setProperty(DeathInfo.COORDINATES_KEY, new CoordinatesProperty(player.getBlockPos()));
        deathInfo.setProperty(DeathInfo.DIMENSION_KEY, new StringProperty("deathlog.deathinfoproperty.dimension", player.world.getRegistryKey().getValue().toString()));
        deathInfo.setProperty(DeathInfo.LOCATION_KEY, new LocationProperty("Server", true));
        deathInfo.setProperty(DeathInfo.SCORE_KEY, new ScoreProperty(player.getScore(), player.experienceLevel, player.experienceProgress, player.totalExperience));
        deathInfo.setProperty(DeathInfo.DEATH_MESSAGE_KEY, new StringProperty("deathlog.deathinfoproperty.death_message", deathMessage.getString()));
        deathInfo.setProperty(DeathInfo.TIME_OF_DEATH_KEY, new StringProperty("deathlog.deathinfoproperty.time_of_death", new Date().toString()));

        SpecialPropertyProvider.apply(deathInfo, player);
        DeathInfoCreatedCallback.EVENT.invoker().event(deathInfo);

        deathInfos.computeIfAbsent(player.getUuid(), uuid -> new ArrayList<>()).add(deathInfo);
        save(deathLogDir.resolve(player.getUuid().toString() + ".dat").toFile(), deathInfos.get(player.getUuid()));
    }

    @Override
    public void restore(int index, @Nullable UUID profile) {
        //NO-OP
    }
}
