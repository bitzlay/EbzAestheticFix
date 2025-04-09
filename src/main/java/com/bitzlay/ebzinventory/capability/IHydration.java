package com.bitzlay.ebzinventory.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Interfaz para la capacidad de hidratación del jugador.
 * Define los métodos que están disponibles para gestionar la hidratación.
 */
public interface IHydration {
    /**
     * Obtiene el nivel actual de hidratación.
     * @return El nivel de hidratación (0-100)
     */
    float getHydrationLevel();

    /**
     * Establece el nivel de hidratación a un valor específico.
     * @param value Nuevo nivel de hidratación (será restringido a 0-100)
     */
    void setHydrationLevel(float value);

    /**
     * Obtiene el nivel actual de agotamiento.
     * @return El nivel de agotamiento
     */
    float getExhaustionLevel();

    /**
     * Establece el nivel de agotamiento a un valor específico.
     * @param value Nuevo nivel de agotamiento
     */
    void setExhaustionLevel(float value);

    /**
     * Añade agotamiento, que eventualmente reducirá la hidratación.
     * @param amount Cantidad de agotamiento a añadir
     */
    void addExhaustion(float amount);

    /**
     * Actualiza el estado de hidratación basado en el tiempo transcurrido.
     * @param player Jugador a actualizar
     * @param deltaTime Tiempo transcurrido desde la última actualización
     */
    void update(Player player, float deltaTime);

    /**
     * Añade hidratación al jugador.
     * @param amount Cantidad de hidratación a añadir
     * @return La cantidad real añadida
     */
    float add(float amount);

    /**
     * Resta hidratación al jugador.
     * @param amount Cantidad de hidratación a restar
     * @return La cantidad real restada
     */
    float subtract(float amount);

    /**
     * Obtiene el nivel máximo de hidratación.
     * @return El nivel máximo de hidratación (normalmente 100)
     */
    float getMaxHydrationLevel();

    /**
     * Obtiene el porcentaje de hidratación.
     * @return El porcentaje de hidratación (0.0-1.0)
     */
    float getHydrationPercentage();

    /**
     * Serializa los datos de hidratación a NBT.
     * @return Un CompoundTag con los datos serializados
     */
    CompoundTag serializeNBT();

    /**
     * Deserializa los datos de hidratación desde NBT.
     * @param tag El CompoundTag con los datos serializados
     */
    void deserializeNBT(CompoundTag tag);
}