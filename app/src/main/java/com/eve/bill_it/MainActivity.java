package com.eve.bill_it;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Bill IT";
    FirebaseDatabase database;
    DatabaseReference myRef;
    List<Report> reportList;
    Button add,save;
    EditText new_value,new_rate;
    RecyclerView recyclerView;
    ReportAdapter adapter;
    TextView month,bill_amt,energy_con;
    LinearLayout rate_linear,unit_linear;
    ImageView loader;
    HashMap<String,Date> dateList;
    String currentMonthIndex;
    float rate=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        add = findViewById(R.id.add);
        new_value = findViewById(R.id.new_value);
        recyclerView = findViewById(R.id.report_list);
        month = findViewById(R.id.month);
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
        month.setOnClickListener(this);
        reportList = new ArrayList<>();
        dateList = new HashMap<>();
        @SuppressLint("SimpleDateFormat")
        String month_year_string = new SimpleDateFormat("MMMM yyyy").format(new Date());
        currentMonthIndex = month_year_string;
        dateList.put(month_year_string,new Date());
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ReportAdapter(reportList,this,recyclerView);
        recyclerView.setAdapter(adapter);
        database = FirebaseDatabase.getInstance();
        getData();
        getRate();

    }


    void calculate(Date c_date){
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(c_date);
        int year = calendar.get(Calendar.YEAR);
        int month_num = calendar.get(Calendar.MONTH);
        @SuppressLint("SimpleDateFormat")
        String month_year_string = new SimpleDateFormat("MMMM yyyy").format(c_date);
        month.setText(month_year_string);
        List<Report> monthReportList = new ArrayList<>();
        for(Report i: reportList){
            Calendar calendar_i = new GregorianCalendar();
            Date d = i.getDate();
            calendar_i.setTime(d);
            Log.d("year i",String.valueOf(calendar_i.get(Calendar.YEAR)));
            Log.d("month i",String.valueOf(calendar_i.get(Calendar.MONTH)));
            Log.d("year",String.valueOf(calendar.get(Calendar.YEAR)));
            Log.d("month",String.valueOf(calendar.get(Calendar.MONTH)));
            if ((calendar_i.get(Calendar.YEAR) == year && calendar_i.get(Calendar.MONTH) == month_num)){
                monthReportList.add(i);
            }else if(monthReportList.size() != 0){
                break;
            }
        }
        int j = monthReportList.size();
        if (j == 0 || j == 1){
            bill_amt.setText("NA");
            energy_con.setText("₹ ".concat(String.valueOf(200+35.70+20)));
        }else {
            long s_date = monthReportList.get(j-1).getDate().getTime();
            long l_date = monthReportList.get(0).getDate().getTime();
            long s_value = monthReportList.get(j-1).getValue();
            long l_value = monthReportList.get(0).getValue();
            float value_diff = (l_value-s_value);
            long time_diff =  l_date-s_date;
            long units  = (long) ((value_diff/time_diff)*2592000000.0);
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
                calculate(dateList.get(currentMonthIndex));
                getDateList();
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

    void getDateList(){
        for(Report r: reportList){
            @SuppressLint("SimpleDateFormat")
            String month_year_string = new SimpleDateFormat("MMMM yyyy").format(r.getDate());
            if(!dateList.containsKey(month_year_string)){
                dateList.put(month_year_string,r.getDate());
            }
        }
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
                calculate(dateList.get(currentMonthIndex));
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
            calculate(dateList.get(currentMonthIndex));
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
            case R.id.month:
                final String[] dates = dateList.keySet().toArray(new String[0]);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose a Month");
                builder.setItems(dates, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentMonthIndex = dates[which];
                        calculate(dateList.get(currentMonthIndex));
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;

        }
    }
}
