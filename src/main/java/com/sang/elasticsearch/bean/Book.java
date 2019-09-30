package com.sang.elasticsearch.bean;

import java.util.Arrays;

public class Book extends ESEntity{
    private String name;
    private String author;
    private String[] types;
    private String style;
    private int  wordCount;
    private long workScore;
    private String updateTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public long getWorkScore() {
        return workScore;
    }

    public void setWorkScore(long workScore) {
        this.workScore = workScore;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Book{" +
                "name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", types=" + Arrays.toString(types) +
                ", style='" + style + '\'' +
                ", wordCount=" + wordCount +
                ", workScore=" + workScore +
                ", updateTime='" + updateTime + '\'' +
                '}';
    }
}
