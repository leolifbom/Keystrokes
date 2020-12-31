package me.arrayofc.keystrokes.command;

import com.mojang.brigadier.CommandDispatcher;
import me.arrayofc.keystrokes.Keystrokes;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;

/**
 * Mod command class.
 * <p>
 * Opens the mod configuration.
 */
public class ModCommand {

    /**
     * Method invoked when {@link RegisterCommandsEvent} is fired to register the command for the mod.
     */
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("keystrokes").executes(context -> openMenu()));
    }

    /**
     * Invoked by the command to open the configuration menu.
     */
    public static int openMenu() {
        Keystrokes.getInstance().setMenuOpen(true);
        return 0;
    }
}