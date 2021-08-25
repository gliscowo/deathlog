package com.glisco.deathlog.death_info.properties;

import com.glisco.deathlog.death_info.DeathInfoProperty;
import com.glisco.deathlog.death_info.DeathInfoPropertyType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class CoordinatesProperty implements DeathInfoProperty {

    private final BlockPos coordinates;

    public CoordinatesProperty(BlockPos coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public DeathInfoPropertyType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public Text formatted() {
        return new LiteralText("§c" + coordinates.getX() + " §a" + coordinates.getY() + " §b" + coordinates.getZ());
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.putLong("Coordinates", coordinates.asLong());
    }

    @Override
    public String toSearchableString() {
        return coordinates.getX() + " " + coordinates.getY() + " " + coordinates.getZ();
    }

    public static class Type extends DeathInfoPropertyType<CoordinatesProperty> {

        public static final Type INSTANCE = new Type();

        private Type() {
            super("deathlog.deathinfoproperty.coordinates", "coordinates");
        }

        @Override
        public boolean displayedInInfoView() {
            return true;
        }

        @Override
        public CoordinatesProperty readFromNbt(NbtCompound nbt) {
            BlockPos location = BlockPos.fromLong(nbt.getLong("Coordinates"));
            return new CoordinatesProperty(location);
        }
    }
}
