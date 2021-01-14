package me.arrayofc.keystrokes.hud;

/**
 * This class holds information and whereabouts of the different HUD overlays.
 */
public class HudPosition {

    // The X- and Y position of the HUD.
    private int x, y;

    // The current width and height of the HUD.
    private int width, height;

    public HudPosition(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public HudPosition() {
        // Height is 1 until rendered as the user can disable
        // certain components of the HUD, giving different height
        this(5, 5, 75, 1);
    }

    /**
     * Returns the current X-position of the HUD.
     */
    public int getX() {
        return this.x;
    }

    /**
     * Returns the current Y-position of the HUD.
     */
    public int getY() {
        return this.y;
    }

    /**
     * Returns the width for the HUD.
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Returns the height for the HUD.
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Sets the X-position of the HUD.
     */
    public void setX(int xPos) {
        this.x = xPos;
    }

    /**
     * Sets the Y-position of the HUD.
     */
    public void setY(int yPos) {
        this.y = yPos;
    }

    /**
     * Sets the width of the HUD.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Sets the height of the HUD.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Move the location of the HUD to a new position.
     *
     * @param x The X position.
     * @param y The Y position.
     */
    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Clones this {@link HudPosition}.
     */
    public HudPosition clone() {
        return new HudPosition(this.x, this.y, this.width, this.height);
    }
}