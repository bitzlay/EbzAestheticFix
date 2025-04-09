package com.bitzlay.ebzinventory.client.gui;



import com.bitzlay.ebzinventory.client.gui.components.*;

import com.bitzlay.ebzinventory.client.gui.model.UIState;

import com.bitzlay.ebzinventory.client.gui.render.*;

import com.bitzlay.ebzinventory.client.gui.util.UIScaling;

import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.gui.components.AbstractWidget;

import net.minecraft.client.gui.components.Button;

import net.minecraft.client.gui.components.Renderable;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import net.minecraft.network.chat.Component;

import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.entity.player.Inventory;

import net.minecraft.world.entity.player.Player;

import net.minecraft.world.inventory.InventoryMenu;

import net.minecraft.world.inventory.Slot;

import net.minecraft.client.gui.components.ImageButton;

import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.level.GameType;

import org.lwjgl.glfw.GLFW;



/**

 * Pantalla de inventario con estilo tipo Rust.

 * Esta clase coordina los diferentes componentes de la interfaz.

 */

public class RustStyleInventoryScreen extends AbstractContainerScreen<InventoryMenu> {

    private static final ResourceLocation INVENTORY_LOCATION =

            new ResourceLocation("ebzinventory", "textures/gui/rust_inventory.png");



    private Button creativeInventoryButton;

    private static final ResourceLocation CREATIVE_BUTTON_TEXTURE =

            new ResourceLocation("ebzinventory", "textures/gui/creative_button.png");



    // Componentes principales

    private final UIState uiState;

    private final InventoryRenderer inventoryRenderer;

    private final CraftingRenderer craftingRenderer;

    private final QueueRenderer queueRenderer;

    private final QuickCraftPanel quickCraftPanel;

    private final InventorySlotManager slotManager;

    private GameType lastKnownGameMode = null;

    // Estado de interacción

    private float xMouse;

    private float yMouse;

    private boolean isMouseDown = false;

    private Button craftingButton;

    private ImageButton gameModeToggleButton;
;
    private static final ResourceLocation GAMEMODE_BUTTON_TEXTURE =
            new ResourceLocation("ebzinventory", "textures/gui/gamemode_button.png");



    /**

     * Constructor principal de la pantalla de inventario

     */

    public RustStyleInventoryScreen(InventoryMenu menu, Inventory inventory, Component title) {

        super(menu, inventory, title);



        // Crear el estado compartido

        this.uiState = new UIState();



        // Inicializar componentes

        this.inventoryRenderer = new InventoryRenderer(this, this.uiState);

        this.craftingRenderer = new CraftingRenderer(this, this.uiState);

        this.queueRenderer = new QueueRenderer(this, this.uiState);

        this.quickCraftPanel = new QuickCraftPanel(this, this.uiState);

        this.slotManager = new InventorySlotManager(this, menu);



        // Ocultar etiquetas estándar

        this.titleLabelX = -999;

        this.titleLabelY = -999;

        this.inventoryLabelX = -999;

        this.inventoryLabelY = -999;



        // Valores iniciales

        this.imageWidth = 240;

        this.imageHeight = 216;

    }



