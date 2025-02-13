package dev.isxander.yacl.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.isxander.yacl.api.ConfigCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;

public class CategoryListWidget extends ElementListWidget<CategoryListWidget.CategoryEntry> {
    private final YACLScreen yaclScreen;

    public CategoryListWidget(MinecraftClient client, YACLScreen yaclScreen, int screenWidth, int screenHeight) {
        super(client, screenWidth / 3, yaclScreen.searchFieldWidget.y - 5, 0, yaclScreen.searchFieldWidget.y - 5, 21);
        this.yaclScreen = yaclScreen;
        setRenderBackground(false);
        setRenderHorizontalShadows(false);

        for (ConfigCategory category : yaclScreen.config.categories()) {
            addEntry(new CategoryEntry(category));
        }
    }

    @Override
    protected void renderList(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        double d = this.client.getWindow().getScaleFactor();
        RenderSystem.enableScissor(0, (int)((yaclScreen.height - bottom) * d), (int)(width * d), (int)(height * d));
        super.renderList(matrices, mouseX, mouseY, delta);
        RenderSystem.disableScissor();
    }

    public void postRender(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        for (CategoryEntry entry : children()) {
            entry.postRender(matrices, mouseX, mouseY, delta);
        }
    }

    @Override
    public int getRowWidth() {
        return width - width / 10;
    }

    @Override
    public int getRowLeft() {
        return super.getRowLeft() - 2;
    }

    @Override
    protected int getScrollbarPositionX() {
        return width - 2;
    }

    public class CategoryEntry extends Entry<CategoryEntry> {
        private final CategoryWidget categoryButton;
        public final int categoryIndex;

        public CategoryEntry(ConfigCategory category) {
            this.categoryIndex = yaclScreen.config.categories().indexOf(category);
            categoryButton = new CategoryWidget(
                    yaclScreen,
                    category,
                    categoryIndex,
                    getRowLeft(), 0,
                    getRowWidth(), 20
            );
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (mouseY > bottom) {
                mouseY = -20;
            }

            categoryButton.y = y;
            categoryButton.render(matrices, mouseX, mouseY, tickDelta);
        }

        private void postRender(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
            categoryButton.renderHoveredTooltip(matrices);
        }

        @Override
        public List<? extends Element> children() {
            return ImmutableList.of(categoryButton);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(categoryButton);
        }
    }
}
