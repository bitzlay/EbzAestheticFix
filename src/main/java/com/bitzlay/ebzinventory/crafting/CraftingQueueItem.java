package com.bitzlay.ebzinventory.crafting;

import com.bitzlay.ebzinventory.recipe.InventoryRecipe;
import net.minecraft.world.item.ItemStack;
import java.util.UUID;

public class CraftingQueueItem {
    private final String recipeId;
    private final UUID playerId;
    private long startTime;
    private final long totalTime;
    private final ItemStack result;
    private boolean completed;
    private boolean paused;
    private long pausedTime;
    private final InventoryRecipe recipe;

    public CraftingQueueItem(String recipeId, UUID playerId, ItemStack result, long totalTimeInTicks, InventoryRecipe recipe) {
        this.recipeId = recipeId;
        this.playerId = playerId;
        this.startTime = System.currentTimeMillis();
        this.totalTime = totalTimeInTicks * 50;
        // Asegurarse de que estamos guardando una copia del resultado
        this.result = result.isEmpty() ? (recipe != null ? recipe.getResult().copy() : result.copy()) : result.copy();
        this.completed = false;
        this.paused = false;
        this.pausedTime = 0;
        this.recipe = recipe;
    }

    public void pause() {
        if (!paused) {
            paused = true;
            pausedTime = System.currentTimeMillis() - startTime;
        }
    }

    public void resume() {
        if (paused) {
            paused = false;
            startTime = System.currentTimeMillis() - pausedTime;
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public float getProgress() {
        if (completed) return 1.0f;
        if (paused) return pausedTime / (float)totalTime;

        long elapsed = System.currentTimeMillis() - startTime;
        return Math.min(1.0f, elapsed / (float)totalTime);
    }

    public boolean isCompleted() {
        if (!completed && !paused) {
            completed = getProgress() >= 1.0f;
        }
        return completed;
    }

    public InventoryRecipe getRecipe() {
        return recipe;
    }

    public ItemStack getResult() {
        // Si el resultado está vacío por alguna razón, intentar recuperarlo de la receta
        if (result.isEmpty() && recipe != null) {
            return recipe.getResult().copy();
        }
        return result;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public UUID getPlayerId() {
        return playerId;
    }
}