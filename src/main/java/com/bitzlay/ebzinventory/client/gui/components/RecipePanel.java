package com.bitzlay.ebzinventory.client.gui.components;

import com.bitzlay.ebzinventory.client.gui.RustStyleInventoryScreen;
import com.bitzlay.ebzinventory.client.gui.model.UIState;
import com.bitzlay.ebzinventory.client.gui.render.UIRenderer;
import com.bitzlay.ebzinventory.recipe.InventoryRecipe;
import com.bitzlay.ebzinventory.recipe.InventoryRecipeManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Panel que muestra las recetas disponibles, con paginación y filtrado por categoría.
 */
public class RecipePanel {
    /** Referencia a la pantalla principal */
    private final RustStyleInventoryScreen screen;

    /** Estado compartido de UI */
    private final UIState uiState;

    /** Renderizador de UI */
    private final UIRenderer uiRenderer;

    /**
     * Constructor del panel de recetas.
     *
     * @param screen Pantalla principal
     * @param uiState Estado compartido de UI
     */
    public RecipePanel(RustStyleInventoryScreen screen, UIState uiState) {
        this.screen = screen;
        this.uiState = uiState;
        this.uiRenderer = new UIRenderer();
    }

    /**
     * Inicializa el panel de recetas.
     */
    public void init() {
        // Cargar recetas para la categoría actual
        loadRecipesForCurrentCategory();
    }

