package com.bitzlay.ebzinventory.mixin;

import com.bitzlay.ebzinventory.EbzInventory;
import com.bitzlay.ebzinventory.player.PlayerHydrationManager;
import com.bitzlay.ebzinventory.player.PlayerHydrationManager.PlayerActivityData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para modificar el comportamiento de FoodData (hambre) y hacerlo similar al sistema de hidratación.
 * Reemplaza el sistema vanilla de hambre con uno personalizado que usa los mismos ratios que la hidratación.
 */
@Mixin(FoodData.class)
public class ImprovedFoodDataMixin {

    @Shadow private int foodLevel;
    @Shadow private float saturationLevel;
    @Shadow private float exhaustionLevel;
    @Shadow private int tickTimer;

    // Variable para llevar el control de tiempo
    private int customTickCounter = 0;

    /**
     * Inyectamos al inicio del método tick para reemplazar completamente su comportamiento
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onCustomTick(Player player, CallbackInfo ci) {
        try {
            // Ignorar en modo creativo o espectador
            if (player.isCreative() || player.isSpectator()) {
                return;
            }

            // Solo procesamos en el lado del servidor
            if (player.level().isClientSide()) {
                return;
            }

            // Incrementamos nuestro contador personalizado
            customTickCounter++;

            // Procesar cada 600 ticks (30 segundos) - igual que la hidratación
            if (customTickCounter % 600 == 0) {
                // Calcular reducción basada en la actividad acumulada
                float reductionAmount = calculateFoodReductionFromActivity(player);

                // La reducción se aplica directamente al nivel de comida
                int previousFood = this.foodLevel;
                this.foodLevel = Math.max(0, this.foodLevel - Math.round(reductionAmount));

                // Si hubo cambio, mostrar log
                if (previousFood != this.foodLevel) {
                    EbzInventory.LOGGER.info("Hambre reducida en {} para {}. Nuevo nivel: {}",
                            reductionAmount, player.getDisplayName().getString(), this.foodLevel);
                }

                // Aplicar daño si el hambre es 0
                if (this.foodLevel <= 0) {
                    player.hurt(player.damageSources().starve(), 2.0F); // 1 corazón
                    EbzInventory.LOGGER.info("Jugador con hambre crítica, recibiendo daño: {}",
                            player.getDisplayName().getString());
                }

                // NOTA: No verificamos el nivel de hambre para sprint
                // El SprintMixin.java permite el sprint independientemente del nivel de hambre
            }

            // Regeneración de salud basada en hambre y saturación
            if (this.foodLevel >= 18 && player.isHurt()) {
                this.tickTimer++;

                // Regenerar cada 10 ticks (0.5 segundos) si hay suficiente alimento
                if (this.tickTimer >= 10) {
                    float healAmount = 1.0F; // Medio corazón

                    // Consumir saturación/hambre al curar
                    if (this.saturationLevel > 0) {
                        this.saturationLevel = Math.max(0, this.saturationLevel - 3.0F);
                    } else {
                        this.foodLevel = Math.max(0, this.foodLevel - 1);
                    }

                    player.heal(healAmount);
                    this.tickTimer = 0;
                }
            } else if (this.foodLevel <= 0) {
                // Contador para daño por hambre - se mantiene nativo
                this.tickTimer++;
                if (this.tickTimer >= 80) {
                    this.tickTimer = 0;
                }
            } else {
                this.tickTimer = 0;
            }

            // Cancelamos el método original
            ci.cancel();
        } catch (Exception e) {
            EbzInventory.LOGGER.error("Error en el procesamiento personalizado de hambre", e);
            // No cancelamos en caso de error para permitir el funcionamiento nativo
        }
    }

    /**
     * Calcula la reducción de hambre basada en la actividad del jugador,
     * usando el mismo sistema y ratios que la hidratación
     */
    private float calculateFoodReductionFromActivity(Player player) {
        // Base de reducción (siempre presente, incluso sin actividad)
        float baseReduction = 0.8F;

        // Obtener datos de actividad desde PlayerHydrationManager
        PlayerActivityData activityData = PlayerHydrationManager.getPlayerActivityData(player.getUUID());
        if (activityData == null) {
            return baseReduction; // Valor por defecto si no hay datos
        }

        float activityFactor = 0.0F;

        // Factor por distancia recorrida (bloques) - igual que en hidratación
        float distanceFactor = Math.min(2.0F, (float)(activityData.getDistanceTraveled() / 100.0) * 0.5F);
        activityFactor += distanceFactor;

        // Factor por saltos - igual que en hidratación
        float jumpFactor = Math.min(1.0F, (activityData.getJumpCount() / 10.0F) * 0.2F);
        activityFactor += jumpFactor;

        // Factor por exposición al sol
        if (player.level().canSeeSky(player.blockPosition()) &&
                player.level().isDay() &&
                !player.level().isRaining()) {
            activityFactor += 0.4F;
        }

        // No reducimos por estar en agua ya que eso es específico de hidratación

        // Calcular total
        float totalReduction = baseReduction + activityFactor;

        // Log para debug con información detallada
        EbzInventory.LOGGER.debug("Actividad para hambre de {}: Distancia={} bloques, Saltos={}, Reducción={}",
                player.getDisplayName().getString(),
                String.format("%.1f", activityData.getDistanceTraveled()),
                activityData.getJumpCount(),
                String.format("%.2f", totalReduction));

        // No reiniciamos los contadores aquí, ya que eso lo hace el sistema de hidratación

        return totalReduction;
    }
}