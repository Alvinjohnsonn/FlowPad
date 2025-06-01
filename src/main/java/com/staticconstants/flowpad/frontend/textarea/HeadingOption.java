package com.staticconstants.flowpad.frontend.textarea;

/**
 * Represents an option in a heading level selector, typically used in a ComboBox.
 * <p>
 * Each option consists of a display label (e.g. "Heading 1") and an associated heading level (e.g. 1 for H1).
 */
public class HeadingOption {
    private final String label;
    private final int level;

    /**
     * Constructs a new {@code HeadingOption} with the given label and heading level.
     *
     * @param label the display label for this heading option (e.g. "Heading 2")
     * @param level the numeric heading level (e.g. 2 for H2); use 0 for normal paragraph text
     */
    public HeadingOption(String label, int level) {
        this.label = label;
        this.level = level;
    }

    /**
     * Returns the display label for this heading option.
     *
     * @return the heading label shown in the UI
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the numeric heading level associated with this option.
     *
     * @return the heading level (e.g. 1, 2, 3...) or 0 for normal text
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the string representation of this heading option.
     * This is used by UI components like ComboBox to display the label.
     *
     * @return the label text
     */
    @Override
    public String toString() {
        return label;
    }
}
