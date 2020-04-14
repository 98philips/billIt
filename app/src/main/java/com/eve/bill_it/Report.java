package com.eve.bill_it;

import java.util.Date;

public class Report {
    Date date;
    long value;

    Report(){}

    Report(Date date,long value){
        this.date = date;
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public long getValue() {
        return value;
    }
}
