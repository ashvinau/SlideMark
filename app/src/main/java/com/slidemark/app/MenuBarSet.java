package com.slidemark.app;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

public class MenuBarSet {
    static ControllerInterface c = new Controller();
    static GUI guiRef = new GUI(c);
    static TextField nameField = new TextField("Untitled");

    public static MenuBar createMenuBar() {
        MenuBar menu = new MenuBar();


        MenuItem openCmd;
        MenuItem saveCmd;
        MenuItem saveAsCmd;


        Menu fileMenu = new Menu("File");
        Menu editMenu = new Menu("Edit");
        Menu viewMenu = new Menu("View");
        Menu insertMenu = new Menu("Insert");
        Menu helpMenu = new Menu("Help");

        fileMenu.getItems().addAll(
                new MenuItem("New Presentation"),
                openCmd = new MenuItem("Open"),

                saveCmd = new MenuItem("Save"),
                saveAsCmd = new MenuItem("Save as.."),
                new MenuItem("Export as PDF"),
                new MenuItem("System Preferences"),
                new MenuItem("Close")
        );

        editMenu.getItems().addAll(
                new MenuItem("Undo"),
                new MenuItem("Redo"),
                new MenuItem("Copy"),
                new MenuItem("Paste"),
                new MenuItem("Paste without Formatting"),
                new MenuItem("Select All")
        );

        viewMenu.getItems().addAll(
                new MenuItem("View only.."),
                new MenuItem("Presentation View")
        );

        insertMenu.getItems().addAll(
                new MenuItem("Image"),
                new MenuItem("Text Box"),
                new MenuItem("Shapes"),
                new MenuItem("Table"),
                new MenuItem("Diagram")
        );

        helpMenu.getItems().addAll(
                new MenuItem("SlideMark Help"),
                new MenuItem("Keyboard Shortcuts"),
                new MenuItem("Terms of Service")
        );

        menu.getMenus().addAll(fileMenu, editMenu, viewMenu, insertMenu, helpMenu);

        openCmd.setOnAction(event -> {
            c.request(guiRef,"LOAD_FILE");
            nameField.setText(retrieveFilename());
        });

        saveCmd.setOnAction(event -> {
            c.request(guiRef,"SAVE_FILE");
            nameField.setText(retrieveFilename());
        });

        saveAsCmd.setOnAction(event -> {
            c.request(guiRef,"SAVE_AS");
            nameField.setText(retrieveFilename());
        });

        return menu;
    }

    protected static String retrieveFilename() {
        return (String) c.request(guiRef, "GET_FILENAME").getValue();
    }

    public static boolean setRefs(ControllerInterface newC, ControllerInterface newRef, TextField filenameField) {
        c = newC;
        guiRef = (GUI) newRef;
        nameField = filenameField;
        return true;
    }





}
