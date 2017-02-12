package flo.org.campusmein.app.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flo.org.campusmein.R;
import flo.org.campusmein.app.Home.MainHomeActivity;
import flo.org.campusmein.app.Home.cart.cartView;
import flo.org.campusmein.app.utils.BackendlessResponse;
import flo.org.campusmein.app.utils.College;
import flo.org.campusmein.app.utils.ConnectivityReceiver;
import flo.org.campusmein.app.utils.Person;
import flo.org.campusmein.app.utils.PersonGSON;
import flo.org.campusmein.app.utils.campusExchangeApp;
import flo.org.campusmein.app.utils.credentials;

/**
 * Created by Mayur on 31/12/16.
 */

public class login extends AppCompatActivity
        implements View.OnClickListener
                    ,ConnectivityReceiver.ConnectivityReceiverListener
                    ,GoogleApiClient.OnConnectionFailedListener
                    ,AdapterView.OnItemSelectedListener{


    private static final String TAG = login.class.getSimpleName();
    private static final int RC_SIGN_IN = 007;
    private static final String RESPONSE_DATA = "data";
    private CoordinatorLayout activity_login;
    private LinearLayout splashLayout;
    private LinearLayout loginLayout;
    private LinearLayout welcomeLayout;
    private SignInButton btn_sign_in;
    private TextView skip_button;
    private CircularImageView profile_img;
    private TextView profile_name;
    private Button btn_lets_start;
    private AppCompatSpinner select_college_spinner,select_course_spinner,select_year_spinner;
    private boolean NETWORK_STATE = false;
    private ProgressDialog mProgressDialog;

    private GoogleSignInOptions gso;
    private GoogleApiClient mGoogleApiClient;

    private List<College> colleges;
    private List<String> collegesStringArray;
    private List<String> branchesStringArray;
    private List<College.Branch> branches;
    spinnerAdapter collegeSpinnerAdapter;
    spinnerAdapter courseSpinnerAdapter;

    Bundle sourceBundle;
    String srcString = "new";
    private static final String SRC_STR_KEY = "src";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupCoordinatorLayout();
        checkConnection();
        setupGoogleLogin();
        setupSplashLayout();
        setupLoginLayout();
        setupWelcomeLayout();
        setupSource();


    }

    private void setupSource() {
        Intent source = getIntent();
        sourceBundle = source.getExtras();
        if(sourceBundle !=null){
            srcString = sourceBundle.getString(SRC_STR_KEY);
        }
        showOrHideSkipOption();

    }

    private void showOrHideSkipOption(){
        if(srcString.equals("new")){
            skip_button.setVisibility(View.VISIBLE);
        }else if(srcString.equals("cart")){
            skip_button.setVisibility(View.GONE);
        }
    }

    private void setupGoogleLogin() {
        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
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

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            getDetailsFromAccount(result);
            setDetailsFromAccount();
            Log.d(TAG, "Updated UI");
            campusExchangeApp.getInstance().getUniversalPerson().setPersonPresent("true");
            updateUI(1);

        } else {
            // Signed out, show unauthenticated UI.
            updateUI(2);
            campusExchangeApp.getInstance().getUniversalPerson().setPersonPresent("false");

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void  setDetailsFromAccount(){
        String personName = campusExchangeApp.getInstance().getUniversalPerson().getPersonName();
        String personPhotoUrl = campusExchangeApp.getInstance().getUniversalPerson().getPersonPhotoUrl();

        showProfile_name(personName);

        showProfile_img(personPhotoUrl);

    }

    private void getDetailsFromAccount(GoogleSignInResult result) {
        GoogleSignInAccount acct = result.getSignInAccount();



        Log.e(TAG, "display name: " + acct.getDisplayName());

        String personName = acct.getDisplayName();
        campusExchangeApp.getInstance().getUniversalPerson().setPersonName(personName);
        String authCode = acct.getServerAuthCode();
        campusExchangeApp.getInstance().getUniversalPerson().setPersonAuthCode(authCode);
        try {
            String personPhotoUrl = acct.getPhotoUrl().toString();
            campusExchangeApp.getInstance().getUniversalPerson().setPersonPhotoUrl(personPhotoUrl);
        }catch (NullPointerException e){
            campusExchangeApp.getInstance().getUniversalPerson().setPersonPhotoUrl("null");
        }

        String email = acct.getEmail();
        campusExchangeApp.getInstance().getUniversalPerson().setPersonEmail(email);

        Log.e(TAG, "Name: " + personName + ", email: " + email
                + ", Image: " + campusExchangeApp.getInstance().getUniversalPerson().getPersonPhotoUrl());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register connection status listener
        campusExchangeApp.getInstance().setConnectivityListener(this);
        checkCacheLogin();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    /**
     * View methods
     * */

    private void updateUI(int i) {
        switch (i){
            case 1 ://signed in UI or skipped UI

                if (NETWORK_STATE) {
                    show_WelcomView();
                    getCredentials();
                }else {
                    showSnack(getString(R.string.NetworkFaliure), Snackbar.LENGTH_INDEFINITE);
                }

                break;
            case 2 ://signed out UI
                show_LoginView();
                break;
        }
    }

    private void set_campusMe_icon_as_avatar() {
        profile_img.setVisibility(View.VISIBLE);
        profile_img.setImageResource(R.mipmap.ic_launcher);
    }

    private void show_WelcomView(){
        showWelcomeLayout();
        hideSplashLayout();
        hideLoginLayout();
    }

    private void show_LoginView(){
        hideWelcomeLayout();
        showSplashLayout();
        showLoginLayout();
    }

    private void setupWelcomeLayout() {
        welcomeLayout = (LinearLayout) findViewById(R.id.welcomeLayout);
        hideWelcomeLayout();
        setupStaticViews();
        setupSpinners();
    }

    private void setupSpinners() {
        select_college_spinner = (AppCompatSpinner) findViewById(R.id.select_college_spinner);
        hide_college_spinner();
        collegesStringArray = new ArrayList<String>();
        collegeSpinnerAdapter = new spinnerAdapter(this,android.R.layout.simple_spinner_dropdown_item,collegesStringArray);
        select_college_spinner.setAdapter(collegeSpinnerAdapter);
        select_college_spinner.setOnItemSelectedListener(this);

        select_course_spinner = (AppCompatSpinner) findViewById(R.id.select_course_spinner);
        hide_course_spinner();
        branchesStringArray = new ArrayList<String>();
        branchesStringArray.add(getResources().getString(R.string.select_your_course));
        courseSpinnerAdapter = new spinnerAdapter(this,android.R.layout.simple_spinner_dropdown_item,branchesStringArray);
        select_course_spinner.setAdapter(courseSpinnerAdapter);
        select_course_spinner.setOnItemSelectedListener(this);


        select_year_spinner = (AppCompatSpinner) findViewById(R.id.select_year_spinner);
        hide_year_spinner();
        spinnerAdapter yearSpinnerAdapter = new spinnerAdapter(this,android.R.layout.simple_spinner_dropdown_item,Arrays.asList(getResources().getStringArray(R.array.courseYears)));
        select_year_spinner.setAdapter(yearSpinnerAdapter);
        select_year_spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.select_college_spinner :
                select_college_spinner.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
                select_course_spinner.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
                if(position > 0) {
                    branches = colleges.get(position-1).branches;
                    for (College.Branch branch : branches) {
                        branchesStringArray.add(branch.branch);
                    }

                }
                show_course_spinner();
                courseSpinnerAdapter.notifyDataSetChanged();
                break;
            case R.id.select_course_spinner :
                if(position > 0) {
                    select_course_spinner.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
                    show_year_spinner();
                }
                break;
            case R.id.select_year_spinner :
                if(position > 0){
                    select_year_spinner.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
                    showBtnLetsStart();
                }
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void hide_year_spinner() {
        select_year_spinner.setVisibility(View.GONE);
    }

    private void show_year_spinner() {
        select_year_spinner.setVisibility(View.VISIBLE);
    }

    private void hide_course_spinner() {
        select_course_spinner.setVisibility(View.GONE);
        select_course_spinner.setEnabled(false);
        select_course_spinner.setClickable(false);
    }

    private void show_course_spinner() {
        select_course_spinner.setVisibility(View.VISIBLE);
        select_course_spinner.setEnabled(true);
        select_course_spinner.setClickable(true);
    }

    private void hide_college_spinner() {
        select_college_spinner.setVisibility(View.GONE);
    }

    private void show_college_spinner() {
        select_college_spinner.setVisibility(View.VISIBLE);
        collegeSpinnerAdapter.notifyDataSetChanged();
    }

    private void showWelcomeLayout() {
        welcomeLayout.setVisibility(View.VISIBLE);
    }

    private void hideWelcomeLayout() {
        welcomeLayout.setVisibility(View.GONE);
    }

    private void setupStaticViews() {
        profile_img = (CircularImageView) findViewById(R.id.profile_img);
        hideProfile_img();
        profile_name = (TextView) findViewById(R.id.profile_name);
        hideProfile_name();
        btn_lets_start = (Button) findViewById(R.id.btn_lets_start);
        hideBtnLetsStart();
        btn_lets_start.setOnClickListener(this);

    }

    private void hideBtnLetsStart() {
        btn_lets_start.setVisibility(View.GONE);
    }

    private void showBtnLetsStart() {
        btn_lets_start.setVisibility(View.VISIBLE);
    }

    private void hideProfile_img() {
        profile_img.setVisibility(View.GONE);
    }

    private void showProfile_img(String photoUrl) {
        profile_img.setVisibility(View.VISIBLE);
        Glide.with(getApplicationContext()).load(Uri.parse(photoUrl))
                .thumbnail(0.5f)
                .crossFade()
//                    .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(profile_img);
    }

    private void showProfile_name(String name) {
        profile_name.setVisibility(View.VISIBLE);
        profile_name.setText(name);
    }

    private void hideProfile_name() {
        profile_name.setVisibility(View.GONE);
    }

    private void setupLoginLayout() {
        loginLayout = (LinearLayout) findViewById(R.id.loginLayout);
        hideLoginLayout();
        btn_sign_in = (SignInButton) findViewById(R.id.btn_sign_in);
        btn_sign_in.setOnClickListener(this);
        skip_button = (TextView) findViewById(R.id.skip_button);
        skip_button.setOnClickListener(this);
        // [START customize_button]
        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes and color schemes. It can also be contextually
        // rendered based on the requested scopes. For example. a red button may
        // be displayed when Google+ scopes are requested, but a white button
        // may be displayed when only basic profile is requested. Try adding the
        // Scopes.PLUS_LOGIN scope to the GoogleSignInOptions to see the
        // difference.
        btn_sign_in.setSize(SignInButton.SIZE_STANDARD);
        btn_sign_in.setScopes(gso.getScopeArray());
        // [END customize_button]
    }

    private void hideLoginLayout() {
        loginLayout.setVisibility(View.GONE);
    }

    private void showLoginLayout() {
        loginLayout.setVisibility(View.VISIBLE);
    }

    private void setupSplashLayout() {
        splashLayout = (LinearLayout) findViewById(R.id.splashLayout);
        hideSplashLayout();
        showSplashLayout();
    }

    private void showSplashLayout() {
        splashLayout.setVisibility(View.VISIBLE);
    }

    private void hideSplashLayout() {
        splashLayout.setVisibility(View.GONE);
    }

    private void setupCoordinatorLayout() {
        activity_login = (CoordinatorLayout) findViewById(R.id.activity_login);
    }

    private void checkConnection() {
        NETWORK_STATE = ConnectivityReceiver.isConnected();
        if(NETWORK_STATE) {
            showSnack(getString(R.string.good_connection), Snackbar.LENGTH_SHORT);
        }else {
            showSnack(getString(R.string.NetworkFaliure), Snackbar.LENGTH_INDEFINITE);
        }

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.d("networkChanged",isConnected?"Connected":"Not Connected");
        NETWORK_STATE = isConnected;
        if (isConnected) {
            showSnack(getString(R.string.good_connection),Snackbar.LENGTH_SHORT);
        } else {
            showSnack(getString(R.string.NetworkFaliure),Snackbar.LENGTH_INDEFINITE);
        }
    }

    private void showSnack(String snackString, int period) {
        String message;
        message = snackString;
        Snackbar snackbar = Snackbar.make(activity_login, message, period);
        snackbar.show();
    }

    @Override
    public void onClick(View view) {
        if (NETWORK_STATE){
            switch (view.getId()){
                case R.id.btn_sign_in:
                    signIn();
                    break;
                case R.id.skip_button:
                    set_campusMe_icon_as_avatar();
                    updateUI(1);
                    break;
                case R.id.btn_lets_start:
                    //condition based activity start
                    if (checkFieldErrors()) {
                        collectInfo();

                        if (Boolean.valueOf(campusExchangeApp.getInstance().getUniversalPerson().getPersonPresent())) {
//                            createUser();
//                            userCreation();
                            userRead();
                        } else {
//                            getCredentials();
                            deviceRegistration();
                            StartMainHomeActivity();
                        }
//                        StartMainHomeActivity();
                    } else {
                        Snackbar.make(activity_login, R.string.infoCollectionError, Snackbar.LENGTH_LONG).show();
                    }
                    break;
            }
        }else{
            showSnack(getString(R.string.NetworkFaliure),Snackbar.LENGTH_INDEFINITE);
        }
    }

    private void showProgressDialog(String progressString) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(progressString);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.setMessage(progressString);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(NETWORK_STATE){
            campusExchangeApp.getInstance().getmRequestQueue().cancelAll(TAG);
        }
        if(mProgressDialog != null){
            mProgressDialog.dismiss();
        }

    }

    @Override
    public void onBackPressed() {
        if(NETWORK_STATE){
            campusExchangeApp.getInstance().getmRequestQueue().cancelAll(TAG);
        }
        finish();
        super.onBackPressed();

    }

    /**
     * Condition Calls
     */

    private boolean checkFieldErrors() {
        boolean returnValue1=true,returnValue2=true,returnValue3=true;
        if (select_college_spinner.getSelectedItemPosition() == 0){
            select_college_spinner.setBackground(getResources().getDrawable(R.drawable.field_error));
            returnValue1 = false;
        }
        if(select_course_spinner.getSelectedItemPosition() == 0){
            select_course_spinner.setBackground(getResources().getDrawable(R.drawable.field_error));
            returnValue2 = false;
        }
        if(select_year_spinner.getSelectedItemPosition() == 0){
            select_year_spinner.setBackground(getResources().getDrawable(R.drawable.field_error));
            returnValue3 = false;
        }
        return returnValue1 && returnValue2 && returnValue3;
    }

    //    Collect Info
    private void collectInfo(){

        String personCollegeName = select_college_spinner.getSelectedItem().toString();
        String collegeObjectID = colleges.get(select_college_spinner.getSelectedItemPosition()-1).objectId;
        String collegeLocation = colleges.get(select_college_spinner.getSelectedItemPosition()-1).location;
        String collegeShort = colleges.get(select_college_spinner.getSelectedItemPosition()-1).collegeShort;
        String personCourseName = select_course_spinner.getSelectedItem().toString();
        String courseObjectId = branches.get(select_course_spinner.getSelectedItemPosition()-1).objectId;
        String courseShort = branches.get(select_course_spinner.getSelectedItemPosition()-1).branchShort;
        String personYear = select_year_spinner.getSelectedItem().toString();
        Person person = campusExchangeApp.getInstance().getUniversalPerson();
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


    /**
     * Network Calls
     */

    private void getCredentials() {
        showProgressDialog(getResources().getString(R.string.gettingCredentials));
        final String credentialString = getResources().getString(R.string.credentialFetcher);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                credentialString, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if (response.isNull("application-id")) {
//                            getCredentials();
                            hideProgressDialog();
                            showSnack(getString(R.string.pleaseRestartApp), Snackbar.LENGTH_INDEFINITE);
                            btn_lets_start.setVisibility(View.GONE);
                            campusExchangeApp.getInstance().getUniversalPerson().setPersonPresent("false");
                        }else {
                            Log.d("credentials", response.toString());
                            credentials c = new credentials(getApplicationContext());
                            c.setCredentials(response.toString());

                            getCollegeList();
//                            StartMainHomeActivity();
                        }
                        hideProgressDialog();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();
                showSnack(getString(R.string.pleaseRestartApp), Snackbar.LENGTH_INDEFINITE);
                btn_lets_start.setVisibility(View.GONE);
                campusExchangeApp.getInstance().getUniversalPerson().setPersonPresent("false");
                VolleyLog.d(TAG+":getting credentials", "Error: " + error.getMessage());
            }
        });

        // Adding request to request queue
        campusExchangeApp.getInstance().addToRequestQueue(jsonObjReq, TAG);

    }

    private void setupSpinnerData(List<String> collegesStringArray) {

        show_college_spinner();

        if(campusExchangeApp.getInstance().getUniversalPerson().getPersonInfoCollected().equals("true")){
            select_college_spinner.setSelection(collegesStringArray.indexOf(campusExchangeApp.getInstance().getUniversalPerson().getCollegeName()));

            if(select_college_spinner.getSelectedItemPosition() > 0 ) {
                branches = colleges.get(select_college_spinner.getSelectedItemPosition() - 1).branches;
                for (College.Branch branch : branches) {
                    branchesStringArray.add(branch.branch);
                }
                select_course_spinner.setSelection(branchesStringArray.indexOf(campusExchangeApp.getInstance().getUniversalPerson().getCourse()));
            }
            select_year_spinner.setSelection(Arrays.asList(getResources().getStringArray(R.array.courseYears)).indexOf(campusExchangeApp.getInstance().getUniversalPerson().getAcademicYear()));
        }
        hideProgressDialog();
    }

    private void getCollegeList() {
        String collegeRequest = "http://api.backendless.com/test/data/colleges?loadRelations=branches&sortBy=collegeName%20asc";
        showProgressDialog(getString(R.string.loading));
        JsonObjectRequest getProductList = new JsonObjectRequest(
                Request.Method.GET,
                collegeRequest,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            String responseData = response.getJSONArray(RESPONSE_DATA).toString();
                            Gson gson = campusExchangeApp.getInstance().getGson();
                            colleges = new ArrayList<>();
                            colleges.clear();
                            colleges = Arrays.asList(gson.fromJson(responseData,College[].class));
                            collegesStringArray.clear();
                            collegesStringArray.add(getResources().getString(R.string.select_your_college));
                            for(College college: colleges){
                                collegesStringArray.add(college.collegeName);
                            }
                            setupSpinnerData(collegesStringArray);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.d(TAG, response.toString());


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers= campusExchangeApp.getInstance().getCredentialsHashMapWithoutUserToken();
                Log.d("Headers", headers.toString());
                return headers;
            }
        };

        campusExchangeApp.getInstance().addToRequestQueue(getProductList,TAG);

    }

    private void userCreation(){
        showProgressDialog(getResources().getString(R.string.creatingUser));

        Person newPerson = campusExchangeApp.getInstance().getUniversalPerson();
        PersonGSON gsonPerson = new PersonGSON();
        PersonGSON.postPerson postObject = gsonPerson.getPostPerson();
        postObject.setName(newPerson.getPersonName());
        postObject.setContactNumber(newPerson.getPhoneNumber());
        postObject.setEmail(newPerson.getPersonEmail());
        postObject.college.setObjectId(newPerson.getPersonCollegeObjectId());
        postObject.course.setObjectId(newPerson.getPersonCourseoBjectId());
        postObject.setCourseYear(newPerson.getAcademicYear());
        postObject.setProfilepic(newPerson.getPersonPhotoUrl());
        postObject.setPassword(newPerson.getPersonAuthCode());

        Gson gtoj = campusExchangeApp.getInstance().getGson();
        String jsonObj = gtoj.toJson(postObject);
        try {
            JSONObject jsonPerson = new JSONObject(jsonObj);

            String createUserString = getResources().getString(R.string.classUsers);

            JsonObjectRequest createNewUser = new JsonObjectRequest(
                    Request.Method.POST,
                    createUserString,
                    jsonPerson,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            String personObjectId = null;
                            try {
                                if (response.isNull("objectId")) {
                                    if (response.getInt("code") == 3033){
                                        Snackbar.make(activity_login," person already exists ", Snackbar.LENGTH_LONG).show();
//                                        readUser();
                                        userUpdate();
                                    }
                                } else {
                                    personObjectId = response.getString("objectId");
    //                                Snackbar.make(activity_login, R.string.setup_complete + " " + personObjectId, Snackbar.LENGTH_LONG).show();
                                    campusExchangeApp.getInstance().getUniversalPerson().setPersonObjectId(personObjectId);
    //                                getCredentials();
                                    loginUser();
                                }
                                hideProgressDialog();

                            } catch (JSONException e) {
                                e.printStackTrace();
                                hideProgressDialog();
                                showSnack(getString(R.string.pleaseRestartApp),Snackbar.LENGTH_INDEFINITE);
                                btn_lets_start.setVisibility(View.GONE);
                                campusExchangeApp.getInstance().getUniversalPerson().setPersonPresent("false");
                                campusExchangeApp.getInstance().getUniversalPerson().setPersonInfoCollected("false");
                                Log.d("cruser",e.toString());
                            }


                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            hideProgressDialog();
                            showSnack(getString(R.string.pleaseRestartApp),Snackbar.LENGTH_INDEFINITE);
                            btn_lets_start.setVisibility(View.GONE);
                            campusExchangeApp.getInstance().getUniversalPerson().setPersonPresent("false");
                            campusExchangeApp.getInstance().getUniversalPerson().setPersonInfoCollected("false");
                            Log.d("cruserinner",error.toString());
                            Log.d("volley",error.toString());
                        }
                    }){
                /**
                 * Passing some request headers
                 * */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers= campusExchangeApp.getInstance().getCredentialsHashMapWithoutUserToken();
                    Log.d("Headers", headers.toString());
                    return headers;
                }
            };

            campusExchangeApp.getInstance().addToRequestQueue(createNewUser,TAG);

        } catch (JSONException e) {
            e.printStackTrace();
            hideProgressDialog();
            showSnack(getString(R.string.pleaseRestartApp),Snackbar.LENGTH_INDEFINITE);
            btn_lets_start.setVisibility(View.GONE);
            campusExchangeApp.getInstance().getUniversalPerson().setPersonPresent("false");
            campusExchangeApp.getInstance().getUniversalPerson().setPersonInfoCollected("false");
            Log.d("cruserouter",e.toString());

        }
    }

    private void userUpdate(){
        showProgressDialog(getResources().getString(R.string.updatingUser));

        Person newPerson = campusExchangeApp.getInstance().getUniversalPerson();
        PersonGSON gsonPerson = new PersonGSON();
        PersonGSON.postPerson postObject = gsonPerson.getPostPerson();
        postObject.setName(newPerson.getPersonName());
        postObject.setContactNumber(newPerson.getPhoneNumber());
        postObject.setEmail(newPerson.getPersonEmail());
        postObject.college.setObjectId(newPerson.getPersonCollegeObjectId());
        postObject.course.setObjectId(newPerson.getPersonCourseoBjectId());
        postObject.setCourseYear(newPerson.getAcademicYear());
        postObject.setProfilepic(newPerson.getPersonPhotoUrl());
        postObject.setPassword(newPerson.getPersonAuthCode());

        Gson gtoj = campusExchangeApp.getInstance().getGson();
        String jsonObjStr = gtoj.toJson(postObject);
        try {
            JSONObject jsonPerson = new JSONObject(jsonObjStr);
            jsonPerson.remove(gsonPerson.getKey_contactNumber());
            jsonPerson.remove(gsonPerson.getKey_email());

            String createUserString = getString(R.string.classUsers);
            createUserString = createUserString+"/"+newPerson.getPersonObjectId();

            JsonObjectRequest updateNewUser = new JsonObjectRequest(
                    Request.Method.PUT,
                    createUserString,
                    jsonPerson,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            hideProgressDialog();
                            if (response.isNull("objectId")) {
                                Snackbar.make(activity_login, R.string.NetworkError, Snackbar.LENGTH_LONG).show();
                            }else {
                                loginUser();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            hideProgressDialog();
                            showSnack(getString(R.string.pleaseRestartApp),Snackbar.LENGTH_INDEFINITE);
                            btn_lets_start.setVisibility(View.GONE);
                            campusExchangeApp.getInstance().getUniversalPerson().setPersonPresent("false");
                            campusExchangeApp.getInstance().getUniversalPerson().setPersonInfoCollected("false");
                            Log.d("upuser",error.toString());
                        }
                    }){
                /**
                 * Passing some request headers
                 * */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers= campusExchangeApp.getInstance().getCredentialsHashMapWithoutUserToken();
                    Log.d("Headers", headers.toString());
                    return headers;
                }
            };

            campusExchangeApp.getInstance().addToRequestQueue(updateNewUser,TAG);

        } catch (JSONException e) {
            e.printStackTrace();
            hideProgressDialog();
            showSnack(getString(R.string.pleaseRestartApp),Snackbar.LENGTH_INDEFINITE);
            btn_lets_start.setVisibility(View.GONE);
            campusExchangeApp.getInstance().getUniversalPerson().setPersonPresent("false");
            campusExchangeApp.getInstance().getUniversalPerson().setPersonInfoCollected("false");
            Log.d("upuserouter",e.toString());

        }
    }

    private void userRead(){
        showProgressDialog(getResources().getString(R.string.setting_up));
        String email = campusExchangeApp.getInstance().getUniversalPerson().getPersonEmail();
        String readUserString = getResources().getString(R.string.classUsers)+"?where=email%3D%27"+email+"%27";

        JsonObjectRequest readeNewUser = new JsonObjectRequest(
                Request.Method.GET,
                readUserString,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String personObjectId = null;
                        try {
                            hideProgressDialog();
                            if (response.getInt("totalObjects") == 1){

                                String responsePersonData = response.getJSONArray(RESPONSE_DATA).toString();
                                Gson gson = campusExchangeApp.getInstance().getGson();
                                List<PersonGSON> personG =  Arrays.asList(gson.fromJson(responsePersonData,PersonGSON[].class));
                                for(PersonGSON recognisedUser: personG){
                                    personObjectId = recognisedUser.objectId;
                                    campusExchangeApp.getInstance().getUniversalPerson().setPersonObjectId(personObjectId);
                                }
                                userUpdate();

                            } else {
                                userCreation();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            hideProgressDialog();
                            showSnack(getString(R.string.pleaseRestartApp),Snackbar.LENGTH_INDEFINITE);
                            btn_lets_start.setVisibility(View.GONE);
                            campusExchangeApp.getInstance().getUniversalPerson().setPersonPresent("false");
                            campusExchangeApp.getInstance().getUniversalPerson().setPersonInfoCollected("false");
                            Log.d("Ruserinner",e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideProgressDialog();
                        showSnack(getString(R.string.pleaseRestartApp),Snackbar.LENGTH_INDEFINITE);
                        btn_lets_start.setVisibility(View.GONE);
                        campusExchangeApp.getInstance().getUniversalPerson().setPersonPresent("false");
                        campusExchangeApp.getInstance().getUniversalPerson().setPersonInfoCollected("false");
                        Log.d("Ruserouter",error.toString());
                        Log.d("volley",error.toString());
                    }
                }){
            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers= campusExchangeApp.getInstance().getCredentialsHashMapWithoutUserToken();
                Log.d("Headers", headers.toString());
                return headers;
            }
        };

        campusExchangeApp.getInstance().addToRequestQueue(readeNewUser,TAG);
    }

    private void deviceRegistration(){
        showProgressDialog(getString(R.string.setting_up));
        new Thread(new Runnable() {
            public void run() {
                //code
                try {
                    InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
                    String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    Log.d("Device_token",token);

                    String id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                    Log.d("Device_id",id);
                    String deviceVersion = android.os.Build.VERSION.RELEASE;

                    String devReg = " {"
                            +"\"deviceToken\""+":\""+token+"\","
                            +    "\"deviceId\""+":\""+id+"\","
                            +   "\"os\""+":"+"\"ANDROID\""+","
                            +    "\"osVersion\""+":"+"\""+deviceVersion+"\""
                            +"}";

                    JSONObject jsonObject = new JSONObject(devReg);
                    Log.d("Device_Reg", jsonObject.toString());
                    //f4bf6491e2a9ecc8
                    uploadDeviceKeys(jsonObject);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("Device_Token Exception",e.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("Device_Json Exception",e.toString());
                }
            }
        }).start();

    }

    private void uploadDeviceKeys(final JSONObject regObj) {
        String createDeviceString = "https://api.backendless.com/test/messaging/registrations";

        JsonObjectRequest registerNewDevice = new JsonObjectRequest(
                Request.Method.POST,
                createDeviceString,
                regObj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("Device_Reg_Id", response.getString("registrationId"));
                            if (Boolean.valueOf(campusExchangeApp.getInstance().getUniversalPerson().getPersonPresent())) {
                                relateUserWithDevice(regObj);

                            } else {
                                hideProgressDialog();
                                StartMainHomeActivity();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Device_Reg_Id", "unsuccessful");
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Device_volley", error.toString());
                    }
                }) {
            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = campusExchangeApp.getInstance().getCredentialsHashMapWithoutUserToken();
//                HashMap<String, String> headers= new HashMap<String, String>();
//                headers.put("application-id","99AD5E9B-7377-7F3E-FF59-F65B19158D00");
//                headers.put("secret-key","8265CE11-7333-14FE-FF11-0A96233C9100");
                headers.put("Content-Type", "application/json");
                headers.put("application-type", "REST");
                Log.d("device_Headers", headers.toString());
                return headers;
            }
        };

        campusExchangeApp.getInstance().addToRequestQueue(registerNewDevice, TAG);

    }

    private void relateUserWithDevice(JSONObject regObj){

        Log.d("RelateUser","Called");

        try {
            String deviceToken = regObj.getString("deviceToken");
            String deviceId = regObj.getString("deviceId");
            String os = regObj.getString("os");
            String osVersion = regObj.getString("osVersion");
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String presentVersion = pInfo.versionName;

            String userDevice = "{\n" +
                    "                \"deviceId\": " + "\""+deviceId+"\",\n" +
                    "                \"appVersion\": \""+presentVersion+"\",\n" +
                    "                \"___class\": \"Users\"\n" +
                    "    }";

            JSONObject jsonPerson = new JSONObject(userDevice);

            String createUserString = getString(R.string.classUsers);
            createUserString = createUserString+"/"+campusExchangeApp.getInstance().getUniversalPerson().getPersonObjectId();

            JsonObjectRequest updateNewUser = new JsonObjectRequest(
                    Request.Method.PUT,
                    createUserString,
                    jsonPerson,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("RelateUser", response.toString());
                            hideProgressDialog();
                                if (response.isNull("objectId")) {
//                                        Snackbar.make(activity_login, R.string.NetworkError, Snackbar.LENGTH_LONG).show();
                                        showSnack(getString(R.string.pleaseRestartApp),Snackbar.LENGTH_INDEFINITE);
                                    }else {
                                        Toast.makeText(getApplicationContext(), "Device registered for notifications", Toast.LENGTH_SHORT).show();
                                    }
                                hideProgressDialog();
                                StartMainHomeActivity();
                            }

                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            hideProgressDialog();
                            showSnack(getString(R.string.pleaseRestartApp),Snackbar.LENGTH_INDEFINITE);
                            btn_lets_start.setVisibility(View.GONE);
                            campusExchangeApp.getInstance().getUniversalPerson().setPersonPresent("false");
                            campusExchangeApp.getInstance().getUniversalPerson().setPersonInfoCollected("false");
                            Log.d("RelateUser",error.toString());
                        }
                    }){
                /**
                 * Passing some request headers
                 * */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers= campusExchangeApp.getInstance().getCredentialsHashMapWithoutUserToken();
                    Log.d("Headers", headers.toString());
                    return headers;
                }
            };

            campusExchangeApp.getInstance().addToRequestQueue(updateNewUser,TAG);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("RelateUser_JSON", e.toString());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.d("RelateUser_Package", e.toString());
        }


    }


    private void loginUser(){
        showProgressDialog(getString(R.string.loginProgress));
        String loginObjString = "{" +
                " \"login\" : \""+campusExchangeApp.getInstance().getUniversalPerson().getPersonEmail()+"\"," +
                " \"password\" : \""+campusExchangeApp.getInstance().getUniversalPerson().getPersonAuthCode()+"\"" +
                "}";
        String LoginUserString = "https://api.backendless.com/test/users/login";

        try {
            JSONObject loginObject = new JSONObject(loginObjString);


        JsonObjectRequest loginUser = new JsonObjectRequest(
                Request.Method.POST,
                LoginUserString,
                loginObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hideProgressDialog();
                        if(response.isNull("user-token")){
//                            loginUser();
                            hideProgressDialog();
                            showSnack(getString(R.string.pleaseRestartApp),Snackbar.LENGTH_INDEFINITE);
                            btn_lets_start.setVisibility(View.GONE);
                            campusExchangeApp.getInstance().getUniversalPerson().setPersonPresent("false");
                            campusExchangeApp.getInstance().getUniversalPerson().setPersonInfoCollected("false");
                        }else {
                            try {
                                Log.d("login_user_token",response.getString("user-token"));
                                campusExchangeApp.getInstance().getUniversal_Credentials().setUserToken(response.getString("user-token"));
                                deviceRegistration();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("Logn_user_token","unsuccessful");
                                hideProgressDialog();
                                showSnack(getString(R.string.pleaseRestartApp),Snackbar.LENGTH_INDEFINITE);
                                btn_lets_start.setVisibility(View.GONE);
                                campusExchangeApp.getInstance().getUniversalPerson().setPersonPresent("false");
                                campusExchangeApp.getInstance().getUniversalPerson().setPersonInfoCollected("false");
                            }
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Login_volley",error.toString());
                        hideProgressDialog();
                        showSnack(getString(R.string.pleaseRestartApp),Snackbar.LENGTH_INDEFINITE);
                        btn_lets_start.setVisibility(View.GONE);
                        campusExchangeApp.getInstance().getUniversalPerson().setPersonPresent("false");
                        campusExchangeApp.getInstance().getUniversalPerson().setPersonInfoCollected("false");
                    }
                }){
            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers= campusExchangeApp.getInstance().getCredentialsHashMapWithoutUserToken();
                headers.put("Content-Type","application/json");
                headers.put("application-type","REST");
                Log.d("device_Headers", headers.toString());
                return headers;
            }
        };

        campusExchangeApp.getInstance().addToRequestQueue(loginUser,TAG);
        } catch (JSONException e) {
            hideProgressDialog();
            showSnack(getString(R.string.pleaseRestartApp),Snackbar.LENGTH_INDEFINITE);
            btn_lets_start.setVisibility(View.GONE);
            campusExchangeApp.getInstance().getUniversalPerson().setPersonPresent("false");
            campusExchangeApp.getInstance().getUniversalPerson().setPersonInfoCollected("false");
            e.printStackTrace();
            Log.d("Login_JSON_exception", e.toString());
        }
    }

    private void StartMainHomeActivity() {




        if(srcString.equals("new")){
            finish();
            Intent homeAction = new Intent(this, MainHomeActivity.class);
            homeAction.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeAction);
        }else if(srcString.equals("cart")){
            finish();
            Intent cartAction = new Intent(this, cartView.class);
            cartAction.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(cartAction);
        }


    }



}
