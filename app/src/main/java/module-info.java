module com.slidemark.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.datatransfer;
    requires org.fxmisc.richtext;
    requires java.desktop;
    requires org.fxmisc.flowless;
    requires javafx.web;
    requires java.sql;


    opens com.slidemark.app to javafx.fxml;
    exports com.slidemark.app;
}