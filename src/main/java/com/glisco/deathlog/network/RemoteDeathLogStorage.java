package com.glisco.deathlog.network;

import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.storage.BaseDeathLogStorage;
import com.glisco.deathlog.storage.SingletonDeathLogStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class RemoteDeathLogStorage extends BaseDeathLogStorage implements SingletonDeathLogStorage {

    private final List<DeathInfo> deathInfoList;

    public RemoteDeathLogStorage(List<DeathInfo> deathInfoList) {
        this.deathInfoList = deathInfoList;
    }

    public static RemoteDeathLogStorage read(PacketByteBuf buffer) {
        var infos = buffer.readList(packetByteBuf -> DeathInfo.readFromNbt(packetByteBuf.readNbt()));
        return new RemoteDeathLogStorage(infos);
    }

    @Override
    public List<DeathInfo> getDeathInfoList(@Nullable UUID player) {
        return deathInfoList;
    }

    @Override
    public void delete(DeathInfo info, @Nullable UUID player) {

    }

    @Override
    public void store(Text deathMessage, PlayerEntity player) {
        //NO-OP
    }

    @Override
    public String getDefaultFilter() {
        return "Server";
    }
}
