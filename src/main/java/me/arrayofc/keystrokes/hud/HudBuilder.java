package me.arrayofc.keystrokes.hud;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.arrayofc.keystrokes.keystroke.Keystroke;

import java.util.EnumMap;
import java.util.List;

/**
 * A class for building HUD overlays.
 */
public class HudBuilder {

    // the map containing the sections and rows for the overlay
    private final EnumMap<OverlayHud.Section, List<Keystroke.Row>> rows = Maps.newEnumMap(OverlayHud.Section.class);

    /**
     * Puts a new section to the builder.
     */
    public Section section(OverlayHud.Section type) {
        this.rows.put(type, Lists.newArrayList());
        return new Section(this, type);
    }

    /**
     * Returns the built map.
     */
    public EnumMap<OverlayHud.Section, List<Keystroke.Row>> build() {
        return this.rows;
    }

    /**
     * Class for building new rows in an overlay section.
     */
    public static class Section {
        private final HudBuilder builder;
        private final OverlayHud.Section section;

        public Section(HudBuilder builder, OverlayHud.Section section) {
            this.builder = builder;
            this.section = section;
        }

        /**
         * Adds the list of keystrokes as a new row in the previously section.
         */
        public Section row(List<Keystroke> keystrokes) {
            this.builder.rows.get(this.section).add(new Keystroke.Row(keystrokes));
            return this;
        }

        public HudBuilder buildSection() {
            return this.builder;
        }
    }
}