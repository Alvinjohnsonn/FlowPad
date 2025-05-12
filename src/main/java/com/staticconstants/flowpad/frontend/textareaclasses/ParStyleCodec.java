package com.staticconstants.flowpad.frontend.textareaclasses;

import org.fxmisc.richtext.model.Codec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ParStyleCodec implements Codec<ParStyle> {

    @Override
    public String getName() {
        return "par-style";
    }

    @Override
    public void encode(DataOutputStream out, ParStyle style) throws IOException {
        // Nothing to write yet
    }

    @Override
    public ParStyle decode(DataInputStream in) throws IOException {
        return ParStyle.EMPTY; // No state yet
    }
}