    @Override
    protected void init() {
        super.init();

        // Actualizar dimensiones basadas en la resolución
        UIScaling.updateDimensions(this.getMinecraftInstance(), this.uiState, this.width, this.height);

        // Centrar la pantalla
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        // Crear botón de crafteo centrado
        int buttonWidth = 80;
        craftingButton = Button.builder(Component.literal("Crafteo"), this::toggleCrafting)
                .pos((this.width - buttonWidth) / 2, 10)
                .size(buttonWidth, 20)
                .build();
        this.addRenderableWidget(craftingButton);

        // Inicializar slots
        slotManager.repositionSlots(this.uiState.isShowCrafting(), this.leftPos, this.topPos);

        // Inicializar componentes UI
        craftingRenderer.init();
        quickCraftPanel.init();

        // Añadir botón de inventario creativo si el jugador está en modo creativo
        int creativeButtonSize = 20;
        int rightSideX = this.width - creativeButtonSize - 10;
        int topY = 10;

        if (getMinecraftInstance() != null && getPlayer() != null) {
            // Verificar si el jugador es OP o tiene cheats activados
            boolean isOp = isPlayerOperator();

            // Añadir botón de creativo si el jugador está en modo creativo
            if (isPlayerInCreativeMode()) {
                creativeInventoryButton = new ImageButton(
                        rightSideX, topY,
                        creativeButtonSize, creativeButtonSize,
                        0, 0, 20,
                        CREATIVE_BUTTON_TEXTURE, 32, 32,
                        button -> openCreativeInventory()
                );
                this.addRenderableWidget(creativeInventoryButton);
            }

            // Añadir botón de cambio de gamemode si el jugador es OP
            if (isOp) {
                int gameModeButtonSize = 20;
                int gameModeButtonX = rightSideX;

                // Si el botón de creativo está presente, colocar debajo de él, si no, usar su posición
                int gameModeButtonY = isPlayerInCreativeMode() ?
                        topY + creativeButtonSize + 10 : // 10px debajo del botón creativo
                        topY; // O en la misma posición si no hay botón creativo

                // Determinar la textura según el modo actual
                int textureY = isPlayerInCreativeMode() ? 0 : 20; // 0 para creativo, 20 para survival

                gameModeToggleButton = new ImageButton(
                        gameModeButtonX, gameModeButtonY,
                        gameModeButtonSize, gameModeButtonSize,
                        0, textureY, 20,
                        GAMEMODE_BUTTON_TEXTURE, 32, 64,
                        button -> toggleGameMode()
                );
                this.addRenderableWidget(gameModeToggleButton);
            }
        }

        // Si estábamos en modo creativo anteriormente, volver a abrirlo
        if (isCreativeModeActive()) {
            // Reset para evitar recursión infinita
            setCreativeModeActive(false);

            // Programar la apertura del inventario creativo para el siguiente tick
            // Esto evita problemas de apertura durante la inicialización
            getMinecraftInstance().tell(() -> openCreativeInventory());
        }
    }




    /**

     * Verifica si el jugador está en modo creativo.

     *

     * @return true si el jugador está en modo creativo

     */

    private boolean isPlayerInCreativeMode() {
        if (getMinecraftInstance() == null || getPlayer() == null) return false;

        // En el cliente, podemos verificar el modo de juego actual
        return getMinecraftInstance().gameMode.getPlayerMode() == GameType.CREATIVE;
    }







    private void openCreativeInventory() {
        if (getMinecraftInstance() == null || getPlayer() == null) return;

        // Cerrar el inventario actual
        this.onClose();

        // Guardar el estado para saber que venimos desde nuestro inventario personalizado
        isCreativeModeActive = false; // Desactivamos esto para evitar bucles

        // Crear una clase personalizada que extiende el creativo vanilla para poder agregar nuestro botón
        net.minecraft.client.Minecraft.getInstance().setScreen(new VanillaCreativeWithReturnButton(
                getPlayer(),
                getPlayer().level().enabledFeatures(),
                true
        ));
    }


    private void addReturnButtonToVanillaCreative(net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen screen) {
        try {
            // Intentamos agregar un botón de regreso en el creativo vanilla
            net.minecraft.client.gui.components.Button returnButton =
                    net.minecraft.client.gui.components.Button.builder(
                                    net.minecraft.network.chat.Component.literal("Volver a Inventario"),
                                    button -> returnFromCreative()
                            )
                            .pos(10, 10)
                            .size(120, 20)
                            .build();

            // Usamos reflection para acceder al método addRenderableWidget protegido
            java.lang.reflect.Method addWidgetMethod =
                    net.minecraft.client.gui.screens.Screen.class.getDeclaredMethod(
                            "addRenderableWidget",
                            net.minecraft.client.gui.components.events.GuiEventListener.class
                    );
            addWidgetMethod.setAccessible(true);
            addWidgetMethod.invoke(screen, returnButton);
        } catch (Exception e) {
            System.err.println("Error al agregar botón de retorno: " + e.getMessage());
        }
    }

    /**
     * Método para volver a nuestro inventario personalizado desde el creativo vanilla
     */
    private static void returnFromCreative() {
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();

        // Verificamos que el jugador exista
        if (minecraft.player == null) return;

        // Cerramos el inventario actual
        minecraft.player.closeContainer();

        // Creamos y mostramos nuestro inventario personalizado
        RustStyleInventoryScreen customInventory = new RustStyleInventoryScreen(
                minecraft.player.inventoryMenu,
                minecraft.player.getInventory(),
                net.minecraft.network.chat.Component.literal("Inventario")
        );

        // Establecer la pantalla
        minecraft.setScreen(customInventory);
    }
    public static class VanillaCreativeWithReturnButton extends net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen {

