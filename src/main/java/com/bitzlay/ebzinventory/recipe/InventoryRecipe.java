package com.bitzlay.ebzinventory.recipe;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.Map;

public class InventoryRecipe {
    private final String id;
    private final String category;
    private final String displayName;
    private final ItemStack result;
    private final Map<Item, Integer> ingredients;
    private final long craftingTime; // en ticks (20 ticks = 1 segundo)

    public InventoryRecipe(String id, String category, String displayName,
                           ItemStack result, Map<Item, Integer> ingredients,
                           long craftingTime) {
        this.id = id;
        this.category = category;
        this.displayName = displayName;
        this.result = result;
        this.ingredients = ingredients;
        this.craftingTime = craftingTime;
    }

    public String getId() { return id; }
    public String getCategory() { return category; }
    public String getDisplayName() { return displayName; }
    public ItemStack getResult() { return result; }
    public Map<Item, Integer> getIngredients() { return ingredients; }
    public long getCraftingTime() { return craftingTime; }
}