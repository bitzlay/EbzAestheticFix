package com.bitzlay.ebzinventory.mixin;

import com.bitzlay.ebzinventory.EbzInventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin ensures that lack of hunger never prevents sprinting
 * by using an alternative workaround.
 */
@Mixin(Player.class)
public class AlternativeSprintMixin {

    @Shadow @Final private FoodData foodData;

    /**
     * Injects at the end of the Player's tick method to ensure that sprinting
     * is not canceled due to hunger.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void onPlayerTick(CallbackInfo ci) {
        try {
            // Get player instance
            Player player = (Player)(Object)this;

            // If the player is trying to sprint but is not currently sprinting,
            // force sprinting
            if (!player.isSprinting() && player.isShiftKeyDown()) {
                // Check if the player has any movement key pressed
                boolean isMoving = player.zza > 0; // zza is forward movement

                // If moving, allow sprint
                if (isMoving) {
                    player.setSprinting(true);

                    if (EbzInventory.LOGGER.isDebugEnabled()) {
                        EbzInventory.LOGGER.debug("Forcing player sprint");
                    }
                }
            }
        } catch (Exception e) {
            EbzInventory.LOGGER.error("Error while forcing sprint", e);
        }
    }
}
