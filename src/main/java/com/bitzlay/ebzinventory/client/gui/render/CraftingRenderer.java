package com.bitzlay.ebzinventory.client.gui.render;

import com.bitzlay.ebzinventory.client.gui.RustStyleInventoryScreen;
import com.bitzlay.ebzinventory.client.gui.components.CategoryManager;
import com.bitzlay.ebzinventory.client.gui.components.RecipeInfoPanel;
import com.bitzlay.ebzinventory.client.gui.components.RecipePanel;
import com.bitzlay.ebzinventory.client.gui.model.UIState;
import com.bitzlay.ebzinventory.crafting.CraftingQueueHandler;
import com.bitzlay.ebzinventory.crafting.CraftingQueueItem;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

/**
 * Clase responsable de renderizar la interfaz de crafteo.
 * Coordina los diferentes paneles que forman la interfaz de crafteo.
 */
public class CraftingRenderer {
    /** Referencia a la pantalla principal */
    private final RustStyleInventoryScreen screen;

    /** Estado compartido de la UI */
    private final UIState uiState;

    /** Gestor de categorías */
    private final CategoryManager categoryManager;

    /** Panel de recetas */
    private final RecipePanel recipePanel;

    /** Panel de información de receta */
    private final RecipeInfoPanel recipeInfoPanel;

    /** Renderizador de la cola de crafteo */
    private final QueueRenderer queueRenderer;

    /** Renderizador de UI compartido */
    private final UIRenderer uiRenderer;

    /**
     * Constructor del renderizador de crafteo.
     *
     * @param screen Pantalla principal
     * @param uiState Estado compartido de UI
     */
    public CraftingRenderer(RustStyleInventoryScreen screen, UIState uiState) {
        this.screen = screen;
        this.uiState = uiState;
        this.categoryManager = new CategoryManager(screen, uiState);
        this.recipePanel = new RecipePanel(screen, uiState);
        this.recipeInfoPanel = new RecipeInfoPanel(screen, uiState);
        this.queueRenderer = new QueueRenderer(screen, uiState);
        this.uiRenderer = new UIRenderer();
    }

    /**
     * Inicializa el renderizador y sus componentes.
     */
    public void init() {
        recipePanel.init();
        recipeInfoPanel.init();
    }

    /**
     * Renderiza toda la interfaz de crafteo.
     *
     * @param guiGraphics Contexto de renderizado
     * @param mouseX Posición X del mouse
     * @param mouseY Posición Y del mouse
     * @param partialTick Tick parcial para animaciones
     */
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderCraftingArea(guiGraphics, mouseX, mouseY);
    }

    /**
     * Renderiza el área principal de crafteo.
     *
     * @param guiGraphics Contexto de renderizado
     * @param mouseX Posición X del mouse
     * @param mouseY Posición Y del mouse
     */
    private void renderCraftingArea(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int screenWidth = screen.getScreenWidth();
        int screenHeight = screen.getScreenHeight();

        // Obtener la escala actual
        float guiScale = uiState.getGuiScale();

        // Calcular dimensiones adaptativas para el área de crafteo
        int craftingAreaWidth;
        if (guiScale >= 4) {
            craftingAreaWidth = Math.min(screenWidth - 20, 640); // Área más estrecha en escala 4
        } else {
            craftingAreaWidth = Math.min(screenWidth - 40, 800);
        }

        // Posición vertical con margen desde el botón de crafteo
        int craftingY = 40; // Justo debajo del botón de crafteo

        // Ajustar dimensiones de paneles según la escala
        int totalUsableWidth = craftingAreaWidth - 40;
        int spacing;

        if (guiScale >= 4) {
            // En escala 4, usar distribución más compacta y reducir espaciado
            spacing = 10;
            // Distribuir espacio con prioridad para recetas e info
            uiState.setRecipesWidth((int)(totalUsableWidth * 0.45f));
            uiState.setInfoWidth((int)(totalUsableWidth * 0.35f));
            uiState.setQueueWidth((int)(totalUsableWidth * 0.20f)); // Aumentar ligeramente
        } else {
            spacing = 20;
            uiState.setRecipesWidth((int)(totalUsableWidth * 0.42f));
            uiState.setInfoWidth((int)(totalUsableWidth * 0.32f));
            uiState.setQueueWidth((int)(totalUsableWidth * 0.26f)); // Aumentar para evitar desbordamiento
        }

        // Calcular alturas de paneles para que quepan en la pantalla
        int availableHeight = screenHeight - craftingY - 40;
        int craftingAreaHeight;
        if (guiScale >= 4) {
            craftingAreaHeight = Math.min(380, availableHeight); // Permitir un poco menos de altura
        } else {
            craftingAreaHeight = Math.min(400, availableHeight);
        }
        uiState.setCraftingAreaHeight(craftingAreaHeight);

        int recipesPanelHeight = craftingAreaHeight - 60;
        int infoPanelHeight = craftingAreaHeight - 80;
        int queuePanelHeight = craftingAreaHeight - 80;

        // Calcular posición centrada en X
        int startX = (screenWidth - craftingAreaWidth) / 2;

        // Fondo general del área de crafteo
        uiRenderer.renderInventoryBackground(guiGraphics, startX, craftingY, craftingAreaWidth, craftingAreaHeight);

        // Renderizar categorías
        categoryManager.renderCategories(guiGraphics, startX + 15, craftingY + 15);

        // Posiciones X para los paneles, con distribución proporcional
        int recipesX = startX + 20;
        int infoX = recipesX + uiState.getRecipesWidth() + spacing;
        int queueX = infoX + uiState.getInfoWidth() + spacing;

        // Ajustar el ancho de la cola para asegurarnos de que no sobresale
        int maxQueueX = startX + craftingAreaWidth - 20;
        if (queueX + uiState.getQueueWidth() > maxQueueX) {
            uiState.setQueueWidth(maxQueueX - queueX);
        }

        // Renderizar paneles
        recipePanel.render(guiGraphics, recipesX, craftingY + 50, uiState.getRecipesWidth(), recipesPanelHeight);
        recipeInfoPanel.render(guiGraphics, infoX, craftingY + 50, uiState.getInfoWidth(), infoPanelHeight);

        // Cola de crafteo
        List<CraftingQueueItem> queue = CraftingQueueHandler.getPlayerQueue(screen.getPlayer().getUUID());
        if (!queue.isEmpty()) {
            uiRenderer.renderInventoryBackground(guiGraphics, queueX - 5, craftingY + 50, uiState.getQueueWidth() + 10, queuePanelHeight);
            queueRenderer.renderCraftingQueue(guiGraphics, queueX, craftingY + 50, uiState.getQueueWidth(), queuePanelHeight);
        }
    }

    /**
     * Maneja los clics de mouse en la interfaz de crafteo.
     *
     * @param mouseX Posición X del mouse
     * @param mouseY Posición Y del mouse
     * @param button Botón del mouse
     * @return true si el evento fue manejado
     */
    public boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        // Verificar clic en categorías
        if (categoryManager.handleCategoryClick(mouseX, mouseY, button)) {
            return true;
        }

        // Verificar clic en recetas
        if (recipePanel.handleMouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Verificar clic en panel de información
        if (recipeInfoPanel.handleMouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Verificar clic en cola de crafteo
        if (queueRenderer.handleMouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return false;
    }
}