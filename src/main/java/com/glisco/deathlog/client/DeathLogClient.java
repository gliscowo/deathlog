package com.glisco.deathlog.client;

import com.glisco.deathlog.client.gui.DeathLogScreen;
import com.glisco.deathlog.mixin.MinecraftServerAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.util.NbtType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.CombatEventS2CPacket;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DeathLogClient implements ClientModInitializer {

    private static final List<DeathInfo> DEATH_INFOS = new ArrayList<>();

    public static final KeyBinding OPEN_DEATH_SCREEN = new KeyBinding("key.deathlog.death_screen", GLFW.GLFW_KEY_END, "key.categories.misc");

    @Override
    public void onInitializeClient() {
        loadFromFile();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof StatsScreen)) return;
            Screens.getButtons(screen).add(new ButtonWidget(10, 5, 60, 20, Text.of("DeathLog"), button -> {
                client.openScreen(new DeathLogScreen(screen));
            }));
        });

        KeyBindingHelper.registerKeyBinding(OPEN_DEATH_SCREEN);
        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
            if (OPEN_DEATH_SCREEN.wasPressed()) {
                minecraftClient.openScreen(new DeathLogScreen(minecraftClient.currentScreen));
            }
        });

    }

    public static void deleteDeathInfo(DeathInfo info) {
        DEATH_INFOS.remove(info);
        saveToFile();
    }

    public static void captureDeathInfo(CombatEventS2CPacket deathPacket) {
        final DeathInfo deathInfo = new DeathInfo();
        final MinecraftClient client = MinecraftClient.getInstance();
        final PlayerEntity player = client.player;

        deathInfo.loadItems(player.inventory);

        deathInfo.setProperty(DeathInfoProperty.Type.TIME_OF_DEATH, new DeathInfoProperty(new Date().toString()));
        deathInfo.setProperty(DeathInfoProperty.Type.DEATH_MESSAGE, new DeathInfoProperty(deathPacket.deathMessage.getString()));

        final String coords = "§c" + player.getBlockPos().getX() + " §a" + player.getBlockPos().getY() + " §b" + player.getBlockPos().getY();
        deathInfo.setProperty(DeathInfoProperty.Type.COORDINATES, new DeathInfoProperty(coords));

        deathInfo.setProperty(DeathInfoProperty.Type.DIMENSION, new DeathInfoProperty(player.world.getRegistryKey().getValue().toString()));
        deathInfo.setProperty(DeathInfoProperty.Type.SCORE, new DeathInfoProperty(String.valueOf(player.getScore())));

        if (client.isInSingleplayer()) {
            deathInfo.setProperty(DeathInfoProperty.Type.LOCATION, new DeathInfoProperty(((MinecraftServerAccessor) client.getServer()).deathlog_getSession().getDirectoryName() + " §7(Singleplayer)"));
        } else {
            deathInfo.setProperty(DeathInfoProperty.Type.LOCATION, new DeathInfoProperty(client.getCurrentServerEntry().name + " §7(Server)"));
        }

        DEATH_INFOS.add(deathInfo);
        saveToFile();
    }

    public static void loadFromFile() {
        File deathFile = new File(FabricLoader.getInstance().getGameDir().resolve("deaths.dat").toString());
        NbtCompound deathNbt;

        if (deathFile.exists()) {
            try {
                deathNbt = NbtIo.read(deathFile);
            } catch (IOException e) {
                deathNbt = new NbtCompound();
                System.err.println("Failed to read death info file");
                e.printStackTrace();
            }
        } else {
            deathNbt = new NbtCompound();
        }

        DEATH_INFOS.clear();
        final NbtList infoList = deathNbt.getList("Deaths", NbtType.COMPOUND);
        for (int i = 0; i < infoList.size(); i++) {
            DEATH_INFOS.add(DeathInfo.readFromNbt(infoList.getCompound(i)));
        }

    }

    public static void saveToFile() {
        final NbtCompound deathNbt = new NbtCompound();
        final NbtList infoList = new NbtList();

        DEATH_INFOS.forEach(deathInfo -> infoList.add(deathInfo.writeNbt()));

        deathNbt.put("Deaths", infoList);

        final File deathFile = new File(FabricLoader.getInstance().getGameDir().resolve("deaths.dat").toString());

        try {
            NbtIo.write(deathNbt, deathFile);
        } catch (IOException e) {
            System.err.println("Failed to write death info file");
            e.printStackTrace();
        }
    }

    public static List<DeathInfo> getDeathInfos() {
        return DEATH_INFOS;
    }
}
