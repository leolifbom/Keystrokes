package me.arrayofc.keystrokes;

import com.electronwill.nightconfig.core.EnumGetMethod;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * The {@link ForgeConfigSpec} provider (configuration) for this mod.
 */
public class KeystrokesConfig {

    /**
     * Represents the preferred way of showing the movement buttons.
     */
    public enum MoveType {
        WASD, ARROWS
    }

    /**
     * Represents a setting for when to show CPS on the HUD.
     */
    public enum CpsType {
        ALWAYS, ON_CLICK, NEVER
    }

    public static ForgeConfigSpec CLIENT_CONFIG;

    public static ForgeConfigSpec.ConfigValue<Double> HUD_SCALE_MULTIPLIER;
    public static ForgeConfigSpec.ConfigValue<Boolean> SHOW_MOVEMENT;
    public static ForgeConfigSpec.ConfigValue<Boolean> SHOW_MOUSE;
    public static ForgeConfigSpec.EnumValue<CpsType> SHOW_CPS;
    public static ForgeConfigSpec.ConfigValue<Boolean> SHOW_SPACEBAR;

    public static ForgeConfigSpec.ConfigValue<Boolean> RAINBOW;
    public static ForgeConfigSpec.ConfigValue<Boolean> TEXT_SHADOW;
    public static ForgeConfigSpec.EnumValue<MoveType> HUD_KEY_LOOK_TYPE;

    public static ForgeConfigSpec.ConfigValue<String> TEXT_RGB;
    public static ForgeConfigSpec.ConfigValue<String> CLICK_RGB;
    public static ForgeConfigSpec.ConfigValue<String> HUD_RGB;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Keystrokes Settings").push("general");
        HUD_SCALE_MULTIPLIER = builder.comment("Represents the multiplier to alter the size of the HUD.")
                .defineInRange("hud-scale", 1D, 1D, 3D);

        SHOW_MOVEMENT = builder.comment("Defines whether or not to show the movement buttons in the HUD.")
                .define("show-movement", true);

        SHOW_MOUSE = builder.comment("Defines whether or not to show the mouse buttons in the HUD.")
                .define("show-mouse", true);

        SHOW_CPS = builder.comment("Defines whether or not to display the CPS", "(clicks-per-second) on the mouse part.")
                .defineEnum("show-cps", CpsType.ALWAYS, EnumGetMethod.ORDINAL_OR_NAME_IGNORECASE);

        SHOW_SPACEBAR = builder.comment("Defines whether or not to show the space bar in the HUD.")
                .define("show-spacebar", true);

        builder.comment("Keystrokes Design").push("looks");
        HUD_KEY_LOOK_TYPE = builder.comment("Choose to display the HUD keys as arrows or key names.")
                .defineEnum("key-display", MoveType.WASD, EnumGetMethod.ORDINAL_OR_NAME_IGNORECASE);

        RAINBOW = builder.comment("If enabled, the HUD will wave in all colors of the rainbow.")
                .define("rainbow", false);

        TEXT_SHADOW = builder.comment("If enabled, the HUD text with show with a text shadow.")
                .define("text-shadow", false);

        builder.comment("Colors").push("colors");
        TEXT_RGB = builder.comment("The text color RGB values. Values goes as: red, green blue. Values may be between 0 => 255.")
                .define("text-color", "255:255:255", o -> o instanceof String);

        CLICK_RGB = builder.comment("The click color RGB values. Values goes as: red, green blue. Values may be between 0 => 255.")
                .define("click-color", "71:71:71", o -> o instanceof String);

        HUD_RGB = builder.comment("The background color RGB values. Values goes as: red, green blue. Values may be between 0 => 255.")
                .define("hud-color", "48:48:48", o -> o instanceof String);

        builder.pop();

        CLIENT_CONFIG = builder.build();
    }
}
