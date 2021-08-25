package com.glisco.deathlog.death_info;

import com.glisco.deathlog.death_info.properties.*;
import net.minecraft.nbt.NbtCompound;

import java.util.LinkedHashMap;
import java.util.Map;

public class DeathInfoPropertySerializer {

    private static final Map<String, DeathInfoPropertyType<?>> TYPES = new LinkedHashMap<>();

    static {
        TYPES.put("inventory", InventoryProperty.Type.INSTANCE);
        TYPES.put("coordinates", CoordinatesProperty.Type.INSTANCE);
        TYPES.put("location", LocationProperty.Type.INSTANCE);
        TYPES.put("score", ScoreProperty.Type.INSTANCE);
        TYPES.put("string", StringProperty.Type.INSTANCE);
    }

    public static NbtCompound save(DeathInfoProperty property) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("Type", property.getType().getId());
        property.writeNbt(nbt);
        return nbt;
    }

    public static DeathInfoProperty load(NbtCompound propertyNbt) {
        String type = propertyNbt.getString("Type");
        return TYPES.get(type).readFromNbt(propertyNbt);
    }

}
