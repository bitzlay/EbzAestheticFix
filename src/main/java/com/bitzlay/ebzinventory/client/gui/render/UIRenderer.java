package com.bitzlay.ebzinventory.client.gui.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;

/**
 * Clase utilitaria que proporciona métodos comunes de renderizado para la interfaz.
 * Centraliza el código de renderizado para mantener consistencia visual.
 */
public class UIRenderer {

    /**
     * Renderiza el fondo estándar de un panel de inventario.
     *
     * @param guiGraphics Contexto de renderizado
     * @param x Posición X
     * @param y Posición Y
     * @param width Ancho
     * @param height Alto
     */
    public void renderInventoryBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Semi-transparent background
        guiGraphics.fill(x - 5, y - 5, x + width + 5, y + height + 5, 0xCC000000);

        // Borders
        guiGraphics.fill(x - 5, y - 5, x + width + 5, y - 4, 0xFF373737);
        guiGraphics.fill(x - 5, y + height + 4, x + width + 5, y + height + 5, 0xFF373737);
        guiGraphics.fill(x - 5, y - 5, x - 4, y + height + 5, 0xFF373737);
        guiGraphics.fill(x + width + 4, y - 5, x + width + 5, y + height + 5, 0xFF373737);
    }

    /**
     * Renderiza un panel con título.
     *
     * @param guiGraphics Contexto de renderizado
     * @param x Posición X
     * @param y Posición Y
     * @param width Ancho
     * @param height Alto
     * @param title Título del panel
     * @param font Fuente para el título
     */
    public void renderPanel(GuiGraphics guiGraphics, int x, int y, int width, int height, String title, Font font) {
        // Panel background
        guiGraphics.fill(x, y, x + width, y + height, 0xAA000000);

        // Panel border
        guiGraphics.fill(x, y, x + width, y + 1, 0xFF555555);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFF555555);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFF555555);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF555555);

        // Title
        guiGraphics.drawString(font, title, x + 5, y + 5, 0xFFFFFF);
    }

    /**
     * Renderiza un botón personalizado.
     *
     * @param guiGraphics Contexto de renderizado
     * @param x Posición X
     * @param y Posición Y
     * @param width Ancho
     * @param height Alto
     * @param text Texto del botón
     * @param font Fuente del texto
     * @param isHovered Si el mouse está sobre el botón
     * @param isActive Si el botón está activo
     */
    public void renderCustomButton(GuiGraphics guiGraphics, int x, int y, int width, int height,
                                   String text, Font font, boolean isHovered, boolean isActive) {
        // Color base según estado
        int bgColor;
        if (!isActive) {
            bgColor = 0xFF444444; // Desactivado
        } else if (isHovered) {
            bgColor = 0xFF666666; // Hover
        } else {
            bgColor = 0xFF555555; // Normal
        }

        // Fondo del botón
        guiGraphics.fill(x, y, x + width, y + height, bgColor);

        // Bordes con efecto 3D
        int topColor = isHovered ? 0xFF888888 : 0xFF777777;
        int bottomColor = 0xFF333333;

        // Borde superior e izquierdo (más claro)
        guiGraphics.fill(x, y, x + width, y + 1, topColor);
        guiGraphics.fill(x, y, x + 1, y + height, topColor);

        // Borde inferior y derecho (más oscuro)
        guiGraphics.fill(x, y + height - 1, x + width, y + height, bottomColor);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, bottomColor);

        // Texto centrado
        int textWidth = font.width(text);
        int textColor = isActive ? 0xFFFFFFFF : 0xFFAAAAAA;
        guiGraphics.drawString(font, text, x + (width - textWidth) / 2, y + (height - 8) / 2, textColor);
    }

    /**
     * Renderiza una barra de progreso.
     *
     * @param guiGraphics Contexto de renderizado
     * @param x Posición X
     * @param y Posición Y
     * @param width Ancho
     * @param barHeight Alto de la barra
     * @param progress Progreso (0.0 - 1.0)
     * @param color Color de la barra de progreso
     * @param isPaused Si la barra está en pausa
     */
    public void renderProgressBar(GuiGraphics guiGraphics, int x, int y, int width, int barHeight,
                                  float progress, int color, boolean isPaused) {
        // Validar progress para evitar valores incorrectos
        progress = Math.max(0.0f, Math.min(1.0f, progress));

        // Calcular ancho de la barra de progreso
        int progressWidth = (int)(width * progress);
        progressWidth = Math.min(progressWidth, width); // Asegurar que no exceda el ancho total

        // Fondo de la barra
        guiGraphics.fill(x, y, x + width, y + barHeight, 0xFF444444);

        // Bordes sutiles
        guiGraphics.fill(x, y, x + width, y + 1, 0xFF555555);
        guiGraphics.fill(x, y, x + 1, y + barHeight, 0xFF555555);
        guiGraphics.fill(x, y + barHeight - 1, x + width, y + barHeight, 0xFF333333);
        guiGraphics.fill(x + width - 1, y, x + width, y + barHeight, 0xFF333333);

        // Cambiar color si está pausado
        int actualColor = isPaused ? 0xFFFFAA00 : color;
        int highlightColor = lightenColor(actualColor, 1.2f);

        // Progreso
        if (progressWidth > 0) {
            guiGraphics.fill(x, y, x + progressWidth, y + barHeight, actualColor);

            // Highlight en la parte superior para efecto 3D
            guiGraphics.fill(x, y, x + progressWidth, y + 1, highlightColor);
        }
    }

    /**
     * Verifica si un punto está dentro de un rectángulo.
     *
     * @param mouseX Posición X del mouse
     * @param mouseY Posición Y del mouse
     * @param x Posición X del rectángulo
     * @param y Posición Y del rectángulo
     * @param width Ancho del rectángulo
     * @param height Alto del rectángulo
     * @return true si el punto está dentro del rectángulo
     */
    public boolean isInRect(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /**
     * Aclara un color.
     *
     * @param color Color base
     * @param factor Factor de aclarado (1.0 = sin cambio, >1.0 más claro)
     * @return Color aclarado
     */
    public int lightenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, (int)(((color >> 16) & 0xFF) * factor));
        int g = Math.min(255, (int)(((color >> 8) & 0xFF) * factor));
        int b = Math.min(255, (int)((color & 0xFF) * factor));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Oscurece un color.
     *
     * @param color Color base
     * @param factor Factor de oscurecimiento (1.0 = sin cambio, <1.0 más oscuro)
     * @return Color oscurecido
     */
    public int darkenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int)(((color >> 16) & 0xFF) * factor);
        int g = (int)(((color >> 8) & 0xFF) * factor);
        int b = (int)((color & 0xFF) * factor);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}