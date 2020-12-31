package me.arrayofc.keystrokes.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.arrayofc.keystrokes.util.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * An easily manageable class for other configuration screens to extend on.
 */
public abstract class AbstractConfigScreen extends Screen {

    // The list of setting buttons in this screen.
    private final Map<ForgeConfigSpec.ConfigValue<?>, Button> settingButtons = Maps.newHashMap();

    protected AbstractConfigScreen(ITextComponent titleIn) {
        super(titleIn);
    }

    /**
     * Invoked when a button has been interacted with (altered) and required to update it's label text.
     *
     * @param translation The translation for this label.
     * @param value       The configuration value.
     * @param <T>         Type of the configuration value.
     */
    protected <T> void updateButton(ITextComponent translation, ForgeConfigSpec.ConfigValue<T> value) {
        this.settingButtons.get(value).setMessage(this.appendSuffix(translation, value));
    }

    /**
     * Appends suffix to a button label.
     *
     * @param label The label to append suffix to.
     * @param value The configuration value.
     * @param <T>   Type of the configuration value.
     * @return The {@link ITextComponent} with the appended suffix.
     */
    protected <T> IFormattableTextComponent appendSuffix(ITextComponent label, ForgeConfigSpec.ConfigValue<T> value) {
        final IFormattableTextComponent copy = label.copyRaw();
        if (value == null) return copy;

        if (value instanceof ForgeConfigSpec.BooleanValue) {
            // we'll make boolean values look more beautiful with styles
            ForgeConfigSpec.BooleanValue boolValue = (ForgeConfigSpec.BooleanValue) value;

            copy.append(new StringTextComponent(boolValue.get() ? "Yes" : "No").setStyle(
                    Style.EMPTY.setColor(Color.fromTextFormatting(boolValue.get() ? TextFormatting.GREEN : TextFormatting.RED))));

        } else if (value instanceof ForgeConfigSpec.EnumValue) {
            ForgeConfigSpec.EnumValue<?> enumValue = (ForgeConfigSpec.EnumValue<?>) value;

            // For detail, we'll just make sure the label names from enums are looking good
            String string = enumValue.get().name().equalsIgnoreCase("WASD") ? enumValue.get().name() :
                    Strings.makeSentence(enumValue.get().name().toLowerCase().replace("_", " "));
            copy.append(new StringTextComponent(string));

        } else {
            copy.append(new StringTextComponent(Strings.makeSentence(value.get().toString().toLowerCase().replace("_", " "))));
        }

        return copy;
    }

    /**
     * Creates a setting button in the menu.
     */
    protected <T> void createSettingButton(int x, int y, int width, int height, ITextComponent label,
                                           ForgeConfigSpec.ConfigValue<T> value, Button.IPressable a,
                                           Supplier<List<ITextComponent>> tooltipText) {

        Button button = new Button(x, y, width, height, this.appendSuffix(label, value), a,
                // handle the tooltip
                (p_onTooltip_1_, p_onTooltip_2_, p_onTooltip_3_, p_onTooltip_4_) -> {
                    if (p_onTooltip_1_.active) {
                        List<ITextComponent> lines = tooltipText.get();
                        if (lines.isEmpty()) return;

                        List<IReorderingProcessor> toolTipLines = Lists.newArrayList();
                        lines.forEach(s -> toolTipLines.addAll(Minecraft.getInstance().fontRenderer.trimStringToWidth(s, Math.max(this.width / 2 - 43, 170))));
                        // renders a tooltip when hovering over the button to explain what the setting is about
                        this.renderTooltip(p_onTooltip_2_, toolTipLines, p_onTooltip_3_, p_onTooltip_4_);
                    }
                });

        Button btn = super.addButton(button);
        if (value != null) this.settingButtons.put(value, btn);
    }

    /**
     * Super invoked to render the setting buttons.
     */
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        for (Widget button : this.buttons) {
            button.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }
}