        public VanillaCreativeWithReturnButton(net.minecraft.world.entity.player.Player player,
                                               net.minecraft.world.flag.FeatureFlagSet enabledFeatures,
                                               boolean displayOperatorCreativeTab) {
            super(player, enabledFeatures, displayOperatorCreativeTab);
        }

        @Override
        protected void init() {
            super.init();

            // Agregar nuestro botón de regreso
            net.minecraft.client.gui.components.Button returnButton =
                    net.minecraft.client.gui.components.Button.builder(
                                    net.minecraft.network.chat.Component.literal("Volver a Inventario"),
                                    button -> returnFromCreative()
                            )
                            .pos(10, 10)
                            .size(120, 20)
                            .build();

            this.addRenderableWidget(returnButton);
        }}



    private static boolean isCreativeModeActive = false;





    private void setCreativeModeActive(boolean active) {

        isCreativeModeActive = active;

    }





    private boolean isCreativeModeActive() {

        return isCreativeModeActive;

    }



    /**

     * Alternar entre modo inventario y crafteo

     */

    private void toggleCrafting(Button button) {

        uiState.toggleShowCrafting();



        if (!uiState.isShowCrafting()) {

            // Limpiar al salir del modo crafteo

            resetWidgets();

            uiState.setSelectedRecipe(null);

            uiState.setCurrentPage(0);



            // Actualizar crafteos rápidos cuando volvemos al modo inventario

            quickCraftPanel.refreshAvailableRecipes();

        }



        if (getMinecraftInstance() != null && getPlayer() != null) {

            getPlayer().playSound(

                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),

                    1.0F, 1.0F

            );

        }



        // Reposicionar slots

