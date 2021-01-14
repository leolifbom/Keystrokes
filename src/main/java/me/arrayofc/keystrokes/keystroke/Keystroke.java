package me.arrayofc.keystrokes.keystroke;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import me.arrayofc.keystrokes.Keystrokes;
import me.arrayofc.keystrokes.KeystrokesConfig;
import me.arrayofc.keystrokes.color.ColorManager;
import me.arrayofc.keystrokes.color.ColorTab;
import me.arrayofc.keystrokes.gui.MainConfigScreen;
import me.arrayofc.keystrokes.hud.OverlayHud;
import me.arrayofc.keystrokes.util.Strings;
import me.arrayofc.keystrokes.util.Translations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.settings.KeyBinding;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.Map;

@SuppressWarnings("deprecation")
public class Keystroke {

    // The default scales for the default overlay HUD
    public static final Map<KeyType, Pair<Double, Double>> DEFAULT_KEY_SCALE = Maps.newHashMap();

    // The color manager class handling the colors to display
    private final static ColorManager colorManager = Keystrokes.getInstance().getColorManager();

    static {
        DEFAULT_KEY_SCALE.put(KeyType.MOUSE_LEFT, Pair.of(27d, 38d));
        DEFAULT_KEY_SCALE.put(KeyType.MOUSE_RIGHT, Pair.of(27d, 38d));
        DEFAULT_KEY_SCALE.put(KeyType.SPACEBAR, Pair.of(15d, 78d));
        DEFAULT_KEY_SCALE.put(KeyType.KEY, Pair.of(25d, 25d));
    }

    // The keybinding of this keystroke object
    private final String keyBindingDescription;

    // The type of this keystroke
    private final KeyType keyType;

    // The height and width of the keystroke
    private double height, width;

    // The current width of the text content to display
    private double textWidth;

    // Whether or not this keystroke is apart of a default overlay HUD
    private final boolean isDefault;

    // The overlay that this keystroke belongs to
    private String owningOverlay;

    public Keystroke(KeyBinding keyBinding, KeyType type, boolean isDefault) {
        this.keyBindingDescription = keyBinding == null ? "BARRIER" : keyBinding.getKeyDescription();
        this.keyType = type;
        this.height = DEFAULT_KEY_SCALE.get(this.keyType).getLeft();

        this.isDefault = isDefault;

        if (this.isDefault || this.getTextContent().length() <= 3) {
            this.width = DEFAULT_KEY_SCALE.get(this.keyType).getRight();

        } else {
            // if it's not a default key (WASD), the text for this keystroke could be much longer, e.g. "Left Shift".
            this.textWidth = Minecraft.getInstance().fontRenderer.getStringWidth(this.getTextContent());
            this.width = this.textWidth + 5;
        }
    }

    /**
     * Get the overlay to which this keystroke is registered to.
     */
    @Nullable
    public OverlayHud getOwningOverlay() {
        return Keystrokes.getInstance().getHudManager().getOverlayHuds().stream().filter(hud -> hud.getName().equals(this.owningOverlay))
                .findFirst().orElse(null);
    }

    /**
     * Sets the owning overlay for this keystroke.
     */
    public void setOwningOverlay(String owningOverlay) {
        this.owningOverlay = owningOverlay;
    }

    /**
     * Returns the scale of the overlay this keystroke is registered to.
     */
    public double getScale() {
        return this.getOwningOverlay() == null ? 1 : this.getOwningOverlay().getScale();
    }

    /**
     * Returns the {@link KeyBinding} for this keystroke.
     *
     * @return The keybinding for this keystroke, null if not applicable.
     */
    @Nullable
    public KeyBinding getKeyBinding() {
        if (this.keyBindingDescription.equals("BARRIER")) return null;
        return Arrays.stream(Minecraft.getInstance().gameSettings.keyBindings)
                .filter(key -> key.getKeyDescription().equals(this.keyBindingDescription)).findFirst().orElse(null);
    }

    /**
     * Checks if the keybinding is currently pressed down.
     */
    public boolean isPressed() {
        if (this.getKeyBinding() == null) return false;
        return this.getKeyBinding().isKeyDown();
    }

    /**
     * Checks whether or not this keystroke acts as a barrier.
     */
    public boolean isBarrier() {
        return this.keyBindingDescription.equals("BARRIER") || this.getKeyBinding() == null;
    }

    /**
     * Updates the width of the text.
     *
     * @return The updated text width.
     */
    public double updateTextWidth() {
        return this.textWidth = Minecraft.getInstance().fontRenderer.getStringWidth(this.getTextContent()) * this.getScale();
    }

    /**
     * Returns the width of the text for a custom keystroke.
     */
    public double getTextWidth() {
        return this.textWidth;
    }

    /**
     * Whether or not his keystroke is a default keystroke.
     */
    public boolean isDefault() {
        return this.isDefault;
    }

