package com.bitzlay.ebzinventory.player;

import com.bitzlay.ebzinventory.EbzInventory;
import com.bitzlay.ebzinventory.capability.IHydration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Implementación de los datos de hidratación del jugador.
 * Versión simplificada y optimizada.
 */
public class HydrationData implements IHydration {
    // Constantes
    private static final float MAX_HYDRATION = 100.0F;
    private static final float MIN_HYDRATION = 0.0F;
    private static final float DEFAULT_HYDRATION = 100.0F;
    private static final String NBT_HYDRATION_KEY = "HydrationLevel";

    // Datos del jugador
    private float hydrationLevel;

    /**
     * Constructor por defecto.
     */
    public HydrationData() {
        this.hydrationLevel = DEFAULT_HYDRATION;
        EbzInventory.LOGGER.info("HydrationData inicializado con valor: {}", this.hydrationLevel);
    }

    /**
     * Añade hidratación cuando el jugador bebe algo.
     */
    @Override
    public float add(float amount) {
        float prevLevel = this.hydrationLevel;
        this.hydrationLevel = Math.min(this.hydrationLevel + amount, MAX_HYDRATION);
        float added = this.hydrationLevel - prevLevel;

        if (added > 0) {
            EbzInventory.LOGGER.info("Hidratación añadida: {} (Anterior: {}, Actual: {})",
                    added, prevLevel, this.hydrationLevel);
        }

        return added;
    }

    /**
     * Resta hidratación directamente.
     */
    @Override
    public float subtract(float amount) {
        float prevLevel = this.hydrationLevel;
        this.hydrationLevel = Math.max(this.hydrationLevel - amount, MIN_HYDRATION);
        float subtracted = prevLevel - this.hydrationLevel;

        return subtracted;
    }

    /**
     * Método de agotamiento - ahora simplificado
     * Lo mantenemos para compatibilidad con la interfaz, pero no hace nada
     */
    @Override
    public void addExhaustion(float exhaustion) {
        // No hace nada - sistema simplificado
    }

    /**
     * Método de actualización - ahora simplificado
     * Lo mantenemos para compatibilidad con la interfaz, pero no hace nada
     */
    @Override
    public void update(Player player, float deltaTime) {
        // No hace nada - la actualización se hace directamente en PlayerHydrationManager
    }

    /**
     * Obtiene el nivel actual de hidratación.
     */
    @Override
    public float getHydrationLevel() {
        return this.hydrationLevel;
    }

    /**
     * Obtiene el nivel máximo de hidratación.
     */
    @Override
    public float getMaxHydrationLevel() {
        return MAX_HYDRATION;
    }

    /**
     * Obtiene el porcentaje de hidratación.
     */
    @Override
    public float getHydrationPercentage() {
        return this.hydrationLevel / MAX_HYDRATION;
    }

    /**
     * Obtiene el nivel de agotamiento.
     * Siempre retorna 0 ya que ya no usamos este sistema.
     */
    @Override
    public float getExhaustionLevel() {
        return 0.0F;
    }

    /**
     * Establece el nivel de agotamiento.
     * No hace nada ya que ya no usamos este sistema.
     */
    @Override
    public void setExhaustionLevel(float level) {
        // No hace nada - sistema simplificado
    }

    /**
     * Establece directamente el nivel de hidratación.
     */
    @Override
    public void setHydrationLevel(float level) {
        float oldLevel = this.hydrationLevel;
        this.hydrationLevel = Math.max(MIN_HYDRATION, Math.min(MAX_HYDRATION, level));

        // Solo loguear si es un cambio significativo
        if (Math.abs(oldLevel - this.hydrationLevel) > 0.5) {
            EbzInventory.LOGGER.debug("Nivel de hidratación establecido: {} -> {}", oldLevel, this.hydrationLevel);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        try {
            CompoundTag tag = new CompoundTag();
            tag.putFloat(NBT_HYDRATION_KEY, this.hydrationLevel);
            return tag;
        } catch (Exception e) {
            EbzInventory.LOGGER.error("Error al serializar HydrationData", e);
            return new CompoundTag();
        }
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        try {
            if (nbt == null) {
                this.hydrationLevel = DEFAULT_HYDRATION;
                return;
            }

            if (nbt.contains(NBT_HYDRATION_KEY)) {
                float loadedValue = nbt.getFloat(NBT_HYDRATION_KEY);
                if (Float.isNaN(loadedValue) || Float.isInfinite(loadedValue)) {
                    this.hydrationLevel = DEFAULT_HYDRATION;
                } else {
                    this.hydrationLevel = Math.max(MIN_HYDRATION, Math.min(MAX_HYDRATION, loadedValue));
                }
            } else {
                this.hydrationLevel = DEFAULT_HYDRATION;
            }

            EbzInventory.LOGGER.debug("HydrationData cargado con valor: {}", this.hydrationLevel);
        } catch (Exception e) {
            EbzInventory.LOGGER.error("Error al deserializar datos de hidratación", e);
            this.hydrationLevel = DEFAULT_HYDRATION;
        }
    }
}