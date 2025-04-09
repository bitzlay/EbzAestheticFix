package com.bitzlay.ebzinventory.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Utility methods for inventory synchronization.
 */
public class InventorySyncUtils {
    /**
     * Syncs the player's inventory with the server.
     * This should be called when closing a screen to ensure all changes are saved.
     *
     * @param minecraft The Minecraft instance
     * @param inventorySlots The list of inventory slots
     */
    public static void syncInventoryWithServer(Minecraft minecraft, List<Slot> inventorySlots) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        // Sync each slot explicitly
        for (Slot slot : inventorySlots) {
            int slotIndex = slot.getContainerSlot();
            ItemStack stack = slot.getItem();

            if (slotIndex >= 0 && slotIndex < minecraft.player.getInventory().items.size()) {
                // Update in player's inventory
                minecraft.player.getInventory().setItem(slotIndex, stack.copy());

                // Hotbar slots need special handling
                if (slotIndex >= 0 && slotIndex <= 8) {
                    updateHotbarSlot(minecraft, slotIndex, stack);
                } else {
                    // Regular inventory slots
                    if (minecraft.gameMode.getPlayerMode() == net.minecraft.world.level.GameType.CREATIVE) {
                        minecraft.gameMode.handleCreativeModeItemAdd(stack.copy(), slotIndex);
                    }
                }
            }
        }

        // Force inventory update
        if (minecraft.player.inventoryMenu != null) {
            minecraft.player.inventoryMenu.broadcastChanges();
        }

        // Send a close container packet to force sync
        if (minecraft.getConnection() != null) {
            minecraft.getConnection().send(
                    new net.minecraft.network.protocol.game.ServerboundContainerClosePacket(
                            minecraft.player.containerMenu.containerId
                    )
            );
        }
    }

    /**
     * Updates a hotbar slot with special handling to ensure proper synchronization.
     *
     * @param minecraft The Minecraft instance
     * @param hotbarIndex The hotbar slot index (0-8)
     * @param stack The item stack to place in the slot
     */
    public static void updateHotbarSlot(Minecraft minecraft, int hotbarIndex, ItemStack stack) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        // Ensure the index is within hotbar range
        if (hotbarIndex < 0 || hotbarIndex > 8) {
            return;
        }

        // Update the inventory directly
        minecraft.player.getInventory().items.set(hotbarIndex, stack.copy());

        // In creative mode, use the specific method for adding creative items
        if (minecraft.gameMode.getPlayerMode() == net.minecraft.world.level.GameType.CREATIVE) {
            // Container slots 36-44 correspond to hotbar slots 0-8
            int containerSlot = 36 + hotbarIndex;
            minecraft.gameMode.handleCreativeModeItemAdd(stack.copy(), containerSlot);
        }

        // Force an inventory update
        minecraft.player.inventoryMenu.broadcastChanges();
    }

    /**
     * Forces a complete inventory synchronization using direct network packets.
     * This is useful when other sync methods fail.
     *
     * @param minecraft The Minecraft instance
     */
    public static void forceInventorySync(Minecraft minecraft) {
        if (minecraft == null || minecraft.player == null || minecraft.getConnection() == null) {
            return;
        }

        // Get player inventory
        net.minecraft.world.entity.player.Inventory inv = minecraft.player.getInventory();

        // Sync each slot individually with a packet
        for (int i = 0; i < inv.items.size(); i++) {
            ItemStack stack = inv.getItem(i);

            // Send a creative mode slot packet for each slot
            net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket packet =
                    new net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket(i, stack);

            // Send the packet
            minecraft.getConnection().send(packet);
        }

        // Force client-side inventory update
        minecraft.player.inventoryMenu.broadcastChanges();
    }
}