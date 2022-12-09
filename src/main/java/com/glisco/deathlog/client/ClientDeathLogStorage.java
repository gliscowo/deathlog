package com.glisco.deathlog.client;

import com.glisco.deathlog.death_info.SpecialPropertyProvider;
import com.glisco.deathlog.death_info.properties.*;
import com.glisco.deathlog.mixin.MinecraftServerAccessor;
import com.glisco.deathlog.network.DeathLogPackets;
import com.glisco.deathlog.storage.BaseDeathLogStorage;
import com.glisco.deathlog.storage.DeathInfoCreatedCallback;
import com.glisco.deathlog.storage.DirectDeathLogStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ClientDeathLogStorage extends BaseDeathLogStorage implements DirectDeathLogStorage {

    private final List<DeathInfo> deathInfos;
    private final File deathLogFile;

    public ClientDeathLogStorage() {
        this.deathLogFile = FabricLoader.getInstance().getGameDir().resolve("deaths.dat").toFile();
        this.deathInfos = load(deathLogFile).join();
    }

    @Override
    public List<DeathInfo> getDeathInfoList(@Nullable UUID profile) {
        return deathInfos;
    }

    @Override
    public void delete(DeathInfo info, @Nullable UUID profile) {
        deathInfos.remove(info);
        save(deathLogFile, deathInfos);
    }

    @Override
    public void store(Text deathMessage, PlayerEntity player) {
        final DeathInfo deathInfo = new DeathInfo();
        final MinecraftClient client = MinecraftClient.getInstance();

        deathInfo.setProperty(DeathInfo.INVENTORY_KEY, new InventoryProperty(player.getInventory()));

        deathInfo.setProperty(DeathInfo.COORDINATES_KEY, new CoordinatesProperty(player.getBlockPos()));
        deathInfo.setProperty(DeathInfo.DIMENSION_KEY, new StringProperty("deathlog.deathinfoproperty.dimension", player.world.getRegistryKey().getValue().toString()));

        if (client.isInSingleplayer()) {
            deathInfo.setProperty(DeathInfo.LOCATION_KEY, new LocationProperty(((MinecraftServerAccessor) client.getServer()).deathlog_getSession().getDirectoryName(), false));
        } else {
            deathInfo.setProperty(DeathInfo.LOCATION_KEY, new LocationProperty(client.getCurrentServerEntry().name, true));
        }

        deathInfo.setProperty(DeathInfo.SCORE_KEY, new ScoreProperty(player.getScore(), player.experienceLevel, player.experienceProgress, player.totalExperience));
        deathInfo.setProperty(DeathInfo.DEATH_MESSAGE_KEY, new StringProperty("deathlog.deathinfoproperty.death_message", deathMessage.getString()));
        deathInfo.setProperty(DeathInfo.TIME_OF_DEATH_KEY, new StringProperty("deathlog.deathinfoproperty.time_of_death", new Date().toString()));

        SpecialPropertyProvider.apply(deathInfo, player);
        DeathInfoCreatedCallback.EVENT.invoker().event(deathInfo);

        deathInfos.add(deathInfo);
        save(deathLogFile, deathInfos);
    }

    @Override
    public void restore(int index, @Nullable UUID profile) {
        DeathLogPackets.Client.requestRestore(MinecraftClient.getInstance().player.getUuid(), index);
    }

    @Override
    public String getDefaultFilter() {
        var client = MinecraftClient.getInstance();
        if (client.getCurrentServerEntry() != null) {
            return client.getCurrentServerEntry().name;
        } else if (client.isInSingleplayer()) {
            return ((MinecraftServerAccessor) client.getServer()).deathlog_getSession().getDirectoryName();
        }

        return "";
    }
}
