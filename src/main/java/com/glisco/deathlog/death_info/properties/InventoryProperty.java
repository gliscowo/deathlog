package com.glisco.deathlog.death_info.properties;

import com.glisco.deathlog.death_info.DeathInfoProperty;
import com.glisco.deathlog.death_info.DeathInfoPropertyType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

public class InventoryProperty implements DeathInfoProperty {

    private final DefaultedList<ItemStack> playerItems;
    private final DefaultedList<ItemStack> playerArmor;

    public InventoryProperty(DefaultedList<ItemStack> playerItems, DefaultedList<ItemStack> playerArmor) {
        this.playerItems = playerItems;
        this.playerArmor = playerArmor;
    }

    public InventoryProperty(PlayerInventory playerInventory) {
        this.playerItems = DefaultedList.ofSize(37, ItemStack.EMPTY);
        this.playerArmor = DefaultedList.ofSize(4, ItemStack.EMPTY);

        for (int i = 0; i < playerArmor.size(); i++) {
            playerArmor.set(i, playerInventory.armor.get(i).copy());
        }
        for (int i = 0; i < 36; i++) {
            playerItems.set(i, playerInventory.main.get(i).copy());
        }

        playerItems.set(36, playerInventory.offHand.get(0));
    }

    @Override
    public DeathInfoPropertyType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public Text formatted() {
        return null;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        final NbtList armorNbt = new NbtList();
        playerArmor.forEach(stack -> armorNbt.add(stack.writeNbt(new NbtCompound())));
        nbt.put("Armor", armorNbt);

        final NbtList inventoryNbt = new NbtList();
        playerItems.forEach(stack -> inventoryNbt.add(stack.writeNbt(new NbtCompound())));
        nbt.put("Items", inventoryNbt);
    }

    @Override
    public String toSearchableString() {
        StringBuilder builder = new StringBuilder();

        for (ItemStack stack : playerItems) {
            builder.append(stack.getName().getString());
        }

        for (ItemStack stack : playerArmor) {
            builder.append(stack.getName().getString());
        }

        return builder.toString();
    }

    public DefaultedList<ItemStack> getPlayerArmor() {
        return playerArmor;
    }

    public DefaultedList<ItemStack> getPlayerItems() {
        return playerItems;
    }

    public static class Type extends DeathInfoPropertyType<InventoryProperty> {

        public static final Type INSTANCE = new Type();

        private Type() {
            super("deathlog.deathinfoproperty.inventory", "inventory");
        }

        @Override
        public boolean displayedInInfoView() {
            return false;
        }

        @Override
        public InventoryProperty readFromNbt(NbtCompound nbt) {

            final NbtList armorNbt = nbt.getList("Armor", NbtElement.COMPOUND_TYPE);
            final var armorList = DefaultedList.ofSize(4, ItemStack.EMPTY);
            for (int i = 0; i < armorNbt.size(); i++) {
                armorList.set(i, ItemStack.fromNbt(armorNbt.getCompound(i)));
            }

            final NbtList itemNbt = nbt.getList("Items", NbtElement.COMPOUND_TYPE);
            final var itemList = DefaultedList.ofSize(37, ItemStack.EMPTY);
            for (int i = 0; i < itemNbt.size(); i++) {
                itemList.set(i, ItemStack.fromNbt(itemNbt.getCompound(i)));
            }

            return new InventoryProperty(itemList, armorList);
        }
    }
}
