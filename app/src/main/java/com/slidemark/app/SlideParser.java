package com.slidemark.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;

public class SlideParser implements ControllerInterface {
    private ControllerInterface c;
    private String sourceText;
    private List<TagComponent> tagObjects;
    private int tagsOpen;
    private HashMap<String, String> subTagMap;

    public SlideParser(ControllerInterface newC) {
        if (newC != null)
            c = newC;
        sourceText = "";
        tagObjects = new ArrayList<>();
        subTagMap = new HashMap<>();
        initSubTags();
    }

    private void initSubTags() {
        tagsOpen = 0;
        subTagMap.put("**", "b"); // Bold
        subTagMap.put("__", "b");
        subTagMap.put("*", "i"); // Italics
        subTagMap.put("_", "i");
    }

    /**
     * Splits the input into markdown token and the rest of the content.
     *
     * @param input  the string to split
     * @return       a two‐element array where [0] is the first word and [1] is the rest of the text
     */
   private String[] getLineToken(String input) {
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
                returnComponent.setTag("h1"); // Headers
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
            case "######":
                returnComponent.setTag("h6");
                break;
            case ">":
                returnComponent.setTag("blockquote");
                break;
            default:
                // Fall through becomes paragraphs
                returnComponent.setTag("p");
                returnComponent.setContent(token + " " + data); // Reassemble paragraph text
                break;
        }
        if (!returnComponent.getTag().equals("p")) {
            returnComponent.setContent(data);
        }
        System.out.println("Created " + returnComponent);
        return returnComponent;
    }

    // Replaces the markdown token with the equivalent html tag inline.
    // The regex \\*\\*(.*?)\\*\\* matches:
    //   \\*\\*   → two literal characters (“**”)
    //   (.*?)    → any content in between, captured as group 1
    //   \\*\\*   → two literal characters to close (“**”)
    private String subParse(String line) {
        line = line.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>"); // Bold -> ** or __
        line = line.replaceAll("\\_\\_(.*?)\\_\\_", "<b>$1</b>");
        line = line.replaceAll("\\*(.*?)\\*", "<i>$1</i>"); // Italic -> * or _
        line = line.replaceAll("\\_(.*?)\\_", "<i>$1</i>");
        line = line.replaceAll("\\~\\~(.*?)\\~\\~", "<del>$1</del>"); // Strikethrough -> ~~
        line = line.replaceAll("\\|\\|(.*?)\\|\\|", "<ins>$1</ins>"); // Underline -> ||
        line = line.replaceAll("\\;\\;(.*?)\\;\\;", "<sup>$1</sup>"); // Superscript -> ;;
        line = line.replaceAll("\\,\\,(.*?)\\,\\,", "<sub>$1</sub>"); // Subscript -> ,,
        line = line.replaceAll("\\`(.*?)\\`", "<code>$1</code>"); // Inline monospace code -> `

        return line;
   }


    private List<TagComponent> parse() {
        System.out.println("Parser: Starting parse...");
        System.out.println(sourceText);
        tagObjects.clear();
        try (BufferedReader input = new BufferedReader(new StringReader(sourceText))) {
            String curLine;
            while ((curLine = input.readLine()) != null) {
                String[] lineComponents = getLineToken(curLine);
                lineComponents[1] = subParse(lineComponents[1]); // Handles inline symbols
                tagObjects.add(processToken(lineComponents[0],lineComponents[1]));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tagObjects;
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
