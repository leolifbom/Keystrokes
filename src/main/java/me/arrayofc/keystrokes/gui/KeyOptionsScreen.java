package me.arrayofc.keystrokes.gui;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import me.arrayofc.keystrokes.Keystrokes;
import me.arrayofc.keystrokes.hud.OverlayHud;
import me.arrayofc.keystrokes.util.Strings;
import me.arrayofc.keystrokes.util.Translations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;

/**
 * The config screen for managing the custom HUD overlays.
 */
public class KeyOptionsScreen extends Screen {

    private final Keystrokes keystrokes;
    private final Screen lastScreen;

    // The current Y offset
    private int yOffset;

    // The overlays with their Y position and corresponding button.
    private final Map<OverlayHud, Pair<Integer, Button>> overlays = Maps.newHashMap();

    public KeyOptionsScreen(Keystrokes keystrokes, Screen lastScreenIn) {
        super(Translations.KEY_SCREEN_TITLE);

        this.keystrokes = keystrokes;
        this.lastScreen = lastScreenIn;
    }

    @Override
    protected void init() {
        super.addButton(new Button(super.width / 2 - 75, super.height - 28, 150, 20, DialogTexts.GUI_DONE, press -> {
            Minecraft.getInstance().displayGuiScreen(this.lastScreen);
            this.keystrokes.getColorManager().updateConfig();
        }));

        this.yOffset = 60;

        // add a row for every overlay with a delete button
        for (OverlayHud overlayHud : this.keystrokes.getHudManager().getOverlayHuds()) {
            this.initOptionsRow(overlayHud, yOffset);
            this.yOffset += 40;
        }

        // add the "new binding" button if there's less than 5 overlays reigstered
        if (this.keystrokes.getHudManager().getOverlayHuds().size() < 5) {
            this.yOffset += 30;

            super.addButton(new Button(super.width / 2 - 80, this.yOffset, 150, 20, Translations.KEY_SCREEN_NEW_BINDING_LABEL, press -> Minecraft.getInstance().displayGuiScreen(new KeyInputScreen(this.keystrokes, this)), (p_onTooltip_1_, p_onTooltip_2_, p_onTooltip_3_, p_onTooltip_4_) -> {
                if (p_onTooltip_1_.active) {
                    this.renderTooltip(p_onTooltip_2_, Minecraft.getInstance().fontRenderer.trimStringToWidth(Translations.KEY_SCREEN_NEW_BINDING_TOOLTIP, Math.max(this.width / 2 - 43, 170)), p_onTooltip_3_, p_onTooltip_4_);
                }
            }));
        }
    }

    /**
     * Adds a new row to display overlay hud options on.
     *
     * @param hud       The HUD for this row.
     * @param yOffset   The Y position where this row can be placed on.
     */
    public void initOptionsRow(OverlayHud hud, int yOffset) {
        this.overlays.put(hud, Pair.of(yOffset, super.addButton(new Button(super.width / 2 + 90, yOffset, 70, 20, Translations.KEY_SCREEN_DELETE_OVERLAY_LABEL.copyRaw().setStyle(Style.EMPTY.setColor(Color.fromTextFormatting(TextFormatting.RED))), press -> {
            if (hud.getName().equalsIgnoreCase("default")) return;
            this.deleteOverlay(hud);
        }, (p_onTooltip_1_, p_onTooltip_2_, p_onTooltip_3_, p_onTooltip_4_) -> {
            if (p_onTooltip_1_.active) {
                this.renderTooltip(p_onTooltip_2_, Minecraft.getInstance().fontRenderer.trimStringToWidth(Translations.KEY_SCREEN_DELETE_OVERLAY_TOOLTIP, Math.max(this.width / 2 - 43, 170)), p_onTooltip_3_, p_onTooltip_4_);
            }
        }))));

        // we don't want users to be able to delete the default one
        if (hud.getName().equalsIgnoreCase("default")) {
            this.overlays.get(hud).getSecond().active = false;
        }
    }

