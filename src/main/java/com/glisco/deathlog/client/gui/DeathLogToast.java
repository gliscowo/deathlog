package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.mixin.SystemToastAccessor;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class DeathLogToast extends SystemToast {

    public DeathLogToast(Type type, Text title, @Nullable Text description) {
        super(type, title, description);
    }

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        super.draw(matrices, manager, startTime);
        return startTime - ((SystemToastAccessor) this).deathlog_getStartTime() < 10000L ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }
}
