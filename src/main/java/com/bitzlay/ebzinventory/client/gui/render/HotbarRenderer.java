package com.bitzlay.ebzinventory.client.gui.render;

import com.bitzlay.ebzinventory.EbzInventory;
import com.bitzlay.ebzinventory.capability.IHydration;
import com.bitzlay.ebzinventory.client.gui.RustStyleInventoryScreen;
import com.bitzlay.ebzinventory.player.PlayerHydrationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EbzInventory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HotbarRenderer {
    // Base sizes
    private static final int BASE_SLOT_SIZE = 18;
    private static final int BASE_BAR_WIDTH = 140; // Barras más anchas
    private static final int BASE_BAR_HEIGHT = 14; // Barras más altas para texto

    // Variables para optimizar el logging de hidratación
    private static long lastHydrationLogTime = 0;
    private static float lastLoggedHydrationValue = -1;

    public static final IGuiOverlay CUSTOM_HOTBAR = ((gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft minecraft = Minecraft.getInstance();
        // Don't render if inventory is open
        if (minecraft.screen instanceof RustStyleInventoryScreen) return;

        Player player = minecraft.player;
        if (player == null) return;

        // Calculate dimensions based on GUI scale and screen size
        int slotSize = calculateSlotSize(minecraft);
        int slotSpacing = Math.max(1, slotSize / 9);
        int barWidth = calculateBarWidth(screenWidth);
        int barHeight = Math.max(12, screenHeight / 50); // Barras más altas
        int barSpacing = 2; // Espacio mínimo entre barras

        // Calculate positions based on screen dimensions
        int centerX = screenWidth / 2;
        int startX = centerX - ((9 * (slotSize + slotSpacing)) / 2);
        int y = screenHeight - slotSize - 8;

        // Colocar las barras de estado al borde izquierdo
        int statsX = 10; // Pegado al borde izquierdo

        // Calcular total de barras (Ahora 4: vida, armadura, comida, hidratación)
        int totalBars = 4;

        // Alinear barras con el borde inferior de la hotbar
        int statsY = y + slotSize - (barHeight * totalBars + barSpacing * (totalBars - 1)); // Modificado para 4 barras

        // Render stats to the left edge
        renderPlayerStats(guiGraphics, statsX, statsY,
                player, minecraft, barWidth, barHeight, barSpacing, totalBars);

        // Hotbar background
        renderHotbarBackground(guiGraphics, startX - 5, y - 5,
                9 * (slotSize + slotSpacing) + 10, slotSize + 10);

        // Render slots
        for (int slot = 0; slot < 9; slot++) {
            int x = startX + (slot * (slotSize + slotSpacing));

            // Slot background
            guiGraphics.fill(x, y, x + slotSize, y + slotSize, 0xFF1D1D1D);

            // Slot borders
            guiGraphics.fill(x, y, x + slotSize, y + 1, 0xFF373737);
            guiGraphics.fill(x, y + slotSize - 1, x + slotSize, y + slotSize, 0xFF373737);
            guiGraphics.fill(x, y, x + 1, y + slotSize, 0xFF373737);
            guiGraphics.fill(x + slotSize - 1, y, x + slotSize, y + slotSize, 0xFF373737);

            // Render item
            ItemStack itemstack = player.getInventory().items.get(slot);
            int itemX = x + (slotSize - 16) / 2;
            int itemY = y + (slotSize - 16) / 2;

            guiGraphics.renderItem(itemstack, itemX, itemY);
            guiGraphics.renderItemDecorations(minecraft.font, itemstack, itemX, itemY);

            // Highlight selected slot
            if (slot == player.getInventory().selected) {
                guiGraphics.fill(x, y, x + slotSize, y + 1, 0xFFFFFFFF);
                guiGraphics.fill(x, y + slotSize - 1, x + slotSize, y + slotSize, 0xFFFFFFFF);
                guiGraphics.fill(x, y, x + 1, y + slotSize, 0xFFFFFFFF);
                guiGraphics.fill(x + slotSize - 1, y, x + slotSize, y + slotSize, 0xFFFFFFFF);
            }
        }
    });

    private static int calculateSlotSize(Minecraft minecraft) {
        if (minecraft == null || minecraft.getWindow() == null) {
            return (int)(BASE_SLOT_SIZE * 1.5f);
        }

        float guiScale = (float)minecraft.getWindow().getGuiScale();
        if (guiScale <= 1) {
            return (int)(BASE_SLOT_SIZE * 1.8f);
        } else if (guiScale <= 2) {
            return (int)(BASE_SLOT_SIZE * 1.5f);
        } else {
            return (int)(BASE_SLOT_SIZE * 1.3f);
        }
    }

    private static int calculateBarWidth(int screenWidth) {
        // Scale bar width based on screen width
        return Math.min(BASE_BAR_WIDTH, screenWidth / 5);
    }

    private static void renderHotbarBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Semi-transparent black background
        guiGraphics.fill(x, y, x + width, y + height, 0xCC000000);

        // Borders
        guiGraphics.fill(x, y, x + width, y + 1, 0xFF373737);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFF373737);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFF373737);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF373737);
    }

    private static void renderPlayerStats(GuiGraphics guiGraphics, int x, int y, Player player,
                                          Minecraft minecraft, int barWidth, int barHeight, int barSpacing, int totalBars) {
        // No dibujamos fondo para las barras - diseño más limpio

        // ORDEN ACTUALIZADO: HP, Armor, Food, Water
        int barIndex = 0;

        // 1. HP bar
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();

        String healthText = String.format("%.0f/%.0f", health, maxHealth);
        renderModernStatBar(guiGraphics, x, y + (barHeight + barSpacing) * barIndex++, "HP", 0xFFE53935, // Rojo moderno
                health / maxHealth, minecraft, barWidth, barHeight, healthText);

        // 2. Armor bar
        float armorValue = player.getArmorValue();
        String armorText = String.format("%.0f", armorValue);
        float armorPercentage = Math.min(1.0f, armorValue / 20f);

        renderModernStatBar(guiGraphics, x, y + (barHeight + barSpacing) * barIndex++, "Armor", 0xFF42A5F5, // Azul moderno
                armorPercentage, minecraft, barWidth, barHeight, armorText);

        // 3. Food bar (NUEVA)
        int foodLevel = player.getFoodData().getFoodLevel();
        String foodText = String.format("%d/20", foodLevel);
        float foodPercentage = foodLevel / 20.0f;

        renderModernStatBar(guiGraphics, x, y + (barHeight + barSpacing) * barIndex++, "Food", 0xFFFF9800, // Naranja moderno
                foodPercentage, minecraft, barWidth, barHeight, foodText);

        // 4. Hydration bar
        IHydration hydrationData = PlayerHydrationManager.getHydrationDataDirect(player);
        float hydrationLevel = hydrationData != null ? hydrationData.getHydrationLevel() : 0;
        float maxHydration = hydrationData != null ? hydrationData.getMaxHydrationLevel() : 100;

        // Optimización: Log de hidratación con control de tiempo y cambios
        logHydrationRender(player.getName().getString(), hydrationLevel, maxHydration);

        String hydrationText = String.format("%.0f/100", hydrationLevel);
        float hydrationPercentage = hydrationLevel / maxHydration;

        renderModernStatBar(guiGraphics, x, y + (barHeight + barSpacing) * barIndex, "Water", 0xFF00BCD4, // Azul agua moderno
                hydrationPercentage, minecraft, barWidth, barHeight, hydrationText);
    }

    // Método optimizado para el logging de hidratación
    private static void logHydrationRender(String playerName, float hydrationLevel, float maxHydration) {
        long currentTime = System.currentTimeMillis();
        boolean valueChanged = Math.abs(lastLoggedHydrationValue - hydrationLevel) > 0.5f;

        // Solo loguear si hay un cambio en la hidratación o han pasado 15 segundos
        if (valueChanged || currentTime - lastHydrationLogTime > 15000) {
            // Solo loguear si el modo debug está activado
            if (EbzInventory.LOGGER.isDebugEnabled()) {
                EbzInventory.LOGGER.debug("Renderizando hidratación para {}: {}/{}",
                        playerName, hydrationLevel, maxHydration);
                lastHydrationLogTime = currentTime;
                lastLoggedHydrationValue = hydrationLevel;
            }
        }
    }

    private static void renderModernStatBar(GuiGraphics guiGraphics, int x, int y, String label,
                                            int color, float percentage, Minecraft minecraft,
                                            int barWidth, int barHeight, String valueText) {
        // Colores con transparencia para un look más moderno
        int backgroundColor = 0x80000000; // Negro semi-transparente
        int borderColor = 0x40FFFFFF; // Borde blanco sutíl
        int labelColor = 0xFFFFFFFF; // Blanco para el texto

        // Bar background with rounded corners effect (simulated)
        guiGraphics.fill(x, y, x + barWidth, y + barHeight, backgroundColor);

        // Progress bar with slightly inset position for modern look
        int progressBarInset = 1;
        int progressWidth = Math.max(0, (int)((barWidth - progressBarInset * 2) * percentage));

        // Gradient effect for progress bar (darker at bottom)
        int gradientTop = color;
        int gradientBottom = darkenColor(color, 0.7f);

        // Fill with main color
        guiGraphics.fill(x + progressBarInset, y + progressBarInset,
                x + progressBarInset + progressWidth,
                y + barHeight - progressBarInset, gradientTop);

        // Subtle highlight at top of bar
        guiGraphics.fill(x + progressBarInset, y + progressBarInset,
                x + progressBarInset + progressWidth,
                y + progressBarInset + 2, lightenColor(color, 1.2f));

        // Very subtle borders
        guiGraphics.fill(x, y, x + barWidth, y + 1, borderColor);
        guiGraphics.fill(x, y + barHeight - 1, x + barWidth, y + barHeight, borderColor);
        guiGraphics.fill(x, y, x + 1, y + barHeight, borderColor);
        guiGraphics.fill(x + barWidth - 1, y, x + barWidth, y + barHeight, borderColor);

        // Label in bold (simulated by drawing twice with 1px offset)
        int textY = y + (barHeight - 8) / 2;
        guiGraphics.drawString(minecraft.font, label, x + 4, textY, 0x80000000); // Shadow
        guiGraphics.drawString(minecraft.font, label, x + 3, textY - 1, labelColor); // Text

        // Value text on right side
        int textWidth = minecraft.font.width(valueText);
        guiGraphics.drawString(minecraft.font, valueText, x + barWidth - textWidth - 4, textY, labelColor);
    }

    // Helper method to darken a color
    private static int darkenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int)(((color >> 16) & 0xFF) * factor);
        int g = (int)(((color >> 8) & 0xFF) * factor);
        int b = (int)((color & 0xFF) * factor);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // Helper method to lighten a color
    private static int lightenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, (int)(((color >> 16) & 0xFF) * factor));
        int g = Math.min(255, (int)(((color >> 8) & 0xFF) * factor));
        int b = Math.min(255, (int)((color & 0xFF) * factor));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "custom_hotbar", CUSTOM_HOTBAR);
    }
}