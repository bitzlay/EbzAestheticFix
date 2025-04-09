package com.bitzlay.ebzinventory.client.gui.render;

import com.bitzlay.ebzinventory.client.gui.RustStyleInventoryScreen;
import com.bitzlay.ebzinventory.client.gui.model.UIState;
import com.bitzlay.ebzinventory.crafting.CraftingQueueHandler;
import com.bitzlay.ebzinventory.crafting.CraftingQueueItem;
import com.bitzlay.ebzinventory.recipe.InventoryRecipe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Renderizador para la cola de crafteo.
 */
public class QueueRenderer {
    /** Referencia a la pantalla principal */
    private final RustStyleInventoryScreen screen;

    /** Estado compartido de UI */
    private final UIState uiState;

    /** Renderizador de UI */
    private final UIRenderer uiRenderer;

    /**
     * Constructor del renderizador de cola.
     *
     * @param screen Pantalla principal
     * @param uiState Estado compartido de UI
     */
    public QueueRenderer(RustStyleInventoryScreen screen, UIState uiState) {
        this.screen = screen;
        this.uiState = uiState;
        this.uiRenderer = new UIRenderer();
    }

    /**
     * Renderiza la cola de crafteo.
     *
     * @param guiGraphics Contexto de renderizado
     * @param x Posición X
     * @param y Posición Y
     * @param width Ancho
     * @param panelHeight Alto
     */
    public void renderCraftingQueue(GuiGraphics guiGraphics, int x, int y, int width, int panelHeight) {
        if (screen.getMinecraftInstance().player == null) return;

        List<CraftingQueueItem> queue = CraftingQueueHandler.getPlayerQueue(screen.getMinecraftInstance().player.getUUID());
        if (queue.isEmpty()) return;

        // Obtener escala GUI para ajustes específicos
        float guiScale = uiState.getGuiScale();

        // Panel principal con título
        uiRenderer.renderPanel(guiGraphics, x, y, width, panelHeight, "Cola de Crafteo", screen.getMinecraftInstance().font);

        // Botón para cancelar toda la cola - versión más grande y clara
        int cancelAllX = x + width - 25;
        int cancelAllY = y + 5;
        boolean hoveringCancelAll = uiRenderer.isInRect(screen.getxMouse(), screen.getyMouse(),
                cancelAllX - 2, cancelAllY - 2, 24, 24);

        // Destacar botón al pasar el cursor
        if (hoveringCancelAll) {
            guiGraphics.fill(cancelAllX - 2, cancelAllY - 2, cancelAllX + 22, cancelAllY + 22, 0xFF888888);
        }

        // Dibujar el botón con border
        guiGraphics.fill(cancelAllX, cancelAllY, cancelAllX + 20, cancelAllY + 20,
                hoveringCancelAll ? 0xFF666666 : 0xFF444444);
        guiGraphics.fill(cancelAllX, cancelAllY, cancelAllX + 20, cancelAllY + 1, 0xFF777777);
        guiGraphics.fill(cancelAllX, cancelAllY, cancelAllX + 1, cancelAllY + 20, 0xFF777777);
        guiGraphics.fill(cancelAllX, cancelAllY + 19, cancelAllX + 20, cancelAllY + 20, 0xFF333333);
        guiGraphics.fill(cancelAllX + 19, cancelAllY, cancelAllX + 20, cancelAllY + 20, 0xFF333333);

        // Símbolo X más visible
        guiGraphics.drawString(screen.getMinecraftInstance().font, "×", cancelAllX + 6, cancelAllY + 6, 0xFFFF0000);

        // Tooltip al pasar el cursor
        if (hoveringCancelAll) {
            guiGraphics.renderTooltip(screen.getMinecraftInstance().font,
                    net.minecraft.network.chat.Component.literal("Cancelar toda la cola"),
                    (int)screen.getxMouse(), (int)screen.getyMouse());
        }

        // Procesar clics
        if (hoveringCancelAll && screen.isMouseDown()) {
            CraftingQueueHandler.clearQueue(screen.getMinecraftInstance().player.getUUID());

            // Reproducir sonido de feedback
            screen.getMinecraftInstance().player.playSound(
                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),
                    1.0F, 1.0F
            );
            screen.setMouseDown(false); // Evitar múltiples clics
            return;
        }

