package flo.org.exchange.app.Login;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.pkmmte.view.CircularImageView;

import flo.org.exchange.R;
import flo.org.exchange.app.utils.ConnectivityReceiver;
import flo.org.exchange.app.utils.campusExchangeApp;

public class profile_options extends AppCompatActivity
        implements View.OnClickListener,
                    GoogleApiClient.OnConnectionFailedListener,
                    ConnectivityReceiver.ConnectivityReceiverListener {

    private static boolean NETWORK_STATE = false;
    private GoogleApiClient mGoogleApiClient;
    private Toolbar activityToolbar;

    private CoordinatorLayout activity_profile_options;
    private CircularImageView person_photo;
    private TextView person_name,person_college;
    private LinearLayout order_history,RateApp, EditProfile;
    private Button btn_sign_out;

    Person person = new Person(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_options);

        setupGoogleSignOut();


        setupActionbar();


        setupViews();
        setupStaticInfo();

    }

    private void setupGoogleSignOut() {
        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // [END build_client]
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupStaticInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register connection status listener
        campusExchangeApp.getInstance().setConnectivityListener(this);
        setupStaticInfo();

    }

    private void setupActionbar() {
        activityToolbar = (Toolbar) findViewById(R.id.action_bar_profile_options);
        setSupportActionBar(activityToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupStaticInfo() {
        String personName = person.getPersonName();
        String personPhotoUrl = person.getPersonPhotoUrl();
        String personCollege = person.getCollegeName();

        person_name.setText(personName);
        person_college.setText(personCollege);
        try {
            Glide.with(getApplicationContext()).load(Uri.parse(personPhotoUrl))
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(person_photo);


        }catch (NullPointerException e) {
            person_photo.setImageResource(R.drawable.ic_default_avatar);
        }

    }

    private void setupViews() {
        //        Coordinator layout
        activity_profile_options = (CoordinatorLayout) findViewById(R.id.activity_profile_options);
        checkConnection();
//        Static
        person_photo = (CircularImageView) findViewById(R.id.profile_img_profile_view);
        person_name = (TextView) findViewById(R.id.person_name_profile_view);
        person_college  = (TextView) findViewById(R.id.person_college_profile_view);

//        Dynamic
        order_history = (LinearLayout) findViewById(R.id.order_history);
        order_history.setOnClickListener(this);
        RateApp = (LinearLayout) findViewById(R.id.RateApp);
        RateApp.setOnClickListener(this);
        EditProfile = (LinearLayout) findViewById(R.id.EditProfile);
        EditProfile.setOnClickListener(this);

        btn_sign_out = (Button) findViewById(R.id.btn_sign_out);
        btn_sign_out.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(NETWORK_STATE){
        switch (v.getId()){
            case R.id.order_history:
                break;
            case R.id.RateApp:
                break;
            case R.id.EditProfile:
                Intent profile_edit_screen = new Intent(this, flo.org.exchange.app.Login.EditProfile.class);
                startActivity(profile_edit_screen);
                break;
            case R.id.btn_sign_out:
                signOut();
                break;
        }
        }
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        person.setPersonPresent("false");
                        person.setPersonInfoCollected("false");
                        finish();
                        Intent signOut = new Intent(profile_options.this, splashActivity.class);
                        signOut.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(signOut);
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showNetworkStateSnack(NETWORK_STATE);

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        NETWORK_STATE = isConnected;
        showNetworkStateSnack(isConnected);

    }

    // Showing the status in Snackbar
    private void showNetworkStateSnack(boolean isConnected) {
        String message;
        int color;
        int timePeriod;
        if (isConnected) {
            message = getString(R.string.good_connection);
            timePeriod = Snackbar.LENGTH_SHORT;
            color = Color.WHITE;
        } else {
            message = getString(R.string.NetworkFaliure);
            timePeriod = Snackbar.LENGTH_INDEFINITE;
            color = Color.RED;
        }

        Snackbar snackbar = Snackbar
                .make(activity_profile_options, message, timePeriod);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        NETWORK_STATE = isConnected;
        showNetworkStateSnack(isConnected);
    }
}
