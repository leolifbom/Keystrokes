package me.arrayofc.keystrokes.keystroke;

import com.google.common.collect.Maps;
import me.arrayofc.keystrokes.Keystrokes;
import me.arrayofc.keystrokes.hud.HudPosition;
import me.arrayofc.keystrokes.util.MouseHandler;
import me.arrayofc.keystrokes.util.Strings;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This class handles the keyboard and mouse inputs.
 */
public class KeystrokeRegistry {

    private final Keystrokes keystrokes;
    private final GameSettings settings;

    // Represents a keystroke that won't be rendered but its width and height offset will be counted
    private final Keystroke barrier = new Keystroke.BarrierKeystroke();

    // The two click handlers for the mouse buttons
    private final MouseHandler right = new MouseHandler(), left = new MouseHandler();

    public KeystrokeRegistry(Keystrokes keystrokes) {
        this.keystrokes = keystrokes;
        this.settings = Minecraft.getInstance().gameSettings;

        // Register the class as a listener for the event we are listening to in this class
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Registers the default keystrokes and registers the default overlay HUD.
     */
    public void initializeDefault() {
        LinkedHashMap<Keystroke.Row.RowType, List<Keystroke.Row>> rows = new Builder()
                .newSection(Keystroke.Row.RowType.KEY)
                    .addRowWithKeystrokes(Arrays.asList(
                            this.barrier,
                            new Keystroke(this.settings.keyBindForward, Keystroke.KeyType.KEY, true),
                            this.barrier))
                    .addRowWithKeystrokes(Arrays.asList(
                            new Keystroke(this.settings.keyBindLeft, Keystroke.KeyType.KEY, true),
                            new Keystroke(this.settings.keyBindBack, Keystroke.KeyType.KEY, true),
                            new Keystroke(this.settings.keyBindRight, Keystroke.KeyType.KEY, true)))
                .newSection(Keystroke.Row.RowType.MOUSE)
                    .addRowWithKeystrokes(Arrays.asList(
                            new Keystroke(this.settings.keyBindAttack, Keystroke.KeyType.MOUSE_LEFT, true),
                            new Keystroke(this.settings.keyBindUseItem, Keystroke.KeyType.MOUSE_RIGHT, true)))
                .newSection(Keystroke.Row.RowType.SPACEBAR)
                    .addRowWithKeystrokes(Collections.singletonList(
                            new Keystroke(this.settings.keyBindJump, Keystroke.KeyType.SPACEBAR, true)))
                .build();

        this.keystrokes.getHudManager().registerOverlay("default", rows, new HudPosition(), false);
    }

    /**
     * Creates a new keystroke and overlay hud for a pressed {@link KeyBinding}.
     *
     * @param keyBinding  Keybinding to create keystroke overlay hud for.
     */
    public void fromKey(KeyBinding keyBinding) {
        Keystroke keystroke;

        // build the row map
        LinkedHashMap<Keystroke.Row.RowType, List<Keystroke.Row>> rows = new Builder()
                .newSection(Keystroke.Row.RowType.KEY)
                    .addRowWithKeystrokes(Collections.singletonList(
                            keystroke = new Keystroke(keyBinding, Keystroke.KeyType.KEY, false)))
                .build();

        // find a suitable location for this HUD to appear on the screen at
        final HudPosition hudPosition = this.keystrokes.getHudManager().getSuitableLocation(
                new HudPosition(5, 5, (int) keystroke.getWidth(), (int) keystroke.getHeight()));

        // no location was found, so we'll just return before registering the hud
        if (hudPosition == null) return;

        this.keystrokes.getHudManager().registerOverlay(Strings.getKeyName(keyBinding), rows, hudPosition, true);
    }

    /**
     * Returns the CPS from the left or right mouse button.
     *
     * @param left True if left button, false for right mouse button.
     */
    public int getCPS(boolean left) {
        return left ? this.left.getCPS() : this.right.getCPS();
    }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        // we only want to update the cps with this event when a key has been pressed
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        // ...and we don't want to count cps when a screen is open
        if (Minecraft.getInstance().currentScreen != null) return;

        if (this.keystrokes.shouldShowOverlays()) {
            switch (event.getButton()) {
                case 0:
                    this.left.clicked();
                    return;
                case 1:
                    this.right.clicked();
            }
        }
    }

    /**
     * A private class for building HUD components.
     */
    public static class Builder {
        private final LinkedHashMap<Keystroke.Row.RowType, List<Keystroke.Row>> rows = Maps.newLinkedHashMap();
        private Keystroke.Row.RowType row;

        public Builder() {
            for (Keystroke.Row.RowType type : Keystroke.Row.RowType.values()) {
                this.rows.put(type, new ArrayList<>());
            }
        }

        /**
         * Puts a new section to the builder.
         */
        public Builder newSection(Keystroke.Row.RowType type) {
            this.rows.put(type, new ArrayList<>());
            this.row = type;
            return this;
        }

        /**
         * Adds the list of keystrokes as a new row in the previously section.
         */
        public Builder addRowWithKeystrokes(List<Keystroke> keystrokes) {
            this.rows.get(this.row).add(new Keystroke.Row(keystrokes.toArray(new Keystroke[]{})));
            return this;
        }

        /**
         * Returns the built map.
         */
        public LinkedHashMap<Keystroke.Row.RowType, List<Keystroke.Row>> build() {
            return this.rows;
        }
    }
}