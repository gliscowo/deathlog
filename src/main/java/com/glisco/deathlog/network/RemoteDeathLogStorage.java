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
    private final UUID profileId;

    public RemoteDeathLogStorage(List<DeathInfo> deathInfoList, UUID profileId) {
        this.deathInfoList = deathInfoList;
        this.profileId = profileId;
    }

    public static RemoteDeathLogStorage read(PacketByteBuf buffer) {
        var infos = buffer.readList(DeathInfo::read);
        var id = buffer.readUuid();
        return new RemoteDeathLogStorage(infos, id);
    }

    @Override
    public List<DeathInfo> getDeathInfoList(@Nullable UUID profile) {
        return deathInfoList;
    }

    @Override
    public void delete(DeathInfo info, @Nullable UUID profile) {
        int index = deathInfoList.indexOf(info);
        DeathLogPackets.Client.requestDeletion(profileId, index);
        deathInfoList.remove(info);
    }

    @Override
    public void store(Text deathMessage, PlayerEntity player) {
        //NO-OP
    }

    @Override
    public void restore(int index, @Nullable UUID profile) {
        DeathLogPackets.Client.requestRestore(profileId, index);
    }

    public void fetchCompleteInfo(DeathInfo info) {
        if (!info.isPartial()) return;

        var idx = this.getDeathInfoList().indexOf(info);
        DeathLogPackets.Client.fetchInfo(profileId, idx);
    }

    @Override
    public String getDefaultFilter() {
        return "Server";
    }
}
