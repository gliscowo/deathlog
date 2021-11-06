package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.storage.SingletonDeathLogStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;

import java.util.Objects;

public class DeathListWidget extends AlwaysSelectedEntryListWidget<DeathListEntry> {

    private final SingletonDeathLogStorage storage;
    private String filter;
    public boolean restoreEnabled;

    public DeathListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight, SingletonDeathLogStorage storage) {
        super(client, width, height, top, bottom, itemHeight);

        this.storage = storage;
        this.filter("");

        this.restoreEnabled = client.player.hasPermissionLevel(4);
    }

    public void deleteInfoFromStorage(DeathInfo info) {
        storage.delete(info);
    }

    public void restoreInfo(DeathInfo info) {
        storage.restore(storage.getDeathInfoList().indexOf(info));
    }

    public boolean filter(String pattern) {
        pattern = pattern.toLowerCase();

        if (Objects.equals(filter, pattern)) return getEntryCount() != 0;
        this.filter = pattern;
        return refilter();
    }

    public boolean refilter() {
        this.clearEntries();
        if (filter.isBlank()) {
            storage.getDeathInfoList().forEach(info -> addEntry(new DeathListEntry(this, info)));
        } else {
            storage.getDeathInfoList().forEach(info -> {
                if (info.createSearchString().contains(filter)) addEntry(new DeathListEntry(this, info));
            });
        }

        return getEntryCount() != 0;
    }

}
