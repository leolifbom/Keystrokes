package me.arrayofc.keystrokes.hud;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.arrayofc.keystrokes.color.ColorTab;
import me.arrayofc.keystrokes.keystroke.Keystroke;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * An overlay hud can be placed on the screen at a set location.
 * <p>
 * It can be moved around, and multiple overlays may be placed on the screen with different keybindings.
 */
public class OverlayHud {

    // The name for this overlay
    private final String name;

    // A map containing the rows for the HUD
    private final EnumMap<OverlayHud.Section, List<Keystroke.Row>> rowMap;

    // The colors for this overlay hud
    private final EnumMap<ColorTab, int[]> rgbValues;

    // Whether or not we're currently dragging the HUD
    private boolean dragMode = false;

    // The current location of the HUD on the screen
    public HudPosition hudPosition;

    // The scaling for this overlay hud
    private double scale;

    // The last appropriate X and Y position this HUD was located at
    private int lastX, lastY;

    // Whether or not the user created this themselves or it's a default overlay
    private final boolean custom;

    public OverlayHud(String name, EnumMap<OverlayHud.Section, List<Keystroke.Row>> rowMap, HudPosition hudPosition, boolean custom) {
        this.name = name;
        this.rowMap = rowMap;

        this.rgbValues = Maps.newEnumMap(ColorTab.class);
        this.rgbValues.put(ColorTab.TEXT, new int[]{255, 255, 255});
        this.rgbValues.put(ColorTab.CLICK, new int[]{71, 71, 71});
        this.rgbValues.put(ColorTab.HUD, new int[]{48, 48, 48});

        this.hudPosition = hudPosition;
        this.scale = 1;

        this.lastX = hudPosition.getX();
        this.lastY = hudPosition.getY();

        this.custom = custom;
    }

    /**
     * Returns the name for this overlay.
     */
    public String getName() {
        return name;
    }

    /**
     * Whether or not this overlay is in drag mode.
     */
    public boolean isDragMode() {
        return this.dragMode;
    }

    /**
     * Sets whether or not this overlay is in drag mode.
     */
    public void setDragMode(boolean dragMode) {
        this.dragMode = dragMode;
    }

    /**
     * Returns the location for this HUD on the screen.
     */
    public HudPosition getHudPosition() {
        return this.hudPosition;
    }

    /**
     * Returns the map with the keystrokes.
     */
    public EnumMap<OverlayHud.Section, List<Keystroke.Row>> getRowMap() {
        return this.rowMap;
    }

    /**
     * Whether or not his overlay hud is made by the user.
     */
    public boolean isCustom() {
        return this.custom;
    }

    /**
     * Set the last valid X coordinate for this overlay in the screen.
     */
    public void setLastX(int lastX) {
        this.lastX = lastX;
    }

    /**
     * Set the last valid Y coordinate for this overlay in the screen.
     */
    public void setLastY(int lastY) {
        this.lastY = lastY;
    }

    /**
     * Get the last valid X coordinate for this overlay in the screen.
     */
    public int getLastX() {
        return this.lastX;
    }

    /**
     * Get the last valid Y coordinate for this overlay in the screen.
     */
    public int getLastY() {
        return this.lastY;
    }

    /**
     * Returns the R, G & B values for this overlay hud.
     */
    public EnumMap<ColorTab, int[]> getRgbValues() {
        return this.rgbValues;
    }

    /**
     * Returns the current scaling of this overlay hud.
     */
    public double getScale() {
        return this.scale;
    }

    /**
     * Sets the scaling for this overlay hud.
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * Returns all the registered keystrokes for this overlay hud.
     */
    public List<Keystroke> getAllKeystrokes() {
        List<Keystroke> keystrokes = Lists.newArrayList();

        for (Map.Entry<Section, List<Keystroke.Row>> entry : this.rowMap.entrySet()) {
            for (Keystroke.Row row : entry.getValue()) {
                keystrokes.addAll(row.getKeystrokes());
            }
        }

        return keystrokes;
    }

    /**
     * Represents a section in an {@link OverlayHud}.
     */
    public enum Section {
        KEY, MOUSE, SPACEBAR
    }
}