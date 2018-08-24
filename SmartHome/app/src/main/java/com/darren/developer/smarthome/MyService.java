package com.darren.developer.smarthome;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.darren.developer.smarthome.data.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyService extends Service {
    public MyService() {
    }

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference alertRef;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sp = getSharedPreferences(Constants.KEY_SP_UNAME,0);
        final String uname = sp.getString(Constants.KEY_SP_UNAME,"");
        if(uname.equals("")){
            stopSelf();
        }
        else {
            alertRef = database.getReference(uname).child("alert");
            Intent p = new Intent(this, MainActivity.class);
            p.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, p, 0);

            long[] vibrate = {500,1000};
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("Alert!")
                    .setContentText("You may have left your lights on. Please check it.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setVibrate(vibrate)
                    .setSound(alarmSound)
                    .setContentIntent(pendingIntent);
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            alertRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean b = dataSnapshot.getValue(Boolean.class);
                    if (b && !uname.equals("")) {
                        notificationManager.notify(1, mBuilder.build());
                        alertRef.setValue(false);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       // Toast.makeText(getApplicationContext(),"Service binded",Toast.LENGTH_SHORT).show();
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
