package com.glisco.deathlog.client;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;

@Modmenu(modId = "deathlog")
@Config(name = "deathlog", wrapperName = "DeathLogConfig")
public class DeathLogConfigModel {
    public boolean screenshotsEnabled = false;
    public boolean useLegacyDeathDetection = false;
}
