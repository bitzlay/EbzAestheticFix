package com.bitzlay.ebzinventory.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Botón personalizado para mostrar una receta de crafteo.
 */
public class InventoryRecipeButton extends Button {
    private final ItemStack result;
    private final String recipeId;
    private final boolean isSelected;
    private final Minecraft minecraft;
    private final String displayName;

    /**
     * Constructor del botón de receta.
     *
     * @param x Posición X
     * @param y Posición Y
     * @param width Ancho
     * @param height Alto
     * @param recipeId ID de la receta
     * @param result Item resultante
     * @param onPress Acción al presionar
     * @param isSelected Si está seleccionado
     * @param displayName Nombre a mostrar
     */
    public InventoryRecipeButton(int x, int y, int width, int height, String recipeId,
                                 ItemStack result, OnPress onPress, boolean isSelected,
                                 String displayName) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.result = result;
        this.recipeId = recipeId;
        this.isSelected = isSelected;
        this.minecraft = Minecraft.getInstance();
        this.displayName = displayName;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean isHovering = mouseX >= this.getX() && mouseX < this.getX() + this.width &&
                mouseY >= this.getY() && mouseY < this.getY() + this.height;

        // Fondo del botón
        int bgColor = isSelected ? 0xFF666666 : (isHovering ? 0xFF4A4A4A : 0xFF333333);
        guiGraphics.fill(this.getX(), this.getY(),
                this.getX() + this.width, this.getY() + this.height,
                bgColor);

        // Borde del botón
        int borderColor = isSelected ? 0xFFFFFF00 : (isHovering ? 0xFF888888 : 0xFF555555);
        renderBorder(guiGraphics, borderColor);

        // Renderizar el item
        int itemY = this.getY() + (this.height - 16) / 2;
        guiGraphics.renderItem(result, this.getX() + 8, itemY);
        guiGraphics.renderItemDecorations(minecraft.font, result,
                this.getX() + 8, itemY);

        // Usar el displayName en lugar del nombre del item
        guiGraphics.drawString(minecraft.font, displayName,
                this.getX() + 35, this.getY() + (this.height - 8) / 2,
                isHovering ? 0xFFFFA0 : 0xFFFFFF);
    }

    private void renderBorder(GuiGraphics guiGraphics, int color) {
        // Borde superior
        guiGraphics.fill(this.getX(), this.getY(),
                this.getX() + this.width, this.getY() + 1, color);
        // Borde inferior
        guiGraphics.fill(this.getX(), this.getY() + this.height - 1,
                this.getX() + this.width, this.getY() + this.height, color);
        // Borde izquierdo
        guiGraphics.fill(this.getX(), this.getY(),
                this.getX() + 1, this.getY() + this.height, color);
        // Borde derecho
        guiGraphics.fill(this.getX() + this.width - 1, this.getY(),
                this.getX() + this.width, this.getY() + this.height, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= this.getX() && mouseX < this.getX() + this.width &&
                mouseY >= this.getY() && mouseY < this.getY() + this.height) {
            this.onPress.onPress(this);
            return true;
        }
        return false;
    }
}