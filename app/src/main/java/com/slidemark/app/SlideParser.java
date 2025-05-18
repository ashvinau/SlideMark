package com.slidemark.app;

import java.util.ArrayList;
import java.util.List;

public class SlideParser implements ControllerInterface {
    private ControllerInterface c;
    private String sourceText;
    private List<LayoutComponent> layoutObjects;

    public SlideParser(ControllerInterface newC) {
        if (newC != null)
            c = newC;
        sourceText = "";
        layoutObjects = new ArrayList<>();
    }

    public ReturnObject<?> request(ControllerInterface sender, String message) {
        return null;
    }
}
