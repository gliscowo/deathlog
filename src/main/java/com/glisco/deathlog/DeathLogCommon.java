package com.glisco.deathlog;

import com.glisco.deathlog.death_info.DeathInfoPropertySerializer;
import com.glisco.deathlog.death_info.SpecialPropertyProvider;
import com.glisco.deathlog.death_info.properties.TrinketComponentProperty;
import com.glisco.deathlog.network.DeathLogPackets;
import com.glisco.deathlog.storage.DeathLogStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class DeathLogCommon implements ModInitializer {

    private static DeathLogStorage currentStorage = null;

    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            SpecialPropertyProvider.register(TrinketComponentProperty::apply);
            DeathInfoPropertySerializer.register(TrinketComponentProperty.Type.INSTANCE.getId(), TrinketComponentProperty.Type.INSTANCE);
        }

        DeathLogPackets.Server.registerCommonListeners();
    }

    public static void setStorage(DeathLogStorage storage) {
        if (DeathLogCommon.currentStorage != null) throw new IllegalStateException("Storage has already been set!");
        if (storage == null) throw new IllegalArgumentException("Storage cannot be null!");
        DeathLogCommon.currentStorage = storage;
    }

    public static DeathLogStorage getStorage() {
        return currentStorage;
    }
}
