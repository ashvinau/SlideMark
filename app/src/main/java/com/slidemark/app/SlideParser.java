package com.slidemark.app;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;

public class SlideParser implements ControllerInterface {
    private ControllerInterface c;
    private String sourceText;
    private List<TagComponent> layoutObjects;

    public SlideParser(ControllerInterface newC) {
        if (newC != null)
            c = newC;
        sourceText = "";
        layoutObjects = new ArrayList<>();
    }

    /**
     * Splits the input into markdown token and the rest of the content.
     *
     * @param input  the string to split
     * @return       a two‐element array where [0] is the first word and [1] is the rest of the text
     */
    public static String[] getLineToken(String input) {
        if (input == null) {
            return new String[] { "", "" }; }

        String firstWord = "";
        String remainder = "";

        // Trim off leading/trailing whitespace
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return new String[] { "", "" };
        }

        // Find the first whitespace
        int splitPos = trimmed.indexOf(' ');

        if (splitPos < 0) { // Entire string is a single word, assuming no token
            firstWord = "";
            remainder = trimmed;
        } else {
            firstWord = trimmed.substring(0, splitPos);
            remainder = trimmed.substring(splitPos + 1).trim();
        }

        return new String[] { firstWord, remainder };
    }

    private TagComponent processToken(String token, String data) {
        TagComponent returnComponent = new TagComponent();
        System.out.println("Token: -" + token + "-");
        switch (token) {
            case "#":
                returnComponent.setTag("h1");
                break;
            case "##":
                returnComponent.setTag("h2");
                break;
            case "###":
                returnComponent.setTag("h3");
                break;
            case "####":
                returnComponent.setTag("h4");
                break;
            case "#####":
                returnComponent.setTag("h5");
                break;
            default:
                returnComponent.setTag("");
                break;
        }
        returnComponent.setContent(data);
        System.out.println("Created " + returnComponent);
        return returnComponent;
    }

    private List<TagComponent> parse() {
        System.out.println("Parser: Starting parse...");
        System.out.println(sourceText);
        layoutObjects.clear();
        try (BufferedReader input = new BufferedReader(new StringReader(sourceText))) {
            String curLine;
            while ((curLine = input.readLine()) != null) {
                String[] tokenString = getLineToken(curLine);
                layoutObjects.add(processToken(tokenString[0],tokenString[1]));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return layoutObjects;
    }

    public ReturnObject<?> request(ControllerInterface sender, String message) {
        System.out.println("");
        List<TagComponent> returnList = new ReturnObject<>(parse()).getValue();
        switch (message) {
            case "PROCESS_SOURCE":
                sourceText = (String) c.request(this, "GET_CONTENT").getValue();
                c.request(this, "LAYOUT_READY");
            case "GET_LAYOUT":
                return new ReturnObject<>(returnList);
        }
        return null; // No return data here.
    }
}
