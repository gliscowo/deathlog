package com.glisco.deathlog.client;

import com.glisco.deathlog.client.gui.DeathLogScreen;
import com.glisco.deathlog.network.DeathLogPackets;
import com.glisco.deathlog.storage.SingletonDeathLogStorage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.mixin.networking.accessor.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class DeathLogClient implements ClientModInitializer {

    public static final KeyBinding OPEN_DEATH_SCREEN = new KeyBinding("key.deathlog.death_screen", GLFW.GLFW_KEY_END, "key.categories.misc");
    private static ClientDeathLogStorage storage;

    @Override
    public void onInitializeClient() {

        storage = new ClientDeathLogStorage();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof StatsScreen)) return;
            Screens.getButtons(screen).add(new ButtonWidget(10, 5, 60, 20, Text.of("DeathLog"), button -> {
                openScreen(getClientStorage());
            }));
        });

        KeyBindingHelper.registerKeyBinding(OPEN_DEATH_SCREEN);
        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
            if (OPEN_DEATH_SCREEN.wasPressed()) {
                openScreen(getClientStorage());
            }
        });

        DeathLogPackets.Client.registerListeners();
    }

    public static void openScreen(SingletonDeathLogStorage storage) {
        MinecraftClient.getInstance().setScreen(new DeathLogScreen(MinecraftClient.getInstance().currentScreen, storage));
    }

    public static SingletonDeathLogStorage getClientStorage() {
        return storage;
    }
}
