package com.bitzlay.ebzinventory.network;

import com.bitzlay.ebzinventory.EbzInventory;
import com.bitzlay.ebzinventory.capability.IHydration;
import com.bitzlay.ebzinventory.player.PlayerHydrationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Clase para sincronizar los datos de hidratación entre servidor y cliente.
 * Implementa el sistema de paquetes de red de Forge para la sincronización.
 */
public class HydrationSyncPacket {
    // Protocol version para el canal de red
    private static final String PROTOCOL_VERSION = "1";

    // Canal de red para enviar/recibir paquetes
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(EbzInventory.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    // Variables para reducir logs
    private static final Map<UUID, Float> lastSyncValues = new HashMap<>();
    private static final Map<UUID, Long> lastSyncTimes = new HashMap<>();
    private static final long MIN_LOG_INTERVAL_MS = 10000; // 10 segundos entre logs
    private static final float MIN_VALUE_CHANGE = 0.5f;    // Cambio mínimo para loguear

    // Datos del paquete
    private final UUID playerId;
    private final float hydrationLevel;

    /**
     * Constructor con los datos necesarios para sincronizar.
     */
    public HydrationSyncPacket(UUID playerId, float hydrationLevel) {
        this.playerId = playerId;
        this.hydrationLevel = hydrationLevel;
    }

    /**
     * Codifica el paquete para enviarlo por la red.
     */
    public static void encode(HydrationSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerId);
        buf.writeFloat(msg.hydrationLevel);
    }

    /**
     * Decodifica el paquete recibido por la red.
     */
    public static HydrationSyncPacket decode(FriendlyByteBuf buf) {
        return new HydrationSyncPacket(buf.readUUID(), buf.readFloat());
    }

    /**
     * Maneja el paquete recibido.
     */
    public static void handle(HydrationSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Solo ejecutamos en el lado del cliente
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                handlePacket(msg);
            });
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Implementación del manejo del paquete en el cliente.
     */
    private static void handlePacket(HydrationSyncPacket msg) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null || minecraft.level == null) {
                EbzInventory.LOGGER.warn("Cliente no inicializado al recibir paquete de hidratación");
                return;
            }

            // Primero verificamos si es el jugador local
            Player localPlayer = minecraft.player;
            if (localPlayer != null && localPlayer.getUUID().equals(msg.playerId)) {
                PlayerHydrationManager.getHydrationData(localPlayer).ifPresent(hydrationData -> {
                    // Actualizar el valor de hidratación
                    hydrationData.setHydrationLevel(msg.hydrationLevel);

                    // Determinar si debemos loguear este cambio
                    if (shouldLogUpdate(msg.playerId, msg.hydrationLevel)) {
                        EbzInventory.LOGGER.info("[CLIENT] Actualizada hidratación del jugador local a: {}",
                                msg.hydrationLevel);
                    }
                });
                return;
            }

            // Si no es el jugador local, buscamos por UUID
            Player player = minecraft.level.getPlayerByUUID(msg.playerId);
            if (player != null) {
                PlayerHydrationManager.getHydrationData(player).ifPresent(hydrationData -> {
                    // Actualizar el valor de hidratación
                    hydrationData.setHydrationLevel(msg.hydrationLevel);

                    // Determinar si debemos loguear este cambio
                    if (shouldLogUpdate(msg.playerId, msg.hydrationLevel)) {
                        EbzInventory.LOGGER.info("[CLIENT] Actualizada hidratación de {} a: {}",
                                player.getName().getString(), msg.hydrationLevel);
                    }
                });
            } else {
                // Solo loguear errores de jugador no encontrado ocasionalmente
                long now = System.currentTimeMillis();
                if (!lastSyncTimes.containsKey(msg.playerId) ||
                        now - lastSyncTimes.getOrDefault(msg.playerId, 0L) > 30000) {
                    EbzInventory.LOGGER.warn("[CLIENT] No se encontró jugador con UUID {} al recibir paquete de hidratación",
                            msg.playerId);
                    lastSyncTimes.put(msg.playerId, now);
                }
            }
        } catch (Exception e) {
            EbzInventory.LOGGER.error("[CLIENT] Error al manejar paquete de hidratación", e);
        }
    }

    /**
     * Determina si debemos loguear la actualización basado en:
     * 1. Si ha habido un cambio significativo en el valor
     * 2. Si ha pasado suficiente tiempo desde el último log
     */
    private static boolean shouldLogUpdate(UUID playerId, float newValue) {
        long now = System.currentTimeMillis();
        long lastTime = lastSyncTimes.getOrDefault(playerId, 0L);
        float lastValue = lastSyncValues.getOrDefault(playerId, -1.0f);

        // Si es la primera vez o ha pasado mucho tiempo, siempre loguear
        if (lastValue < 0 || now - lastTime > 60000) {
            lastSyncValues.put(playerId, newValue);
            lastSyncTimes.put(playerId, now);
            return true;
        }

        // Si hay un cambio significativo y ha pasado el tiempo mínimo
        boolean significantChange = Math.abs(lastValue - newValue) >= MIN_VALUE_CHANGE;
        boolean timeElapsed = now - lastTime >= MIN_LOG_INTERVAL_MS;

        if (significantChange && timeElapsed) {
            lastSyncValues.put(playerId, newValue);
            lastSyncTimes.put(playerId, now);
            return true;
        }

        // Si ha pasado mucho tiempo, actualizar el tiempo pero sin loguear
        if (now - lastTime > MIN_LOG_INTERVAL_MS * 3) {
            lastSyncTimes.put(playerId, now);
        }

        return false;
    }

    /**
     * Registra los paquetes de red.
     * Este método debe ser llamado durante la inicialización del mod.
     */
    public static void register() {
        INSTANCE.registerMessage(0, HydrationSyncPacket.class,
                HydrationSyncPacket::encode,
                HydrationSyncPacket::decode,
                HydrationSyncPacket::handle);
        EbzInventory.LOGGER.info("Registrados paquetes de sincronización de hidratación");
    }

    /**
     * Envía los datos de hidratación al cliente.
     * Debe ser llamado desde el servidor.
     */
    public static void sendToClient(ServerPlayer player) {
        if (player == null) {
            EbzInventory.LOGGER.warn("Intento de enviar paquete de hidratación a jugador nulo");
            return;
        }

        try {
            PlayerHydrationManager.getHydrationData(player).ifPresent(hydrationData -> {
                float level = hydrationData.getHydrationLevel();

                // Determinar si debemos loguear este envío
                if (shouldLogUpdate(player.getUUID(), level)) {
                    EbzInventory.LOGGER.debug("[SERVER] Enviando hidratación {} a {}",
                            level, player.getName().getString());
                }

                // Siempre enviamos el paquete, pero solo logueamos cuando es necesario
                INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                        new HydrationSyncPacket(player.getUUID(), level));
            });
        } catch (Exception e) {
            EbzInventory.LOGGER.error("[SERVER] Error al enviar paquete de hidratación", e);
        }
    }
}