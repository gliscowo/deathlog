package com.glisco.deathlog.death_info.properties;

import com.glisco.deathlog.death_info.DeathInfoProperty;
import com.glisco.deathlog.death_info.DeathInfoPropertyType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class MissingDeathInfoProperty implements DeathInfoProperty {

    private final Type type;
    private final NbtCompound data;

    public MissingDeathInfoProperty(Type type, NbtCompound data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public DeathInfoPropertyType<?> getType() {
        return this.type;
    }

    @Override
    public Text formatted() {
        return Text.empty();
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.copyFrom(this.data);
    }

    @Override
    public String toSearchableString() {
        return null;
    }

    public static class Type extends DeathInfoPropertyType<MissingDeathInfoProperty> {

        public Type(String id) {
            super(null, id);
        }

        @Override
        public boolean displayedInInfoView() {
            return false;
        }

        @Override
        public MissingDeathInfoProperty readFromNbt(NbtCompound nbt) {
            return new MissingDeathInfoProperty(this, nbt);
        }
    }

}
