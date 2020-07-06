package com.eve.bill_it;

public class SelectedItem {
    int pos;
    String key;

    public SelectedItem(int pos, String key) {
        this.pos = pos;
        this.key = key;
    }

    public int getPos() {
        return pos;
    }

    public String getKey() {
        return key;
    }
}
