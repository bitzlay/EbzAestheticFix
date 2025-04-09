package com.bitzlay.ebzinventory.client.gui.render;

import com.bitzlay.ebzinventory.client.gui.RustStyleInventoryScreen;
import com.bitzlay.ebzinventory.client.gui.model.UIState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;

/**
 * Clase encargada de renderizar el inventario base y sus slots.
 */
public class InventoryRenderer {
    /** Referencia a la pantalla principal */
    private final RustStyleInventoryScreen screen;

    /** Estado compartido de UI */
    private final UIState uiState;

    /** Renderizador de UI */
    private final UIRenderer uiRenderer;

    /**
     * Constructor del renderizador de inventario.
     *
     * @param screen Pantalla principal
     * @param uiState Estado compartido de UI
     */
    public InventoryRenderer(RustStyleInventoryScreen screen, UIState uiState) {
        this.screen = screen;
        this.uiState = uiState;
        this.uiRenderer = new UIRenderer();
    }

    /**
     * Renderiza el fondo del inventario.
     *
     * @param guiGraphics Contexto de renderizado
     * @param partialTick Tick parcial para animaciones
     * @param mouseX Posición X del mouse
     * @param mouseY Posición Y del mouse
     */
    public void renderBackground(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        if (screen.getMinecraftInstance() == null || screen.getMinecraftInstance().getWindow() == null) return;

        int screenWidth = screen.getMinecraftInstance().getWindow().getGuiScaledWidth();

        // Usar exactamente las mismas fórmulas que en repositionSlots()
        int slotSize = uiState.getSlotSize();
        int slotSpacing = uiState.getSlotSpacing();

        int inventoryWidth = 9 * (slotSize + slotSpacing);
        int armorWidth = 5 * (slotSize + slotSpacing);
        int separation = Math.min(40, Math.max(10, (screenWidth - inventoryWidth - armorWidth) / 3));

        int mainInventoryX = (screenWidth + separation) / 2 - inventoryWidth / 2;

        // Ajustar si es necesario
        if (mainInventoryX + inventoryWidth > screenWidth - 10) {
            mainInventoryX = screenWidth - 10 - inventoryWidth;
        }

        int mainInventoryY = screen.getScreenHeight() - (4 * (slotSize + slotSpacing)) - 10;
        int hotbarY = mainInventoryY + (3 * (slotSize + slotSpacing)) + 4;

        // Calcular posición de armadura
        int armorStartX = mainInventoryX - separation - armorWidth;

        // Asegurar que la armadura no esté fuera de los límites
        if (armorStartX < 10) {
            armorStartX = 10;
            mainInventoryX = armorStartX + armorWidth + separation;
        }

        // Fondo del inventario principal
        uiRenderer.renderInventoryBackground(guiGraphics, mainInventoryX, mainInventoryY,
                inventoryWidth, 3 * (slotSize + slotSpacing));

        // Fondo de la hotbar
        uiRenderer.renderInventoryBackground(guiGraphics, mainInventoryX, hotbarY,
                inventoryWidth, slotSize);

        // Fondo de la armadura
        uiRenderer.renderInventoryBackground(guiGraphics, armorStartX, hotbarY,
                armorWidth, slotSize);

        // Renderizar los slots individuales
        renderInventorySlots(guiGraphics);
    }

    /**
     * Renderiza los slots individuales del inventario.
     *
     * @param guiGraphics Contexto de renderizado
     */
    private void renderInventorySlots(GuiGraphics guiGraphics) {
        if (screen.getMinecraftInstance() == null || screen.getMinecraftInstance().getWindow() == null) return;

        int screenWidth = screen.getMinecraftInstance().getWindow().getGuiScaledWidth();

        // Calcular las mismas dimensiones que usamos para posicionar
        int slotSize = uiState.getSlotSize();
        int slotSpacing = uiState.getSlotSpacing();

        int inventoryWidth = 9 * (slotSize + slotSpacing);
        int armorWidth = 5 * (slotSize + slotSpacing);
        int separation = Math.min(40, Math.max(10, (screenWidth - inventoryWidth - armorWidth) / 3));

        int mainInventoryX = (screenWidth + separation) / 2 - inventoryWidth / 2;
        if (mainInventoryX + inventoryWidth > screenWidth - 10) {
            mainInventoryX = screenWidth - 10 - inventoryWidth;
        }

        int mainInventoryY = screen.getScreenHeight() - (4 * (slotSize + slotSpacing)) - 10;
        int hotbarY = mainInventoryY + (3 * (slotSize + slotSpacing)) + 4;
        int armorStartX = mainInventoryX - separation - armorWidth;
        if (armorStartX < 10) {
            armorStartX = 10;
            mainInventoryX = armorStartX + armorWidth + separation;
        }

        // Renderizar cada slot individualmente
        for (int i = 0; i < screen.getMenu().slots.size(); i++) {
            Slot slot = screen.getMenu().slots.get(i);

            if (slot.x < 0 || slot.y < 0) continue; // Saltar slots ocultos

            int slotX, slotY;

            // Determinar posición basada en el índice del slot
            if (i == 45) { // Escudo
                slotX = armorStartX;
                slotY = hotbarY;
            } else if (i >= 5 && i <= 8) { // Armadura
                int armorIndex = i - 5;
                slotX = armorStartX + (armorIndex + 1) * (slotSize + slotSpacing);
                slotY = hotbarY;
            } else if (i >= 9 && i < 36) { // Inventario principal
                int row = (i - 9) / 9;
                int col = (i - 9) % 9;
                slotX = mainInventoryX + col * (slotSize + slotSpacing);
                slotY = mainInventoryY + row * (slotSize + slotSpacing);
            } else if (i >= 36 && i < 45) { // Hotbar
                int hotbarIndex = i - 36;
                slotX = mainInventoryX + hotbarIndex * (slotSize + slotSpacing);
                slotY = hotbarY;
            } else {
                continue; // Saltar otros slots (crafteo)
            }

            // Render slot background
            guiGraphics.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, 0xFF1D1D1D);

            // Render slot borders
            guiGraphics.fill(slotX, slotY, slotX + slotSize, slotY + 1, 0xFF373737);
            guiGraphics.fill(slotX, slotY + slotSize - 1, slotX + slotSize, slotY + slotSize, 0xFF373737);
            guiGraphics.fill(slotX, slotY, slotX + 1, slotY + slotSize, 0xFF373737);
            guiGraphics.fill(slotX + slotSize - 1, slotY, slotX + slotSize, slotY + slotSize, 0xFF373737);

            // Borde especial para slots de armadura
            boolean isArmorSlot = i >= 5 && i <= 8 || i == 45;
            if (isArmorSlot) {
                guiGraphics.fill(slotX - 1, slotY - 1, slotX + slotSize + 1, slotY, 0xFF373737);
                guiGraphics.fill(slotX - 1, slotY + slotSize, slotX + slotSize + 1, slotY + slotSize + 1, 0xFF373737);
                guiGraphics.fill(slotX - 1, slotY - 1, slotX, slotY + slotSize + 1, 0xFF373737);
                guiGraphics.fill(slotX + slotSize, slotY - 1, slotX + slotSize + 1, slotY + slotSize + 1, 0xFF373737);
            }
        }
    }
}