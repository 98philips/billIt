package com.eve.bill_it;

import java.util.Date;

public class Report {
    Date date;
    long value;
    String key;
    boolean selected;

    Report(){}

    Report(Date date,long value){
        this.date = date;
        this.value = value;
        this.selected = false;
    }

    public Date getDate() {
        return date;
    }

    public long getValue() {
        return value;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
