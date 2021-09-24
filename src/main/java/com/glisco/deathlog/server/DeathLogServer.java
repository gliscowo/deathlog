package com.glisco.deathlog.server;

import com.glisco.deathlog.DeathLogCommon;
import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.network.DeathLogPackets;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DeathLogServer implements DedicatedServerModInitializer {

    private static final DynamicCommandExceptionType INVALID_INDEX = new DynamicCommandExceptionType(o -> new LiteralText("No DeathInfo found for index " + o));
    private static final DynamicCommandExceptionType NO_PLAYER_FOR_PROFILE = new DynamicCommandExceptionType(o -> new LiteralText("Player " + ((GameProfile) o).getName() + " is not online"));
    private static final SimpleCommandExceptionType NO_DEATHS = new SimpleCommandExceptionType(new LiteralText("No DeathInfo found"));

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
                    while (leftText.hasNext()) {
                        context.getSource().sendFeedback(((MutableText) leftText.next()).append(new LiteralText(": ")).append(((MutableText) rightText.next()).formatted(Formatting.WHITE)), false);
                    }
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
        return DeathLogCommon.usePermissions() ? Permissions.require(node, 4) : serverCommandSource -> serverCommandSource.hasPermissionLevel(4);
    }

    private static int executeRestoreLatest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        restore(context, deathInfos -> deathInfos.size() - 1, index -> NO_DEATHS.create());
        return 0;
    }

    private static int executeRestore(CommandContext<ServerCommandSource> context, int index) throws CommandSyntaxException {
        restore(context, deathInfos -> index, INVALID_INDEX::create);
        return 0;
    }

    private static void restore(CommandContext<ServerCommandSource> context, Function<List<DeathInfo>, Integer> indexProvider, Function<Integer, CommandSyntaxException> exceptionProvider) throws CommandSyntaxException {
        final var targetProfile = getProfile(context);
        final var deathInfoList = DeathLogServer.getStorage().getDeathInfoList(targetProfile.getId());

        final var targetPlayer = context.getSource().getServer().getPlayerManager().getPlayer(targetProfile.getId());
        if (targetPlayer == null) throw NO_PLAYER_FOR_PROFILE.create(targetProfile);

        final int index = indexProvider.apply(deathInfoList);
        if (deathInfoList.isEmpty() || index > deathInfoList.size() - 1) throw exceptionProvider.apply(index);

        deathInfoList.get(index).restore(targetPlayer);
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
