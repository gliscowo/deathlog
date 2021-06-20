package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;

import java.util.List;
import java.util.Objects;

public class DeathListWidget extends AlwaysSelectedEntryListWidget<DeathListEntry> {

    private final List<DeathInfo> infos;
    private String filter;

    public DeathListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight, List<DeathInfo> infos) {
        super(client, width, height, top, bottom, itemHeight);

        this.infos = infos;
        this.filter("");
    }

    public boolean filter(String pattern) {
        pattern = pattern.toLowerCase();

        if(Objects.equals(filter, pattern)) return getEntryCount() != 0;
        this.filter = pattern;
        return refilter();
    }

    public boolean refilter(){
        this.clearEntries();
        if (filter.isBlank()) {
            infos.forEach(info -> addEntry(new DeathListEntry(this, info)));
        } else {
            infos.forEach(info -> {
                if (info.createSearchString().contains(filter)) addEntry(new DeathListEntry(this, info));
            });
        }

        return getEntryCount() != 0;
    }

}
