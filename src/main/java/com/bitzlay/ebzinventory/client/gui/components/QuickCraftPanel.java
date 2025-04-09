package com.bitzlay.ebzinventory.client.gui.components;

import com.bitzlay.ebzinventory.client.gui.RustStyleInventoryScreen;
import com.bitzlay.ebzinventory.client.gui.model.ItemCategory;
import com.bitzlay.ebzinventory.client.gui.model.UIState;
import com.bitzlay.ebzinventory.client.gui.render.UIRenderer;
import com.bitzlay.ebzinventory.crafting.CraftingHelper;
import com.bitzlay.ebzinventory.recipe.InventoryRecipe;
import com.bitzlay.ebzinventory.recipe.InventoryRecipeManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Panel para mostrar crafteos rápidos directamente en el inventario.
 */
public class QuickCraftPanel {
    /** Referencia a la pantalla principal */
    private final RustStyleInventoryScreen screen;

    /** Estado compartido de UI */
    private final UIState uiState;

    /** Renderizador compartido de UI */
    private final UIRenderer uiRenderer;

    /** Lista de recetas disponibles para crafteo rápido */
    private List<InventoryRecipe> availableRecipes = new ArrayList<>();

    /** Temporizador para actualización de recetas */
    private int updateTicker = 0;

    /** Intervalo de actualización (en ticks, 20 ticks = 1 segundo) */
    private static final int UPDATE_INTERVAL = 10;  // Actualización más frecuente

    /** Página actual de los crafteos rápidos */
    private int currentPage = 0;

    /** Cantidad máxima de recetas por página */
    private int maxRecipesPerPage = 12;

    /**
     * Constructor del panel de crafteo rápido.
     *
     * @param screen Pantalla principal
     * @param uiState Estado compartido de UI
     */
    public QuickCraftPanel(RustStyleInventoryScreen screen, UIState uiState) {
        this.screen = screen;
        this.uiState = uiState;
        this.uiRenderer = new UIRenderer();
    }

    /**
     * Inicializa el panel.
     */
    public void init() {
        // Actualizar recetas disponibles
        refreshAvailableRecipes();
        currentPage = 0;
    }

    /**
     * Actualiza las recetas periódicamente durante el renderizado
     */
    public void update() {
        updateTicker++;
        if (updateTicker >= UPDATE_INTERVAL) {
            refreshAvailableRecipes();
            updateTicker = 0;
        }
    }

    /**
     * Renderiza el panel de crafteo rápido.
     *
     * @param guiGraphics Contexto de renderizado
     * @param mouseX Posición X del mouse
     * @param mouseY Posición Y del mouse
     */
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Solo mostrar crafteos rápidos si no estamos en el modo de crafteo completo
        if (uiState.isShowCrafting()) return;

        // Actualizar recetas periódicamente
        update();

        // Si no hay recetas disponibles, mostrar panel vacío
        boolean noRecipes = availableRecipes.isEmpty();

        // Obtener la escala para ajustes
        float guiScale = uiState.getGuiScale();

        // Calcular dimensiones del panel
        int screenWidth = screen.getScreenWidth();
        int slotSize = uiState.getSlotSize();
        int slotSpacing = uiState.getSlotSpacing();

        // Ancho fijo basado en 6 slots
        int panelWidth = 6 * (slotSize + slotSpacing) + 20;

        // Calcular recetas totales y páginas
        int totalPages = noRecipes ? 1 : (int) Math.ceil((double) availableRecipes.size() / maxRecipesPerPage);

