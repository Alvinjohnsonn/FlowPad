package com.staticconstants.flowpad.frontend.textarea;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.model.*;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;

import static org.fxmisc.richtext.model.TwoDimensional.Bias.Backward;
import static org.fxmisc.richtext.model.TwoDimensional.Bias.Forward;

/**
 * A custom rich text area based on {@link GenericStyledArea} that supports rich segments,
 * styled paragraphs, list formatting, and AI integration.
 *
 * @param <P> paragraph style type
 * @param <R> segment type (not used directly, but inherited)
 * @param <T> text style type
 */
public class CustomStyledArea<P, R, T> extends GenericStyledArea<ParStyle, RichSegment, TextStyle> {

    /**
     * Factory for rendering paragraph graphics (e.g. list bullets or numbers).
     */
    private final IntFunction<Node> graphicFactory = paragraph -> {
        TextFlow flow = new TextFlow();
        ParStyle pStyle = getParagraph(paragraph).getParagraphStyle();

        TextStyle style = getParagraph(paragraph).getSegments().isEmpty()
                ? TextStyle.EMPTY
                : getStyleAtPosition(getAbsolutePosition(paragraph, 0));

        flow.setPadding(new Insets(pStyle.getTopMargin(), 0, pStyle.getBottomMargin(), 0));

        if (pStyle.getListType() != ParStyle.ListType.NONE) {
            Text prefix;

            int level = Math.max(0, pStyle.getListLevel());
            double indent = 20 * (level - 1);
            flow.setPadding(new Insets(pStyle.getTopMargin(), 0, pStyle.getBottomMargin(), indent));

            if (pStyle.getListType() == ParStyle.ListType.BULLET) {
                String symbol = switch (level) {
                    case 1 -> "• ";
                    case 2 -> "◦ ";
                    case 3 -> "▪ ";
                    default -> "• ";
                };
                prefix = new Text(symbol);
            } else {
                int number = 1;
                for (int i = paragraph - 1; i >= 0; i--) {
                    ParStyle prevStyle = getParagraph(i).getParagraphStyle();
                    if (prevStyle.getListType() == ParStyle.ListType.NUMBERED) {
                        number++;
                    } else {
                        break;
                    }
                }
                prefix = new Text(number + ". ");
            }
            prefix.setFont(Font.font(style.getFontFamily(), FontWeight.BOLD, style.getFontSize()));
            flow.getChildren().add(prefix);
        }

        return flow;
    };

    private AIConnector aiConnector;

    /**
     * Constructs a CustomStyledArea with paragraph/text styles, segment ops, and a node factory.
     *
     * @param parStyle initial paragraph style
     * @param paragraphStyler function to apply paragraph styles to a TextFlow
     * @param textStyle initial text style
     * @param segmentOps segment operations for handling {@link RichSegment}
     * @param nodeFactory factory for creating display nodes for segments
     */
    public CustomStyledArea(
            ParStyle parStyle,
            BiConsumer<TextFlow, ParStyle> paragraphStyler,
            TextStyle textStyle,
            RichTextOps<RichSegment, TextStyle> segmentOps,
            Function<StyledSegment<RichSegment, TextStyle>, Node> nodeFactory
    ) {
        super(
                ParStyle.EMPTY,
                paragraphStyler,
                TextStyle.EMPTY,
                segmentOps,
                nodeFactory
        );
        setParagraphGraphicFactory(graphicFactory);
    }

    /**
     * Sets the AI connector used for generating or augmenting content.
     *
     * @param aiCon the AI connector instance
     */
    public void setAiConnector(AIConnector aiCon) {
        aiConnector = aiCon;
    }

    /**
     * Returns the current AI connector used by this editor.
     *
     * @return the AI connector instance
     */
    public AIConnector getAiConnector() {
        return aiConnector;
    }

    /**
     * Overrides the deletion logic to safely delete {@link ImageSegment} instances
     * when pressing backspace, especially handling edge cases like the start of a line.
     */
    @Override
    public void deletePreviousChar() {
        int caretPosition = getCaretPosition();

        if (caretPosition == 0) {
            return;
        }

        int paragraphIndex = getCurrentParagraph();
        int columnPosition = getCaretColumn();

        Paragraph<ParStyle, RichSegment, TextStyle> paragraph = getParagraph(paragraphIndex);
        List<StyledSegment<RichSegment, TextStyle>> segments = paragraph.getStyledSegments();

        int charCount = 0;
        for (StyledSegment<RichSegment, TextStyle> segment : segments) {
            RichSegment richSegment = segment.getSegment();
            int segLength = richSegment.length();

            if (charCount + segLength >= columnPosition) {
                if (richSegment instanceof ImageSegment) {
                    // TODO: Fix runtime error when deleting image located on the first line
                    deleteText(caretPosition - 1, caretPosition);
                    return;
                } else {
                    break;
                }
            }
            charCount += segLength;
        }

        super.deletePreviousChar();
    }

    /**
     * Applies a paragraph style to all paragraphs within the current selection.
     *
     * @param style the paragraph style to apply
     */
    public void applyParStyleToSelection(ParStyle style) {
        int startPar = offsetToPosition(getSelection().getStart(), Forward).getMajor();
        int endPar = offsetToPosition(getSelection().getEnd(), Backward).getMajor();

        for (int i = startPar; i <= endPar; i++) {
            setParagraphStyle(i, style);
            refreshParagraphGraphics();
        }
    }

    /**
     * Applies a paragraph style to a specific paragraph by index.
     *
     * @param paragraphIndex index of the paragraph
     * @param style          the paragraph style to apply
     */
    public void applyParStyleToParagraph(int paragraphIndex, ParStyle style) {
        setParagraphStyle(paragraphIndex, style);
        refreshParagraphGraphics();
    }

    /**
     * Refreshes the paragraph graphic factory to update bullet/number rendering.
     */
    public void refreshParagraphGraphics() {
        setParagraphGraphicFactory(null); // Force refresh
        setParagraphGraphicFactory(graphicFactory);
    }
}
