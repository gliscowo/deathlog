package com.glisco.deathlog.storage;

import com.glisco.deathlog.client.DeathInfo;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class BaseDeathLogStorage implements DeathLogStorage {

    private static final int FORMAT_REVISION = 2;
    public static final Logger LOGGER = LogManager.getLogger();

    private boolean errored = false;
    private String errorCondition = "";

    protected void load(File file, List<DeathInfo> into) {
        if (errored) {
            LOGGER.warn("Attempted to load DeathLog database even though disk operations are disabled");
            return;
        }

        NbtCompound deathNbt;

        if (file.exists()) {
            try {
                deathNbt = NbtIo.read(file);

                if (deathNbt.getInt("FormatRevision") != FORMAT_REVISION) {
                    raiseError("Incompatible format");

                    LOGGER.error("Incompatible DeathLog database format detected. Database not loaded and further disk operations disabled");
                    return;
                }
            } catch (IOException e) {
                raiseError("Disk access failed");

                e.printStackTrace();
                LOGGER.error("Failed to load DeathLog database, further disk operations have been disabled");
                return;
            }
        } else {
            deathNbt = new NbtCompound();
        }

        into.clear();
        final NbtList infoList = deathNbt.getList("Deaths", NbtElement.LIST_TYPE);
        for (int i = 0; i < infoList.size(); i++) {
            into.add(DeathInfo.readFromNbt(infoList.getList(i)));
        }
    }

    protected void save(File file, List<DeathInfo> list) {
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
