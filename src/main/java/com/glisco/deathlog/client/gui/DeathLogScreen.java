package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.ClientDeathLogStorage;
import com.glisco.deathlog.client.Config;
import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.storage.SingletonDeathLogStorage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class DeathLogScreen extends Screen {

    private static final Identifier INVENTORY_TEXTURE = new Identifier("deathlog", "textures/gui/inventory_overlay.png");

    private final Screen previousScreen;
    private final SingletonDeathLogStorage storage;
    private DeathListWidget deathList;

    private ItemStack hoveredStack = null;

    public DeathLogScreen(Screen previousScreen, SingletonDeathLogStorage storage) {
        super(Text.of("Death Log"));
        this.previousScreen = previousScreen;
        this.storage = storage;
    }

    public void disableRestore() {
        this.deathList.restoreEnabled = false;
    }

    public void updateInfo(DeathInfo info, int index) {
        this.storage.getDeathInfoList().set(index, info);
        this.deathList.refilter();
    }

    @Override
    protected void init() {

        deathList = new DeathListWidget(client, 220, this.height, 32, this.height - 100, 40, storage);
        deathList.setLeftPos(10);
        this.addDrawableChild(deathList);

        ;
        this.addDrawableChild(ButtonWidget.builder(Text.of("Done"), button -> this.close())
                .position(137, this.height - 32)
                .size(96, 20)
                .build()
        );

        final TextFieldWidget searchField = new TextFieldWidget(textRenderer, 10, this.height - 95, 220, 20, Text.of(""));
        searchField.setChangedListener(s -> {
            searchField.setEditableColor(deathList.filter(s) ? 0xFFFFFF : 0xFF2222);
        });
        this.addDrawableChild(searchField);

        searchField.setText(storage.getDefaultFilter());

        if (storage instanceof ClientDeathLogStorage) {

            this.addDrawableChild(ButtonWidget.builder(createScreenshotButtonText(), button -> {
                        Config.instance().screenshotsEnabled = !Config.instance().screenshotsEnabled;
                        Config.save();
                        button.setMessage(createScreenshotButtonText());
                    }).position(12, this.height - 53)
                    .size(120, 20)
                    .build());

            this.addDrawableChild(ButtonWidget.builder(createLegacyButtonText(), button -> {
                        Config.instance().useLegacyDeathDetection = !Config.instance().useLegacyDeathDetection;
                        Config.save();

                        button.setMessage(createLegacyButtonText());
                    }).position(12, this.height - 32)
                    .size(120, 20)
                    .tooltip(Tooltip.of(Text.of("Uses a less reliable but more sensitive method of detecting deaths that works with protocol translators like ViaFabric")))
                    .build()
            );
        }
    }

    private static Text createScreenshotButtonText() {
        return Text.of("Screenshot: " + (Config.instance().screenshotsEnabled ? "§aON" : "§cOFF"));
    }

    private static Text createLegacyButtonText() {
        return Text.of("Legacy Detection: " + (Config.instance().useLegacyDeathDetection ? "§aON" : "§cOFF"));
    }

    @Override
    public void close() {
        client.setScreen(previousScreen);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);

        final var originX = 230 + 30;
        final var originY = Math.min(this.height - 40, 300);

        final var selectedEntry = deathList.getSelectedOrNull();
        if (selectedEntry != null && !selectedEntry.getInfo().isPartial()) {
            DeathInfo info = selectedEntry.getInfo();

            textRenderer.draw(matrices, info.getTitle(), originX, 16, 0xFFFFFF);

            final var leftColumnText = info.getLeftColumnText();
            for (int i = 0; i < leftColumnText.size(); i++) {
                textRenderer.draw(matrices, leftColumnText.get(i), originX, 60 + 14 * i, 0xFFFFFF);
            }

            final var rightColumnText = info.getRightColumnText();
            for (int i = 0; i < rightColumnText.size(); i++) {
                textRenderer.draw(matrices, rightColumnText.get(i), originX + 100, 60 + 14 * i, 0xFFFFFF);
            }

            RenderSystem.setShaderTexture(0, INVENTORY_TEXTURE);
            drawTexture(matrices, originX - 8, originY - 83, 0, 0, 210, 107);

            hoveredStack = null;

            for (int i = 0; i < info.getPlayerItems().size() - 1; i++) {
                final ItemStack stack = info.getPlayerItems().get(i);
                if (stack.isEmpty()) continue;

                final var slotX = originX + 18 * (i % 9);
                final var slotY = originY + (i < 9 ? 0 : -58 + 18 * (i / 9 - 1));

                renderSlotWithPossibleTooltip(matrices, stack, slotX, slotY, mouseX, mouseY);
            }

            if (!info.getPlayerItems().get(36).isEmpty()) {
                renderSlotWithPossibleTooltip(matrices, info.getPlayerItems().get(36), originX + 178, originY - 75, mouseX, mouseY);
            }

            for (int i = 0; i < info.getPlayerArmor().size(); i++) {
                final ItemStack stack = info.getPlayerArmor().get(i);
                if (stack.isEmpty()) continue;

                final var slotX = originX + 178;
                final var slotY = originY - 18 * i;

                renderSlotWithPossibleTooltip(matrices, stack, slotX, slotY, mouseX, mouseY);
            }

            if (hoveredStack != null) {
                final var tooltip = getTooltipFromItem(hoveredStack);
                tooltip.add(Text.of(""));
                tooltip.add(Text.literal("Press Mouse 3 to " + (client.player.isCreative() ? "spawn" : "copy /give")).formatted(Formatting.GRAY));
                renderTooltip(matrices, tooltip, mouseX, mouseY);
            }

        }
        super.render(matrices, mouseX, mouseY, delta);
        textRenderer.draw(matrices, title, 16, 12, 0xFFFFFF);
        drawCenteredText(matrices, textRenderer, "Total Deaths: " + storage.getDeathInfoList().size(), 185, this.height - 48, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hoveredStack != null && button == 2) {
            if (client.player.isCreative()) {
                client.interactionManager.dropCreativeStack(hoveredStack);
            } else {

                String command = "/give " + client.player.getName().getString() +
                        " " +
                        Registries.ITEM.getId(hoveredStack.getItem()) +
                        hoveredStack.getOrCreateNbt().toString();

                client.keyboard.setClipboard(command);
            }
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) || this.deathList.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private void renderSlotWithPossibleTooltip(MatrixStack matrices, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        if (mouseX > x && mouseX < x + 16 && mouseY > y && mouseY < y + 16) {
            fill(matrices, x, y, x + 16, y + 16, 0xFFBBBBBB);
            this.hoveredStack = stack.copy();
        }

        itemRenderer.renderGuiItemIcon(stack, x, y);
        itemRenderer.renderGuiItemOverlay(textRenderer, stack, x, y);
    }
}