    /**
     * Renderiza el panel de recetas.
     *
     * @param guiGraphics Contexto de renderizado
     * @param x Posición X
     * @param y Posición Y
     * @param width Ancho
     * @param panelHeight Alto
     */
    public void render(GuiGraphics guiGraphics, int x, int y, int width, int panelHeight) {
        // Renderizar panel con título
        uiRenderer.renderPanel(guiGraphics, x, y, width, panelHeight, "Recetas Disponibles", screen.getMinecraftInstance().font);

        // Obtener recetas actuales
        List<InventoryRecipe> recipes = uiState.getCurrentRecipes();
        if (recipes.isEmpty()) {
            guiGraphics.drawString(screen.getMinecraftInstance().font, "No hay recetas disponibles", x + 10, y + 30, 0xAAAAAA);
            return;
        }

        // Detectar la escala actual
        float guiScale = uiState.getGuiScale();

        // Ajustar tamaños según la escala
        int buttonHeight;
        int spacing;
        int minButtonWidth;

        if (guiScale >= 4) {
            // Configuración para escala 4 (más compacta)
            buttonHeight = 32;
            spacing = 4;
            minButtonWidth = 100;
        } else if (guiScale >= 3) {
            // Configuración para escala 3
            buttonHeight = 36;
            spacing = 5;
            minButtonWidth = 110;
        } else {
            // Configuración estándar
            buttonHeight = 40;
            spacing = 6;
            minButtonWidth = 120;
        }

        // Calcular el número de columnas basado en el ancho disponible
        int maxButtonsPerRow = Math.max(1, (width - 20) / minButtonWidth);
        int buttonWidth = (width - 20 - (spacing * (maxButtonsPerRow - 1))) / maxButtonsPerRow;

        // Asegurar que cada botón tenga suficiente espacio horizontal
        if (buttonWidth < minButtonWidth) {
            maxButtonsPerRow = Math.max(1, (width - 20) / minButtonWidth);
            buttonWidth = (width - 20 - (spacing * (maxButtonsPerRow - 1))) / maxButtonsPerRow;
        }

        // Calcular cuántas filas necesitamos por página
        int availableRowHeight = panelHeight - 80; // Restar espacio para título y navegación
        int rowsPerPage = Math.max(1, availableRowHeight / (buttonHeight + spacing));

        // Calcular recetas por página
        int recipesPerPage = maxButtonsPerRow * rowsPerPage;

        int totalPages = Math.max(1, (recipes.size() - 1) / recipesPerPage + 1);

        // Validar página actual
        int currentPage = uiState.getCurrentPage();
        if (currentPage >= totalPages) {
            currentPage = 0;
            uiState.setCurrentPage(0);
        }

        int startIndex = currentPage * recipesPerPage;
        int endIndex = Math.min(startIndex + recipesPerPage, recipes.size());

        // Ajustar margen superior para recetas en escala 4
        int contentStartY = guiScale >= 4 ? y + 22 : y + 25;
        int maxTextWidth = buttonWidth - 45; // Espacio para texto en el botón

        // Limpiar widgets existentes y mantener el botón de crafteo
        screen.resetWidgets();

        // Renderizar recetas para la página actual
        for (int i = startIndex; i < endIndex; i++) {
            int relativeIndex = i - startIndex;
            int row = relativeIndex / maxButtonsPerRow;
            int col = relativeIndex % maxButtonsPerRow;
            int buttonX = x + 10 + (col * (buttonWidth + spacing));
            int buttonY = contentStartY + (row * (buttonHeight + spacing));

            // Usar final para variables capturadas en lambda
            final InventoryRecipe recipe = recipes.get(i);
            String truncatedName = screen.getMinecraftInstance().font.plainSubstrByWidth(recipe.getDisplayName(), maxTextWidth);

            InventoryRecipeButton recipeButton = new InventoryRecipeButton(
                    buttonX,
                    buttonY,
                    buttonWidth,
                    buttonHeight,
                    recipe.getId(),
                    recipe.getResult(),
                    button -> selectRecipe(recipe),
                    recipe == uiState.getSelectedRecipe(),
                    truncatedName
            );
            screen.addWidget(recipeButton);
        }

        // Ajustar tamaños de botones de navegación para escala 4
        int navButtonWidth = guiScale >= 4 ? 70 : 80;
        int navButtonY = y + panelHeight - 25;

        // Botones de navegación
        if (totalPages > 1) {
            if (currentPage > 0) {
                final int prevPage = currentPage - 1;
                Button prevButton = Button.builder(Component.literal("< Anterior"), b -> {
                    uiState.setCurrentPage(prevPage);
                    screen.resetWidgets();
                }).pos(x + 10, navButtonY).size(navButtonWidth, 20).build();
                screen.addWidget(prevButton);
            }

            if (currentPage < totalPages - 1) {
                final int nextPage = currentPage + 1;
                Button nextButton = Button.builder(Component.literal("Siguiente >"), b -> {
                    uiState.setCurrentPage(nextPage);
                    screen.resetWidgets();
                }).pos(x + width - navButtonWidth - 10, navButtonY).size(navButtonWidth, 20).build();
                screen.addWidget(nextButton);
            }
        }
    }

    /**
     * Selecciona una receta.
     *
     * @param recipe Receta a seleccionar
     */
    private void selectRecipe(InventoryRecipe recipe) {
        if (uiState.getSelectedRecipe() == recipe) {
            uiState.setSelectedRecipe(null);
        } else {
            uiState.setSelectedRecipe(recipe);
        }

        if (screen.getPlayer() != null) {
            screen.getPlayer().playSound(
                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),
                    1.0F, 1.0F
            );
        }

        // Actualizar interfaz sin limpiar todos los widgets
        screen.resetWidgets();
    }

    /**
     * Carga las recetas para la categoría actual.
     */
    private void loadRecipesForCurrentCategory() {
        List<InventoryRecipe> recipes = InventoryRecipeManager.getRecipesByCategory(uiState.getCurrentCategory());
        uiState.setCurrentRecipes(recipes);
    }

    /**
     * Maneja los clics de mouse en el panel de recetas.
     *
     * @param mouseX Posición X del mouse
     * @param mouseY Posición Y del mouse
     * @param button Botón del mouse
     * @return true si el clic fue manejado
     */
    public boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        // Los clicks se manejan automáticamente por los widgets de Minecraft
        return false;
    }
}