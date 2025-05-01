package com.staticconstants.flowpad.backend.notes;

import com.google.gson.Gson;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class StyledTextCodecs {

    public static byte[] serializeStyledText(InlineCssTextArea area) throws IOException {
        List<StyledTextSegment> segments = new ArrayList<>();
        int length = area.getLength();

        if (length == 0) return new byte[]{};

        int segmentStart = 0;
        String currentStyle = area.getStyleOfChar(0);

        for (int i = 1; i < length; i++) {
            String style = area.getStyleOfChar(i);
            if (!style.equals(currentStyle)) {
                String segmentText = area.getText(segmentStart, i);
                segments.add(new StyledTextSegment(segmentText, currentStyle));
                segmentStart = i;
                currentStyle = style;
            }
        }

        // Add last segment
        String lastSegmentText = area.getText(segmentStart, length);
        segments.add(new StyledTextSegment(lastSegmentText, currentStyle));

        String jsonStr = new Gson().toJson(segments);
        return compressJsonString(jsonStr);
    }

    static byte[] compressJsonString(String jsonStr) throws IOException
    {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
            gzipStream.write(jsonStr.getBytes("UTF-8"));
        }
        return byteStream.toByteArray();
    }

    public static void deserializeStyledText(byte[] compressedData, InlineCssTextArea area) throws IOException
    {
        String json = decompressJsonString(compressedData);
        Gson gson = new Gson();
        StyledTextSegment[] segments = gson.fromJson(json, StyledTextSegment[].class);

        area.clear();
        for (StyledTextSegment seg : segments) {
            int start = area.getLength();
            area.appendText(seg.text);
            area.setStyle(start, start + seg.text.length(), seg.style);
        }
    }

    static String decompressJsonString(byte[] compressedData) throws IOException
    {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipStream = new GZIPInputStream(byteStream)) {

            StringBuilder output = new StringBuilder();
            int byteRead;
            while ((byteRead = gzipStream.read()) != -1) {
                output.append((char) byteRead);
            }
            return output.toString();
        }
    }

}
