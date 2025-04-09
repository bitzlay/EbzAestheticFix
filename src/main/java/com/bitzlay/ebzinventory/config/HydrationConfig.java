package com.bitzlay.ebzinventory.config;

import com.bitzlay.ebzinventory.EbzInventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = EbzInventory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HydrationConfig {
    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

    // Configuración de hidratación
    public static final ForgeConfigSpec.DoubleValue HYDRATION_DECREASE_INTERVAL;
    public static final ForgeConfigSpec.DoubleValue HYDRATION_DECREASE_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HYDRATION_ITEMS; // Añadido este campo faltante

    // Mapa para almacenar los valores de hidratación
    private static final Map<Item, Float> hydrationValues = new HashMap<>();

    // Lista de configuración predeterminada de items y sus valores de hidratación
    private static final List<String> DEFAULT_HYDRATION_ITEMS = Arrays.asList(
            // Formato: registry_name:value
            "minecraft:water_bottle:35.0",
            "minecraft:potion:40.0",    // Poción (debería verificarse si es agua)
            "minecraft:milk_bucket:30.0",
            "minecraft:melon_slice:5.0",
            "minecraft:apple:3.0",
            "minecraft:golden_apple:-10.0",
            "minecraft:enchanted_golden_apple:20.0",
            "minecraft:carrot:2.0",
            "minecraft:beetroot:2.0",
            "minecraft:beetroot_soup:15.0",
            "minecraft:mushroom_stew:10.0",
            "minecraft:rabbit_stew:15.0",
            "minecraft:suspicious_stew:10.0",
            "minecraft:sweet_berries:2.0",
            "minecraft:glow_berries:2.0",
            "minecraft:honey_bottle:10.0"
    );

    static {
        SERVER_BUILDER.comment("Configuración del sistema de hidratación").push("hydration");

        HYDRATION_DECREASE_INTERVAL = SERVER_BUILDER
                .comment("Cada cuántos segundos disminuye la hidratación del jugador",
                        "Valores más altos = menos actualizaciones = mejor rendimiento",
                        "Valores recomendados: 30-60 segundos")
                .defineInRange("hydrationDecreaseInterval", 60.0, 5.0, 300.0);

        HYDRATION_DECREASE_AMOUNT = SERVER_BUILDER
                .comment("Cuántos puntos de hidratación se reducen en cada intervalo",
                        "Valores más altos = deshidratación más rápida",
                        "Valores recomendados: 1-3 puntos")
                .defineInRange("hydrationDecreaseAmount", 7.0, 0.1, 10.0);



        // Definición del campo HYDRATION_ITEMS
        HYDRATION_ITEMS = SERVER_BUILDER
                .comment("Lista de items y sus valores de hidratación",
                        "Formato: modid:itemname:value",
                        "Ejemplo: minecraft:water_bucket:40.0")
                .defineList("hydrationItems", DEFAULT_HYDRATION_ITEMS, s -> true);

        SERVER_BUILDER.pop();
    }

    public static final ForgeConfigSpec SERVER_CONFIG = SERVER_BUILDER.build();

    // Inicializamos valores predeterminados inmediatamente para tener algo disponible
    static {
        // Cargar valores predeterminados inmediatamente
        registerDefaultHydrationValues();
    }

    /**
     * Registra la configuración con Forge
     */
    public static void init() {
        try {
            ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG);
            EbzInventory.LOGGER.info("Configuración de hidratación registrada");

            // Cargar valores predeterminados inmediatamente
            loadHydrationValues();

            // Verificar que los valores de configuración se cargaron correctamente
            EbzInventory.LOGGER.info("Intervalo de deshidratación configurado: {} segundos",
                    HYDRATION_DECREASE_INTERVAL.get());
            EbzInventory.LOGGER.info("Cantidad de deshidratación configurada: {} puntos",
                    HYDRATION_DECREASE_AMOUNT.get());
        } catch (Exception e) {
            EbzInventory.LOGGER.error("Error al inicializar configuración de hidratación", e);
        }
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        EbzInventory.LOGGER.info("Cargando configuración de hidratación...");
        loadHydrationValues();
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading event) {
        EbzInventory.LOGGER.info("Recargando configuración de hidratación...");
        loadHydrationValues();
    }

    /**
     * Carga los valores de hidratación desde la configuración
     */
    private static void loadHydrationValues() {
        // No limpiamos el mapa para preservar valores predeterminados si la configuración falla

        try {
            // Log para depuración
            EbzInventory.LOGGER.info("Cargando valores de hidratación desde la configuración...");

            // Cargamos valores desde la configuración
            List<? extends String> configItems = HYDRATION_ITEMS.get();

            if (configItems != null && !configItems.isEmpty()) {
                for (String entry : configItems) {
                    try {
                        // Formato esperado: modid:itemname:value
                        String[] parts = entry.split(":");
                        if (parts.length < 3) {
                            EbzInventory.LOGGER.warn("Formato incorrecto para valor de hidratación: {}", entry);
                            continue;
                        }

                        String modId = parts[0];
                        String itemName = parts[1];
                        float value = Float.parseFloat(parts[2]);

                        Item item = ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation(modId, itemName));
                        if (item != null && item != Items.AIR) {
                            hydrationValues.put(item, value);
                            EbzInventory.LOGGER.debug("Registrado valor de hidratación para {}: {}",
                                    modId + ":" + itemName, value);
                        } else {
                            EbzInventory.LOGGER.warn("Item no encontrado: {}", modId + ":" + itemName);
                        }
                    } catch (NumberFormatException e) {
                        EbzInventory.LOGGER.warn("Valor de hidratación inválido en: {}", entry);
                    } catch (Exception e) {
                        EbzInventory.LOGGER.error("Error al procesar valor de hidratación: {}", entry, e);
                    }
                }
                EbzInventory.LOGGER.info("Cargados {} valores de hidratación desde la configuración",
                        configItems.size());
            } else {
                EbzInventory.LOGGER.warn("No se encontraron items en la configuración, usando valores predeterminados");
            }

            // Imprimir todos los valores de hidratación para depuración
            StringBuilder valuesLog = new StringBuilder("Valores de hidratación actuales:\n");
            hydrationValues.forEach((item, value) -> {
                valuesLog.append(ForgeRegistries.ITEMS.getKey(item))
                        .append(" -> ")
                        .append(value)
                        .append("\n");
            });
            EbzInventory.LOGGER.info(valuesLog.toString());

        } catch (Exception e) {
            EbzInventory.LOGGER.error("Error al cargar la configuración de hidratación", e);
            EbzInventory.LOGGER.info("Usando valores predeterminados para hidratación");
            registerDefaultHydrationValues();
        }
    }

    /**
     * Registra los valores predeterminados para compatibilidad con mods
     * En caso de que no se carguen desde la configuración
     */
    private static void registerDefaultHydrationValues() {
        // Valores predeterminados
        registerHydrationValueDirect(Items.POTION, 35.0F);
        registerHydrationValueDirect(Items.WATER_BUCKET, 40.0F);
        registerHydrationValueDirect(Items.MILK_BUCKET, 30.0F);
        registerHydrationValueDirect(Items.MELON_SLICE, 5.0F);
        registerHydrationValueDirect(Items.APPLE, 3.0F);
        registerHydrationValueDirect(Items.GOLDEN_APPLE, -10.0F);
        registerHydrationValueDirect(Items.ENCHANTED_GOLDEN_APPLE, 20.0F);
        registerHydrationValueDirect(Items.CARROT, 2.0F);
        registerHydrationValueDirect(Items.BEETROOT, 2.0F);
        registerHydrationValueDirect(Items.BEETROOT_SOUP, 15.0F);
        registerHydrationValueDirect(Items.MUSHROOM_STEW, 10.0F);
        registerHydrationValueDirect(Items.RABBIT_STEW, 15.0F);
        registerHydrationValueDirect(Items.SUSPICIOUS_STEW, 10.0F);
        registerHydrationValueDirect(Items.SWEET_BERRIES, 2.0F);
        registerHydrationValueDirect(Items.GLOW_BERRIES, 2.0F);
        registerHydrationValueDirect(Items.HONEY_BOTTLE, 10.0F);

        // Añadir más alimentos comunes para mayor compatibilidad
        registerHydrationValueDirect(Items.BREAD, 1.0F);
        registerHydrationValueDirect(Items.COOKED_BEEF, 1.0F);
        registerHydrationValueDirect(Items.COOKED_CHICKEN, 1.0F);
        registerHydrationValueDirect(Items.COOKED_MUTTON, 1.0F);
        registerHydrationValueDirect(Items.COOKED_PORKCHOP, 1.0F);
        registerHydrationValueDirect(Items.COOKED_COD, 2.0F);
        registerHydrationValueDirect(Items.COOKED_SALMON, 2.0F);

        EbzInventory.LOGGER.info("Valores de hidratación predeterminados registrados");
    }

    /**
     * Obtiene el valor de hidratación para un item
     *
     * @param item Item a consultar
     * @return Valor de hidratación, o 0 si no tiene
     */
    public static float getHydrationValue(Item item) {
        float value = hydrationValues.getOrDefault(item, 0.0F);
        EbzInventory.LOGGER.debug("Obteniendo valor de hidratación para {}: {}",
                ForgeRegistries.ITEMS.getKey(item), value);
        return value;
    }

    /**
     * Método interno para registrar valores predeterminados
     */
    private static void registerHydrationValueDirect(Item item, float value) {
        hydrationValues.put(item, value);
    }

    /**
     * Registra manualmente un valor de hidratación para un item
     * Útil para que otros mods puedan añadir sus propios items
     *
     * @param item Item a registrar
     * @param value Valor de hidratación
     */
    public static void registerHydrationValue(Item item, float value) {
        hydrationValues.put(item, value);
        EbzInventory.LOGGER.info("Registrado manualmente valor de hidratación para {}: {}",
                ForgeRegistries.ITEMS.getKey(item), value);
    }

    /**
     * Obtiene una copia del mapa de valores de hidratación
     *
     * @return Mapa de items y sus valores de hidratación
     */
    public static Map<Item, Float> getHydrationValues() {
        return new HashMap<>(hydrationValues);
    }
}