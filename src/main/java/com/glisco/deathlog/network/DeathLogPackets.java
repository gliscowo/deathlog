package com.glisco.deathlog.network;

import com.glisco.deathlog.DeathLogCommon;
import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.client.DeathLogClient;
import com.glisco.deathlog.client.gui.DeathLogScreen;
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
        public static final Identifier FETCH_INFO_ID = new Identifier("deathlog", "fetch_info");

        public static void registerListeners() {
            ClientPlayNetworking.registerGlobalReceiver(Server.OPEN_SCREEN_ID, Client::handleOpenScreen);
            ClientPlayNetworking.registerGlobalReceiver(Server.SEND_INFO_ID, Client::receiveInfo);
        }

        private static void receiveInfo(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf byteBuf, PacketSender packetSender) {
            var index = byteBuf.readVarInt();
            var info = DeathInfo.read(byteBuf);

            client.execute(() -> {
                if (!(client.currentScreen instanceof DeathLogScreen screen)) {
                    BaseDeathLogStorage.LOGGER.warn("Received invalid death info packet");
                    return;
                }

                screen.updateInfo(info, index);
            });
        }

        private static void handleOpenScreen(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
            var storage = RemoteDeathLogStorage.read(packetByteBuf);
            var canRestore = packetByteBuf.readBoolean();
            minecraftClient.execute(() -> DeathLogClient.openScreen(storage, canRestore));
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

        public static void fetchInfo(UUID profile, int index) {
            var buffer = PacketByteBufs.create();
            buffer.writeUuid(profile);
            buffer.writeVarInt(index);
            ClientPlayNetworking.send(FETCH_INFO_ID, buffer);
        }

    }

    public static class Server {

        public static final Identifier OPEN_SCREEN_ID = new Identifier("deathlog", "open_screen");
        public static final Identifier SEND_INFO_ID = new Identifier("deathlog", "send_info");

        public static void registerDedicatedListeners() {
            ServerPlayNetworking.registerGlobalReceiver(Client.REQUEST_DELETION_ID, Server::handleDelete);
        }

        public static void registerCommonListeners() {
            ServerPlayNetworking.registerGlobalReceiver(Client.REQUEST_RESTORE_ID, Server::handleRestore);
            ServerPlayNetworking.registerGlobalReceiver(Client.FETCH_INFO_ID, Server::sendInfo);
        }

        private static void sendInfo(MinecraftServer minecraftServer, ServerPlayerEntity player, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf byteBuf, PacketSender packetSender) {
            var profileId = byteBuf.readUuid();
            var index = byteBuf.readVarInt();

            minecraftServer.execute(() -> {
                if (!DeathLogServer.hasPermission(player, "deathlog.view")) {
                    BaseDeathLogStorage.LOGGER.warn("Received unauthorized info request from {}", player.getName().getString());
                    return;
                }

                var info = DeathLogCommon.getStorage().getDeathInfoList(profileId).get(index);
                var buffer = PacketByteBufs.create();
                buffer.writeVarInt(index);
                info.write(buffer);
                ServerPlayNetworking.send(player, SEND_INFO_ID, buffer);
            });
        }

        private static void handleRestore(MinecraftServer minecraftServer, ServerPlayerEntity player, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf byteBuf, PacketSender packetSender) {
            var profileId = byteBuf.readUuid();
            var index = byteBuf.readVarInt();

            minecraftServer.execute(() -> {
                if (!DeathLogServer.hasPermission(player, "deathlog.restore")) {
                    BaseDeathLogStorage.LOGGER.warn("Received unauthorized restore packet from {}", player.getName().getString());
                    return;
                }

                var targetPlayer = minecraftServer.getPlayerManager().getPlayer(profileId);
                if (targetPlayer == null) {
                    BaseDeathLogStorage.LOGGER.warn("Received restore packet for invalid player");
                    return;
                }

                final var infoList = DeathLogCommon.getStorage().getDeathInfoList(profileId);
                if (index > infoList.size() - 1) {
                    BaseDeathLogStorage.LOGGER.warn("Received restore packet with invalid index from '{}'", player.getName().getString());
                    return;
                }

                infoList.get(index).restore(targetPlayer);
            });
        }

        private static void handleDelete(MinecraftServer minecraftServer, ServerPlayerEntity player, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf byteBuf, PacketSender packetSender) {
            var profileId = byteBuf.readUuid();
            var index = byteBuf.readVarInt();

            minecraftServer.execute(() -> {
                if (!DeathLogServer.hasPermission(player, "deathlog.delete")) {
                    BaseDeathLogStorage.LOGGER.warn("Received unauthorized delete packet from {}", player.getName().getString());
                    return;
                }

                DeathLogServer.getStorage().delete(DeathLogServer.getStorage().getDeathInfoList(profileId).get(index), profileId);
            });
        }

        public static void openScreen(UUID profileId, ServerPlayerEntity target) {
            var buffer = PacketByteBufs.create();
            var infos = DeathLogServer.getStorage().getDeathInfoList(profileId);

            buffer.writeCollection(infos, (packetByteBuf, info) -> info.writePartial(packetByteBuf));
            buffer.writeUuid(profileId);
            buffer.writeBoolean(target.getServer().getPlayerManager().getPlayer(profileId) != null);

            ServerPlayNetworking.send(target, OPEN_SCREEN_ID, buffer);
        }
    }
}
