package com.bitzlay.ebzinventory.recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.ForgeRegistries;
import java.io.*;
import java.util.*;

public class InventoryRecipeManager {
    private static final Map<String, InventoryRecipe> RECIPES = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void loadRecipes(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Recipe file not found, creating template...");
            saveRecipeTemplate(filename);
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Map<String, Object> jsonMap = GSON.fromJson(reader, Map.class);

            if (!jsonMap.containsKey("recipes")) {
                System.err.println("Error: Invalid recipe file format. Missing 'recipes' array.");
                return;
            }

            RECIPES.clear();

            List<Map<String, Object>> recipes = (List<Map<String, Object>>) jsonMap.get("recipes");
            for (Map<String, Object> recipeMap : recipes) {
                try {
                    loadRecipe(recipeMap);
                } catch (Exception e) {
                    System.err.println("Error loading recipe: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading recipes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void loadRecipe(Map<String, Object> recipeMap) {
        String category = (String) recipeMap.get("category");
        String recipeId = (String) recipeMap.get("recipeId");
        String displayName = (String) recipeMap.get("displayName");
        ItemStack resultStack = createItemWithNBT(
                (String) recipeMap.get("result"),
                (Map<String, Object>) recipeMap.get("nbt")
        );

        if (resultStack.isEmpty()) return;

        Map<String, Double> rawIngredients = (Map<String, Double>) recipeMap.get("ingredients");
        Map<Item, Integer> ingredients = new HashMap<>();

        for (Map.Entry<String, Double> entry : rawIngredients.entrySet()) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.getKey()));
            if (item != null) {
                ingredients.put(item, entry.getValue().intValue());
            }
        }

        // Obtener el tiempo de crafteo, usar 100 ticks (5 segundos) por defecto si no está especificado
        long craftingTime = 100;
        if (recipeMap.containsKey("craftingTime")) {
            Object timeObj = recipeMap.get("craftingTime");
            if (timeObj instanceof Number) {
                craftingTime = ((Number) timeObj).longValue();
            }
        }

        InventoryRecipe recipe = new InventoryRecipe(
                recipeId,
                category,
                displayName,
                resultStack,
                ingredients,
                craftingTime
        );

        addRecipe(recipe);
        System.out.println("Loaded recipe: " + displayName + " (Crafting Time: " + craftingTime + " ticks)");
    }

    private static ItemStack createItemWithNBT(String itemId, Map<String, Object> nbt) {
        try {
            if (itemId == null || itemId.isEmpty()) {
                System.err.println("Error: itemId is null or empty");
                return ItemStack.EMPTY;
            }

            ResourceLocation itemLocation = new ResourceLocation(itemId);
            Item item = ForgeRegistries.ITEMS.getValue(itemLocation);

            if (item == null) {
                System.err.println("Error: Could not find item with ID: " + itemId);
                return ItemStack.EMPTY;
            }

            ItemStack stack = new ItemStack(item);

            if (nbt != null && !nbt.isEmpty()) {
                CompoundTag nbtTag = new CompoundTag();
                for (Map.Entry<String, Object> entry : nbt.entrySet()) {
                    addNBTValue(nbtTag, entry.getKey(), entry.getValue());
                }
                stack.setTag(nbtTag);
            }

            return stack;
        } catch (Exception e) {
            System.err.println("Error creating item with NBT: " + e.getMessage());
            e.printStackTrace();
            return ItemStack.EMPTY;
        }
    }

    private static void addNBTValue(CompoundTag tag, String key, Object value) {
        if (value instanceof String) {
            tag.putString(key, (String) value);
        } else if (value instanceof Number) {
            tag.putInt(key, ((Number) value).intValue());
        } else if (value instanceof Boolean) {
            tag.putBoolean(key, (Boolean) value);
        }
    }

    private static void saveRecipeTemplate(String filename) {
        try {
            File configDir = new File("config");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }

            File file = new File(filename);
            if (file.exists()) return;

            Map<String, Object> root = new HashMap<>();
            List<Map<String, Object>> recipes = new ArrayList<>();

            // Ejemplo de receta por defecto
            Map<String, Object> recipe = new HashMap<>();
            recipe.put("category", "CA");
            recipe.put("recipeId", "CA1");
            recipe.put("displayName", "Espada de Hierro Mejorada");
            recipe.put("result", "minecraft:iron_sword");

            Map<String, Object> nbt = new HashMap<>();
            nbt.put("Damage", 0);
            recipe.put("nbt", nbt);

            Map<String, Integer> ingredients = new HashMap<>();
            ingredients.put("minecraft:iron_ingot", 2);
            ingredients.put("minecraft:stick", 1);
            recipe.put("ingredients", ingredients);

            recipes.add(recipe);
            root.put("recipes", recipes);

            try (Writer writer = new FileWriter(file)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            System.err.println("Error saving recipe template: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void addRecipe(InventoryRecipe recipe) {
        RECIPES.put(recipe.getId(), recipe);
    }

    public static InventoryRecipe getRecipe(String id) {
        return RECIPES.get(id);
    }

    public static List<InventoryRecipe> getRecipesByCategory(String category) {
        List<InventoryRecipe> categoryRecipes = new ArrayList<>();
        for (InventoryRecipe recipe : RECIPES.values()) {
            if (recipe.getCategory().equals(category)) {
                categoryRecipes.add(recipe);
            }
        }
        return categoryRecipes;
    }

    public static Map<String, InventoryRecipe> getAllRecipes() {
        return RECIPES;
    }
}