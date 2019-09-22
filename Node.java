package com.company;

class Node {
    private int value;
    private String url;

    Node(int value, String url) {
        this.value = value;
        this.url = url;
    }

    String getURL() {
        return this.url;
    }
    void setURL(String url) {this.url = url;}

    int getValue() {
        return this.value;
    }
    void setValue(int val) {this.value = val;}

    @Override
    public String toString() {
        return "Depth: " + this.value + " URL: " + this.url;
    }
}