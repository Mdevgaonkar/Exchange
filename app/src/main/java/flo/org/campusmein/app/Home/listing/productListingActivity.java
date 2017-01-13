package flo.org.campusmein.app.Home.listing;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flo.org.campusmein.R;
import flo.org.campusmein.app.Home.cart.cartView;
import flo.org.campusmein.app.utils.College;
import flo.org.campusmein.app.utils.Person;
import flo.org.campusmein.app.Login.spinnerAdapter;
import flo.org.campusmein.app.utils.ConnectivityReceiver;
import flo.org.campusmein.app.utils.Products;
import flo.org.campusmein.app.utils.RealmUtils.RealmController;
import flo.org.campusmein.app.utils.Subjects;
import flo.org.campusmein.app.utils.campusExchangeApp;
import flo.org.campusmein.app.utils.cartViewUtils.BadgeDrawable;
import flo.org.campusmein.app.utils.chromeCustomTab.CustomTabActivityHelper;
import flo.org.campusmein.app.utils.chromeCustomTab.WebviewFallback;
import flo.org.campusmein.app.utils.searchableSpinnerViewUtils.SearchableSpinner;

public class productListingActivity extends AppCompatActivity
            implements View.OnClickListener,ConnectivityReceiver.ConnectivityReceiverListener{

    private static final String TAG = productListingActivity.class.getSimpleName();
    private static final String QUERY = "?";
    private static final String LOAD_RELATIONS ="loadRelations=book%2Cinstrument%2Ccombopack";
    private static final String QUERY_SEPERATOR = "&";
    private static final String LOAD_PROPS = "props=listPrice%2CobjectId%2Cmrp%2CdateEnlisted%2Ctype";
    private static final String WHERE_EQUAL_TO = "where=";
    private static boolean NETWORK_STATE = false;

    private static final String PRODUCT_TITLE = "productTitle";
    private static final String PRODUCT_STATUS = "productStatus";
    private static final String PRODUCT_WHERE_CLAUSE = "productWhereClause";
    private static final String PRODUCT_CLASS = "productClass";
    private static final String PRODUCT_POLL = "productPoll";
    private static final String PRODUCT_POLL_URL = "productPollUrl";
    private static final String PRODUCT_TYPE = "type";

    private static final String RESPONSE_DATA = "data";

    private static final int FILTER_APPLY_VISIBLE = 1;
    private static final int FILTER_APPLY_GONE = 2;
    private static final int FILTER_CLEAR_VISIBLE = 3;

    private RecyclerView productRecyclerView;
    private productListingAdapter productListingAdapter;
    private List<Products> productList;
    private CoordinatorLayout activity_product_listing;


//    private ScrollView  productScrollView;

    private LinearLayout errorLayout;
    private LinearLayout emptyLayout;
    private LinearLayout progressBarLayout;
    private LinearLayout comingSoonLayout;

    private Button takeApoll;

    private ProgressBar subjectsLoader;

    private LinearLayout optionsprogressBarLayout;
    private LinearLayout filterOptionsLayout;
    private SearchableSpinner branchFilter;
    spinnerAdapter branchOptionsSpinnerAdapter;
    private SearchableSpinner semesterFilter;
    spinnerAdapter semesterOptionsSpinnerAdapter;
    private SearchableSpinner subjectFilter;
    spinnerAdapter subjectOptionsSpinnerAdapter;

    private Button btn_apply_remove_filter;
    private Boolean apply_remove_action= null;        //true --> Apply  false-->Clear

    private int status;
    private String whereClause;
    private boolean poll;
    private String pollUrl;
    private String ___class;
    private String type;


    List<College> colleges;
    List<String> collegesStringArray;
    List<College.Branch> branches;
    List<String> branchesStringArray;
    List<Subjects> subjects;
    List<String> subjectsStringArray;


    //    Chrome custom activity helper
    private CustomTabActivityHelper mCustomTabActivityHelper;

    // Cart Icon
    private LayerDrawable mCartMenuIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_listing);
        Intent productType = getIntent();

        Bundle b = productType.getExtras();
        String title = b.getString(PRODUCT_TITLE);
        status = b.getInt(PRODUCT_STATUS,1);
        whereClause = b.getString(PRODUCT_WHERE_CLAUSE);
        ___class = b.getString(PRODUCT_CLASS);
        poll = b.getBoolean(PRODUCT_POLL);
        pollUrl = b.getString(PRODUCT_POLL_URL);
        type = b.getString(PRODUCT_TYPE);

        setupActionbar(title);

        checkConnection();
        setupCoordinatorLayout();
        setupErrorLayout();
        setupComingSoonLayout();
        setupProgressBarLayout();
        setupEmptyView();
        setupRecyclerView();
