package com.bitzlay.ebzinventory.client.gui.components;

import com.bitzlay.ebzinventory.client.gui.RustStyleInventoryScreen;
import com.bitzlay.ebzinventory.client.gui.model.UIState;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;

/**
 * Clase responsable de gestionar las posiciones de los slots del inventario.
 * Maneja el posicionamiento dinámico basado en la escala y resolución.
 */
public class InventorySlotManager {
    /** Referencia a la pantalla de inventario */
    private final RustStyleInventoryScreen screen;

    /** Referencia al menú que contiene los slots */
    private final InventoryMenu menu;

    /**
     * Constructor del administrador de slots.
     *
     * @param screen Pantalla de inventario
     * @param menu Menú con los slots
     */
    public InventorySlotManager(RustStyleInventoryScreen screen, InventoryMenu menu) {
        this.screen = screen;
        this.menu = menu;
    }

    /**
     * Reposiciona todos los slots según el modo de pantalla y las dimensiones.
     *
     * @param showCrafting Si estamos en modo crafteo
     * @param leftPos Posición X base de la pantalla
     * @param topPos Posición Y base de la pantalla
     */
    public void repositionSlots(boolean showCrafting, int leftPos, int topPos) {
        if (showCrafting) {
            // Ocultar todos los slots cuando se muestra el crafteo
            for (Slot slot : this.menu.slots) {
                setSlotPosition(slot, -999, -999);
            }
            return;
        }

        UIState uiState = screen.getUiState();
        int screenWidth = screen.getMinecraftInstance().getWindow().getGuiScaledWidth();

        // Tamaños y espaciados
        int slotSize = uiState.getSlotSize();
        int slotSpacing = uiState.getSlotSpacing();

        // Calcular posiciones basadas en el tamaño de la pantalla actual
        int inventoryWidth = 9 * (slotSize + slotSpacing);
        int armorWidth = 5 * (slotSize + slotSpacing);
        int separation = Math.min(40, Math.max(10, (screenWidth - inventoryWidth - armorWidth) / 3));

        // Centrar inventario en el espacio disponible
        int mainInventoryX = (screenWidth + separation) / 2 - inventoryWidth / 2;

        // Ajustar si el inventario se sale de la pantalla
        if (mainInventoryX + inventoryWidth > screenWidth - 10) {
            mainInventoryX = screenWidth - 10 - inventoryWidth;
        }

        int mainInventoryY = screen.getScreenHeight() - (4 * (slotSize + slotSpacing)) - 10;

        // Posición de la armadura con separación dinámica
        int armorStartX = mainInventoryX - separation - armorWidth;

        // Asegurar que la armadura no quede fuera de la pantalla
        if (armorStartX < 10) {
            armorStartX = 10;
            mainInventoryX = armorStartX + armorWidth + separation;
        }

        int hotbarY = mainInventoryY + (3 * (slotSize + slotSpacing)) + 4;

        // Posicionar escudo (slot 45)
        setSlotPosition(this.menu.slots.get(45),
                armorStartX - leftPos,
                hotbarY - topPos);

        // Posicionar armadura (slots 5-8)
        for (int i = 0; i < 4; i++) {
            setSlotPosition(this.menu.slots.get(5 + i),
                    armorStartX - leftPos + (i + 1) * (slotSize + slotSpacing),
                    hotbarY - topPos);
        }

        // Posicionar inventario principal (slots 9-35)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                setSlotPosition(this.menu.slots.get(9 + row * 9 + col),
                        mainInventoryX - leftPos + col * (slotSize + slotSpacing),
                        mainInventoryY - topPos + row * (slotSize + slotSpacing));
            }
        }

        // Posicionar hotbar (slots 36-44)
        for (int i = 0; i < 9; i++) {
            setSlotPosition(this.menu.slots.get(36 + i),
                    mainInventoryX - leftPos + i * (slotSize + slotSpacing),
                    hotbarY - topPos);
        }

        // Ocultar slots de crafteo vanilla (slots 0-4)
        for (int i = 0; i < 5; i++) {
            setSlotPosition(this.menu.slots.get(i), -999, -999);
        }
    }

    /**
     * Establece la posición de un slot usando reflexión.
     *
     * @param slot Slot a posicionar
     * @param x Posición X
     * @param y Posición Y
     */
    private void setSlotPosition(Slot slot, int x, int y) {
        try {
            java.lang.reflect.Field xField = net.minecraft.world.inventory.Slot.class.getDeclaredField("x");
            java.lang.reflect.Field yField = net.minecraft.world.inventory.Slot.class.getDeclaredField("y");

            xField.setAccessible(true);
            yField.setAccessible(true);

            // Calcular el offset para centrar el item (16x16) dentro del slot
            UIState uiState = screen.getUiState();
            int slotSize = uiState.getSlotSize();
            int offsetToCenter = (slotSize - 16) / 2;

            // Ajuste fino para escalas muy altas
            if (uiState.getGuiScale() >= 4) {
                offsetToCenter = (int)((slotSize - 16) / 2.0f);
            }

            // Establecer las posiciones
            xField.set(slot, x + offsetToCenter);
            yField.set(slot, y + offsetToCenter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Determina si un punto está dentro de los límites del inventario.
     *
     * @param mouseX Posición X del mouse
     * @param mouseY Posición Y del mouse
     * @return true si el punto está dentro del inventario
     */
    public boolean isWithinInventoryBounds(double mouseX, double mouseY) {
        if (screen.getMinecraftInstance() == null || screen.getMinecraftInstance().getWindow() == null) return false;

        UIState uiState = screen.getUiState();
        int screenWidth = screen.getMinecraftInstance().getWindow().getGuiScaledWidth();

        // Obtener dimensiones actuales
        int slotSize = uiState.getSlotSize();
        int slotSpacing = uiState.getSlotSpacing();

        // Calcular dimensiones
        int inventoryWidth = 9 * (slotSize + slotSpacing);
        int armorWidth = 5 * (slotSize + slotSpacing);
        int separation = Math.min(40, Math.max(10, (screenWidth - inventoryWidth - armorWidth) / 3));

        // Calcular posiciones
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

        // Verificar si el punto está dentro de alguna de las tres secciones
        // 1. Inventario principal (3 filas)
        boolean inMainInventory = mouseX >= mainInventoryX - 5 &&
                mouseX <= mainInventoryX + inventoryWidth + 5 &&
                mouseY >= mainInventoryY - 5 &&
                mouseY <= mainInventoryY + 3 * (slotSize + slotSpacing) + 5;

        // 2. Hotbar (1 fila)
        boolean inHotbar = mouseX >= mainInventoryX - 5 &&
                mouseX <= mainInventoryX + inventoryWidth + 5 &&
                mouseY >= hotbarY - 5 &&
                mouseY <= hotbarY + slotSize + 5;

        // 3. Armadura (1 fila)
        boolean inArmor = mouseX >= armorStartX - 5 &&
                mouseX <= armorStartX + armorWidth + 5 &&
                mouseY >= hotbarY - 5 &&
                mouseY <= hotbarY + slotSize + 5;

        // Estamos dentro de la interfaz si estamos en cualquiera de las tres secciones
        return inMainInventory || inHotbar || inArmor;
    }

    /**
     * Maneja la liberación del clic del mouse para controlar dónde se pueden soltar items.
     *
     * @param mouseX Posición X del mouse
     * @param mouseY Posición Y del mouse
     * @param button Botón del mouse
     * @return true si el evento fue manejado
     */
    public boolean handleMouseReleased(double mouseX, double mouseY, int button) {
        // Verificar soltar items
        Slot hoveredSlot = screen.getSlotUnderMouse();
        boolean isCarryingItem = this.menu.getCarried() != null && !this.menu.getCarried().isEmpty();

        // Comprobar si estamos soltando un item en una zona válida
        if (isCarryingItem && hoveredSlot == null) {
            // Caso 1: Estamos dentro de la interfaz pero no sobre un slot
            if (isWithinInventoryBounds(mouseX, mouseY)) {
                // No permitir soltar el item aquí
                return true;
            }
            // Caso 2: Estamos fuera de la interfaz completamente
            // Permitir el comportamiento normal de soltar (tirar al suelo)
        }

        // No manejamos el evento
        return false;
    }
}