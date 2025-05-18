package com.staticconstants.flowpad.backend.notes;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javafx.scene.paint.Color;

import java.io.IOException;

public class ColorAdapter extends TypeAdapter<Color> {

    @Override
    public void write(JsonWriter out, Color color) throws IOException {
        if (color == null) {
            out.nullValue();
            return;
        }
        String colorStr = String.format("#%02X%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                (int) (color.getOpacity() * 255));
        out.value(colorStr);
    }

    @Override
    public Color read(JsonReader in) throws IOException {
        String colorStr = in.nextString();
        if (colorStr == null || colorStr.isEmpty()) {
            return null;
        }
        return Color.web(colorStr);
    }
}
