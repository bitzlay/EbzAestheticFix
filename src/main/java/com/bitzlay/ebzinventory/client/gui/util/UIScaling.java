package com.bitzlay.ebzinventory.client.gui.util;

import com.bitzlay.ebzinventory.client.gui.model.UIState;
import net.minecraft.client.Minecraft;

/**
 * Clase de utilidad para manejar el escalado de la interfaz de usuario
 * basado en la resolución de pantalla y escala de GUI.
 */
public class UIScaling {
    /** Tamaño base de un slot de inventario */
    private static final int BASE_SLOT_SIZE = 18;

    /**
     * Actualiza las dimensiones de la interfaz basado en la resolución y escala.
     *
     * @param minecraft Instancia de Minecraft
     * @param uiState Estado compartido de UI para actualizar
     * @param screenWidth Ancho de la pantalla
     * @param screenHeight Alto de la pantalla
     */
    public static void updateDimensions(Minecraft minecraft, UIState uiState, int screenWidth, int screenHeight) {
        if (minecraft == null || minecraft.getWindow() == null) return;

        // Obtener la escala actual de GUI
        float guiScale = (float)minecraft.getWindow().getGuiScale();
        uiState.setGuiScale(guiScale);

        // Ajuste específico para cada escala de GUI
        if (guiScale >= 4) {
            uiState.setSlotSize((int)(BASE_SLOT_SIZE * 1.1f)); // Reducción significativa para escala 4
            uiState.setSlotSpacing(1);
        } else if (guiScale >= 3) {
            uiState.setSlotSize((int)(BASE_SLOT_SIZE * 1.2f)); // Reducción para escala 3
            uiState.setSlotSpacing(1);
        } else if (guiScale <= 1) {
            uiState.setSlotSize((int)(BASE_SLOT_SIZE * 1.8f)); // Más grande para escalas pequeñas
            uiState.setSlotSpacing(3);
        } else { // Escala 2 (más común)
            uiState.setSlotSize((int)(BASE_SLOT_SIZE * 1.5f)); // Tamaño estándar
            uiState.setSlotSpacing(2);
        }

        // Obtener dimensiones de pantalla
        int guiScaledWidth = minecraft.getWindow().getGuiScaledWidth();
        int guiScaledHeight = minecraft.getWindow().getGuiScaledHeight();

        // Asegurar que haya suficiente espacio para inventario y armadura
        // Primero calcular ancho total necesario
        int inventoryWidth = 9 * (uiState.getSlotSize() + uiState.getSlotSpacing());
        int armorWidth = 5 * (uiState.getSlotSize() + uiState.getSlotSpacing());
        int totalNeededWidth = inventoryWidth + armorWidth + 40; // Espacio extra para padding

        // Si el ancho necesario excede el ancho de pantalla, reducir elementos
        if (totalNeededWidth > guiScaledWidth - 20) {
            // Calcular factor de escala para que todo quepa
            float scaleFactor = (float)(guiScaledWidth - 30) / totalNeededWidth;
            uiState.setSlotSize((int)(uiState.getSlotSize() * scaleFactor));
            uiState.setSlotSpacing(Math.max(1, (int)(uiState.getSlotSpacing() * scaleFactor)));
        }

        // Anchos de panel como porcentajes del ancho de pantalla, ajustado para escala 4
        if (guiScale >= 4) {
            uiState.setRecipesWidth(Math.min(180, guiScaledWidth / 4));
            uiState.setInfoWidth(Math.min(180, guiScaledWidth / 4));
            uiState.setQueueWidth(Math.min(140, guiScaledWidth / 5));
        } else {
            uiState.setRecipesWidth(Math.min(200, guiScaledWidth / 4));
            uiState.setInfoWidth(Math.min(200, guiScaledWidth / 4));
            uiState.setQueueWidth(Math.min(160, guiScaledWidth / 5));
        }

        // Ajustar altura del área de crafteo basado en altura de pantalla
        uiState.setCraftingAreaHeight(Math.min(180, guiScaledHeight - 100));

        // Ajustar dimensiones de tarjetas
        uiState.setQueueCardHeight(Math.min(25, uiState.getCraftingAreaHeight() / 10));
        uiState.setQueueSpacing(4);
    }

    /**
     * Calcula la dimensión óptima de los slots basado en la escala de GUI.
     *
     * @param guiScale Escala de la GUI
     * @return Tamaño recomendado para los slots
     */
    public static int calculateSlotSize(float guiScale) {
        if (guiScale <= 1) {
            return (int)(BASE_SLOT_SIZE * 1.8f);
        } else if (guiScale <= 2) {
            return (int)(BASE_SLOT_SIZE * 1.5f);
        } else if (guiScale <= 3) {
            return (int)(BASE_SLOT_SIZE * 1.2f);
        } else {
            return (int)(BASE_SLOT_SIZE * 1.1f);
        }
    }
}