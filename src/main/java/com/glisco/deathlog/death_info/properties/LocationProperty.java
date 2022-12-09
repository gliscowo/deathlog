package com.glisco.deathlog.death_info.properties;

import com.glisco.deathlog.death_info.DeathInfoProperty;
import com.glisco.deathlog.death_info.DeathInfoPropertyType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class LocationProperty implements DeathInfoProperty {

    private final String location;
    private final boolean multiplayer;

    public LocationProperty(String location, boolean multiplayer) {
        this.location = location;
        this.multiplayer = multiplayer;
    }

    @Override
    public DeathInfoPropertyType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public Text formatted() {
        return Text.translatable(
                "deathlog.deathinfoproperty.location.value", location,
                multiplayer
                        ? Text.translatable("deathlog.deathinfoproperty.location.multiplayer")
                        : Text.translatable("deathlog.deathinfoproperty.location.singleplayer")
        );
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.putString("Location", location);
        nbt.putBoolean("Multiplayer", multiplayer);
    }

    @Override
    public String toSearchableString() {
        return location;
    }

    public static class Type extends DeathInfoPropertyType<LocationProperty> {

        public static final Type INSTANCE = new Type();

        private Type() {
            super("deathlog.deathinfoproperty.location", "location");
        }

        @Override
        public boolean displayedInInfoView() {
            return true;
        }

        @Override
        public LocationProperty readFromNbt(NbtCompound nbt) {
            String location = nbt.getString("Location");
            boolean multiplayer = nbt.getBoolean("Multiplayer");
            return new LocationProperty(location, multiplayer);
        }
    }
}
