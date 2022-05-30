package com.glisco.deathlog.death_info.properties;

import com.glisco.deathlog.death_info.DeathInfoProperty;
import com.glisco.deathlog.death_info.DeathInfoPropertyType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class StringProperty implements DeathInfoProperty {

    private final String translationKey;
    private final String data;

    public StringProperty(String translationKey, String data) {
        this.translationKey = translationKey;
        this.data = data;
    }

    @Override
    public DeathInfoPropertyType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public Text formatted() {
        return Text.literal(data);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.putString("TranslationKey", translationKey);
        nbt.putString("Data", data);
    }

    @Override
    public String toSearchableString() {
        return data;
    }

    @Override
    public Text getName() {
        return DeathInfoPropertyType.decorateName(Text.translatable(translationKey));
    }

    public static class Type extends DeathInfoPropertyType<StringProperty> {

        public static final Type INSTANCE = new Type();

        private Type() {super("deathlog.deathinfoproperty.string", "string");}

        @Override
        public boolean displayedInInfoView() {
            return true;
        }

        @Override
        public StringProperty readFromNbt(NbtCompound nbt) {
            String key = nbt.getString("TranslationKey");
            String data = nbt.getString("Data");

            return new StringProperty(key, data);
        }
    }
}
