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
import java.nio.file.Path;

public class SlideEditor implements ControllerInterface {
    private ControllerInterface c;
    private static String content;
    private static CodeArea editor;
    private File currentFile = null;

    public SlideEditor(ControllerInterface newC) {
        if (newC != null)
            c = newC;
        content = "";
        editor = new CodeArea();
        //loadFile(); // for testing
    }

    private String getContent(){
        if(content == null){
            return "";
        }
       return content;
    }

    private static void setContent(String newContent) {
        if (newContent != null)
            content = newContent;
    }

    private CodeArea getEditor() {
        return editor;
    }

    private void update() {
        System.out.println("Editor data update");
        content = editor.getText();
        c.request(this, "PROCESS_SOURCE");
        // Eventually more logic so we dont repeatedly send all the source
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
    public boolean saveFile() {
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
    public boolean saveAs() {
        update();

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save As");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Documents", "*.txt", "*.md"));
        File chosen = chooser.showSaveDialog(null);
        if (chosen == null)
            return false;

        currentFile = chosen;
        return writeCurrentFile();
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
            case "SAVE_FILE":
                saveFile();
            case "SAVE_AS":
                saveAs();
            default:
                return null;
        }
    }

}