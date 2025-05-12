package SlideMark;

public class Controller implements ControllerInterface {
    private ControllerInterface editor;
    private ControllerInterface renderer;
    private ControllerInterface parser;
    private ControllerInterface GUI;
    private boolean setupComplete = false;

    public Controller() {
        editor = null;
        renderer = null;
        parser = null;
        GUI = null;
    }

    public Controller(ControllerInterface newEditor,
                      ControllerInterface newRenderer,
                      ControllerInterface newParser,
                      ControllerInterface newGUI) {
        setEditor(newEditor);
        setRenderer(newRenderer);
        setParser(newParser);
        setGUI(newGUI);
        checkSetup();
    }

    /**
     * @param sender A reference to the object sending the request
     * @param message The request's intent
     * @return A ReturnObject encapsulating the appropriate data type for
     * return to the sender, if needed.
     */
    public ReturnObject<?> request(ControllerInterface sender, String message) {
        if (!setupComplete)
            return null;
        if (sender == editor)
            return reactToEditor(sender, message);
        else if (sender == renderer)
            return reactToRenderer(sender, message);
        else if (sender == parser)
            return reactToParser(sender, message);
        else if (sender == GUI)
            return reactToGUI(sender, message);
        else {
            System.out.println("Error, request component invalid.");
            return null;
        }
    }

    private ReturnObject<?> reactToEditor(ControllerInterface sender, String message) {
        System.out.println("Request recieved from editor: " + message);
        // logic here
        return null;
    }

    private ReturnObject<?> reactToRenderer(ControllerInterface renderer, String message) {
        System.out.println("Request recieved from renderer: " + message);
        // logic here
        return null;
    }

    private ReturnObject<?> reactToParser(ControllerInterface parser, String message) {
        System.out.println("Request recieved from parser: " + message);
        // logic here
        return null;
    }

    private ReturnObject<?> reactToGUI(ControllerInterface GUI, String message) {
        System.out.println("Request recieved from GUI: " + message);
        // logic here
        return null;
    }

    public boolean setEditor(ControllerInterface newEditor) {
        if (!setupComplete) {
            editor = newEditor;
            return true;

        } else
            return false;
    }

    public boolean setRenderer(ControllerInterface newRenderer) {
        if (!setupComplete) {
            renderer = newRenderer;
            return true;
        } else
            return false;
    }

    public boolean setParser(ControllerInterface newParser) {
        if (!setupComplete) {
            parser = newParser;
            return true;
        } else
            return false;
    }

    public boolean setGUI(ControllerInterface newGUI) {
        if (!setupComplete) {
            GUI = newGUI;
            return true;
        } else
            return false;
    }

    private boolean checkSetup() {
        boolean setup = (editor != null) && (renderer != null) && (parser != null) && (GUI != null);
        if (!setupComplete && setup)
            setupComplete = true;

        return setup;
    }

}