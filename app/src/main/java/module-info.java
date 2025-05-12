module SlideMark {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.fxmisc.flowless;
    requires org.fxmisc.richtext;
    requires java.desktop;


    opens SlideMark to javafx.fxml;
    exports SlideMark;
}