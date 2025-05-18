package com.staticconstants.flowpad.backend.notes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.staticconstants.flowpad.frontend.textarea.*;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyledSegment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class StyledTextCodec {

    private static Gson gson;

    static {
        gson = new GsonBuilder()
                .registerTypeAdapter(Color.class, new ColorAdapter())
                .create();
    }

    public static byte[] serializeStyledText(CustomStyledArea<?, ?, ?> area) throws IOException {
        List<SerialisableParagraph> serializableParagraphs = new ArrayList<>();

        for (int i = 0; i < area.getParagraphs().size(); i++) {
            Paragraph<ParStyle, RichSegment, TextStyle> paragraph = area.getParagraph(i);
            List<SerialisableSegment> serializableSegments = new ArrayList<>();

            for (StyledSegment<RichSegment, TextStyle> seg : paragraph.getStyledSegments()) {
                RichSegment rs = seg.getSegment();
                TextStyle style = seg.getStyle();

                if (rs instanceof TextSegment textSeg) {
                    serializableSegments.add(new SerialisableSegment(SerialisableSegment.Type.TEXT, textSeg.getText(), style));
                } else if (rs instanceof ImageSegment imgSeg) {
                    // Convert image to base64 for storage
                    String base64 = imgSeg.toBase64();
                    serializableSegments.add(new SerialisableSegment(SerialisableSegment.Type.IMAGE, base64, style));
                }
            }

            serializableParagraphs.add(new SerialisableParagraph(paragraph.getParagraphStyle(), serializableSegments));
        }

        String json = gson.toJson(serializableParagraphs);
        return compressJsonString(json);
    }

    public static void deserializeStyledText(byte[] compressedData, CustomStyledArea<?, ?, ?> area) throws IOException {
        String json = decompressJsonString(compressedData);
        SerialisableParagraph[] paragraphs = new Gson().fromJson(json, SerialisableParagraph[].class);

        area.clear();

        for (SerialisableParagraph para : paragraphs) {
            List<StyledSegment<RichSegment, TextStyle>> styledSegments = new ArrayList<>();

            for (SerialisableSegment seg : para.segments) {
                RichSegment rs;
                if (seg.type == SerialisableSegment.Type.TEXT) {
                    rs = new TextSegment(seg.content);
                } else if (seg.type == SerialisableSegment.Type.IMAGE) {
                    rs = ImageSegment.fromBase64(seg.content);
                } else {
                    continue; // skip unknown segment types
                }

                area.append(rs, seg.style);
            }

        }

        area.refreshParagraphGraphics();
    }



    private static byte[] compressJsonString(String jsonStr) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
            gzipStream.write(jsonStr.getBytes(StandardCharsets.UTF_8));
        }
        return byteStream.toByteArray();
    }

    private static String decompressJsonString(byte[] compressedData) throws IOException {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipStream = new GZIPInputStream(byteStream)) {
            return new String(gzipStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}
