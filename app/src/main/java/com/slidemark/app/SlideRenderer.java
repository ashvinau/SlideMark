package com.slidemark.app;


import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
    ScrollPane carouselScroll;
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
    private boolean isCarouselReady = false;

    List<String> slideHtmlList = new ArrayList<>();
    private List<List<TagComponent>> allSlides = new ArrayList<>();
    private int carouselStartIndex = 0;
    private final int windowSize = 4;


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

       isCarouselReady = false;
       updateCarousel();

        //System.out.println(finalHtml);
    }


    // THUMBNAIL CAROUSEL FUNCTIONS
    public void updateCarousel() {
        if (isCarouselReady) return;

        slideCarousel.getChildren().clear();
        slideHtmlList.clear();

        int maxSlides = 100;
        int emptySlideStreak = 0;
        int maxEmptyAllowed = 1;

        for (int i = 1; i <= maxSlides; i++) {
            curSlideIndex = i;
            c.request(this, "SET_SLIDE_NUM");
            List<TagComponent> layout = (List<TagComponent>) c.request(this, "GET_LAYOUT").getValue();

            if (layout == null || layout.isEmpty()) {
                emptySlideStreak++;
                if (emptySlideStreak >= maxEmptyAllowed) break;
                else continue;
            }

            emptySlideStreak = 0;
            this.list = layout;

            String template = getTemplate();
            String templateHtml = loadTemplate(template);
            if (templateHtml == null) continue;

            String finalHtml = buildSlideHtml(layout, templateHtml);
            slideHtmlList.add(finalHtml);

            WebView thumb = new WebView();
            thumb.setPrefSize(160, 90);
            thumb.setZoom(0.08);
            thumb.getEngine().loadContent(finalHtml);

            StackPane wrapper = new StackPane(thumb);
            wrapper.getStyleClass().add("thumbnail-wrapper");

            // controls the mouse clicks on thumbnail.
            final int slideNumber = i;
            wrapper.setOnMouseClicked(e -> {
                curSlideIndex = slideNumber;
                c.request(this, "SET_SLIDE_NUM");
                c.request(this, "PROCESS_SOURCE");
            });

            Label label = new Label(String.valueOf(slideNumber));
            label.getStyleClass().add("thumbnail-label");
            StackPane.setAlignment(label, Pos.TOP_LEFT);
            wrapper.getChildren().add(label);

            slideCarousel.getChildren().add(wrapper);
        }

        curSlideIndex = 1;
        c.request(this, "SET_SLIDE_NUM");

        isCarouselReady = true;
    }





    private String buildSlideHtml(List<TagComponent> layout, String templateHtml) {
        List<StringBuilder> builders = new ArrayList<>();
        builders.add(new StringBuilder()); // Start first section

        int sectionCount = 1;

        for (TagComponent comp : layout) {
            if ("sectChange".equals(comp.getTag())) {
                builders.add(new StringBuilder());
                sectionCount++;
            } else if (comp.getVisible()) {
                StringBuilder sb = builders.get(builders.size() - 1);
                sb.append("<")
                        .append(comp.getTag());

                if (comp.getParams() != null && !comp.getParams().isBlank()) {
                    sb.append(" ").append(comp.getParams().trim());
                }

                sb.append(">");
                if (!List.of("pre", "/pre", "nextSlide", "sectChange", "ul", "/ul", "ol", "/ol", "table", "/table", "img", "hr")
                        .contains(comp.getTag())) {
                    sb.append(comp.getContent())
                            .append("</").append(comp.getTag()).append(">");
                }
            }
        }

        // Replace placeholders in the template
        for (int i = 1; i <= sectionCount; i++) {
            String placeholder = "{{Content" + i + "}}";
            String content = (i - 1 < builders.size()) ? builders.get(i - 1).toString() : "<p>(Empty Section)</p>";
            templateHtml = templateHtml.replace(placeholder, content);
        }

        return templateHtml;
    }



    // BUTTON FUNCTIONS

    public void addSlide() {
        c.request(this, "NEW_SLIDE");
        updateCarousel();
    }

    public void nextSlide() {
        if (curSlideIndex < slideHtmlList.size()) {
            curSlideIndex++;
            c.request(this, "SET_SLIDE_NUM");
            c.request(this, "PROCESS_SOURCE");
        }
        scrollToCurrentThumbnail();

    }

    public void prevSlide() {
        if (curSlideIndex > 1) {
            curSlideIndex--;
            c.request(this, "SET_SLIDE_NUM");
            c.request(this, "PROCESS_SOURCE");
        }
        scrollToCurrentThumbnail();
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

    private void scrollToCurrentThumbnail() {
        Platform.runLater(() -> {
            int targetIndex = curSlideIndex - 1;
            if (targetIndex >= 0 && targetIndex < slideCarousel.getChildren().size()) {
                Node thumb = slideCarousel.getChildren().get(targetIndex);

                double thumbX = thumb.getLayoutX();
                double thumbWidth = thumb.getBoundsInParent().getWidth();
                double scrollWidth = slideCarousel.getWidth();

                double scrollTarget = thumbX + (thumbWidth / 2.0) - (carouselScroll.getViewportBounds().getWidth() / 2.0);
                double maxScroll = scrollWidth - carouselScroll.getViewportBounds().getWidth();
                double hValue = Math.max(0, Math.min(scrollTarget / maxScroll, 1));

                carouselScroll.setHvalue(hValue);
            }
        });
    }


    // RENDERER PANEL
    public VBox create() {
        // MAIN CONTAINER
        renderViewContain.getStyleClass().add("render-view-contain");
        renderViewContain.setAlignment(Pos.TOP_CENTER);

        Label renderCap = new Label("Slide View");
        renderCap.getStyleClass().add("render-cap");

        // LIVE VIEW SETUP
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

        // CAROUSEL SETUP
        slideCarousel = new HBox();
        slideCarousel.getStyleClass().add("slide-carousel");
        slideCarousel.setSpacing(10);

        carouselScroll = new ScrollPane(slideCarousel); // 👈 make this a class field
        carouselScroll.setFitToHeight(true);
        carouselScroll.setPrefHeight(100);
        carouselScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        carouselScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        carouselScroll.setPannable(true);
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

        // BUTTONS SETUP
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

        // PANEL INIT
        VBox mainContent = new VBox(10, slideBox, carouselWrapper);
        mainContent.setAlignment(Pos.TOP_CENTER);
        renderViewContain.getChildren().setAll(renderCap, mainContent, buttonBox);
        carouselScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);


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
                updateCarousel();
            case "WHAT_SLIDE":
                return new ReturnObject<>(curSlideIndex);

        }
        return null;
    }
}