package com.example.app;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

public class SlideRenderer {
    private static List<String> slides = new ArrayList<>();
    private static int currSlideIndex = 0;
    private static VBox renderViewContain = new VBox();
    private static TextFlow slideContent = new TextFlow();
    private static HBox slideCarousel = new HBox();

    static {
        // Placeholder slides
        slides.add("# Slide 1\nThis is the first slide.");
        slides.add("# Slide 2\nAnother slide example.");
        slides.add("# Slide 3\nMarkdown will be rendered here.");
    }

    public static VBox create() {
        renderViewContain.setPrefWidth(1000);
        renderViewContain.setStyle("-fx-background-color: #eeeeee; -fx-padding: 10;");

        Label renderCap = new Label("Slide View");
        renderCap.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        slideContent.getChildren().add(new Text("Markdown Rendering Placeholder"));
        slideContent.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14;");
        slideContent.setTextAlignment(TextAlignment.CENTER);

        StackPane slideBox = new StackPane(slideContent);
        slideBox.setStyle("-fx-background-color: white; -fx-border-color: #888888; -fx-padding: 20;");
        slideBox.setPrefSize(800, 400);


        slideCarousel.setStyle("-fx-background-color: #888888; -fx-padding: 5;");
        slideCarousel.setSpacing(10);
        populateCarousel();

        ScrollPane carouselScroll = new ScrollPane(slideCarousel);
        carouselScroll.setFitToWidth(true);
        carouselScroll.setStyle("-fx-background-color: transparent;");


        Button presentButton = new Button("Presentation Mode");
        presentButton.setStyle("-fx-font-size: 14; -fx-padding: 10;");
        presentButton.setOnAction(e -> startPresentation());

        HBox buttonBox = new HBox(presentButton);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.setStyle("-fx-padding: 10;");

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
}