package com.slidemark.app;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.awt.*;

import java.util.ArrayList;
import java.util.List;

public class SlideRenderer implements ControllerInterface {
    private ControllerInterface c;
    private static List<WebView> slides = new ArrayList<>();
    private static int currSlideIndex = 0;
    private static VBox renderViewContain = new VBox();
    private static WebView slideContent = new WebView();
    private static WebEngine engine = slideContent.getEngine();
    private static HBox slideCarousel = new HBox();

    private List<TagComponent> list;
    private String outputHTML;
    private Point size;

    public SlideRenderer(ControllerInterface c) {
        this.c = c;
    }

    public void setLayout(List<TagComponent> tagComponents) {
        this.list = tagComponents;
        updateSlideView();
    }

    public List<TagComponent> getLayouts() {
        return this.list;

    }

    public void updateSlideView() {
        if (list == null || list.isEmpty()) {
            engine.loadContent("<html><body><h2>No content to display.</h2></body></html>");
            return;
        }

        StringBuilder html = new StringBuilder("<html><body style='font-family:sans-serif;'>");

        for (TagComponent comp : list) {
            if (!comp.getVisible()) continue;

            html.append("<").append(comp.getTag());
            if (!comp.getParams().isEmpty()) {
                html.append(" ").append(comp.getParams());
            }
            html.append(">")
                    .append(comp.getContent())
                    .append("</").append(comp.getTag()).append(">");
        }

        html.append("</body></html>");
        engine.loadContent(html.toString());
    }


    public static void renderSlide() {
        slideContent.getChildrenUnmodifiable().clear();

        VBox slideWrapper = new VBox();
        slideWrapper.setAlignment(Pos.CENTER);
        slideWrapper.setPrefSize(800, 400);
        slideWrapper.setStyle("-fx-background-color: white; -fx-border-color: #888888; -fx-padding: 20;");

        Text slideText;
        if (slides.isEmpty()) {
            slideText = new Text("No slides available.");
        } else {
            String currentSlideText = String.valueOf(slides.get(currSlideIndex));
            slideText = new Text(currentSlideText);
        }

        slideText.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 16;");
        slideText.setTextAlignment(TextAlignment.CENTER);

        slideWrapper.getChildren().add(slideText);
        slideContent.getChildrenUnmodifiable().add(slideWrapper);
    }

    public boolean generateHTML() {
        if (list == null) return false;

        StringBuilder html = new StringBuilder("<html><body>\n");

        for (TagComponent comp : list) {
            if (!comp.getVisible()) continue;

            html.append("<").append(comp.getTag());
            if (!comp.getParams().isEmpty()) {
                html.append(" ").append(comp.getParams());
            }
            html.append(">")
                    .append(comp.getContent())
                    .append("</").append(comp.getTag()).append(">\n");
        }

        html.append("</body></html>");
        outputHTML = html.toString();
        return true;
    }

    public String getHTML(String html) {
        return outputHTML;
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

        // Optional: clip content to bounds
        slideContentWrapper.setClip(new javafx.scene.shape.Rectangle(780, 380));

        StackPane slideBox = new StackPane(slideContentWrapper);
        slideBox.getStyleClass().add("slide-box");
        slideBox.setPrefSize(800, 400);

        // Carousel section
        slideCarousel.getStyleClass().add("slide-carousel");
        slideCarousel.setSpacing(10);
        populateCarousel();

        ScrollPane carouselScroll = new ScrollPane(slideCarousel);
        carouselScroll.setFitToWidth(true);
        carouselScroll.setPrefHeight(100);
        carouselScroll.getStyleClass().add("carousel-scroll");

        Button presentButton = new Button("Presentation Mode");
        presentButton.getStyleClass().add("present-button");
        presentButton.setOnAction(e -> startPresentation());

        HBox buttonBox = new HBox(presentButton);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.getStyleClass().add("button-box");

        VBox mainContent = new VBox(10);
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.getChildren().addAll(slideBox, carouselScroll);

        renderViewContain.getChildren().clear();
        renderViewContain.getChildren().addAll(renderCap, mainContent, buttonBox);


        updateSlideView();

        return renderViewContain;
    }





    private static void populateCarousel() {
        for (int i = 0; i < slides.size(); i++) {
            Label slideThumbnail = new Label("Slide " + (i + 1));
            slideThumbnail.setStyle("-fx-background-color: white; -fx-border-color: #888888; -fx-padding: 5;");
            slideThumbnail.setPrefSize(120, 80);
            slideCarousel.getChildren().add(slideThumbnail);
        }
    }

    private static void startPresentation() {
        System.out.println("Presentation Mode Activated!"); // Placeholder for actual fullscreen logic
    }


    @Override
    public ReturnObject<?> request(ControllerInterface sender, String message) {
        switch (message) {
            case "GET_SLIDE_RENDERER":
                return new ReturnObject<>(create());

            case "LAYOUT_READY":
                setLayout((List<TagComponent>) c.request(this, "GET_LAYOUT").getValue());


        }
        return null;
    }
}