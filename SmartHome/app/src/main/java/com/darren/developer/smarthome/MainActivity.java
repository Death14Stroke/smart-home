package com.darren.developer.smarthome;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.darren.developer.smarthome.data.Constants;
import com.darren.developer.smarthome.model.Device;
import com.darren.developer.smarthome.model.TTSManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference devicesRef,tempRef,pressRef,timeRef,tempReqRef,pressReqRef,readTempRef,readPressRef;
    String [] numbers = {"zero","one","two","three","four","five","six","seven","eight","nine"};
    TTSManager ttsManager = null;
    private TextView textView;
    private ProgressBar progressBar;
    private ChildEventListener tempCEL, pressCEL;
    SharedPreferences sp;

    @Override
    protected void onStart() {
        super.onStart();
        String text="Welcome back, "+Constants.CURRENT_USER_NAME;
        ttsManager.initQueue(text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = getSharedPreferences(Constants.KEY_SP_UNAME,0);
        Constants.CURRENT_USER_NAME = sp.getString(Constants.KEY_SP_UNAME,"");
        Constants.CURRENT_USER_PASS = sp.getString(Constants.KEY_SP_PASS,"");
        devicesRef = database.getReference(Constants.CURRENT_USER_NAME).child(Constants.DEVICES_KEY);
        tempRef = database.getReference(Constants.CURRENT_USER_NAME).child(Constants.TEMP_KEY);
        pressRef = database.getReference(Constants.CURRENT_USER_NAME).child(Constants.PRESS_KEY);
        timeRef = database.getReference(Constants.CURRENT_USER_NAME).child(Constants.KEY_STSP);
        tempReqRef = database.getReference(Constants.CURRENT_USER_NAME).child(Constants.KEY_TEMPREQ);
        pressReqRef = database.getReference(Constants.CURRENT_USER_NAME).child(Constants.KEY_PRESSREQ);
        progressBar = findViewById(R.id.progressBarMain);
        tempRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Constants.CURRENT_TEMP=dataSnapshot.getValue(Float.class);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        pressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Constants.CURRENT_PRESSURE=dataSnapshot.getValue(Float.class);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getApplicationContext().registerReceiver(
                new MyReceiver(),
                new IntentFilter(
                        ConnectivityManager.CONNECTIVITY_ACTION));

        FloatingActionButton fab = findViewById(R.id.fab);
        ttsManager = new TTSManager();
        ttsManager.init(this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptSpeechInput();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        textView = navigationView.getHeaderView(0).findViewById(R.id.textView);
        textView.setText(Constants.CURRENT_USER_NAME);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        displayFragment(R.id.nav_view);
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say A Command in "+Locale.getDefault());
        try{
            startActivityForResult(intent, Constants.SPEECH_INPUT);
        }
        catch (ActivityNotFoundException e){
            Toast.makeText(getApplicationContext(),"Speech not supported!",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case Constants.SPEECH_INPUT:
                if(resultCode==RESULT_OK && data!=null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    final boolean[] success = {false};
                    for(int i=0;i<result.size();i++) {
                        final String s = result.get(i).toLowerCase().replaceAll("\\s","");
                        Log.i("speech results", "speech is "+s);
                        //Toast.makeText(getApplicationContext(),"speech is "+s,Toast.LENGTH_SHORT).show();
                        if(s.contains("temperature")){
                            speakTemperature();
                            success[0]=true;
                        }
                        else if(s.contains("pressure")){
                            speakPressure();
                            success[0]=true;
                        }
                        else if(s.contains("date") || s.contains("today") || s.contains("time")){
                            speakTime();
                            success[0]=true;
                        }
                        else if(s.contains("turnalloff")){
                            devicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    int cnt=0;
                                    for(DataSnapshot ds:dataSnapshot.getChildren()){
                                        final Device d = ds.getValue(Device.class);
                                        final boolean stat = d.getStatus();
                                        if(d.getRating()!=-1 && !d.getDname().isEmpty() && d.getStatus()) {
                                            devicesRef.child(Constants.KEY_DEVICE + d.getPort()).child(Constants.KEY_STATUS).setValue(false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    if(stat) {
                                                        timeRef.child("d" + d.getPort()).child(Constants.KEY_STOP_TIME).setValue(String.valueOf(System.currentTimeMillis()/1000));
                                                    }
                                                }
                                            });
                                            cnt++;
                                        }
                                    }
                                    if(cnt==0){
                                        String text="All devices are already off";
                                        ttsManager.initQueue(text);
                                        success[0]=true;
                                    }
                                    else {
                                        String text = "All devices now turned off";
                                        ttsManager.initQueue(text);
                                        success[0] = true;
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                        else if(s.contains("turnallon")){
                            devicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    int cnt=0;
                                    for(DataSnapshot ds:dataSnapshot.getChildren()){
                                        final Device d = ds.getValue(Device.class);
                                        final boolean stat = d.getStatus();
                                        if(d.getRating()!=-1 && !d.getDname().isEmpty() && !d.getStatus()) {
                                            devicesRef.child(Constants.KEY_DEVICE + d.getPort()).child(Constants.KEY_STATUS).setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    if(!stat){
                                                        timeRef.child("d"+d.getPort()).child(Constants.KEY_START_TIME).setValue(String.valueOf(System.currentTimeMillis()/1000));
                                                    }
                                                }
                                            });
                                            cnt++;
                                        }
                                    }
                                    if(cnt==0){
                                        String text="All devices are already on";
                                        ttsManager.initQueue(text);
                                        success[0]=true;
                                    }
                                    else {
                                        String text = "All devices now turned on";
                                        ttsManager.initQueue(text);
                                        success[0] = true;
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });

                        }
                        else if(s.contains("port")){
                            devicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                                        final Device d = ds.getValue(Device.class);
                                        if((s.contains(String.valueOf(d.getPort())) || s.contains(numbers[d.getPort()]))){
                                            //         Toast.makeText(getApplicationContext(),"rating : "+d.getRating(),Toast.LENGTH_SHORT).show();
                                            Log.i("rating","is the rating");
                                            final String[] text = {"Port " + numbers[d.getPort()]};
                                            if(d.getDname().isEmpty() && d.getRating()==-1){
                                                text[0] +=" is currently unavailable";
                                                ttsManager.initQueue(text[0]);
                                            }
                                            else if(s.contains("turnon")){
                                                if(d.getStatus()){
                                                    text[0] +=" is already on";
                                                    ttsManager.initQueue(text[0]);
                                                }
                                                else{
                                                    devicesRef.child(Constants.KEY_DEVICE+d.getPort()).child(Constants.KEY_STATUS).setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            timeRef.child("d"+d.getPort()).child(Constants.KEY_START_TIME).setValue(String.valueOf(System.currentTimeMillis()/1000));
                                                        }
                                                    });
                                                    text[0] +=" is now turned on";
                                                    ttsManager.initQueue(text[0]);
                                                }
                                                success[0] =true;
                                            }
                                            else if(s.contains("turnoff") || s.contains("turnof") && !s.contains("status")){
                                                if(!d.getStatus()){
                                                    text[0] +=" is already off";
                                                    ttsManager.initQueue(text[0]);
                                                }
                                                else{
                                                    devicesRef.child(Constants.KEY_DEVICE+d.getPort()).child(Constants.KEY_STATUS).setValue(false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            timeRef.child("d"+d.getPort()).child(Constants.KEY_STOP_TIME).setValue(String.valueOf(System.currentTimeMillis()/1000));
                                                        }
                                                    });
                                                    text[0] +=" is now turned off";
                                                    ttsManager.initQueue(text[0]);
                                                }
                                                success[0] =true;
                                            }
                                            else if(s.contains("status")){
                                                text[0] +=" is currently turned ";
                                                if(d.getStatus()) {
                                                    text[0] += "on";
                                                }
                                                else {
                                                    text[0] += "off";
                                                }
                                                ttsManager.initQueue(text[0]);
                                                success[0]=true;
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                        else{
                            devicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                                        final Device d = ds.getValue(Device.class);
                                        final String[] text = {d.getDname()};
                                        Log.i("dname","name is "+d.getDname());
                                        if(s.contains(d.getDname()) && !d.getDname().isEmpty()){
                                            if(s.contains("turnon")){
                                                if(d.getStatus()){
                                                    text[0] +=" is already on";
                                                    ttsManager.initQueue(text[0]);
                                                }
                                                else{
                                                    devicesRef.child(Constants.KEY_DEVICE+d.getPort()).child(Constants.KEY_STATUS).setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            timeRef.child("d"+d.getPort()).child(Constants.KEY_START_TIME).setValue(String.valueOf(System.currentTimeMillis()/1000));
                                                        }
                                                    });
                                                    text[0] +=" is now turned on";
                                                    ttsManager.initQueue(text[0]);
                                                }
                                                success[0]=true;
                                            }
                                            else if(s.contains("turnoff") || s.contains("turnof") && !s.contains("status")){
                                                if(!d.getStatus()){
                                                    text[0] +=" is already off";
                                                    ttsManager.initQueue(text[0]);
                                                }
                                                else{
                                                    devicesRef.child(Constants.KEY_DEVICE+d.getPort()).child(Constants.KEY_STATUS).setValue(false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            timeRef.child("d"+d.getPort()).child(Constants.KEY_STOP_TIME).setValue(String.valueOf(System.currentTimeMillis()/1000));
                                                        }
                                                    });
                                                    text[0] +=" is now turned off";
                                                    ttsManager.initQueue(text[0]);
                                                }
                                                success[0] =true;
                                            }
                                            else if(s.contains("status")){
                                                text[0] +=" is currently turned ";
                                                if(d.getStatus()) {
                                                    text[0] += "on.";
                                                }
                                                else {
                                                    text[0] += "off.";
                                                }
                                                ttsManager.initQueue(text[0]);
                                                success[0]=true;
                                            }
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                        if(success[0])
                            break;
                    }
                    if(!success[0]){
                        String t = "Command not recognized";
                        ttsManager.initQueue(t);
                    }
                }
        }
    }

    private void speakTime() {
        long ms = System.currentTimeMillis();
        Date date = new Date(ms);
        SimpleDateFormat dateformat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        String d = dateformat.format(date);
        ttsManager.initQueue(d);
    }

    private void speakPressure() {
        final ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean status = dataSnapshot.getValue(boolean.class);
                if(!status){
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    progressBar.setVisibility(View.GONE);
                    pressRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String text = "The current pressure is "+dataSnapshot.getValue()+" Pascals.";
                            ttsManager.initQueue(text);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    pressReqRef.removeEventListener(this);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        pressReqRef.setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressBar.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                pressReqRef.addValueEventListener(valueEventListener);
            }
        });
    }

    private void speakTemperature() {
       final ValueEventListener valueEventListener = new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               boolean status = dataSnapshot.getValue(boolean.class);
               if(!status){
                   getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                   progressBar.setVisibility(View.GONE);
                   tempRef.addListenerForSingleValueEvent(new ValueEventListener() {
                       @Override
                       public void onDataChange(DataSnapshot dataSnapshot) {
                           String text = "The current temperature is "+dataSnapshot.getValue()+" degree Celsius.";
                           ttsManager.initQueue(text);
                       }
                       @Override
                       public void onCancelled(DatabaseError databaseError) {

                       }
                   });
                   tempReqRef.removeEventListener(this);
               }
           }
           @Override
           public void onCancelled(DatabaseError databaseError) {
           }
       };
        tempReqRef.setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressBar.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                tempReqRef.addValueEventListener(valueEventListener);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        tempRef.getParent().removeEventListener(tempCEL);
        pressRef.getParent().removeEventListener(pressCEL);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.main_logout) {
            finish();
            sp.edit().putString(Constants.KEY_SP_UNAME,"").apply();
            sp.edit().putString(Constants.KEY_SP_PASS,"").apply();
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayFragment(int itemid){
        Fragment fragment = null;

        //initializing the fragment object which is selected
        switch (itemid) {
            case R.id.nav_view:
                fragment = new ViewDevicesFragment();
                break;
            case R.id.nav_stats:
                fragment = new ViewStatsFragment();
                break;
            case R.id.nav_monthly:
                fragment = new MonthlyFragment();
                break;
            case R.id.nav_help:
                fragment = new HelpFragment();
                break;
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        displayFragment(item.getItemId());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ttsManager.shutDown();
    }
}
