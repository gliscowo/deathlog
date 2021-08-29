package com.glisco.deathlog.death_info.properties;

import com.glisco.deathlog.death_info.DeathInfoPropertyType;
import com.glisco.deathlog.death_info.RestorableDeathInfoProperty;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

public class InventoryProperty implements RestorableDeathInfoProperty {

    private final DefaultedList<ItemStack> playerItems;
    private final DefaultedList<ItemStack> playerArmor;

    public InventoryProperty(DefaultedList<ItemStack> playerItems, DefaultedList<ItemStack> playerArmor) {
        this.playerItems = playerItems;
        this.playerArmor = playerArmor;
    }

    public InventoryProperty(PlayerInventory playerInventory) {
        this.playerItems = DefaultedList.ofSize(37, ItemStack.EMPTY);
        this.playerArmor = DefaultedList.ofSize(4, ItemStack.EMPTY);

        copy(playerInventory.armor, playerArmor);
        copy(playerInventory.main, playerItems);

        playerItems.set(36, playerInventory.offHand.get(0).copy());
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

        playerItems.forEach(stack -> builder.append(stack.getName().getString()));
        playerArmor.forEach(stack -> builder.append(stack.getName().getString()));

        return builder.toString();
    }

    @Override
    public void restore(ServerPlayerEntity player) {
        final var inventory = player.getInventory();
        inventory.clear();

        copy(playerArmor, inventory.armor);
        copy(playerItems, inventory.main, 36);

        inventory.offHand.set(0, playerItems.get(36));
    }

    public DefaultedList<ItemStack> getPlayerArmor() {
        return playerArmor;
    }

    public DefaultedList<ItemStack> getPlayerItems() {
        return playerItems;
    }

    private static void copy(DefaultedList<ItemStack> list, DefaultedList<ItemStack> other) {
        copy(list, other, list.size());
    }

    private static void copy(DefaultedList<ItemStack> list, DefaultedList<ItemStack> other, int maxItems) {
        for (int i = 0; i < maxItems; i++) other.set(i, list.get(i).copy());
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
