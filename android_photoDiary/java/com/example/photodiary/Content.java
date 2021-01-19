package com.example.photodiary;

public class Content {
    int no;

    @Override
    public String toString() {
        return "Content{" +
                "no=" + no +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", text='" + text + '\'' +
                ", img='" + img + '\'' +
                ", fileName='" + fileName + '\'' +
                ", date='" + date + '\'' +
                '}';
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    String id;
    String name;
    String text;
    String img;
    String fileName;
    String date;

    public Content(int no, String id, String name, String text, String img, String fileName, String date) {
        this.no = no;
        this.id = id;
        this.name = name;
        this.text = text;
        this.img = img;
        this.fileName = fileName;
        this.date = date;
    }

    public Content(){

    }
}





