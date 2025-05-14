package com.staticconstants.flowpad.frontend.textarea;

public class HeadingOption {
    private final String label;
    private final int level;

    public HeadingOption(String label, int level) {
        this.label = label;
        this.level = level;
    }

    public String getLabel() {
        return label;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return label;
    }
}
