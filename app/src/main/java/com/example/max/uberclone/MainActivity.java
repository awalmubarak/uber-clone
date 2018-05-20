package com.example.max.uberclone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends AppCompatActivity {
    Switch userSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userSwitch = (Switch) findViewById(R.id.userSwitch);

        if (ParseUser.getCurrentUser() == null) {

            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e == null) {
                        Log.i("Info", "Login Success");

                    }else{
                        e.printStackTrace();
                    }
                }
            });
        }else{

            Log.i("Info", "Already Logged in");

            if (ParseUser.getCurrentUser().get("driverOrRider") != null){

                String userType = ParseUser.getCurrentUser().getString("driverOrRider");

                Log.i("Not Null", userType);

                redirect(userType);
            }
        }
    }

    public void getStarted(View v){

        String userType = "rider";
        if (userSwitch.isChecked()) userType = "driver";

        Log.i("Info", userType);

        if (ParseUser.getCurrentUser() != null) {
            saveUserState(userType);

        }else{
            final String finalUserType = userType;
            ParseAnonymousUtils.logIn(new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e == null) {
                            saveUserState(finalUserType);

                        }else{
                            e.printStackTrace();
                        }
                    }
                });

            }




    }

    public void redirect(String userType){
            if (userType.equals("rider")){
                Intent i = new Intent(getApplicationContext(), RiderActivity.class);
                startActivity(i);

            }else{
                if (userType.equals("driver")){
                    Intent i = new Intent(getApplicationContext(), RequestListActivity.class);
                    startActivity(i);
                }
            }

    }

    public void saveUserState(String userType){
        ParseUser.getCurrentUser().put("driverOrRider", userType);
        final String finalUserType = userType;
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    redirect(finalUserType);
                } else {
                    e.printStackTrace();
                }
            }
        });

    }
}
