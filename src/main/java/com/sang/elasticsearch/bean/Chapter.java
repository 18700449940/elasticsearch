package com.sang.elasticsearch.bean;

public class Chapter extends ESEntity {

    private String name;
    private String title;
    private int wordCount;
    private String updateTime;
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", wordCount=" + wordCount +
                ", updateTime='" + updateTime + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
