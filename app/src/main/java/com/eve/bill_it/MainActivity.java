package com.eve.bill_it;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.collection.LLRBNode;

import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, MultiModeListener {

    private static final String TAG = "Bill IT";
    FirebaseDatabase database;
    DatabaseReference myRef;
    List<Report> reportList;
    List<Report> recyclerList;
    List<SelectedItem> selectedItemList;
    Map<String,Float> rateMap;
    ImageButton add,save,close;
    EditText new_value,new_rate;
    RecyclerView recyclerView;
    ReportAdapter adapter;
    ImageView rate_info,delete_multi,cancel_multi;
    TextView start_date,end_date,bill_amt,energy_con,bill_label,show_more,number_multi;
    LinearLayout rate_linear,unit_linear;
    ProgressBar loader;
    CardView multi_tab;
    String chosen_date;
    float rate=0;
    Date startDate,endDate;
    int item_count = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_content);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ThemePicker.setStatusBar(MainActivity.this);
        add = findViewById(R.id.add);
        new_value = findViewById(R.id.new_value);
        recyclerView = findViewById(R.id.report_list);
        start_date = findViewById(R.id.startDate);
        end_date = findViewById(R.id.endDate);
        bill_amt = findViewById(R.id.bill_amt);
        energy_con = findViewById(R.id.energy_con);
        rate_linear = findViewById(R.id.rate_linear);
        unit_linear = findViewById(R.id.unit_linear);
        bill_label  = findViewById(R.id.bill_label);
        show_more = findViewById(R.id.show_more);
        rate_info = findViewById(R.id.price_info);
        new_rate = findViewById(R.id.new_rate);
        multi_tab = findViewById(R.id.multi_select_tab);
        delete_multi = findViewById(R.id.delete_multi);
        cancel_multi = findViewById(R.id.cancel_multi);
        number_multi = findViewById(R.id.number_multi);
        close = findViewById(R.id.close);
        loader = findViewById(R.id.loader);
        save = findViewById(R.id.save);
        rate_info.setOnClickListener(this);
        save.setOnClickListener(this);
        show_more.setOnClickListener(this);
        add.setOnClickListener(this);
        close.setOnClickListener(this);
        start_date.setOnClickListener(this);
        delete_multi.setOnClickListener(this);
        cancel_multi.setOnClickListener(this);
        end_date.setOnClickListener(this);
        reportList = new ArrayList<>();
        recyclerList = new ArrayList<>();
        selectedItemList = new ArrayList<>();
        rateMap = new HashMap<>();
        initDate();
        rate_linear.setVisibility(View.GONE);
        unit_linear.setVisibility(View.VISIBLE);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ReportAdapter(recyclerList,this,recyclerView);
        recyclerView.setAdapter(adapter);
        database = FirebaseDatabase.getInstance();
        adapter.addMultiModeListener(this);
        setDateText();
        getRate();

        //getTestData();
    }

    void initDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        calendar.set(Calendar.MILLISECOND,999);
        endDate = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        calendar.add(Calendar.MONTH,-2);
        startDate = calendar.getTime();

    }

    void setDateText(){
        @SuppressLint("SimpleDateFormat")
        String time_date_string = new SimpleDateFormat("dd MMM yyyy").format(startDate);
        start_date.setText(time_date_string);
        @SuppressLint("SimpleDateFormat")
        String time_date_string_end = new SimpleDateFormat("dd MMM yyyy").format(endDate);
        end_date.setText(time_date_string_end);
    }


    void calculate(Date start_date,Date end_date){
        List<Report> monthReportList = new ArrayList<>();
        for(Report i: reportList){
            Calendar calendar_i = Calendar.getInstance();;
            Date d = i.getDate();
            calendar_i.setTime(d);
            if (i.getDate().after(start_date) && i.getDate().before(end_date)){
                monthReportList.add(i);
            }else if(monthReportList.size() != 0){
                break;
            }
        }
        int j = monthReportList.size();
        if (j == 0 || j == 1){
            bill_amt.setText("₹ ".concat(String.valueOf(200+35.70+20)));
            energy_con.setText("0 kWh");
        }else {
            long s_date = monthReportList.get(j-1).getDate().getTime();
            long l_date = monthReportList.get(0).getDate().getTime();
            long s_value = monthReportList.get(j-1).getValue();
            long l_value = monthReportList.get(0).getValue();
            float value_diff = (l_value-s_value);
            long time_diff =  l_date-s_date;
            long units  = (long) ((value_diff/time_diff)*(end_date.getTime()-start_date.getTime()));
            Log.d("units",String.valueOf(units));
            Log.d("value diff",String.valueOf(l_value-s_value));
            Log.d("time diff",String.valueOf(l_date-s_date));
            double rupees = getPrice(units);
            bill_amt.setText("₹ ".concat(String.format("%.2f",rupees)));
            bill_label.setText("Est. Bill Amt.");
            energy_con.setText(String.valueOf(units).concat(" kWh"));
        }
    }

    double getPrice(long units){
        double amt = 0;
        if(units<=100){
            amt = units*rateMap.get("100");
        }else if(units<=200){
            amt = 100*rateMap.get("100") + (rateMap.get("200")*units-100);
        }else if(units<=300){
            amt = 100*rateMap.get("100") + rateMap.get("200")*100 + (rateMap.get("300")*(units-200));
        }else if(units<=400){
            amt = 100*rateMap.get("100") + rateMap.get("200")*100 + rateMap.get("300")*100 + (rateMap.get("400")*(units-300));
        }else if(units<=500){
            amt = 100*rateMap.get("100") + rateMap.get("200")*100 + rateMap.get("300")*100 + rateMap.get("400")*100 + (rateMap.get("500")*(units-400));
        }else if(units<=600){
            amt = rateMap.get("600") * units;
        }else{
            amt = rateMap.get("else") * units;
        }
        return amt+amt*0.1+200+35.70+0.1*units;
    }

    void sendData(){
        String value = new_value.getText().toString();
        if(!value.equals("")){
            Report r = new Report(new Date(),Long.parseLong(value));
            reportList.add(r);
            Collections.sort(reportList,new TimeCompare());
            myRef = database.getReference("/recordList/Home/reportList");
            myRef.push().setValue(r);
            new_value.setText("");
            new_value.clearFocus();
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(new_value.getWindowToken(), 0);
        }

    }



    void getData(){
        // Read from the database
        loader.setVisibility(View.VISIBLE);
        //recyclerView.setVisibility(View.GONE);
        //show_more.setVisibility(View.GONE);
        myRef = database.getReference("/recordList/Home/reportList");
        myRef.keepSynced(true);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                loader.setVisibility(View.GONE);
                //recyclerView.setVisibility(View.VISIBLE);
                reportList.clear();
                for(DataSnapshot report: dataSnapshot.getChildren()){
                    Report r = report.getValue(Report.class);
                    if (r != null) {
                        r.key = report.getKey();
                    }
                    reportList.add(r);
                }
                if(reportList.size()>10){
                    show_more.setVisibility(View.VISIBLE);
                }
                Collections.sort(reportList,new TimeCompare());
                calculate(startDate,endDate);
                Log.d("Size ", String.valueOf(reportList.size()));
//                transferData();
                populate_data();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                loader.setVisibility(View.GONE);
                //recyclerView.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this,"Something Went Wrong",Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    private void populate_data() {
        recyclerList.clear();
        recyclerList.addAll(reportList.subList(0, item_count));
        adapter.notifyDataSetChanged();
    }


