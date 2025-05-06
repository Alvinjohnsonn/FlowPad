package com.staticconstants.flowpad.frontend.textareaclasses;

import javafx.scene.text.TextFlow;
import java.util.Objects;

public class ParStyle {

    public static final ParStyle EMPTY = new ParStyle();

    public static void apply(TextFlow textFlow, ParStyle style) {
        // In this minimal example, we don't apply anything, but you could add alignment, spacing, etc.
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ParStyle; // no fields yet
    }

    @Override
    public int hashCode() {
        return Objects.hash(); // no fields
    }
}