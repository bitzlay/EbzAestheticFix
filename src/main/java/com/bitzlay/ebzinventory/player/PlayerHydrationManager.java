package com.bitzlay.ebzinventory.player;

import com.bitzlay.ebzinventory.EbzInventory;
import com.bitzlay.ebzinventory.capability.HydrationProvider;
import com.bitzlay.ebzinventory.capability.IHydration;
import com.bitzlay.ebzinventory.capability.ModCapabilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.bitzlay.ebzinventory.EbzInventory.LOGGER;

/**
 * Gestor principal del sistema de hidratación.
 * Maneja eventos, callbacks y acceso a los datos de hidratación.
 */
@Mod.EventBusSubscriber(modid = EbzInventory.MOD_ID)
public class PlayerHydrationManager {
    // Definimos la capability - IMPORTANTE: Usar exactamente la misma instancia que en ModCapabilities
    public static final Capability<IHydration> HYDRATION_CAPABILITY = ModCapabilities.PLAYER_HYDRATION;

    private static final Map<UUID, PlayerActivityData> playerActivityMap = new HashMap<>();

    /**
     * Inicializa el sistema de hidratación
     */
    public static void init() {
        // Registramos eventos
        MinecraftForge.EVENT_BUS.register(PlayerHydrationManager.class);
        LOGGER.info("Sistema de hidratación inicializado");
    }

    /**
     * Registra un valor de hidratación para un item
     */
    public static void registerHydrationValue(Item item, float hydrationValue) {
        com.bitzlay.ebzinventory.config.HydrationConfig.registerHydrationValue(item, hydrationValue);
    }

    /**
     * Método para diagnóstico - imprime información detallada sobre las capabilities
     */
    public static void debugCapabilities(Player player) {
        try {
            if (player == null) {
                LOGGER.error("DEBUG: Player es nulo en debugCapabilities");
                return;
            }

            LOGGER.info("=================== DIAGNÓSTICO DE CAPABILITIES ===================");
            LOGGER.info("Jugador: {}", player.getName().getString());
            LOGGER.info("UUID: {}", player.getUUID());

            // 1. Comprobar si ModCapabilities.PLAYER_HYDRATION está inicializado
            LOGGER.info("ModCapabilities.PLAYER_HYDRATION inicializado: {}",
                    (ModCapabilities.PLAYER_HYDRATION != null ? "SI" : "NO"));

            // 2. Comprobar si HYDRATION_CAPABILITY está inicializado
            LOGGER.info("HYDRATION_CAPABILITY inicializado: {}",
                    (HYDRATION_CAPABILITY != null ? "SI" : "NO"));

            // 3. Comprobar si las dos instances son iguales
            LOGGER.info("¿Son la misma instancia?: {}",
                    (ModCapabilities.PLAYER_HYDRATION == HYDRATION_CAPABILITY ? "SI" : "NO"));

            // 4. Intentar obtener la capability directamente
            LazyOptional<IHydration> directCapOpt = player.getCapability(ModCapabilities.PLAYER_HYDRATION);
            LOGGER.info("Capacidad (directa) presente: {}", directCapOpt.isPresent() ? "SI" : "NO");

            // 5. Comprobar todas las capabilities del jugador
            LOGGER.info("Listado de capabilities adjuntas al jugador:");
            // No hay un método directo para enumerar todas las capabilities,
            // pero podemos imprimir si la nuestra existe

            // 6. Ejecutar getHydrationData y ver qué devuelve
            LazyOptional<IHydration> hydrationOpt = getHydrationData(player);
            LOGGER.info("getHydrationData devuelve present: {}", hydrationOpt.isPresent() ? "SI" : "NO");

            // 7. Si está presente, obtener los datos actuales
            hydrationOpt.ifPresent(hydration -> {
                LOGGER.info("  Nivel actual: {}", hydration.getHydrationLevel());
                LOGGER.info("  Nivel máximo: {}", hydration.getMaxHydrationLevel());
                LOGGER.info("  Nivel de agotamiento: {}", hydration.getExhaustionLevel());
            });

            LOGGER.info("==================================================================");
        } catch (Exception e) {
            LOGGER.error("Error en diagnóstico de capabilities", e);
            e.printStackTrace();
        }
    }

