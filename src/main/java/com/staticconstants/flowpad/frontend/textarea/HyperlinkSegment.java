package com.staticconstants.flowpad.frontend.textarea;

import com.staticconstants.flowpad.frontend.MainEditorController;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.Paragraph;

import java.util.Objects;
import java.util.Optional;

public final class HyperlinkSegment implements RichSegment{
    private final String displayText;
    private final String url;

    @Override
    public int length() {
        return displayText.length();
    }

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

        text.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1 && event.isStillSincePress()) {
                if (event.isControlDown()){
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
                alert.setContentText("This will open the URL: "+ url);

//                TODO: Add 'do not ask again' option

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
        text.setOnContextMenuRequested(event -> {
            MainEditorController.showHyperlinkEditorPopup(text, this, event.getScreenX(), event.getScreenY(), null);
        });

        text.setOnMouseEntered(e -> text.setStyle("-fx-cursor: hand;"));
        text.setOnMouseExited(e -> text.setStyle(""));

        return text;
    }

    public HyperlinkSegment(String displayText, String url) {
        this.displayText = displayText;
        this.url = url;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getUrl() {
        return url;
    }

    public String getText() {
        return displayText;
    }

    public HyperlinkSegment subSequence(int start) {
        return new HyperlinkSegment(displayText.substring(start), url);
    }
    public HyperlinkSegment subSequence(int start, int end) {
        return new HyperlinkSegment(displayText.substring(start, end), url);
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HyperlinkSegment)) return false;
        HyperlinkSegment that = (HyperlinkSegment) o;
        return displayText.equals(that.displayText) && url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayText, url);
    }
}
