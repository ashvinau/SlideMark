package com.slidemark.app;

import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import javafx.scene.input.KeyCode;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


public class SlideEditor implements ControllerInterface {
    private ControllerInterface c;
    private static String content;
    private static CodeArea editor;
    private File currentFile = null;
    private String workingDir = ".";

    public SlideEditor(ControllerInterface newC) {
        if (newC != null)
            c = newC;
        content = "";
        editor = new CodeArea();
    }

    private static void setContent(String newContent) {
        if (newContent != null)
            content = newContent;
    }

    private void update() {
        System.out.println("Editor data update");
        content = editor.getText();
        c.request(this, "PROCESS_SOURCE");
    }

    protected VBox create() {
        VBox markdownView = new VBox();
        markdownView.setPrefWidth(1000);

        Label markCap = new Label("Markdown View");
        markCap.setStyle("-fx-text-fill: white;");

        editor.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                update();
            }
        );
        editor.setWrapText(true);
        editor.setParagraphGraphicFactory(LineNumberFactory.get(editor));

        if (content != null) {
            editor.replaceText(content);
        } else {
            editor.replaceText("");
        }
        editor.getStyleClass().add("code-area");
        VirtualizedScrollPane<CodeArea> vsPane = new VirtualizedScrollPane<>(editor);

        VBox.setVgrow(vsPane, Priority.ALWAYS);
        markdownView.getChildren().addAll(markCap, vsPane);

        return markdownView;
    }

    private boolean loadFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select File to Load");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Documents", "*.txt", "*.md"));
        currentFile = chooser.showOpenDialog(null);
        if (currentFile == null)
            return false;
        workingDir = currentFile.getParentFile().getAbsolutePath();
        System.out.println("Working dir established: " + workingDir);


        try {
            setContent(Files.readString(currentFile.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.clear();
        editor.replaceText(content);
        update();
        return true;
    }

    private boolean writeCurrentFile() {
        try {
            Files.writeString(currentFile.toPath(), content, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Saves into `currentFile`, or if no `currentFile` is set, invokes “Save As”.
     *
     * @return true on successful write; false if the user cancels “Save As” or I/O fails.
     */
    private boolean saveFile() {
        update();

        if (currentFile == null) { // No existing file—fall back to Save As
            return saveAs();
        }

        return writeCurrentFile();
    }

    /**
     * Prompts the user with a “Save As” dialog. If they select a File,
     * assigns it to `currentFile` and then writes the contents. Returns
     * true if the write succeeded, or false if they canceled or an error occurred.
     *
     * @return true on successful save; false if user cancels or I/O fails.
     */
    private boolean saveAs() {
        update();

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save As");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Documents", "*.txt", "*.md"));
        File chosen = chooser.showSaveDialog(null);
        if (chosen == null)
            return false;

        currentFile = chosen;
        workingDir = currentFile.getParentFile().getAbsolutePath();
        System.out.println("Working dir established: " + workingDir);
        return writeCurrentFile();
    }

    private String getCurFilename() {
        if (currentFile != null)
            return currentFile.getName();
        else
            return "Untitled";
    }


    public ReturnObject<?> request(ControllerInterface sender, String message) {
        switch (message) {
            case "GET_CONTENT":
                return new ReturnObject<Object>(content);
            case "GET_SLIDE_EDITOR":
                VBox editorPane = create();
                return new ReturnObject<Object>(editorPane);
            case "LOAD_FILE":
                loadFile();
                break;
            case "SAVE_FILE":
                saveFile();
                break;
            case "SAVE_AS":
                saveAs();
                break;
            case "GET_FILENAME":
                return new ReturnObject<Object>(getCurFilename());
            case "EDITOR_INSERT_DELIMITER":
                editor.insertText(editor.getLength(), "\n===\n");
                editor.requestFollowCaret();
                editor.moveTo(editor.getText().length());
                update();
                break;
            case "GET_WORKING_DIRECTORY":
                return new ReturnObject<>(workingDir);
            default:
                return null;
        }
        return null;
    }

}