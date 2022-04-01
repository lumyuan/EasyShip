package com.pointer.wave.easyship.pojo;

public class TipsBen {
    private String name;
    private String is_dir;
    private String time;
    private String size;
    private String content;

    public TipsBen() {
    }

    public TipsBen(String name, String is_dir, String time, String size, String content) {
        this.name = name;
        this.is_dir = is_dir;
        this.time = time;
        this.size = size;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIs_dir() {
        return is_dir;
    }

    public void setIs_dir(String is_dir) {
        this.is_dir = is_dir;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "TipsBen{" +
                "name='" + name + '\'' +
                ", is_dir='" + is_dir + '\'' +
                ", time='" + time + '\'' +
                ", size='" + size + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
