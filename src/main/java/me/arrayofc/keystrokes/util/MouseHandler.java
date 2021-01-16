package me.arrayofc.keystrokes.util;

import com.google.common.collect.Lists;
import me.arrayofc.keystrokes.Keystrokes;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;

import java.util.Queue;

/**
 * A mouse handler listens and manages the mouse click inputs.
 */
public class MouseHandler {

    private static final MouseHandler left, right;
    static {
        left = new MouseHandler();
        right = new MouseHandler();

        MinecraftForge.EVENT_BUS.addListener(MouseHandler::onMouseInput);
    }

    public static void onMouseInput(InputEvent.MouseInputEvent event) {
        // we only want to update the cps with this event when a key has been pressed
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        // ...and we don't want to count cps when a screen is open
        if (Minecraft.getInstance().currentScreen != null) return;

        if (Keystrokes.getInstance().shouldShowOverlays()) {
            if (event.getButton() == 0) {
                left.clicks.add(System.currentTimeMillis() + 1000);
            } else {
                right.clicks.add(System.currentTimeMillis() + 1000);
            }
        }
    }

    /**
     * Returns the left mouse handler.
     */
    public static MouseHandler getLeft() {
        return left;
    }

    /**
     * Returns the right mouse handler.
     */
    public static MouseHandler getRight() {
        return right;
    }

    // A queue containing the click times within a second
    private final Queue<Long> clicks = Lists.newLinkedList();

    /**
     * Returns the clicks per second for this mouse button.
     */
    public int getCPS() {
        while (!this.clicks.isEmpty() && this.clicks.peek() < System.currentTimeMillis())
            this.clicks.remove();
        return this.clicks.size();
    }
}