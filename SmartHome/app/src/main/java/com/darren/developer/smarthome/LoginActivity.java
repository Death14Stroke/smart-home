package com.darren.developer.smarthome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.darren.developer.smarthome.data.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameET, passwordET;
    private Button loginBtn, signupBtn;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference userRef = database.getReference();
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameET = findViewById(R.id.usernameET);
        passwordET = findViewById(R.id.passwordET);
        loginBtn = findViewById(R.id.loginBtn);
        signupBtn = findViewById(R.id.signupBtn);
        sp=getSharedPreferences(Constants.KEY_SP_UNAME,0);
        String uname = sp.getString(Constants.KEY_SP_UNAME,"");
        String pass = sp.getString(Constants.KEY_SP_PASS,"");
        usernameET.setText(uname);
        passwordET.setText(pass);
        if(!uname.isEmpty()){
            finish();
            startActivity(new Intent(LoginActivity.this,MainActivity.class));
        }
        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String uname = usernameET.getText().toString();
                final String pass = passwordET.getText().toString();
                if(pass.isEmpty()){
                    passwordET.setError("Please set a password");
                    return;
                }
                userRef.orderByKey().equalTo(uname).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildrenCount()==0) {
                            userRef.child(uname).child(Constants.KEY_PASS).setValue(pass);
                            for(int i=1;i<=6;i++){
                                userRef.child(uname).child(Constants.DEVICES_KEY).child(Constants.KEY_DEVICE+String.valueOf(i)).child(Constants.KEY_RATING).setValue(-1);
                                userRef.child(uname).child(Constants.DEVICES_KEY).child(Constants.KEY_DEVICE+String.valueOf(i)).child(Constants.KEY_PORT).setValue(i);
                            }
                            userRef.child(uname).child(Constants.TEMP_KEY).setValue(0);
                            userRef.child(uname).child(Constants.PRESS_KEY).setValue(0);
                            userRef.child(uname).child(Constants.KEY_ALLOFF).setValue(false);
                            userRef.child(uname).child(Constants.KEY_ALERT).setValue(false);
                            Toast.makeText(getApplicationContext(),"You can now log in",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"You are already registered",Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String uname = usernameET.getText().toString();
                final String pass = passwordET.getText().toString();
                userRef.orderByKey().equalTo(uname).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildrenCount()==0) {
                            usernameET.setError("Invalid username");
                            return;
                        }
                        String p=dataSnapshot.child(uname).child(Constants.KEY_PASS).getValue(String.class);
                        if(p.equals(pass)){
                            Constants.CURRENT_USER_NAME=uname;
                            Constants.CURRENT_USER_PASS=pass;
                            sp.edit().putString(Constants.KEY_SP_UNAME,uname).apply();
                            sp.edit().putString(Constants.KEY_PASS,pass).apply();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            finish();
                            startActivity(intent);
                        }
                        else{
                            passwordET.setError("Invalid password");
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
