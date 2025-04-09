package com.bitzlay.ebzinventory;

import com.bitzlay.ebzinventory.capability.IHydration;
import com.bitzlay.ebzinventory.capability.ModCapabilities;
import com.bitzlay.ebzinventory.client.gui.RustStyleInventoryScreen;
import com.bitzlay.ebzinventory.player.FoodManager;
import com.bitzlay.ebzinventory.player.PlayerHydrationManager;
import com.bitzlay.ebzinventory.recipe.InventoryRecipeManager;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Clase principal del mod EbzInventory.
 */
@Mod(EbzInventory.MOD_ID)
public class EbzInventory {
    public static final String MOD_ID = "ebzinventory";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EbzInventory() {
        LOGGER.info("⚙️ Iniciando EbzInventory mod");
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();


        // CRÍTICO: Registramos el listener para capabilities EXPLÍCITAMENTE
        modEventBus.addListener(this::registerCapabilities);

        // Registramos la configuración
        LOGGER.info("⚙️ Inicializando configuración");
        com.bitzlay.ebzinventory.config.HydrationConfig.init();

        FoodManager.init();
        LOGGER.info("✅ Sistema de alimentación personalizado inicializado");

        // Registramos eventos en el bus del mod
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(FoodManager.class);

        // Registramos eventos en el bus de Forge
        MinecraftForge.EVENT_BUS.register(this);

        // Cargamos recetas
        InventoryRecipeManager.loadRecipes("config/inventory_recipes.json");

        LOGGER.info("✅ EbzInventory mod inicializado correctamente");
    }

    /**
     * Registramos explícitamente las capabilities en el evento RegisterCapabilitiesEvent
     */
    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        LOGGER.info("⚙️ Registrando capability de hidratación");
        try {
            // Registramos la interfaz IHydration
            event.register(IHydration.class);
            LOGGER.info("✅ Capacidad de hidratación registrada con éxito");
        } catch (Exception e) {
            LOGGER.error("❌ Error al registrar capability de hidratación", e);
            e.printStackTrace();
        }
    }

    /**
     * Configuración común para cliente y servidor.
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("⚙️ Iniciando configuración común");
        event.enqueueWork(() -> {
            try {
                // IMPORTANTE: Primero registramos los paquetes de red para sincronización
                com.bitzlay.ebzinventory.network.HydrationSyncPacket.register();
                LOGGER.info("✅ Red inicializada");

                // Después inicializamos el sistema de hidratación
                PlayerHydrationManager.init();
                LOGGER.info("✅ Sistema de hidratación inicializado");

                // Imprimimos para verificar que todo está bien
                LOGGER.info("   -> ModCapabilities.PLAYER_HYDRATION inicializado: {}",
                        (ModCapabilities.PLAYER_HYDRATION != null ? "SI" : "NO"));
                LOGGER.info("   -> PlayerHydrationManager.HYDRATION_CAPABILITY inicializado: {}",
                        (PlayerHydrationManager.HYDRATION_CAPABILITY != null ? "SI" : "NO"));
                LOGGER.info("   -> Son iguales: {}",
                        (ModCapabilities.PLAYER_HYDRATION == PlayerHydrationManager.HYDRATION_CAPABILITY ? "SI" : "NO"));
            } catch (Exception e) {
                LOGGER.error("❌ Error en configuración común", e);
                e.printStackTrace();
            }
        });
    }

    /**
     * Configuración específica del cliente.
     */
    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("⚙️ Iniciando configuración del cliente");
        event.enqueueWork(() -> {
            try {
                // Cualquier inicialización específica del cliente
                LOGGER.info("✅ Cliente inicializado");
            } catch (Exception e) {
                LOGGER.error("❌ Error en configuración del cliente", e);
            }
        });
    }

    /**
     * Evento de inicio del servidor.
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("⚙️ Servidor iniciando");
        // Verificar que las capabilities estén correctamente registradas
        LOGGER.info("   -> ModCapabilities.PLAYER_HYDRATION disponible: {}",
                (ModCapabilities.PLAYER_HYDRATION != null ? "SI" : "NO"));
    }

    /**
     * Eventos específicos del cliente.
     */
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onScreenOpen(ScreenEvent.Opening event) {
            if (event.getScreen() instanceof InventoryScreen && !(event.getScreen() instanceof RustStyleInventoryScreen)) {
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    event.setNewScreen(new RustStyleInventoryScreen(player.inventoryMenu, player.getInventory(), player.getDisplayName()));
                }
            }
        }

        @SubscribeEvent
        public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
            // Cancela el renderizado de la hotbar y barras de estado vanilla
            if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type() ||
                    event.getOverlay() == VanillaGuiOverlay.PLAYER_HEALTH.type() ||
                    event.getOverlay() == VanillaGuiOverlay.ARMOR_LEVEL.type() ||
                    event.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type() ||
                    event.getOverlay() == VanillaGuiOverlay.EXPERIENCE_BAR.type()) {
                event.setCanceled(true);
            }
        }
    }
}