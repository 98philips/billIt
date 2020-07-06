package com.eve.bill_it;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class BillitAppMain  extends Application {

    public boolean isMultiMode = false;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
