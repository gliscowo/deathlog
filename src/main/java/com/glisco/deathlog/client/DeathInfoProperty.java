package com.glisco.deathlog.client;

import com.google.common.collect.ImmutableMap;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Pair;

public record DeathInfoProperty(String data) {

    public static final DeathInfoProperty FALLBACK = new DeathInfoProperty("INVALID");
    public static final ImmutableMap<Type, Pair<PropertyTypeProcessor, PropertyDataProcessor>> DISPLAY_SCHEMA;

    static {
        DISPLAY_SCHEMA = ImmutableMap.<Type, Pair<PropertyTypeProcessor, PropertyDataProcessor>>builder()
                .put(Type.COORDINATES, new Pair<>(DeathInfoProperty::processTypeDefault, data1 -> new LiteralText("§7[" + data1.data + "§7]")))
                .put(Type.DIMENSION, new Pair<>(DeathInfoProperty::processTypeDefault, DeathInfoProperty::processDataDefault))
                .put(Type.LOCATION, new Pair<>(DeathInfoProperty::processTypeDefault, DeathInfoProperty::processDataDefault))
                .put(Type.SCORE, new Pair<>(DeathInfoProperty::processTypeDefault, DeathInfoProperty::processDataDefault))
                .put(Type.DEATH_MESSAGE, new Pair<>(DeathInfoProperty::processTypeDefault, DeathInfoProperty::processDataDefault))
                .put(Type.TIME_OF_DEATH, new Pair<>(DeathInfoProperty::processTypeDefault, DeathInfoProperty::processDataDefault))
                .build();
    }

    private static Text processTypeDefault(Type type) {
        return new LiteralText("§9" + type.getTranslatedName() + "§r");
    }

    private static Text processDataDefault(DeathInfoProperty property) {
        return new LiteralText(property.data());
    }

    public enum Type {

        COORDINATES("deathlog.deathinfoproperty.coordinates"),
        DIMENSION("deathlog.deathinfoproperty.dimension"),
        LOCATION("deathlog.deathinfoproperty.location"),
        SCORE("deathlog.deathinfoproperty.score"),
        DEATH_MESSAGE("deathlog.deathinfoproperty.death_message"),
        TIME_OF_DEATH("deathlog.deathinfoproperty.time_of_death");

        private final MutableText name;

        Type(String translationKey) {
            this.name = new TranslatableText(translationKey);
        }

        public String getTranslatedName() {
            return name.getString();
        }

    }

    @FunctionalInterface
    interface PropertyTypeProcessor {
        Text processType(Type type);
    }

    @FunctionalInterface
    interface PropertyDataProcessor {
        Text processData(DeathInfoProperty data);
    }

}
