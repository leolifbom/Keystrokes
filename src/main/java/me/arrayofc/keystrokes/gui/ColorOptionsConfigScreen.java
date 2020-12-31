package me.arrayofc.keystrokes.gui;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.arrayofc.keystrokes.Keystrokes;
import me.arrayofc.keystrokes.KeystrokesConfig;
import me.arrayofc.keystrokes.color.ColorTab;
import me.arrayofc.keystrokes.util.Translations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.settings.SliderPercentageOption;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.Map;

public class ColorOptionsConfigScreen extends Screen {

    // Minecraft resource location for advancement screen used for color previews
    private static final ResourceLocation WINDOW = new ResourceLocation("textures/gui/advancements/window.png");
    // The length of a full button in this screen
    private static final int FULL_BUTTON_LENGTH = 220;

    // Main class instance
    private final Keystrokes keystrokes;
    // Screen before this one was opened
    private final Screen lastScreen;
    // The RGB controllers
    private final SliderPercentageOption[] rgbControls;
    // Map for the tab switch buttons
    private final Map<ColorTab, Button> buttonMap = Maps.newHashMapWithExpectedSize(ColorTab.values().length);

    // The text input field for HEX values
    private TextFieldWidget textField;

    // The location for the color preview window.
    private int offsetX, offsetY;

    // Whether or not the user itself is typing in the text field
    private boolean clientTyping = false;

    // The current tab the user is in
    public ColorTab currentTab;

    public ColorOptionsConfigScreen(Keystrokes keystrokes, Screen lastScreenIn, ColorTab current) {
        super(Translations.COLOR_SCREEN_TITLE);
        this.keystrokes = keystrokes;
        this.lastScreen = lastScreenIn;

        // Create the RGB controllers
        this.rgbControls = new SliderPercentageOption[]{
                this.createRgbControl(0),
                this.createRgbControl(1),
                this.createRgbControl(2)
        };

        // default tab is text
        this.currentTab = current;
    }

    /**
     * Updates the screen when a new tab has been selected.
     *
     * @param newTab The new selected tab.
     */
    private void switchTab(ColorTab newTab) {
        if (currentTab == newTab) return;
        currentTab = newTab;

        MainConfigScreen.currentColorOptionsScreen = new ColorOptionsConfigScreen(this.keystrokes, this.lastScreen, newTab);
        Minecraft.getInstance().displayGuiScreen(MainConfigScreen.currentColorOptionsScreen);
    }

