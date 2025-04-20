module com.staticconstants.flowpad.flowpad {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;

    opens com.staticconstants.flowpad to javafx.fxml;
    exports com.staticconstants.flowpad;
}