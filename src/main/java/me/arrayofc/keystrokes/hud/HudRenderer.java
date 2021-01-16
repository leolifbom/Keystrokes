package me.arrayofc.keystrokes.hud;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import me.arrayofc.keystrokes.Keystrokes;
import me.arrayofc.keystrokes.KeystrokesConfig;
import me.arrayofc.keystrokes.keystroke.Keystroke;
import me.arrayofc.keystrokes.util.RenderUtil;
import me.arrayofc.keystrokes.util.Translations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * The HudRenderer class manages the rendering of the different HUD overlays.
 * <p>
 * It also acts as the "Change Position" screen, where the user can rescale and move around the HUDs.
 */
@SuppressWarnings("deprecation")
public class HudRenderer extends Screen {

    // Main class instance
    private final Keystrokes keystrokes;

    // The current overlay hud we're dragging
    private OverlayHud dragging = null;

    // The current key width offset for the rainbow effect
    public double rainbowXOffset;

    public HudRenderer(Keystrokes keystrokes) {
        super(new TranslationTextComponent(""));
        this.keystrokes = keystrokes;
    }

    /**
     * Initializes this screen.
     * <p>
     * Invoked when the {@link Minecraft#currentScreen} is set to this object.
     */
    @Override
    protected void init() {
        super.addButton(new Button(super.width / 2 - 75, super.height - 28, 150, 20, DialogTexts.GUI_DONE, (press) -> {
            Minecraft.getInstance().displayGuiScreen(null);
            // update the hud positioning in the config
            this.keystrokes.getHudManager().getOverlayHuds().forEach(overlayHud -> this.keystrokes.getHudManager().saveOverlay(overlayHud));
        }));
    }

    /**
     * Renders this screen.
     * <p>
     * Invoked when the {@link Minecraft#currentScreen} is set to this object.
     */
    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // renders the dimmed background (or dirt if world is null)
        super.renderBackground(matrixStack);
        // render grid lines throughout the screen & overlay huds
        this.renderGrid(matrixStack);
        // render the screen title
        drawCenteredString(matrixStack, super.font, Translations.POSITION_LABEL, super.width / 2, 20, 16777215);
        drawCenteredString(matrixStack, super.font, Translations.GRID_SCREEN_SIZE_TITLE, super.width / 2, 50, 16777215);
        drawCenteredString(matrixStack, super.font, Translations.GRID_SCREEN_DRAG_TITLE, super.width / 2, 65, 16777215);
        // we'll need to render the overlays as well as we're now in a screen
        this.renderScreenOverlays();
        // renders the buttons added to this screen
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    /**
     * Renders the help grid and the lines around the HUD.
     * <p>
     * Invoked when the {@link Minecraft#currentScreen} is set to this object.
     */
    public void renderGrid(MatrixStack matrixStack) {
        int stopWidth = this.width - 5;
        int stopHeight = this.height - 5;

        // Render the horizontal help lines
        RenderUtil.hLine(matrixStack, 5, stopWidth, 5, Color.GRAY.getRGB());
        RenderUtil.hLine(matrixStack, 5, stopWidth, (stopHeight / 2) / 2, Color.GRAY.getRGB());
        RenderUtil.hLine(matrixStack, 5, stopWidth, stopHeight / 2, Color.WHITE.getRGB());
        RenderUtil.hLine(matrixStack, 5, stopWidth, (stopHeight / 2) / 2 + (stopHeight / 2), Color.GRAY.getRGB());
        RenderUtil.hLine(matrixStack, 5, stopWidth, stopHeight, Color.GRAY.getRGB());

        // Render the vertical help lines
        RenderUtil.vLine(matrixStack, 5, 5, stopHeight, Color.GRAY.getRGB());
        RenderUtil.vLine(matrixStack, (stopWidth / 2) / 2, 5, stopHeight, Color.GRAY.getRGB());
        RenderUtil.vLine(matrixStack, stopWidth / 2, 5, stopHeight, Color.WHITE.getRGB());
        RenderUtil.vLine(matrixStack, (stopWidth / 2) / 2 + (stopWidth / 2), 5, stopHeight, Color.GRAY.getRGB());
        RenderUtil.vLine(matrixStack, stopWidth, 5, stopHeight, Color.GRAY.getRGB());

        for (OverlayHud overlayHud : this.keystrokes.getHudManager().getOverlayHuds()) {
            final HudPosition hudPosition = overlayHud.getHudPosition();

            // make it easier to see which HUD is being dragged
            final int color = (overlayHud.isDragMode() ? Color.GREEN : Color.WHITE).getRGB();

            // Render the lines around the HUD
            RenderUtil.hLine(matrixStack, hudPosition.getX() - 1, hudPosition.getX() + hudPosition.getWidth() - 2, hudPosition.getY() + 1, color);
            RenderUtil.hLine(matrixStack, hudPosition.getX() - 1, hudPosition.getX() + hudPosition.getWidth() - 2, hudPosition.getY() + hudPosition.getHeight() - 1, color);
            RenderUtil.vLine(matrixStack, hudPosition.getX() - 1, hudPosition.getY(), hudPosition.getY() + hudPosition.getHeight(), color);
            RenderUtil.vLine(matrixStack, hudPosition.getX() + hudPosition.getWidth() - 1, hudPosition.getY(), hudPosition.getY() + hudPosition.getHeight(), color);
        }
    }

