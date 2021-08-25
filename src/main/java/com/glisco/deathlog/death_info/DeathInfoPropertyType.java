package com.glisco.deathlog.death_info;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public abstract class DeathInfoPropertyType<P extends DeathInfoProperty> {

    private final TranslatableText name;
    private final String id;

    public DeathInfoPropertyType(String translationKey, String id) {
        this.name = new TranslatableText(translationKey);
        this.id = id;
    }

    public String getLocalizedName() {
        return name.getString();
    }

    public static Text decorateName(String name) {
        return new LiteralText("§9" + name + "§r");
    }

    public String getId() {
        return id;
    }

    public abstract boolean displayedInInfoView();

    public abstract P readFromNbt(NbtCompound nbt);

}
