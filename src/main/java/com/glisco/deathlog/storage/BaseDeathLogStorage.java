package com.glisco.deathlog.storage;

import com.glisco.deathlog.client.DeathInfo;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class BaseDeathLogStorage implements DeathLogStorage {

    private static final int FORMAT_REVISION = 2;
    public static final Logger LOGGER = LogManager.getLogger();

    private boolean errored = false;
    private String errorCondition = "";

    protected CompletableFuture<List<DeathInfo>> load(File file) {
        final var future = new CompletableFuture<List<DeathInfo>>();
        Util.getIoWorkerExecutor().submit(() -> {
            if (errored) {
                LOGGER.warn("Attempted to load DeathLog database even though disk operations are disabled");
                future.complete(null);
                return;
            }

            NbtCompound deathNbt;

            if (file.exists()) {
                try {
                    deathNbt = NbtIo.read(file);

                    if (deathNbt.getInt("FormatRevision") != FORMAT_REVISION) {
                        raiseError("Incompatible format");

                        LOGGER.error("Incompatible DeathLog database format detected. Database not loaded and further disk operations disabled");

                        future.complete(null);
                        return;
                    }
                } catch (IOException e) {
                    raiseError("Disk access failed");

                    e.printStackTrace();
                    LOGGER.error("Failed to load DeathLog database, further disk operations have been disabled");

                    future.completeExceptionally(e);
                    return;
                }
            } else {
                deathNbt = new NbtCompound();
            }

            final var list = new ArrayList<DeathInfo>();
            final NbtList infoList = deathNbt.getList("Deaths", NbtElement.LIST_TYPE);
            for (int i = 0; i < infoList.size(); i++) {
                list.add(DeathInfo.readFromNbt(infoList.getList(i)));
            }

            future.complete(list);
        });

        return future;
    }

    protected void save(File file, List<DeathInfo> listIn) {
        final var list = ImmutableList.copyOf(listIn);
        Util.getIoWorkerExecutor().submit(() -> {
            if (errored) {
                LOGGER.warn("Attempted to save DeathLog database even though disk operations are disabled");
                return;
            }

            final NbtCompound deathNbt = new NbtCompound();
            final NbtList infoList = new NbtList();

            list.forEach(deathInfo -> infoList.add(deathInfo.writeNbt()));

            deathNbt.put("Deaths", infoList);
            deathNbt.putInt("FormatRevision", FORMAT_REVISION);

            try {
                NbtIo.write(deathNbt, file);
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error("Failed to save DeathLog database");
            }
        });
    }

    @Override
    public boolean isErrored() {
        return errored;
    }

    @Override
    public String getErrorCondition() {
        return errorCondition;
    }

    protected void raiseError(String error) {
        this.errored = true;
        this.errorCondition = error;
    }

}