        slotManager.repositionSlots(uiState.isShowCrafting(), this.leftPos, this.topPos);

    }



    /**

     * Renderizar la pantalla completa

     */

    @Override

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        this.renderBackground(guiGraphics);



        // Renderizado condicional según el modo

        if (uiState.isShowCrafting()) {

            craftingRenderer.render(guiGraphics, mouseX, mouseY, partialTick);

        } else {

            super.render(guiGraphics, mouseX, mouseY, partialTick);



            // Render crafteo rápido (la actualización ahora se maneja dentro de render)

            quickCraftPanel.render(guiGraphics, mouseX, mouseY);

        }



        // Renderizar widgets

        for (Renderable renderable : this.renderables) {

            renderable.render(guiGraphics, mouseX, mouseY, partialTick);

        }



        // Actualizar posición del mouse para tooltips

        this.xMouse = (float)mouseX;

        this.yMouse = (float)mouseY;

        this.renderTooltip(guiGraphics, mouseX, mouseY);

    }



    /**

     * Llamado cada tick para actualizar la interfaz

     */


    @Override
    public void containerTick() {
        super.containerTick();

        // Verificar periódicamente si el modo de juego ha cambiado
        // y actualizar la interfaz en consecuencia
        if (getMinecraftInstance() != null && getPlayer() != null) {
            GameType currentMode = getMinecraftInstance().gameMode.getPlayerMode();

            // Verificar si el modo ha cambiado (usando una variable de clase para tracking)
            if (lastKnownGameMode != currentMode) {
                lastKnownGameMode = currentMode;
                refreshGameModeUI();
            }
        }

        // Actualizar crafteos rápidos
        if (!uiState.isShowCrafting()) {
            // Esta llamada no es necesaria ya que la actualización se hace dentro de render
            // La dejamos comentada como referencia
            // quickCraftPanel.update();
        }
    }



    /**

     * Renderizar el fondo del inventario

     */

    @Override

    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {

        if (getMinecraftInstance() == null || getMinecraftInstance().getWindow() == null) return;

        inventoryRenderer.renderBackground(guiGraphics, partialTick, mouseX, mouseY);

    }



    /**

     * Reiniciar widgets manteniendo el botón de crafteo

     */

    public void resetWidgets() {

        Button savedCraftingButton = this.craftingButton;

        Button savedCreativeButton = this.creativeInventoryButton;

        super.clearWidgets();

        if (savedCraftingButton != null) {

            this.addRenderableWidget(savedCraftingButton);

        }

        if (savedCreativeButton != null && isPlayerInCreativeMode()) {

            this.addRenderableWidget(savedCreativeButton);

        }

    }



    /**

     * Añadir un widget al renderizado

     */

    public void addWidget(AbstractWidget widget) {

        this.addRenderableWidget(widget);

    }



    /**

     * Manejo de clics de ratón

     */

    @Override

    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        this.isMouseDown = true;



        if (uiState.isShowCrafting()) {

            // Delegar a componentes de crafteo

            if (craftingRenderer.handleMouseClicked(mouseX, mouseY, button)) {

                return true;

            }

        } else {

            // Delegar a crafteo rápido

            if (quickCraftPanel.handleMouseClicked(mouseX, mouseY, button)) {

                // Actualizar inmediatamente después de un clic en crafteo rápido

                quickCraftPanel.refreshAvailableRecipes();

                return true;

            }

        }



        return super.mouseClicked(mouseX, mouseY, button);

    }



    /**

     * Manejo de liberación de clic

     */

    @Override

    public boolean mouseReleased(double mouseX, double mouseY, int button) {

        this.isMouseDown = false;

        return slotManager.handleMouseReleased(mouseX, mouseY, button) ||

                super.mouseReleased(mouseX, mouseY, button);

    }



    /**

     * Actualizar dimensiones al cambiar el tamaño de la ventana

     */

    @Override

    public void resize(net.minecraft.client.Minecraft minecraft, int width, int height) {

        super.resize(minecraft, width, height);

        UIScaling.updateDimensions(minecraft, uiState, width, height);

        slotManager.repositionSlots(uiState.isShowCrafting(), this.leftPos, this.topPos);

    }



    /**

     * Se llama cuando se toma un item del inventario

     */

    @Override

    protected void slotClicked(Slot slot, int mouseX, int mouseY, net.minecraft.world.inventory.ClickType clickType) {

        super.slotClicked(slot, mouseX, mouseY, clickType);



        // Actualizar crafteos rápidos después de cualquier interacción con slots

        if (!uiState.isShowCrafting() && slot != null) {

            quickCraftPanel.refreshAvailableRecipes();

        }

    }



    // Métodos de acceso para componentes



    public float getxMouse() {

        return xMouse;

    }



    public float getyMouse() {

        return yMouse;

    }



    public boolean isMouseDown() {

        return isMouseDown;

    }



    public void setMouseDown(boolean mouseDown) {

        isMouseDown = mouseDown;

    }



    public UIState getUiState() {

        return uiState;

    }



    /**

     * Recupera el slot sobre el que está actualmente el mouse.

     *

     * @return El slot bajo el mouse, o null si no hay ninguno

     */

    public Slot getSlotUnderMouse() {

        return this.hoveredSlot;

    }



    /**

     * Obtiene la instancia de Minecraft.

     */

    public Minecraft getMinecraftInstance() {

        return this.minecraft;

    }



    /**

     * Obtiene el jugador actual.

     */

    public Player getPlayer() {

        return this.minecraft != null ? this.minecraft.player : null;

    }



    /**

     * Obtiene el ancho de la pantalla.

     */

    public int getScreenWidth() {

        return this.width;

    }

    /**
     * Verifica si el jugador está en modo creativo.
     *
     * @return true si el jugador está en modo creativo
     */


    /**
     * Verifica si el jugador es operador (OP) en el servidor.
     *
     * @return true si el jugador es OP
     */
    private boolean isPlayerOperator() {
        if (getMinecraftInstance() == null || getPlayer() == null) return false;

        // En singleplayer, podemos verificar el permissionLevel
        if (getMinecraftInstance().hasSingleplayerServer()) {
            return getMinecraftInstance().player.hasPermissions(2);
        }

        // En multiplayer, verificamos por permisos específicos
        // Esto es aproximado ya que el cliente no tiene acceso directo al estado OP
        return getMinecraftInstance().player.hasPermissions(2);
    }

    /**
     * Alternar entre modo de juego Creativo y Supervivencia.
     */
    /**
     * Alternar entre modo de juego Creativo y Supervivencia.
     */
    /**
     * Alternar entre modo de juego Creativo y Supervivencia.
     */
    /**
     * Alternar entre modo de juego Creativo y Supervivencia.
     */
    /**
     * Alternar entre modo de juego Creativo y Supervivencia.
     */
    /**
     * Alternar entre modo de juego Creativo y Supervivencia.
     */
    /**
     * Alternar entre modo de juego Creativo y Supervivencia.
     */
    private void toggleGameMode() {
        if (getMinecraftInstance() == null || getPlayer() == null) return;

        // Determinar el modo a cambiar
        GameType targetMode = isPlayerInCreativeMode() ? GameType.SURVIVAL : GameType.CREATIVE;

        // Enviar comando al servidor a través de la conexión
        String command = "gamemode " + (targetMode == GameType.CREATIVE ? "creative" : "survival");

        // Obtener la conexión al servidor y enviar el comando
        if (getMinecraftInstance().getConnection() != null) {
            getMinecraftInstance().getConnection().sendCommand(command);
        }

        // Reproducir sonido de feedback
        getPlayer().playSound(
                net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),
                1.0F, 1.0F
        );

        // Programar una actualización de la interfaz después de un breve retraso
        // para dar tiempo a que el servidor procese el cambio de modo
        getMinecraftInstance().tell(() -> {
            // Refrescar la interfaz para reflejar el nuevo modo de juego
            refreshGameModeUI();
        });
    }


    /**
     * Llamado cada tick para actualizar la interfaz
     */


    /**
     * Elimina un widget específico de la interfaz.
     *
     * @param widget El widget a eliminar
     */
    protected void removeWidget(AbstractWidget widget) {
        if (widget != null) {
            // Solo usamos métodos públicos y listas accesibles
            this.renderables.remove(widget);
            this.children().remove(widget);

            // Nota: No podemos acceder directamente a narratables porque es privado
            // En su lugar, confiamos en que el garbage collector limpiará esta referencia
        }
    }

    /**
     * Refresca los elementos de la interfaz según el modo de juego actual.
     * Útil cuando se cambia de modo sin cerrar la pantalla.
     */
    public void refreshGameModeUI() {
        if (getMinecraftInstance() == null || getPlayer() == null) return;

        boolean isInCreative = isPlayerInCreativeMode();
        boolean isOp = isPlayerOperator();
        int creativeButtonSize = 20;
        int rightSideX = this.width - creativeButtonSize - 10;
        int topY = 10;

        // Manejar el botón de inventario creativo
        if (isInCreative) {
            // Si estamos en creativo pero no hay botón, añadirlo
            if (creativeInventoryButton == null) {
                creativeInventoryButton = new ImageButton(
                        rightSideX, topY,
                        creativeButtonSize, creativeButtonSize,
                        0, 0, 20,
                        CREATIVE_BUTTON_TEXTURE, 32, 32,
                        button -> openCreativeInventory()
                );
                this.addRenderableWidget(creativeInventoryButton);
            }
        } else {
            // Si no estamos en creativo pero hay botón, quitarlo
            if (creativeInventoryButton != null) {
                this.removeWidget(creativeInventoryButton);
                creativeInventoryButton = null;
            }
        }

        // Manejar el botón de cambio de gamemode
        if (isOp) {
            int gameModeButtonSize = 20;
            int gameModeButtonX = rightSideX;
            int gameModeButtonY = isInCreative ?
                    topY + creativeButtonSize + 10 : // 10px debajo del botón creativo
                    topY; // O en la misma posición si no hay botón creativo

            // Determinar la textura según el modo actual
            int textureY = isInCreative ? 0 : 20; // 0 para creativo, 20 para survival

            // Actualizar la posición y textura del botón si ya existe
            if (gameModeToggleButton != null) {
                gameModeToggleButton.setPosition(gameModeButtonX, gameModeButtonY);
                // No podemos cambiar la textura directamente, así que tenemos que recrear el botón
                this.removeWidget(gameModeToggleButton);
            }

            // Crear/recrear el botón con la textura actualizada
            gameModeToggleButton = new ImageButton(
                    gameModeButtonX, gameModeButtonY,
                    gameModeButtonSize, gameModeButtonSize,
                    0, textureY, 20,
                    GAMEMODE_BUTTON_TEXTURE, 32, 64,
                    button -> toggleGameMode()
            );
            this.addRenderableWidget(gameModeToggleButton);
        } else if (gameModeToggleButton != null) {
            // Si no somos OP pero hay botón, quitarlo
            this.removeWidget(gameModeToggleButton);
            gameModeToggleButton = null;
        }
    }

    /**

     * Obtiene el alto de la pantalla.

     */

    public int getScreenHeight() {

        return this.height;

    }

}