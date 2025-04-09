package com.bitzlay.ebzinventory.crafting;

import com.bitzlay.ebzinventory.recipe.InventoryRecipe;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Clase utilitaria centralizada para manejar operaciones de crafteo.
 * Asegura que los materiales solo se consuman si hay espacio en la cola.
 */
public class CraftingHelper {

    /**
     * Inicia el crafteo de una receta verificando primero si hay espacio en la cola.
     * Solo consume materiales si hay espacio disponible.
     *
     * @param player Jugador que realiza el crafteo
     * @param recipe Receta a craftear
     * @return true si el crafteo se inició correctamente, false si no
     */
    public static boolean startCrafting(Player player, InventoryRecipe recipe) {
        if (player == null || recipe == null) return false;

        UUID playerId = player.getUUID();

        // 1. Verificar si hay espacio en la cola
        if (!CraftingQueueHandler.canAddToQueue(playerId)) {
            player.displayClientMessage(
                    Component.literal("§cLa cola de crafteo está llena"),
                    false
            );
            return false;
        }

        // 2. Verificar si hay suficientes materiales
        if (!canCraft(player, recipe)) {
            player.displayClientMessage(
                    Component.literal("§cNo tienes suficientes materiales"),
                    false
            );
            return false;
        }

        // 3. Consumir materiales (solo si hay espacio en la cola)
        consumeMaterials(player, recipe);

        // 4. Crear y añadir a la cola
        ItemStack resultCopy = recipe.getResult().copy();

        CraftingQueueItem queueItem = new CraftingQueueItem(
                recipe.getId(),
                playerId,
                resultCopy,
                recipe.getCraftingTime(),
                recipe
        );

        // 5. Añadir a la cola (esto no debería fallar ya que verificamos antes)
        boolean added = CraftingQueueHandler.addToQueue(queueItem);

        // 6. Si por alguna razón falla (lo cual no debería ocurrir), devolver materiales
        if (!added) {
            // Esto es solo una medida de seguridad adicional
            returnMaterials(player, recipe);
            return false;
        }

        // 7. Feedback de sonido
        player.playSound(
                net.minecraft.sounds.SoundEvents.UI_STONECUTTER_TAKE_RESULT,
                1.0F, 1.0F
        );

        return true;
    }

    /**
     * Verifica si una receta puede ser crafteada con los materiales disponibles.
     *
     * @param player Jugador que realiza el crafteo
     * @param recipe Receta a verificar
     * @return true si hay suficientes materiales
     */
    public static boolean canCraft(Player player, InventoryRecipe recipe) {
        if (player == null || recipe == null) return false;

        for (Map.Entry<Item, Integer> ingredient : recipe.getIngredients().entrySet()) {
            if (player.getInventory().countItem(ingredient.getKey()) < ingredient.getValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Consume los materiales necesarios para una receta.
     *
     * @param player Jugador que realiza el crafteo
     * @param recipe Receta cuyos materiales serán consumidos
     */
    private static void consumeMaterials(Player player, InventoryRecipe recipe) {
        if (player == null || recipe == null) return;

        for (Map.Entry<Item, Integer> ingredient : recipe.getIngredients().entrySet()) {
            int remaining = ingredient.getValue();
            while (remaining > 0) {
                int slot = player.getInventory().findSlotMatchingItem(new ItemStack(ingredient.getKey()));
                if (slot == -1) break;

                ItemStack stack = player.getInventory().getItem(slot);
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }
    }

    /**
     * Devuelve los materiales al inventario del jugador.
     *
     * @param player Jugador que recibirá los materiales
     * @param recipe Receta cuyos materiales serán devueltos
     */
    private static void returnMaterials(Player player, InventoryRecipe recipe) {
        if (player == null || recipe == null) return;

        for (Map.Entry<Item, Integer> ingredient : recipe.getIngredients().entrySet()) {
            ItemStack returnStack = new ItemStack(ingredient.getKey(), ingredient.getValue());
            if (!player.getInventory().add(returnStack)) {
                player.drop(returnStack, false);
            }
        }
    }
}