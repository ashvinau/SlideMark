module com.example.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.fxmisc.flowless;
    requires org.fxmisc.richtext;


    opens com.example.app to javafx.fxml;
    exports com.example.app;
}