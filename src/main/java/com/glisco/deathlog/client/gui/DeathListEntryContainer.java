package com.glisco.deathlog.client.gui;

import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.core.Easing;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

public class DeathListEntryContainer extends HorizontalFlowLayout {

    protected final EventStream<OnSelected> selectedEvents = OnSelected.newStream();
    protected boolean selected = false;

    public DeathListEntryContainer() {
        super(Sizing.content(), Sizing.content());

        var slideAnimation = this.padding.animate(150, Easing.QUADRATIC, this.padding.get().add(0, 0, 6, 0));
        this.mouseEnter().subscribe(slideAnimation::forwards);
        this.mouseLeave().subscribe(slideAnimation::backwards);
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
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        super.onMouseDown(mouseX, mouseY, button);

        this.select();
        return true;
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
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return source == FocusSource.KEYBOARD_CYCLE;
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
