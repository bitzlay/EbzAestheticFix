package com.bitzlay.ebzinventory.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

/**
 * Campo de búsqueda personalizado con texto de placeholder.
 */
public class SearchField extends EditBox {
    private final String placeholderText;
    private final int placeholderColor;
    private final Font fontRenderer;

    /**
     * Constructor del campo de búsqueda.
     *
     * @param font Fuente a utilizar
     * @param x Posición X
     * @param y Posición Y
     * @param width Ancho
     * @param height Alto
     * @param message Mensaje de componente
     * @param placeholderText Texto a mostrar cuando está vacío
     */
    public SearchField(Font font, int x, int y, int width, int height,
                       Component message, String placeholderText) {
        super(font, x, y, width, height, message);
        this.fontRenderer = font;
        this.placeholderText = placeholderText;
        this.placeholderColor = 0x808080; // Gris claro
    }

    /**
     * Constructor con placeholder por defecto.
     */
    public SearchField(Font font, int x, int y, int width, int height, Component message) {
        this(font, x, y, width, height, message, "🔍 Buscar...");
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        // Si no hay texto y no tiene foco, mostrar el placeholder
        if (this.getValue().isEmpty() && !this.isFocused()) {
            guiGraphics.drawString(this.fontRenderer,
                    Component.literal(this.placeholderText),
                    this.getX() + 5, this.getY() + 6, this.placeholderColor);
        }
    }
}