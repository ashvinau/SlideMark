package com.slidemark.app;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlideRenderer implements ControllerInterface {
    private ControllerInterface c;
    private static VBox renderViewContain = new VBox();
    private static WebView slideContent = new WebView();
    private static WebEngine engine = slideContent.getEngine();
    private static HBox slideCarousel = new HBox();
    private static StackPane slideBox;

    private List<TagComponent> list;
    private String outputHTML;
    private Point size;
    private String currTemplate;
    private int currNumSect;
    private int curSlideIndex = 1;
    private static final Pattern CONTENT_PLACEHOLDER =
            Pattern.compile("\\{\\{Content(\\d+)\\}\\}");

    private List<String> templates = Arrays.asList(
            "templates/blankslide.html",
            "templates/3rowslide.html",
            "templates/centertitleslide.html",
            "templates/contentonlyslide.html",
            "templates/hamburgerslide.html",
            "templates/imagecontentleft.html",
            "templates/imagecontentright.html",
            "templates/tableofcontents4.html",
            "templates/tableofcontents6.html",
            "templates/titleandcontent.html"
    );

    public SlideRenderer(ControllerInterface c) {
        this.c = c;

    }

    public void setLayout(List<TagComponent> tagComponents) {
        this.list = tagComponents;
        updateSlideView(); // Render the slide whenever the layout changes
    }

    public void setCurrentTemplateByName(String templateName) {
        this.currTemplate = templateName;
    }

    public List<TagComponent> getags() {
        return this.list;
    }

    public void showTemplateChooser() {
        String selected = chooseTemplate();
        if (selected != null) {
            setCurrentTemplateByName(selected);


            WebView firstSlide = new WebView();
            firstSlide.setPrefSize(780, 380);
            firstSlide.setMaxSize(780, 380);
            firstSlide.setContextMenuEnabled(false);

            updateCarousel();
            updateSlideView();
        } else {
            System.out.println("Template selection cancelled.");
        }
    }



    public String chooseTemplate() {
        /*ChoiceDialog<String> dialog = new ChoiceDialog<>(templates.get(0), templates);
        dialog.setTitle("Choose Slide Template");
        dialog.setHeaderText("Select a slide template to start with:");
        dialog.setContentText("Template:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);*/
        return "templates/3rowslide.html";
    }

    private String loadTemplate(String path) {
        String htmlText;
        try (InputStream is = getClass().getResourceAsStream("/" + path)) {
            if (is == null) {
                System.err.println("Template NOT FOUND at path: /" + path);
                return null;
            }
            currNumSect = 0;
            htmlText = new String(is.readAllBytes(), StandardCharsets.UTF_8);;
            // Identify how many content areas are in the file
            Matcher m = CONTENT_PLACEHOLDER.matcher(htmlText);
            while (m.find())
                currNumSect++;
            System.out.println("Num of sections for current template: " + currNumSect);

            return htmlText;
        } catch (IOException e) {
            System.err.println("IOException while loading template: " + path);
            e.printStackTrace();
            return null;
        }

    }

    public void updateSlideView() {
        System.out.println("updateSlideView() called. List is " + (list == null ? "null" : ("size " + list.size())));

        String templateHtml = loadTemplate(currTemplate);
        if (templateHtml == null) {
            engine.loadContent("<html><body><h2>Template not found.</h2></body></html>");
            return;
        }

        List<StringBuilder> builders = new ArrayList<>();
        builders.add(new StringBuilder()); // start first section

        if (list != null && !list.isEmpty()) {
            for (TagComponent comp : list) {
                if ("sectChange".equals(comp.getTag())) {
                    builders.add(new StringBuilder());
                } else if (comp.getVisible()) {
                    StringBuilder sb = builders.get(builders.size() - 1);
                    sb.append("<")
                            .append(comp.getTag());
                    if (comp.getParams() != null && !comp.getParams().isBlank()) {
                        sb.append(" ").append(comp.getParams().trim());
                    }
                    sb.append(">");
                    if (comp.getTag().equals("pre") || // All the tags we don't want to cap off
                        comp.getTag().equals("/pre") ||
                        comp.getTag().equals("nextSlide") ||
                        comp.getTag().equals("sectChange") ||
                        comp.getTag().equals("ul") ||
                        comp.getTag().equals("/ul") ||
                        comp.getTag().equals("ol") ||
                        comp.getTag().equals("/ol")
                    ) {
                        continue;
                    } else {
                        sb.append(comp.getContent())
                                .append("</")
                                .append(comp.getTag())
                                .append(">");
                    }

                }
            }
        } else {
            builders.set(0, new StringBuilder("<p>No content to display.</p>"));
        }

        System.out.println("# Sections processed: " + builders.size());
        System.out.println("Num of sections for current template: " + currNumSect);

        String finalHtml = templateHtml;

        for (int i = 1; i <= currNumSect; i++) {
            String placeholder = "{{Content" + i + "}}";
            String content;
            if (i - 1 < builders.size()) {
                content = builders.get(i - 1).toString();
            } else {
                content = "<p>(Empty Section)</p>";
            }

            finalHtml = finalHtml.replace(placeholder, content);
        }
        //Sets the current information to Parser and sets the current slide # to parser
        c.request(this, "SET_SLIDE_NUM");

        engine.loadContent(finalHtml);

        //System.out.println(finalHtml);
    }


    public void addSlide() {
       //I need to clear the previous slide to add the new one in...

        //Increase slide index by 1. Update the slide and add it to the carousel
        curSlideIndex ++;
        updateSlideView();
        updateCarousel();
    }



    private static void updateCarousel() {

    }


    public boolean resizeRender(Point dimensions) {
        if (dimensions == null) return false;

        renderViewContain.setPrefSize(dimensions.getX(), dimensions.getY());
        return true;
    }

    public boolean handleInput(Point location) {
        System.out.println("Input received at: " + location);
        return true;
    }


    public VBox create() {
        renderViewContain.setPrefWidth(1000);
        renderViewContain.getStyleClass().add("render-view-contain");
        renderViewContain.setAlignment(Pos.TOP_CENTER);

        Label renderCap = new Label("Slide View");
        renderCap.getStyleClass().add("render-cap");

        slideContent.setPrefSize(780, 380);
        slideContent.setMaxSize(780, 380);
        slideContent.setContextMenuEnabled(false);
        slideContent.getStyleClass().add("slide-webview");

        StackPane slideContentWrapper = new StackPane(slideContent);
        slideContentWrapper.setAlignment(Pos.CENTER);
        slideContentWrapper.getStyleClass().add("slide-content");
        slideContentWrapper.setPrefSize(780, 380);
        slideContentWrapper.setMaxSize(780, 380);
        slideContentWrapper.setClip(new Rectangle(780, 380));

        // Make slideBox a class-level static variable, if not already
        slideBox = new StackPane(slideContentWrapper);
        slideBox.getStyleClass().add("slide-box");
        slideBox.setPrefSize(780, 380);
        slideBox.setMaxSize(780, 380);

        slideCarousel.getStyleClass().add("slide-carousel");
        slideCarousel.setSpacing(10);

        ScrollPane carouselScroll = new ScrollPane(slideCarousel);
        carouselScroll.setFitToWidth(true);
        carouselScroll.setPrefHeight(100);
        carouselScroll.getStyleClass().add("carousel-scroll");

        Button presentButton = new Button("Presentation Mode");
        presentButton.getStyleClass().add("present-button");
        presentButton.setOnAction(e -> startPresentation());

        Button addSlideButton = new Button("Add Slide");
        addSlideButton.getStyleClass().add("add-slide-button");
        addSlideButton.setOnAction(e -> addSlide());

        HBox buttonBox = new HBox(presentButton, addSlideButton);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.getStyleClass().add("button-box");

        VBox mainContent = new VBox(10);
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.getChildren().addAll(slideBox, carouselScroll);

        renderViewContain.getChildren().clear();
        renderViewContain.getChildren().addAll(renderCap, mainContent, buttonBox);

//        curSlideIndex = 1;

        Platform.runLater(this::showTemplateChooser);

        return renderViewContain;
    }



    private static void startPresentation() {
        System.out.println("Presentation Mode Activated!"); // Placeholder
    }

    @Override
    public ReturnObject<?> request(ControllerInterface sender, String message) {
        switch (message) {
            case "GET_SLIDE_RENDERER":
                return new ReturnObject<>(create());
            case "LAYOUT_READY":
                setLayout((List<TagComponent>) c.request(this, "GET_LAYOUT").getValue());
            case "WHAT_SLIDE":
                return new ReturnObject<>(curSlideIndex);

        }
        return null;
    }
}
