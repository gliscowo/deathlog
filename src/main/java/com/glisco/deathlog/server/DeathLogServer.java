package com.glisco.deathlog.server;

import com.glisco.deathlog.DeathLogCommon;
import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.network.DeathLogPackets;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DeathLogServer implements DedicatedServerModInitializer {

    private static ServerDeathLogStorage storage;

    @Override
    public void onInitializeServer() {
        storage = new ServerDeathLogStorage();
        DeathLogCommon.setStorage(storage);

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("deathlog").then(literal("list").requires(hasPermission("deathlog.list")).then(createProfileArgument().executes(context -> {
                var profile = getProfile(context);

                final var deathInfoList = DeathLogServer.getStorage().getDeathInfoList(profile.getId());
                final var infoListSize = deathInfoList.size();
                for (DeathInfo deathInfo : deathInfoList) {
                    var leftText = deathInfo.getLeftColumnText().iterator();
                    var rightText = deathInfo.getRightColumnText().iterator();

                    context.getSource().sendFeedback(new LiteralText(""), false);
                    context.getSource().sendFeedback(new LiteralText("§7-- §aBegin §bDeath Info Entry §7--"), false);
                    while (leftText.hasNext()) context.getSource().sendFeedback(((MutableText) leftText.next()).append("   ").append(rightText.next()), false);
                    context.getSource().sendFeedback(new LiteralText("§7-- §cEnd §bDeath Info Entry §7--"), false);
                }

                if (infoListSize > 0) context.getSource().sendFeedback(new LiteralText(""), false);
                context.getSource().sendFeedback(new LiteralText("Queried §b" + infoListSize + "§r death info entries for player ").append("§b" + profile.getName()), false);

                return infoListSize;
            }))).then(literal("view").requires(hasPermission("deathlog.view")).then(createProfileArgument().executes(context -> {
                DeathLogPackets.Server.openScreen(getProfile(context).getId(), context.getSource().getPlayer());
                return 0;
            }))).then(literal("restore").requires(hasPermission("deathlog.restore")).then(createProfileArgument().then(argument("index", IntegerArgumentType.integer()).executes(context -> {
                int index = IntegerArgumentType.getInteger(context, "index");
                return executeRestore(context, index);
            })).then(literal("latest").executes(DeathLogServer::executeRestoreLatest)))));
        });

        DeathLogPackets.Server.registerDedicatedListeners();
    }

    private static Predicate<ServerCommandSource> hasPermission(String node) {
        if (FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0")) return Permissions.require(node);
        return serverCommandSource -> serverCommandSource.hasPermissionLevel(4);
    }

    private static int executeRestoreLatest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var deathInfoList = DeathLogServer.getStorage().getDeathInfoList(getProfile(context).getId());
        deathInfoList.get(deathInfoList.size() - 1).restore(context.getSource().getPlayer());
        return 0;
    }

    private static int executeRestore(CommandContext<ServerCommandSource> context, int index) throws CommandSyntaxException {
        DeathLogServer.getStorage().getDeathInfoList(getProfile(context).getId()).get(index).restore(context.getSource().getPlayer());
        return 0;
    }

    private static GameProfile getProfile(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var profileArgument = GameProfileArgumentType.getProfileArgument(context, "player");
        return profileArgument.iterator().next();
    }

    private static RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> createProfileArgument() {
        return argument("player", GameProfileArgumentType.gameProfile()).suggests((context, builder) -> {
            PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
            return CommandSource.suggestMatching(playerManager.getPlayerList().stream().map((player) -> player.getGameProfile().getName()), builder);
        });
    }

    public static ServerDeathLogStorage getStorage() {
        return storage;
    }
}
