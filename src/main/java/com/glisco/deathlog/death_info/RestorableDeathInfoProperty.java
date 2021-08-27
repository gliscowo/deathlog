package com.glisco.deathlog.death_info;

import net.minecraft.server.network.ServerPlayerEntity;

public interface RestorableDeathInfoProperty extends DeathInfoProperty{

    void restore(ServerPlayerEntity player);

}
