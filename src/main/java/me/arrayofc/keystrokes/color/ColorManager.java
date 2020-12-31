package me.arrayofc.keystrokes.color;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import me.arrayofc.keystrokes.Keystrokes;
import me.arrayofc.keystrokes.KeystrokesConfig;
import me.arrayofc.keystrokes.gui.MainConfigScreen;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class handles the colors in this mod.
 */
public class ColorManager {

    // The instance of the main class
    private final Keystrokes keystrokes;

    // Holds the colors for every color tab during runtime
    private final Map<ColorTab, int[]> rgbValues = Maps.newHashMapWithExpectedSize(ColorTab.values().length);

    public ColorManager(Keystrokes keystrokes) {
        this.keystrokes = keystrokes;
    }

    /**
     * Initialize this class with the configuration values.
     */
    public void initialize() {
        this.set(ColorTab.TEXT, KeystrokesConfig.TEXT_RGB.get());
        this.set(ColorTab.CLICK, KeystrokesConfig.CLICK_RGB.get());
        this.set(ColorTab.HUD, KeystrokesConfig.HUD_RGB.get());
    }

    /**
     * Sets the current RGB values the specified tab.
     *
     * @param colorTab Tab to set for.
     * @param values   Values to set.
     */
    public void set(ColorTab colorTab, List<Integer> values) {
        if (!this.rgbValues.containsKey(colorTab)) {
            this.rgbValues.put(colorTab, new int[]{0, 0, 0});
        }

        this.rgbValues.get(colorTab)[0] = values.get(0);
        this.rgbValues.get(colorTab)[1] = values.get(1);
        this.rgbValues.get(colorTab)[2] = values.get(2);
    }

    /**
     * Sets the current RGB values for the specified tab from the configuration values.
     *
     * @param colorTab Tab to set for.
     * @param values   Values to set.
     */
    private void set(ColorTab colorTab, String values) {
        if (!this.rgbValues.containsKey(colorTab)) {
            this.rgbValues.put(colorTab, new int[]{0, 0, 0});
        }

        try {
            // R/G & B values in config are separated with the ":" delimiter
            String[] colors = values.split(":");
            this.rgbValues.get(colorTab)[0] = Integer.parseInt(colors[0]);
            this.rgbValues.get(colorTab)[1] = Integer.parseInt(colors[1]);
            this.rgbValues.get(colorTab)[2] = Integer.parseInt(colors[2]);

        } catch (PatternSyntaxException e) {
            // If something is messed up in the config we'll have to assign the colors a fallback value
            IntStream.range(0, 2).forEach(value -> this.rgbValues.get(colorTab)[value] = 0);
        }
    }

    /**
     * Set a R, G or B value for the specified tab.
     *
     * @param colorTab Tab to set for.
     * @param type     Color type (r, g or b represented by integer)
     * @param value    Color value.
     */
    public void set(ColorTab colorTab, int type, int value) {
        if (!this.rgbValues.containsKey(colorTab)) {
            this.rgbValues.put(colorTab, new int[]{0, 0, 0});
        }

        this.rgbValues.get(colorTab)[type] = value;
    }

    /**
     * Creates a {@link Color} object from the current r, g and b values.
     *
     * @return The created color.
     */
    public Color createColor(ColorTab colorTab) {
        return new Color(this.rgbValues.get(colorTab)[0], this.rgbValues.get(colorTab)[1], this.rgbValues.get(colorTab)[2]);
    }

    /**
     * Returns the currently selected RGB values.
     */
    public int[] getRgbValues(ColorTab colorTab) {
        return this.rgbValues.get(colorTab);
    }

    /**
     * Converts the RGB value to a hexadecimal color value.
     */
    public String getAsHexadecimal(ColorTab colorTab) {
        int[] colors = this.getRgbValues(colorTab);
        return String.format("#%02x%02x%02x", colors[0], colors[1], colors[2]);
    }

    /**
     * Updates the {@link KeystrokesConfig} with the color values.
     */
    public void updateConfig() {
        this.rgbValues.forEach((key, value) -> {
            String rgb = Arrays.stream(value).boxed().map(Object::toString).collect(Collectors.joining(":"));
            if (key == ColorTab.TEXT) {
                KeystrokesConfig.TEXT_RGB.set(rgb);

            } else if (key == ColorTab.CLICK) {
                KeystrokesConfig.CLICK_RGB.set(rgb);

            } else {
                KeystrokesConfig.HUD_RGB.set(rgb);
            }
        });
        KeystrokesConfig.CLIENT_CONFIG.save();
    }

    /**
     * Returns the text color.
     */
    public int getHudTextColor(double offset) {
        // Check if the user is trying out colors right now, if so, put the temporary colors
        if (this.keystrokes.isChangingColors()) {
            // if they are previewing the click color we'll need to display that on the HUD
            if (MainConfigScreen.currentColorOptionsScreen.currentTab == ColorTab.CLICK) {
                return this.createColor(ColorTab.CLICK).getRGB();

                // if current tab is HUD & rainbow mode is on, we still show the rainbow color
            } else if (MainConfigScreen.currentColorOptionsScreen.currentTab == ColorTab.HUD && KeystrokesConfig.RAINBOW.get()) {
                return this.getRainbowColor(offset);
            }

            return this.createColor(ColorTab.TEXT).getRGB();

        } else {
            // if user isn't trying out colors
            if (KeystrokesConfig.RAINBOW.get()) {
                // return the rainbow color offset if rainbow mode is on
                return this.getRainbowColor(offset);

            } else {
                // else we'll just return the selected rgb color
                return this.createColor(ColorTab.TEXT).getRGB();
            }
        }
    }

    /**
     * Returns the color to display for when a button is pressed.
     */
    public int getHudClickColor(double offset) {
        if (KeystrokesConfig.RAINBOW.get()) {
            return this.getRainbowColor(offset);
        } else {
            return this.createColor(ColorTab.CLICK).getRGB();
        }
    }

    /**
     * Returns the color to display for when a button is pressed.
     */
    public int getHudBackgroundColor() {
        return this.createColor(ColorTab.HUD).getRGB();
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