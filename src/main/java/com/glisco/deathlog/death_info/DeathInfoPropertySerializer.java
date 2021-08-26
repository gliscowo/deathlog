package com.glisco.deathlog.death_info;

import com.glisco.deathlog.death_info.properties.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Pair;

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

    public static NbtCompound save(DeathInfoProperty property, String identifier) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("Type", property.getType().getId());
        nbt.putString("Identifier", identifier);
        property.writeNbt(nbt);
        return nbt;
    }

    public static Pair<DeathInfoProperty, String> load(NbtCompound propertyNbt) {
        String type = propertyNbt.getString("Type");
        String identifier = propertyNbt.getString("Identifier");
        return new Pair<>(TYPES.get(type).readFromNbt(propertyNbt), identifier);
    }

}
