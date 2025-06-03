package com.slidemark.app;

import com.slidemark.app.ControllerInterface;
import com.slidemark.app.MenuBarSet;
import javafx.application.Application;
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
import org.fxmisc.richtext.CodeArea;

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


    public void renderUI() {

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

    public static String getCAText() {
        if (editorData == null || editorData.getValue() == null) {
            System.err.println("ERROR: editorData or its value is null!");
            return "";
        }

        CodeArea data = (CodeArea) editorData.getValue();
        content = data.getText();
        return content;
    }



    public boolean setStages(Stage mainStage, MenuBar menu, VBox editorPane, VBox slidesPane, HBox toolbar) {
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

        mainStage.setTitle("SlideMark");
        mainStage.setScene(scene);
        mainStage.setX(screenBounds.getMinX());
        mainStage.setY(screenBounds.getMinY());
        mainStage.show();

        return true;
    }

    public void setDivider(SplitPane split,double leftProportion) {
        leftProportion = Math.min(Math.max(leftProportion, 0), 1);
        split.setDividerPosition(0, leftProportion);
        split.setPrefHeight(Region.USE_COMPUTED_SIZE);

        parent.setCenter(split);

    }

    // Create a toolbar with filename field and lock toggle
    public static HBox createToolBar() {
        HBox fileToolbar = new HBox();
        fileToolbar.setPadding(new Insets(5, 10, 5, 10));
        fileToolbar.setSpacing(10);
        fileToolbar.setAlignment(Pos.CENTER_LEFT);
        fileToolbar.getStyleClass().add("toolbar"); // <-- This line is crucial for CSS targeting

        TextField fileNameField = new TextField("Untitled");
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
