package SlideMark;

import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

public class SlideEditor implements ControllerInterface {
    private ControllerInterface c;
    private static String content;
    private static CodeArea editor;

    public static void setContent(String newContent) {
        if (newContent != null)
            content = newContent;
    }

    public static CodeArea getEditor() {
        return editor;
    }

    public static VBox create() {
        VBox markdownView = new VBox();
        markdownView.setPrefWidth(1000);
        markdownView.setStyle("-fx-background-color: #eeeeee; -fx-padding: 10;");

        Label markCap = new Label("Markdown View");
        markCap.setStyle("-fx-font-size: 18; -fx-font-weight: bold");

        CodeArea codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14;");
        VirtualizedScrollPane<CodeArea> vsPane = new VirtualizedScrollPane<>(codeArea);


        VBox.setVgrow(vsPane, Priority.ALWAYS);
        markdownView.getChildren().addAll(markCap, vsPane);

        return markdownView;
    }

    /**
     * @param sender
     * @param message
     * @return
     */
    public ReturnObject<?> request(ControllerInterface sender, String message) {
        if (message.equals("SET_CONTENT"))
            content = GUI.getCAText();
        return null;
    }
}