//        updatefilterOptionsLayout();
//        getCollegeBranches();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCustomTabActivityHelper.bindCustomTabsService(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCustomTabActivityHelper.unbindCustomTabsService(this);
    }

    private void setupOptionProgress() {
        optionsprogressBarLayout = (LinearLayout) findViewById(R.id.optionsProgressBarLayout);
        optionsprogressBarLayout.setVisibility(View.VISIBLE);

    }

    public void showoptionsprogressBarLayout(){
        optionsprogressBarLayout.setVisibility(View.VISIBLE);
        hidefilterOptionsLayout();
    }

    public void hideoptionsprogressBarLayout(){
        optionsprogressBarLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        // register connection status listener
        loadUI();
        campusExchangeApp.getInstance().setConnectivityListener(this);
        if(mCartMenuIcon!=null){
            int cartSize = RealmController.getInstance().getItems().size();
            setBadgeCount(this, mCartMenuIcon, String.valueOf(cartSize));
        }
        super.onResume();
    }

    private void loadUI() {
        if (NETWORK_STATE){
            if(status == 0) {
                setupfilterOptionsLayout();
                prepareProducts();
//                prepareAlbums();
                hidefilterOptionsLayout();
                hideoptionsprogressBarLayout();
            }else if(status == 4){
                setupfilterOptionsLayout();
                prepareProducts();
//                prepareAlbums();
                showfilterOptionsLayout();
                getCollegeBranches();
            }else if(status == 1){
                finish();
            }else if(status == 2){
                setupfilterOptionsLayout();
                showEmptyView();
                hidefilterOptionsLayout();
                hideoptionsprogressBarLayout();
            }else if(status == 3){
                setupfilterOptionsLayout();
                hidefilterOptionsLayout();
                hideoptionsprogressBarLayout();
                showComingSoonLayout();
                if(poll){
                    showTakeApoll();
                }else {
                    hideTakeApoll();
                }
            }
        }else {
            showSnack(getString(R.string.NetworkFaliure));
            showErrorLayout();
        }
    }

    private  void toggleApplyClearButton(int i){

        switch (i){
            case FILTER_APPLY_VISIBLE:
                btn_apply_remove_filter.setText("APPLY");
                btn_apply_remove_filter.setVisibility(View.VISIBLE);
                apply_remove_action = true;
                break;
            case FILTER_APPLY_GONE:
                btn_apply_remove_filter.setText("APPLY");
                btn_apply_remove_filter.setVisibility(View.GONE);
                apply_remove_action = null;
                break;
            case FILTER_CLEAR_VISIBLE:
                btn_apply_remove_filter.setText("CLEAR");
                btn_apply_remove_filter.setVisibility(View.VISIBLE);
                apply_remove_action = false;
                break;
        }
    }

    private String generateFilterClause() {
        String FilterClause = "";
        boolean semFilterDiscarded;
        boolean branchFilterdiscarded;
        boolean subjectFilterDiscarded;
        int semesterSelectedPosition =semesterFilter.getSelectedItemPosition();
        if (semesterSelectedPosition <= 0){
            semFilterDiscarded = true;
        }else semFilterDiscarded = false;
        int branchSelectedPosition = branchFilter.getSelectedItemPosition()-1;
        if(branchSelectedPosition < 0){
            branchFilterdiscarded = true;
        }else branchFilterdiscarded = false;
        int subjectSelectedPosition = subjectFilter.getSelectedItemPosition()-1;
        if(subjectSelectedPosition < 0){
            subjectFilterDiscarded = true;
        }else subjectFilterDiscarded = false;

        if(type.equals(getString(R.string.bookType))){
            FilterClause = FilterClause + "type%3D%27B%27"; //type%3D%27B%27
            FilterClause = FilterClause + "%20AND%20";  // AND
        }else if(type.equals(getString(R.string.instrumentType))){
            FilterClause = FilterClause + "type%3D%27I%27"; //type%3D%27I%27
            FilterClause = FilterClause + "%20AND%20";  // AND
        }else if(type.equals(getString(R.string.comboType))){
            FilterClause = FilterClause + "type%3D%27C%27"; //type%3D%27C%27
            FilterClause = FilterClause + "%20AND%20";  // AND
        }else if(type.equals(getString(R.string.allType))){
            FilterClause = "";
        }


        if(!semFilterDiscarded){

            FilterClause = FilterClause + "term%3D%27"; //term%3D%27 %27
            FilterClause = FilterClause + semesterSelectedPosition;
            FilterClause = FilterClause + "%27";
        }
        if(!branchFilterdiscarded){
            FilterClause = FilterClause + "%20AND%20";  // AND
            FilterClause = FilterClause + "specialization.branchShort%3D%27"; //term%3D%27 %27
            FilterClause = FilterClause + branches.get(branchSelectedPosition).branchShort;
            FilterClause = FilterClause + "%27";
        }
        if(!subjectFilterDiscarded){
            FilterClause = FilterClause + "%20AND%20";  // AND
            FilterClause = FilterClause + "subject.subject%3D%27"; //term%3D%27 %27
            FilterClause = FilterClause + URLEncoder.encode(subjects.get(subjectSelectedPosition).subject);
            FilterClause = FilterClause + "%27";
//            showSnack(subjects.get(branchSelectedPosition).subjectShort);
        }
        FilterClause = FilterClause + "%20AND%20";  // AND
        FilterClause = FilterClause + "college.collegeName%3D%27"; //term%3D%27 %27
        FilterClause = FilterClause + URLEncoder.encode(campusExchangeApp.getInstance().getUniversalPerson().getCollegeName());
        FilterClause = FilterClause + "%27";

        FilterClause = FilterClause + "%20AND%20";  // AND
        FilterClause = FilterClause + "enlisted%3DTRUE"; //enlisted = TRUE

        return FilterClause;
    }

    private void setupfilterOptionsLayout() {
        setupOptionProgress();
        filterOptionsLayout = (LinearLayout) findViewById(R.id.filterOptionsLayout);
        filterOptionsLayout.setVisibility(View.GONE);
        subjectsLoader = (ProgressBar) findViewById(R.id.subjectLoader);
        subjectsLoader.setVisibility(View.GONE);

        btn_apply_remove_filter = (Button) findViewById(R.id.apply_remove_filter);
        btn_apply_remove_filter.setOnClickListener(this);
        btn_apply_remove_filter.setVisibility(View.GONE);
        btn_apply_remove_filter.setOnClickListener(this);

        branchFilter = (SearchableSpinner) findViewById(R.id.branchFilter);
        branchFilter.setTitle(getString(R.string.branch_filterOptionTitle));
        branchFilter.setPositiveButton(getString(R.string.ok_action));
        branchesStringArray = new ArrayList<>();
        branchesStringArray.add(getString(R.string.filterTextBranch));
        branchOptionsSpinnerAdapter = new spinnerAdapter(this,android.R.layout.simple_list_item_1, branchesStringArray);
        branchFilter.setAdapter(branchOptionsSpinnerAdapter);
        branchFilter.setSelection(0);
//        showSnack(branchFilter.getChildCount()+"");
//        if(branchFilter.getChildCount() >=0){
//            TextView tv = (TextView) branchFilter.getChildAt(0);
//            tv.setEllipsize(TextUtils.TruncateAt.END);
//            tv.setMaxEms(10);
//            tv.setLines(1);
//        }


        semesterFilter = (SearchableSpinner) findViewById(R.id.semesterFilter);
        semesterFilter.setTitle(getString(R.string.semester_filterOptionTitle));
        semesterFilter.setPositiveButton(getString(R.string.ok_action));
        semesterOptionsSpinnerAdapter = new spinnerAdapter(this,android.R.layout.simple_list_item_1, Arrays.asList(getResources().getStringArray(R.array.semesters)));
        semesterFilter.setAdapter(semesterOptionsSpinnerAdapter);
        semesterFilter.setSelection(0);

        subjectFilter = (SearchableSpinner) findViewById(R.id.subjectFilter);
        subjectFilter.setTitle(getString(R.string.subject_filterOptionTitle));
        subjectFilter.setPositiveButton(getString(R.string.ok_action));
        subjectsStringArray = new ArrayList<>();
        subjectsStringArray.add(getString(R.string.filterTextSubject));
        subjectOptionsSpinnerAdapter = new spinnerAdapter(this,android.R.layout.simple_list_item_1, subjectsStringArray);
        subjectFilter.setAdapter(subjectOptionsSpinnerAdapter);
        subjectFilter.setSelection(0);


        semesterFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clearSubjectsFilter();
                if (semesterFilter.getSelectedItemPosition() == 0) {
                     toggleApplyClearButton(FILTER_APPLY_GONE);

                    branchFilter.setSelection(0);

                } else {
                    //showSnack("Semester "+semesterFilter.getSelectedItemPosition()+" Selected");
                    toggleApplyClearButton(FILTER_APPLY_VISIBLE);
                    if(branchFilter.getSelectedItemPosition() > 0){

                        if (NETWORK_STATE){
                            getsubjectList();
                        }else {
                            showSnack(getString(R.string.NetworkFaliure));
                        }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        branchFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clearSubjectsFilter();
                if (branchFilter.getSelectedItemPosition() <= 0) {

                    if(semesterFilter.getSelectedItemPosition()>0) {
                        toggleApplyClearButton(FILTER_APPLY_VISIBLE);
                    }else toggleApplyClearButton(FILTER_APPLY_GONE);

                } else {
                    //showSnack(branchFilter.getSelectedItemPosition()+" Branch Selected");
                    if(semesterFilter.getSelectedItemPosition()>0){
                        toggleApplyClearButton(FILTER_APPLY_VISIBLE);
                        if (NETWORK_STATE){
                            getsubjectList();
                        }else {
                            showSnack(getString(R.string.NetworkFaliure));
                        }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        subjectFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (subjectFilter.getSelectedItemPosition() == 0) {
                    if(semesterFilter.getSelectedItemPosition()>0 || branchFilter.getSelectedItemPosition()>0) {
                        toggleApplyClearButton(FILTER_APPLY_VISIBLE);
                    }else toggleApplyClearButton(FILTER_APPLY_GONE);

                } else {
//                    showSnack(subjectFilter.getSelectedItemPosition()+" Selected");
                    toggleApplyClearButton(FILTER_APPLY_VISIBLE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }

    private void showfilterOptionsLayout(){
        hideoptionsprogressBarLayout();
        filterOptionsLayout.setVisibility(View.VISIBLE);
    }

    private void hidefilterOptionsLayout(){
        filterOptionsLayout.setVisibility(View.GONE);
    }

    private void setupErrorLayout() {
        errorLayout = (LinearLayout) findViewById(R.id.error_Layout);
        errorLayout.setOnClickListener(this);
        errorLayout.setVisibility(View.GONE);
    }

    private void showErrorLayout(){
        hideEmptyView();
        hideProgressLayout();
        hideRecyclerScrollView();
        hideComingSoonLayout();
        errorLayout.setVisibility(View.VISIBLE);

    }

    private void hideErrorLayout(){
        errorLayout.setVisibility(View.GONE);

    }

    private void setupComingSoonLayout() {
        mCustomTabActivityHelper = new CustomTabActivityHelper();
        comingSoonLayout = (LinearLayout) findViewById(R.id.comingSoonLayout);
        comingSoonLayout.setVisibility(View.GONE);
        takeApoll = (Button) findViewById(R.id.take_a_poll);
        takeApoll.setOnClickListener(this);
        takeApoll.setVisibility(View.GONE);
    }

    private void showComingSoonLayout(){
        hideEmptyView();
        hideProgressLayout();
        hideRecyclerScrollView();
        hideErrorLayout();
        comingSoonLayout.setVisibility(View.VISIBLE);
    }

    private void hideComingSoonLayout(){
        comingSoonLayout.setVisibility(View.GONE);
    }

    private void showTakeApoll(){
        takeApoll.setVisibility(View.VISIBLE);
    }

    private void hideTakeApoll(){
        takeApoll.setVisibility(View.GONE);
    }


    private void setupCoordinatorLayout() {
        activity_product_listing = (CoordinatorLayout) findViewById(R.id.activity_product_listing);
    }


    private void checkConnection() {
        NETWORK_STATE = ConnectivityReceiver.isConnected();

    }

    private void updateUI() {
        if (productList.size() == 0) {
            showEmptyView();
            Log.d("Product listing","List is Empty");
        }
        else {
            showRecyclerScrollView();
            Log.d("Product listing","List is shown");

        }
    }

    private void setupProgressBarLayout() {
        progressBarLayout = (LinearLayout) findViewById(R.id.progressBarLayout);
        progressBarLayout.setVisibility(View.GONE);
    }

    private  void  showProgressLayout(){
        hideRecyclerScrollView();
        hideEmptyView();
        hideComingSoonLayout();
        hideErrorLayout();
        progressBarLayout.setVisibility(View.VISIBLE);
    }

    private void hideProgressLayout(){
        progressBarLayout.setVisibility(View.GONE);
    }

    private void setupEmptyView() {
        emptyLayout = (LinearLayout) findViewById(R.id.emptyLayout);
        emptyLayout.setVisibility(View.GONE);
    }

    private void showEmptyView(){
        hideRecyclerScrollView();
        hideProgressLayout();
        hideComingSoonLayout();
        hideErrorLayout();
        emptyLayout.setVisibility(View.VISIBLE);
    }

    private void hideEmptyView(){
        emptyLayout.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        productRecyclerView = (RecyclerView) findViewById(R.id.product_list_recycler_view);
        productRecyclerView.setVisibility(View.GONE);

        productList = new ArrayList<>();
        productListingAdapter = new productListingAdapter(this, productList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        productRecyclerView.setLayoutManager(mLayoutManager);
        productRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        productRecyclerView.setItemAnimator(new DefaultItemAnimator());
        productRecyclerView.setAdapter(productListingAdapter);
    }

    private void showRecyclerScrollView(){
//        productScrollView.setVisibility(View.VISIBLE);
        productRecyclerView.setVisibility(View.VISIBLE);
        hideEmptyView();
        hideProgressLayout();
        hideComingSoonLayout();
        hideErrorLayout();
        Log.d("product Recyler view","Shown");
    }

    private void hideRecyclerScrollView(){
//        productScrollView.setVisibility(View.GONE);
        productRecyclerView.setVisibility(View.GONE);
        Log.d("product Recyler view","Hidden");
    }

//    private void showProgressDialog(String showString) {
//        if (mProgressDialog == null) {
//            mProgressDialog = new ProgressDialog(this);
//            mProgressDialog.setMessage(showString);
//            mProgressDialog.setIndeterminate(true);
//        }
//        mProgressDialog.setMessage(showString);
//        mProgressDialog.setCanceledOnTouchOutside(false);
//        mProgressDialog.setCancelable(false);
//        mProgressDialog.show();
//    }
//
//    private void hideProgressDialog() {
//        if (mProgressDialog != null && mProgressDialog.isShowing()) {
//            mProgressDialog.hide();
//        }
//    }

    private void getCollegeBranches(){
        showoptionsprogressBarLayout();
        Person person = campusExchangeApp.getInstance().getUniversalPerson();
        String whereClauseForCollege = "colleges?loadRelations=branches&where=objectId%20%3D%20%27"+person.getPersonCollegeObjectId()+"%27";
        String backendRequestUrl = getString(R.string.baseBackendUrl);
        backendRequestUrl = backendRequestUrl+whereClauseForCollege;
        JsonObjectRequest getCollegeList = new JsonObjectRequest(
                Request.Method.GET,
                backendRequestUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String responseData = response.getJSONArray(RESPONSE_DATA).toString();
                            Gson gson = campusExchangeApp.getInstance().getGson();
                            colleges = Arrays.asList(gson.fromJson(responseData,College[].class));
                            collegesStringArray = new ArrayList<String>();
//                            collegesStringArray.add(getResources().getString(R.string.select_your_college));
                            for(College college: colleges){
                                collegesStringArray.add(college.collegeName);
                                branches = college.branches;
                                for(College.Branch branch : branches){
                                    branchesStringArray.add(branch.branch);
                                }
                            }
                            branchOptionsSpinnerAdapter.notifyDataSetChanged();

                            showfilterOptionsLayout();


                        }catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("college list","no rational data");
                            showErrorLayout();

                        }
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showErrorLayout();
                    }
                }){

//

            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers= campusExchangeApp.getInstance().getCredentialsHashMap();
                Log.d("Headers", headers.toString());
                return headers;
            }
        };

        campusExchangeApp.getInstance().addToRequestQueue(getCollegeList,TAG);

    }

    private void getsubjectList() {
//        showoptionsprogressBarLayout();
        subjectsLoader.setVisibility(View.VISIBLE);
        subjectFilter.setVisibility(View.GONE);
        int sem = semesterFilter.getSelectedItemPosition();
        int branch = branchFilter.getSelectedItemPosition()-1;
        if(sem > 0 && branch >= 0){
            String whereClauseForSubjects = "Subjects?props=subject%2CsubjectShort&where=branch.branch%3D%27"+URLEncoder.encode(branches.get(branch).branch)+"%27%20AND%20semester%3D"+sem;
            String backendRequestUrl = getString(R.string.baseBackendUrl);
            backendRequestUrl = backendRequestUrl+whereClauseForSubjects;
            JsonObjectRequest getSubjectList = new JsonObjectRequest(
                    Request.Method.GET,
                    backendRequestUrl,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String responseData = response.getJSONArray(RESPONSE_DATA).toString();
                                Gson gson = campusExchangeApp.getInstance().getGson();
                                subjects = Arrays.asList(gson.fromJson(responseData,Subjects[].class));
                                subjectsStringArray.clear();
                                subjectsStringArray.add(getString(R.string.filterTextSubject));
                                for(Subjects sub: subjects){
                                    subjectsStringArray.add(sub.subject);
                                }
                                subjectOptionsSpinnerAdapter.notifyDataSetChanged();
                                subjectsLoader.setVisibility(View.GONE);
                                subjectFilter.setVisibility(View.VISIBLE);


                            }catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("Subject list","no rational data");
                                showErrorLayout();

                            }
                            Log.d("Response", response.toString());
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            showErrorLayout();
                        }
                    }){

//

                /**
                 * Passing some request headers
                 * */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers= campusExchangeApp.getInstance().getCredentialsHashMap();
                    Log.d("Headers", headers.toString());
                    return headers;
                }
            };

            campusExchangeApp.getInstance().addToRequestQueue(getSubjectList,TAG);
        }else{
            if(sem <= 0) {
                showSnack("Please select semester");
            }else showSnack("Please select branch");
        }



    }

    private void prepareProducts() {

//        http://api.backendless.com/test/data/products?loadRelations=book%2Cinstrument%2Ccombopack&props=listPrice%2CobjectId%2Cmrp
        String productRequest = getString(R.string.baseBackendUrl);
//        productRequest = productRequest+getString(R.string.products);
        productRequest = productRequest+___class+QUERY+LOAD_RELATIONS+QUERY_SEPERATOR+LOAD_PROPS+QUERY_SEPERATOR+WHERE_EQUAL_TO+whereClause;
        showProgressLayout();
        JsonObjectRequest getProductList = new JsonObjectRequest(
                Request.Method.GET,
                productRequest,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String responseData = response.getJSONArray(RESPONSE_DATA).toString();
                            Gson gson = campusExchangeApp.getInstance().getGson();
                            List<Products> productsList = Arrays.asList(gson.fromJson(responseData,Products[].class));
                            productList.clear();
                            for(Products product : productsList) {
                                productList.add(product);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Product list","no rational data");
                            showErrorLayout();

                        }
                        updateDataSetOfRecyclerView();
                        updateUI();
                        hideProgressLayout();

//                        tv.setText(response.toString());
//                        tv.setVisibility(View.VISIBLE);
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showErrorLayout();
                    }
                }){

//            /**
//             * Passing some request params
//             * */
//
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("name", "Androidhive");
//                params.put("email", "abc@androidhive.info");
//                params.put("password", "password123");
//
//                return params;
//            }

            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers= campusExchangeApp.getInstance().getCredentialsHashMap();
                Log.d("Headers", headers.toString());
                return headers;
            }
        };

        campusExchangeApp.getInstance().addToRequestQueue(getProductList,TAG);
    }

    private void prepareProducts(String filter) {
        String productRequest = getString(R.string.baseBackendUrl);
//        productRequest = productRequest+getString(R.string.products);
        productRequest = productRequest+___class+QUERY+LOAD_RELATIONS+QUERY_SEPERATOR+LOAD_PROPS+QUERY_SEPERATOR+WHERE_EQUAL_TO+filter;
        showProgressLayout();
        JsonObjectRequest getProductList = new JsonObjectRequest(
                Request.Method.GET,
                productRequest,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String responseData = response.getJSONArray(RESPONSE_DATA).toString();
                            Gson gson = campusExchangeApp.getInstance().getGson();
                            List<Products> productsList = Arrays.asList(gson.fromJson(responseData,Products[].class));
                            productList.clear();
                            for(Products product : productsList) {
                                productList.add(product);
                            }
//                            showSnack(productList.size()+"");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Product list","no rational data");
                            Log.d("Product list",response.toString());
                            showErrorLayout();

                        }
                        updateDataSetOfRecyclerView();
                        updateUI();
//                        showRecyclerScrollView();

//                        tv.setText(response.toString());
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("volley Error",error.toString());
                        showErrorLayout();
                    }
                }){

//            /**
//             * Passing some request params
//             * */
//
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("name", "Androidhive");
//                params.put("email", "abc@androidhive.info");
//                params.put("password", "password123");
//
//                return params;
//            }

            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers= campusExchangeApp.getInstance().getCredentialsHashMap();
                Log.d("Headers", headers.toString());
                return headers;
            }
        };

        campusExchangeApp.getInstance().addToRequestQueue(getProductList,TAG);
    }

    private void updateDataSetOfRecyclerView() {
        productListingAdapter.notifyDataSetChanged();

    }

    private void setupActionbar(String title) {
        Toolbar activityToolbar = (Toolbar) findViewById(R.id.action_bar_product_listings);
        setSupportActionBar(activityToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(title);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_home, menu);
        mCartMenuIcon = (LayerDrawable) menu.findItem(R.id.action_cart).getIcon();
        int cartSize = RealmController.getInstance().getItems().size();
        setBadgeCount(this, mCartMenuIcon, String.valueOf(cartSize));

        for(int i = 0; i < menu.size(); i++){
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.colorCard), PorterDuff.Mode.SRC_ATOP);
            }
        }

        return true;
    }

    public static void setBadgeCount(Context context, LayerDrawable icon, String count) {

        BadgeDrawable badge;

        // Reuse drawable if possible
        Drawable reuse = icon.findDrawableByLayerId(R.id.ic_badge);
        if (reuse != null && reuse instanceof BadgeDrawable) {
            badge = (BadgeDrawable) reuse;
        } else {
            badge = new BadgeDrawable(context);
        }

        badge.setCount(count);
        icon.mutate();
        icon.setDrawableByLayerId(R.id.ic_badge, badge);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_cart:
                openCart();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openCart() {
        Intent cart = new Intent(this,cartView.class);
        startActivity(cart);
    }

    @Override
    public void onClick(View v) {
        if(NETWORK_STATE){
        switch (v.getId()){
            case R.id.take_a_poll:
                //code to show chrome custom tab for the poll
                if(pollUrl.isEmpty() || pollUrl.equals("") ){//|| pollUrl.equals(null)){
                    showSnack(getString(R.string.optionNotAvailable));
                }else openCustomTab(pollUrl);
                break;
            case R.id.apply_remove_filter:
                if(apply_remove_action){
                    //apply filter

                        String filter = generateFilterClause();
                        prepareProducts(filter);
                        apply_remove_action = false;
                        toggleApplyClearButton(FILTER_CLEAR_VISIBLE);


                }else {
                    //clear filter
                    semesterFilter.setSelection(0);
                    branchFilter.setSelection(0);
                    clearSubjectsFilter();
                    loadUI();
                    toggleApplyClearButton(FILTER_APPLY_GONE);
                }
                break;
            case R.id.error_Layout:
                loadUI();
                break;
        }
        }else {
            showSnack(getString(R.string.NetworkFaliure));
        }
    }

    public void clearSubjectsFilter(){
        subjectsStringArray.clear();
        subjectsStringArray.add(getString(R.string.filterTextSubject));
        subjectFilter.setSelection(0);
        subjectOptionsSpinnerAdapter.notifyDataSetChanged();
    }

    public void openCustomTab(String urlFromCall) {

        int color = getColor("#0097a7");
        int secondaryColor = getColor("#0097a7");

        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        intentBuilder.setToolbarColor(color);
        intentBuilder.setSecondaryToolbarColor(secondaryColor);

//        if (mShowActionButtonCheckbox.isChecked()) {
//            //Generally you do not want to decode bitmaps in the UI thread. Decoding it in the
//            //UI thread to keep the example short.
//            String actionLabel = getString(R.string.label_action);
//            Bitmap icon = BitmapFactory.decodeResource(getResources(),
//                    android.R.drawable.ic_menu_share);
//            PendingIntent pendingIntent =
//                    createPendingIntent(ActionBroadcastReceiver.ACTION_ACTION_BUTTON);
//            intentBuilder.setActionButton(icon, actionLabel, pendingIntent);
//        }

//        if (mAddMenusCheckbox.isChecked()) {
//            String menuItemTitle = getString(R.string.menu_item_title);
//            PendingIntent menuItemPendingIntent =
//                    createPendingIntent(ActionBroadcastReceiver.ACTION_MENU_ITEM);
//            intentBuilder.addMenuItem(menuItemTitle, menuItemPendingIntent);
//        }


        intentBuilder.addDefaultShareMenuItem();

//        if (mToolbarItemCheckbox.isChecked()) {
//            //Generally you do not want to decode bitmaps in the UI thread. Decoding it in the
//            //UI thread to keep the example short.
//            String actionLabel = getString(R.string.label_action);
//            Bitmap icon = BitmapFactory.decodeResource(getResources(),
//                    android.R.drawable.ic_menu_share);
//            PendingIntent pendingIntent =
//                    createPendingIntent(ActionBroadcastReceiver.ACTION_TOOLBAR);
//            intentBuilder.addToolbarItem(TOOLBAR_ITEM_ID, icon, actionLabel, pendingIntent);
//        }

        intentBuilder.setShowTitle(true);



        intentBuilder.enableUrlBarHiding();



        intentBuilder.setCloseButtonIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_back_black_24dp));

        intentBuilder.setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left);
        intentBuilder.setExitAnimations(this, android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        CustomTabActivityHelper.openCustomTab(
                this, intentBuilder.build(), Uri.parse(urlFromCall), new WebviewFallback());
    }

    private int getColor(String color) {
        try {
            return Color.parseColor(color);
        } catch (NumberFormatException ex) {
            Log.i(TAG, "Unable to parse Color: " + color);
            return Color.LTGRAY;
        }
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    private void showSnack(String snackString) {
        String message;
        message = snackString;
        Snackbar snackbar = Snackbar.make(activity_product_listing, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.d("networkChanged",isConnected?"Connected":"Not Connected");
        NETWORK_STATE = isConnected;
        if (isConnected) {
            showSnack(getString(R.string.good_connection));
//            loadUI();
//            refreshFragment();
        } else {
            showSnack(getString(R.string.NetworkFaliure));
        }

    }

    @Override
    public void onBackPressed() {
        if(NETWORK_STATE) {
            campusExchangeApp.getInstance().getmRequestQueue().cancelAll(TAG);
        }
        finish();
        super.onBackPressed();

    }

}
