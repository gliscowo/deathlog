package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.client.DeathLogClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class DeathListEntry extends AlwaysSelectedEntryListWidget.Entry<DeathListEntry> {

    private static final Identifier TRASH_CAN_TEXTURE = new Identifier("deathlog", "textures/gui/trash_can.png");

    private final TextRenderer textRenderer;
    private final DeathInfo info;
    private final DeathListWidget parent;

    private int lastRenderX = 0;
    private int lastRenderY = 0;

    public DeathListEntry(DeathListWidget parent, DeathInfo info) {
        this.info = info;
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.parent = parent;
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        textRenderer.draw(matrices, info.getListName(), x, y + 6, 0xFFFFFF);
        textRenderer.draw(matrices, info.getTitle(), x, y + 16 + 6, 0xFFFFFF);

        RenderSystem.setShaderTexture(0, TRASH_CAN_TEXTURE);

        final boolean trashCanHovered = mouseX > x + 185 && mouseX < x + 213 && mouseY > y + 5 && mouseY < y + 35;
        int v = trashCanHovered ? 34 : 17;
        DrawableHelper.drawTexture(matrices, x + 190, y + 10, 0, v, 17, 17, 64, 64);

        if (trashCanHovered) MinecraftClient.getInstance().currentScreen.renderTooltip(matrices, Text.of("Shift-Click to delete"), mouseX, mouseY);

        this.lastRenderX = x;
        this.lastRenderY = y;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        final boolean isDeleteClick = mouseX > lastRenderX + 185 && mouseX < lastRenderX + 213 && mouseY > lastRenderY + 5 && mouseY < lastRenderY + 35;

        if (isDeleteClick && Screen.hasShiftDown()) {
            parent.deleteInfoFromStorage(this.info);
            this.parent.refilter();
        } else {
            this.parent.setSelected(this);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public DeathInfo getInfo() {
        return info;
    }

    @Override
    public Text getNarration() {
        return Text.of("");
    }
}
