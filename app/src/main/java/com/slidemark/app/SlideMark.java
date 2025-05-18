package com.slidemark.app;

import javafx.application.Application;
import javafx.stage.Stage;


public class SlideMark extends Application {

    private Controller controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        controller = new Controller();
        SlideEditor editor = new SlideEditor();
        SlideRenderer renderer = new SlideRenderer();
        GUI gui = new GUI(controller);

        controller.setEditor(editor);
        controller.setRenderer(renderer);
        controller.setGUI(gui);
        controller.setSetup(true);

        gui.renderUI();
    }
}
