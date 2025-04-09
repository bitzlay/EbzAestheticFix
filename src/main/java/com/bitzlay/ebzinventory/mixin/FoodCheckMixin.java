package com.bitzlay.ebzinventory.mixin;

import com.bitzlay.ebzinventory.EbzInventory;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Este mixin modifica el FoodData para que siempre reporte
 * un nivel de comida suficiente para correr.
 */
@Mixin(FoodData.class)
public class FoodCheckMixin {

    /**
     * Modifica el método getFoodLevel para devolver un valor alto
     * cuando se usa para comprobar el sprint.
     */
    @Inject(method = "getFoodLevel", at = @At("HEAD"), cancellable = true)
    private void ensureEnoughFoodForSprint(CallbackInfoReturnable<Integer> cir) {
        // Analizar la pila de llamadas para ver si este getFoodLevel es para sprint
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 0; i < Math.min(5, stackTrace.length); i++) {
            String methodName = stackTrace[i].getMethodName().toLowerCase();
            if (methodName.contains("sprint") || methodName.contains("run")) {
                // Si es para verificar sprint, devolvemos un valor alto (20)
                cir.setReturnValue(20);

                if (EbzInventory.LOGGER.isDebugEnabled()) {
                    EbzInventory.LOGGER.debug("Modificando getFoodLevel para permitir sprint: 20");
                }
                return;
            }
        }

        // Si no es para sprint, dejamos que continúe normalmente
    }
}