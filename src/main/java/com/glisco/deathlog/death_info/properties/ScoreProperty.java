package com.glisco.deathlog.death_info.properties;

import com.glisco.deathlog.death_info.DeathInfoPropertyType;
import com.glisco.deathlog.death_info.RestorableDeathInfoProperty;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ScoreProperty implements RestorableDeathInfoProperty {

    private final int score;
    private final int levels;
    private final float progress;

    private final int xp;

    public ScoreProperty(int score, int level, float progress, int xp) {
        this.score = score;
        this.levels = level;
        this.progress = progress;
        this.xp = xp;
    }

    @Override
    public DeathInfoPropertyType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public Text formatted() {
        return Text.translatable(
                "deathlog.deathinfoproperty.score.value",
                score, levels, xp
        );
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("Score", score);
        nbt.putInt("Levels", levels);
        nbt.putFloat("Progress", progress);
        nbt.putInt("XP", xp);
    }

    @Override
    public String toSearchableString() {
        return xp + " " + levels;
    }

    @Override
    public void restore(ServerPlayerEntity player) {
        player.experienceProgress = progress;
        player.setExperienceLevel(levels);
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
            float progress = nbt.getFloat("Progress");
            int xp = nbt.getInt("XP");

            return new ScoreProperty(score, levels, progress, xp);
        }
    }
}
