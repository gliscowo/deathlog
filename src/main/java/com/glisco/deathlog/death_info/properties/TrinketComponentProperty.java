package com.glisco.deathlog.death_info.properties;

import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.death_info.DeathInfoPropertyType;
import com.glisco.deathlog.death_info.RestorableDeathInfoProperty;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

public class TrinketComponentProperty implements RestorableDeathInfoProperty {

    private final NbtCompound componentNbt;
    private final DefaultedList<ItemStack> trinkets;

    public TrinketComponentProperty(NbtCompound componentNbt, DefaultedList<ItemStack> trinkets) {
        this.componentNbt = componentNbt;
        this.trinkets = trinkets;
    }

    @Override
    public DeathInfoPropertyType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public Text formatted() {
        return new LiteralText("§b" + trinkets.size() + "§r items");
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.put("ComponentData", componentNbt);
        Inventories.writeNbt(nbt, trinkets);
    }

    @Override
    public String toSearchableString() {
        StringBuilder builder = new StringBuilder();
        trinkets.forEach(stack -> builder.append(stack.getName().getString()));
        return builder.toString();
    }

    @Override
    public void restore(ServerPlayerEntity player) {
        TrinketsApi.getTrinketComponent(player).get().readFromNbt(componentNbt);
    }

    public static void apply(DeathInfo info, PlayerEntity player) {
        final var trinketComponent = TrinketsApi.getTrinketComponent(player).get();
        var list = trinketComponent.getAllEquipped().stream().map(pair -> pair.getRight().copy()).toList();

        var nbt = new NbtCompound();
        trinketComponent.writeToNbt(nbt);

        info.setProperty("trinket_component", new TrinketComponentProperty(nbt, DefaultedList.copyOf(ItemStack.EMPTY, list.toArray(new ItemStack[0]))));
    }

    public static class Type extends DeathInfoPropertyType<TrinketComponentProperty> {

        public static final Type INSTANCE = new Type();

        private Type() {
            super("deathlog.deathinfoproperty.trinket_component", "trinket_component");
        }

        @Override
        public boolean displayedInInfoView() {
            return true;
        }

        @Override
        public TrinketComponentProperty readFromNbt(NbtCompound nbt) {
            var componentNbt = nbt.getCompound("ComponentData");

            var trinketList = DefaultedList.ofSize(nbt.getList("Items", NbtElement.COMPOUND_TYPE).size(), ItemStack.EMPTY);
            Inventories.readNbt(nbt, trinketList);

            return new TrinketComponentProperty(componentNbt, trinketList);
        }
    }
}