        // Configuración de paginación con ajustes para todas las escalas
        int itemHeight = guiScale >= 4 ? 22 : 25;
        int itemSpacing = 2;

        // CAMBIO: Aumentar el número máximo de items por página
        // Calcular cuántos items caben por página
        int availableHeight = panelHeight - 80;
        int itemsPerPage = Math.max(1, availableHeight / (itemHeight + itemSpacing));

        // Asegurar que al menos muestre 11 items si hay suficiente espacio
        if (availableHeight >= ((itemHeight + itemSpacing) * 11)) {
            itemsPerPage = 11;
        }

        int totalPages = (queue.size() - 1) / itemsPerPage + 1;
        int queuePage = uiState.getQueuePage();
        int startIndex = queuePage * itemsPerPage;

        // Asegurar página válida
        if (startIndex >= queue.size()) {
            queuePage = 0;
            uiState.setQueuePage(0);
            startIndex = 0;
        }

        int endIndex = Math.min(startIndex + itemsPerPage, queue.size());

        // Renderizar items con más espacio entre ellos
        int itemY = y + 30; // Más espacio después del título
        for (int i = startIndex; i < endIndex; i++) {
            if (i >= queue.size()) break;
            CraftingQueueItem item = queue.get(i);
            // Asegurarnos de que el item de la cola no se dibuja fuera del panel
            int adjustedWidth = Math.min(width - 10, width - 10);
            renderQueueItem(guiGraphics, x + 5, itemY, adjustedWidth, item, i, itemHeight);
            itemY += itemHeight + itemSpacing;
        }

        // Botones de navegación compactos - versión mejorada
        if (totalPages > 1) {
            int navY = y + panelHeight - 25;
            int buttonWidth = Math.min(30, width / 4);

            // Botón anterior
            if (queuePage > 0) {
                int prevX = x + 10;
                boolean hoveringPrev = uiRenderer.isInRect(screen.getxMouse(), screen.getyMouse(),
                        prevX, navY, buttonWidth, 20);

                // Dibujar botón
                guiGraphics.fill(prevX, navY, prevX + buttonWidth, navY + 20,
                        hoveringPrev ? 0xFF555555 : 0xFF333333);
                // Bordes
                guiGraphics.fill(prevX, navY, prevX + buttonWidth, navY + 1, 0xFF666666);
                guiGraphics.fill(prevX, navY, prevX + 1, navY + 20, 0xFF666666);
                guiGraphics.fill(prevX, navY + 19, prevX + buttonWidth, navY + 20, 0xFF222222);
                guiGraphics.fill(prevX + buttonWidth - 1, navY, prevX + buttonWidth, navY + 20, 0xFF222222);

                // Texto
                guiGraphics.drawString(screen.getMinecraftInstance().font, "<", prevX + (buttonWidth - screen.getMinecraftInstance().font.width("<")) / 2,
                        navY + 6, 0xFFFFFFFF);

                // Procesar clic
                if (hoveringPrev && screen.isMouseDown()) {
                    uiState.setQueuePage(queuePage - 1);
                    screen.getMinecraftInstance().player.playSound(
                            net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),
                            1.0F, 1.0F);
                    screen.setMouseDown(false);
                    screen.resetWidgets();
                }
            }

            // Indicador de página
            String pageText = (queuePage + 1) + "/" + totalPages;
            int textWidth = screen.getMinecraftInstance().font.width(pageText);
            guiGraphics.drawString(screen.getMinecraftInstance().font, pageText,
                    x + (width - textWidth) / 2,
                    navY + 6, 0xFFAAAAAA);

