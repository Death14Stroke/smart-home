package com.darren.developer.smarthome.model;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.darren.developer.smarthome.R;
import com.darren.developer.smarthome.data.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.List;

/**
 * Created by user on 31-03-2018.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.MyViewHolder> {

    private Context mContext;
    private List<Device> deviceList;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference deviceRef = database.getReference(Constants.CURRENT_USER_NAME).child(Constants.DEVICES_KEY);
    DatabaseReference timeRef = database.getReference(Constants.CURRENT_USER_NAME).child(Constants.KEY_STSP);

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private EditText deviceName, rating;
        private TextView portNumber;
        private Switch onoffSwitch;
        private ImageView imageView;
        private Button editButton, deleteButton;

        public MyViewHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.tv_device_name);
            rating = itemView.findViewById(R.id.tv_rating);
            portNumber = itemView.findViewById(R.id.tv_port_num);
            onoffSwitch = itemView.findViewById(R.id.onoff_switch);
            imageView = itemView.findViewById(R.id.thumbnail);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);

            onoffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, final boolean b) {
                    final int index = getAdapterPosition();
                    if (b != deviceList.get(index).getStatus()) {
                        deviceRef.child(Constants.KEY_DEVICE + (index + 1)).child(Constants.KEY_STATUS).setValue(b).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.v("timeref", "switch " + index);
                                if (b)
                                    timeRef.child("d" + (index + 1)).child(Constants.KEY_START_TIME).setValue(String.valueOf(System.currentTimeMillis()/1000));
                                else
                                    timeRef.child("d" + (index + 1)).child(Constants.KEY_STOP_TIME).setValue(String.valueOf(System.currentTimeMillis()/1000));
                            }
                        });
                    }
                }
            });
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int index = getAdapterPosition();
                    final boolean status = deviceList.get(index).getStatus();
                    if(deviceList.get(index).getRating()!=-1){
                        deviceRef.child(Constants.KEY_DEVICE+(index+1)).child(Constants.KEY_STATUS).setValue(!status).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.v("timeref","image "+index);
                                if(!status){
                                    timeRef.child("d"+(index+1)).child(Constants.KEY_START_TIME).setValue(String.valueOf(System.currentTimeMillis()/1000));
                                }
                                else{
                                    timeRef.child("d"+(index+1)).child(Constants.KEY_STOP_TIME).setValue(String.valueOf(System.currentTimeMillis()/1000));
                                }
                            }
                        });
                    }
                }
            });
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int portNum = Integer.parseInt(portNumber.getText().toString());
                    Device d = new Device("",false,portNum,-1);
                    deviceRef.child(Constants.KEY_DEVICE+portNum).setValue(d);
                }
            });
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deviceName.setEnabled(!deviceName.isEnabled());
                    rating.setEnabled(!rating.isEnabled());
                    portNumber.setEnabled(!portNumber.isEnabled());
                    if(deviceName.isEnabled()){
                        editButton.setText("Save");
                    }
                    else {
                        editButton.setText("Edit");
                        String dname = deviceName.getText().toString();
                        String x = rating.getText().toString();
                        if(dname.isEmpty() || x.isEmpty()){
                            Toast.makeText(mContext,"Please fill up the details",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        float ratingVal = Float.parseFloat(x);
                        int portNum = getAdapterPosition()+1;
                        boolean status = onoffSwitch.isChecked();
                        Device d = new Device(dname,status,portNum,ratingVal);
                        deviceRef.child(Constants.KEY_DEVICE+portNum).setValue(d);
                    }
                }
            });
        }
    }

    public DeviceAdapter(Context mContext, List<Device> deviceList) {
        this.mContext = mContext;
        this.deviceList = deviceList;
    }

    @Override
    public DeviceAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.devices_card, parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DeviceAdapter.MyViewHolder holder, final int position) {
        Log.v("bindviewholder","size of list is "+deviceList.size()+" position is "+position);
        final Device device = deviceList.get(position);
        holder.deviceName.setText(device.getDname());
        if(device.getRating()==-1) {
            holder.rating.setText("");
            holder.onoffSwitch.setEnabled(false);
            holder.onoffSwitch.setChecked(false);
            holder.imageView.setImageResource(R.drawable.bulb_off);
        }
        else {
            holder.rating.setText(device.getRating() + "");
            holder.onoffSwitch.setEnabled(true);
            holder.onoffSwitch.setChecked(device.getStatus());
            if(device.getStatus())
                holder.imageView.setImageResource(R.drawable.bulb_on);
            else
                holder.imageView.setImageResource(R.drawable.bulb_off);
        }
        holder.portNumber.setText(""+device.getPort());
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }
}
