package com.glisco.deathlog.mixin;

import com.glisco.deathlog.server.DeathLogServer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void onServerDeath(DamageSource source, CallbackInfo ci) {
        var player = (PlayerEntity) (Object) this;
        DeathLogServer.getStorage().store(player.getDamageTracker().getDeathMessage(), player);
    }

}
