package com.slidemark.app;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import java.awt.*;

import java.util.ArrayList;
import java.util.List;

public class SlideRenderer implements ControllerInterface {
    private ControllerInterface c;
    private static List<String> slides = new ArrayList<>(); // TODO: Change to WebView later
    private static int currSlideIndex = 0;
    private static VBox renderViewContain = new VBox();
    private static TextFlow slideContent = new TextFlow();
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
        if (slideContent.getChildren().isEmpty()) {
            // If for some reason slideTemplate missing, just return or handle error
            return;
        }

        VBox slideTemplate = (VBox) slideContent.getChildren().get(0);
        slideTemplate.getChildren().clear();

        if (list == null || list.isEmpty()) {
            slideTemplate.getChildren().add(new Text("No content to display."));
            return;
        }

        for (TagComponent comp : list) {
            if (!comp.getVisible()) continue;

            String tag = comp.getTag().toLowerCase();
            String content = comp.getContent();

            Text textNode = new Text();

            switch (tag) {
                case "h1":
                    textNode.setText(content + "\n\n");
                    textNode.setStyle("-fx-font-size: 30; -fx-font-weight: bold;");
                    break;
                case "h2":
                    textNode.setText(content + "\n\n");
                    textNode.setStyle("-fx-font-size: 25; -fx-font-weight: bold;");
                    break;
                case "h3":
                    textNode.setText(content + "\n\n");
                    textNode.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
                    break;
                case "h4":
                    textNode.setText(content + "\n\n");
                    textNode.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
                    break;
                case "h5":
                    textNode.setText(content + "\n\n");
                    textNode.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
                    break;
                case "p":
                    textNode.setText(content + "\n\n");
                    textNode.setStyle("-fx-font-size: 12;");
                    break;
                default:
                    textNode.setText(content + "\n\n");
                    break;
            }

            slideTemplate.getChildren().add(textNode);
        }
    }


    public static void renderSlide() {
        slideContent.getChildren().clear();

        VBox slideWrapper = new VBox();
        slideWrapper.setAlignment(Pos.CENTER);
        slideWrapper.setPrefSize(800, 400);
        slideWrapper.setStyle("-fx-background-color: white; -fx-border-color: #888888; -fx-padding: 20;");

        Text slideText;
        if (slides.isEmpty()) {
            slideText = new Text("No slides available.");
        } else {
            String currentSlideText = slides.get(currSlideIndex);
            slideText = new Text(currentSlideText);
        }

        slideText.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 16;");
        slideText.setTextAlignment(TextAlignment.CENTER);

        slideWrapper.getChildren().add(slideText);
        slideContent.getChildren().add(slideWrapper);
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


    static {
        // Placeholder slides
        slides.add("# Slide 1\nThis is the first slide.");
        slides.add("# Slide 2\nAnother slide example.");
        slides.add("# Slide 3\nMarkdown will be rendered here.");
    }

    public VBox create() {

        renderViewContain.setPrefWidth(1000);
        renderViewContain.setStyle("-fx-background-color: #eeeeee; -fx-padding: 10;");
        renderViewContain.setAlignment(Pos.TOP_CENTER);

        Label renderCap = new Label("Slide View");
        renderCap.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");



        slideContent.getChildren().clear();


        VBox slideTemplate = new VBox(10);
        slideTemplate.setPrefSize(780, 380);
        slideTemplate.setStyle("-fx-background-color: white; -fx-border-color: #444444; -fx-padding: 20;");

        Text placeholder = new Text("No content to display.");
        placeholder.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14;");
        placeholder.setTextAlignment(TextAlignment.CENTER);
        slideTemplate.getChildren().add(placeholder);


        slideContent.getChildren().add(slideTemplate);


        StackPane slideBox = new StackPane(slideContent);
        slideBox.setStyle("-fx-background-color: white; -fx-border-color: #888888; -fx-padding: 10;");
        slideBox.setPrefSize(800, 400);


        slideCarousel.setStyle("-fx-background-color: #888888; -fx-padding: 5;");
        slideCarousel.setSpacing(10);
        populateCarousel();

        ScrollPane carouselScroll = new ScrollPane(slideCarousel);
        carouselScroll.setFitToWidth(true);
        carouselScroll.setStyle("-fx-background-color: transparent;");
        carouselScroll.setPrefHeight(100);  // Give a fixed height so it doesn't collapse

        Button presentButton = new Button("Presentation Mode");
        presentButton.setStyle("-fx-font-size: 14; -fx-padding: 10;");
        presentButton.setOnAction(e -> startPresentation());

        HBox buttonBox = new HBox(presentButton);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.setStyle("-fx-padding: 10;");


        renderViewContain.getChildren().clear();
        renderViewContain.getChildren().addAll(renderCap, slideBox, carouselScroll, buttonBox);

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