    /**
     * Invoked from the "Delete" button.
     * <p>
     * Deletes the specified overlay from the mod.
     */
    private void deleteOverlay(OverlayHud overlayHud) {
        this.buttons.remove(this.overlays.remove(overlayHud).getSecond());
        this.keystrokes.getHudManager().deleteOverlay(overlayHud);
        Minecraft.getInstance().displayGuiScreen(new KeyOptionsScreen(this.keystrokes, this.lastScreen));
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(matrixStack);
        this.keystrokes.getHudRenderer().renderScreenOverlays();

        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 20, 16777215);

        // Draw the overlay names
        this.overlays.forEach((overlayHud, y) -> drawCenteredString(matrixStack, this.font, new StringTextComponent(Strings.makeSentence(overlayHud.getName())), this.width / 2 - 100, y.getFirst(), 16777215));

        if (this.keystrokes.getHudManager().getOverlayHuds().size() == 5) {
            drawCenteredString(matrixStack, this.font, Translations.KEY_SCREEN_MAX, this.width / 2, this.yOffset + 20, 16777215);
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    /**
     * The screen where user inputs a new key binding.
     */
    public static class KeyInputScreen extends Screen {

        // Main class instance
        private final Keystrokes keystrokes;
        // Previous screen
        private final Screen lastScreen;

        // Whether or not the pressed key is an actual key binding
        private boolean validPress = true;
        // Whether or not the pressed key already exists in an overlay hud
        private boolean pressedExisting = false;

        protected KeyInputScreen(Keystrokes keystrokes, Screen lastScreenIn) {
            super(Translations.KEY_SCREEN_INPUT_TITLE);
            this.keystrokes = keystrokes;
            this.lastScreen = lastScreenIn;
        }

        @Override
        protected void init() {
            super.addButton(new Button(super.width / 2 - 75, super.height - 28, 150, 20, DialogTexts.GUI_CANCEL, press -> Minecraft.getInstance().displayGuiScreen(this.lastScreen)));
        }

        @Override
        public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            // render title of this screen
            drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 50, 16777215);

            if (!this.validPress) {
                // if not valid press, we'll render a string to inform the user
                drawCenteredString(matrixStack, this.font, Translations.KEY_SCREEN_INPUT_INVALID, this.width / 2, 75, 16777215);

            } else if (this.pressedExisting) {
                // if the pressed key already exists in another overlay, we'll also render a string to inform the user
                drawCenteredString(matrixStack, this.font, Translations.KEY_SCREEN_INPUT_EXISTING, this.width / 2, 75, 16777215);
            }

            super.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            final InputMappings.Input input = InputMappings.getInputByCode(keyCode, scanCode);

            final KeyBinding keyBinding = Arrays.stream(Minecraft.getInstance().gameSettings.keyBindings)
                    .filter(keyBinding1 -> keyBinding1.getKey().getTranslationKey().equals(input.getTranslationKey()))
                    .findFirst().orElse(null);

            if (keyBinding != null) {
                // see if this keybind is used in a hud overlay already or not
                if (this.keystrokes.getHudManager().isKeybindBusy(keyBinding)) {
                    this.pressedExisting = true;
                    this.validPress = true;
                    // it did, so the render method will now inform of that

                } else {
                    // it's not, so we can create a new HUD overlay for this keybind
                    this.acceptKeybinding(keyBinding);
                }
            } else {
                // the pressed key isn't a keybind
                this.validPress = false;
            }

            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        /**
         * Invoked when the user has pressed a valid keybind to create a new
         * overlay HUD for the pressed keybind.
         */
        public void acceptKeybinding(KeyBinding keyBinding) {
            // Create the keystroke HUD
            this.keystrokes.getKeystrokeRegistry().fromKey(keyBinding);
            // display previous window
            Minecraft.getInstance().displayGuiScreen(this.lastScreen);
        }
    }
}