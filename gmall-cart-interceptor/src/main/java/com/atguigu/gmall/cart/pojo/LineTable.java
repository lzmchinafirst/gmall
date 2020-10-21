package com.atguigu.gmall.cart.pojo;

import javax.sound.sampled.Line;

public class LineTable {
    private LineTable pre;
    private Object value;
    private LineTable post;

    public LineTable(Object value) {
        this.value = value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public LineTable getPre() {
        return pre;
    }

    public Object getValue() {
        return value;
    }

    public LineTable getPost() {
        return post;
    }
}
