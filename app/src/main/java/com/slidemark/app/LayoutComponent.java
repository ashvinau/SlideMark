package com.slidemark.app;

import java.util.Objects;

public class LayoutComponent {
    private String tag;
    private String params;
    private String content;
    private boolean visible;

    public LayoutComponent(){
        this.tag = "";
        this.params = "";
        this.content = "";
        this.visible = true;
    }

    public LayoutComponent(String tags, String params, String content){
        this.tag = Objects.requireNonNullElse(tags, "");
        this.params = Objects.requireNonNullElse(params, "");
        this.content = Objects.requireNonNullElse(content, "");
    }

    public void setTag(String newTag){
        if(newTag!= null) {
            this.tag = newTag;
        }

    }

    public String getTag(){
        return this.tag;
    }

    public void setParams(String newParam){
        if(newParam != null) {
            this.params = newParam;
        }
    }
    public boolean getVisible() {
        return this.visible;
    }

    public String getParams(){
        return this.params;
    }

    public void setContent(String newContent) {
        if(newContent != null) {
            this.content = newContent;
        }
    }

    public String getContent(){
        return this.content;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String toString() {
        return "LayoutComponent- Tag: " + tag + " Params: " + params + " Content: " + content;
    }


}
