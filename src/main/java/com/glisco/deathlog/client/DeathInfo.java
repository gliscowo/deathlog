package com.glisco.deathlog.client;

import com.glisco.deathlog.death_info.DeathInfoProperty;
import com.glisco.deathlog.death_info.DeathInfoPropertySerializer;
import com.glisco.deathlog.death_info.properties.InventoryProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;
import java.util.function.Consumer;

public class DeathInfo {

    public static final String COORDINATES_KEY = "coordinates";
    public static final String DIMENSION_KEY = "dimension";
    public static final String LOCATION_KEY = "location";
    public static final String SCORE_KEY = "score";
    public static final String DEATH_MESSAGE_KEY = "death_message";
    public static final String TIME_OF_DEATH_KEY = "time_of_death";
    public static final String INVENTORY_KEY = "inventory";

    private final Map<String, DeathInfoProperty> properties;

    public DeathInfo() {
        this.properties = new LinkedHashMap<>();
    }

    public static DeathInfo readFromNbt(NbtCompound nbt) {
        final DeathInfo deathInfo = new DeathInfo();
        nbt.getKeys().forEach(s -> deathInfo.setProperty(s, DeathInfoPropertySerializer.load(nbt.getCompound(s))));
        return deathInfo;
    }

    public NbtCompound writeNbt() {
        final NbtCompound nbt = new NbtCompound();
        properties.forEach((s, property) -> nbt.put(s, DeathInfoPropertySerializer.save(property)));
        return nbt;
    }

    public void setProperty(String property, DeathInfoProperty value) {
        this.properties.put(property, value);
    }

    public Optional<DeathInfoProperty> getProperty(String property) {
        return Optional.ofNullable(properties.get(property));
    }

    public Text getListName() {
        DeathInfoProperty property = getProperty(TIME_OF_DEATH_KEY).orElse(null);
        return property == null ? new LiteralText("Time missing") : property.formatted();
    }

    public Text getTitle() {
        DeathInfoProperty property = getProperty(DEATH_MESSAGE_KEY).orElse(null);
        return property == null ? new LiteralText("Death message missing") : property.formatted();
    }

    public List<Text> getLeftColumnText() {
        final var texts = new ArrayList<Text>();
        iterateDisplayProperties(property -> texts.add(property.getName()));
        return texts;
    }

    public List<Text> getRightColumnText() {
        final var texts = new ArrayList<Text>();
        iterateDisplayProperties(property -> texts.add(property.formatted()));
        return texts;
    }

    public String createSearchString() {
        final StringBuilder builder = new StringBuilder();
        properties.forEach((s, property) -> builder.append(property.toSearchableString()));
        return builder.toString().toLowerCase();
    }

    private void iterateDisplayProperties(Consumer<DeathInfoProperty> callback) {
        properties.forEach((s, property) -> {
            if (!property.getType().displayedInInfoView()) return;

            callback.accept(property);
        });
    }

    public DefaultedList<ItemStack> getPlayerArmor() {
        var propertyOptional = getProperty(INVENTORY_KEY);
        if (propertyOptional.isEmpty()) return DefaultedList.of();
        return ((InventoryProperty) propertyOptional.get()).getPlayerArmor();
    }

    public DefaultedList<ItemStack> getPlayerItems() {
        var propertyOptional = getProperty(INVENTORY_KEY);
        if (propertyOptional.isEmpty()) return DefaultedList.of();
        return ((InventoryProperty) propertyOptional.get()).getPlayerItems();
    }
}
