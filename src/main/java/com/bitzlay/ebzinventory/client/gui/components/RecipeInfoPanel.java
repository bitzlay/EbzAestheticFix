package com.bitzlay.ebzinventory.client.gui.components;

import com.bitzlay.ebzinventory.client.gui.RustStyleInventoryScreen;
import com.bitzlay.ebzinventory.client.gui.model.UIState;
import com.bitzlay.ebzinventory.client.gui.render.UIRenderer;
import com.bitzlay.ebzinventory.crafting.CraftingHelper;
import com.bitzlay.ebzinventory.crafting.CraftingQueueHandler;
import com.bitzlay.ebzinventory.crafting.CraftingQueueItem;
import com.bitzlay.ebzinventory.recipe.InventoryRecipe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Panel que muestra información detallada de la receta seleccionada.
 */
public class RecipeInfoPanel {
    /** Referencia a la pantalla principal */
    private final RustStyleInventoryScreen screen;

    /** Estado compartido de UI */
    private final UIState uiState;

    /** Renderizador de UI */
    private final UIRenderer uiRenderer;

    /**
     * Constructor del panel de información de receta.
     *
     * @param screen Pantalla principal
     * @param uiState Estado compartido de UI
     */
    public RecipeInfoPanel(RustStyleInventoryScreen screen, UIState uiState) {
        this.screen = screen;
        this.uiState = uiState;
        this.uiRenderer = new UIRenderer();
    }

    /**
     * Inicializa el panel.
     */
    public void init() {
        // No se requiere inicialización específica
    }

