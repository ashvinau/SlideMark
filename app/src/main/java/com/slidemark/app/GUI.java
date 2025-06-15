package com.slidemark.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.*;
import java.util.Objects;

import static javafx.application.Application.launch;

public class GUI implements ControllerInterface {
    ControllerInterface c;

    private final BorderPane parent = new BorderPane();
    private static String content;
    private MenuBar menuBar = MenuBarSet.createMenuBar();
    private VBox editorPane = new VBox();
    private VBox slidesPane = new VBox();

    private static ReturnObject<?> editorData;
    private ReturnObject<?> rendererData;

    private static boolean editingLocked = false;
    TextField fileNameField = new TextField();


    public GUI(ControllerInterface c) {
        this.c = c;
    }

    @Override
    public ReturnObject<?> request(ControllerInterface sender, String message) {
        System.out.println("Controller request from " + sender.getClass().getSimpleName() + ": " + message); // DEBUGGING CHECK
        switch (message) {
            case "CREATE_EDITOR":
                editorData = c.request(this, "GET_SLIDE_EDITOR");
                this.editorPane = (VBox) editorData.getValue();
                return null;

            case "CREATE_RENDERER":
                rendererData = c.request(this, "GET_SLIDE_RENDERER");
                this.slidesPane = (VBox) rendererData.getValue();
                return null;

            case "GET_EDITOR_DATA":
                return editorData;

            default:
                return null;
        }
    }



   protected void renderUI() {

        editorData = c.request(this, "GET_SLIDE_EDITOR");
        if (editorData == null) {
            System.err.println("editorData is null"); //DEBUGGING CHECK
            return;
        }
        if (editorData.getValue() == null) {
            System.err.println("editorData.getValue() is null"); //DEBUGGING CHECK
            return;
        }
        this.editorPane = (VBox) editorData.getValue();

        rendererData = c.request(this, "GET_SLIDE_RENDERER");
        this.slidesPane = (VBox) rendererData.getValue();
        HBox toolbar = createToolBar();
        if (rendererData == null) {
            System.err.println("rendererData is null"); //DEBUGGING CHECK
            return;
        }
        if (rendererData.getValue() == null) {
            System.err.println("rendererData.getValue() is null"); //DEBUGGING CHECK
            return;
        }

        Stage mainStage = new Stage();
        setStages(mainStage, this.menuBar, this.editorPane, this.slidesPane, toolbar);


    }

    private boolean setStages(Stage mainStage, MenuBar menu, VBox editorPane, VBox slidesPane, HBox toolbar) {
        if (mainStage == null|| menu == null || editorPane == null || slidesPane == null || toolbar == null) {
            return false;
        }

        VBox topSection = new VBox(menu, toolbar);
        parent.setTop(topSection);

        //Takes the editorpane and slidespane and divides them among one another in order to seperate the components using a splitpane
        SplitPane split = new SplitPane();
        split.getItems().addAll(editorPane, slidesPane);
        setDivider(split, 0.5);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(parent, screenBounds.getWidth(), screenBounds.getHeight());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/guistyles.css")).toExternalForm());
        MenuBarSet.setRefs(c, this, fileNameField);
        mainStage.setTitle("SlideMark");
        mainStage.setScene(scene);
        mainStage.setX(screenBounds.getMinX());
        mainStage.setY(screenBounds.getMinY());
        mainStage.show();

        return true;
    }

    private void setDivider(SplitPane split,double leftProportion) {
        leftProportion = Math.min(Math.max(leftProportion, 0), 1);
        split.setDividerPosition(0, leftProportion);
        split.setPrefHeight(Region.USE_COMPUTED_SIZE);

        parent.setCenter(split);

    }

    // Create a toolbar with filename field and lock toggle
    public HBox createToolBar() {
        HBox fileToolbar = new HBox();
        fileToolbar.setPadding(new Insets(5, 10, 5, 10));
        fileToolbar.setSpacing(10);
        fileToolbar.setAlignment(Pos.CENTER_LEFT);
        fileToolbar.getStyleClass().add("toolbar"); // <-- This line is crucial for CSS targeting
        String fileName = "Untitled";
        fileNameField = new TextField(fileName);
        fileNameField.setPrefWidth(300);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ToggleButton lockButton = new ToggleButton("Lock Editing");
        lockButton.setOnAction(e -> toggleEditing(lockButton));

        fileToolbar.getChildren().addAll(fileNameField, spacer, lockButton);
        return fileToolbar;
    }

    private static void toggleEditing(ToggleButton button) {
        editingLocked = !editingLocked;
        button.setText(editingLocked ? "Unlock Editing" : "Lock Editing");
        System.out.println("Editing Tools " + (editingLocked ? "Disabled" : "Enabled"));
    }

}
