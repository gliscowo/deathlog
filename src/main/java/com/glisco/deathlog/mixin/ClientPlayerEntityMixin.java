package com.glisco.deathlog.mixin;

import com.glisco.deathlog.client.DeathLogClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "onDeathMessage", at = @At("HEAD"))
    public void onClientDeath(DeathMessageS2CPacket packet, CallbackInfo ci) {
        if(!RenderSystem.isOnRenderThread()) return;
        DeathLogClient.captureDeathInfo(packet);
    }

}
