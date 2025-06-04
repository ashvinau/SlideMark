package com.slidemark.app;

import javafx.application.Application;
import javafx.stage.Stage;


public class SlideMark extends Application {

    private Controller controller;
    private static SlideEditor editor;
    private SlideRenderer renderer;
    private GUI gui;
    private SlideParser parser;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        controller = new Controller();
        editor = new SlideEditor(controller);
        renderer = new SlideRenderer(controller);
        gui = new GUI(controller);
        parser = new SlideParser(controller);

        controller.setEditor(editor);
        controller.setRenderer(renderer);
        controller.setGUI(gui);
        controller.setParser(parser);
        controller.setSetup(true);

        gui.renderUI();
        // Testing messages
        editor.request(null,"LOAD_FILE");
        editor.request(null, "SAVE_AS");
    }
}
