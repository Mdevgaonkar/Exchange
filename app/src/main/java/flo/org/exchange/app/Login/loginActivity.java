package flo.org.exchange.app.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pkmmte.view.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import flo.org.exchange.R;
import flo.org.exchange.app.Home.MainHomeActivity;
import flo.org.exchange.app.utils.ConnectivityReceiver;
import flo.org.exchange.app.utils.PersonGSON;
import flo.org.exchange.app.utils.campusExchangeApp;
import flo.org.exchange.app.utils.credentials;

/**
 * Created by Mayur on 01/11/16.
 */

public class loginActivity extends AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        AdapterView.OnItemSelectedListener,
        ConnectivityReceiver.ConnectivityReceiverListener{


    private static final String TAG = "SignInActivity";
    private static boolean NETWORK_STATE = false;
    private static final int RC_SIGN_IN = 007;

    private GoogleApiClient mGoogleApiClient;
    private SignInButton signInButton;
    private ProgressDialog mProgressDialog;

    private LinearLayout welcomeLayout, loginLayout, splashLayout;
    private CoordinatorLayout activity_login;
    private CircularImageView profile_img;
    private TextView profile_name, skip_button, college_list;
    private Spinner collegeSpinner, courseSpinner, yearSpinner;
    private TextView select1,select2,select3;
    private Button btn_lets_start;

    Person person;

//    Volley requests

    // Tag used to cancel the request
    String tag_json_obj = "json_obj_req";
    private static final String GET_COLLEGES_URL = "http://xchange.hol.es/readColleges.php";

//    GSON instances
    private Gson gson;
    private static final String RESPONSE_DATA = "data";
    List<College> colleges;
    List<String> collegesStringArray;
    List<College.Branch> branches;
    List<String> branchesStringArray;






    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        activity_login = (CoordinatorLayout) findViewById(R.id.activity_login);
        checkConnection();
        splashLayout = (LinearLayout) findViewById(R.id.splashLayout);
        welcomeLayout = (LinearLayout) findViewById(R.id.welcomeLayout);
        loginLayout = (LinearLayout) findViewById(R.id.loginLayout);
        updateUI(2);

        skip_button = (TextView) findViewById(R.id.skip_button);
        skip_button.setOnClickListener(this);

        profile_img = (CircularImageView) findViewById(R.id.profile_img);
        profile_name = (TextView) findViewById(R.id.profile_name);
        college_list = (TextView) findViewById(R.id.college_list);

        btn_lets_start = (Button) findViewById(R.id.btn_lets_start);
        btn_lets_start.setOnClickListener(this);

        person = new Person(this);
        setUpGSON();

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

        // [START customize_button]
        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes and color schemes. It can also be contextually
        // rendered based on the requested scopes. For example. a red button may
        // be displayed when Google+ scopes are requested, but a white button
        // may be displayed when only basic profile is requested. Try adding the
        // Scopes.PLUS_LOGIN scope to the GoogleSignInOptions to see the
        // difference.

        signInButton = (SignInButton) findViewById(R.id.btn_sign_in);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
        signInButton.setOnClickListener(this);
        // [END customize_button]
    }

    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        NETWORK_STATE = isConnected;
        showNetworkStateSnack(isConnected);
    }


    //    Collect Info
    private void collectInfo(){

        String personCollegeName = collegeSpinner.getSelectedItem().toString();
        String collegeObjectID = colleges.get(collegeSpinner.getSelectedItemPosition()-1).objectId;
        String collegeLocation = colleges.get(collegeSpinner.getSelectedItemPosition()-1).location;
        String collegeShort = colleges.get(collegeSpinner.getSelectedItemPosition()-1).collegeShort;
        String personCourseName = courseSpinner.getSelectedItem().toString();
        String courseObjectId = branches.get(courseSpinner.getSelectedItemPosition()-1).objectId;
        String courseShort = branches.get(courseSpinner.getSelectedItemPosition()-1).branchShort;
        String personYear = yearSpinner.getSelectedItem().toString();
        person.setCollegeName(personCollegeName);
        person.setPersonCollegeObjectId(collegeObjectID);
        person.setPersonCollegeLocation(collegeLocation);
        person.setPersonCollegeShort(collegeShort);
        person.setCourse(personCourseName);
        person.setPersonCourseoBjectId(courseObjectId);
        person.setPersonCourseShort(courseShort);
        person.setAcademicYear(personYear);
        person.setPersonInfoCollected("true");

    }

    private boolean checkFieldErrors() {
        boolean returnValue1=true,returnValue2=true,returnValue3=true;
        if (collegeSpinner.getSelectedItemPosition() == 0){
            collegeSpinner.setBackground(getResources().getDrawable(R.drawable.field_error));
            returnValue1 = false;
        }
        if(courseSpinner.getSelectedItemPosition() == 0){
            courseSpinner.setBackground(getResources().getDrawable(R.drawable.field_error));
            returnValue2 = false;
        }
        if(yearSpinner.getSelectedItemPosition() == 0){
            yearSpinner.setBackground(getResources().getDrawable(R.drawable.field_error));
            returnValue3 = false;
        }
        return returnValue1 && returnValue2 && returnValue3;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_sign_in:
                if(NETWORK_STATE){
                    signIn();
                }else {
                    showNetworkStateSnack(NETWORK_STATE);
                }
                break;
            case R.id.skip_button:
                updateUI(1);
                break;
            case R.id.btn_lets_start:


                    if (checkFieldErrors()) {
                        collectInfo();

                        if (Boolean.valueOf(person.getPersonPresent())) {
                            if (NETWORK_STATE) {
                                createUser();
                            }
                            else {
                                showNetworkStateSnack(NETWORK_STATE);
                            }
                        } else {
                            getCredentials();
                        }
//                        StartMainHomeActivity();
                    } else {
                        Snackbar.make(activity_login, R.string.infoCollectionError, Snackbar.LENGTH_LONG).show();
                    }

                break;

        }
    }

    private void createUser() {
        showProgressDialog(getResources().getString(R.string.setting_up));
        String createUserString = getResources().getString(R.string.createUser);
        createUserString = createUserString+"?";
        try {
            createUserString = createUserString + "name=" + URLEncoder.encode(person.getPersonName(), "UTF-8");
            createUserString = createUserString+"&contactNumber="+ URLEncoder.encode(person.getPhoneNumber(),"UTF-8");
            createUserString = createUserString+"&email="+URLEncoder.encode(person.getPersonEmail(),"UTF-8");
            createUserString = createUserString+"&college="+URLEncoder.encode(person.getPersonCollegeObjectId(),"UTF-8");
            createUserString = createUserString+"&course="+URLEncoder.encode(person.getPersonCourseoBjectId(),"UTF-8");
            createUserString = createUserString+"&courseYear="+URLEncoder.encode(person.getAcademicYear(),"UTF-8");
            createUserString = createUserString+"&profilepic="+URLEncoder.encode(person.getPersonPhotoUrl(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


//        Snackbar.make(activity_login, R.string.setup_complete, Snackbar.LENGTH_LONG).show();
        Log.d(TAG+"CUser", createUserString);
//        hideProgressDialog();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                createUserString, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String personObjectId = null;
                        try {
                            hideProgressDialog();
                            if (response.isNull("objectId")) {
                                if (response.getInt("code") == 3033){
                                    Snackbar.make(activity_login," person already exists ", Snackbar.LENGTH_LONG).show();
                                    readUser();
                                }
                            } else {
                                personObjectId = response.getString("objectId");
//                                Snackbar.make(activity_login, R.string.setup_complete + " " + personObjectId, Snackbar.LENGTH_LONG).show();
                                person.setPersonObjectId(personObjectId);
                                getCredentials();//StartMainHomeActivity();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG+" :while creating user", "Error: " + error.getMessage());
            }
        });

        // Adding request to request queue
        campusExchangeApp.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj+":CreatingUser");

    }

    private void readUser(){

        showProgressDialog(getResources().getString(R.string.readingUser));
        String readUserString = getResources().getString(R.string.readUser);
        readUserString = readUserString+"?";
        try {
            readUserString = readUserString + "email=" + URLEncoder.encode(person.getPersonEmail(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Log.d(TAG+"ReUser", readUserString);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                readUserString, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String personObjectId = null;
                        try {
                            hideProgressDialog();
                                if (response.getInt("totalObjects") == 1){

                                    String responsePersonData = response.getJSONArray(RESPONSE_DATA).toString();
                                    List<PersonGSON> personG =  Arrays.asList(gson.fromJson(responsePersonData,PersonGSON[].class));
                                    for(PersonGSON recognisedUser: personG){
                                        personObjectId = recognisedUser.objectId;
                                        person.setPersonObjectId(personObjectId);
                                    }
//                                        Snackbar.make(activity_login," person already exists ", Snackbar.LENGTH_LONG).show();
                                        updateUser();
//                                        StartMainHomeActivity();

                            } else {

                                Snackbar.make(activity_login, R.string.noUser, Snackbar.LENGTH_LONG).show();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG+" :while creating user", "Error: " + error.getMessage());
            }
        });

        // Adding request to request queue
        campusExchangeApp.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj+":CreatingUser");


    }

    private void updateUser() {
        showProgressDialog(getResources().getString(R.string.updatingUser));
        String updateUserString = getResources().getString(R.string.updateUser);
        updateUserString = updateUserString+"?";
        try {
            updateUserString = updateUserString+ "objectId=" + URLEncoder.encode(person.getPersonObjectId(), "UTF-8");
            updateUserString = updateUserString+ "&name=" + URLEncoder.encode(person.getPersonName(), "UTF-8");
            updateUserString = updateUserString+"&contactNumber="+ URLEncoder.encode(person.getPhoneNumber(),"UTF-8");
//            updateUserString = updateUserString+"&email="+URLEncoder.encode(person.getPersonEmail(),"UTF-8");
            updateUserString = updateUserString+"&college="+URLEncoder.encode(person.getPersonCollegeObjectId(),"UTF-8");
            updateUserString = updateUserString+"&course="+URLEncoder.encode(person.getPersonCourseoBjectId(),"UTF-8");
            updateUserString = updateUserString+"&courseYear="+URLEncoder.encode(person.getAcademicYear(),"UTF-8");
            updateUserString = updateUserString+"&profilepic="+URLEncoder.encode(person.getPersonPhotoUrl(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Log.d(TAG+"UpUser", updateUserString);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                updateUserString, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String personObjectId = null;

                            hideProgressDialog();
                        try {
                            Log.d("Person ObjectId", response.getString("objectId"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (response.isNull("objectId")) {
                            Snackbar.make(activity_login, R.string.NetworkError, Snackbar.LENGTH_LONG).show();
                            }else {
                                getCredentials();

                            }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG+" :while creating user", "Error: " + error.getMessage());
            }
        });

        // Adding request to request queue
        campusExchangeApp.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj+":CreatingUser");


    }

    private void getCredentials() {
        showProgressDialog(getResources().getString(R.string.gettingCredentials));
        final String credentialString = getResources().getString(R.string.credentialFetcher);


        Log.d(TAG+"UpUser", credentialString);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                credentialString, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if (response.isNull("application-id")) {
                            getCredentials();
                        }else {
                            Log.d("credentials", response.toString());
                            credentials c = new credentials(getApplicationContext());
                            c.setCredentials(response.toString());
                            StartMainHomeActivity();
                        }
                        hideProgressDialog();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG+" :while creating user", "Error: " + error.getMessage());
            }
        });

        // Adding request to request queue
        campusExchangeApp.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj+":FetchingCredentials");

    }

    private void StartMainHomeActivity() {

        finish();
        Intent homeAction = new Intent(this, MainHomeActivity.class);
        homeAction.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeAction);
    }



    // [START signIn]
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]


    @Override
    protected void onResume() {
        super.onResume();
        // register connection status listener
        campusExchangeApp.getInstance().setConnectivityListener(this);
        checkCacheLogin();
    }

    @Override
    public void onStart() {
        super.onStart();
        checkCacheLogin();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgressDialog.dismiss();
    }

    public void checkCacheLogin(){
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog(getResources().getString(R.string.loading));
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }
    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }
    // [END onActivityResult]

    private void getDetailsFromAcount(GoogleSignInResult result){
        GoogleSignInAccount acct = result.getSignInAccount();

        Log.e(TAG, "display name: " + acct.getDisplayName());

        String personName = acct.getDisplayName();
        person.setPersonName(personName);
        try {
            String personPhotoUrl = acct.getPhotoUrl().toString();
            person.setPersonPhotoUrl(personPhotoUrl);
        }catch (NullPointerException e){
            person.setPersonPhotoUrl("null");
        }

        String email = acct.getEmail();
        person.setPersonEmail(email);

        Log.e(TAG, "Name: " + personName + ", email: " + email
                + ", Image: " + person.getPersonPhotoUrl());

    }

    private void  setDetailsFromAcount(Person person){
        String personName = person.getPersonName();
        String personPhotoUrl = person.getPersonPhotoUrl();
        String personEmail = person.getPersonEmail();

        profile_name.setText(personName);

        try {
            Glide.with(getApplicationContext()).load(personPhotoUrl)
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(profile_img);
        }catch (NullPointerException e) {
            Glide.with(getApplicationContext()).load(R.drawable.ic_default_avatar).into(profile_img);
        }

    }

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            getDetailsFromAcount(result);
            setDetailsFromAcount(person);
            Log.d(TAG, "Updated UI");
            updateUI(1);
            person.setPersonPresent("true");
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(2);
        }
    }
    // [END handleSignInResult]

    private void showProgressDialog(String progressString) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(progressString);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.setMessage(progressString);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void updateUI(int i){
        switch (i){
            case 1 ://signed in UI or skipped UI
                splashLayout.setVisibility(View.GONE);
                loginLayout.setVisibility(View.GONE);
                welcomeLayout.setVisibility(View.VISIBLE);
                if (NETWORK_STATE) {
                    getCollegeList();
                }else {
                    showNetworkStateSnack(NETWORK_STATE);
                }

                break;
            case 2 ://signed out UI
                splashLayout.setVisibility(View.VISIBLE);
                loginLayout.setVisibility(View.VISIBLE);
                welcomeLayout.setVisibility(View.GONE);
                break;
        }
    }

    private void setUpGSON(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    private void getCollegeList(){
        //get list of colleges from server

        showProgressDialog(getResources().getString(R.string.loading));
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                GET_COLLEGES_URL, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String responseData = response.getJSONArray(RESPONSE_DATA).toString();
                            colleges = Arrays.asList(gson.fromJson(responseData,College[].class));
                            collegesStringArray = new ArrayList<String>();
                            collegesStringArray.add(getResources().getString(R.string.select_your_college));
                            for(College college: colleges){
                                collegesStringArray.add(college.collegeName);
                            }
                            setupSpinners(collegesStringArray);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.d(TAG, response.toString());
                        hideProgressDialog();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                // hide the progress dialog
                hideProgressDialog();
            }
        });

