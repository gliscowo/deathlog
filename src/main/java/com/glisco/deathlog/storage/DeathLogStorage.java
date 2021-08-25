package com.glisco.deathlog.storage;

import com.glisco.deathlog.client.DeathInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface DeathLogStorage {

    List<DeathInfo> getDeathInfoList(@Nullable UUID player);

    void delete(DeathInfo info, @Nullable UUID player);

    void store(Text deathMessage, PlayerEntity player);

    boolean isErrored();

    String getErrorCondition();
}
