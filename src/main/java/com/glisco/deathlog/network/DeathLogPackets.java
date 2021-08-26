package com.glisco.deathlog.network;

import com.glisco.deathlog.client.DeathLogClient;
import com.glisco.deathlog.server.DeathLogServer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class DeathLogPackets {

    public static class Client {

        public static final Identifier REQUEST_DELETION_ID = new Identifier("deathlog", "request_deletion");

        public static void registerListeners() {
            ClientPlayNetworking.registerGlobalReceiver(Server.OPEN_SCREEN_ID, Client::handleOpenScreen);
        }

        private static void handleOpenScreen(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
            var storage = RemoteDeathLogStorage.read(packetByteBuf);
            minecraftClient.execute(() -> DeathLogClient.openScreen(storage));
        }

        public static void requestDeletion(UUID profile, int index) {
            var buffer = PacketByteBufs.create();
            buffer.writeUuid(profile);
            buffer.writeVarInt(index);
            ClientPlayNetworking.send(REQUEST_DELETION_ID, buffer);
        }

    }

    public static class Server {

        public static final Identifier OPEN_SCREEN_ID = new Identifier("deathlog", "open_screen");

        public static void registerListeners() {
            ServerPlayNetworking.registerGlobalReceiver(Client.REQUEST_DELETION_ID, Server::handleDelete);
        }

        private static void handleDelete(MinecraftServer minecraftServer, ServerPlayerEntity player, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf byteBuf, PacketSender packetSender) {
            var profileId = byteBuf.readUuid();
            var index = byteBuf.readVarInt();
            DeathLogServer.getStorage().delete(DeathLogServer.getStorage().getDeathInfoList(profileId).get(index), profileId);
        }

        public static void openScreen(UUID profileId, ServerPlayerEntity target) {
            var buffer = PacketByteBufs.create();
            var infos = DeathLogServer.getStorage().getDeathInfoList(profileId);
            buffer.writeCollection(infos, (packetByteBuf, info) -> info.write(packetByteBuf));
            buffer.writeUuid(profileId);

            ServerPlayNetworking.send(target, OPEN_SCREEN_ID, buffer);
        }

    }

}
