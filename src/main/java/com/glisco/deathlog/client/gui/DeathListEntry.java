package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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

        final boolean trashCanHovered = trashCanHovered(x, y, mouseX, mouseY);
        int trashV = trashCanHovered ? 16 : 0;
        DrawableHelper.drawTexture(matrices, x + 195, y + 10, 0, trashV, 16, 16, 32, 32);

        final boolean restoreHovered = restoreHovered(x, y, mouseX, mouseY);
        int restoreV = restoreHovered ? 16 : 0;
        if (parent.restoreEnabled) DrawableHelper.drawTexture(matrices, x + 170, y + 10, 16, restoreV, 16, 16, 32, 32);

        if (trashCanHovered) MinecraftClient.getInstance().currentScreen.renderTooltip(matrices, Text.of("Shift-Click to delete"), mouseX, mouseY);
        if (restoreHovered) MinecraftClient.getInstance().currentScreen.renderTooltip(matrices, Text.of("Shift-Click to restore"), mouseX, mouseY);

        this.lastRenderX = x;
        this.lastRenderY = y;
    }

    private boolean trashCanHovered(int x, int y, double mouseX, double mouseY) {
        return mouseX > x + 191 && mouseX < x + 218 && mouseY > y + 5 && mouseY < y + 35;
    }

    private boolean restoreHovered(int x, int y, double mouseX, double mouseY) {
        return parent.restoreEnabled && mouseX > x + 165 && mouseX < x + 192 && mouseY > y + 5 && mouseY < y + 35;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        final boolean isDeleteClick = trashCanHovered(lastRenderX, lastRenderY, mouseX, mouseY);
        final boolean isRestoreClick = restoreHovered(lastRenderX, lastRenderY, mouseX, mouseY);

        if (Screen.hasShiftDown()) {
            if (isDeleteClick) {
                parent.deleteInfoFromStorage(this.info);
                this.parent.refilter();
            } else if (isRestoreClick) {
                parent.restoreInfo(this.info);
            }
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
