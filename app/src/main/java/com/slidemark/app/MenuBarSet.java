package com.slidemark.app;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

public class MenuBarSet {
    public static MenuBar createMenuBar() {
        MenuBar menu = new MenuBar();

        Menu fileMenu = new Menu("File");
        Menu editMenu = new Menu("Edit");
        Menu viewMenu = new Menu("View");
        Menu insertMenu = new Menu("Insert");
        Menu helpMenu = new Menu("Help");

        fileMenu.getItems().addAll(
                new MenuItem("New Presentation"),
                new MenuItem("Open"),
                new MenuItem("Save"),
                new MenuItem("Save as.."),
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
        return menu;
    }
}
