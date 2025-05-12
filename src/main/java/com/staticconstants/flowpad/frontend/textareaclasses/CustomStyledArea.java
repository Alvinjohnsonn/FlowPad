package com.staticconstants.flowpad.frontend.textareaclasses;

import javafx.application.Platform;
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

public class CustomStyledArea<P, R, T> extends GenericStyledArea<ParStyle, RichSegment, TextStyle> {

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

        setParagraphGraphicFactory(paragraph -> {
            TextFlow flow = new TextFlow();
            ParStyle pStyle = getParagraph(paragraph).getParagraphStyle();

            TextStyle style = getParagraph(paragraph).getSegments().isEmpty()
                    ? TextStyle.EMPTY
                    : getStyleAtPosition(getAbsolutePosition(paragraph, 0));

            flow.setPadding(new Insets(pStyle.getTopMargin(), 0, pStyle.getBottomMargin(), 0));

            if (pStyle.getListType() != ParStyle.ListType.NONE) {
                Text prefix = new Text(
                        pStyle.getListType() == ParStyle.ListType.BULLET
                                ? "•  "
                                : (paragraph + 1) + ". "
                );
                prefix.setFont(Font.font(style.getFontFamily(), FontWeight.BOLD, style.getFontSize()));
                flow.getChildren().add(prefix);
            }

            return flow;
        });

        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.BACK_SPACE) {
                int paragraphIndex = getCurrentParagraph();
                int caretColumn = getCaretColumn();

                if (caretColumn == 0) {
                    ParStyle currentStyle = getParagraph(paragraphIndex).getParagraphStyle();

                    if (currentStyle.getListType() != ParStyle.ListType.NONE) {
                        ParStyle newStyle = currentStyle.setListType(ParStyle.ListType.NONE);
                        setParagraphStyle(paragraphIndex, newStyle);
                        updateBulletForParagraph(paragraphIndex, newStyle);
                        event.consume();
//                        TODO: Fix bullet not immediately dissappearing. If the first bullet is empty and deleted it doesnt update the interface automatically
                    }
                }
            }
        });
    }

    private void updateBulletForParagraph(int index, ParStyle parStyle) {
        if (parStyle.getListType() != ParStyle.ListType.NONE) {
            Node node = getParagraphGraphic(index);

            TextStyle style = getParagraph(index).getSegments().isEmpty()
                    ? TextStyle.EMPTY
                    : getStyleAtPosition(getAbsolutePosition(index, 0));

            if (node instanceof TextFlow textFlow) {
                if (!textFlow.getChildren().isEmpty()
                        && textFlow.getChildren().get(0) instanceof Text prefix
                        && (prefix.getText().equals("• ") || prefix.getText().matches("\\d+\\. "))) {
                    textFlow.getChildren().remove(0);
                }

                Text prefix = new Text(
                        parStyle.getListType() == ParStyle.ListType.BULLET
                                ? "• "
                                : (index + 1) + ". "
                );
                prefix.setFont(Font.font(style.getFontFamily(), FontWeight.BOLD, style.getFontSize()));
                textFlow.setPadding(new Insets(parStyle.getTopMargin(), 0, parStyle.getBottomMargin(), 0));
                textFlow.getChildren().add(0, prefix);
            }
        }
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

}