    /**
     * Obtiene los datos de hidratación de un jugador
     */
    public static LazyOptional<IHydration> getHydrationData(Player player) {
        if (player == null) return LazyOptional.empty();
        try {
            return player.getCapability(HYDRATION_CAPABILITY);
        } catch (Exception e) {
            LOGGER.error("Error al obtener capability de hidratación", e);
            return LazyOptional.empty();
        }
    }

    /**
     * Obtiene directamente los datos de hidratación
     */
    @Nullable
    public static IHydration getHydrationDataDirect(Player player) {
        try {
            return getHydrationData(player).orElse(null);
        } catch (Exception e) {
            LOGGER.error("Error al obtener datos de hidratación directos", e);
            return null;
        }
    }

    /**
     * Añade hidratación a un jugador
     */
    public static boolean addHydration(Player player, float amount) {
        try {
            LOGGER.debug("Intentando modificar {} de hidratación a {}", amount, player.getDisplayName().getString());

            LazyOptional<IHydration> capability = getHydrationData(player);
            if (capability.isPresent()) {
                capability.ifPresent(hydrationData -> {
                    try {
                        float added;
                        // Decidir si añadir o restar basado en el signo del valor
                        if (amount >= 0) {
                            added = hydrationData.add(amount);
                        } else {
                            added = -hydrationData.subtract(-amount); // Negativo para consistencia en logs
                        }

                        String operacion = amount >= 0 ? "añadido" : "restado";
                        LOGGER.info("{} {} de hidratación al jugador {}. Nuevo nivel: {}",
                                operacion, Math.abs(added), player.getDisplayName().getString(),
                                hydrationData.getHydrationLevel());

                        // Sincronizar con cliente
                        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                            com.bitzlay.ebzinventory.network.HydrationSyncPacket.sendToClient(serverPlayer);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error al modificar hidratación", e);
                    }
                });
                return true;
            }
            LOGGER.warn("No se pudo modificar hidratación a {} - capability no encontrada",
                    player.getDisplayName().getString());
            return false;
        } catch (Exception e) {
            LOGGER.error("Error al procesar modificación de hidratación", e);
            return false;
        }
    }

    // Eventos de Forge

