module com.staticconstants.flowpad.flowpad {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens com.staticconstants.flowpad.flowpad to javafx.fxml;
    exports com.staticconstants.flowpad.flowpad;
}