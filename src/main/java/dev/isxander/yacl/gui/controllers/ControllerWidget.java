package dev.isxander.yacl.gui.controllers;

import dev.isxander.yacl.api.Controller;
import dev.isxander.yacl.api.utils.Dimension;
import dev.isxander.yacl.gui.AbstractWidget;
import dev.isxander.yacl.gui.YACLScreen;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public abstract class ControllerWidget<T extends Controller<?>> extends AbstractWidget {
    protected final T control;
    protected MultilineText wrappedTooltip;
    protected final YACLScreen screen;

    protected boolean focused = false;
    protected boolean hovered = false;

    protected final Text modifiedOptionName;
    protected final String optionNameString;

    public ControllerWidget(T control, YACLScreen screen, Dimension<Integer> dim) {
        super(dim);
        this.control = control;
        this.screen = screen;
        control.option().addListener((opt, pending) -> updateTooltip());
        updateTooltip();
        this.modifiedOptionName = control.option().name().copy().formatted(Formatting.ITALIC);
        this.optionNameString = control.option().name().getString().toLowerCase();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        hovered = isMouseOver(mouseX, mouseY);

        Text name = control.option().changed() ? modifiedOptionName : control.option().name();
        String nameString = name.getString();

        boolean firstIter = true;
        while (textRenderer.getWidth(nameString) > dim.width() - getControlWidth() - getXPadding() - 7) {
            nameString = nameString.substring(0, Math.max(nameString.length() - (firstIter ? 2 : 5), 0)).trim();
            nameString += "...";

            firstIter = false;
        }

        Text shortenedName = Text.literal(nameString).fillStyle(name.getStyle());

        drawButtonRect(matrices, dim.x(), dim.y(), dim.xLimit(), dim.yLimit(), isHovered(), isAvailable());
        matrices.push();
        matrices.translate(dim.x() + getXPadding(), getTextY(), 0);
        textRenderer.drawWithShadow(matrices, shortenedName, 0, 0, getValueColor());
        matrices.pop();

        drawValueText(matrices, mouseX, mouseY, delta);
        if (isHovered()) {
            drawHoveredControl(matrices, mouseX, mouseY, delta);
        }
    }

    @Override
    public void postRender(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (hovered) {
            YACLScreen.renderMultilineTooltip(matrices, textRenderer, wrappedTooltip, dim.centerX(), dim.y() - 5, dim.yLimit() + 5, screen.width, screen.height);
        }
    }

    protected void drawHoveredControl(MatrixStack matrices, int mouseX, int mouseY, float delta) {

    }

    protected void drawValueText(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        Text valueText = getValueText();
        matrices.push();
        matrices.translate(dim.xLimit() - textRenderer.getWidth(valueText) - getXPadding(), getTextY(), 0);
        textRenderer.drawWithShadow(matrices, valueText, 0, 0, getValueColor());
        matrices.pop();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (dim == null) return false;
        return this.dim.isPointInside((int) mouseX, (int) mouseY);
    }

    private void updateTooltip() {
        this.wrappedTooltip = MultilineText.create(textRenderer, control.option().tooltip(), screen.width / 3 * 2 - 10);
    }

    protected int getControlWidth() {
        return isHovered() ? getHoveredControlWidth() : getUnhoveredControlWidth();
    }

    public boolean isHovered() {
        return isAvailable() && (hovered || focused);
    }

    protected abstract int getHoveredControlWidth();

    protected int getUnhoveredControlWidth() {
        return textRenderer.getWidth(getValueText());
    }

    protected int getXPadding() {
        return 5;
    }

    protected int getYPadding() {
        return 2;
    }

    protected Text getValueText() {
        return control.formatValue();
    }

    protected boolean isAvailable() {
        return control.option().available();
    }

    protected int getValueColor() {
        return isAvailable() ? -1 : inactiveColor;
    }

    protected void drawOutline(MatrixStack matrices, int x1, int y1, int x2, int y2, int width, int color) {
        DrawableHelper.fill(matrices, x1, y1, x2, y1 + width, color);
        DrawableHelper.fill(matrices, x2, y1, x2 - width, y2, color);
        DrawableHelper.fill(matrices, x1, y2, x2, y2 - width, color);
        DrawableHelper.fill(matrices, x1, y1, x1 + width, y2, color);
    }

    protected float getTextY() {
        return dim.y() + dim.height() / 2f - textRenderer.fontHeight / 2f;
    }

    @Override
    public boolean changeFocus(boolean lookForwards) {
        if (!isAvailable())
            return false;

        this.focused = !this.focused;
        return this.focused;
    }

    @Override
    public void unfocus() {
        this.focused = false;
    }

    @Override
    public boolean matchesSearch(String query) {
        return optionNameString.contains(query.toLowerCase());
    }

    @Override
    public SelectionType getType() {
        return focused ? SelectionType.FOCUSED : isHovered() ? SelectionType.HOVERED : SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }
}