// Adding request to request queue
        campusExchangeApp.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    private void setupSpinners(List<String> collegesStringArray) {
        collegeSpinner = (Spinner) findViewById(R.id.select_college_spinner);
        spinnerAdapter collegeSpinnerAdapter = new spinnerAdapter(this,android.R.layout.simple_spinner_dropdown_item,collegesStringArray);
        collegeSpinner.setAdapter(collegeSpinnerAdapter);
        collegeSpinner.setOnItemSelectedListener(this);
        collegeSpinner.setVisibility(View.VISIBLE);

        courseSpinner = (Spinner) findViewById(R.id.select_course_spinner);
        branchesStringArray = new ArrayList<String>();
        branchesStringArray.add(getResources().getString(R.string.select_your_course));
        spinnerAdapter courseSpinnerAdapter = new spinnerAdapter(this,android.R.layout.simple_spinner_dropdown_item,branchesStringArray);
        courseSpinner.setAdapter(courseSpinnerAdapter);
        courseSpinner.setOnItemSelectedListener(this);
        courseSpinner.setEnabled(false);
        courseSpinner.setClickable(false);
        courseSpinner.setVisibility(View.VISIBLE);

        yearSpinner = (Spinner) findViewById(R.id.select_year_spinner);
        spinnerAdapter yearSpinnerAdapter = new spinnerAdapter(this,android.R.layout.simple_spinner_dropdown_item,Arrays.asList(getResources().getStringArray(R.array.courseYears)));
        yearSpinner.setAdapter(yearSpinnerAdapter);
        yearSpinner.setOnItemSelectedListener(this);
        yearSpinner.setVisibility(View.VISIBLE);

        if(person.getPersonInfoCollected().equals("true")){
                collegeSpinner.setSelection(collegesStringArray.indexOf(person.getCollegeName()));

                if(collegeSpinner.getSelectedItemPosition() > 0 ) {
                    branches = colleges.get(collegeSpinner.getSelectedItemPosition() - 1).branches;
                    for (College.Branch branch : branches) {
                        branchesStringArray.add(branch.branch);
                    }
                    courseSpinner.setSelection(branchesStringArray.indexOf(person.getCourse()));
                }

                yearSpinner.setSelection(Arrays.asList(getResources().getStringArray(R.array.courseYears)).indexOf(person.getAcademicYear()));
        }


    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.select_college_spinner :
                courseSpinner.setEnabled(true);
                courseSpinner.setClickable(true);
                collegeSpinner.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
                courseSpinner.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
                if(position > 0) {
                    branches = colleges.get(position-1).branches;
                    for (College.Branch branch : branches) {
                        branchesStringArray.add(branch.branch);
                    }
//                    Toast.makeText(getBaseContext(), parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.select_course_spinner :
                if(position > 0) {
//                    Toast.makeText(getBaseContext(), parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
                    courseSpinner.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
                }
                break;
            case R.id.select_year_spinner :
                if(position > 0){
//                    Toast.makeText(getBaseContext(), parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
                    yearSpinner.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
                }


                break;
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
            timePeriod = Snackbar.LENGTH_LONG;
            color = Color.WHITE;
        } else {
            message = getString(R.string.NetworkFaliure);
            timePeriod = Snackbar.LENGTH_INDEFINITE;
            color = Color.RED;
        }

        Snackbar snackbar = Snackbar
                .make(activity_login, message, timePeriod);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }
}
