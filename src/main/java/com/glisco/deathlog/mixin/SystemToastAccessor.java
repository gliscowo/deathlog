package com.glisco.deathlog.mixin;

import net.minecraft.client.toast.SystemToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SystemToast.class)
public interface SystemToastAccessor {

    @Accessor("startTime")
    long deathlog_getStartTime();

}
