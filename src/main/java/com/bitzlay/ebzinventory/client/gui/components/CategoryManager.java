package com.bitzlay.ebzinventory.client.gui.components;

import com.bitzlay.ebzinventory.client.gui.RustStyleInventoryScreen;
import com.bitzlay.ebzinventory.client.gui.model.ItemCategory;
import com.bitzlay.ebzinventory.client.gui.model.UIState;
import com.bitzlay.ebzinventory.recipe.InventoryRecipe;
import com.bitzlay.ebzinventory.recipe.InventoryRecipeManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Gestiona las categorías de crafteo y su renderizado.
 */
public class CategoryManager {
    /** Referencia a la pantalla principal */
    private final RustStyleInventoryScreen screen;

    /** Estado compartido de UI */
    private final UIState uiState;

    /**
     * Constructor del gestor de categorías.
     *
     * @param screen Pantalla principal
     * @param uiState Estado compartido de UI
     */
    public CategoryManager(RustStyleInventoryScreen screen, UIState uiState) {
        this.screen = screen;
        this.uiState = uiState;
    }

    /**
     * Renderiza los botones de categoría.
     *
     * @param guiGraphics Contexto de renderizado
     * @param x Posición X inicial
     * @param y Posición Y
     */
    public void renderCategories(GuiGraphics guiGraphics, int x, int y) {
        int buttonSize = 20;
        int spacing = 5;
        int originalX = x; // Guardar posición inicial para mouseClicked

        Map<String, ItemCategory> categories = uiState.getCategories();
        String currentCategory = uiState.getCurrentCategory();

        for (ItemCategory category : categories.values()) {
            boolean selected = category.getId().equals(currentCategory);

            // Background del botón
            int color = selected ? 0xFF4A4A4A : 0xFF333333;
            guiGraphics.fill(x, y, x + buttonSize, y + buttonSize, color);

            // Icono del item
            guiGraphics.renderItem(new ItemStack(category.getIcon()), x + 2, y + 2);

            // Tooltip con nombre de categoría
            if (screen.getxMouse() >= x && screen.getxMouse() < x + buttonSize &&
                    screen.getyMouse() >= y && screen.getyMouse() < y + buttonSize) {
                guiGraphics.renderTooltip(screen.getMinecraftInstance().font,
                        net.minecraft.network.chat.Component.literal(category.getName()),
                        (int)screen.getxMouse(), (int)screen.getyMouse());
            }

            x += buttonSize + spacing;
        }

        // Guardar coordenadas para mouseClicked
        uiState.setCategoryButtonsStartX(originalX);
        uiState.setCategoryButtonsY(y);
        uiState.setCategoryButtonsEndX(x - spacing);
    }

    /**
     * Maneja los clics en botones de categoría.
     *
     * @param mouseX Posición X del mouse
     * @param mouseY Posición Y del mouse
     * @param button Botón del mouse
     * @return true si el clic fue manejado
     */
    public boolean handleCategoryClick(double mouseX, double mouseY, int button) {
        // Solo manejar clics izquierdos (button == 0)
        if (button != 0) return false;

        // Verificar si el clic está en el área de botones de categoría
        if (mouseX >= uiState.getCategoryButtonsStartX() &&
                mouseX <= uiState.getCategoryButtonsEndX() &&
                mouseY >= uiState.getCategoryButtonsY() &&
                mouseY <= uiState.getCategoryButtonsY() + 20) {

            int buttonSize = 20;
            int spacing = 5;
            int x = uiState.getCategoryButtonsStartX();

            Map<String, ItemCategory> categories = uiState.getCategories();

            // Determinar qué categoría fue clicada
            for (ItemCategory category : categories.values()) {
                if (mouseX >= x && mouseX < x + buttonSize) {
                    // Cambiar a esta categoría
                    if (!category.getId().equals(uiState.getCurrentCategory())) {
                        uiState.setCurrentCategory(category.getId());
                        uiState.setCurrentPage(0); // Reiniciar página al cambiar de categoría
                        screen.resetWidgets(); // Actualizar widgets para la nueva categoría

                        // Cargar recetas para esta categoría
                        List<InventoryRecipe> recipes = getRecipesForCategory(category.getId());
                        uiState.setCurrentRecipes(recipes);

                        // Reproducir sonido para feedback
                        if (screen.getPlayer() != null) {
                            screen.getPlayer().playSound(
                                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),
                                    1.0F, 1.0F);
                        }
                    }
                    return true;
                }
                x += buttonSize + spacing;
            }
            return true;
        }

        return false;
    }

    /**
     * Obtiene las recetas para una categoría.
     *
     * @param categoryId ID de la categoría
     * @return Lista de recetas en la categoría
     */
    private List<InventoryRecipe> getRecipesForCategory(String categoryId) {
        List<InventoryRecipe> recipes = InventoryRecipeManager.getRecipesByCategory(categoryId);

        // Si la categoría actual no tiene recetas, buscar la primera categoría que tenga recetas
        if (recipes.isEmpty() && !uiState.getCategories().isEmpty()) {
            for (String catId : uiState.getCategories().keySet()) {
                List<InventoryRecipe> categoryRecipes = InventoryRecipeManager.getRecipesByCategory(catId);
                if (!categoryRecipes.isEmpty()) {
                    // Actualizar la categoría actual si encontramos una con recetas
                    if (!catId.equals(categoryId)) {
                        uiState.setCurrentCategory(catId);
                        recipes = categoryRecipes;
                    }
                    break;
                }
            }
        }

        return recipes;
    }
}