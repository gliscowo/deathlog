package com.glisco.deathlog.server;

import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.network.DeathLogPackets;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DeathLogServer implements DedicatedServerModInitializer {

    private static ServerDeathLogStorage storage;

    @Override
    public void onInitializeServer() {
        storage = new ServerDeathLogStorage();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("deathlog").then(literal("list").then(createProfileArgument().executes(context -> {
                var profileArgument = GameProfileArgumentType.getProfileArgument(context, "player");
                var profile = profileArgument.iterator().next();

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
            }))).then(literal("view").then(createProfileArgument().executes(context -> {
                var profileArgument = GameProfileArgumentType.getProfileArgument(context, "player");
                var profile = profileArgument.iterator().next();

                DeathLogPackets.Server.openScreen(profile.getId(), context.getSource().getPlayer());
                return 0;
            }))));
        });

        DeathLogPackets.Server.registerListeners();
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
