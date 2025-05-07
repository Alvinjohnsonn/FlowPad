package com.staticconstants.flowpad.frontend.textareaclasses;

import javafx.scene.Node;
import javafx.scene.text.TextFlow;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyledSegment;

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
    }

    @Override
    public void deletePreviousChar() {
        int caretPosition = getCaretPosition();
        System.out.print(caretPosition);

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
                    System.out.print("Deleted!");

                    if (caretPosition == 1){
                        deleteText(0, caretPosition);
                    }
                    else deleteText(caretPosition - 1, caretPosition);
                    return;
                } else {
                    break;
                }
            }
            charCount += segLength;
        }

        // Default behavior
        super.deletePreviousChar();
    }
}

