package me.arrayofc.keystrokes.color;

import com.mojang.blaze3d.systems.RenderSystem;
import me.arrayofc.keystrokes.Keystrokes;
import me.arrayofc.keystrokes.KeystrokesConfig;
import me.arrayofc.keystrokes.gui.MainConfigScreen;
import me.arrayofc.keystrokes.hud.OverlayHud;

import java.awt.*;
import java.util.List;

/**
 * This class handles the colors in this mod.
 */
public class ColorManager {

    // The instance of the main class
    private final Keystrokes keystrokes;

    public ColorManager(Keystrokes keystrokes) {
        this.keystrokes = keystrokes;
    }

    /**
     * Sets the current RGB values the specified tab in an overlay hud.
     *
     * @param hud      Overlay HUD to set colors for.
     * @param colorTab Tab to set for.
     * @param values   Values to set.
     */
    public void set(OverlayHud hud, ColorTab colorTab, List<Integer> values) {
        if (!hud.getRgbValues().containsKey(colorTab)) {
            hud.getRgbValues().put(colorTab, new int[]{0, 0, 0});
        }

        hud.getRgbValues().get(colorTab)[0] = values.get(0);
        hud.getRgbValues().get(colorTab)[1] = values.get(1);
        hud.getRgbValues().get(colorTab)[2] = values.get(2);
    }

    /**
     * Set a R, G or B value for the specified tab in an overlay hud.
     *
     * @param hud      Overlay HUD to set color for.
     * @param colorTab Tab to set for.
     * @param type     Color type (r, g or b represented by integer)
     * @param value    Color value.
     */
    public void set(OverlayHud hud, ColorTab colorTab, int type, int value) {
        if (!hud.getRgbValues().containsKey(colorTab)) {
            hud.getRgbValues().put(colorTab, new int[]{0, 0, 0});
        }

        hud.getRgbValues().get(colorTab)[type] = value;
    }

    /**
     * Creates a {@link Color} object from the current r, g and b values.
     *
     * @return The created color.
     */
    public Color createColor(OverlayHud hud, ColorTab colorTab) {
        return new Color(hud.getRgbValues().get(colorTab)[0], hud.getRgbValues().get(colorTab)[1], hud.getRgbValues().get(colorTab)[2]);
    }

    /**
     * Returns the currently selected RGB values.
     */
    public int[] getRgbValues(OverlayHud hud, ColorTab colorTab) {
        return hud.getRgbValues().get(colorTab);
    }

    /**
     * Converts the RGB value to a hexadecimal color value.
     */
    public String getAsHexadecimal(OverlayHud hud, ColorTab colorTab) {
        int[] colors = this.getRgbValues(hud, colorTab);
        return String.format("#%02x%02x%02x", colors[0], colors[1], colors[2]);
    }

    /**
     * Returns the text color.
     */
    public int getHudTextColor(OverlayHud hud, double offset) {
        boolean syncColors = hud.isCustom() && KeystrokesConfig.SYNC_COLORS.get();

        // Check if the user is trying out colors right now, if so, put the temporary colors
        if (this.keystrokes.isChangingColors()) {
            // if they are previewing the click color we'll need to display that on the HUD
            if (MainConfigScreen.currentColorOptionsScreen.currentTab == ColorTab.CLICK) {
                return this.createColor(syncColors ? this.keystrokes.getHudManager().getDefaultOverlay() : hud, ColorTab.CLICK).getRGB();

                // if current tab is HUD & rainbow mode is on, we still show the rainbow color
            } else if (MainConfigScreen.currentColorOptionsScreen.currentTab == ColorTab.HUD && KeystrokesConfig.RAINBOW.get()) {
                return this.getRainbowColor(offset);
            }

            return this.createColor(syncColors ? this.keystrokes.getHudManager().getDefaultOverlay() : hud, ColorTab.TEXT).getRGB();

        } else {
            // if user isn't trying out colors
            if (KeystrokesConfig.RAINBOW.get()) {
                // return the rainbow color offset if rainbow mode is on
                return this.getRainbowColor(offset);

            } else {
                // else we'll just return the selected rgb color
                return this.createColor(syncColors ? this.keystrokes.getHudManager().getDefaultOverlay() : hud, ColorTab.TEXT).getRGB();
            }
        }
    }

    /**
     * Returns the color to display for when a button is pressed.
     */
    public int getHudClickColor(OverlayHud hud, double offset) {
        if (hud.isCustom() && KeystrokesConfig.SYNC_COLORS.get()) {
            return this.getHudClickColor(this.keystrokes.getHudManager().getDefaultOverlay(), offset);
        }

        if (KeystrokesConfig.RAINBOW.get()) {
            return this.getRainbowColor(offset);
        } else {
            return this.createColor(hud, ColorTab.CLICK).getRGB();
        }
    }

    /**
     * Returns the color to display for when a button is pressed.
     */
    public int getHudBackgroundColor(OverlayHud hud) {
        if (hud.isCustom() && KeystrokesConfig.SYNC_COLORS.get()) {
            return this.createColor(this.keystrokes.getHudManager().getDefaultOverlay(), ColorTab.HUD).getRGB();
        }

        return this.createColor(hud, ColorTab.HUD).getRGB();
    }

    /**
     * Creates a RGB from a generated hue creating a waving rainbow effect.
     */
    public int getRainbowColor(double offset) {
        float hue = (float) (System.currentTimeMillis() % 1000 / 1000.0);
        hue += (this.keystrokes.getHudRenderer().rainbowXOffset + offset / this.keystrokes.getHudManager().getOverlayHuds().get(0).getHudPosition().getWidth()) * 0.4;
        return Color.HSBtoRGB(hue, 1.0f, 1.0f);
    }

    /**
     * Assigns a color RGB value to the render system.
     *
     * @param color Color RGB value.
     * @param alpha The alpha value.
     */
    public static void glColor(int color, float alpha) {
        // if no valid alpha value is given, we'll generate it
        if (alpha <= -1f || alpha > 1f) alpha = (color >> 24 & 0xFF) / 255.0f;

        float red = (color >> 16 & 0xFF) / 255.0f;
        float green = (color >> 8 & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;

        RenderSystem.color4f(red, green, blue, alpha);
    }
}