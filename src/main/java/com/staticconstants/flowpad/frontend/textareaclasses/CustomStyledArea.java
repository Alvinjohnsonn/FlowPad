package com.staticconstants.flowpad.frontend.textareaclasses;

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

import static org.fxmisc.richtext.model.TwoDimensional.Bias.Backward;
import static org.fxmisc.richtext.model.TwoDimensional.Bias.Forward;

public class CustomStyledArea<P, R, T> extends GenericStyledArea<ParStyle, RichSegment, TextStyle> {
    private final IntFunction<Node> graphicFactory = paragraph -> {
        TextFlow flow = new TextFlow();
        ParStyle pStyle = getParagraph(paragraph).getParagraphStyle();

        TextStyle style = getParagraph(paragraph).getSegments().isEmpty()
                ? TextStyle.EMPTY
                : getStyleAtPosition(getAbsolutePosition(paragraph, 0));

        flow.setPadding(new Insets(pStyle.getTopMargin(), 0, pStyle.getBottomMargin(), 0));

        if (pStyle.getListType() != ParStyle.ListType.NONE) {
            Text prefix;

            if (pStyle.getListType() == ParStyle.ListType.BULLET) {
                prefix = new Text("•  ");
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

        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.BACK_SPACE) {
                int paragraphIndex = getCurrentParagraph();
                int caretColumn = getCaretColumn();

                if (caretColumn == 0) {
                    ParStyle currentStyle = getParagraph(paragraphIndex).getParagraphStyle();

                    if (currentStyle.getListType() != ParStyle.ListType.NONE) {
                        ParStyle newStyle = currentStyle.setListType(ParStyle.ListType.NONE);
                        setParagraphStyle(paragraphIndex, newStyle);
                        refreshParagraphGraphics();
                        event.consume();
//                        TODO: Fix bullet not immediately dissappearing. If the first bullet is empty and deleted it doesnt update the interface automatically
                    }
                }
            }
        });
    }



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
        for (int i = 0; i < segments.size(); i++) {
            StyledSegment<RichSegment, TextStyle> segment = segments.get(i);
            RichSegment richSegment = segment.getSegment();

            int segLength = richSegment.length();

            if (charCount + segLength >= columnPosition) {
                if (richSegment instanceof ImageSegment) {

//                    TODO: Fix runtime error when deleting image located on the first line

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

    public void applyParStyleToSelection(ParStyle style) {
        int startPar = offsetToPosition(getSelection().getStart(), Forward).getMajor();
        int endPar = offsetToPosition(getSelection().getEnd(), Backward).getMajor();

        for (int i = startPar; i <= endPar; i++) {
//            ParStyle existing = getParagraph(i).getParagraphStyle();
//            ParStyle updated = existing.merge(style);

            setParagraphStyle(i, style);
            refreshParagraphGraphics();
        }
    }
    public void refreshParagraphGraphics() {
        setParagraphGraphicFactory(null);
        setParagraphGraphicFactory(graphicFactory);
    }
}

