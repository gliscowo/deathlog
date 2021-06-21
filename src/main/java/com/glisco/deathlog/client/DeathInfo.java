package com.glisco.deathlog.client;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtTypes;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;

public class DeathInfo {

    //TODO move these to properties
    private DefaultedList<ItemStack> playerItems;
    private DefaultedList<ItemStack> playerArmor;
    private Map<DeathInfoProperty.Type, DeathInfoProperty> properties;

    public DeathInfo() {
        this.playerItems = DefaultedList.ofSize(37, ItemStack.EMPTY);
        this.playerArmor = DefaultedList.ofSize(4, ItemStack.EMPTY);
        this.properties = new HashMap<>();
    }

    public static DeathInfo readFromNbt(NbtCompound nbt) {

        final DeathInfo deathInfo = new DeathInfo();

        final NbtList armorList = nbt.getList("Armor", NbtType.COMPOUND);
        for (int i = 0; i < armorList.size(); i++) {
            deathInfo.playerArmor.set(i, ItemStack.fromNbt(armorList.getCompound(i)));
        }

        final NbtList itemList = nbt.getList("Items", NbtType.COMPOUND);
        for (int i = 0; i < itemList.size(); i++) {
            deathInfo.playerItems.set(i, ItemStack.fromNbt(itemList.getCompound(i)));
        }

        final NbtCompound propertyNbt = nbt.getCompound("Properties");
        propertyNbt.getKeys().forEach(s -> deathInfo.setProperty(DeathInfoProperty.Type.valueOf(s), new DeathInfoProperty(propertyNbt.getString(s))));

        return deathInfo;

    }

    public NbtCompound writeNbt() {
        final NbtCompound nbt = new NbtCompound();

        final NbtList armorNbt = new NbtList();
        playerArmor.forEach(stack -> armorNbt.add(stack.writeNbt(new NbtCompound())));
        nbt.put("Armor", armorNbt);

        final NbtList inventoryNbt = new NbtList();
        playerItems.forEach(stack -> inventoryNbt.add(stack.writeNbt(new NbtCompound())));
        nbt.put("Items", inventoryNbt);

        final NbtCompound propertyNbt = new NbtCompound();
        properties.forEach((type, deathInfoProperty) -> propertyNbt.putString(type.name(), deathInfoProperty.data()));

        nbt.put("Properties", propertyNbt);

        return nbt;
    }

    public void setProperty(DeathInfoProperty.Type property, DeathInfoProperty value) {
        this.properties.put(property, value);
    }

    public Optional<DeathInfoProperty> getProperty(DeathInfoProperty.Type property) {
        return Optional.ofNullable(properties.get(property));
    }

    public void loadItems(PlayerInventory playerInventory) {
        for (int i = 0; i < playerArmor.size(); i++) {
            playerArmor.set(i, playerInventory.armor.get(i).copy());
        }
        for (int i = 0; i < 36; i++) {
            playerItems.set(i, playerInventory.main.get(i).copy());
        }
        playerItems.set(36, playerInventory.offHand.get(0));
    }

    public Text getListName() {
        return Text.of(getProperty(DeathInfoProperty.Type.TIME_OF_DEATH).orElse(new DeathInfoProperty("TIME_MISSING")).data());
    }

    public Text getTitle() {
        return Text.of(getProperty(DeathInfoProperty.Type.DEATH_MESSAGE).orElse(DeathInfoProperty.FALLBACK).data());
    }

    public List<Text> getLeftColumnText() {
        final ArrayList<Text> texts = new ArrayList<>();
        DeathInfoProperty.DISPLAY_SCHEMA.forEach((type, propertyTypeProcessorPropertyDataProcessorPair) -> {
            if (!getProperty(type).isPresent()) return;
            texts.add(propertyTypeProcessorPropertyDataProcessorPair.getLeft().processType(type));
        });

        return texts;
    }

    public String createSearchString() {
        final StringBuilder builder = new StringBuilder();

        for (DeathInfoProperty.Type type : DeathInfoProperty.Type.values()) {
            if (!getProperty(type).isPresent()) continue;
            builder.append(getProperty(type).get().data());
        }

        for (ItemStack stack : playerItems) {
            if (stack.isEmpty()) continue;
            builder.append(stack.getName().getString());
        }

        for (ItemStack stack : playerArmor) {
            if (stack.isEmpty()) continue;
            builder.append(stack.getName().getString());
        }

        return builder.toString().toLowerCase();
    }

    public List<Text> getRightColumnText() {
        final ArrayList<Text> texts = new ArrayList<>();
        DeathInfoProperty.DISPLAY_SCHEMA.forEach((type, propertyTypeProcessorPropertyDataProcessorPair) -> {
            if (!getProperty(type).isPresent()) return;
            texts.add(propertyTypeProcessorPropertyDataProcessorPair.getRight().processData(getProperty(type).get()));
        });

        return texts;
    }

    public DefaultedList<ItemStack> getPlayerArmor() {
        return playerArmor;
    }

    public DefaultedList<ItemStack> getPlayerItems() {
        return playerItems;
    }
}
