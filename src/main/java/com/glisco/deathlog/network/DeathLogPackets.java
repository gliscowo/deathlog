package com.glisco.deathlog.network;

import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.client.DeathLogClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

public class DeathLogPackets {

    public static class Client {

        public static void registerListeners() {
            ClientPlayNetworking.registerGlobalReceiver(Server.OPEN_SCREEN_ID, Client::handleOpenScreen);
        }

        private static void handleOpenScreen(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
            var storage = RemoteDeathLogStorage.read(packetByteBuf);
            minecraftClient.execute(() -> DeathLogClient.openScreen(storage));
        }

    }

    public static class Server {

        public static final Identifier OPEN_SCREEN_ID = new Identifier("deathlog", "open_screen");

        public static void openScreen(List<DeathInfo> infos, ServerPlayerEntity target) {
            var buffer = PacketByteBufs.create();
            buffer.writeCollection(infos, (packetByteBuf, info) -> buffer.writeNbt(info.writeNbt()));

            ServerPlayNetworking.send(target, OPEN_SCREEN_ID, buffer);
        }

    }

}