    /**
     * Returns the text content for this keystroke.
     */
    public String getTextContent() {
        if (this.keyType == KeyType.KEY) {
            // if the HUD type is WASD, we'll display the key name
            if (KeystrokesConfig.HUD_KEY_LOOK_TYPE.get() == KeystrokesConfig.MoveType.WASD)
                return getKeyBinding() == null ? "N/A" : Strings.getKeyName(getKeyBinding()).toUpperCase();

            // otherwise if the keybinding isn't null we'll put the arrow symbols
            if (this.getKeyBinding() != null) {
                switch (this.getKeyBinding().getKeyDescription()) {
                    case "key.forward":
                        return Translations.ARROW_UP.getString();
                    case "key.left":
                        return Translations.ARROW_LEFT.getString();
                    case "key.right":
                        return Translations.ARROW_RIGHT.getString();
                    case "key.back":
                        return Translations.ARROW_DOWN.getString();
                    default:
                        // for any other keys that aren't "WASD" related
                        return getKeyBinding() == null ? "N/A" : Strings.getKeyName(getKeyBinding()).toUpperCase();
                }
            }

        } else if (this.keyType == KeyType.MOUSE_LEFT) {
            return this.getOnClickText(true);

        } else if (this.keyType == KeyType.MOUSE_RIGHT) {
            return this.getOnClickText(false);
        }

        return "";
    }

    /**
     * Renders the keystroke object.
     */
    public void render(OverlayHud hud) {
        // Render the HUD background
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        // quads requires 4 vertices to be passed, one for each corner
        GL11.glBegin(GL11.GL_QUADS);
        // Select color for the HUD background
        if (this.isPressed() || MainConfigScreen.currentColorOptionsScreen != null && MainConfigScreen.currentColorOptionsScreen.currentTab == ColorTab.CLICK)
            ColorManager.glColor(Color.LIGHT_GRAY.getRGB(), 0.6f);
        else
            ColorManager.glColor(colorManager.getHudBackgroundColor(hud), 0.6f);
        GL11.glVertex3d(0.0, this.height, 0.0);
        GL11.glVertex3d(this.width, this.height, 0.0);
        GL11.glVertex3d(this.width, 0.0, 0.0);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Render the text on top of the background
        this.renderText(hud);
    }

    /**
     * Renders the text for this keystroke.
     */
    private void renderText(OverlayHud hud) {
        if (this.keyType == KeyType.KEY) {
            this.renderKeyText(this.getTextContent(), hud);

        } else if (this.keyType == KeyType.MOUSE_LEFT) {
            this.renderMouse(true, hud);

        } else if (this.keyType == KeyType.MOUSE_RIGHT) {
            this.renderMouse(false, hud);

        } else if (this.keyType == KeyType.SPACEBAR) {
            this.renderSpacebar(hud);
        }
    }

    /**
     * Renders text for a key.
     *
     * @param text Text to render.
     */
    private void renderKeyText(String text, OverlayHud hud) {
        if (this.getKeyBinding() == null) return;

        final FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;

        // the centered x & y text position
        float x = (float) ((this.width - this.updateTextWidth()) / (2 * hud.getScale()));
        float y = (float) ((this.height - Minecraft.getInstance().fontRenderer.FONT_HEIGHT * hud.getScale()) / (2 * hud.getScale()));

        GlStateManager.pushMatrix();
        GlStateManager.scaled(hud.getScale(), hud.getScale(), 0);

        if (KeystrokesConfig.TEXT_SHADOW.get()) {
            fontRenderer.drawStringWithShadow(new MatrixStack(), text, x, y, this.isPressed() ? colorManager.getHudClickColor(hud, x) : colorManager.getHudTextColor(hud, x));
        } else {
            fontRenderer.drawString(new MatrixStack(), text, x, y, this.isPressed() ? colorManager.getHudClickColor(hud, x) : colorManager.getHudTextColor(hud, x));
        }

        GlStateManager.popMatrix();
    }

