package me.arrayofc.keystrokes.hud;

import me.arrayofc.keystrokes.keystroke.Keystroke;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * An overlay hud can be placed on the screen at a set location.
 * <p>
 * It can be moved around, and multiple overlays may be placed on the screen with different keybindings.
 */
public class OverlayHud {

    // The name for this overlay
    private final String name;

    // A map containing the rows for the HUD
    private final LinkedHashMap<Keystroke.Row.RowType, List<Keystroke.Row>> rowMap;

    // Whether or not we're currently dragging the HUD
    private boolean dragMode = false;

    // The current location of the HUD on the screen
    public HudPosition hudPosition;

    // The last appropriate X and Y position this HUD was located at
    private int lastX, lastY;

    // Whether or not the user created this themselves or it's a default overlay
    private final boolean custom;

    public OverlayHud(String name, LinkedHashMap<Keystroke.Row.RowType, List<Keystroke.Row>> rowMap, HudPosition hudPosition, boolean custom) {
        this.name = name;
        this.rowMap = rowMap;
        this.hudPosition = hudPosition;
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
    public LinkedHashMap<Keystroke.Row.RowType, List<Keystroke.Row>> getRowMap() {
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
}