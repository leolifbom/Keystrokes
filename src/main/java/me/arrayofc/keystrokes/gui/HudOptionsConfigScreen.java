package me.arrayofc.keystrokes.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.arrayofc.keystrokes.Keystrokes;
import me.arrayofc.keystrokes.KeystrokesConfig;
import me.arrayofc.keystrokes.util.Translations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * The HUD options config screen.
 */
public class HudOptionsConfigScreen extends AbstractConfigScreen {

    private final Keystrokes keystrokes;
    private final Screen lastScreen;

    protected HudOptionsConfigScreen(Keystrokes keystrokes, Screen lastScreenIn) {
        super(Translations.HUD_OPTIONS_TITLE);

        this.keystrokes = keystrokes;
        this.lastScreen = lastScreenIn;
    }

    @Override
    protected void init() {
        super.addButton(new Button(super.width / 2 - 75, super.height - 28, 150, 20, DialogTexts.GUI_DONE, (press) -> Minecraft.getInstance().displayGuiScreen(this.lastScreen)));

        // Show Movement Button
        this.createSettingButton(this.width / 2 - 155, 70, 150, 20, Translations.SHOW_MOVE_LABEL, KeystrokesConfig.SHOW_MOVEMENT, press -> {
            KeystrokesConfig.SHOW_MOVEMENT.set(!KeystrokesConfig.SHOW_MOVEMENT.get());
            this.updateButton(Translations.SHOW_MOVE_LABEL, KeystrokesConfig.SHOW_MOVEMENT);

        }, () -> Collections.singletonList(Translations.SHOW_MOVE_TOOLTIP));

        // CPS Type Button
        this.createSettingButton(this.width / 2 + 5, 70, 150, 20, Translations.SHOW_CPS_LABEL, KeystrokesConfig.SHOW_CPS, press -> {
            final KeystrokesConfig.CpsType current = KeystrokesConfig.SHOW_CPS.get();
            try {
                KeystrokesConfig.CpsType next = KeystrokesConfig.CpsType.values()[current.ordinal() + 1];
                KeystrokesConfig.SHOW_CPS.set(next);
            } catch (ArrayIndexOutOfBoundsException e) {
                KeystrokesConfig.SHOW_CPS.set(KeystrokesConfig.CpsType.ALWAYS);
            }

            this.updateButton(Translations.SHOW_CPS_LABEL, KeystrokesConfig.SHOW_CPS);

        }, () -> {
            List<ITextComponent> components = Lists.newArrayList();
            components.add(Translations.SHOW_CPS_TOOLTIP);
            final KeystrokesConfig.CpsType current = KeystrokesConfig.SHOW_CPS.get();
            if (current == KeystrokesConfig.CpsType.ALWAYS) {
                components.add(Translations.SHOW_CPS_TOOLTIP_ALWAYS);

            } else if (current == KeystrokesConfig.CpsType.ON_CLICK) {
                components.add(Translations.SHOW_CPS_TOOLTIP_ON_CLICK);

            } else components.add(Translations.SHOW_CPS_TOOLTIP_NEVER);

            return components;
        });

        // Show Mouse Button
        this.createSettingButton(this.width / 2 - 155, 100, 150, 20, Translations.SHOW_MOUSE_LABEL, KeystrokesConfig.SHOW_MOUSE, press -> {
            KeystrokesConfig.SHOW_MOUSE.set(!KeystrokesConfig.SHOW_MOUSE.get());
            this.updateButton(Translations.SHOW_MOUSE_LABEL, KeystrokesConfig.SHOW_MOUSE);

        }, () -> Collections.singletonList(Translations.SHOW_MOUSE_TOOLTIP));

        // Spacebar Button
        this.createSettingButton(this.width / 2 + 5, 100, 150, 20, Translations.SHOW_SPACEBAR_LABEL, KeystrokesConfig.SHOW_SPACEBAR, press -> {
            KeystrokesConfig.SHOW_SPACEBAR.set(!KeystrokesConfig.SHOW_SPACEBAR.get());
            this.updateButton(Translations.SHOW_SPACEBAR_LABEL, KeystrokesConfig.SHOW_SPACEBAR);

        }, () -> Collections.singletonList(Translations.SHOW_SPACEBAR_TOOLTIP));
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(matrixStack);
        this.keystrokes.getHudRenderer().renderScreenOverlays();

        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 50, 16777215);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        KeystrokesConfig.CLIENT_CONFIG.save();
    }
}