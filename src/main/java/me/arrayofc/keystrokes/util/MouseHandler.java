package me.arrayofc.keystrokes.util;

import com.google.common.collect.Lists;
import net.minecraftforge.client.event.InputEvent;

import java.util.Queue;

/**
 * A mouse handler listens and manages the mouse click inputs.
 */
public class MouseHandler {

    // A queue containing the click times within a second
    private final Queue<Long> clicks = Lists.newLinkedList();

    /**
     * Invoked when the {@link InputEvent.MouseInputEvent} is fired to
     * indicate that a mouse button has been pressed.
     */
    public void clicked() {
        this.clicks.add(System.currentTimeMillis() + 1000);
    }

    /**
     * Returns the clicks per second for this mouse button.
     */
    public int getCPS() {
        while (!this.clicks.isEmpty() && this.clicks.peek() < System.currentTimeMillis())
            this.clicks.remove();
        return this.clicks.size();
    }
}
