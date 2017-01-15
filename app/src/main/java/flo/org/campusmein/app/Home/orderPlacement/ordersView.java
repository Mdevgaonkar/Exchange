package flo.org.campusmein.app.Home.orderPlacement;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flo.org.campusmein.R;
import flo.org.campusmein.app.utils.ConnectivityReceiver;
import flo.org.campusmein.app.utils.EndlessRecyclerViewScrollListener;
import flo.org.campusmein.app.utils.campusExchangeApp;
import flo.org.campusmein.app.utils.orders;

public class ordersView extends AppCompatActivity
        implements ConnectivityReceiver.ConnectivityReceiverListener
                    ,View.OnClickListener {

    private static final String TAG = ordersView.class.getSimpleName();
    private static final String QUERY = "?";
    private static final String LOAD_RELATIONS ="loadRelations=product.book%2Cproduct.combopack%2Cproduct.instrument";
    private static final String QUERY_SEPERATOR = "&";
    private static final String PAGE_SIZE = "pageSize=20";
    private static final String WHERE_EQUAL_TO = "where=";
    private static final String ___CLASS = "orders";
    //buyerId.objectId%20%3D%20%273FE37766-2E46-F9B1-FFFA-0A5C9C731E00%27%20AND%20(status%20%3D%20-1%20OR%20status%20%3D%201%20OR%20status%20%3D%20-2%20)
    private static final String WHERE_CLAUSE1 = "buyerId.objectId%20%3D%20%27"+campusExchangeApp.getInstance().getUniversalPerson().getPersonObjectId()+"%27%20AND%20(status%20%3D%20-1%20OR%20status%20%3D%201%20OR%20status%20%3D%20-2%20)";
    private static final String WHERE_CLAUSE2 = "buyerId.objectId%3D%27"+campusExchangeApp.getInstance().getUniversalPerson().getPersonObjectId()+"%27";
    private static final String RESPONSE_DATA = "data";
    private static final String SRC_STR_KEY = "src";
    private String src = "sidebar";

    private CoordinatorLayout activity_ord_listing;
    private CardView empty_layout, error_layout;
    private LinearLayout  loadingLayout;
    private RecyclerView ordListLayout;
    private boolean NETWORK_STATE= false;


    private List<orders> ListOforders ;//= new ArrayList<>();
    private ordersListAdapter ordersListAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private String nextPage="";
    private ProgressBar ord_loadMore_progress;
    private TextView ord_allOrdersShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_view);
        setupCoordinatorLayout();
        setupActionbar(getString(R.string.myOrders));
        checkConnection();
        setupEmptyAndErrorLayout();
        setupLoadingLayout();
        setupOrdListLayout();
        setupSource();
    }

    private void setupSource() {
        Intent sourceIntent = getIntent();
        Bundle sourceBundle = sourceIntent.getExtras();
        if(sourceBundle != null){
            src = sourceBundle.getString(SRC_STR_KEY, "sidebar");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        campusExchangeApp.getInstance().setConnectivityListener(this);
        loadUI();
    }



    private void loadUI() {
        if (NETWORK_STATE){
            fetchOrders();
        }else {
            showSnack(getString(R.string.NetworkFaliure), Snackbar.LENGTH_LONG);
            show_error_layout();
        }

    }

    private void fetchOrders(){

//       http://api.backendless.com/test/data/orders?loadRelations=product.book%2Cproduct.combopack%2Cproduct.instrument
        String ordersRequest = getString(R.string.baseBackendUrl);
//        productRequest = productRequest+getString(R.string.products);
        if(src.equals("sidebar")){
            ordersRequest = ordersRequest+___CLASS+QUERY+PAGE_SIZE+QUERY_SEPERATOR+LOAD_RELATIONS+QUERY_SEPERATOR+WHERE_EQUAL_TO+WHERE_CLAUSE1;
        }else{
            ordersRequest = ordersRequest+___CLASS+QUERY+PAGE_SIZE+QUERY_SEPERATOR+LOAD_RELATIONS+QUERY_SEPERATOR+WHERE_EQUAL_TO+WHERE_CLAUSE2;
        }

        show_loadingLayout();
        JsonObjectRequest getOrderList = new JsonObjectRequest(
                Request.Method.GET,
                ordersRequest,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String responseData = response.getJSONArray(RESPONSE_DATA).toString();
                            Gson gson = campusExchangeApp.getInstance().getGson();
                            List<orders> ordersList= Arrays.asList(gson.fromJson(responseData,orders[].class));
                            ListOforders.clear();
                            for(orders order : ordersList) {
                                ListOforders.add(order);
                            }

                            nextPage = response.getString("nextPage");
                            Log.d("nextPage",nextPage);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Product list","no rational data");
                            show_error_layout();

                        }
                        updateDataSetOfRecyclerView();
                        updateUI();
                        hide_loadingLayout();

//                        tv.setText(response.toString());
//                        tv.setVisibility(View.VISIBLE);
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        show_error_layout();
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

        campusExchangeApp.getInstance().addToRequestQueue(getOrderList,TAG);
    }

    private void updateUI() {
        if (ListOforders.size() == 0) {
            show_empty_layout();
            Log.d("Product listing","List is Empty");
        }
        else {
            show_ordListLayout();
            Log.d("Product listing","List is shown");
        }
    }

    private void updateDataSetOfRecyclerView() {
        ordersListAdapter.notifyDataSetChanged();
    }

    private void setupCoordinatorLayout() {
        activity_ord_listing = (CoordinatorLayout) findViewById(R.id.activity_ord_listing);
    }

    private void setupOrdListLayout(){
        ordListLayout = (RecyclerView) findViewById(R.id.ordListLayout);
        ListOforders = new ArrayList<>();
        ordersListAdapter = new ordersListAdapter(ListOforders,this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        ordListLayout.setLayoutManager(mLayoutManager);
        ordListLayout.setItemAnimator(new DefaultItemAnimator());
        ordListLayout.setAdapter(ordersListAdapter);

        ord_loadMore_progress = (ProgressBar) findViewById(R.id.ord_loadMore_progress);
        ord_loadMore_progress.setVisibility(View.GONE);
        ord_allOrdersShown = (TextView) findViewById(R.id.ord_allOrdersShown);
        ord_allOrdersShown.setVisibility(View.GONE);

        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                if(!nextPage.isEmpty() && !nextPage.equals("null")){
                    loadNextDataFromApi(nextPage);
                    ord_loadMore_progress.setVisibility(View.VISIBLE);
                    ord_allOrdersShown.setVisibility(View.GONE);
                }else {
                    ord_loadMore_progress.setVisibility(View.GONE);
                    ord_allOrdersShown.setVisibility(View.VISIBLE);
                }



            }
        };
        // Adds the scroll listener to RecyclerView
        ordListLayout.addOnScrollListener(scrollListener);

        hide_ordListLayout();


    }



    private void setupLoadingLayout(){
        loadingLayout = (LinearLayout) findViewById(R.id.lodaingLayout);
        hide_loadingLayout();
    }

    private void hide_loadingLayout() {
        loadingLayout.setVisibility(View.GONE);
    }

    private void show_loadingLayout() {
        loadingLayout.setVisibility(View.VISIBLE);
        hide_ordListLayout();
        hide_error_layout();
        hide_empty_layout();
    }

    private void hide_ordListLayout() {
        ordListLayout.setVisibility(View.GONE);
    }

    private void show_ordListLayout() {
        ordListLayout.setVisibility(View.VISIBLE);
        hide_error_layout();
        hide_empty_layout();
    }

    private void setupEmptyAndErrorLayout(){
        empty_layout = (CardView) findViewById(R.id.empty_Layout);
        error_layout = (CardView) findViewById(R.id.error_Layout);
        hide_empty_layout();
        hide_error_layout();
    }

    private void hide_error_layout() {
        error_layout.setVisibility(View.GONE);
    }

    private void hide_empty_layout() {
        empty_layout.setVisibility(View.GONE);
    }

    private void show_error_layout() {
        error_layout.setVisibility(View.VISIBLE);
        hide_empty_layout();
        hide_ordListLayout();
    }

    private void show_empty_layout() {
        empty_layout.setVisibility(View.VISIBLE);
        hide_error_layout();
        hide_ordListLayout();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void checkConnection() {
        NETWORK_STATE = ConnectivityReceiver.isConnected();
//        showSnack(isConnected);

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.d("networkChanged",isConnected?"Connected":"Not Connected");
        NETWORK_STATE = isConnected;
        if (isConnected) {
            showSnack(getString(R.string.good_connection), Snackbar.LENGTH_LONG);
        } else {
            showSnack(getString(R.string.NetworkFaliure), Snackbar.LENGTH_LONG);
        }
    }

    private void showSnack(String snackString, int timePeriod) {

        Snackbar snackbar = Snackbar.make(activity_ord_listing, snackString, timePeriod);
        snackbar.show();
    }

    private void setupActionbar(String title) {
        Toolbar activityToolbar = (Toolbar) findViewById(R.id.action_bar_cart_listings);
        setSupportActionBar(activityToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(title);
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

    private void loadNextDataFromApi(String nextPage) {

        JsonObjectRequest getOrderList = new JsonObjectRequest(
                Request.Method.GET,
                nextPage,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String responseData = response.getJSONArray(RESPONSE_DATA).toString();
                            Gson gson = campusExchangeApp.getInstance().getGson();
                            List<orders> ordersList= Arrays.asList(gson.fromJson(responseData,orders[].class));
                            for(orders order : ordersList) {
                                ListOforders.add(order);
                            }

                            String nextPageNew= response.getString("nextPage");
                            updateNextPage(nextPageNew);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Product list","no rational data");
                            show_error_layout();

                        }
                        updateDataSetOfRecyclerView();
//                        updateUI();
//                        hide_loadingLayout();

//                        tv.setText(response.toString());
//                        tv.setVisibility(View.VISIBLE);
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showSnack(getString(R.string.someErrorOccurred), Snackbar.LENGTH_INDEFINITE);
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

        campusExchangeApp.getInstance().addToRequestQueue(getOrderList,TAG);

    }

    private void updateNextPage(String nextPageNew) {
        nextPage = nextPageNew;
        Log.d("nextPage",nextPage);
    }

}
