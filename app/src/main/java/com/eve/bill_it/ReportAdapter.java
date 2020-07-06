package com.eve.bill_it;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    List<Report> reportList;
    Context context;
    RecyclerView recyclerView;
    List<MultiModeListener> multiModeListeners;
    boolean isMultiMode = false;

    public ReportAdapter(List<Report> reportList,Context context, RecyclerView recyclerView){
        this.reportList = reportList;
        this.context = context;
        this.recyclerView  = recyclerView;
        Log.d("Size Adapter ", String.valueOf(this.reportList.size()));
        multiModeListeners = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_item, parent, false);
        return new ViewHolder(v);
    }

    void addMultiModeListener(MultiModeListener multiModeListener){
        multiModeListeners.add(multiModeListener);
    }

    void setMultiMode(int pos, String key){
        for (MultiModeListener i: multiModeListeners) {
            i.onSetMultiMode(true);
            i.onAddItem(pos,key);
        }
    }

    void addMultiItem(int pos, String key){
        for (MultiModeListener i: multiModeListeners) {
            i.onAddItem(pos,key);
        }
    }

    void removeMultiItem(int pos, String key){
        for (MultiModeListener i: multiModeListeners) {
            i.onRemoveItem(pos,key);
        }
    }

    public void setIsMultiMode(boolean multiMode) {
        isMultiMode = multiMode;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder h, final int position) {
        final Report report = reportList.get(position);
        @SuppressLint("SimpleDateFormat")
        String month_year_string = new SimpleDateFormat("EEEE \nMMMM yyyy").format(report.getDate());
        @SuppressLint("SimpleDateFormat") String date_string = new SimpleDateFormat("dd").format(report.getDate());
        @SuppressLint("SimpleDateFormat") String time_string = new SimpleDateFormat("HH:mm").format(report.getDate());
        h.date.setText(date_string);
        h.time.setText(time_string);
        h.month_year.setText(month_year_string);
        String unit = String.valueOf(report.getValue()).concat("\nkWh");
        h.unit_value.setText(unit);
        if(report.isSelected()){
            h.cardView.setBackground(context.getResources().getDrawable(R.drawable.selected_bg));
        }else{
            h.cardView.setBackground(context.getResources().getDrawable(R.drawable.transparent_bg));
        }
        @SuppressLint("SimpleDateFormat")
        String day_String =  new SimpleDateFormat("EEEE").format(report.getDate());
        h.day.setText(day_String);
        h.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMultiMode) {
                    if (report.isSelected()) {
                        removeMultiItem(position, report.key);
                    } else {
                        addMultiItem(position,report.key);
                    }
                }
                TransitionManager.beginDelayedTransition(recyclerView);
            }
        });
        h.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(isMultiMode){
                    addMultiItem(position,report.key);
                }else{
                    setMultiMode(position,report.key);
                }

                return true;
            }
        });
        h.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("/recordList/Home/reportList");
                myRef.child(report.key).removeValue();
                reportList.remove(position);
                notifyItemRemoved(position);
                h.extra_details.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView month_year,unit_value,day,date,time;
        private ImageView delete;
        private LinearLayout extra_details,cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_tile);
            month_year = itemView.findViewById(R.id.month_year);
            unit_value = itemView.findViewById(R.id.unit_value);
            delete = itemView.findViewById(R.id.delete);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
            day = itemView.findViewById(R.id.day_text);
            extra_details = itemView.findViewById(R.id.extra_details);

        }
    }
}
