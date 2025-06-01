module com.staticconstants.flowpad.flowpad {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires org.fxmisc.richtext;
    requires reactfx;
    requires com.google.gson;
    requires ollama4j;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;
    requires java.sql;
    requires javafx.swing;
    requires java.desktop;

    opens com.staticconstants.flowpad to javafx.fxml;
    exports com.staticconstants.flowpad;
    exports com.staticconstants.flowpad.frontend;
    opens com.staticconstants.flowpad.frontend to javafx.fxml;
    exports com.staticconstants.flowpad.backend.notes to com.google.gson;
    opens com.staticconstants.flowpad.frontend.textarea to com.google.gson;

    opens com.staticconstants.flowpad.frontend.Settings to javafx.fxml;

//    TODO: Evaluate this code, I'm not sure if it's a safe approach for the maven build test
    opens com.staticconstants.flowpad.backend.security;
    opens com.staticconstants.flowpad.backend.db;
}