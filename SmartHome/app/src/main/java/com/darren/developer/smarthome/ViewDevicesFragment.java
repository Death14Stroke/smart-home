package com.darren.developer.smarthome;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.darren.developer.smarthome.data.Constants;
import com.darren.developer.smarthome.model.Device;
import com.darren.developer.smarthome.model.DeviceAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 31-03-2018.
 */

public class ViewDevicesFragment extends Fragment {

    private RecyclerView recyclerView;
    private DeviceAdapter deviceAdapter;
    private List<Device> deviceList = new ArrayList<>();
    private TextView tempTV, pressTV;
    private Button all_on_button, all_off_button;
    private ImageButton tempReqBtn, pressReqBtn;
    private ProgressBar progressBar;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference pressureRef = database.getReference(Constants.CURRENT_USER_NAME).child(Constants.PRESS_KEY);
    DatabaseReference tempRef = database.getReference(Constants.CURRENT_USER_NAME).child(Constants.TEMP_KEY);
    DatabaseReference devicesRef = database.getReference(Constants.CURRENT_USER_NAME).child(Constants.DEVICES_KEY);
    DatabaseReference alloffRef = database.getReference(Constants.CURRENT_USER_NAME).child(Constants.KEY_ALLOFF);
    DatabaseReference tempReq = database.getReference(Constants.CURRENT_USER_NAME).child(Constants.KEY_TEMPREQ);
    DatabaseReference pressReq = database.getReference(Constants.CURRENT_USER_NAME).child(Constants.KEY_PRESSREQ);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        View view = inflater.inflate(R.layout.view_devices,container,false);

        tempTV = view.findViewById(R.id.temp_tv);
        pressTV = view.findViewById(R.id.press_tv);
        progressBar = view.findViewById(R.id.progressBarView);
        all_off_button = view.findViewById(R.id.all_off_button);
        all_on_button = view.findViewById(R.id.all_on_button);
        tempReqBtn = view.findViewById(R.id.tempReqBtn);
        pressReqBtn = view.findViewById(R.id.pressReqBtn);
        deviceAdapter = new DeviceAdapter(getContext(),deviceList);
        recyclerView = view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(deviceAdapter);
        prepareDevices();

        tempReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean status = dataSnapshot.getValue(boolean.class);
                        if (!status) {
                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            progressBar.setVisibility(View.GONE);
                            tempRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    tempTV.setText("Temperature is "+dataSnapshot.getValue()+ " C.");
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            tempReq.removeEventListener(this);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                };
                tempReq.setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.VISIBLE);
                        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        tempReq.addValueEventListener(valueEventListener);
                    }
                });
            }
        });

        pressReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean status = dataSnapshot.getValue(boolean.class);
                        if(!status){
                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            progressBar.setVisibility(View.GONE);
                            pressureRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    pressTV.setText("Pressure is "+dataSnapshot.getValue()+" Pa.");
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            pressReq.removeEventListener(this);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                };
                pressReq.setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.VISIBLE);
                        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        pressReq.addValueEventListener(valueEventListener);
                    }
                });
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("View Devices");
    }

    private void prepareDevices() {
        devicesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.v("onDataChange","singlevaluekey");
                deviceList.clear();
                int cnt=0;
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Device d = ds.getValue(Device.class);
                    if(d.getStatus())
                        cnt++;
                    deviceList.add(d);
                }
                if(cnt==0)
                    alloffRef.setValue(true);
                else
                    alloffRef.setValue(false);
                deviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        all_off_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                devicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int i=1;
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            final Device d = ds.getValue(Device.class);
                            Log.d("all-issue","port is "+d.getPort());
                            if(d.getRating()!=-1){
                                //int port = d.getPort();
                                devicesRef.child(Constants.KEY_DEVICE+d.getPort()).child(Constants.KEY_STATUS).setValue(false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("all-issue","success port inside if is "+d.getPort());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("all-issue","failure port inside if is "+d.getPort());
                                    }
                                });
                            }
                            else{
                                Log.d("all-issue","either null or -1 port num "+d.getPort());
                            }
                            i++;
                        }
                        //all_off_button.callOnClick();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        all_on_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                devicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int i=1;
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            Device d = ds.getValue(Device.class);
                            if (d != null && d.getRating() != -1) {
                                //int port = d.getPort();
                                devicesRef.child(Constants.KEY_DEVICE +d.getPort()).child(Constants.KEY_STATUS).setValue(true);
                            }
                            else{
                                Log.d("all-issue","either null or -1 port num "+d.getPort());
                            }
                            i++;
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

}
