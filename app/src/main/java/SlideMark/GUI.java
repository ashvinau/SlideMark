package SlideMark;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class GUI extends Application {
    //1. Set the controller to call the requests of the views within the GUI.
    //2. Creating a Notify method that will be enforced for our calls. (C.notify)
    private final BorderPane parent = new BorderPane();

    @Override
    public void start(Stage stage) {
        setUpStage(stage);
    }

    private void setUpStage(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(parent, screenBounds.getWidth(), screenBounds.getHeight());

        // Create UI Components
        MenuBar menuBar = MenuBarSet.createMenuBar();
        HBox fileToolbar = FileToolbar.create();
        VBox markdownView = MarkdownView.create();
        VBox renderView = RenderView.create();

        // Set Layout in BorderPane
        VBox topSection = new VBox(menuBar, fileToolbar);
        parent.setTop(topSection);
        parent.setLeft(markdownView);
        parent.setRight(renderView);

        // Ensure resizing behavior
        markdownView.setPrefWidth(screenBounds.getWidth() * 0.4);
        renderView.setPrefWidth(screenBounds.getWidth() * 0.6);

        stage.setTitle("SlideMark");
        stage.setScene(scene);
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.show();
    }

    private static boolean editingLocked = false;

    public static HBox create() {
        HBox fileToolbar = new HBox();
        fileToolbar.setPadding(new Insets(5, 10, 5, 10));
        fileToolbar.setSpacing(10);
        fileToolbar.setStyle("-fx-background-color: #cccccc; -fx-padding: 10;");
        fileToolbar.setAlignment(Pos.CENTER_LEFT);

        // File name (editable)
        TextField fileNameField = new TextField("Untitled.md");
        fileNameField.setPrefWidth(300);
        fileNameField.setStyle("-fx-font-size: 14;");

        // Spacer to push the button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Lock Editing Toggle
        ToggleButton lockButton = new ToggleButton("Lock Editing");
        lockButton.setOnAction(e -> toggleEditing(lockButton));

        fileToolbar.getChildren().addAll(fileNameField, spacer, lockButton);
        return fileToolbar;
    }

    private static void toggleEditing(ToggleButton button) {
        editingLocked = !editingLocked;
        button.setText(editingLocked ? "Unlock Editing" : "Lock Editing");
        System.out.println("Editing Tools " + (editingLocked ? "Disabled" : "Enabled")); // Placeholder for actual logic
    }

    public static void main(String[] args) {
        launch(args);
    }
}