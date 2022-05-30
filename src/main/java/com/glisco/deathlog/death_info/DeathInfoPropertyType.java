package com.glisco.deathlog.death_info;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public abstract class DeathInfoPropertyType<P extends DeathInfoProperty> {

    private final String translationKey;
    private final String id;

    public DeathInfoPropertyType(String translationKey, String id) {
        this.translationKey = translationKey;
        this.id = id;
    }

    public MutableText getName() {
        return Text.translatable(translationKey);
    }

    public static Text decorateName(MutableText name) {
        return name.formatted(Formatting.BLUE);
    }

    public String getId() {
        return id;
    }

    public abstract boolean displayedInInfoView();

    public abstract P readFromNbt(NbtCompound nbt);

}
