module com.staticconstants.flowpad.flowpad {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires org.fxmisc.richtext;
    requires java.desktop;

    opens com.staticconstants.flowpad to javafx.fxml;
    exports com.staticconstants.flowpad;
    exports com.staticconstants.flowpad.controller;
    opens com.staticconstants.flowpad.controller to javafx.fxml;
}