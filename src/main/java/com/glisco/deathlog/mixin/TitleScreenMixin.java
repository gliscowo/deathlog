package com.glisco.deathlog.mixin;

import com.glisco.deathlog.client.DeathLogClient;
import com.glisco.deathlog.client.gui.DeathLogToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Unique
    private static boolean firstRenderCompleted = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void onFirstRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (firstRenderCompleted) return;
        firstRenderCompleted = true;

        final var storage = DeathLogClient.getClientStorage();
        if (!storage.isErrored()) return;
        MinecraftClient.getInstance().getToastManager().add(new DeathLogToast(SystemToast.Type.TUTORIAL_HINT, Text.of("DeathLog Database Error"), Text.of(storage.getErrorCondition())));
        MinecraftClient.getInstance().getToastManager().add(new DeathLogToast(SystemToast.Type.TUTORIAL_HINT, Text.of("DeathLog Problem"), Text.of("Check your log for details")));
    }

}