//    void transferData(){
//        myRef = database.getReference("/recordList/Home/reportList");
//        for(Report r: reportList){
//            myRef.push().setValue(r);
//        }
//
//    }

    void getTestData(){
        // Read from the database
        myRef = database.getReference("/recordList/");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d("data ", dataSnapshot.toString());
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read test value.", error.toException());
            }
        });

    }



    void getRate(){
        myRef = database.getReference("/recordList/Home/rate");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for(DataSnapshot rate: dataSnapshot.getChildren()){
                    float value =  Float.parseFloat(Objects.requireNonNull(rate.getValue()).toString());
                    String key = rate.getKey();
                    rateMap.put(key,value);
                    Log.d("Key",key);
                }
                getData();
                Log.d("Rate",String.valueOf(rate));
                calculate(startDate,endDate);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    void setRate(){
        String value = new_rate.getText().toString();
        if(!value.equals("")){
            rate = Float.parseFloat(value);
            myRef = database.getReference("/recordList/Home/rate");
            myRef.setValue(rate);
            calculate(startDate,endDate);
        }

    }

    @Override
    public void onClick(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        switch (view.getId()){
            case R.id.add:
                sendData();
                break;
            case R.id.save:
                setRate();
                rate_linear.setVisibility(View.GONE);
                unit_linear.setVisibility(View.VISIBLE);
                new_rate.clearFocus();
                imm.hideSoftInputFromWindow(new_rate.getWindowToken(), 0);
                break;
            case R.id.startDate:
                chosen_date = "start";
                showDatePicker(startDate);
                break;
            case R.id.endDate:
                chosen_date = "end";
                showDatePicker(endDate);
                break;
            case R.id.close:
                new_rate.clearFocus();
                rate_linear.setVisibility(View.GONE);
                unit_linear.setVisibility(View.VISIBLE);
                imm.hideSoftInputFromWindow(new_rate.getWindowToken(), 0);
                break;
            case R.id.show_more:
                if(show_more.getText().toString().equals("show all")){
                    show_more.setText("show less");
                    item_count = reportList.size();
                }else{
                    show_more.setText("show all");
                    item_count = 10;
                }
                populate_data();
                break;
            case R.id.price_info:
                String message="";
                for(String s: rateMap.keySet()){
                    String line = "";
                    if(s.equals("600")){
                        line = "up to ".concat(s.concat(" : ₹")).concat(String.valueOf(rateMap.get(s))).concat("/unit\n");
                    }else if(s.equals("else")){
                        line = "above 600 : ₹".concat(String.valueOf(rateMap.get(s))).concat("/unit\n");
                    }else if(s.equals("100")){
                        line = "up to ".concat(s.concat(" : ₹")).concat(String.valueOf(rateMap.get(s))).concat("/unit\n");
                    }
                    else{
                        line = "up to ".concat(s.concat(" : ₹")).concat(String.valueOf(rateMap.get(s))).concat(" split by 100 units\n");
                    }
                    message = message.concat(line);
                }
                message = message.concat("\nAdditional Charges:\n\n10% Duty\n₹200 Fixed Charge\n₹35.7 Meter rent\n10 paise per unit Fuel surcharge");
                new AlertDialog.Builder(this)
                        .setTitle("Rate Info")
                        .setMessage(message)
                        .show();
                break;
            case R.id.cancel_multi:
                onSetMultiMode(false);
                break;
            case R.id.delete_multi:
                multi_delete();
                break;

        }
    }

    private void multi_delete() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/recordList/Home/reportList");
        for (SelectedItem selectedItem:
             selectedItemList) {
            if(selectedItem.key.equals(reportList.get(selectedItem.pos).key)){
                myRef.child(selectedItem.key).removeValue();
                reportList.remove(selectedItem.pos);
                adapter.notifyItemRemoved(selectedItem.pos);
                TransitionManager.beginDelayedTransition(recyclerView);
            }

        }
        selectedItemList.clear();
        onSetMultiMode(false);
    }

    void showDatePicker(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        DatePickerDialog dialog = new DatePickerDialog(MainActivity.this, this,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar myCalendar = Calendar.getInstance();
        myCalendar.set(Calendar.YEAR, year);
        myCalendar.set(Calendar.MONTH, month);
        myCalendar.set(Calendar.DAY_OF_MONTH, day);
        if (chosen_date.equals("start")){
            myCalendar.set(Calendar.HOUR_OF_DAY,0);
            myCalendar.set(Calendar.MINUTE,0);
            myCalendar.set(Calendar.SECOND,0);
            myCalendar.set(Calendar.MILLISECOND,0);
            Date date = myCalendar.getTime();
            if(date.after(endDate)){
                showDatePicker(startDate);
                Toast.makeText(this,"Invalid Date Range",Toast.LENGTH_SHORT).show();
                return;
            }
            startDate = date;
        }else{
            myCalendar.set(Calendar.HOUR_OF_DAY,23);
            myCalendar.set(Calendar.MINUTE,59);
            myCalendar.set(Calendar.SECOND,59);
            myCalendar.set(Calendar.MILLISECOND,999);
            Date date = myCalendar.getTime();
            if(date.before(startDate)){
                showDatePicker(endDate);
                Toast.makeText(this,"Invalid Date Range",Toast.LENGTH_SHORT).show();
                return;
            }
            endDate = date;
        }
        setDateText();
        calculate(startDate,endDate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.cost_per_unit:
//                new_rate.setText(String.valueOf(rate));
//                rate_linear.setVisibility(View.VISIBLE);
//                unit_linear.setVisibility(View.GONE);
//                new_rate.requestFocus();
//                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.showSoftInput(new_rate,0);
//                break;
            case R.id.logout:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // user is now signed out
                                startActivity(new Intent(MainActivity.this, Login.class));
                                finish();
                            }
                        });
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSetMultiMode(boolean b) {
        adapter.setIsMultiMode(b);
        if(b){
            unit_linear.setVisibility(View.GONE);
            multi_tab.setVisibility(View.VISIBLE);
        }else{
            unit_linear.setVisibility(View.VISIBLE);
            multi_tab.setVisibility(View.GONE);
            if(!selectedItemList.isEmpty()){
                for (SelectedItem i:
                        selectedItemList) {
                    reportList.get(i.pos).setSelected(false);
                }
                adapter.notifyDataSetChanged();
                selectedItemList.clear();
            }
        }
    }

    @Override
    public void onAddItem(int pos, String key) {
        Report report = reportList.get(pos);
        if(report.key.equals(key)){
            selectedItemList.add(new SelectedItem(pos,key));
            report.setSelected(true);
            adapter.notifyItemChanged(pos);
            number_multi.setText(String.valueOf(selectedItemList.size()));
        }
    }

    @Override
    public void onRemoveItem(int pos, String key) {
        Report report = reportList.get(pos);
        if(report.key.equals(key)){
            report.setSelected(false);
            for (SelectedItem i: selectedItemList) {
                if(i.key.equals(key)){
                    selectedItemList.remove(i);
                    break;
                }
            }
            adapter.notifyItemChanged(pos);
            if(selectedItemList.isEmpty()){
                onSetMultiMode(false);
            }
            number_multi.setText(String.valueOf(selectedItemList.size()));
        }
    }
}
