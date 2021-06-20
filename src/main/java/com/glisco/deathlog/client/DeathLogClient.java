package com.glisco.deathlog.client;

import com.glisco.deathlog.client.gui.DeathLogScreen;
import com.glisco.deathlog.mixin.MinecraftServerAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DeathLogClient implements ClientModInitializer {

    private static final List<DeathInfo> DEATH_INFOS = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        loadFromFile();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if(!(screen instanceof StatsScreen)) return;
            Screens.getButtons(screen).add(new ButtonWidget(10, 5, 60, 20, Text.of("DeathLog"), button -> {
                client.openScreen(new DeathLogScreen(screen));
            }));
        });

    }

    public static void deleteDeathInfo(DeathInfo info){
        DEATH_INFOS.remove(info);
        saveToFile();
    }

    public static void captureDeathInfo(DeathMessageS2CPacket deathPacket) {
        final var deathInfo = new DeathInfo();
        final var client = MinecraftClient.getInstance();
        final var player = client.player;

        deathInfo.loadItems(player.getInventory());

        deathInfo.setProperty(DeathInfoProperty.Type.TIME_OF_DEATH, new DeathInfoProperty(new Date().toString()));
        deathInfo.setProperty(DeathInfoProperty.Type.DEATH_MESSAGE, new DeathInfoProperty(deathPacket.getMessage().getString()));

        final var coords = "§c" + player.getBlockX() + " §a" + player.getBlockY() + " §b" + player.getBlockZ();
        deathInfo.setProperty(DeathInfoProperty.Type.COORDINATES, new DeathInfoProperty(coords));

        deathInfo.setProperty(DeathInfoProperty.Type.DIMENSION, new DeathInfoProperty(player.world.getRegistryKey().getValue().toString()));
        deathInfo.setProperty(DeathInfoProperty.Type.SCORE, new DeathInfoProperty(String.valueOf(player.getScore())));

        if (client.isInSingleplayer()) {
            deathInfo.setProperty(DeathInfoProperty.Type.LOCATION, new DeathInfoProperty(((MinecraftServerAccessor)client.getServer()).deathlog_getSession().getDirectoryName() + " §7(Singleplayer)"));
        } else {
            deathInfo.setProperty(DeathInfoProperty.Type.LOCATION, new DeathInfoProperty(client.getCurrentServerEntry().name + " §7(Server)"));
        }

        DEATH_INFOS.add(deathInfo);
        saveToFile();
    }

    public static void loadFromFile() {
        File deathFile = new File(FabricLoader.getInstance().getGameDir().resolve("deaths.dat").toString());
        NbtCompound deathNbt;

        if(deathFile.exists()){
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
        final var infoList = deathNbt.getList("Deaths", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < infoList.size(); i++) {
            DEATH_INFOS.add(DeathInfo.readFromNbt(infoList.getCompound(i)));
        }

    }

    public static void saveToFile() {
        final var deathNbt = new NbtCompound();
        final var infoList = new NbtList();

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
