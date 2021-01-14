package me.arrayofc.keystrokes.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.arrayofc.keystrokes.Keystrokes;
import me.arrayofc.keystrokes.KeystrokesConfig;
import me.arrayofc.keystrokes.util.Translations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.widget.button.Button;

import javax.annotation.Nonnull;
import java.util.Collections;

/**
 * The main configuration screen.
 */
public class MainConfigScreen extends AbstractConfigScreen {

    // Config screen for the color options
    public static ColorOptionsConfigScreen currentColorOptionsScreen = null;

    private final Keystrokes keystrokes;
    private final Minecraft minecraft;

    // Config screen for the hud options
    public final HudOptionsConfigScreen hudOptionsConfigScreen;

    public MainConfigScreen(Keystrokes keystrokes) {
        super(Translations.SCREEN_TITLE);
        this.keystrokes = keystrokes;
        this.minecraft = Minecraft.getInstance();

        this.hudOptionsConfigScreen = new HudOptionsConfigScreen(this.keystrokes, this);
    }

    /**
     * Initializes this screen.
     */
    public void init() {
        // the "Done" button
        this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, DialogTexts.GUI_DONE, (press) -> {
            KeystrokesConfig.CLIENT_CONFIG.save();
            this.minecraft.displayGuiScreen(null);
        }));

        this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, DialogTexts.GUI_CANCEL, (press) -> this.minecraft.displayGuiScreen(null)));

        // Position Button
        this.createSettingButton(this.width / 2 - 155, 70, 150, 20, Translations.POSITION_LABEL, null, press -> this.minecraft.displayGuiScreen(this.keystrokes.getHudRenderer()), () -> Collections.singletonList(Translations.POSITION_TOOLTIP));

        // HUD Options Button
        this.createSettingButton(this.width / 2 + 5, 70, 150, 20, Translations.HUD_OPTIONS_LABEL, null, press -> this.minecraft.displayGuiScreen(this.hudOptionsConfigScreen), () -> Collections.singletonList(Translations.HUD_OPTIONS_TOOLTIP));

        // HUD Overlays Button
        this.createSettingButton(this.width / 2 - 75, 100, 150, 20, Translations.HUD_OVERLAYS_LABEL, null, press -> this.minecraft.displayGuiScreen(new KeyOptionsScreen(this.keystrokes, this)), () -> Collections.singletonList(Translations.HUD_OVERLAYS_TOOLTIP));

        // Hud Type Button
        this.createSettingButton(this.width / 2 - 155, 150, 150, 20, Translations.HUD_TYPE_LABEL, KeystrokesConfig.HUD_KEY_LOOK_TYPE, press -> {
            final KeystrokesConfig.MoveType type = KeystrokesConfig.HUD_KEY_LOOK_TYPE.get();
            if (type == KeystrokesConfig.MoveType.WASD) {
                KeystrokesConfig.HUD_KEY_LOOK_TYPE.set(KeystrokesConfig.MoveType.ARROWS);
            } else {
                KeystrokesConfig.HUD_KEY_LOOK_TYPE.set(KeystrokesConfig.MoveType.WASD);
            }

            this.updateButton(Translations.HUD_TYPE_LABEL, KeystrokesConfig.HUD_KEY_LOOK_TYPE);

        }, () -> Collections.singletonList(Translations.HUD_TYPE_TOOLTIP));

        // Sync Colors
        this.createSettingButton(this.width / 2 + 5, 150, 150, 20, Translations.COLOR_SYNC_LABEL, KeystrokesConfig.SYNC_COLORS, press -> {
            KeystrokesConfig.SYNC_COLORS.set(!KeystrokesConfig.SYNC_COLORS.get());
            this.updateButton(Translations.COLOR_SYNC_LABEL, KeystrokesConfig.SYNC_COLORS);

        }, () -> Collections.singletonList(Translations.COLOR_SYNC_TOOLTIP));

        // Chroma Mode Button
        this.createSettingButton(this.width / 2 - 155, 180, 150, 20, Translations.RAINBOW_LABEL, KeystrokesConfig.RAINBOW, press -> {
            KeystrokesConfig.RAINBOW.set(!KeystrokesConfig.RAINBOW.get());
            this.updateButton(Translations.RAINBOW_LABEL, KeystrokesConfig.RAINBOW);

        }, () -> Collections.singletonList(Translations.RAINBOW_TOOLTIP));

        // Text Shadow Button
        this.createSettingButton(this.width / 2 + 5, 180, 150, 20, Translations.TEXT_SHADOW_LABEL, KeystrokesConfig.TEXT_SHADOW, press -> {
            KeystrokesConfig.TEXT_SHADOW.set(!KeystrokesConfig.TEXT_SHADOW.get());
            this.updateButton(Translations.TEXT_SHADOW_LABEL, KeystrokesConfig.TEXT_SHADOW);

        }, () -> Collections.singletonList(Translations.TEXT_SHADOW_TOOLTIP));
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(matrixStack);
        this.keystrokes.getHudRenderer().renderScreenOverlays();

        // render the different title texts
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 20, 16777215);
        drawCenteredString(matrixStack, this.font, Translations.HUD_OPTIONS_TITLE, this.width / 2, 50, 16777215);
        drawCenteredString(matrixStack, this.font, Translations.COLOR_OPTIONS_TITLE, this.width / 2, 130, 16777215);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        this.keystrokes.setMenuOpen(false);
    }
}