package flo.org.campusmein.app.Login;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import flo.org.campusmein.R;
import flo.org.campusmein.app.Home.MainHomeActivity;
import flo.org.campusmein.app.utils.Person;

public class splashActivity extends AppCompatActivity {

    Person person;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms

                decideAction();
            }
        }, 3000);
    }

    private void decideAction(){
        person = new Person(getApplicationContext());
        if(Boolean.valueOf(person.getPersonPresent())){
            if (Boolean.valueOf(person.getPersonInfoCollected())){
                HomeAction();
            }else {
                SignInAction();
            }
        }else if(Boolean.valueOf(person.getPersonInfoCollected())){
            HomeAction();
        }else {
            SignInAction();
        }

    }

    private void SignInAction(){
        finish();
        Intent signInAction = new Intent(this, login.class);
        startActivity(signInAction);
    }
    private void HomeAction(){
        finish();
        Intent homeAction = new Intent(this, MainHomeActivity.class);
        startActivity(homeAction);
    }
}