    /**
     * Renders all the registered {@link OverlayHud}.
     * <p>
     * This method is continually invoked, even if the {@link Minecraft#currentScreen} is not set to this object.
     */
    public void renderScreenOverlays() {
        final boolean isRainbow = KeystrokesConfig.RAINBOW.get();

        // get all the registered huds from the hud manager
        for (OverlayHud overlay : this.keystrokes.getHudManager().getOverlayHuds()) {
            final HudPosition hudPosition = overlay.getHudPosition();

            // push matrix and manipulate the current matrix with the huds x and y position
            GlStateManager.pushMatrix();
            GlStateManager.translated(hudPosition.getX(), hudPosition.getY(), 0.0);

            // with these variables we count the current height and width of the hud
            int currentHeight = 0, currentWidth = 0;
            boolean countedWidth = false;

            // loop the lists of rows in this overlay
            for (Map.Entry<OverlayHud.Section, List<Keystroke.Row>> rowEntry : overlay.getRowMap().entrySet()) {
                if (!overlay.isCustom()) {
                    // skip the hud components that are disabled in the settings
                    if (rowEntry.getKey() == OverlayHud.Section.KEY && !KeystrokesConfig.SHOW_MOVEMENT.get()) continue;
                    if (rowEntry.getKey() == OverlayHud.Section.MOUSE && !KeystrokesConfig.SHOW_MOUSE.get()) continue;
                    if (rowEntry.getKey() == OverlayHud.Section.SPACEBAR && !KeystrokesConfig.SHOW_SPACEBAR.get()) continue;
                }

                // now loop the rows in the list
                for (int i = 0; i < rowEntry.getValue().size(); i++) {
                    final Keystroke.Row row = rowEntry.getValue().get(i);

                    // we'll only count the width of an overlay once
                    if (i > 0) countedWidth = true;

                    // push the matrix
                    GlStateManager.pushMatrix();

                    // loop the keystrokes on this row
                    for (int k = 0; k < row.getKeystrokes().size(); k++) {
                        Keystroke keystroke = row.getKeystrokes().get(k);
                        // render keystroke if it's not a barrier
                        if (!keystroke.isBarrier()) keystroke.render(overlay);

                        // increment the x offset for the keystroke next to this
                        double xOffset = keystroke.getWidth() + (rowEntry.getKey() == OverlayHud.Section.MOUSE ? 2 : 1.5) * overlay.getScale();

                        // increment the count of the width of the overlay
                        if (!countedWidth) currentWidth += xOffset;

                        GlStateManager.translated(xOffset, 0, 0);

                        if (isRainbow) {
                            // increment the keystroke rainbow effect offset
                            this.rainbowXOffset += xOffset / keystroke.getWidth();
                        }
                    }

                    // for every push, we need to pop
                    GlStateManager.popMatrix();
                    // and once again change matrix
                    GlStateManager.translated(0, row.getRowHeightOffset(), 0);

                    if (isRainbow) {
                        // reset the offset
                        this.rainbowXOffset = 0f;
                    }

                    // increment the height count with the row height offset
                    currentHeight += row.getRowHeightOffset();
                }
            }

            // set the width and height of the overlay, in case it has changed
            hudPosition.setWidth(currentWidth);
            // subtract the last offset from the width and height
            hudPosition.setHeight(currentHeight - 1 + ((int) overlay.getScale()));

            // finally pop the matrix from the first push
            GlStateManager.popMatrix();

            // lastly we'll make sure the overlay is with in screen parameters
            this.checkDimensions(hudPosition);
        }
    }

    /**
     * Checks if a overlay HUD is no longer inside of the current screen height or width,
     * and if so, moves it to a valid location on the screen.
     */
    public void checkDimensions(HudPosition hudPosition) {
        if (hudPosition.getX() < 5)
            hudPosition.setX(5);

        if (hudPosition.getY() < 5)
            hudPosition.setY(5);

        final int[] dimensions = this.getScreenDimensions();

        if (hudPosition.getX() + hudPosition.getWidth() > dimensions[0] - 5)
            hudPosition.setX(dimensions[0] - 5 - hudPosition.getWidth());

        if (hudPosition.getY() + hudPosition.getHeight() > dimensions[1] - 5)
            hudPosition.setY(dimensions[1] - 5 - hudPosition.getHeight());
    }

