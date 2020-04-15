package com.eve.bill_it;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private static final String TAG = "Bill IT";
    FirebaseDatabase database;
    DatabaseReference myRef;
    List<Report> reportList;
    Button add,save;
    EditText new_value,new_rate;
    RecyclerView recyclerView;
    ReportAdapter adapter;
    TextView start_date,end_date,bill_amt,energy_con;
    LinearLayout rate_linear,unit_linear;
    ImageView loader;
    String chosen_date;
    float rate=0;
    Date startDate,endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        add = findViewById(R.id.add);
        new_value = findViewById(R.id.new_value);
        recyclerView = findViewById(R.id.report_list);
        start_date = findViewById(R.id.startDate);
        end_date = findViewById(R.id.endDate);
        bill_amt = findViewById(R.id.bill_amt);
        energy_con = findViewById(R.id.energy_con);
        rate_linear = findViewById(R.id.rate_linear);
        unit_linear = findViewById(R.id.unit_linear);
        new_rate = findViewById(R.id.new_rate);
        loader = findViewById(R.id.loader);
        save = findViewById(R.id.save);
        save.setOnClickListener(this);
        bill_amt.setOnClickListener(this);
        add.setOnClickListener(this);
        start_date.setOnClickListener(this);
        end_date.setOnClickListener(this);
        reportList = new ArrayList<>();
        initDate();
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ReportAdapter(reportList,this,recyclerView);
        recyclerView.setAdapter(adapter);
        database = FirebaseDatabase.getInstance();
        setDateText();
        getData();
        getRate();

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
        calendar.add(Calendar.MONTH,-1);
        startDate = calendar.getTime();

    }

    void setDateText(){
        @SuppressLint("SimpleDateFormat")
        String time_date_string = new SimpleDateFormat("dd MMMM yyyy").format(startDate);
        start_date.setText(time_date_string);
        @SuppressLint("SimpleDateFormat")
        String time_date_string_end = new SimpleDateFormat("dd MMMM yyyy").format(endDate);
        end_date.setText(time_date_string_end);
    }


    void calculate(Date start_date,Date end_date){
        List<Report> monthReportList = new ArrayList<>();
        for(Report i: reportList){
            Calendar calendar_i = new GregorianCalendar();
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
            double rupees = units*rate+units*rate*0.1+200+35.70+20;
            bill_amt.setText("₹ ".concat(String.format("%.2f",rupees)));
            energy_con.setText(String.valueOf(units).concat(" kWh"));
        }
    }

    void sendData(){
        String value = new_value.getText().toString();
        if(!value.equals("")){
            reportList.add(new Report(new Date(),Long.parseLong(value)));
            myRef = database.getReference("/Home/reportList");
            myRef.setValue(reportList);
            new_value.setText("");
            new_value.clearFocus();
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(new_value.getWindowToken(), 0);
        }

    }



    void getData(){
        // Read from the database
        loader.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        Glide.with(this).load(R.drawable.loading).into(loader);
        myRef = database.getReference("/Home/reportList");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                loader.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                reportList.clear();
                for(DataSnapshot report: dataSnapshot.getChildren()){
                    reportList.add(0,report.getValue(Report.class));
                }
                calculate(startDate,endDate);
                adapter.notifyDataSetChanged();
                Log.d("Size ", String.valueOf(reportList.size()));
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }



    void getRate(){
        myRef = database.getReference("/Home/rate");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                rate =  Float.parseFloat(dataSnapshot.getValue().toString());
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
            myRef = database.getReference("/Home/rate");
            myRef.setValue(rate);
            calculate(startDate,endDate);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.add:
                sendData();
                break;
            case R.id.save:
                setRate();
                rate_linear.setVisibility(View.GONE);
                unit_linear.setVisibility(View.VISIBLE);
                new_rate.clearFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(new_rate.getWindowToken(), 0);
                break;
            case R.id.bill_amt:
                new_rate.setText(String.valueOf(rate));
                rate_linear.setVisibility(View.VISIBLE);
                unit_linear.setVisibility(View.GONE);
                new_rate.requestFocus();
                break;
            case R.id.startDate:
                chosen_date = "start";
                showDatePicker(startDate);
                break;
            case R.id.endDate:
                chosen_date = "end";
                showDatePicker(endDate);
                break;

        }
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
            setDateText();
            SimpleDateFormat ft =
                    new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
            Log.d("Start Date",ft.format(startDate));
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
            setDateText();
            SimpleDateFormat ft =
                    new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
            Log.d("End Date",ft.format(endDate));
        }
        calculate(startDate,endDate);
    }
}