    /**
     * Evento para adjuntar la capability a los jugadores
     */
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        try {
            if (!(event.getObject() instanceof Player)) return;

            // Creamos un nuevo proveedor para la capability
            HydrationProvider provider = new HydrationProvider();

            // IMPORTANTE: Usar un identificador único y consistente
            ResourceLocation resourceLocation = new ResourceLocation(EbzInventory.MOD_ID, "hydration");

            // Adjuntamos el proveedor al jugador
            event.addCapability(resourceLocation, provider);

            // Invalidamos el proveedor cuando se liberan las capabilities
            event.addListener(provider::invalidate);

            // Log sin usar getName() para evitar NPE
            EbzInventory.LOGGER.info("Capability de hidratación adjuntada a un jugador");

            // Solo logueamos el nombre si es seguro
            try {
                Player player = (Player) event.getObject();
                if (player.getGameProfile() != null && player.getDisplayName().getString() != null) {
                    EbzInventory.LOGGER.info("Capability adjuntada a jugador: {}", player.getDisplayName().getString());
                }
            } catch (Exception ignored) {
                // Si hay cualquier error al intentar obtener el nombre, simplemente lo ignoramos
            }
        } catch (Exception e) {
            EbzInventory.LOGGER.error("Error al adjuntar capability de hidratación", e);
            // No propagamos la excepción para evitar que falle el inicio de sesión
        }
    }

    /**
     * Clase para almacenar datos de actividad del jugador.
     * Se hace pública para permitir su acceso desde FoodManager.
     */
    public static class PlayerActivityData {
        private Vec3 lastPosition;
        private double distanceTraveled = 0;
        private int jumpCount = 0;
        private boolean wasOnGround = true;  // Variable para rastrear si estaba en el suelo
        private long lastJumpTime = 0;       // Tiempo del último salto

        public PlayerActivityData(Vec3 initialPosition, boolean onGround) {
            this.lastPosition = initialPosition;
            this.wasOnGround = onGround;
        }

        // Getters para permitir acceso desde FoodManager
        public double getDistanceTraveled() {
            return distanceTraveled;
        }

        public int getJumpCount() {
            return jumpCount;
        }

        public void setLastPosition(Vec3 position) {
            this.lastPosition = position;
        }

        public Vec3 getLastPosition() {
            return lastPosition;
        }

        public void setWasOnGround(boolean wasOnGround) {
            this.wasOnGround = wasOnGround;
        }

        public boolean getWasOnGround() {
            return wasOnGround;
        }

        public void setLastJumpTime(long time) {
            this.lastJumpTime = time;
        }

        public long getLastJumpTime() {
            return lastJumpTime;
        }

        public void addDistanceTraveled(double distance) {
            this.distanceTraveled += distance;
        }

        public void incrementJumpCount() {
            this.jumpCount++;
        }

        public void resetCounters() {
            this.distanceTraveled = 0;
            this.jumpCount = 0;
        }
    }

    /**
     * Sistema para actualizar hidratación y actividad del jugador.
     * El control de regeneración ahora se maneja en ImprovedFoodDataMixin.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Solo procesar en la fase END y en el lado del servidor
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }

        // No procesar en modo creativo o espectador
        if (event.player.isCreative() || event.player.isSpectator()) {
            return;
        }

        Player player = event.player;
        UUID playerId = player.getUUID();

        // Actualizar datos de actividad cada tick
        updatePlayerActivity(player, playerId);

        // Procesamiento de hidratación cada 30 segundos (600 ticks)
        if (player.tickCount % 600 == 0) {
            getHydrationData(player).ifPresent(hydrationData -> {
                try {
                    // Calcular reducción basada en actividad acumulada
                    float reductionAmount = calculateHydrationReductionFromActivity(player, playerId);

                    // Aplicar reducción
                    hydrationData.subtract(reductionAmount);

                    // Aplicar efectos si es necesario
                    if (hydrationData.getHydrationLevel() <= 40) {
                        applyDehydrationEffects(player, hydrationData.getHydrationLevel());
                    }

                    // Aplicar daño si la hidratación es 0
                    if (hydrationData.getHydrationLevel() <= 0) {
                        player.hurt(player.damageSources().starve(), 2.0F); // 1 corazón
                        LOGGER.info("Jugador completamente deshidratado, recibiendo daño: {}",
                                player.getDisplayName().getString());
                    }

                    // Sincronizar con cliente
                    if (player instanceof ServerPlayer serverPlayer) {
                        com.bitzlay.ebzinventory.network.HydrationSyncPacket.sendToClient(serverPlayer);
                    }

                    // Log con más detalles
                    PlayerActivityData activityData = playerActivityMap.get(playerId);
                    LOGGER.info("Hidratación reducida en {} para {}. Nuevo nivel: {} (Distancia: {}, Saltos: {})",
                            reductionAmount,
                            player.getDisplayName().getString(),
                            hydrationData.getHydrationLevel(),
                            String.format("%.1f", activityData != null ? activityData.distanceTraveled : 0),
                            activityData != null ? activityData.jumpCount : 0);
                } catch (Exception e) {
                    LOGGER.error("Error al actualizar hidratación", e);
                }
            });
        }

        // Sincronizamos con el cliente cada 5 segundos
        if (player.tickCount % 100 == 0 && player instanceof ServerPlayer serverPlayer) {
            com.bitzlay.ebzinventory.network.HydrationSyncPacket.sendToClient(serverPlayer);
        }
    }
    /**
     * Método auxiliar para aplicar efectos de deshidratación
     */
    private static void applyDehydrationEffects(Player player, float hydrationLevel) {
        try {
            // No aplicamos daño aquí, eso lo hacemos en onPlayerTick
            // Solo efectos visuales o de juego

            if (hydrationLevel <= 20 && hydrationLevel > 0) {
                // Deshidratación moderada
                LOGGER.info("Jugador con deshidratación severa: {}, nivel: {}",
                        player.getDisplayName().getString(), hydrationLevel);

                // Opcionalmente, puedes aplicar efectos negativos
                // player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0));
            } else if (hydrationLevel <= 40 && hydrationLevel > 20) {
                // Deshidratación leve
                LOGGER.debug("Jugador con deshidratación moderada: {}, nivel: {}",
                        player.getDisplayName().getString(), hydrationLevel);
            }
        } catch (Exception e) {
            LOGGER.error("Error al aplicar efectos de deshidratación", e);
        }
    }


    // Actualiza los datos de actividad del jugador - Mejorado para detectar movimiento y saltos correctamente
    private static void updatePlayerActivity(Player player, UUID playerId) {
        Vec3 currentPosition = player.position();
        boolean isOnGround = player.onGround();

        // Obtener o crear datos de actividad para este jugador
        PlayerActivityData activityData = playerActivityMap.computeIfAbsent(
                playerId, id -> new PlayerActivityData(currentPosition, isOnGround));

        // Calcular distancia recorrida desde la última posición
        if (activityData.lastPosition != null) {
            double dx = currentPosition.x - activityData.lastPosition.x;
            double dz = currentPosition.z - activityData.lastPosition.z; // Ignoramos Y para distancia horizontal
            double distance = Math.sqrt(dx * dx + dz * dz);

            // Solo considerar movimientos significativos
            if (distance > 0.05) {  // Reducido el umbral para ser más sensible
                activityData.distanceTraveled += distance;
                // Log detallado para depuración ocasional
                if (player.tickCount % 100 == 0) {
                    LOGGER.debug("Jugador {} se movió {} bloques, acumulado: {}",
                            player.getDisplayName().getString(), String.format("%.2f", distance),
                            String.format("%.2f", activityData.distanceTraveled));
                }
            }
        }

        // Actualizar la posición para el próximo tick
        activityData.lastPosition = currentPosition;

        // Detectar saltos - Mejorado para detectar transiciones de en-suelo a no-en-suelo
        long currentTime = System.currentTimeMillis();
        if (activityData.wasOnGround && !isOnGround && player.getDeltaMovement().y > 0.1) {
            // El jugador estaba en el suelo, ahora no lo está, y está moviéndose hacia arriba = salto
            if (currentTime - activityData.lastJumpTime > 500) { // Evitar detectar el mismo salto múltiples veces
                activityData.jumpCount++;
                activityData.lastJumpTime = currentTime;
                LOGGER.debug("Jugador {} realizó un salto, total: {}",
                        player.getDisplayName().getString(), activityData.jumpCount);
            }
        }

        // Actualizar estado en suelo para el próximo tick
        activityData.wasOnGround = isOnGround;
    }

    // Calcula la reducción de hidratación basada en actividad acumulada
    private static float calculateHydrationReductionFromActivity(Player player, UUID playerId) {
        // Base de reducción (siempre presente, incluso sin actividad)
        float baseReduction = 0.8F;

        // Obtener datos de actividad
        PlayerActivityData activityData = playerActivityMap.get(playerId);
        if (activityData == null) {
            return baseReduction; // Valor por defecto si no hay datos
        }

        float activityFactor = 0.0F;

        // Factor por distancia recorrida (bloques)
        // 0.5 puntos adicionales por cada 100 bloques recorridos, hasta un máximo de 2.0
        float distanceFactor = Math.min(2.0F, (float)(activityData.distanceTraveled / 100.0) * 0.5F);
        activityFactor += distanceFactor;

        // Factor por saltos
        // 0.2 puntos adicionales por cada 10 saltos, hasta un máximo de 1.0
        float jumpFactor = Math.min(1.0F, (activityData.jumpCount / 10.0F) * 0.2F);
        activityFactor += jumpFactor;

/*        // Factor por bioma
        if (player.level().getBiome(player.blockPosition()).is(net.minecraft.tags.BiomeTags.IS_DESERT) ||
                player.level().getBiome(player.blockPosition()).is(net.minecraft.tags.BiomeTags.IS_SAVANNA) ||
                player.level().getBiome(player.blockPosition()).is(net.minecraft.tags.BiomeTags.IS_JUNGLE)) {
            activityFactor += 0.7F;
        }
*/
        // Factor por exposición al sol
        if (player.level().canSeeSky(player.blockPosition()) &&
                player.level().isDay() &&
                !player.level().isRaining()) {
            activityFactor += 0.4F;
        }

        // Factor por estar en agua (reduce)
        if (player.isInWater()) {
            activityFactor -= 0.3F;
        }

        // Calcular total y permitir que sea negativo (para hidratación en agua, por ejemplo)
        float totalReduction = baseReduction + activityFactor;
        // Opcional: establecer un mínimo si no quieres que pueda ser negativo
        // totalReduction = Math.max(totalReduction, -1.0F);  // Permitir hasta -1 de "ganancia" de hidratación

        // Log para debug con información detallada
        LOGGER.debug("Actividad de {}: Distancia={} bloques, Saltos={}, Reducción={}",
                player.getDisplayName().getString(),
                String.format("%.1f", activityData.distanceTraveled),
                activityData.jumpCount,
                String.format("%.2f", totalReduction));

        // Reiniciar contadores después de aplicar la reducción
        activityData.distanceTraveled = 0;
        activityData.jumpCount = 0;

        return totalReduction;
    }



    /**
     * Cuando el jugador está listo en el cliente
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        try {
            Player player = event.getEntity();
            if (!player.level().isClientSide()) {
                LOGGER.info("Jugador {} iniciando sesión, verificando hidratación", player.getName().getString());

                // Debug de capabilities
                debugCapabilities(player);

                getHydrationData(player).ifPresent(hydrationData -> {
                    if (hydrationData.getHydrationLevel() <= 0) {
                        hydrationData.setHydrationLevel(100.0F);
                        LOGGER.info("Inicializada hidratación a 100 para jugador que inicia sesión: {}",
                                player.getName().getString());
                    } else {
                        LOGGER.info("Jugador {} tiene hidratación de {}",
                                player.getName().getString(), hydrationData.getHydrationLevel());
                    }

                    // Sincronizar siempre con el cliente
                    if (player instanceof ServerPlayer serverPlayer) {
                        com.bitzlay.ebzinventory.network.HydrationSyncPacket.sendToClient(serverPlayer);
                        LOGGER.debug("Enviado paquete de sincronización inicial a {}", player.getName().getString());
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("Error en evento de inicio de sesión", e);
        }
    }

    /**
     * Cuando el jugador consume un item
     * Este evento es crítico para la hidratación al beber
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        try {
            if (!(event.getEntity() instanceof Player)) return;
            if (event.getEntity().level().isClientSide()) return;

            Player player = (Player) event.getEntity();

            // Debug de capabilities
            debugCapabilities(player);

            ItemStack itemStack = event.getItem();
            Item item = itemStack.getItem();

            // Log para depuración
            LOGGER.info("Jugador {} consumió item: {}",
                    player.getDisplayName().getString(), itemStack.getDisplayName().getString());

            // Verificar valor de hidratación
            float hydrationValue = com.bitzlay.ebzinventory.config.HydrationConfig.getHydrationValue(item);

            // Log para depuración
            LOGGER.info("Valor de hidratación para {}: {}",
                    ForgeRegistries.ITEMS.getKey(item), hydrationValue);

            // Procesar cualquier valor de hidratación, no solo positivos
            if (hydrationValue != 0) {
                boolean success = addHydration(player, hydrationValue);
                LOGGER.info("Modificar hidratación resultado: {} - Valor: {}", success, hydrationValue);

                if (success && player instanceof ServerPlayer serverPlayer) {
                    com.bitzlay.ebzinventory.network.HydrationSyncPacket.sendToClient(serverPlayer);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error al procesar hidratación para item consumido", e);
        }
    }

    /**
     * Copiar datos de hidratación en respawn/clonación
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        try {
            Player newPlayer = event.getEntity();
            Player originalPlayer = event.getOriginal();

            LOGGER.info("Evento PlayerClone: isDeath={}", event.isWasDeath());

            if (event.isWasDeath()) {
                // Para muerte, inicializar con valor máximo
                getHydrationData(newPlayer).ifPresent(newStore -> {
                    try {
                        newStore.setHydrationLevel(100.0F);
                        LOGGER.info("Hidratación inicializada a 100 para jugador en respawn: {}",
                                newPlayer.getName().getString());

                        if (newPlayer instanceof ServerPlayer serverPlayer) {
                            com.bitzlay.ebzinventory.network.HydrationSyncPacket.sendToClient(serverPlayer);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error al establecer hidratación en respawn", e);
                    }
                });
            } else {
                // Para cambio dimensional, copiar datos
                try {
                    originalPlayer.reviveCaps();

                    LazyOptional<IHydration> oldData = getHydrationData(originalPlayer);
                    LazyOptional<IHydration> newData = getHydrationData(newPlayer);

                    if (oldData.isPresent() && newData.isPresent()) {
                        oldData.ifPresent(oldStore -> {
                            newData.ifPresent(newStore -> {
                                try {
                                    // Copiar los valores individualmente ya que no tenemos un método copy
                                    newStore.setHydrationLevel(oldStore.getHydrationLevel());
                                    newStore.setExhaustionLevel(oldStore.getExhaustionLevel());
                                    LOGGER.info("Datos de hidratación copiados para jugador en cambio dimensional: {}",
                                            newPlayer.getName().getString());

                                    if (newPlayer instanceof ServerPlayer serverPlayer) {
                                        com.bitzlay.ebzinventory.network.HydrationSyncPacket.sendToClient(serverPlayer);
                                    }
                                } catch (Exception e) {
                                    LOGGER.error("Error al copiar datos de hidratación", e);
                                    newStore.setHydrationLevel(100.0F);
                                }
                            });
                        });
                    }

                    originalPlayer.invalidateCaps();
                } catch (Exception e) {
                    LOGGER.error("Error al manejar clon dimensional", e);
                    // En caso de error, establecer a 100
                    getHydrationData(newPlayer).ifPresent(data -> data.setHydrationLevel(100.0F));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error en evento de clonación", e);
        }
    }

    /**
     * Mantener sincronización en cambio de dimensión
     */
    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        try {
            Player player = event.getEntity();
            if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                LOGGER.info("Sincronizando hidratación para jugador después de cambio de dimensión: {}",
                        player.getName().getString());
                com.bitzlay.ebzinventory.network.HydrationSyncPacket.sendToClient(serverPlayer);
            }
        } catch (Exception e) {
            LOGGER.error("Error en evento de cambio de dimensión", e);
        }
    }

    public static PlayerActivityData getPlayerActivityData(UUID playerId) {
        return playerActivityMap.get(playerId);
    }

    /**
     * Mantener sincronización en respawn
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        try {
            Player player = event.getEntity();
            if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                LOGGER.info("Sincronizando hidratación para jugador después de respawn: {}",
                        player.getName().getString());
                com.bitzlay.ebzinventory.network.HydrationSyncPacket.sendToClient(serverPlayer);
            }
        } catch (Exception e) {
            LOGGER.error("Error en evento de respawn", e);
        }
    }
}