package com.gtech.shohozhandnote.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;


public class Ebook extends RealmObject {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("menu")
    @Expose
    private String menu;
    @SerializedName("content")
    @Expose
    private String content;

    @SerializedName("audio")
    @Expose
    private String audio;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public String getContent() {
        return content;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Ebook{" +
                "id='" + id + '\'' +
                ", menu='" + menu + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}