    /**
     * Renderiza el panel de información de receta.
     *
     * @param guiGraphics Contexto de renderizado
     * @param x Posición X
     * @param y Posición Y
     * @param width Ancho
     * @param panelHeight Alto
     */
    public void render(GuiGraphics guiGraphics, int x, int y, int width, int panelHeight) {
        InventoryRecipe selectedRecipe = uiState.getSelectedRecipe();

        if (selectedRecipe == null) {
            uiRenderer.renderPanel(guiGraphics, x, y, width, panelHeight, "Información", screen.getMinecraftInstance().font);
            guiGraphics.drawString(screen.getMinecraftInstance().font, "Selecciona una receta", x + 10, y + 30, 0xAAAAAA);
            return;
        }

        uiRenderer.renderPanel(guiGraphics, x, y, width, panelHeight, "Información", screen.getMinecraftInstance().font);

        // Detectar la escala actual
        float guiScale = uiState.getGuiScale();

        int contentX = x + 10;
        int contentY = y + 20;

        // Ajustar espaciado según la escala
        int spacing = guiScale >= 4 ? 16 : 18;

        // Nombre con estilo más grande y destacado
        guiGraphics.drawString(screen.getMinecraftInstance().font, selectedRecipe.getDisplayName(), contentX, contentY, 0xFFFFFF);
        contentY += spacing + 5;

        // Item resultante con más espacio
        guiGraphics.renderItem(selectedRecipe.getResult(), contentX, contentY);
        guiGraphics.drawString(screen.getMinecraftInstance().font, selectedRecipe.getResult().getHoverName().getString(),
                contentX + 25, contentY + 5, 0xFFFFFF);
        contentY += spacing + (guiScale >= 4 ? 10 : 15); // Menor separación en escala 4

        // Sección de materiales con título destacado
        guiGraphics.fill(contentX, contentY - 2, contentX + width - 20, contentY - 1, 0x80FFFFFF);
        guiGraphics.drawString(screen.getMinecraftInstance().font, "Materiales:", contentX, contentY, 0xFFFF55);
        contentY += spacing;

        // Verificar si hay ingredientes
        Map<Item, Integer> ingredients = selectedRecipe.getIngredients();
        if (ingredients.isEmpty()) {
            guiGraphics.drawString(screen.getMinecraftInstance().font, "No hay materiales", contentX, contentY, 0xAAAAAA);
            contentY += spacing;
        } else {
            // Calcular mejor distribución de columnas basado en el ancho disponible
            int availableWidth = width - 20;
            int itemSpacing = guiScale >= 4 ? 18 : 20; // Ajustar espaciado en escala 4
            int itemWidth = guiScale >= 4 ? 75 : 85;

            // Determinar cuántas columnas podemos usar (en escala 4, priorizar más columnas)
            int numColumns;
            if (guiScale >= 4) {
                numColumns = Math.max(1, Math.min(4, availableWidth / itemWidth));
            } else {
                numColumns = Math.max(1, Math.min(3, availableWidth / itemWidth));
            }
            int columnWidth = availableWidth / numColumns;

            // Distribuir ingredientes en filas y columnas
            int itemsPerColumn = (ingredients.size() + numColumns - 1) / numColumns;
            int currentColumn = 0;
            int currentRow = 0;

            for (Map.Entry<Item, Integer> ingredient : ingredients.entrySet()) {
                int itemX = contentX + (currentColumn * columnWidth);
                int itemY = contentY + (currentRow * itemSpacing);

                ItemStack ingredientStack = new ItemStack(ingredient.getKey());
                guiGraphics.renderItem(ingredientStack, itemX, itemY);

                // Mejorar visualización de conteo
                int playerHas = screen.getPlayer() != null ?
                        screen.getPlayer().getInventory().countItem(ingredient.getKey()) : 0;
                boolean hasEnough = playerHas >= ingredient.getValue();

                // Formato "tienes/necesitas" con colores claros
                String countText = playerHas + "/" + ingredient.getValue();
                int color = hasEnough ? 0x55FF55 : 0xFF5555;

                // Ajustar posición de texto para escala 4
                int textOffsetX = guiScale >= 4 ? 20 : 21;
                int textOffsetY = guiScale >= 4 ? 3 : 4;

                // Añadir sombreado al texto
                guiGraphics.drawString(screen.getMinecraftInstance().font, countText, itemX + textOffsetX + 1, itemY + textOffsetY + 1, 0x80000000);
                guiGraphics.drawString(screen.getMinecraftInstance().font, countText, itemX + textOffsetX, itemY + textOffsetY, color);

                currentRow++;
                if (currentRow >= itemsPerColumn) {
                    currentRow = 0;
                    currentColumn++;
                }
            }

            // Ajustar contentY para el botón de crafteo
            contentY += (itemsPerColumn * itemSpacing) + 10;
        }

        // Botón de crafteo con mejor posicionamiento
        final boolean canCraft = CraftingHelper.canCraft(screen.getPlayer(), selectedRecipe);
        final List<CraftingQueueItem> queue = CraftingQueueHandler.getPlayerQueue(screen.getPlayer().getUUID());
        final String buttonText;
        final boolean enableButton;

        if (queue.size() >= MAX_QUEUE_SIZE) {
            buttonText = "Cola llena";
            enableButton = false;
        } else if (!canCraft) {
            buttonText = "Faltan materiales";
            enableButton = false;
        } else {
            buttonText = "Craftear";
            enableButton = true;
        }

        // Ajustar tamaño de botón para escala 4
        int buttonWidth = guiScale >= 4 ? Math.min(100, width - 30) : Math.min(120, width - 40);
        int buttonHeight = guiScale >= 4 ? 22 : 25;

        // Crear referencia final para la lambda
        final InventoryRecipe finalRecipe = selectedRecipe;

        Button craftButton = Button.builder(Component.literal(buttonText),
                        button -> { if (enableButton) startCrafting(finalRecipe); })
                .pos(x + (width - buttonWidth) / 2, y + panelHeight - buttonHeight - 10)
                .size(buttonWidth, buttonHeight)
                .build();

        craftButton.active = enableButton;
        screen.addWidget(craftButton);
    }

    // Máximo tamaño de cola
    private static final int MAX_QUEUE_SIZE = 11;

    /**
     * Inicia el crafteo de una receta.
     *
     * @param recipe Receta a craftear
     */
    private void startCrafting(InventoryRecipe recipe) {
        if (screen.getPlayer() == null) return;

        // Usar CraftingHelper para manejar todo el proceso de crafteo
        boolean success = CraftingHelper.startCrafting(screen.getPlayer(), recipe);

        // No se necesita hacer nada más aquí, ya que CraftingHelper maneja todos los mensajes y sonidos
    }

    /**
     * Maneja los clics de mouse en el panel de información.
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