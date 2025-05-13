package SlideMark;

import javafx.application.Application;
import javafx.stage.Stage;

//public class SlideMark {
//    // App startup:
//    // We instantiate all the components and the controller,
//    // then link them together by setting fields
//
//    public static void main(String[] args) {
//        Controller controller = new Controller();
//        SlideEditor slideEditor = new SlideEditor();
//        GUI gui = new GUI(controller);
//        controller.setEditor(slideEditor);
//        controller.setGUI(gui);
//        controller.setSetup(true); //debug function
//        //gui.start(new Stage());
//    }
//}
//package SlideMark;
//
//import javafx.application.Application;
//import javafx.stage.Stage;

public class SlideMark extends Application {

    private Controller controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        controller = new Controller();
        SlideEditor editor = new SlideEditor();
        controller.setEditor(editor);
        controller.setSetup(true);

        GUI gui = new GUI(controller);
        controller.setGUI(gui);

        gui.renderUI();
    }
}
