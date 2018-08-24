package com.darren.developer.smarthome;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.darren.developer.smarthome.data.Constants;
import com.darren.developer.smarthome.model.Record;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MonthlyFragment extends Fragment {

    private PieChart pieChart;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference recordRef = database.getReference(Constants.CURRENT_USER_NAME).child(Constants.KEY_LOGS);
    float [] units = {0,0,0,0,0,0};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly,container,false);
        pieChart = view.findViewById(R.id.monthly_pie);
        recordRef.limitToLast(30).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(int i=0;i<6;i++)
                    units[i]=0;
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    Record r = ds.getValue(Record.class);
                    units[0]+=r.getUnits1();
                    units[1]+=r.getUnits2();
                    units[2]+=r.getUnits3();
                    units[3]+=r.getUnits4();
                    units[4]+=r.getUnits5();
                    units[5]+=r.getUnits6();
                }
                ArrayList<Entry> yvalues = new ArrayList<>();
                yvalues.add(new Entry(units[0], 0));
                yvalues.add(new Entry(units[1], 1));
                yvalues.add(new Entry(units[2], 2));
                yvalues.add(new Entry(units[3], 3));
                yvalues.add(new Entry(units[4], 4));
                yvalues.add(new Entry(units[5], 5));
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
                pieChart.setData(data);
                pieChart.setDescription("");
                pieChart.setDrawHoleEnabled(false);
                pieChart.animateXY(1400, 1400);
                Legend l = pieChart.getLegend();
                l.setTextColor(Color.BLACK);
                l.setTextSize(10f);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Monthly Analysis");
    }
}
