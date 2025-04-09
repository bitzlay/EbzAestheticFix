package com.bitzlay.ebzinventory.client.gui.model;

import com.bitzlay.ebzinventory.recipe.InventoryRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase que mantiene el estado compartido de la interfaz de usuario.
 * Centraliza las variables de estado para facilitar su acceso desde diferentes componentes.
 */
public class UIState {
    // Estado del modo de pantalla
    private boolean showCrafting = false;

    // Estado de la navegación
    private String currentCategory = "CA";
    private int currentPage = 0;
    private int queuePage = 0;

    // Estado de selección
    private InventoryRecipe selectedRecipe = null;
    private List<InventoryRecipe> currentRecipes = new ArrayList<>();

    // Dimensiones de UI calculadas dinámicamente
    private int slotSize;
    private int slotSpacing;
    private int recipesWidth;
    private int infoWidth;
    private int queueWidth;
    private int craftingAreaHeight;
    private int queueCardHeight = 25;
    private int queueSpacing = 4;
    private float guiScale = 1.0f;

    // Categorías disponibles
    private static final Map<String, ItemCategory> CATEGORIES = new HashMap<>();
    static {
        CATEGORIES.put("CA", new ItemCategory("CA", "Armas", Items.IRON_SWORD));
        CATEGORIES.put("CB", new ItemCategory("CB", "Bloques", Items.STONE));
        CATEGORIES.put("CC", new ItemCategory("CC", "Componentes", Items.REDSTONE));
        CATEGORIES.put("CD", new ItemCategory("CD", "Decoración", Items.PAINTING));
        CATEGORIES.put("CM", new ItemCategory("CM", "Mecánica", Items.PISTON));
    }

    // Coordenadas para manejo de clics en categorías
    private int categoryButtonsStartX;
    private int categoryButtonsY;
    private int categoryButtonsEndX;

    /**
     * Constructor por defecto con valores iniciales.
     */
    public UIState() {
        // Valores iniciales
        this.slotSize = 27; // 18 * 1.5
        this.slotSpacing = 2;
        this.craftingAreaHeight = 180;
    }

    /**
     * Alterna entre el modo inventario y el modo crafteo.
     */
    public void toggleShowCrafting() {
        this.showCrafting = !this.showCrafting;
    }

    // Getters y setters

    public boolean isShowCrafting() {
        return showCrafting;
    }

    public void setShowCrafting(boolean showCrafting) {
        this.showCrafting = showCrafting;
    }

    public String getCurrentCategory() {
        return currentCategory;
    }

    public void setCurrentCategory(String currentCategory) {
        this.currentCategory = currentCategory;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public InventoryRecipe getSelectedRecipe() {
        return selectedRecipe;
    }

    public void setSelectedRecipe(InventoryRecipe selectedRecipe) {
        this.selectedRecipe = selectedRecipe;
    }

    public List<InventoryRecipe> getCurrentRecipes() {
        return currentRecipes;
    }

    public void setCurrentRecipes(List<InventoryRecipe> currentRecipes) {
        this.currentRecipes = currentRecipes;
    }

    public int getSlotSize() {
        return slotSize;
    }

    public void setSlotSize(int slotSize) {
        this.slotSize = slotSize;
    }

    public int getSlotSpacing() {
        return slotSpacing;
    }

    public void setSlotSpacing(int slotSpacing) {
        this.slotSpacing = slotSpacing;
    }

    public int getRecipesWidth() {
        return recipesWidth;
    }

    public void setRecipesWidth(int recipesWidth) {
        this.recipesWidth = recipesWidth;
    }

    public int getInfoWidth() {
        return infoWidth;
    }

    public void setInfoWidth(int infoWidth) {
        this.infoWidth = infoWidth;
    }

    public int getQueueWidth() {
        return queueWidth;
    }

    public void setQueueWidth(int queueWidth) {
        this.queueWidth = queueWidth;
    }

    public int getCraftingAreaHeight() {
        return craftingAreaHeight;
    }

    public void setCraftingAreaHeight(int craftingAreaHeight) {
        this.craftingAreaHeight = craftingAreaHeight;
    }

    public int getQueueCardHeight() {
        return queueCardHeight;
    }

    public void setQueueCardHeight(int queueCardHeight) {
        this.queueCardHeight = queueCardHeight;
    }

    public int getQueueSpacing() {
        return queueSpacing;
    }

    public void setQueueSpacing(int queueSpacing) {
        this.queueSpacing = queueSpacing;
    }

    public float getGuiScale() {
        return guiScale;
    }

    public void setGuiScale(float guiScale) {
        this.guiScale = guiScale;
    }

    public int getQueuePage() {
        return queuePage;
    }

    public void setQueuePage(int queuePage) {
        this.queuePage = queuePage;
    }

    public Map<String, ItemCategory> getCategories() {
        return CATEGORIES;
    }

    public int getCategoryButtonsStartX() {
        return categoryButtonsStartX;
    }

    public void setCategoryButtonsStartX(int categoryButtonsStartX) {
        this.categoryButtonsStartX = categoryButtonsStartX;
    }

    public int getCategoryButtonsY() {
        return categoryButtonsY;
    }

    public void setCategoryButtonsY(int categoryButtonsY) {
        this.categoryButtonsY = categoryButtonsY;
    }

    public int getCategoryButtonsEndX() {
        return categoryButtonsEndX;
    }

    public void setCategoryButtonsEndX(int categoryButtonsEndX) {
        this.categoryButtonsEndX = categoryButtonsEndX;
    }
}