package me.arrayofc.keystrokes.hud;

import com.google.common.collect.Lists;
import com.google.gson.stream.JsonReader;
import me.arrayofc.keystrokes.Keystrokes;
import me.arrayofc.keystrokes.keystroke.Keystroke;
import me.arrayofc.keystrokes.util.Strings;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * This class manages all the different overlay HUDs on the screen.
 */
public class HudManager {

    // Main class instance
    private final Keystrokes keystrokes;

    // The list of registered HUDs
    private final List<OverlayHud> overlayHuds = Lists.newArrayList();

    // The directory where the serialized overlay HUDs are stored
    private final Path hudFile;

    public HudManager(Keystrokes keystrokes) {
        this.keystrokes = keystrokes;
        this.hudFile = FMLPaths.CONFIGDIR.get().resolve("keystroke-overlays");
    }

    /**
     * Registers the saved HUD overlays, and registers the default one if necessary.
     */
    public void initialize() {
        // if the directory doesn't exist, we'll need to create it
        if (Files.notExists(this.hudFile)) {
            try {
                Files.createDirectory(this.hudFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // List the files in this directory
        File[] files = this.hudFile.toFile().listFiles();

        // no files are in the dir, which means we'll have to create the first default one
        if (files == null || files.length == 0) {
            this.createDefaultOverlay();
            return;
        }

        // loop the files in the directory & deserialize them
        for (File file : files) {
            try (JsonReader reader = new JsonReader(Files.newBufferedReader(file.toPath()))) {
                OverlayHud overlayHud = Keystrokes.GSON.fromJson(reader, OverlayHud.class);
                this.registerOverlay(overlayHud);

            } catch (IOException e) {
                // something went wrong when deserializing this file, so we'll remove it
                try {
                    Files.deleteIfExists(file.toPath());
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Returns all the registered HUD:s.
     */
    public List<OverlayHud> getOverlayHuds() {
        return this.overlayHuds;
    }

    /**
     * Returns the default Overlay HUD.
     */
    public OverlayHud getDefaultOverlay() {
        return this.overlayHuds.stream().filter(hud -> !hud.isCustom()).findFirst().orElseGet(this::createDefaultOverlay);
    }

    /**
     * Registers an overlay HUD.
     *
     * @param overlayHud The overlay hud to add.
     */
    public void registerOverlay(OverlayHud overlayHud) {
        overlayHud.getAllKeystrokes().forEach(keystroke -> keystroke.setOwningOverlay(overlayHud.getName()));
        this.keystrokes.getHudManager().getOverlayHuds().add(overlayHud);
    }

    /**
     * Registers an overlay HUD.
     *
     * @param rows        The rows for this HUD.
     * @param hudPosition The location for this HUD.
     */
    public OverlayHud registerOverlay(String name, EnumMap<OverlayHud.Section, List<Keystroke.Row>> rows, HudPosition hudPosition, boolean custom) {
        OverlayHud overlayHud = new OverlayHud(name, rows, hudPosition, custom);
        overlayHud.getAllKeystrokes().forEach(keystroke -> keystroke.setOwningOverlay(overlayHud.getName()));

        this.keystrokes.getHudManager().getOverlayHuds().add(overlayHud);

        return overlayHud;
    }


    /**
     * Deletes an overlay HUD from the mod.
     *
     * @param overlayHud HUD to delete.
     */
    public void deleteOverlay(OverlayHud overlayHud) {
        this.overlayHuds.remove(overlayHud);

        Path path = this.hudFile.resolve(overlayHud.getName() + ".json");
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves a HUD overlay to the hud directory.
     *
     * @param overlayHud The hud overlay to save.
     */
    public void saveOverlay(OverlayHud overlayHud) {
        Path path = this.hudFile.resolve(overlayHud.getName() + ".json");
        String json = Keystrokes.GSON.toJson(overlayHud, OverlayHud.class);

        try {
            Files.write(path, json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();

            // alert the user that the overlay wasn't saved
            SystemToast.addOrUpdate(Minecraft.getInstance().getToastGui(), SystemToast.Type.TUTORIAL_HINT, new StringTextComponent("Warning"),
                    new StringTextComponent("Couldn't save overlay."));
        }
    }

    /**
     * Attempts to find a {@link OverlayHud} at a clicked position on the screen.
     *
     * @param x X position
     * @param y Y position
     * @return The clicked overlay hud, null if none.
     */
    @Nullable
    public OverlayHud getClickedOverlay(double x, double y) {
        return this.overlayHuds.stream().filter(overlayHud -> this.isInsideOverlay(overlayHud.getHudPosition(), x, y))
                .findFirst().orElse(null);
    }

    /**
     * Attempts to find a suitable location for a new HUD overlay to appear at.
     *
     * @param from The location to start looking from.
     * @return Returns the suitable location, null if no location were suitable.
     */
    @Nullable
    public HudPosition getSuitableLocation(HudPosition from) {
        HudPosition hudPosition = from.clone();

        double currentX = hudPosition.getX(), currentY = hudPosition.getY();

        while (this.isOccupied(currentX, currentY)) {
            currentX += 10;

            // x went as far as it could, now we'll go down 20px and attempt to find a place on this line
            if (currentX >= this.keystrokes.getHudRenderer().getScreenDimensions()[0]) {
                currentY += 20;
                currentX = 5;
            }

            // if no suitable location was found then we'll return null
            if (currentY >= this.keystrokes.getHudRenderer().getScreenDimensions()[1]) {
                return null;
            }
        }

        hudPosition.setX((int) currentX);
        hudPosition.setY((int) currentY);

        return hudPosition;
    }

    /**
     * Checks if a X- and Y position is occupied by an overlay on the screen.
     *
     * @param x X position
     * @param y Y posistion
     * @return True if occupied, false otherwise.
     */
    public boolean isOccupied(double x, double y) {
        return this.overlayHuds.stream().anyMatch(overlayHud -> this.isInsideOverlay(overlayHud.getHudPosition(), x, y));
    }

    /**
     * Checks whether or not a position is inside of the HUD area.
     *
     * @param clickX X-position
     * @param clickY Y-position
     * @return True if inside, false otherwise.
     */
    public boolean isInsideOverlay(HudPosition hudPosition, double clickX, double clickY) {
        int width = hudPosition.getWidth();
        int height = hudPosition.getHeight();
        if ((width | height) < 0) return false;

        int x = hudPosition.getX();
        int y = hudPosition.getY();
        if (clickX < x || clickY < y) return false;

        width += x;
        height += y;
        return ((width < x || width > clickX) && (height < y || height > clickY));
    }

    /**
     * Checks whether or not a keybind already exists in a HUD overlay.
     *
     * @param keyBinding Keybinding to check for.
     * @return True if a HUD overlay is already including this keybind.
     */
    public boolean isKeybindBusy(KeyBinding keyBinding) {
        // loop overlay huds
        for (OverlayHud overlayHud : this.overlayHuds) {
            // list of rows
            for (Map.Entry<OverlayHud.Section, List<Keystroke.Row>> entry : overlayHud.getRowMap().entrySet()) {
                // rows
                for (Keystroke.Row row : entry.getValue()) {
                    // keystrokes on row
                    for (Keystroke keystroke : row.getKeystrokes()) {
                        if (keystroke.isBarrier()) continue;
                        if (keystroke.getKeyBinding() != null && keystroke.getKeyBinding() == keyBinding) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Creates and registers the default Overlay HUD.
     */
    public OverlayHud createDefaultOverlay() {
        GameSettings settings = Minecraft.getInstance().gameSettings;
        final Keystroke barrier = new Keystroke.BarrierKeystroke();

        final EnumMap<OverlayHud.Section, List<Keystroke.Row>> rows = new HudBuilder()
                .section(OverlayHud.Section.KEY)
                    .row(Arrays.asList(
                        barrier,
                        Keystroke.newKeystroke(settings.keyBindForward, Keystroke.KeyType.KEY, true),
                        barrier))
                    .row(Arrays.asList(
                        Keystroke.newKeystroke(settings.keyBindLeft, Keystroke.KeyType.KEY, true),
                        Keystroke.newKeystroke(settings.keyBindBack, Keystroke.KeyType.KEY, true),
                        Keystroke.newKeystroke(settings.keyBindRight, Keystroke.KeyType.KEY, true)))
                    .buildSection()

                .section(OverlayHud.Section.MOUSE)
                    .row(Arrays.asList(
                        Keystroke.newKeystroke(settings.keyBindAttack, Keystroke.KeyType.MOUSE_LEFT, true),
                        Keystroke.newKeystroke(settings.keyBindUseItem, Keystroke.KeyType.MOUSE_RIGHT, true)))
                    .buildSection()

                .section(OverlayHud.Section.SPACEBAR)
                    .row(Collections.singletonList(
                        Keystroke.newKeystroke(settings.keyBindJump, Keystroke.KeyType.SPACEBAR, true)))
                    .buildSection()

                .build();

        return this.registerOverlay("default", rows, new HudPosition(), false);
    }

    /**
     * Creates a new overlay HUD with a single keystroke from a pressed {@link KeyBinding}.
     *
     * @param keyBinding Keybinding to create overlay hud for.
     */
    public void createOverlayFromKeybind(KeyBinding keyBinding) {
        Keystroke keystroke;

        // build the row map
        final EnumMap<OverlayHud.Section, List<Keystroke.Row>> rows = new HudBuilder()
                .section(OverlayHud.Section.KEY)
                    .row(Collections.singletonList(
                        keystroke = Keystroke.newKeystroke(keyBinding, Keystroke.KeyType.KEY, false)))
                    .buildSection()
                .build();

        // find a suitable location for this HUD to appear on the screen at
        final HudPosition hudPosition = this.keystrokes.getHudManager().getSuitableLocation(
                new HudPosition(5, 5, (int) keystroke.getWidth(), (int) keystroke.getHeight()));

        // no location was found, so we'll just return before registering the hud & alert the user
        if (hudPosition == null) {
            SystemToast.addOrUpdate(Minecraft.getInstance().getToastGui(), SystemToast.Type.TUTORIAL_HINT,
                    new StringTextComponent("Notice"), new StringTextComponent("No space for new HUD."));
            return;
        }

        this.registerOverlay(Strings.getKeyName(keyBinding), rows, hudPosition, true);
    }
}