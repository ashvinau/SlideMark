package com.slidemark.app;


import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    List<String> slideHtmlList = new ArrayList<>();
    private List<List<TagComponent>> allSlides = new ArrayList<>();

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
        updateSlideView();
        updateCarousel();
    }



    // LIVEVIEW FUNCTIONS

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
                            "/ul", "ol", "/ol", "table", "/table", "img", "hr");
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

//        System.out.println("# Sections processed: " + builders.size());
//        System.out.println("Num of sections for current template: " + currNumSect);

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

        if (curSlideIndex - 1 < slideHtmlList.size()) {
            slideHtmlList.set(curSlideIndex - 1, finalHtml);
        } else {
            slideHtmlList.add(finalHtml);
        }

        //System.out.println(finalHtml);
    }


    // THUMBNAIL CAROUSEL FUNCTIONS
    private void updateCarousel() {
        Platform.runLater(() -> {
            slideCarousel.getChildren().clear();
            allSlides = (List<List<TagComponent>>) c.request(this, "GET_ALL_SLIDES").getValue();
            slideHtmlList.clear();
            Set<String> seenHtmls = new HashSet<>();

            //Sorts through the HTML list to prevent duplicates
            for (List<TagComponent> slideComponents : allSlides) {
                String html = generateHtml(slideComponents);
                if (seenHtmls.add(html)) {
                    slideHtmlList.add(html);
                } else {
                    System.out.println("Duplicate slide skipped.");
                }
            }

            int totalSlides = slideHtmlList.size();
            curSlideIndex = Math.max(1, Math.min(curSlideIndex, totalSlides));
            int start = Math.max(0, Math.min(curSlideIndex - 2, Math.max(0, totalSlides - 4)));
            int end = Math.min(start + 4, totalSlides);

            List<StackPane> thumbnailNodes = new ArrayList<>();

            for (int i = start; i < end; i++) {
                final int finalIndex = i + 1;
                final String html = slideHtmlList.get(i);

                WebView thumb = new WebView();
                thumb.setPrefSize(160, 90);
                thumb.setZoom(0.08);

                StackPane wrapper = new StackPane();
                wrapper.getStyleClass().add("thumbnail-wrapper");

                if (finalIndex == curSlideIndex) {
                    wrapper.getStyleClass().add("current-slide");
                }

                Label label = new Label(String.valueOf(finalIndex));
                label.getStyleClass().add("thumbnail-label");
                StackPane.setAlignment(label, Pos.TOP_LEFT);

                wrapper.getChildren().addAll(thumb, label);
                wrapper.setOnMouseClicked(e -> {
                    if (finalIndex <= slideHtmlList.size()) {
                        curSlideIndex = finalIndex;
                        c.request(this, "SET_SLIDE_NUM");
                        c.request(this, "PROCESS_SOURCE");
                        updateCarousel();
                    } else {
                        System.out.println("Thumbnail index out of bounds: ");
                    }
                });

                thumb.getEngine().loadContent(html);
                thumbnailNodes.add(wrapper);
            }

            slideCarousel.getChildren().addAll(thumbnailNodes);
        });
    }

    // This method is JUST for the carousel's live view.
    private String generateHtml(List<TagComponent> components) {
        String templateHtml = loadTemplate(getTemplateFromComponents(components));
        if (templateHtml == null) return "<html><body><h2>Template not found.</h2></body></html>";

        List<StringBuilder> builders = new ArrayList<>();
        builders.add(new StringBuilder());

        for (TagComponent comp : components) {
            if ("sectChange".equals(comp.getTag())) {
                builders.add(new StringBuilder());
            } else if (comp.getVisible()) {
                StringBuilder sb = builders.get(builders.size() - 1);
                sb.append("<").append(comp.getTag());
                if (comp.getParams() != null && !comp.getParams().isBlank()) {
                    sb.append(" ").append(comp.getParams().trim());
                }
                sb.append(">");
                List<String> doNotCap = Arrays.asList("pre", "/pre", "nextSlide", "sectChange", "ul",
                        "/ul", "ol", "/ol", "table", "/table", "img", "hr");
                if (!doNotCap.contains(comp.getTag())) {
                    sb.append(comp.getContent()).append("</").append(comp.getTag()).append(">");
                }
            }
        }

        for (int i = 1; i <= currNumSect; i++) {
            String placeholder = "{{Content" + i + "}}";
            String content = (i - 1 < builders.size()) ? builders.get(i - 1).toString() : "<p>(Empty Section)</p>";
            templateHtml = templateHtml.replace(placeholder, content);
        }

        return templateHtml;
    }

    private String getTemplateFromComponents(List<TagComponent> components) {
        String path = "templates/";
        String extension = ".html";
        String def = path + "single-center" + extension;

        if (components != null && !components.isEmpty()) {
            String filename = components.get(components.size() - 1).getContent();
            filename = path + filename + extension;

            if (templates.contains(filename)) {
                return filename;
            }
        }
        return def;
    }




    // BUTTON FUNCTIONS

    public void addSlide() {
        c.request(this, "NEW_SLIDE");
        updateCarousel();
    }

    public void nextSlide() {
        int totalSlides = slideHtmlList.size();
        //If the next slide would be greater than the total amount of slide return nothing and do not go next.
        if (curSlideIndex >= totalSlides) {
            return;
        }

        curSlideIndex++;
        c.request(this, "SET_SLIDE_NUM");
        c.request(this, "PROCESS_SOURCE");
    }




    public void prevSlide() {
        //If the previous slide starts at index -1 return nothing.
        if (curSlideIndex <= 1) {
            return;
        }

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
    }


    // RENDERER PANEL
    public VBox create() {
        //MAIN CONTAINER
        renderViewContain.getStyleClass().add("render-view-contain");
        renderViewContain.setAlignment(Pos.TOP_CENTER);

        Label renderCap = new Label("Slide View");
        renderCap.getStyleClass().add("render-cap");

        // LIVE VIEW SET UP
        slideContent.setContextMenuEnabled(false);
        slideContent.getStyleClass().add("slide-webview");

        StackPane slideContentWrapper = new StackPane(slideContent);
        slideContentWrapper.setAlignment(Pos.CENTER);
        slideContentWrapper.getStyleClass().add("slide-content");

        slideBox = new StackPane(slideContentWrapper);
        slideBox.getStyleClass().add("slide-box");

        slideContent.prefWidthProperty().bind(renderViewContain.widthProperty());
        slideContent.prefHeightProperty().bind(renderViewContain.widthProperty().multiply(0.56));
        slideContent.maxWidthProperty().bind(renderViewContain.widthProperty());
        slideContent.maxHeightProperty().bind(renderViewContain.widthProperty().multiply(0.56));
        slideContentWrapper.prefWidthProperty().bind(renderViewContain.widthProperty());
        slideContentWrapper.prefHeightProperty().bind(renderViewContain.widthProperty().multiply(0.56));

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(slideContentWrapper.widthProperty());
        clip.heightProperty().bind(slideContentWrapper.heightProperty());
        slideContentWrapper.setClip(clip);
        slideBox.prefWidthProperty().bind(renderViewContain.widthProperty());
        slideBox.prefHeightProperty().bind(renderViewContain.widthProperty().multiply(0.56));

        // CAROUSEL SET UP
        slideCarousel = new HBox();
        slideCarousel.getStyleClass().add("slide-carousel");
        slideCarousel.setSpacing(10);

        ScrollPane carouselScroll = new ScrollPane(slideCarousel);
        carouselScroll.setFitToWidth(true);
        carouselScroll.setPrefHeight(100);
        carouselScroll.getStyleClass().add("carousel-scroll");

        Button prevArrow = new Button("←");
        prevArrow.getStyleClass().add("carousel-arrow");
        prevArrow.setOnAction(e -> prevSlide());

        Button nextArrow = new Button("→");
        nextArrow.getStyleClass().add("carousel-arrow");
        nextArrow.setOnAction(e -> nextSlide());

        HBox carouselWrapper = new HBox(10, prevArrow, carouselScroll, nextArrow);
        carouselWrapper.setAlignment(Pos.CENTER);
        carouselWrapper.setStyle("-fx-padding: 5;");

        // BUTTON SET UP
        Image projectorIcon = new Image(getClass().getResourceAsStream("/icons/projector.png"));
        ImageView projectorIconView = new ImageView(projectorIcon);
        projectorIconView.setFitWidth(30);
        projectorIconView.setFitHeight(30);

        Button presentButton = new Button("", projectorIconView);
        presentButton.getStyleClass().add("present-button");
        presentButton.setOnAction(e -> startPresentation());
        presentButton.setTooltip(new Tooltip("Presentation Mode"));


        Button addSlideButton = new Button("+");
        addSlideButton.getStyleClass().add("add-slide-button");
        addSlideButton.setOnAction(e -> addSlide());
        addSlideButton.setTooltip(new Tooltip("Add Slide"));

        HBox leftBox = new HBox(presentButton);
        leftBox.setAlignment(Pos.BOTTOM_LEFT);
        HBox rightBox = new HBox(addSlideButton);
        rightBox.setAlignment(Pos.BOTTOM_RIGHT);

        HBox buttonBox = new HBox(leftBox, new Region(), rightBox);
        HBox.setHgrow(buttonBox.getChildren().get(1), Priority.ALWAYS);
        buttonBox.getStyleClass().add("button-box");
        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(10));

        // PANEL INITIATED
        VBox mainContent = new VBox(10, slideBox, carouselWrapper);
        mainContent.setAlignment(Pos.TOP_CENTER);
        renderViewContain.getChildren().setAll(renderCap, mainContent, buttonBox);
        updateCarousel();
        return renderViewContain;
    }


    // CONTROLLER REQUESTS
    @Override
    public ReturnObject<?> request(ControllerInterface sender, String message) {
        switch (message) {
            case "GET_SLIDE_RENDERER":
                return new ReturnObject<>(create());
            case "LAYOUT_READY":
                // Get current/active slide only
                setLayout((List<TagComponent>) c.request(this, "GET_LAYOUT").getValue());
            case "WHAT_SLIDE":
                return new ReturnObject<>(curSlideIndex);

        }
        return null;
    }
}