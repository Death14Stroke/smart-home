package com.darren.developer.smarthome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.darren.developer.smarthome.data.Constants;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = context.getSharedPreferences(Constants.KEY_SP_UNAME,0);
        String uname = sp.getString(Constants.KEY_SP_UNAME,"");
        if(uname.equals("")){
            return;
        }
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        //Toast.makeText(context,"isConnected : "+isConnected,Toast.LENGTH_LONG).show();
        if(isConnected && !uname.equals("")){
            context.startService(new Intent(context,MyService.class));
        }
    }
}
