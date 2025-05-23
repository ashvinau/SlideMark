package com.slidemark.app;

import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

public class SlideEditor implements ControllerInterface {
    private ControllerInterface c;
    private static String content;
    private static CodeArea editor;

    public SlideEditor(ControllerInterface newC) {
        if (newC != null)
            c = newC;
        content = "";
        editor = new CodeArea();
    }

    public String getContent(){
        if(content == null){
            return "";
        }
       return content;
    }

    public static void setContent(String newContent) {
        if (newContent != null)
            content = newContent;
    }

    public static CodeArea getEditor() {
        return editor;
    }

    private void update() {
        System.out.println("Editor data update");
        content = editor.getText();
        c.request(this, "PROCESS_SOURCE");
        // Eventually more logic so we dont repeatedly send all the source
    }

    public VBox create() {
        VBox markdownView = new VBox();
        markdownView.setPrefWidth(1000);
        markdownView.setStyle("-fx-background-color: #eeeeee; -fx-padding: 10;");

        Label markCap = new Label("Markdown View");
        markCap.setStyle("-fx-font-size: 18; -fx-font-weight: bold");

        editor.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                update();
            }
        );
        editor.setParagraphGraphicFactory(LineNumberFactory.get(editor));
        editor.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14;");

        if (content != null) {
            editor.replaceText(content);
        } else {
            editor.replaceText("");
        }

        VirtualizedScrollPane<CodeArea> vsPane = new VirtualizedScrollPane<>(editor);

        VBox.setVgrow(vsPane, Priority.ALWAYS);
        markdownView.getChildren().addAll(markCap, vsPane);

        return markdownView;
    }

    public ReturnObject<?> request(ControllerInterface sender, String message) {
        if (message.equals("GET_CONTENT")) {
            return new ReturnObject<Object>(content);
        }
        else if (message.equals("GET_SLIDE_EDITOR")){
            VBox editorPane = create();
            return new ReturnObject<Object>(editorPane);
        }

        return null;
    }

}