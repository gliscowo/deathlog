package com.glisco.deathlog.client.gui;

import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.core.Animation;
import io.wispforest.owo.ui.core.Easing;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

public class DeathListEntryContainer extends HorizontalFlowLayout {

    protected final EventStream<OnSelected> selectedEvents = OnSelected.newStream();
    protected final Animation<Insets> slideAnimation;

    protected boolean focused = false;
    protected boolean selected = false;

    public DeathListEntryContainer() {
        super(Sizing.content(), Sizing.content());
        this.slideAnimation = this.padding.animate(150, Easing.QUADRATIC, this.padding.get().add(0, 0, 5, 0));
    }

    public EventSource<OnSelected> onSelected() {
        return this.selectedEvents.source();
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
        if (this.selected) Drawer.drawRectOutline(matrices, this.x, this.y, this.width, this.height, 0xFFAFAFAF);
    }

    @Override
    protected void parentUpdate(float delta, int mouseX, int mouseY) {
        if (this.hovered || this.focused || this.selected) {
            this.slideAnimation.forwards();
        } else {
            this.slideAnimation.backwards();
        }
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            super.onMouseDown(mouseX, mouseY, button);

            this.select();
            return true;
        } else {
            return super.onMouseDown(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        boolean success = super.onKeyPress(keyCode, scanCode, modifiers);

        if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_SPACE && keyCode != GLFW.GLFW_KEY_KP_ENTER) {
            return success;
        }

        this.select();
        return true;
    }

    private void select() {
        for (var sibling : this.parent.children()) {
            if (sibling == this || !(sibling instanceof DeathListEntryContainer container)) continue;
            container.selected = false;
        }

        this.selected = true;
        this.selectedEvents.sink().onSelected(this);

        UISounds.playInteractionSound();
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return true;
    }

    @Override
    public void onFocusGained(FocusSource source) {
        super.onFocusGained(source);
        this.focused = true;
    }

    @Override
    public void onFocusLost() {
        super.onFocusLost();
        this.focused = false;
    }

    public interface OnSelected {
        void onSelected(DeathListEntryContainer container);

        static EventStream<OnSelected> newStream() {
            return new EventStream<>(subscribers -> container -> {
                for (var subscriber : subscribers) {
                    subscriber.onSelected(container);
                }
            });
        }
    }
}
