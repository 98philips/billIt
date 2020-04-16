package com.eve.bill_it;

import java.util.Comparator;

public class TimeCompare implements Comparator<Report> {
    @Override
    public int compare(Report t1, Report t2) {
        if(t1.getDate().after(t2.getDate())){
            return -1;
        }
        return 1;
    }
}
