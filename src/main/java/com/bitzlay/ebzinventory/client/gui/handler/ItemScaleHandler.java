package com.bitzlay.ebzinventory.client.gui.handler;

import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.bitzlay.ebzinventory.EbzInventory;

@Mod.EventBusSubscriber(modid = EbzInventory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemScaleHandler {

    // Base scale for item rendering
    private static final float BASE_SCALE = 1.5f;

    @SubscribeEvent
    public static void onGuiRender(ScreenEvent.Render.Post event) {
        // No intentar escalar adicionalmente - la interfaz ya está ajustada
    }
}