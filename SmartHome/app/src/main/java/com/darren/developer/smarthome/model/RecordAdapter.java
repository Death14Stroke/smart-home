package com.darren.developer.smarthome.model;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.darren.developer.smarthome.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.MyViewHolder> {

    private Context mContext;
    private List<Record> recordList;

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView dateTV, totalTV, totalTV2;
        private TextView d1unitsTV, d2unitsTV, d3unitsTV, d4unitsTV, d5unitsTV, d6unitsTV;
        private TextView d1nameTV, d2nameTV, d3nameTV, d4nameTV, d5nameTV, d6nameTV;
        private ImageButton detailsButton;
        private TableLayout tableLayout;
        private PieChart pieChart;
        private RelativeLayout relativeLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            dateTV = itemView.findViewById(R.id.date_tv);
            totalTV = itemView.findViewById(R.id.units_used_tv);
            detailsButton = itemView.findViewById(R.id.details_btn);
            d1unitsTV = itemView.findViewById(R.id.d1units_tv);
            d2unitsTV = itemView.findViewById(R.id.d2units_tv);
            d3unitsTV = itemView.findViewById(R.id.d3units_tv);
            d4unitsTV = itemView.findViewById(R.id.d4units_tv);
            d5unitsTV = itemView.findViewById(R.id.d5units_tv);
            d6unitsTV = itemView.findViewById(R.id.d6units_tv);
            d1nameTV = itemView.findViewById(R.id.d1name_tv);
            d2nameTV = itemView.findViewById(R.id.d2name_tv);
            d3nameTV = itemView.findViewById(R.id.d3name_tv);
            d4nameTV = itemView.findViewById(R.id.d4name_tv);
            d5nameTV = itemView.findViewById(R.id.d5name_tv);
            d6nameTV = itemView.findViewById(R.id.d6name_tv);
            totalTV2 = itemView.findViewById(R.id.total_units_tv);
            tableLayout = itemView.findViewById(R.id.table_layout);
            pieChart = itemView.findViewById(R.id.pie_chart);
            relativeLayout = itemView.findViewById(R.id.relative_layout_log);
        }
    }

    public RecordAdapter(Context mContext, List<Record> recordList) {
        this.mContext = mContext;
        this.recordList = recordList;
    }

    @Override
    public RecordAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.log_view, parent,false);
        return new RecordAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecordAdapter.MyViewHolder holder, int position) {
        final Record record = recordList.get(position);
        long ms = record.getDate()*1000;
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        Date d = new Date(ms);
        String date = df.format(d);
        holder.dateTV.setText(date);
        float total = record.getUnits1()+record.getUnits2()+record.getUnits3()+record.getUnits4()+record.getUnits5()+record.getUnits6();
        holder.totalTV.setText(String.valueOf(total)+" units ");
        holder.totalTV2.setText(String.valueOf(total));
        holder.d1unitsTV.setText(String.valueOf(record.getUnits1()));
        holder.d2unitsTV.setText(String.valueOf(record.getUnits2()));
        holder.d3unitsTV.setText(String.valueOf(record.getUnits3()));
        holder.d4unitsTV.setText(String.valueOf(record.getUnits4()));
        holder.d5unitsTV.setText(String.valueOf(record.getUnits5()));
        holder.d6unitsTV.setText(String.valueOf(record.getUnits6()));
        holder.d1nameTV.setText("Port 1");
        holder.d2nameTV.setText("Port 2");
        holder.d3nameTV.setText("Port 3");
        holder.d4nameTV.setText("Port 4");
        holder.d5nameTV.setText("Port 5");
        holder.d6nameTV.setText("Port 6");
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.detailsButton.callOnClick();
            }
        });
        holder.detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.tableLayout.getVisibility()==View.GONE) {
                    holder.tableLayout.setVisibility(View.VISIBLE);
                }
                else{
                    holder.tableLayout.setVisibility(View.GONE);
                }
                if(holder.pieChart.getVisibility()==View.GONE) {
                    holder.pieChart.setVisibility(View.VISIBLE);
                }
                else{
                    holder.pieChart.setVisibility(View.GONE);
                }
            }
        });
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.detailsButton.callOnClick();
            }
        });
        ArrayList<Entry> yvalues = new ArrayList<>();
        yvalues.add(new Entry(record.getUnits1(), 0));
        yvalues.add(new Entry(record.getUnits2(), 1));
        yvalues.add(new Entry(record.getUnits3(), 2));
        yvalues.add(new Entry(record.getUnits4(), 3));
        yvalues.add(new Entry(record.getUnits5(), 4));
        yvalues.add(new Entry(record.getUnits6(), 5));
        PieDataSet dataSet = new PieDataSet(yvalues,"");
        ArrayList<String> xVals = new ArrayList<>();
        xVals.add("Port 1");
        xVals.add("Port 2");
        xVals.add("Port 3");
        xVals.add("Port 4");
        xVals.add("Port 5");
        xVals.add("Port 6");
        PieData data = new PieData(xVals, dataSet);
        final int[] MY_COLORS = {
                Color.rgb(245, 140, 255),
                Color.rgb(255, 0, 0),
                Color.rgb(153, 255, 0),
                Color.rgb(255, 238, 0),
                Color.rgb(145, 255, 253),
                Color.rgb(255, 186, 68)};
        ArrayList<Integer> colors = new ArrayList<>();
        for(int c: MY_COLORS) colors.add(c);
        dataSet.setColors(colors);
        data.setValueTextSize(13f);
        data.setValueTextColor(Color.DKGRAY);
        holder.pieChart.setData(data);
        holder.pieChart.setDescription("");
        holder.pieChart.setDrawHoleEnabled(false);
        holder.pieChart.animateXY(1400, 1400);
        Legend l = holder.pieChart.getLegend();
        l.setTextColor(Color.YELLOW);
        l.setTextSize(10f);
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

}