    @Override
    protected void init() {
        this.offsetX = (this.width - 252) / 2;
        this.offsetY = 40;

        int offsetHeight = this.offsetY + 150;
        int startX = super.width / 2 - 155;

        this.initHexInput(startX, offsetHeight, 80, 20);

        startX += 90;
        this.initSliders(startX, offsetHeight);

        offsetHeight += 40;

        // to properly see all of the content required for this screen the height for the screen must be at least 565 and the width 964.
        // at 474 h, 943 w minecraft appears to resize on its own to the smaller UI size, so between that and 565 we'll inform
        // the user that to see all the content of the screen, they should increase the screen size.
        if (Minecraft.getInstance().getMainWindow().getHeight() > 480 && Minecraft.getInstance().getMainWindow().getHeight() < 560) {
            SystemToast.addOrUpdate(Minecraft.getInstance().getToastGui(), SystemToast.Type.TUTORIAL_HINT, new StringTextComponent("Notice"), new StringTextComponent("Increase screen size!"));
        }

        this.buttonMap.put(ColorTab.TEXT, super.addButton(new Button(super.width / 2 - 160, offsetHeight, 100, 20, Translations.COLOR_SCREEN_TEXT_LABEL, p_onPress_1_ -> this.switchTab(ColorTab.TEXT), (p_onTooltip_1_, p_onTooltip_2_, p_onTooltip_3_, p_onTooltip_4_) -> {
            if (p_onTooltip_1_.active)
                this.renderTooltip(p_onTooltip_2_, Minecraft.getInstance().fontRenderer.trimStringToWidth(Translations.COLOR_SCREEN_TEXT_TOOLTIP, Math.max(this.width / 2 - 43, 170)), p_onTooltip_3_, p_onTooltip_4_);
        })));

        this.buttonMap.put(ColorTab.CLICK, super.addButton(new Button(super.width / 2 - 50, offsetHeight, 100, 20, Translations.COLOR_SCREEN_CLICK_LABEL, p_onPress_1_ -> this.switchTab(ColorTab.CLICK), (p_onTooltip_1_, p_onTooltip_2_, p_onTooltip_3_, p_onTooltip_4_) -> {
            if (p_onTooltip_1_.active)
                this.renderTooltip(p_onTooltip_2_, Minecraft.getInstance().fontRenderer.trimStringToWidth(Translations.COLOR_SCREEN_CLICK_TOOLTIP, Math.max(this.width / 2 - 43, 170)), p_onTooltip_3_, p_onTooltip_4_);
        })));

        this.buttonMap.put(ColorTab.HUD, super.addButton(new Button(super.width / 2 + 60, offsetHeight, 100, 20, Translations.COLOR_SCREEN_HUD_LABEL, p_onPress_1_ -> this.switchTab(ColorTab.HUD), (p_onTooltip_1_, p_onTooltip_2_, p_onTooltip_3_, p_onTooltip_4_) -> {
            if (p_onTooltip_1_.active)
                this.renderTooltip(p_onTooltip_2_, Minecraft.getInstance().fontRenderer.trimStringToWidth(Translations.COLOR_SCREEN_HUD_TOOLTIP, Math.max(this.width / 2 - 43, 170)), p_onTooltip_3_, p_onTooltip_4_);
        })));

        this.buttonMap.get(this.currentTab).active = false;

        super.addButton(new Button(super.width / 2 - 75, super.height - 28, 150, 20, DialogTexts.GUI_DONE, (press) -> {
            Minecraft.getInstance().displayGuiScreen(this.lastScreen);
            this.keystrokes.getColorManager().updateConfig();
        }));
    }

    /**
     * Initializes the RGB value sliders.
     */
    public void initSliders(int startWidth, int offsetHeight) {
        int startWidthInc = 5;
        int width = FULL_BUTTON_LENGTH / this.rgbControls.length;
        for (SliderPercentageOption rgbControl : this.rgbControls) {
            final Widget widget = rgbControl.createWidget(Minecraft.getInstance().gameSettings, startWidth, offsetHeight, width);
            super.addButton(widget);
            startWidth += width + startWidthInc;
        }
    }

    /**
     * Initializes the text field for HEX values.
     */
    private void initHexInput(int xIn, int yIn, int width, int height) {
        Minecraft.getInstance().keyboardListener.enableRepeatEvents(true);
        this.textField = new TextFieldWidget(this.font, xIn, yIn, width, height, new StringTextComponent(""));
        this.textField.setMaxStringLength(7); // only allow 7 chars in the text field
        this.textField.setText(this.keystrokes.getColorManager().getAsHexadecimal(this.currentTab));
        this.textField.setTextColor(Color.WHITE.getRGB());
        this.textField.setResponder(s -> {
            // normally this consumer would be invoked at any time the color sliders were
            // used (as it sets text), so this check makes sure that we only listen for text
            // field updates when the user actually typed something themselves
            if (!this.clientTyping) return;

            final String text = this.textField.getText();

            // a hex color code only contains 6 chars, 7 with the #
            if (text.startsWith("#") && text.length() == 7) {
                try {
                    final Color color = Color.decode(text);

                    int[] colors = this.keystrokes.getColorManager().getRgbValues(this.currentTab);

                    // if this hex code isn't the same as the current selected color we'll update
                    if (colors[0] != color.getRed() || colors[1] != color.getGreen() || colors[2] != color.getBlue()) {
                        this.keystrokes.getColorManager().set(this.currentTab, Arrays.asList(color.getRed(), color.getGreen(), color.getBlue()));
                        Minecraft.getInstance().displayGuiScreen(new ColorOptionsConfigScreen(this.keystrokes, this.lastScreen, this.currentTab));
                    }
                } catch (NumberFormatException e) {
                    // Since newer versions there's system toast alerts, why not display one as a notice for the user.
                    SystemToast.addOrUpdate(Minecraft.getInstance().getToastGui(), SystemToast.Type.TUTORIAL_HINT, Translations.TOAST_ERROR_TITLE, Translations.TOAST_INVALID_HEX);
                }
            } else if (!text.startsWith("#") && text.length() > 4) {
                SystemToast.addOrUpdate(Minecraft.getInstance().getToastGui(), SystemToast.Type.TUTORIAL_HINT, Translations.TOAST_ERROR_TITLE, Translations.TOAST_INVALID_HEX);
            }
        });

        this.setFocusedDefault(this.textField);
        this.children.add(this.textField);
    }