            // Botón siguiente
            if (queuePage < totalPages - 1) {
                int nextX = x + width - buttonWidth - 10;
                boolean hoveringNext = uiRenderer.isInRect(screen.getxMouse(), screen.getyMouse(),
                        nextX, navY, buttonWidth, 20);

                // Dibujar botón
                guiGraphics.fill(nextX, navY, nextX + buttonWidth, navY + 20,
                        hoveringNext ? 0xFF555555 : 0xFF333333);
                // Bordes
                guiGraphics.fill(nextX, navY, nextX + buttonWidth, navY + 1, 0xFF666666);
                guiGraphics.fill(nextX, navY, nextX + 1, navY + 20, 0xFF666666);
                guiGraphics.fill(nextX, navY + 19, nextX + buttonWidth, navY + 20, 0xFF222222);
                guiGraphics.fill(nextX + buttonWidth - 1, navY, nextX + buttonWidth, navY + 20, 0xFF222222);

                // Texto
                guiGraphics.drawString(screen.getMinecraftInstance().font, ">", nextX + (buttonWidth - screen.getMinecraftInstance().font.width(">")) / 2,
                        navY + 6, 0xFFFFFFFF);

                // Procesar clic
                if (hoveringNext && screen.isMouseDown()) {
                    uiState.setQueuePage(queuePage + 1);
                    screen.getMinecraftInstance().player.playSound(
                            net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),
                            1.0F, 1.0F);
                    screen.setMouseDown(false);
                    screen.resetWidgets();
                }
            }
        }
    }

    /**
     * Renderiza un item individual de la cola de crafteo.
     *
     * @param guiGraphics Contexto de renderizado
     * @param x Posición X
     * @param y Posición Y
     * @param width Ancho
     * @param item Item de cola a renderizar
     * @param index Índice del item en la cola
     * @param itemHeight Alto del item
     */
    private void renderQueueItem(GuiGraphics guiGraphics, int x, int y, int width, CraftingQueueItem item, int index, int itemHeight) {
        // Obtener escala para ajustes
        float guiScale = uiState.getGuiScale();

        // Ajustar altura según escala si no se proporciona explícitamente
        if (itemHeight <= 0) {
            itemHeight = guiScale >= 4 ? 22 : 25;
        }

        // Fondo del item
        renderQueueItemBackground(guiGraphics, x, y, width, itemHeight);

        // Obtener item resultado asegurando que no sea nulo
        ItemStack resultItem = item.getResult();
        if (resultItem.isEmpty() && item.getRecipe() != null) {
            resultItem = item.getRecipe().getResult().copy();
        }

        // Barra de progreso - garantizando que no se salga del ancho disponible
        int progressBarWidth = Math.min(Math.max(50, width - 90), width - 30);
        renderProgressBar(guiGraphics, x + 25, y, progressBarWidth, item, itemHeight);

        // Renderizar item a la izquierda
        int itemSize = 16;
        int itemX = x + 5;
        int itemY = y + (itemHeight - itemSize) / 2; // Centrado vertical
        guiGraphics.renderItem(resultItem, itemX, itemY);

        // Nombre del item - Acortado y con elipsis si es necesario
        String itemName = item.getRecipe() != null ? item.getRecipe().getDisplayName() : "Item";

        // Calcular ancho máximo disponible para el nombre
        int maxNameWidth = width - 85; // Asegurar suficiente espacio para todo
        if (screen.getMinecraftInstance().font.width(itemName) > maxNameWidth) {
            itemName = screen.getMinecraftInstance().font.plainSubstrByWidth(itemName, maxNameWidth - screen.getMinecraftInstance().font.width("...")) + "...";
        }

        // Mostrar nombre con sombra para mejor legibilidad
        guiGraphics.drawString(screen.getMinecraftInstance().font, itemName, x + 25, y + 3, 0xFFFFFFFF);

        // Porcentaje a la derecha del nombre, con suficiente separación y asegurando que quepa
        float progress = item.getProgress() * 100;
        String percentage = String.format("%.0f%%", progress);

        // Calcular posición X para el porcentaje garantizando que quede dentro del panel
        int percentX = Math.min(x + 30 + progressBarWidth, x + width - screen.getMinecraftInstance().font.width(percentage) - 5);
        int percentY = y + 3;
        guiGraphics.drawString(screen.getMinecraftInstance().font, percentage, percentX, percentY, 0xFFAAAAAA);

        // Botón cancelar - versión mejorada y ajustada para caber
        int cancelX = x + width - 18;
        int cancelY = y + 3;
        boolean hoveringCancel = uiRenderer.isInRect(screen.getxMouse(), screen.getyMouse(),
                cancelX - 2, cancelY - 2, 18, 18);

        // Destacar al pasar cursor
        if (hoveringCancel) {
            guiGraphics.fill(cancelX - 2, cancelY - 2, cancelX + 16, cancelY + 16, 0xFF666666);
        }

        // Dibujar X
        guiGraphics.drawString(screen.getMinecraftInstance().font, "×", cancelX + 2, cancelY + 1, hoveringCancel ? 0xFFFF0000 : 0xFFDDDDDD);

        // Tooltip
        if (hoveringCancel) {
            guiGraphics.renderTooltip(screen.getMinecraftInstance().font,
                    net.minecraft.network.chat.Component.literal("Cancelar crafteo"),
                    (int)screen.getxMouse(), (int)screen.getyMouse());
        }

        // Procesar clic
        if (hoveringCancel && screen.isMouseDown()) {
            cancelCrafting(item, index);
            // Sonido feedback
            screen.getMinecraftInstance().player.playSound(
                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),
                    1.0F, 1.0F);
            screen.setMouseDown(false);
        }
    }

    /**
     * Renderiza el fondo de un item de la cola.
     *
     * @param guiGraphics Contexto de renderizado
     * @param x Posición X
     * @param y Posición Y
     * @param width Ancho
     * @param height Alto
     */
    private void renderQueueItemBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Background con bordes más definidos
        guiGraphics.fill(x, y, x + width, y + height, 0xFF333333);

        // Bordes con mejor contraste
        guiGraphics.fill(x, y, x + width, y + 1, 0xFF555555);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFF555555);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFF222222);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF222222);
    }

    /**
     * Renderiza una barra de progreso para un item de cola.
     *
     * @param guiGraphics Contexto de renderizado
     * @param x Posición X
     * @param y Posición Y
     * @param width Ancho
     * @param item Item de cola
     * @param itemHeight Alto del item
     */
    private void renderProgressBar(GuiGraphics guiGraphics, int x, int y, int width, CraftingQueueItem item, int itemHeight) {
        int barHeight = 4;
        int barY = y + itemHeight - 7;

        // Validar ancho mínimo
        width = Math.max(10, width);

        // Barra de fondo
        guiGraphics.fill(x, barY, x + width, barY + barHeight, 0xFF444444);

        // Bordes sutiles
        guiGraphics.fill(x, barY, x + width, barY + 1, 0xFF555555);
        guiGraphics.fill(x, barY, x + 1, barY + barHeight, 0xFF555555);
        guiGraphics.fill(x, barY + barHeight - 1, x + width, barY + barHeight, 0xFF333333);
        guiGraphics.fill(x + width - 1, barY, x + width, barY + barHeight, 0xFF333333);

        // Progreso con color apropiado
        float progress = item.getProgress();
        progress = Math.max(0.0f, Math.min(1.0f, progress)); // Validar rango

        int progressWidth = (int)(width * progress);
        progressWidth = Math.min(progressWidth, width); // Evitar desbordamiento

        int progressColor = item.isPaused() ? 0xFFFFAA00 : 0xFF00BB00;
        int progressHighlight = item.isPaused() ? 0xFFFFCC00 : 0xFF00DD00;

        // Relleno con gradiente
        if (progressWidth > 0) {
            guiGraphics.fill(x, barY, x + progressWidth, barY + barHeight, progressColor);

            // Highlight en la parte superior
            guiGraphics.fill(x, barY, x + progressWidth, barY + 1, progressHighlight);
        }
    }

    /**
     * Cancela un item de crafteo.
     *
     * @param item Item a cancelar
     * @param index Índice en la cola
     */
    private void cancelCrafting(CraftingQueueItem item, int index) {
        // Devolver materiales si el progreso es menor que 100%
        if (item.getProgress() < 1.0f) {
            InventoryRecipe recipe = item.getRecipe();
            if (recipe != null && screen.getMinecraftInstance().player != null) {
                for (Map.Entry<Item, Integer> ingredient : recipe.getIngredients().entrySet()) {
                    ItemStack returnStack = new ItemStack(ingredient.getKey(), ingredient.getValue());
                    if (!screen.getMinecraftInstance().player.getInventory().add(returnStack)) {
                        screen.getMinecraftInstance().player.drop(returnStack, false);
                    }
                }
            }
        }

        // Cancelar este crafteo específico
        CraftingQueueHandler.cancelItem(screen.getMinecraftInstance().player.getUUID(), index);
    }

    /**
     * Maneja los clics de mouse en la cola de crafteo.
     *
     * @param mouseX Posición X del mouse
     * @param mouseY Posición Y del mouse
     * @param button Botón del mouse
     * @return true si el clic fue manejado
     */
    public boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        // Los clics ya son manejados en los métodos de renderizado
        return false;
    }
}