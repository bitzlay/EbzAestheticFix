package com.bitzlay.ebzinventory.mixin;

import com.bitzlay.ebzinventory.player.PlayerHydrationManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayer {
    /**
     * Inyecta directamente en el método eat para asegurarnos de que se aplique hidratación
     * al consumir cualquier item.
     */
    @Inject(method = "eat", at = @At("HEAD"))
    public void onEatDrink(Level level, ItemStack item, CallbackInfoReturnable<ItemStack> cir) {
        Player player = (Player) ((Object) this);
        com.bitzlay.ebzinventory.EbzInventory.LOGGER.info("MixinPlayer: Jugador {} consumiendo item {}",
                player.getName().getString(), item.getDisplayName().getString());

        // Verificamos si el item tiene un valor de hidratación
        float hydrationValue = com.bitzlay.ebzinventory.config.HydrationConfig.getHydrationValue(item.getItem());

        if (hydrationValue > 0) {
            com.bitzlay.ebzinventory.EbzInventory.LOGGER.info("MixinPlayer: Añadiendo hidratación {} para {}",
                    hydrationValue, player.getName().getString());

            // Añadir hidratación directamente
            PlayerHydrationManager.addHydration(player, hydrationValue);
        }
    }
}