package me.arrayofc.keystrokes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.arrayofc.keystrokes.color.ColorManager;
import me.arrayofc.keystrokes.command.ModCommand;
import me.arrayofc.keystrokes.gui.ColorOptionsConfigScreen;
import me.arrayofc.keystrokes.gui.MainConfigScreen;
import me.arrayofc.keystrokes.hud.HudManager;
import me.arrayofc.keystrokes.hud.HudRenderer;
import me.arrayofc.keystrokes.keystroke.KeystrokeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("keystrokesmod")
public class Keystrokes {

    // The static instance of this class
    private static Keystrokes instance;

    // The Mod logger
    private static final Logger logger = LogManager.getLogger();

    // A static GSON instance for overlay hud serialization
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final ColorManager colorManager;
    private final HudManager hudManager;
    private final HudRenderer hudRenderer;
    private final KeystrokeRegistry keystrokeRegistry;
    private final MainConfigScreen mainConfigScreen;

    private boolean menuOpen = false;

    public Keystrokes() {
        instance = this;

        // Initialize the mod configuration
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, KeystrokesConfig.CLIENT_CONFIG);

        // Assign FML event listener for loading complete event
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onLoadComplete);

        this.colorManager = new ColorManager(this);
        this.hudManager = new HudManager(this);
        this.keystrokeRegistry = new KeystrokeRegistry(this);

        this.mainConfigScreen = new MainConfigScreen(this);
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, screen) -> this.mainConfigScreen);

        this.hudRenderer = new HudRenderer(this);
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, screen) -> this.hudRenderer);

        // Register this class a listener for the event bus
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Listener for when the mod has finished loading.
     * <p>
     * When fired, we initialize the color and keystroke hud with values from the configuration.
     */
    public void onLoadComplete(final FMLLoadCompleteEvent event) {
        this.colorManager.initialize();
        this.hudManager.initialize();
        this.hudRenderer.initialize();
        logger.info("Mod loading complete.");
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (this.menuOpen) {
            Minecraft.getInstance().displayGuiScreen(this.mainConfigScreen);
            this.menuOpen = false;
            return;
        }

        if (this.shouldShowOverlays()) {
            this.hudRenderer.renderScreenOverlays();
        }
    }

    @SubscribeEvent
    public void onCommandsRegister(RegisterCommandsEvent event) {
        // Register the command to display the settings screen
        ModCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onClientChat(ClientChatEvent event) {
        // adding client-sided only commands when the client is connected to multiplayer
        // is a struggle, so this is the work-around for command execution on multiplayer
        if (Minecraft.getInstance().getCurrentServerData() != null) {
            if (event.getMessage().toLowerCase().startsWith("/keystrokes")) {
                // cancel the event so nothing is actually sent
                event.setCanceled(true);
                // open the menu
                this.menuOpen = true;

                // save command to sent messages so user can scroll up
                Minecraft.getInstance().ingameGUI.getChatGUI().addToSentMessages(event.getMessage());
            }
        }
    }

    /**
     * Checks whether or not it is appropriate to show the HUD overlays at the current time.
     */
    public boolean shouldShowOverlays() {
        // don't show if F3 is displayed or there's a loading screen
        if (Minecraft.getInstance().gameSettings.showDebugInfo || Minecraft.getInstance().loadingGui != null) return false;

        // check if the current GameSession isn't null (GameSession as singleplayer or multiplayer) & and if the game isn't paused
        return Minecraft.getInstance().getMinecraftGame().getCurrentSession() != null && !Minecraft.getInstance().isGamePaused();
    }

    /**
     * Sets whether or not the main configuration menu is open.
     */
    public void setMenuOpen(boolean menuOpen) {
        this.menuOpen = menuOpen;
    }

    /**
     * Returns the color manager class, handling the colors for the mod.
     */
    public ColorManager getColorManager() {
        return this.colorManager;
    }

    /**
     * Returns the class that handles rendering the different HUD overlays.
     *
     * It also acts as a settable {@link net.minecraft.client.gui.screen.Screen} for moving around overlays.
     */
    public HudRenderer getHudRenderer() {
        return this.hudRenderer;
    }

    /**
     * Returns the class handling the different HUD overlays.
     */
    public HudManager getHudManager() {
        return this.hudManager;
    }

    /**
     * Returns the class that creates new keystrokes.
     */
    public KeystrokeRegistry getKeystrokeRegistry() {
        return this.keystrokeRegistry;
    }

    /**
     * Checks whether or not the user currently has the color settings menu open.
     */
    public boolean isChangingColors() {
        return Minecraft.getInstance().currentScreen instanceof ColorOptionsConfigScreen;
    }

    /**
     * The static instance of this class.
     *
     * @return The static instance of the main mod class.
     */
    public static Keystrokes getInstance() {
        return instance;
    }
}