    @Override
    public void tick() {
        this.textField.tick();
    }

    /**
     * Renders the required contents for the screen.
     */
    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // renders the dimmed background (or dirt if world is null)
        super.renderBackground(matrixStack);
        // renders the filled color preview rectangle
        this.renderColorPreview(matrixStack);
        // renders the texture window around the color preview
        this.renderWindow(matrixStack);
        // render the huds & text field
        this.keystrokes.getHudRenderer().renderScreenOverlays();
        this.textField.render(matrixStack, mouseX, mouseY, partialTicks);

        // renders the title of this screen
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 20, 16777215);

        // Show info text if Rainbow mode is on and user is changing text color
        if (KeystrokesConfig.RAINBOW.get() && (this.currentTab == ColorTab.TEXT || this.currentTab == ColorTab.CLICK)) {
            drawCenteredString(matrixStack, this.font, (this.currentTab == ColorTab.TEXT ? Translations.COLOR_SCREEN_TEXT_LABEL : Translations.COLOR_SCREEN_CLICK_LABEL).copyRaw().appendString(" ")
                    .append(Translations.COLOR_SCREEN_NOT_SHOW), this.width / 2, 260, 16777215);
        }

        // super call necessary to render the buttons
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    /**
     * Renders the color preview on the screen.
     */
    private void renderColorPreview(MatrixStack matrixStack) {
        fill(matrixStack, this.offsetX + 9, this.offsetY + 18, this.offsetX + 9 + 234, this.offsetY + 18 + 113,
                this.keystrokes.getColorManager().createColor(this.currentTab).getRGB());
    }

    /**
     * Renders the border of the color preview on the screen.
     */
    public void renderWindow(MatrixStack matrixStack) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        // for this window we use a default minecraft texture
        Minecraft.getInstance().getTextureManager().bindTexture(WINDOW);
        this.blit(matrixStack, this.offsetX, this.offsetY, 0, 0, 252, 140);
        // draw the title text of the minecraft texture
        this.font.func_243248_b(matrixStack, Translations.COLOR_PREVIEW_TITLE.copyRaw().appendString(" - ").append(
                this.currentTab == ColorTab.TEXT ? Translations.COLOR_SCREEN_TEXT_LABEL : this.currentTab == ColorTab.CLICK ? Translations.COLOR_SCREEN_CLICK_LABEL
                        : Translations.COLOR_SCREEN_HUD_LABEL), (float) (this.offsetX + 8), (float) (this.offsetY + 6), 4210752);
    }

    /**
     * Creates a slider for a RGB controller.
     *
     * @param type Type of the controller, 0 => red, 1 => green, 2 => blue
     * @return The created {@link SliderPercentageOption} object.
     */
    public SliderPercentageOption createRgbControl(int type) {
        return new SliderPercentageOption("", 0.0D, 255D, 1F,
                (s) -> (double) (this.keystrokes.getColorManager().getRgbValues(this.currentTab)[type]),
                (s, o) -> {
                    final int value = MathHelper.clamp(o.intValue(), 0, 255);
                    this.keystrokes.getColorManager().set(this.currentTab, type, value);

                    // Also update HEX text field when colors have been changed
                    if (this.textField != null) {
                        this.textField.setText(this.keystrokes.getColorManager().getAsHexadecimal(this.currentTab));
                    }
                }, (s, o) -> new StringTextComponent((type == 0 ? "Red" : type == 1 ? "Green" : "Blue")
                + ": " + this.keystrokes.getColorManager().getRgbValues(this.currentTab)[type]));
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        this.clientTyping = true;
        // inform text field that a char has been typed
        return this.textField.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.clientTyping = true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.clientTyping = false;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().keyboardListener.enableRepeatEvents(false);
        this.currentTab = ColorTab.TEXT;
        this.keystrokes.getColorManager().updateConfig();
    }
}