    /**
     * Returns the current screen width and height.
     */
    public int[] getScreenDimensions() {
        if (Minecraft.getInstance().currentScreen != null) {
            return new int[]{Minecraft.getInstance().currentScreen.width, Minecraft.getInstance().currentScreen.height};
        } else {
            return new int[]{Minecraft.getInstance().getMainWindow().getWidth(), Minecraft.getInstance().getMainWindow().getHeight()};
        }
    }

    /**
     * Checks whether or not the given X and Y coordinate is outside of allowed parameters.
     *
     * @param x X position
     * @param y Y position
     * @return True if outside of allowed parameters
     */
    private boolean isOutsideParameters(HudPosition hudPosition, int x, int y) {
        int width = x + hudPosition.getWidth();
        int height = y + hudPosition.getHeight();

        int[] dimensions = this.getScreenDimensions();

        if (width > dimensions[0] - 5) {
            return true;

        } else if (height > dimensions[1] - 5) {
            return true;

        } else if (hudPosition.getX() < 5) {
            return true;

        } else return hudPosition.getY() < 5;
    }


    /**
     * Multiplies all HUD heights and widths with the given parameter to change size of the HUDs.
     *
     * @param scale The new scale.
     */
    public void rescale(OverlayHud hud, double scale) {
        for (Map.Entry<OverlayHud.Section, List<Keystroke.Row>> entry : hud.getRowMap().entrySet()) {
            for (Keystroke.Row row : entry.getValue()) {
                for (Keystroke keystroke : row.getKeystrokes()) {
                    keystroke.setHeight(Keystroke.DEFAULT_KEY_SCALE.get(keystroke.getKeyType()).getLeft() * scale);
                    if (keystroke.isDefault() || keystroke.getTextContent().length() <= 3) {
                        keystroke.setWidth(Keystroke.DEFAULT_KEY_SCALE.get(keystroke.getKeyType()).getRight() * scale);
                    } else {
                        keystroke.updateTextWidth();
                        keystroke.setWidth(keystroke.getTextWidth() + 5 * scale);
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // (left click mouse)
            // check if there's a hud overlay at this clicked location
            OverlayHud clicked = this.keystrokes.getHudManager().getClickedOverlay(mouseX, mouseY);
            if (clicked != null) {
                // if so, we'll set it into drag mode
                clicked.setDragMode(true);
                this.dragging = clicked;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void tick() {
        if (this.dragging != null) {
            this.renderScreenOverlays();
        }
        super.tick();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // check if we're dragging something
        if (this.dragging != null) {
            if (!this.isOutsideParameters(this.dragging.getHudPosition(), (int) dragX, (int) dragY)) {
                // if the drag action is inside of the allowed screen size we can move the dragged HUD
                this.dragging.getHudPosition().move((int) (this.dragging.getHudPosition().getX() + dragX), (int) (this.dragging.getHudPosition().getY() + dragY));
                this.dragging.setLastX(this.dragging.getHudPosition().getX());
                this.dragging.setLastY(this.dragging.getHudPosition().getY());

            } else {
                // if not, we'll attempt to move the HUD to the last allowed X and Y coordinate
                int lastX = this.dragging.getLastX();
                int lastY = this.dragging.getLastY();

                // 0 == width, 1 == height
                final int[] dimensions = getScreenDimensions();

                // quick check if the last values are ok, if not we'll fix them
                if (lastX < 5) lastX = 5;
                if (lastY < 5) lastY = 5;
                if (lastX + this.dragging.getHudPosition().getWidth() > dimensions[0])
                    lastX = dimensions[0] - 5 - this.dragging.getHudPosition().getWidth();
                if (lastY + this.dragging.getHudPosition().getHeight() > dimensions[1])
                    lastY = dimensions[1] - 5 - this.dragging.getHudPosition().getHeight();

                // move the HUD to the ok location
                this.dragging.getHudPosition().move(lastX, lastY);
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.dragging != null) {
            this.dragging.setDragMode(false);
            this.dragging = null;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        OverlayHud selected = this.dragging == null ? this.keystrokes.getHudManager().getClickedOverlay(mouseX, mouseY) : this.dragging;
        if (selected == null) return false;

        final double del = delta * 0.1;
        double diff = selected.getScale() + del;
        // scale limits
        if (diff <= 1 || diff >= 3) return false;

        selected.setScale(diff);
        this.rescale(selected, diff);
        return false;
    }

    @Override
    public void onClose() {
        if (this.dragging != null) {
            this.dragging.setDragMode(false);
            this.dragging = null;
        }

        // when the screen is closed we'll save the overlay huds
        this.keystrokes.getHudManager().getOverlayHuds().forEach(overlayHud -> this.keystrokes.getHudManager().saveOverlay(overlayHud));
        this.keystrokes.getHudManager().getOverlayHuds().forEach(hud -> this.keystrokes.getHudManager().saveOverlay(hud));
    }
}