    /**
     * Renders text for a mouse button.
     *
     * @param left True for left mouse button, false for right.
     */
    private void renderMouse(boolean left, OverlayHud hud) {
        final KeystrokesConfig.CpsType type = KeystrokesConfig.SHOW_CPS.get();

        if (type == KeystrokesConfig.CpsType.NEVER || type == KeystrokesConfig.CpsType.ON_CLICK) {
            // if this is the case, we won't have to render two lines of text
            this.renderKeyText(this.getOnClickText(left), hud);

        } else {
            final FontRenderer font = Minecraft.getInstance().fontRenderer;
            // the text to display on the 1st and 2nd row
            final String firstRow = left ? "LMB" : "RMB";
            final String secondRow = Keystrokes.getInstance().getKeystrokeRegistry().getCPS(left) + " CPS";

            // the text width of the 1st and 2nd row
            double firstRowTextWidth = font.getStringWidth(firstRow) * hud.getScale();
            double secondRowTextWidth = font.getStringWidth(secondRow) * hud.getScale();

            // the centered x text position for 1st and 2nd row
            float firstRowX = (float) ((this.width - firstRowTextWidth) / (2 * hud.getScale()));
            float secondRowX = (float) ((this.width - secondRowTextWidth) / (2 * hud.getScale()));

            // the centered y position for both text rows, offset is +/- 5 for each row
            float y = (float) ((this.height - Minecraft.getInstance().fontRenderer.FONT_HEIGHT * hud.getScale()) / (2 * hud.getScale()));

            GlStateManager.pushMatrix();
            GlStateManager.scaled(this.getScale(), this.getScale(), 0);

            if (KeystrokesConfig.TEXT_SHADOW.get()) {
                font.drawStringWithShadow(new MatrixStack(), firstRow, firstRowX, y - 5, this.isPressed() ? colorManager.getHudClickColor(hud, firstRowX) : colorManager.getHudTextColor(hud, firstRowX));
                font.drawStringWithShadow(new MatrixStack(), secondRow, secondRowX, y + 5, this.isPressed() ? colorManager.getHudClickColor(hud, secondRowX) : colorManager.getHudTextColor(hud, secondRowX));
            } else {
                font.drawString(new MatrixStack(), firstRow, firstRowX, y - 5, this.isPressed() ? colorManager.getHudClickColor(hud, firstRowX) : colorManager.getHudTextColor(hud, firstRowX));
                font.drawString(new MatrixStack(), secondRow, secondRowX, y + 5, this.isPressed() ? colorManager.getHudClickColor(hud, secondRowX) : colorManager.getHudTextColor(hud, secondRowX));
            }

            GlStateManager.popMatrix();
        }
    }

    /**
     * Renders the space bar rectangle.
     */
    private void renderSpacebar(OverlayHud hud) {
        final double xMin = this.width * 0.25, yMin = this.height / 2 - 1, xMax = this.width * 0.75, yMax = this.height / 2 + 1;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        // quads requires 4 vertices to be passed, one for each corner
        GL11.glBegin(GL11.GL_QUADS);
        ColorManager.glColor(this.isPressed() ? colorManager.getHudClickColor(hud, xMin) : colorManager.getHudTextColor(hud, xMin), -1);
        GL11.glVertex3d(xMin, yMax, 0.0);
        ColorManager.glColor(this.isPressed() ? colorManager.getHudClickColor(hud, xMax) : colorManager.getHudTextColor(hud, xMax), -1);
        GL11.glVertex3d(xMax, yMax, 0.0);
        GL11.glVertex3d(xMax, yMin, 0.0);
        ColorManager.glColor(this.isPressed() ? colorManager.getHudClickColor(hud, xMin) : colorManager.getHudTextColor(hud, xMin), -1);
        GL11.glVertex3d(xMin, yMin, 0.0);

        GL11.glEnd();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * Returns the text to display for the mouse keystrokes if CPS type is "On Click".
     *
     * @param left If true, it will return the text for the left mouse button. Otherwise right.
     * @return The text for the mouse button.
     */
    private String getOnClickText(boolean left) {
        if (KeystrokesConfig.SHOW_CPS.get() == KeystrokesConfig.CpsType.NEVER) return left ? "LMB" : "RMB";

        int cps = Keystrokes.getInstance().getKeystrokeRegistry().getCPS(left);
        if (cps == 0) {
            return left ? "LMB" : "RMB";
        } else {
            return cps + " CPS";
        }
    }

    /**
     * Returns the height of this keystroke.
     */
    public double getHeight() {
        return this.height;
    }

    /**
     * Sets the height for this keystroke.
     *
     * @param height The height to set.
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * Returns the width of this keystroke.
     */
    public double getWidth() {
        return this.width;
    }

    /**
     * Sets the width for this keystroke.
     *
     * @param width The width to set.
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * Returns the {@link KeyType} of this keystroke.
     */
    public KeyType getKeyType() {
        return this.keyType;
    }

    // Inner Classes Below

    /**
     * Represents a type for the keystroke.
     */
    public enum KeyType {
        SPACEBAR, MOUSE_LEFT, MOUSE_RIGHT, KEY
    }

    /**
     * Represents a "hidden" Keystroke object.
     */
    public static class BarrierKeystroke extends Keystroke {

        public BarrierKeystroke() {
            super(null, KeyType.KEY, true);
        }
    }

    /**
     * Holds the different {@link Keystroke} objects to display for each row in the HUD.
     */
    public static class Row {
        private final Keystroke[] keystrokes;

        public Row(Keystroke[] keystrokes) {
            this.keystrokes = keystrokes;
        }

        /**
         * Returns the amount of {@link Keystroke} on this row.
         */
        public Keystroke[] getKeystrokes() {
            return this.keystrokes;
        }

        /**
         * Returns the height gap for the next row in the HUD.
         */
        public double getRowHeightOffset() {
            return Arrays.stream(this.keystrokes).findFirst().map(keystroke -> keystroke.getHeight() + 1.5 * keystroke.getScale())
                    .orElseThrow(() -> new RuntimeException("Keys on row misses height"));
        }

        /**
         * Represents the type of a {@link Row}
         */
        public enum RowType {
            KEY, MOUSE, SPACEBAR
        }
    }
}