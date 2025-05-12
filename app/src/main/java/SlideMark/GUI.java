package SlideMark;

import SlideMark.ControllerInterface;
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

import static javafx.application.Application.launch;

public abstract class GUI implements ControllerInterface {
    ControllerInterface c;

    private final BorderPane parent = new BorderPane();
    private Scene content = new Scene(parent);
    private MenuBar menuBar = new MenuBar();

    private VBox editorPane = new VBox();
    private VBox slidesPane = new VBox();

    private static boolean editingLocked = false;

    public GUI(ControllerInterface c) {
        this.c = c;
    }

    public void renderUI() {

        ReturnObject<?> menuBarReturn = c.request(this, "GET_MENU_BAR");
        this.menuBar = (MenuBar) menuBarReturn.getValue();

        ReturnObject<?> editorReturn = c.request(this, "GET_SLIDE_EDITOR");
        this.editorPane = (VBox) editorReturn.getValue();

        ReturnObject<?> rendererReturn = c.request(this, "GET_SLIDE_RENDERER");
        this.slidesPane = (VBox) rendererReturn.getValue();
        HBox toolbar = createToolBar();

        Stage mainStage = new Stage();
        setStages(mainStage, this.menuBar, this.editorPane, this.slidesPane, toolbar);
    }
    public boolean setStages(Stage mainStage, MenuBar menu, VBox editorPane, VBox slidesPane, HBox toolbar) {
        if (mainStage == null|| menu == null || editorPane == null || slidesPane == null || toolbar == null) {
            return false;
        }

        BorderPane parent = new BorderPane();

        VBox topSection = new VBox(menu, toolbar);
        parent.setTop(topSection);

        //Takes the editorpane and slidespane and divides them among one another in order to seperate the components using a splitpane
        SplitPane split = new SplitPane();
        split.getItems().addAll(editorPane, slidesPane);
        setDivider(split, 0.5);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(parent, screenBounds.getWidth(), screenBounds.getHeight());

        mainStage.setTitle("SlideMark");
        mainStage.setScene(scene);
        mainStage.setX(screenBounds.getMinX());
        mainStage.setY(screenBounds.getMinY());
        mainStage.show();

        return true;
    }

    public void setDivider(SplitPane split,double leftProportion) {
        leftProportion = Math.min(Math.max(leftProportion, 0), 1);
        split.getItems().addAll(editorPane, slidesPane);
        split.setDividerPosition(0, leftProportion);
        split.setPrefHeight(Region.USE_COMPUTED_SIZE);

        parent.setCenter(split);

    }


//    public void menuEvent() {
//        c.request(this, "MENU_SELECTED");
//    }
    // I don't think we need this as the menu bar will already be called and will handle majority of functionality?

    public void editorEvent() {
        c.request(this, "EDITOR_UPDATED");
    }

    public void darkEvent() {
        c.request(this, "DARK_MODE_ON");
    }

    public boolean setScene() {
        if (content != null) {
            this.content = new Scene(parent);
            return true;
        }
        return false;
    }

    public boolean setScene(Scene content) {
        if (content != null) {
            this.content = content;
            return true;
        }
        return false;
    }


//    public void setMenu(MenuBar menu) {
//        if (menu != null) {
//            this.menuBar = menu;
//        }
//    }
    //Once again we're calling menubar in RenderUI do we need to set the menu?

    public void setEditorPane(VBox editor) {
        if (editor != null) {
            parent.setLeft(editor);
        }
    }

    // Create a toolbar with filename field and lock toggle
    public static HBox createToolBar() {
        HBox fileToolbar = new HBox();
        fileToolbar.setPadding(new Insets(5, 10, 5, 10));
        fileToolbar.setSpacing(10);
        fileToolbar.setStyle("-fx-background-color: #cccccc;");
        fileToolbar.setAlignment(Pos.CENTER_LEFT);

        TextField fileNameField = new TextField("Untitled.md");
        fileNameField.setPrefWidth(300);
        fileNameField.setStyle("-fx-font-size: 14;");

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
