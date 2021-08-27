package com.glisco.deathlog.death_info;

import com.glisco.deathlog.client.DeathInfo;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SpecialPropertyProvider {

    private static final List<BiConsumer<DeathInfo, PlayerEntity>> applyFunctions = new ArrayList<>();

    public static void register(BiConsumer<DeathInfo, PlayerEntity> applyFunction) {
        applyFunctions.add(applyFunction);
    }

    public static void apply(DeathInfo info, PlayerEntity player) {
        applyFunctions.forEach(deathInfoConsumer -> deathInfoConsumer.accept(info, player));
    }

}
