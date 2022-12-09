package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.network.RemoteDeathLogStorage;
import com.glisco.deathlog.storage.DirectDeathLogStorage;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.util.Observable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.Map;

public class BasedDeathLogScreen extends BaseUIModelScreen<FlowLayout> {

    private final Screen parent;
    private final DirectDeathLogStorage storage;

    private FlowLayout detailPanel;
    private DropdownComponent activeDropdown = null;

    private final Observable<String> currentSearchTerm = Observable.of("");
    private DeathInfo selectedInfo = null;

    public BasedDeathLogScreen(Screen parent, DirectDeathLogStorage storage) {
        super(FlowLayout.class, DataSource.file("../src/main/resources/assets/deathlog/owo_ui/deathlog.xml"));
        this.parent = parent;
        this.storage = storage;

        this.currentSearchTerm.observe(s -> {
            this.buildDeathList();
        });
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        this.detailPanel = rootComponent.childById(FlowLayout.class, "detail-panel");

        rootComponent.childById(TextBoxComponent.class, "search-box").<TextBoxComponent>configure(searchBox -> {
            searchBox.onChanged().subscribe(value -> {
                this.currentSearchTerm.set(value.toLowerCase(Locale.ROOT));
            });
        });

        this.buildDeathList();
    }

    public void updateInfo(DeathInfo info, int index) {
        this.storage.getDeathInfoList().set(index, info);
        this.selectInfo(this.selectedInfo);
    }

    private void buildDeathList() {
        this.uiAdapter.rootComponent.childById(FlowLayout.class, "death-list").<FlowLayout>configure(deathList -> {
            deathList.clearChildren();
            this.storage.getDeathInfoList().forEach(deathInfo -> {
                if (!this.currentSearchTerm.get().isBlank() && !deathInfo.createSearchString().contains(this.currentSearchTerm.get())) {
                    return;
                }

                deathList.child(this.model.expandTemplate(
                        DeathListEntryContainer.class,
                        "death-list-entry",
                        Map.of(
                                "death-time", deathInfo.getListName().getString(),
                                "death-message", deathInfo.getTitle().getString()
                        )
                ).<DeathListEntryContainer>configure(container -> {
                    container.onSelected().subscribe(c -> {
                        this.selectInfo(deathInfo);
                    });

                    container.mouseDown().subscribe((mouseX, mouseY, button) -> {
                        if (button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return false;

                        this.uiAdapter.rootComponent.removeChild(this.activeDropdown);
                        this.uiAdapter.rootComponent.child(
                                (this.activeDropdown = Components.dropdown(Sizing.content())
                                        .button(Text.literal("Restore"), dropdown -> {
                                            this.storage.restore(this.storage.getDeathInfoList().indexOf(deathInfo));
                                            this.uiAdapter.rootComponent.removeChild(dropdown);
                                        })
                                        .button(Text.literal("Delete").formatted(Formatting.RED), dropdown -> {
                                            this.storage.delete(deathInfo);
                                            this.buildDeathList();
                                            this.uiAdapter.rootComponent.removeChild(dropdown);
                                        }))
                                        .positioning(Positioning.absolute(
                                                container.x() + (int) mouseX - this.uiAdapter.rootComponent.padding().get().left(),
                                                container.y() + (int) mouseY - this.uiAdapter.rootComponent.padding().get().top()
                                        ))
                        );

                        return true;
                    });
                }));
            });
        });
    }

    private void selectInfo(DeathInfo info) {
        this.selectedInfo = info;
        if (this.storage instanceof RemoteDeathLogStorage remoteStorage) remoteStorage.fetchCompleteInfo(info);

        this.detailPanel.<FlowLayout>configure(panel -> {
            panel.clearChildren();
            panel.child(Components.label(info.getTitle()).shadow(true).margins(Insets.bottom(25)));

            FlowLayout leftColumn;
            FlowLayout rightColumn;
            panel.child(Containers.horizontalFlow(Sizing.content(), Sizing.content())
                    .child(leftColumn = Containers.verticalFlow(Sizing.content(), Sizing.content()))
                    .child(rightColumn = Containers.verticalFlow(Sizing.content(), Sizing.content())));

            leftColumn.gap(2);
            for (var text : info.getLeftColumnText()) {
                leftColumn.child(Components.label(text).shadow(true));
            }

            rightColumn.gap(2).margins(Insets.left(5));
            for (var text : info.getRightColumnText()) {
                rightColumn.child(Components.label(text));
            }

            FlowLayout itemContainer;
            panel.child(itemContainer = Containers.verticalFlow(Sizing.content(), Sizing.content()));

            itemContainer.child(Components.texture(new Identifier("deathlog", "textures/gui/inventory_overlay.png"), 0, 0, 210, 107));

            FlowLayout armorFlow;
            itemContainer.child(armorFlow = Containers.verticalFlow(Sizing.content(), Sizing.content()));

            armorFlow.positioning(Positioning.absolute(185, 28));
            for (int i = 0; i < info.getPlayerArmor().size(); i++) {
                armorFlow.child(0, this.makeItem(info.getPlayerArmor().get(i), Insets.of(1)));
            }

            GridLayout itemGrid;
            itemContainer.child(itemGrid = Containers.grid(Sizing.content(), Sizing.content(), 4, 9));

            var inventory = info.getPlayerItems();

            itemGrid.positioning(Positioning.absolute(7, 24));
            for (int i = 0; i < 9; i++) {
                itemGrid.child(this.makeItem(inventory.get(i), Insets.of(5, 1, 1, 1)), 3, i);
            }

            for (int i = 0; i < 27; i++) {
                itemGrid.child(this.makeItem(inventory.get(9 + i), Insets.of(1)), i / 9, i % 9);
            }

            if (!inventory.get(36).isEmpty()) {
                itemContainer.child(this.makeItem(inventory.get(36), Insets.none()).positioning(Positioning.absolute(186, 8)));
            }
        });
    }

    private ItemComponent makeItem(ItemStack stack, Insets margins) {
        var item = Components.item(stack);
        item.margins(margins);

        if (!stack.isEmpty()) {
            item.tooltip(stack.getTooltip(
                    client.player,
                    client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL
            ));
        }

        return item;
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    static {
        UIParsing.registerFactory("death-list-entry-container", element -> new DeathListEntryContainer());
    }
}
