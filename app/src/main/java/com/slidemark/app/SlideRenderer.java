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

import javax.swing.text.html.ImageView;
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
    private List<List<TagComponent>> allSlidesContent;
    private String outputHTML;
    private Point size;
    private String currTemplate;
    private int currNumSect;
    private int curSlideIndex = 1;
    private static final Pattern CONTENT_PLACEHOLDER =
            Pattern.compile("\\{\\{Content(\\d+)\\}\\}");
    private ImageView thumbnailView;
    private final double THUMBNAIL_WIDTH = 200;
    private final double THUMBNAIL_HEIGHT = 112.5;


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
            htmlText = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            ;
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
        updateCarousel();
        engine.loadContent(finalHtml);
    }


    public void advanceSlideState() {
        curSlideIndex++;
        System.out.println("Renderer: curSlideIndex incremented to: " + curSlideIndex);
        c.request(this, "LAYOUT_READY");
    }

    public void addSlide() {
        c.request(this, "INSERT_DELIMITER_INTO_EDITOR");

    }

    private void updateCarousel() {
        List<List<TagComponent>> totalSlides = (List<List<TagComponent>>) c.request(this, "SEND_ALL_LAYOUTS").getValue();

        slideCarousel.getChildren().clear();

        if (totalSlides == null || totalSlides.isEmpty()) {
            System.out.println("Renderer: No slides content available for carousel.");
            return;
        }

        String templateHtml = loadTemplate(currTemplate);
        if (templateHtml == null) {
            System.err.println("Renderer: Template not loaded for thumbnail generation.");
            return;
        }

        for (int i = 0; i < totalSlides.size(); i++) {
            List<TagComponent> singleSlideData = totalSlides.get(i);

            // Redoing logic for updateSlide -> Thumbnails. Not the best Optimization but seems to show live previews.
            List<StringBuilder> builders = new ArrayList<>();
            builders.add(new StringBuilder());

            if (singleSlideData != null && !singleSlideData.isEmpty()) {
                for (TagComponent comp : singleSlideData) {
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
                        if (comp.getTag().equals("pre") ||
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

            String slideHtml = templateHtml;

            for (int j = 1; j <= currNumSect; j++) {
                String placeholder = "{{Content" + j + "}}";
                String content;
                if (j - 1 < builders.size()) {
                    content = builders.get(j - 1).toString();
                } else {
                    content = "<p>(Empty Section)</p>";
                }
                slideHtml = slideHtml.replace(placeholder, content);
            }

            WebView thumbnailWebView = new WebView();
            thumbnailWebView.setPrefSize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
            thumbnailWebView.setMaxSize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
            thumbnailWebView.setContextMenuEnabled(false);

            thumbnailWebView.getEngine().loadContent(slideHtml);

            // Listener to apply CSS zoom after content is loaded, enabling live updates
            thumbnailWebView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == javafx.concurrent.Worker.State.SUCCEEDED) {
                    org.w3c.dom.Document doc = thumbnailWebView.getEngine().getDocument();
                    if (doc != null) {
                        // Injected CSS to have the thumbnails keep their size.
                        String script = "var style = document.createElement('style');" +
                                "style.innerHTML = 'body { zoom: " + (THUMBNAIL_WIDTH / 780.0) + "; transform-origin: top left; }';" +
                                "document.head.appendChild(style);";
                        thumbnailWebView.getEngine().executeScript(script);
                    }
                }
            });

            //Label for slides to show what number they are.
            Label slideNumberLabel = new Label(String.valueOf(i + 1));
            slideNumberLabel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);" + "-fx-text-fill: white;" + "-fx-padding: 2 5 2 5;" + "-fx-font-size: 10px;" + "-fx-font-weight: bold;");
            StackPane thumbnailWrapper = new StackPane(thumbnailWebView, slideNumberLabel);
            StackPane.setAlignment(slideNumberLabel, Pos.TOP_LEFT);

            //Thumbnail Wrapper that keeps the thumbnails together.
            thumbnailWrapper.getStyleClass().add("thumbnail-wrapper");
            thumbnailWrapper.setClip(new Rectangle(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT));

            if ((i + 1) == curSlideIndex) {
                thumbnailWrapper.getStyleClass().add("thumbnail-selected");
            }

            final int slideIndex = i + 1;
            thumbnailWrapper.setOnMouseClicked(event -> {
                curSlideIndex = slideIndex;
                c.request(this, "LAYOUT_READY");
            });

            slideCarousel.getChildren().add(thumbnailWrapper);
        }
    }


    public VBox create() {
        // Container for the entire renderer.
        renderViewContain.setPrefWidth(1000);
        renderViewContain.setSpacing(5);
        renderViewContain.getStyleClass().add("render-view-contain");
        renderViewContain.setAlignment(Pos.TOP_CENTER);

        //Label that holds the name for the Slide View
        Label renderCap = new Label("Slide View");
        renderCap.getStyleClass().add("render-cap");

        //Container that holds the Slide Content inside the slide.
        slideContent.setPrefSize(780, 380);
        slideContent.setContextMenuEnabled(false);
        slideContent.getStyleClass().add("slide-webview");

        //Wrapper that contains the Slide "board" that keeps it one space.
        StackPane slideContentWrapper = new StackPane(slideContent);
        slideContentWrapper.setAlignment(Pos.CENTER);
        slideContentWrapper.getStyleClass().add("slide-content");
        slideContentWrapper.setPrefSize(780, 380);
        slideBox = new StackPane(slideContentWrapper);
        slideBox.getStyleClass().add("slide-box");

        //Slide Carousel Styling
        slideCarousel.getStyleClass().add("slide-carousel");
        slideCarousel.setSpacing(10);

        ScrollPane carouselScroll = new ScrollPane(slideCarousel);
        carouselScroll.setPrefHeight(THUMBNAIL_HEIGHT + 30.0);
        carouselScroll.setPrefWidth(renderViewContain.getPrefWidth());
        carouselScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        carouselScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        carouselScroll.getStyleClass().add("carousel-scroll");

        //Presentation Button
        Button presentButton = new Button("Presentation Mode");
        presentButton.getStyleClass().add("present-button");
        presentButton.setOnAction(e -> startPresentation());

        //Add Slide Button
        Button addSlideButton = new Button("Add Slide");
        addSlideButton.getStyleClass().add("add-slide-button");
        addSlideButton.setOnAction(e -> addSlide());

        //Container for buttons and Conainer that holds everything.
        HBox buttonBox = new HBox(presentButton, addSlideButton);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.getStyleClass().add("button-box");

        VBox mainContent = new VBox(10);
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setMaxWidth(Double.MAX_VALUE);
        mainContent.getChildren().addAll(slideBox, carouselScroll);

        renderViewContain.getChildren().clear();
        renderViewContain.getChildren().addAll(renderCap, mainContent, buttonBox);

        Platform.runLater(this::showTemplateChooser);

        return renderViewContain;
    }
    public void startPresentation(){

    }

    @Override
    public ReturnObject<?> request(ControllerInterface sender, String message) {
        switch (message) {
            case "GET_SLIDE_RENDERER":
                return new ReturnObject<>(create());
            case "LAYOUT_READY":
                setLayout((List<TagComponent>) c.request(this, "GET_LAYOUT").getValue());
                break;
            case "WHAT_SLIDE":
                return new ReturnObject<>(curSlideIndex);
            case "SET_CUR_SLIDE":
                advanceSlideState();
                break;
        }
        return null;
    }
}