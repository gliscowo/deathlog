package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.client.DeathLogClient;
import com.glisco.deathlog.mixin.MinecraftServerAccessor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class DeathLogScreen extends Screen {

    private static final Identifier INVENTORY_TEXTURE = new Identifier("deathlog", "textures/gui/inventory_overlay.png");

    private final Screen previousScreen;
    private DeathListWidget deathList;

    private ItemStack hoveredStack = null;

    public DeathLogScreen(Screen previousScreen) {
        super(Text.of("Death Log"));
        this.previousScreen = previousScreen;
    }

    @Override
    protected void init() {

        deathList = new DeathListWidget(client, 220, this.height, 32, this.height - 100, 40, DeathLogClient.getDeathInfos());
        deathList.setLeftPos(10);
        this.addChild(deathList);

        this.addButton(new ButtonWidget(110 - 50, this.height - 32, 100, 20, Text.of("Done"), button -> {
            this.onClose();
        }));

        final TextFieldWidget searchField = new TextFieldWidget(textRenderer, 10, this.height - 95, 220, 20, Text.of(""));
        searchField.setChangedListener(s -> {
            searchField.setEditableColor(deathList.filter(s) ? 0xFFFFFF : 0xFF2222);
        });
        this.addButton(searchField);

        if (client.getCurrentServerEntry() != null) {
            searchField.setText(client.getCurrentServerEntry().name);
        } else if (client.isInSingleplayer()) {
            searchField.setText(((MinecraftServerAccessor) client.getServer()).deathlog_getSession().getDirectoryName());
        }
    }

    @Override
    public void onClose() {
        client.openScreen(previousScreen);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);

        this.deathList.render(matrices, mouseX, mouseY, delta);

        final int originX = 230 + 30;
        final int originY = Math.min(this.height - 40, 300);

        if (deathList.getSelected() != null) {
            DeathInfo info = deathList.getSelected().getInfo();
            textRenderer.draw(matrices, info.getTitle(), originX, 16, 0xFFFFFF);

            final List<Text> leftColumnText = info.getLeftColumnText();
            for (int i = 0; i < leftColumnText.size(); i++) {
                textRenderer.draw(matrices, leftColumnText.get(i), originX, 60 + 14 * i, 0xFFFFFF);
            }

            final List<Text> rightColumnText = info.getRightColumnText();
            for (int i = 0; i < rightColumnText.size(); i++) {
                textRenderer.draw(matrices, rightColumnText.get(i), originX + 100, 60 + 14 * i, 0xFFFFFF);
            }

            client.getTextureManager().bindTexture(INVENTORY_TEXTURE);
            drawTexture(matrices, originX - 8, originY - 83, 0, 0, 210, 107);

            hoveredStack = null;

            for (int i = 0; i < info.getPlayerItems().size() - 1; i++) {
                final ItemStack stack = info.getPlayerItems().get(i);
                if (stack.isEmpty()) continue;

                final int slotX = originX + 18 * (i % 9);
                final int slotY = originY + (i < 9 ? 0 : -58 + 18 * (i / 9 - 1));

                renderSlotWithPossibleTooltip(matrices, stack, slotX, slotY, mouseX, mouseY);
            }

            if (!info.getPlayerItems().get(36).isEmpty()) {
                renderSlotWithPossibleTooltip(matrices, info.getPlayerItems().get(36), originX + 178, originY - 75, mouseX, mouseY);
            }

            for (int i = 0; i < info.getPlayerArmor().size(); i++) {
                final ItemStack stack = info.getPlayerArmor().get(i);
                if (stack.isEmpty()) continue;

                final int slotX = originX + 178;
                final int slotY = originY - 18 * i;

                renderSlotWithPossibleTooltip(matrices, stack, slotX, slotY, mouseX, mouseY);
            }

            if (hoveredStack != null) {
                final List<Text> tooltip = getTooltipFromItem(hoveredStack);
                tooltip.add(Text.of(""));
                tooltip.add(Text.of("§7Press Mouse 3 to " + (client.player.isCreative() ? "spawn" : "copy /give")));
                renderTooltip(matrices, tooltip, mouseX, mouseY);
            }

        }
        super.render(matrices, mouseX, mouseY, delta);
        textRenderer.draw(matrices, title, 16, 12, 0xFFFFFF);
        textRenderer.draw(matrices, "Total Deaths: " + DeathLogClient.getDeathInfos().size(), 15, this.height - 60, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hoveredStack != null && button == 2) {
            if (client.player.isCreative()) {
                client.interactionManager.dropCreativeStack(hoveredStack);
            } else {
                final StringBuilder command = new StringBuilder("/give ");
                command.append(client.player.getName().asString());
                command.append(" ");

                command.append(Registry.ITEM.getId(hoveredStack.getItem()));
                command.append(hoveredStack.getTag().toString());

                client.keyboard.setClipboard(command.toString());
            }
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
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
