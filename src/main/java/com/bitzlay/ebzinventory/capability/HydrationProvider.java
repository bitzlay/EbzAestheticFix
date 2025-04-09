package com.bitzlay.ebzinventory.capability;

import com.bitzlay.ebzinventory.EbzInventory;
import com.bitzlay.ebzinventory.player.HydrationData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Proveedor para la capacidad de hidratación.
 * Permite adjuntar la capacidad a entidades y serializar/deserializar sus datos.
 */
public class HydrationProvider implements ICapabilitySerializable<CompoundTag> {
    /**
     * Identificador único para la capability de hidratación.
     * Debe ser consistente en todo el código.
     */
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(EbzInventory.MOD_ID, "hydration");

    /**
     * Instancia de datos de hidratación que será gestionada por este proveedor.
     */
    private final HydrationData hydrationData = new HydrationData();

    /**
     * Instancia de LazyOptional para la capability.
     * Es importante que se mantenga constante para toda la vida del proveedor.
     */
    private final LazyOptional<IHydration> hydrationOptional = LazyOptional.of(() -> hydrationData);

    // Para limitar la frecuencia de logs - aumentado a 60 segundos
    private long lastLogTime = 0;
    // Bandera para solo mostrar el log una vez al inicio
    private boolean initialLogDone = false;

    /**
     * Método principal para obtener la capability solicitada.
     * Este método es llamado por Forge cuando se necesita acceder a una capability.
     */
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        // Verificación simple sin logs frecuentes
        if (cap == ModCapabilities.PLAYER_HYDRATION) {
            // Solo logueamos la primera vez o cada 60 segundos en modo debug
            long currentTime = System.currentTimeMillis();
            if (!initialLogDone || (currentTime - lastLogTime > 60000)) {
                // Solo en modo DEBUG, no en modo INFO
                if (EbzInventory.LOGGER.isDebugEnabled()) {
                    EbzInventory.LOGGER.debug("HydrationProvider accedido");
                    lastLogTime = currentTime;
                    initialLogDone = true;
                }
            }

            return hydrationOptional.cast();
        }

        return LazyOptional.empty();
    }

    /**
     * Serializa los datos de la capability a NBT.
     * Este método es llamado por Forge cuando se guarda el mundo.
     */
    @Override
    public CompoundTag serializeNBT() {
        try {
            return hydrationData.serializeNBT();
        } catch (Exception e) {
            EbzInventory.LOGGER.error("Error al serializar datos de hidratación", e);
            // Devolvemos un tag vacío para evitar errores de guardado
            return new CompoundTag();
        }
    }

    /**
     * Deserializa los datos de la capability desde NBT.
     * Este método es llamado por Forge cuando se carga el mundo.
     */
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        try {
            hydrationData.deserializeNBT(nbt);
        } catch (Exception e) {
            EbzInventory.LOGGER.error("Error al deserializar datos de hidratación", e);
            // No hacemos nada más, mantenemos los valores por defecto
        }
    }

    /**
     * Método para invalidar el LazyOptional cuando la capability es liberada.
     * Este método es llamado por Forge cuando la entidad es destruida.
     */
    public void invalidate() {
        hydrationOptional.invalidate();
    }
}