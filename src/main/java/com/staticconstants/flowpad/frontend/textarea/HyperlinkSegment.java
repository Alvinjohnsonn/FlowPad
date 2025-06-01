package com.staticconstants.flowpad.frontend.textarea;

import com.staticconstants.flowpad.frontend.MainEditorController;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.fxmisc.richtext.TextExt;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link RichSegment} implementation representing a hyperlink.
 * <p>
 * Displays styled, underlined text that can be clicked to open a URL.
 * Also supports context menu editing and basic styling via {@link TextStyle}.
 */
public final class HyperlinkSegment implements RichSegment {
    private final String displayText;
    private final String url;

    /**
     * Constructs a new {@code HyperlinkSegment} with the specified display text and URL.
     *
     * @param displayText the visible text of the hyperlink
     * @param url         the target URL the hyperlink opens
     */
    public HyperlinkSegment(String displayText, String url) {
        this.displayText = displayText;
        this.url = url;
    }

    /**
     * Returns the length of the visible hyperlink text.
     *
     * @return the number of characters in the display text
     */
    @Override
    public int length() {
        return displayText.length();
    }

    /**
     * Creates a styled {@link TextExt} node representing this hyperlink segment.
     * Includes hover and click behavior for navigating or editing the link.
     *
     * @param style the {@link TextStyle} to apply (e.g., font, size, bold, italic)
     * @return a JavaFX node representing the hyperlink
     */
    @Override
    public Node createNode(TextStyle style) {
        TextExt text = new TextExt(getText());
        String fontFamily = style.getFontFamily();

        int fontSize = switch (style.getHeadingLevel()) {
            case 1 -> 28;
            case 2 -> 24;
            case 3 -> 20;
            case 4 -> 16;
            case 5 -> 14;
            default -> style.getFontSize();
        };

        FontWeight weight = (style.isBold() || style.getHeadingLevel() > 0) ? FontWeight.BOLD : FontWeight.NORMAL;
        FontPosture posture = style.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR;
        Font font = Font.font(fontFamily, weight, posture, fontSize);

        text.setFont(font);
        text.setUnderline(true);
        text.setFill(Color.BLUE);

        Tooltip tooltip = new Tooltip("Hold Ctrl and Click to open " + url);
        Tooltip.install(text, tooltip);

        // Handle mouse clicks to open or confirm opening the URL
        text.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1 && event.isStillSincePress()) {
                if (event.isControlDown()) {
                    try {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Open Link");
                alert.setHeaderText("Do you want to open this link?");
                alert.setContentText("This will open the URL: " + url);

                // TODO: Add 'do not ask again' option
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Show hyperlink editing popup on right-click
        text.setOnContextMenuRequested(event -> {
            MainEditorController.showHyperlinkEditorPopup(text, this, event.getScreenX(), event.getScreenY(), null);
        });

        // Change cursor to pointer on hover
        text.setOnMouseEntered(e -> text.setStyle("-fx-cursor: hand;"));
        text.setOnMouseExited(e -> text.setStyle(""));

        return text;
    }

    /**
     * Returns the visible hyperlink text.
     *
     * @return the display text
     */
    public String getDisplayText() {
        return displayText;
    }

    /**
     * Returns the URL the hyperlink points to.
     *
     * @return the hyperlink target URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the display text, equivalent to {@link #getDisplayText()}.
     *
     * @return the hyperlink text
     */
    public String getText() {
        return displayText;
    }

    /**
     * Returns a subsegment of this hyperlink from the given start index to the end.
     *
     * @param start the start index (inclusive)
     * @return a new {@code HyperlinkSegment} with a substring of the text
     */
    public HyperlinkSegment subSequence(int start) {
        return new HyperlinkSegment(displayText.substring(start), url);
    }

    /**
     * Returns a subsegment of this hyperlink from the given start to end index.
     *
     * @param start the start index (inclusive)
     * @param end   the end index (exclusive)
     * @return a new {@code HyperlinkSegment} with a substring of the text
     */
    public HyperlinkSegment subSequence(int start, int end) {
        return new HyperlinkSegment(displayText.substring(start, end), url);
    }

    /**
     * Compares this segment with another for equality.
     *
     * @param o the object to compare with
     * @return true if both display text and URLs are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HyperlinkSegment)) return false;
        HyperlinkSegment that = (HyperlinkSegment) o;
        return displayText.equals(that.displayText) && url.equals(that.url);
    }

    /**
     * Computes the hash code based on display text and URL.
     *
     * @return the hash code for this segment
     */
    @Override
    public int hashCode() {
        return Objects.hash(displayText, url);
    }
}
