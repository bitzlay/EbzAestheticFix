package com.bitzlay.ebzinventory.capability;

import com.bitzlay.ebzinventory.EbzInventory;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Clase central para el registro y acceso a todas las capacidades del mod.
 * Es importante que esta clase se cargue temprano en el ciclo de vida del mod.
 */
@Mod.EventBusSubscriber(modid = EbzInventory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCapabilities {
    /**
     * Capability para acceder a los datos de hidratación del jugador.
     * Esta es LA ÚNICA instancia que debería ser usada en todo el código.
     */
    public static final Capability<IHydration> PLAYER_HYDRATION = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        EbzInventory.LOGGER.info("⚙️ Registrando capabilities desde ModCapabilities");
        try {
            // Registramos la interfaz IHydration
            event.register(IHydration.class);
            EbzInventory.LOGGER.info("✅ Capability IHydration registrada con éxito desde ModCapabilities");
        } catch (Exception e) {
            EbzInventory.LOGGER.error("❌ Error al registrar capability desde ModCapabilities", e);
            e.printStackTrace();
        }
    }
}