package com.glisco.deathlog.death_info.properties;

import com.glisco.deathlog.death_info.DeathInfoProperty;
import com.glisco.deathlog.death_info.DeathInfoPropertyType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class ScoreProperty implements DeathInfoProperty {

    private final int score;
    private final int levels;
    private final int xp;

    public ScoreProperty(int score, int level, int xp) {
        this.score = score;
        this.levels = level;
        this.xp = xp;
    }

    @Override
    public DeathInfoPropertyType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public Text formatted() {
        return new LiteralText(score + " §7(" + levels + " levels, " + xp + " xp)");
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("Score", score);
        nbt.putInt("Levels", levels);
        nbt.putInt("XP", xp);
    }

    @Override
    public String toSearchableString() {
        return xp + " " + levels;
    }

    public static class Type extends DeathInfoPropertyType<ScoreProperty> {

        public static final Type INSTANCE = new Type();

        private Type() {
            super("deathlog.deathinfoproperty.score", "score");
        }

        @Override
        public boolean displayedInInfoView() {
            return true;
        }

        @Override
        public ScoreProperty readFromNbt(NbtCompound nbt) {

            int score = nbt.getInt("Score");
            int levels = nbt.getInt("Levels");
            int xp = nbt.getInt("XP");

            return new ScoreProperty(score, levels, xp);
        }
    }
}
