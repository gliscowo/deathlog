package com.glisco.deathlog.network;

import com.glisco.deathlog.DeathLogCommon;
import com.glisco.deathlog.client.DeathLogClient;
import com.glisco.deathlog.server.DeathLogServer;
import com.glisco.deathlog.storage.BaseDeathLogStorage;
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
        public static final Identifier REQUEST_RESTORE_ID = new Identifier("deathlog", "request_restore");

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

        public static void requestRestore(UUID profile, int index) {
            var buffer = PacketByteBufs.create();
            buffer.writeUuid(profile);
            buffer.writeVarInt(index);
            ClientPlayNetworking.send(REQUEST_RESTORE_ID, buffer);
        }

    }

    public static class Server {

        public static final Identifier OPEN_SCREEN_ID = new Identifier("deathlog", "open_screen");

        public static void registerDedicatedListeners() {
            ServerPlayNetworking.registerGlobalReceiver(Client.REQUEST_DELETION_ID, Server::handleDelete);
        }

        public static void registerCommonListeners() {
            ServerPlayNetworking.registerGlobalReceiver(Client.REQUEST_RESTORE_ID, Server::handleRestore);
        }

        private static void handleRestore(MinecraftServer minecraftServer, ServerPlayerEntity player, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf byteBuf, PacketSender packetSender) {
            var profileId = byteBuf.readUuid();
            var index = byteBuf.readVarInt();

            minecraftServer.execute(() -> {
                if (!player.hasPermissionLevel(4)) {
                    BaseDeathLogStorage.LOGGER.warn("Received unauthorized restore packet");
                    return;
                }

                var targetPlayer = minecraftServer.getPlayerManager().getPlayer(profileId);
                if (targetPlayer == null) {
                    BaseDeathLogStorage.LOGGER.warn("Received restore packet for invalid player");
                    return;
                }

                DeathLogCommon.getStorage().getDeathInfoList(profileId).get(index).restore(targetPlayer);
            });
        }

        private static void handleDelete(MinecraftServer minecraftServer, ServerPlayerEntity player, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf byteBuf, PacketSender packetSender) {
            var profileId = byteBuf.readUuid();
            var index = byteBuf.readVarInt();

            minecraftServer.execute(() -> DeathLogServer.getStorage().delete(DeathLogServer.getStorage().getDeathInfoList(profileId).get(index), profileId));
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
