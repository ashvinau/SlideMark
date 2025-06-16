package com.slidemark.app;

import java.util.*;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlideParser implements ControllerInterface {
    private ControllerInterface c;
    private String sourceText;
    private List<TagComponent> tagObjects;
    private boolean cfOpen = false;
    private boolean tableOpen = false;
    int curSlide = 1;
    int ULlistDepth = 0;
    int OLlistDepth = 0;
    String workingDirectory = ".";
    private static final Pattern UL_ITEM = Pattern.compile("^(\\t*)-");
    private static final Pattern OL_ITEM = Pattern.compile("^(\\t*)(\\d+)\\.");
    private static final Pattern LINK_TOKEN = Pattern.compile("\\[(.*?)\\]\\s*\\(([^)]+)\\)");
    private static final Pattern IMAGE_TOKEN = Pattern.compile("!\\[(.*?)\\]\\s*\\(([^)]+)\\)");

    public SlideParser(ControllerInterface newC) {
        if (newC != null)
            c = newC;
        sourceText = "";
        tagObjects = new ArrayList<>();
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

        // Trim off trailing whitespace
        String trimmed = input.stripTrailing();
        if (trimmed.isEmpty()) {
            return new String[] { "", "" };
        }

        // Find the first whitespace
        int splitPos = trimmed.indexOf(' ');

        if (splitPos < 0) { // Entire string is a single word, token with no content, or paragraph
            switch (trimmed) {
                case "->":
                    firstWord = "->";
                    remainder = "";
                    break;
                case "```":
                    firstWord = "```";
                    remainder = "";
                    break;
                case "===":
                    firstWord = "===";
                    remainder = "";
                    break;
                default:
                    firstWord = "";
                    remainder = trimmed;
            }
        } else {
            firstWord = trimmed.substring(0, splitPos);
            remainder = trimmed.substring(splitPos + 1).trim();
        }

        // System.out.println("Tag: " + firstWord + " Remaining: " + remainder);
        return new String[] { firstWord, remainder };
    }

    private void adjustTableStatus(boolean open) {
        System.out.println("Adjust table status called, current open status: " + tableOpen + " requested status: " + open);
        if (tableOpen == false && open == true) { // Table is closed - open requested
                tagObjects.add(processToken("==-", ""));
                tableOpen = true;
                System.out.println("Table status changed from false -> true");
        }

        if (tableOpen == true && open == false) { // Table is open - close requested
                tagObjects.add(processToken("-==", ""));
                tableOpen = false;
                System.out.println("Table status changed from true -> false");
        }
    }

    private TagComponent processToken(String token, String data) {
        TagComponent returnComponent = new TagComponent();
        System.out.println("Token: -" + token + "-");
        String curLine = String.join(" ", token, data); // We need both the original token + data
        String curToken = token; // as well as a potential internal token

        if (curToken.matches(UL_ITEM.pattern())) {
            curToken = "+-+"; // Unique token generated for unordered list items since switch cant regex
        } else if ("+-+".equals(curToken)) {
            // Do nothing - Infinite loop protection
        } else if (Arrays.asList("+--", "--+").contains(curToken)) {
            // Token is a ul or /ul, we can reset the depth of the OL.
            System.out.println("OL Depth reset");
            adjustOLDepth(0);
        } else {
            System.out.println("UL Depth reset"); // Token is neither
            adjustULDepth(0);
        }

        if (curToken.matches(OL_ITEM.pattern())) {
            curToken = "-+-";
        } else if ("-+-".equals(curToken)) {
            // Do nothing
        } else if (Arrays.asList("-++", "++-").contains(curToken)) {
            System.out.println("UL Depth reset");
            adjustULDepth(0);
        } else {
            System.out.println("OL Depth reset");
            adjustOLDepth(0);
        }

        if (curLine.matches(IMAGE_TOKEN.pattern())) {
            curToken = "-=-";
            System.out.println("Image pattern matched, replaced with -=-");
        } else if ("-=-".equals(curToken)) {
            // Nothing - Converts image pattern to internal token
        }

        if (curLine.matches(LINK_TOKEN.pattern())) {
            curToken = "=-=";
            System.out.println("Link pattern matched, replaced with =-=");
        } else if ("=-=".equals(curToken)) {
            // Nothing - Converts link pattern to internal token
        }

        if (curToken.equals("|")) {
            adjustTableStatus(true);
            curToken = "-*-";
        } else if (Arrays.asList("-*-", "==-", "-==").contains(curToken)) {
            // Nothing - Converts table row to internal token
        } else {
            adjustTableStatus(false);
        }

        switch (curToken) {
            case "==-":
                returnComponent.setTag("table");
                break;
            case "-==":
                returnComponent.setTag("/table");
                break;
            case "-*-":
                returnComponent.setTag("tr");
                List<String> rowData = Arrays.asList(data.split("\\|"));
                StringBuilder rowHTML = new StringBuilder();
                for (String item : rowData) {
                    rowHTML.append("<td>").append(item.strip()).append("</td>");
                }
                data = rowHTML.toString();
                break;
            case "=-=": // Link
                Matcher linkM = LINK_TOKEN.matcher(curLine);
                System.out.println("Current line: " + curLine);
                while (linkM.find()) {
                    String text = linkM.group(1);
                    String url = linkM.group(2);
                    returnComponent.setTag("a");
                    returnComponent.setParams("href = " + url);
                    data = text;
                }
                break;
            case "-=-": // Image
                Matcher imgM = IMAGE_TOKEN.matcher(curLine);
                System.out.println("Current line: " + curLine);
                while (imgM.find()) {
                    String alt = imgM.group(1);
                    String src = imgM.group(2);
                    returnComponent.setTag("img");
                    returnComponent.setParams("src = \"" + workingDirectory + "/" + src + "\" alt = \"" + alt + "\"");
                    data = "";
                }
                break;
            case "+-+": // Unordered List item
                processUL(token, data);
                returnComponent.setTag("li");
                break;
            case "-+-": // Ordered list item
                processOL(token, data);
                returnComponent.setTag("li");
                break;
            case "+--":
                returnComponent.setTag("ul");
                break;
            case "--+":
                returnComponent.setTag("/ul");
                break;
            case "-++":
                returnComponent.setTag("ol");
                break;
            case "++-":
                returnComponent.setTag("/ol");
                break;
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
            case "->":
                returnComponent.setTag("sectChange");
                break;
            case "```":
                if (!cfOpen) {
                    cfOpen = true;
                    returnComponent.setTag("pre");
                } else {
                    cfOpen = false;
                    returnComponent.setTag("/pre");
                }
                break;
            case "===":
                returnComponent.setTag("nextSlide");
                returnComponent.setContent(data);
                break;
            default:
                // Fall through becomes paragraphs
                returnComponent.setTag("p");
                returnComponent.setContent(curToken + " " + data); // Reassemble paragraph text
                break;
        }
        if (!(returnComponent.getTag().equals("p")))
            returnComponent.setContent(data);

        System.out.println("Created " + returnComponent);
        return returnComponent;
    }

    private int countTabsByScan(String prefix) {
        int depth = 1; // Assume token is list and at least one is open
        for (char c : prefix.toCharArray()) {
            if (c == '\t') depth++;
            else break;    // stop as soon as you hit something that isn’t a tab
        }
        return depth;
    }

    private void processUL(String token, String data) {
        System.out.println("UL Processing, token: " + token + " data: " + data);
        int targetDepth = countTabsByScan(token);
        System.out.println("Target Depth: " + targetDepth + " Current Depth: " + ULlistDepth);
        adjustULDepth(targetDepth);
    }

    private void adjustULDepth(int targetDepth) {
        while (ULlistDepth < targetDepth) {
            tagObjects.add(processToken("+--", ""));
            ULlistDepth++;
            System.out.println("Opened UL");
        }
        while (ULlistDepth > targetDepth) {
            tagObjects.add(processToken("--+", ""));
            ULlistDepth--;
            System.out.println("Closed UL");
        }
    }

    private void processOL(String token, String data) {
        System.out.println("OL Processing, token: " + token + " data: " + data);
        int targetDepth = countTabsByScan(token);
        System.out.println("Target Depth: " + targetDepth + " Current Depth: " + OLlistDepth);
        adjustOLDepth(targetDepth);
    }

    private void adjustOLDepth(int targetDepth) {
        while (OLlistDepth < targetDepth) {
            tagObjects.add(processToken("-++", ""));
            OLlistDepth++;
            System.out.println("Opened OL");
        }
        while (OLlistDepth > targetDepth) {
            tagObjects.add(processToken("++-", ""));
            OLlistDepth--;
            System.out.println("Closed OL");
        }
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
        line = line.replaceAll("\\+\\+(.*?)\\+\\+", "<ins>$1</ins>"); // Underline -> ++
        line = line.replaceAll("\\;\\;(.*?)\\;\\;", "<sup>$1</sup>"); // Superscript -> ;;
        line = line.replaceAll("\\,\\,(.*?)\\,\\,", "<sub>$1</sub>"); // Subscript -> ,,
        if (!line.equals("```"))
            line = line.replaceAll("\\`(.*?)\\`", "<code>$1</code>"); // Inline monospace code -> `

        return line;
    }

    private List<TagComponent> parse() {
        System.out.println("Parser: Starting parse...");
        System.out.println("UL/OL Depth reset");
        adjustULDepth(0);
        adjustOLDepth(0);
        System.out.println("Initial table reset");
        tableOpen = false;
        workingDirectory = (String) c.request(this, "GET_WORKING_DIRECTORY").getValue();
        //System.out.println(sourceText);
        tagObjects.clear();
        try (BufferedReader input = new BufferedReader(new StringReader(sourceText))) {
            String curLine;
            while ((curLine = input.readLine()) != null) {
                if (curLine.isEmpty())
                    continue;
                String[] lineComponents = getLineToken(curLine);
                lineComponents[1] = subParse(lineComponents[1]); // Handles inline symbols
                TagComponent curToken = processToken(lineComponents[0],lineComponents[1]);
                tagObjects.add(curToken);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tagObjects;
    }

    private List<List<TagComponent>> splitOnDelimiter(List<TagComponent> input, String delimiterTag) {
        List<List<TagComponent>> sections = new ArrayList<>();
        sections.add(new ArrayList<>());

        for (TagComponent comp : input) {
            if (delimiterTag.equals(comp.getTag())) {
                sections.get(sections.size() - 1).add(comp);
                sections.add(new ArrayList<>());
            } else {
                sections.get(sections.size() - 1).add(comp);
            }
        }

        int lastIdx = sections.size() - 1;
        if (sections.get(lastIdx).isEmpty() && sections.size() > 1) {
            sections.remove(lastIdx);
        }

        return sections;
    }

    public ReturnObject<?> request(ControllerInterface sender, String message) {

        List<TagComponent> returnList = new ReturnObject<>(parse()).getValue();
        switch (message) {
            case "PROCESS_SOURCE":
                sourceText = "";
                sourceText = (String) c.request(this, "GET_CONTENT").getValue();
                c.request(this, "LAYOUT_READY");
                break;
            case "GET_LAYOUT":
                List<TagComponent> splitList = splitOnDelimiter(returnList, "nextSlide").get(curSlide - 1);
                System.out.println(splitList);
                return new ReturnObject<>(splitList);
            case "SET_SLIDE_NUM":
                curSlide = (int) c.request(this, "WHAT_SLIDE").getValue();
                System.out.println("current slide assigned: " + curSlide);
                break;
        }
        return null; // No return data here.
    }
}