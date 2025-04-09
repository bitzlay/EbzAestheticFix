package com.bitzlay.ebzinventory.mixin;

import com.bitzlay.ebzinventory.EbzInventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Este mixin sobrescribe el método canSprint para permitir el sprint
 * independientemente del nivel de hambre.
 */
@Mixin(Player.class)
public class SprintMixin {

    /**
     * Sobrescribe completamente el método canSprint para que siempre devuelva true.
     *
     * @author EbzInventory
     * @reason Permitir sprint siempre, independientemente del nivel de hambre
     */
    @Overwrite
    public boolean canSprint() {
        // Simplemente devolver true siempre, ignorando el nivel de hambre
        return true;
    }
}