        // Validar página actual
        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }

        // Calcular índices de recetas para esta página
        int startIndex = currentPage * maxRecipesPerPage;
        int endIndex = Math.min(startIndex + maxRecipesPerPage, availableRecipes.size());

        // Configuración para el layout de recetas
        int recipesPerColumn = 6;  // 6 recetas en la primera columna
        int visibleRecipes = noRecipes ? 0 : (endIndex - startIndex);
        int columns = Math.min(2, noRecipes ? 1 : (visibleRecipes + recipesPerColumn - 1) / recipesPerColumn);
        int rows = Math.min(recipesPerColumn, noRecipes ? 1 : (visibleRecipes + columns - 1) / columns);

        // Altura mínima incluso si no hay recetas, añadiendo espacio para la navegación
        int panelHeight = Math.max(100, (rows * (slotSize + slotSpacing)) + 50);

        // Posicionamiento para alinear con el borde inferior del inventario
        int inventoryBottom = screen.getScreenHeight() - 10;

        // CAMBIO: Posicionar el panel para que su borde inferior coincida con el borde inferior del inventario
        int panelX = screenWidth - panelWidth - 10;
        int panelY = inventoryBottom - panelHeight;

        // Dibujar panel de fondo con borde más visible
        guiGraphics.fill(panelX - 2, panelY - 2, panelX + panelWidth + 2, panelY + panelHeight + 2, 0xFF555555);
        uiRenderer.renderInventoryBackground(guiGraphics, panelX, panelY, panelWidth, panelHeight);

        // Título del panel con mejor visibilidad
        guiGraphics.drawString(screen.getMinecraftInstance().font,
                Component.literal("§e§lCrafteos Rápidos"),
                panelX + 10, panelY + 6, 0xFFFFFF);

        // Mostrar mensaje si no hay recetas
        if (noRecipes) {
            guiGraphics.drawString(screen.getMinecraftInstance().font,
                    Component.literal("§7No hay crafteos disponibles"),
                    panelX + 10, panelY + 30, 0xAAAAAA);
            return;
        }

        // Calcular espacio disponible para los items
        int itemAreaX = panelX + 10;
        int itemAreaY = panelY + 25;  // Más espacio para el título

        // Configurar tamaño y espaciado de los iconos de crafteo rápido
        int itemSize = slotSize;
        int itemSpacing = slotSpacing + 2;  // Mayor separación para mejor visibilidad

        // Renderizar grid de items
        for (int i = startIndex; i < endIndex; i++) {
            int relativeIndex = i - startIndex;
            int col = relativeIndex / rows;
            int row = relativeIndex % rows;

            InventoryRecipe recipe = availableRecipes.get(i);

            int x = itemAreaX + col * (itemSize + 8 + itemSpacing * 2);
            int y = itemAreaY + row * (itemSize + itemSpacing);

            // Fondo del item más destacado
            boolean hovered = mouseX >= x && mouseX < x + itemSize &&
                    mouseY >= y && mouseY < y + itemSize;

            // Color de fondo más vivo cuando está seleccionado
            int bgColor = hovered ? 0xFF5A5A5A : 0xFF333333;
            guiGraphics.fill(x - 1, y - 1, x + itemSize + 1, y + itemSize + 1, 0xFF666666);
            guiGraphics.fill(x, y, x + itemSize, y + itemSize, bgColor);

            // Renderizar item
            ItemStack resultItem = recipe.getResult();
            guiGraphics.renderItem(resultItem, x + (itemSize - 16) / 2, y + (itemSize - 16) / 2);

            // Renderizar cantidad de resultado con sombra
            guiGraphics.renderItemDecorations(screen.getMinecraftInstance().font, resultItem,
                    x + (itemSize - 16) / 2, y + (itemSize - 16) / 2);

            // Tooltip mejorado
            if (hovered) {
                List<Component> tooltipLines = new ArrayList<>();
                tooltipLines.add(Component.literal("§e§l" + recipe.getDisplayName()));
                tooltipLines.add(Component.literal("§7Click para craftear"));

                // Añadir ingredientes al tooltip con mejor formato
                tooltipLines.add(Component.literal(""));
                tooltipLines.add(Component.literal("§6§lMateriales:"));
                for (Map.Entry<Item, Integer> ingredient : recipe.getIngredients().entrySet()) {
                    int playerHas = screen.getPlayer().getInventory().countItem(ingredient.getKey());
                    String color = playerHas >= ingredient.getValue() ? "§a" : "§c";
                    tooltipLines.add(Component.literal(
                            color + ingredient.getValue() + "x " + new ItemStack(ingredient.getKey()).getHoverName().getString() +
                                    " §7(" + playerHas + " disponibles)"));
                }

                guiGraphics.renderComponentTooltip(screen.getMinecraftInstance().font, tooltipLines, mouseX, mouseY);
            }
        }

        // Renderizar controles de navegación si hay más de una página
        if (totalPages > 1) {
            int navY = panelY + panelHeight - 20;

            // Indicador de página
            String pageInfo = (currentPage + 1) + " / " + totalPages;
            int pageInfoWidth = screen.getMinecraftInstance().font.width(pageInfo);
            int pageInfoX = panelX + (panelWidth - pageInfoWidth) / 2;
            guiGraphics.drawString(screen.getMinecraftInstance().font, pageInfo, pageInfoX, navY + 6, 0xFFFFFFFF);

            // Botón página anterior
            if (currentPage > 0) {
                int prevX = panelX + 10;
                int prevWidth = 20;
                boolean hoveredPrev = mouseX >= prevX && mouseX < prevX + prevWidth &&
                        mouseY >= navY && mouseY < navY + 20;

                // Fondo del botón
                int prevBgColor = hoveredPrev ? 0xFF555555 : 0xFF333333;
                guiGraphics.fill(prevX, navY, prevX + prevWidth, navY + 20, prevBgColor);

                // Bordes
                guiGraphics.fill(prevX, navY, prevX + prevWidth, navY + 1, 0xFF666666);
                guiGraphics.fill(prevX, navY, prevX + 1, navY + 20, 0xFF666666);
                guiGraphics.fill(prevX, navY + 19, prevX + prevWidth, navY + 20, 0xFF222222);
                guiGraphics.fill(prevX + prevWidth - 1, navY, prevX + prevWidth, navY + 20, 0xFF222222);

                // Símbolo '<'
                guiGraphics.drawString(screen.getMinecraftInstance().font, "<", prevX + 7, navY + 6, 0xFFFFFFFF);

                // Tooltip
                if (hoveredPrev) {
                    guiGraphics.renderTooltip(screen.getMinecraftInstance().font,
                            Component.literal("Página anterior"), mouseX, mouseY);
                }

                // Procesar clic (en handleMouseClicked)
            }

            // Botón página siguiente
            if (currentPage < totalPages - 1) {
                int nextX = panelX + panelWidth - 30;
                int nextWidth = 20;
                boolean hoveredNext = mouseX >= nextX && mouseX < nextX + nextWidth &&
                        mouseY >= navY && mouseY < navY + 20;

                // Fondo del botón
                int nextBgColor = hoveredNext ? 0xFF555555 : 0xFF333333;
                guiGraphics.fill(nextX, navY, nextX + nextWidth, navY + 20, nextBgColor);

                // Bordes
                guiGraphics.fill(nextX, navY, nextX + nextWidth, navY + 1, 0xFF666666);
                guiGraphics.fill(nextX, navY, nextX + 1, navY + 20, 0xFF666666);
                guiGraphics.fill(nextX, navY + 19, nextX + nextWidth, navY + 20, 0xFF222222);
                guiGraphics.fill(nextX + nextWidth - 1, navY, nextX + nextWidth, navY + 20, 0xFF222222);

                // Símbolo '>'
                guiGraphics.drawString(screen.getMinecraftInstance().font, ">", nextX + 7, navY + 6, 0xFFFFFFFF);

                // Tooltip
                if (hoveredNext) {
                    guiGraphics.renderTooltip(screen.getMinecraftInstance().font,
                            Component.literal("Página siguiente"), mouseX, mouseY);
                }

                // Procesar clic (en handleMouseClicked)
            }
        }
    }

    /**
     * Maneja los clics en el panel de crafteo rápido.
     *
     * @param mouseX Posición X del mouse
     * @param mouseY Posición Y del mouse
     * @param button Botón del mouse
     * @return true si el clic fue manejado
     */
    public boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        // Solo procesar si no estamos en modo crafteo y es un clic izquierdo
        if (uiState.isShowCrafting() || button != 0) return false;

        // Si no hay recetas disponibles, ignorar
        if (availableRecipes.isEmpty()) return false;

        // Calcular la misma posición que en render()
        int screenWidth = screen.getScreenWidth();
        float guiScale = uiState.getGuiScale();

        int slotSize = uiState.getSlotSize();
        int slotSpacing = uiState.getSlotSpacing();

        int panelWidth = 6 * (slotSize + slotSpacing) + 20;

        // Calcular recetas totales y páginas
        int totalPages = (int) Math.ceil((double) availableRecipes.size() / maxRecipesPerPage);

        // Índices de recetas para esta página
        int startIndex = currentPage * maxRecipesPerPage;
        int endIndex = Math.min(startIndex + maxRecipesPerPage, availableRecipes.size());

        // Configuración para el layout de recetas
        int recipesPerColumn = 6;
        int visibleRecipes = endIndex - startIndex;
        int columns = Math.min(2, (visibleRecipes + recipesPerColumn - 1) / recipesPerColumn);
        int rows = Math.min(recipesPerColumn, (visibleRecipes + columns - 1) / columns);

        int panelHeight = Math.max(100, (rows * (slotSize + slotSpacing)) + 50);

        int inventoryBottom = screen.getScreenHeight() - 10;
        int panelX = screenWidth - panelWidth - 10;
        int panelY = inventoryBottom - panelHeight;

        // Si el clic no está en el área del panel, ignorar
        if (mouseX < panelX || mouseX >= panelX + panelWidth ||
                mouseY < panelY || mouseY >= panelY + panelHeight) {
            return false;
        }

        // Verificar clics en los controles de navegación
        if (totalPages > 1) {
            int navY = panelY + panelHeight - 20;

            // Botón página anterior
            if (currentPage > 0) {
                int prevX = panelX + 10;
                int prevWidth = 20;

                if (mouseX >= prevX && mouseX < prevX + prevWidth &&
                        mouseY >= navY && mouseY < navY + 20) {
                    // Página anterior
                    currentPage--;
                    playClickSound();
                    return true;
                }
            }

            // Botón página siguiente
            if (currentPage < totalPages - 1) {
                int nextX = panelX + panelWidth - 30;
                int nextWidth = 20;

                if (mouseX >= nextX && mouseX < nextX + nextWidth &&
                        mouseY >= navY && mouseY < navY + 20) {
                    // Página siguiente
                    currentPage++;
                    playClickSound();
                    return true;
                }
            }
        }

        // Verificar clics en recetas
        int itemAreaX = panelX + 10;
        int itemAreaY = panelY + 25;
        int itemSize = slotSize;
        int itemSpacing = slotSpacing + 2;

        // Verificar en cuál item se hizo clic
        for (int i = 0; i < Math.min(maxRecipesPerPage, endIndex - startIndex); i++) {
            int col = i / rows;
            int row = i % rows;

            int x = itemAreaX + col * (itemSize + 8 + itemSpacing * 2);
            int y = itemAreaY + row * (itemSize + itemSpacing);

            if (mouseX >= x && mouseX < x + itemSize &&
                    mouseY >= y && mouseY < y + itemSize) {

                // Iniciar crafteo del item seleccionado
                int recipeIndex = startIndex + i;
                if (recipeIndex < availableRecipes.size()) {
                    startCrafting(availableRecipes.get(recipeIndex));
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Reproduce un sonido de clic para feedback.
     */
    private void playClickSound() {
        if (screen.getPlayer() != null) {
            screen.getPlayer().playSound(
                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),
                    1.0F, 1.0F);
        }
    }

    /**
     * Inicia el crafteo de una receta.
     *
     * @param recipe Receta a craftear
     */
    private void startCrafting(InventoryRecipe recipe) {
        if (screen.getPlayer() == null) return;

        // Usar CraftingHelper para manejar todo el proceso de crafteo
        boolean success = CraftingHelper.startCrafting(screen.getPlayer(), recipe);

        // Si el crafteo fue exitoso, actualizar la lista de recetas
        if (success) {
            refreshAvailableRecipes();
        }
    }

    /**
     * Actualiza la lista de recetas disponibles para crafteo rápido.
     */
    public void refreshAvailableRecipes() {
        if (screen.getMinecraftInstance() == null || screen.getPlayer() == null) {
            availableRecipes = Collections.emptyList();
            return;
        }

        availableRecipes = new ArrayList<>();

        // Obtener todas las recetas disponibles de todas las categorías
        Map<String, ItemCategory> categories = uiState.getCategories();
        for (String category : categories.keySet()) {
            List<InventoryRecipe> categoryRecipes = InventoryRecipeManager.getRecipesByCategory(category);

            for (InventoryRecipe recipe : categoryRecipes) {
                if (CraftingHelper.canCraft(screen.getPlayer(), recipe)) {
                    availableRecipes.add(recipe);
                }
            }
        }

        // Validar página actual con el nuevo número de recetas
        int totalPages = availableRecipes.isEmpty() ? 1 :
                (int) Math.ceil((double) availableRecipes.size() / maxRecipesPerPage);
        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }
    }
}