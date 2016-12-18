package flo.org.exchange.app.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.truecaller.android.sdk.ITrueCallback;
import com.truecaller.android.sdk.TrueButton;
import com.truecaller.android.sdk.TrueClient;
import com.truecaller.android.sdk.TrueError;
import com.truecaller.android.sdk.TrueProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import flo.org.exchange.R;
import flo.org.exchange.app.utils.ConnectivityReceiver;
import flo.org.exchange.app.utils.campusExchangeApp;

public class EditProfile extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener,
                    View.OnClickListener,
                    ITrueCallback,
                    ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = "EditUser profile";
    private static boolean NETWORK_STATE = false;
    private static final String PHONE_REGEX = "((\\+*)((0[ -]+)*|(91 )*)(\\d{12}+|\\d{10}+))|\\d{5}([- ]*)\\d{6}";
    private static final String NAME_REGEX = "^[\\p{L} .'-]+$";
    private Toolbar activityToolbar;
    private CoordinatorLayout activity_profile_edit;
    private EditText person_fullname, person_email, person_mobilenumber;
    private Spinner person_collegename, person_branchname, person_year;
    private Button btn_save_profile_edits;
    private TrueButton register_mobile;
    private TextInputLayout personName_TextInputLayout,personEmail_TextInputLayout, personNumber_TextInputLayout;
    private LinearLayout mobile_number_registration;

    private Person person = new Person(this);
    private TrueClient mTrueClient;

    private ProgressDialog mProgressDialog;

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

    private String old_personName;
    private String old_personEmail;
    private String old_personNumber;
    private String old_personCollege;
    private String old_personCourse;
    private String old_personYear;
    private String old_collegeLocation;
    private String old_collegeShort;
    private String old_collegeObjectID;
    private String old_courseObjectId;
    private String old_courseShort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        setupActionbar();

        setUpViews();
        autofillEditText();
        getCollegeList();

    }

    private void autofillEditText() {
        old_personName = person.getPersonName();
        old_personEmail = person.getPersonEmail();
        old_personNumber = person.getPhoneNumber();

        if (!old_personName.equals("null")){
            person_fullname.setText(old_personName);
        }
        if (!old_personEmail.equals("null")){
            person_email.setText(old_personEmail);
            person_email.setEnabled(false);
            person_email.setClickable(false);
        }
        if (isPersonNumberValid(old_personNumber)){
            personNumber_TextInputLayout.setVisibility(View.VISIBLE);
            person_mobilenumber.setText(old_personNumber);
            person_mobilenumber.setEnabled(false);
            person_mobilenumber.setClickable(false);
        }else {
//            show option of truecaller mobile registration
            personNumber_TextInputLayout.setVisibility(View.GONE);
            person_mobilenumber.setVisibility(View.GONE);
            mobile_number_registration.setVisibility(View.VISIBLE);

        }
    }

    private void setUpGSON(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }


    private void getCollegeList(){
        //get list of colleges from server

        setUpGSON();

        showProgressDialog(getString(R.string.loading));
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

    private void showProgressDialog(String showString) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(showString);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.setMessage(showString);
        mProgressDialog.setCanceledOnTouchOutside(false);
//        mProgressDialog.setCancelable(false);
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
        mProgressDialog.dismiss();
    }

    private void setupSpinners(List<String> collegesStringArray) {

        spinnerAdapter collegeSpinnerAdapter = new spinnerAdapter(this,android.R.layout.simple_spinner_dropdown_item,collegesStringArray);
        person_collegename.setAdapter(collegeSpinnerAdapter);
        person_collegename.setOnItemSelectedListener(this);
        person_collegename.setVisibility(View.VISIBLE);


        branchesStringArray = new ArrayList<String>();
        branchesStringArray.add(getResources().getString(R.string.select_your_course));
        spinnerAdapter courseSpinnerAdapter = new spinnerAdapter(this,android.R.layout.simple_spinner_dropdown_item,branchesStringArray);
        person_branchname.setAdapter(courseSpinnerAdapter);
        person_branchname.setOnItemSelectedListener(this);
        person_branchname.setEnabled(false);
        person_branchname.setClickable(false);
        person_branchname.setVisibility(View.VISIBLE);


        spinnerAdapter yearSpinnerAdapter = new spinnerAdapter(this,android.R.layout.simple_spinner_dropdown_item,Arrays.asList(getResources().getStringArray(R.array.courseYears)));
        person_year.setAdapter(yearSpinnerAdapter);
        person_year.setOnItemSelectedListener(this);
        person_year.setVisibility(View.VISIBLE);

        if(person.getPersonInfoCollected().equals("true")){
            old_personCollege = person.getCollegeName();
            old_collegeLocation = person.getPersonCollegeLocation();
            old_collegeShort = person.getPersonCollegeShort();
            old_collegeObjectID = person.getPersonCollegeObjectId();
            person_collegename.setSelection(collegesStringArray.indexOf(person.getCollegeName()));

            if(person_collegename.getSelectedItemPosition() > 0 ) {
                branches = colleges.get(person_collegename.getSelectedItemPosition() - 1).branches;
                for (College.Branch branch : branches) {
                    branchesStringArray.add(branch.branch);
                }
                person_branchname.setEnabled(true);
                person_branchname.setClickable(true);
                person_branchname.setSelection(branchesStringArray.indexOf(person.getCourse()));
                old_personCourse = person.getCourse();
                old_courseObjectId = person.getPersonCourseoBjectId();
                old_courseShort = person.getPersonCourseShort();

            }

            person_year.setSelection(Arrays.asList(getResources().getStringArray(R.array.courseYears)).indexOf(person.getAcademicYear()));
            old_personYear = person.getAcademicYear();
        }


    }

    private void setUpViews() {
//        Coordinator layout
        activity_profile_edit = (CoordinatorLayout) findViewById(R.id.activity_profile_edit);
        checkConnection();

//        Linear Layouts
        mobile_number_registration = (LinearLayout) findViewById(R.id.mobile_number_registration);
        mobile_number_registration.setVisibility(View.GONE);

//        Edit texts
        person_fullname = (EditText) findViewById(R.id.input_name);
        person_mobilenumber = (EditText) findViewById(R.id.input_mobile_number);
        person_email = (EditText) findViewById(R.id.input_email);

//        Text Input Layouts
        personName_TextInputLayout = (TextInputLayout) findViewById(R.id.personName_TextInputLayout);
        personEmail_TextInputLayout = (TextInputLayout) findViewById(R.id.personEmail_TextInputLayout);
        personNumber_TextInputLayout = (TextInputLayout) findViewById(R.id.personNumber_TextInputLayout);
        personNumber_TextInputLayout.setVisibility(View.GONE);

//        buttons
        btn_save_profile_edits = (Button) findViewById(R.id.btn_save_profile_edits);
        btn_save_profile_edits.setOnClickListener(this);
        mTrueClient = new TrueClient(getApplicationContext(), this);
        register_mobile = ((TrueButton) findViewById(R.id.com_truecaller_android_sdk_truebutton));
        register_mobile.setTrueClient(mTrueClient);
//        register_mobile = (TrueButton) findViewById(R.id.btn_truecaller);
//        register_mobile.setTrueClient(mTrueClient);

//        Spinners
        person_collegename = (Spinner) findViewById(R.id.profile_edit_select_college_spinner);
        person_collegename.setVisibility(View.GONE);
        person_branchname = (Spinner) findViewById(R.id.profile_edit_select_course_spinner);
        person_branchname.setVisibility(View.GONE);
        person_year = (Spinner) findViewById(R.id.profile_edit_select_year_spinner);
        person_year.setVisibility(View.GONE);

    }

    private void setupActionbar() {
        activityToolbar = (Toolbar) findViewById(R.id.action_bar_edit_profile);
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.select_college_spinner :
                person_branchname.setEnabled(true);
                person_branchname.setClickable(true);
                person_collegename.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
                person_branchname.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
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
                    person_branchname.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
                }
                break;
            case R.id.select_year_spinner :
                if(position > 0){
//                    Toast.makeText(getBaseContext(), parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
                    person_year.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
                }


                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private boolean checkFieldErrors() {
        boolean returnValue1=true,returnValue2=true,returnValue3=true,returnValue4=true,returnValue5=true,returnValue6=true;
        if (person_collegename.getSelectedItemPosition() == 0){
            person_collegename.setBackground(getResources().getDrawable(R.drawable.field_error));
            returnValue1 = false;
        }else {
            person_collegename.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
        }
        if(person_branchname.getSelectedItemPosition() == 0){
            person_branchname.setBackground(getResources().getDrawable(R.drawable.field_error));
            returnValue2 = false;
        }else {
            person_branchname.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
        }
        if(person_year.getSelectedItemPosition() == 0){
            person_year.setBackground(getResources().getDrawable(R.drawable.field_error));
            returnValue3 = false;
        }else {
            person_year.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
        }
        if(!isPersonNameValid(person_fullname.getText().toString())){
            personName_TextInputLayout.setBackground(getResources().getDrawable(R.drawable.field_error));
            returnValue4 = false;
        }else {
            personName_TextInputLayout.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
        }
        if(!isPersonEmailValid(person_email.getText().toString())){
            personEmail_TextInputLayout.setBackground(getResources().getDrawable(R.drawable.field_error));
            returnValue5 = false;
        }else {
            personEmail_TextInputLayout.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
        }
        if(!isPersonNumberValid(person_mobilenumber.getText().toString())){
            personNumber_TextInputLayout.setBackground(getResources().getDrawable(R.drawable.field_error));
            returnValue6 = false;
        }else {
            personNumber_TextInputLayout.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
        }
        return returnValue1 && returnValue2 && returnValue3 && returnValue4 && returnValue5 && returnValue6 ;
    }

    private static boolean isPersonNameValid(String name){
        return name != null && name.length() > 6 && Pattern.matches(NAME_REGEX, name);
    }

    public static boolean isPersonEmailValid(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private static boolean isPersonNumberValid(String number) {
        return number != null && Pattern.matches(PHONE_REGEX, number);
    }





    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_save_profile_edits:
                if(NETWORK_STATE){
                    if(areChangesPresent() && checkFieldErrors()){
                        savePersonDetails();
                        uploadPersonDetails();
                    }
                }else {
                    showNetworkStateSnack(NETWORK_STATE);
                }
                break;
        }
    }

    private boolean areChangesPresent() {
        if(        old_personName.equals(person_fullname.getText().toString())
                && old_personEmail.equals(person_email.getText().toString())
                && old_personNumber.equals(person_mobilenumber.getText().toString())
                && old_personCollege.equals(person_collegename.getSelectedItem().toString())
                && old_personCourse.equals(person_branchname.getSelectedItem().toString())
                && old_personYear.equals(person_year.getSelectedItem().toString())){
            Snackbar.make(activity_profile_edit, R.string.no_changes_were_made, Snackbar.LENGTH_LONG).show();
            return false;
        }

        return true;

    }

    private void uploadPersonDetails() {

        showProgressDialog(getString(R.string.uploading));
        String updateUserString = getResources().getString(R.string.updateUser);
        updateUserString = updateUserString+"?";
        try {
            updateUserString = updateUserString+ "objectId=" + URLEncoder.encode(person.getPersonObjectId(), "UTF-8");
            updateUserString = updateUserString+ "&name=" + URLEncoder.encode(person.getPersonName(), "UTF-8");
            updateUserString = updateUserString+"&contactNumber="+ URLEncoder.encode(person.getPhoneNumber(),"UTF-8");
//            updateUserString = updateUserString+"&email="+URLEncoder.encode(person.getPersonEmail(),"UTF-8");
            updateUserString = updateUserString+"&collegeObjectId="+URLEncoder.encode(person.getPersonCollegeObjectId(),"UTF-8");
            Log.d("personCourseObjectId",person.getPersonCollegeObjectId());
            updateUserString = updateUserString+"&courseObjectId="+URLEncoder.encode(person.getPersonCourseoBjectId(),"UTF-8");
            Log.d("personCourseObjectId",person.getPersonCourseoBjectId());
            updateUserString = updateUserString+"&courseYear="+URLEncoder.encode(person.getAcademicYear(),"UTF-8");
//            updateUserString = updateUserString+"&profilepic="+URLEncoder.encode(person.getPersonPhotoUrl(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Log.d(TAG+"UpUser", updateUserString);

        JsonObjectRequest jsonuserUpdateReq = new JsonObjectRequest(Request.Method.GET,
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
                            Snackbar.make(activity_profile_edit, R.string.NetworkError, Snackbar.LENGTH_LONG).show();
                            rollbackIfError();
                        }else {
                            Snackbar.make(activity_profile_edit, R.string.savedSuccessfully, Snackbar.LENGTH_LONG).show();
                            finish();
//                            StartMainHomeActivity();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                rollbackIfError();
                Snackbar.make(activity_profile_edit, R.string.NetworkError, Snackbar.LENGTH_LONG).show();
                VolleyLog.d(TAG+" :while updating user", "Error: " + error.getMessage());
            }
        });

        // Adding request to request queue
        campusExchangeApp.getInstance().addToRequestQueue(jsonuserUpdateReq, tag_json_obj+":CreatingUser");

        hideProgressDialog();
    }

    private void rollbackIfError() {

        showProgressDialog(getString(R.string.rollingback));

//      Details to be rolled back
        person.setPersonName(old_personName);
        person.setPersonEmail(old_personEmail);
        person.setPhoneNumber(old_personNumber);
        person.setCollegeName(old_personCollege);
        person.setPersonCollegeLocation(old_collegeLocation);
        person.setPersonCollegeShort(old_collegeShort);
        person.setPersonCollegeObjectId(old_collegeObjectID);
        person.setCourse(old_personCourse);
        person.setPersonCourseoBjectId(old_courseObjectId);
        person.setPersonCourseShort(old_courseShort);
        person.setAcademicYear(old_personYear);
        hideProgressDialog();

    }

    private void registerPhoneNumber() {

        showProgressDialog(getString(R.string.savingPhoneNumber));
        String updateUserString = getResources().getString(R.string.updateUser);
        updateUserString = updateUserString+"?";
        try {
            updateUserString = updateUserString+ "objectId=" + URLEncoder.encode(person.getPersonObjectId(), "UTF-8");
            updateUserString = updateUserString+"&contactNumber="+ URLEncoder.encode(person.getPhoneNumber(),"UTF-8");
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
                            Snackbar.make(activity_profile_edit, R.string.NetworkError, Snackbar.LENGTH_LONG).show();
                        }else {
                            Snackbar.make(activity_profile_edit, R.string.PhoneNumberSaved, Snackbar.LENGTH_LONG).show();
//                            finish();
//                            StartMainHomeActivity();
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

        hideProgressDialog();
    }

    private void savePersonDetails() {
        showProgressDialog(getString(R.string.saving));

//      Details from edit texts
        person.setPersonName(person_fullname.getText().toString());
        person.setPersonEmail(person_email.getText().toString());
        person.setPhoneNumber(person_mobilenumber.getText().toString());

//      Details from Spinner
        String personCollegeName = person_collegename.getSelectedItem().toString();
        String collegeObjectID = colleges.get(person_collegename.getSelectedItemPosition()-1).objectId;
        String collegeLocation = colleges.get(person_collegename.getSelectedItemPosition()-1).location;
        String collegeShort = colleges.get(person_collegename.getSelectedItemPosition()-1).collegeShort;
        String personCourseName = person_branchname.getSelectedItem().toString();
        String courseObjectId = branches.get(person_branchname.getSelectedItemPosition()-1).objectId;
        String courseShort = branches.get(person_branchname.getSelectedItemPosition()-1).branchShort;
        String personYear = person_year.getSelectedItem().toString();
        person.setCollegeName(personCollegeName);
        person.setPersonCollegeLocation(collegeLocation);
        person.setPersonCollegeShort(collegeShort);
        person.setPersonCollegeObjectId(collegeObjectID);
        person.setCourse(personCourseName);
        person.setPersonCourseoBjectId(courseObjectId);
        person.setPersonCourseShort(courseShort);
        person.setAcademicYear(personYear);

        hideProgressDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mTrueClient.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }

    @Override
    public void onSuccesProfileShared(@NonNull TrueProfile trueProfile) {
        person.setPhoneNumber(trueProfile.phoneNumber);
        mobile_number_registration.setVisibility(View.GONE);
        personNumber_TextInputLayout.setVisibility(View.VISIBLE);
        person_mobilenumber.setVisibility(View.VISIBLE);
        person_mobilenumber.setText(trueProfile.phoneNumber);
        person_mobilenumber.setEnabled(false);
        person_mobilenumber.setClickable(false);
        Snackbar.make(activity_profile_edit, R.string.text_thanks_for_phone, Snackbar.LENGTH_LONG).show();
        registerPhoneNumber();
        old_personNumber = person.getPhoneNumber();

    }

    @Override
    public void onFailureProfileShared(@NonNull TrueError trueError) {
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        NETWORK_STATE = isConnected;
        showNetworkStateSnack(isConnected);
        if(isConnected){
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

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
                .make(activity_profile_edit, message, timePeriod);
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

    @Override
    protected void onResume() {
        super.onResume();
        // register connection status listener
        campusExchangeApp.getInstance().setConnectivityListener(this);
    }
}
