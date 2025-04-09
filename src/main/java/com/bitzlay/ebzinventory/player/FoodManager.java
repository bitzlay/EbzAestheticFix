package com.bitzlay.ebzinventory.player;

import com.bitzlay.ebzinventory.EbzInventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestor del sistema personalizado de alimentos.
 * Complementa el ImprovedFoodDataMixin proporcionando funciones adicionales.
 */
@Mod.EventBusSubscriber(modid = EbzInventory.MOD_ID)
public class FoodManager {

    // Mapa para ajustar valores nutricionales de items específicos
    private static final Map<Item, Integer> customFoodValues = new HashMap<>();

    /**
     * Inicializa el FoodManager y registra valores personalizados
     */
    public static void init() {
        EbzInventory.LOGGER.info("Inicializando sistema personalizado de alimentación");

        // Registrar algunos valores personalizados de ejemplo
        // Estos se pueden mover a la configuración similar al sistema de hidratación
        registerCustomFoodValue(net.minecraft.world.item.Items.GOLDEN_APPLE, 8);
        registerCustomFoodValue(net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE, 10);
    }

    /**
     * Registra un valor nutricional personalizado para un item
     */
    public static void registerCustomFoodValue(Item item, int foodValue) {
        customFoodValues.put(item, foodValue);
        EbzInventory.LOGGER.debug("Registrado valor nutricional personalizado para {}: {}",
                ForgeRegistries.ITEMS.getKey(item), foodValue);
    }

    /**
     * Obtiene el valor nutricional personalizado para un item, o su valor vanilla si no tiene uno personalizado
     */
    public static int getFoodValue(Item item) {
        // Si tiene un valor personalizado, lo usamos
        if (customFoodValues.containsKey(item)) {
            return customFoodValues.get(item);
        }

        // Si no, usamos el valor vanilla (si existe)
        FoodProperties foodProperties = item.getFoodProperties();
        return foodProperties != null ? foodProperties.getNutrition() : 0;
    }

    /**
     * Evento que se dispara cuando un jugador consume un item
     * Complementa el comportamiento del mixin para items específicos
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        try {
            if (!(event.getEntity() instanceof Player) || event.getEntity().level().isClientSide()) {
                return;
            }

            Player player = (Player) event.getEntity();
            ItemStack itemStack = event.getItem();
            Item item = itemStack.getItem();

            // Si es un item con valor nutricional personalizado, aplicar efectos adicionales
            if (customFoodValues.containsKey(item)) {
                // Aquí podrías implementar efectos especiales para alimentos específicos
                // Por ejemplo, efectos temporales, bonificaciones, etc.
                EbzInventory.LOGGER.info("Jugador {} consumió alimento especial: {}",
                        player.getDisplayName().getString(), itemStack.getDisplayName().getString());
            }
        } catch (Exception e) {
            EbzInventory.LOGGER.error("Error al procesar efectos de alimentación personalizados", e);
        }
    }
}