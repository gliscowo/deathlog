package com.glisco.deathlog;

import com.glisco.deathlog.death_info.DeathInfoPropertySerializer;
import com.glisco.deathlog.death_info.SpecialPropertyProvider;
import com.glisco.deathlog.death_info.properties.TrinketComponentProperty;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class DeathLogCommon implements ModInitializer {

    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            SpecialPropertyProvider.register(TrinketComponentProperty::apply);
            DeathInfoPropertySerializer.register(TrinketComponentProperty.Type.INSTANCE.getId(), TrinketComponentProperty.Type.INSTANCE);
        }
    }

}
