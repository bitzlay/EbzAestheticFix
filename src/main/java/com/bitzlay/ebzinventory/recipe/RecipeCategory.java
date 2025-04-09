package com.bitzlay.ebzinventory.recipe;


import net.minecraft.world.item.Item;
import java.util.HashMap;
import java.util.Map;

public class RecipeCategory {
    private final String name;
    private final String prefix;
    private final Item icon;
    private final Map<String, InventoryRecipe> recipes;

    public RecipeCategory(String name, String prefix, Item icon) {
        this.name = name;
        this.prefix = prefix;
        this.icon = icon;
        this.recipes = new HashMap<>();
    }

    public void addRecipe(String id, InventoryRecipe recipe) {
        recipes.put(id, recipe);
    }

    public InventoryRecipe getRecipe(String id) {
        return recipes.get(id);
    }

    public void clearRecipes() {
        recipes.clear();
    }

    public String getName() { return name; }
    public String getPrefix() { return prefix; }
    public Item getIcon() { return icon; }
    public Map<String, InventoryRecipe> getRecipes() { return recipes; }
}