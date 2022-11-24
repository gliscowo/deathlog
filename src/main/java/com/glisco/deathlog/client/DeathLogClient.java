package com.glisco.deathlog.client;

import com.glisco.deathlog.DeathLogCommon;
import com.glisco.deathlog.client.gui.DeathLogScreen;
import com.glisco.deathlog.network.DeathLogPackets;
import com.glisco.deathlog.storage.SingletonDeathLogStorage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class DeathLogClient implements ClientModInitializer {

    public static final KeyBinding OPEN_DEATH_SCREEN = new KeyBinding("key.deathlog.death_screen", GLFW.GLFW_KEY_END, "key.categories.misc");
    private static ClientDeathLogStorage storage;

    @Override
    public void onInitializeClient() {

        storage = new ClientDeathLogStorage();
        DeathLogCommon.setStorage(storage);

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof StatsScreen)) return;

            Screens.getButtons(screen).add(
                    ButtonWidget.builder(Text.of("DeathLog"), button -> {
                                openScreen(getClientStorage());
                            }).position(10, 5)
                            .size(60, 20)
                            .build());
        });

        KeyBindingHelper.registerKeyBinding(OPEN_DEATH_SCREEN);
        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
            if (OPEN_DEATH_SCREEN.wasPressed()) {
                openScreen(getClientStorage());
            }
        });

        DeathLogPackets.Client.registerListeners();
        Config.load();
    }

    private void openScreen(SingletonDeathLogStorage clientStorage) {
        openScreen(clientStorage, MinecraftClient.getInstance().getCurrentServerEntry() == null);
    }

    public static void openScreen(SingletonDeathLogStorage storage, boolean canRestore) {
        final var screen = new DeathLogScreen(MinecraftClient.getInstance().currentScreen, storage);
        MinecraftClient.getInstance().setScreen(screen);
        if (!canRestore) screen.disableRestore();
    }

    public static SingletonDeathLogStorage getClientStorage() {
        return storage;
    }
}
