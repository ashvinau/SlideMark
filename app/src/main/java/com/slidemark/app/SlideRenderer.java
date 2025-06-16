package com.slidemark.app;


import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
    private String finalHtml;
    private String currTemplate;
    private int currNumSect;
    private int curSlideIndex = 1;
    private static final Pattern CONTENT_PLACEHOLDER =
            Pattern.compile("\\{\\{Content(\\d+)\\}\\}");
    double zoomFactor = 0;
    private int currCaroIndex;
    String workDir = "";

    private List<String> templates = Arrays.asList(
            "templates/double-vertical.html",
            "templates/double-single-vertical.html",
            "templates/double-single-horizontal.html",
            "templates/double-horizontal.html",
            "templates/four-by-two.html",
            "templates/single-center.html",
            "templates/single-double-vertical.html",
            "templates/single-double-horizontal.html",
            "templates/single-left.html",
            "templates/single-right.html",
            "templates/three-by-two.html",
            "templates/triple-vertical.html",
            "templates/triple-horizontal.html",
            "templates/two-by-three.html",
            "templates/two-by-two.html"
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

    public String getTemplate() {
        String path = "templates/";
        String extension = ".html";
        String def = path + "single-center" + extension;
        String filename;

        if (list != null) {// Grabs the last
            filename = list.get(list.size() - 1).getContent();
            filename = path + filename + extension;
        } else {
            filename = "";
        }

        if (templates.contains(filename)) {
            return filename;
        } else {
            return def;
        }

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
       if(list.isEmpty()){
           return;
       }
        currTemplate = getTemplate();
        String templateHtml = loadTemplate(currTemplate);
        if (templateHtml == null) {
            engine.loadContent("<html><body><h2>Template not found.</h2></body></html>");
            return;
        }

        List<StringBuilder> builders = new ArrayList<>();
        builders.add(new StringBuilder()); // start first section

        System.out.println("Last element: " + list.get(list.size() - 1));
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
                    List<String> doNotCap = Arrays.asList("pre", "/pre", "nextSlide", "sectChange", "ul",
                            "/ul", "ol", "/ol", "table", "/table", "img");
                    // These tags will be manually capped by the parser
                    if (doNotCap.contains(comp.getTag())) {
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

        finalHtml = templateHtml;

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

        engine.loadContent(finalHtml);
        zoomFactor = renderViewContain.widthProperty().getValue() / 1920;
        slideContent.setZoom(zoomFactor);
        System.out.println("zoom factor: " + zoomFactor);
        c.request(this, "SET_SLIDE_NUMS");

        //System.out.println(finalHtml);
    }

    public void addSlide() {

    }


    private void updateCarousel() {
        slideCarousel.getChildren().clear();

        /*int maxThumbnails = 4;
        List<Integer> keys = new ArrayList<>(slideThumbnails.keySet());
        Collections.sort(keys);

        int startKeyIndex = 0;
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i) >= Math.max(1, curSlideIndex - 1)) {
                startKeyIndex = i;
                break;
            }
        }
        for (int i = startKeyIndex; i < Math.min(startKeyIndex + maxThumbnails, keys.size()); i++) {
            int slideNum = keys.get(i);
            WritableImage snapshot = slideThumbnails.get(slideNum);

            if (snapshot != null) {
                ImageView thumbnail = new ImageView(snapshot);
                thumbnail.setFitWidth(200);
                thumbnail.setFitHeight(120);
                thumbnail.setPreserveRatio(true);
                slideCarousel.getChildren().add(thumbnail);
            } else {
                Label placeholder = new Label("Slide " + slideNum);
                placeholder.setPrefSize(200, 120);
                placeholder.setStyle("-fx-border-color: gray; -fx-alignment: center;");
                slideCarousel.getChildren().add(placeholder);
            }
        }*/
    }

    public void nextSlide(){
        curSlideIndex++;
        c.request(this, "SET_SLIDE_NUM");
        c.request(this, "PROCESS_SOURCE");
    }

    public void prevSlide(){
        curSlideIndex--;
        c.request(this, "SET_SLIDE_NUM");
        c.request(this, "PROCESS_SOURCE");
    }

    private void startPresentation() {
        Stage present = new Stage();
        present.initStyle(StageStyle.UNDECORATED);
        WebView slidePres = new WebView();
        slidePres.getEngine().loadContent(finalHtml);

        StackPane presWrapper = new StackPane(slidePres);
        presWrapper.prefWidthProperty().bind(present.widthProperty());
        presWrapper.prefHeightProperty().bind(present.heightProperty());

        Scene newScene = new Scene(presWrapper);
        present.setScene(newScene);
        present.setFullScreen(true);
        present.show();

        newScene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    present.close();
                    break;
                case LEFT:
                    prevSlide();
                    slidePres.getEngine().loadContent(finalHtml);
                    break;
                case RIGHT:
                    nextSlide();
                    slidePres.getEngine().loadContent(finalHtml);
                    break;
                default:
                    break;
            }
        });

        newScene.setOnMouseClicked(event -> {
            switch (event.getButton()) {
                case PRIMARY:
                    nextSlide();
                    slidePres.getEngine().loadContent(finalHtml);
                    break;
                case SECONDARY:
                    prevSlide();
                    slidePres.getEngine().loadContent(finalHtml);
                    break;
                default:
                    break;
            }
        });
        //SnapshotParameters params = new SnapshotParameters();



    }


    public VBox create() {
        // Outer container
        renderViewContain.getStyleClass().add("render-view-contain");
        renderViewContain.setAlignment(Pos.TOP_CENTER);

        Label renderCap = new Label("Slide View");
        renderCap.getStyleClass().add("render-cap");

        // Main slide WebView
        slideContent.setContextMenuEnabled(false);
        slideContent.getStyleClass().add("slide-webview");

        StackPane slideContentWrapper = new StackPane(slideContent);
        slideContentWrapper.setAlignment(Pos.CENTER);
        slideContentWrapper.getStyleClass().add("slide-content");

        slideBox = new StackPane(slideContentWrapper);
        slideBox.getStyleClass().add("slide-box");

        // Responsive sizing
        slideContent.prefWidthProperty().bind(renderViewContain.widthProperty());
        slideContent.prefHeightProperty().bind(renderViewContain.widthProperty().multiply(0.56));
        slideContent.maxWidthProperty().bind(renderViewContain.widthProperty());
        slideContent.maxHeightProperty().bind(renderViewContain.widthProperty().multiply(0.56));

        zoomFactor = renderViewContain.widthProperty().getValue() / 1920;
        slideContent.setZoom(zoomFactor);

        slideContentWrapper.prefWidthProperty().bind(renderViewContain.widthProperty());
        slideContentWrapper.prefHeightProperty().bind(renderViewContain.widthProperty().multiply(0.56));

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(slideContentWrapper.widthProperty());
        clip.heightProperty().bind(slideContentWrapper.heightProperty());
        slideContentWrapper.setClip(clip);

        slideBox.prefWidthProperty().bind(renderViewContain.widthProperty());
        slideBox.prefHeightProperty().bind(renderViewContain.widthProperty().multiply(0.56));

        // Slide carousel (initialized but populated later)
        slideCarousel = new HBox();
        slideCarousel.getStyleClass().add("slide-carousel");
        slideCarousel.setSpacing(10);

        ScrollPane carouselScroll = new ScrollPane(slideCarousel);
        carouselScroll.setFitToWidth(true);
        carouselScroll.setPrefHeight(100);
        carouselScroll.getStyleClass().add("carousel-scroll");

        // Buttons
        Button presentButton = new Button("Presentation Mode");
        presentButton.getStyleClass().add("present-button");
        presentButton.setOnAction(e -> startPresentation());

        Button addSlideButton = new Button("Add Slide");
        addSlideButton.getStyleClass().add("add-slide-button");
        addSlideButton.setOnAction(e -> addSlide());

        Button nextSlideButton = new Button("Next");
        nextSlideButton.getStyleClass().add("next-slide-button");
        nextSlideButton.setOnAction(e -> nextSlide());

        Button prevSlideButton = new Button("Prev");
        prevSlideButton.getStyleClass().add("prev-slide-button");
        prevSlideButton.setOnAction(e -> prevSlide());

        HBox buttonBox = new HBox(presentButton, addSlideButton, nextSlideButton, prevSlideButton);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.getStyleClass().add("button-box");

        VBox mainContent = new VBox(10, slideBox, carouselScroll);
        mainContent.setAlignment(Pos.TOP_CENTER);

        renderViewContain.getChildren().setAll(renderCap, mainContent, buttonBox);

        Platform.runLater(this::updateCarousel);

        return renderViewContain;
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