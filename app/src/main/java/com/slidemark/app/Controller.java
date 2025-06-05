package com.slidemark.app;

import javafx.scene.control.MenuBar;
import javafx.scene.layout.VBox;

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
        System.out.println("Controller: Request recieved from editor: " + message);
        switch (message) {
            case "PROCESS_SOURCE":
                return parser.request(sender, "PROCESS_SOURCE");
        }
        return null;
    }

    private ReturnObject<?> reactToRenderer(ControllerInterface renderer, String message) {
        System.out.println("Controller: Request recieved from renderer: " + message);
        switch (message) {
            case "GET_LAYOUT":
                return parser.request(this, "GET_LAYOUT");
        }

        return null;
    }

    private ReturnObject<?> reactToParser(ControllerInterface parser, String message) {
        System.out.println("Controller: Request recieved from parser: " + message);
        switch (message) {
            case "GET_CONTENT":
                return editor.request(this, "GET_CONTENT");
            case "LAYOUT_READY":
                return renderer.request(this, "LAYOUT_READY");
        }
        return null;
    }



    private ReturnObject<?> reactToGUI(ControllerInterface GUI, String message) {
        System.out.println("Controller: Request recieved from GUI: " + message); // Just until we are sure everything works
        switch (message) {
            case "GET_SLIDE_EDITOR":
                if (editor == null) {
                    System.err.println("ERROR: Editor not initialized!");
                    return null;
                }
                return editor.request(this, "GET_SLIDE_EDITOR");

            case "EDITOR_UPDATED":
                ReturnObject<?> editorData = GUI.request(this, "GET_EDITOR_DATA");
                if (editorData != null && editorData.getValue() != null) {
                    editor.request(GUI, "SET_CONTENT");
                }
                break;

            case "GET_SLIDE_RENDERER":
                if (renderer != null) {
                    return renderer.request(GUI, "GET_SLIDE_RENDERER");
                } else {
                    System.err.println("Renderer not initialized!");
                    return null;
                }
            case "LOAD_FILE":
                editor.request(this, "LOAD_FILE");
                break;
            case "SAVE_FILE":
                editor.request(this, "SAVE_FILE");
                break;
            case "SAVE_AS":
                editor.request(this, "SAVE_AS");
                break;
            case "GET_FILENAME":
                return editor.request(this, "GET_FILENAME");
            default:
                return null;
        }
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

    public void setSetup(boolean value) { // Debug function
        setupComplete = value;
        if (setupComplete) {
            checkSetup();
        }
    }

}