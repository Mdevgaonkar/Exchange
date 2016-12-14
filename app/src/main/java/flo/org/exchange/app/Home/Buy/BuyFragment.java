package flo.org.exchange.app.Home.Buy;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import flo.org.exchange.R;
import flo.org.exchange.app.utils.ConnectivityReceiver;
import flo.org.exchange.app.utils.buyFragmentVariables;
import flo.org.exchange.app.utils.campusExchangeApp;

/**
 * A simple {@link Fragment} subclass.
 */
public class BuyFragment extends Fragment implements
        ViewPager.OnPageChangeListener,
        View.OnClickListener,
        ConnectivityReceiver.ConnectivityReceiverListener{

    private ProgressDialog mProgressDialog;

    private static final long ANIM_VIEWPAGER_DELAY = 5000;
    private static boolean NETWORK_STATE = false;
    private buyFragmentVariables buyFragmentVariablesObject;
    private Gson gson = campusExchangeApp.getInstance().getGson();


    private static final String PRODUCT_TITLE = "productTitle";
    private static final String PRODUCT_STATUS = "productStatus";
    private static final String PRODUCT_WHERE_CLAUSE = "productWhereClause";
    private static final String PRODUCT_CLASS = "productClass";
    private static final String PRODUCT_POLL = "productPoll";
    private static final String PRODUCT_POLL_URL = "productPollUrl";

    private static final String CAROUSAL = "carousal";
    private static final String CATEGORIES = "categories";
    private static final String STORES = "stores";

    private Handler handler;
    private Runnable animateViewPager;
    boolean stopSliding = false;

    protected View home_view;
    private ViewPager intro_images;
    private LinearLayout pager_indicator;
    private int dotsCount;
    private ImageView[] dots;
    private ViewPagerAdapter mAdapter;

    private FrameLayout fragment_buy;
    private LinearLayout store_cards;
    private TextView store_heading_text;
    private CardView[] card_view_store;
    private ArrayList<buyFragmentVariables.stores.items> storeItems;
    private LinearLayout booksCategory, utilitiesCategory,novelsCategory,servicesCategory,instumentsCategory,othersCategory;
    private LinearLayout buy_progressBarLayout;
    private LinearLayout buy_error_Layout;
    private ScrollView buy_scrollView;

    buyFragmentVariables.carousal varsCarousal;
    buyFragmentVariables.categories varsCategories;
    buyFragmentVariables.stores varsStores;

    private ArrayList<buyFragmentVariables.carousal.items> carousalItems;


    public BuyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        home_view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_buy,container,false);
        setupFramelayout();
        setupVariablesObject();
        setupViews();
        setupStoreCards();
        setupViewPager();
        setupCategoryViews();
        checkConnection();
        return home_view;

    }

    private void setupVariablesObject() {
        buyFragmentVariablesObject = new buyFragmentVariables(getActivity());
    }

    private void setupViews() {
        buy_progressBarLayout = (LinearLayout) home_view.findViewById(R.id.buy_progressBarLayout);
        buy_progressBarLayout.setVisibility(View.VISIBLE);
        buy_error_Layout = (LinearLayout) home_view.findViewById(R.id.error_Layout);
        buy_error_Layout.setVisibility(View.GONE);
        buy_error_Layout.setOnClickListener(this);
        buy_scrollView = (ScrollView) home_view.findViewById(R.id.buy_scrollView);
        buy_scrollView.setVisibility(View.GONE);
    }


    public int dpTopixels(int dps){
        final float scale = getActivity().getResources().getDisplayMetrics().density;
//        int pixels = (int) (dps * scale + 0.5f);
        int pixels = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dps,
                getActivity().getResources().getDisplayMetrics());
        return pixels;
    }

    public void setupStoreCards() {
        store_heading_text = (TextView) home_view.findViewById(R.id.store_heading_text);
        store_heading_text.setVisibility(View.GONE);
        storeItems = new ArrayList<>();
        store_cards = (LinearLayout) home_view.findViewById(R.id.store_cards);
        store_cards.setVisibility(View.GONE);
    }

    public void updateStoresView(){

        if(varsStores.count > 0 ) {
            //adding elements to the array
            storeItems.clear();
            for (int i = 0; i < varsStores.count; i++) {
                storeItems.add(i,varsStores.items.get(i));
            }
            if(storeItems.size()>0){
                store_heading_text.setVisibility(View.VISIBLE);
                store_cards.setVisibility(View.VISIBLE);
                //emptying the linear layout off stores
                if(store_cards.getChildCount() > 0){
                    store_cards.removeAllViews();
                }
                //adding views to the linear layout
                int number_of_cards = varsStores.count;
                card_view_store = new CardView[number_of_cards];
                for (int i = 0; i < number_of_cards; i++) {
                    View store_card_view = getActivity().getLayoutInflater().inflate(R.layout.store_card_view,card_view_store[i],false);
                    card_view_store [i]= (CardView) store_card_view.findViewById(R.id.card_view_top_store);
                    ImageView iv = (ImageView) store_card_view.findViewById(R.id.store_image);
                    try{
                        Glide.with(getActivity())
                                .load(Uri.parse(storeItems.get(i).photoUrl))
                                .thumbnail(0.5f)
                                .crossFade()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(iv);
                    }catch (NullPointerException e){
                        e.printStackTrace();
                        Log.d("Stores :", "Photos Not Loaded");
                        iv.setImageDrawable(getResources().getDrawable(R.drawable.sidebar_header_background));
                    }

                    final int index = i;
                    iv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    gatherExtrasFromStores(index);
//                                        showSnack("you chose store "+ index);

                            }
                    });

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            dpTopixels(300),
                            dpTopixels(110)
                    );

//            params.leftMargin = dpTopixels(8);
                    params.setMargins(dpTopixels(8),0,0,0);

                    store_cards.addView(store_card_view,params);

                }
            }else {
                store_heading_text.setVisibility(View.GONE);
                store_cards.setVisibility(View.GONE);
            }
    }

}

    public void gatherExtrasFromStores(int index){
        int status = 0;
        String whereClause= null;
        String ___class = null;
        String title = null;
        boolean poll =  false;
        String pollUrl = null;
        title = varsStores.items.get(index).title;
        status = varsStores.items.get(index).Status;
        poll = varsStores.items.get(index).poll;
        pollUrl = varsStores.items.get(index).pollUrl;
        whereClause = varsStores.items.get(index).whereClause;
        ___class = varsStores.items.get(index).___class;
//                 showSnack(title+" "+status+" "+whereClause);
        if(status == 1){
            showSnack(getString(R.string.optionNotAvailable));
        }else {
            openProductStoreList(title,status,whereClause,poll,pollUrl,___class);
        }

    }

    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        NETWORK_STATE = isConnected;
//        showSnack(isConnected);
        if (isConnected){
            fetchVariables();
        }else {
            showSnack(getString(R.string.NetworkFaliure));
            fetchOfflineData();
        }
    }

    private void fetchOfflineData() {
        String offlineJSONdataString = buyFragmentVariablesObject.getOfflineJson();
        String carousalString = null, categoryString = null, storesString = null;
        try {
            JSONObject offlineJSONdata = new JSONObject(offlineJSONdataString);
            carousalString = offlineJSONdata.get(CAROUSAL).toString();
            categoryString = offlineJSONdata.get(CATEGORIES).toString();
            storesString = offlineJSONdata.get(STORES).toString();
            varsCarousal = gson.fromJson(carousalString,buyFragmentVariables.carousal.class);
            varsCategories = gson.fromJson(categoryString,buyFragmentVariables.categories.class);
            varsStores = gson.fromJson(storesString,buyFragmentVariables.stores.class);
            updateViewpagerData();
            hideProgressBar();
            hideErrorLayout();
        } catch (JSONException e) {
            showErrorLayout();
            Log.d("Offline","Not available");
            e.printStackTrace();
        }


    }

    private void showErrorLayout() {
        buy_progressBarLayout.setVisibility(View.GONE);
        buy_error_Layout.setVisibility(View.VISIBLE);
        buy_scrollView.setVisibility(View.GONE);
    }

    private void hideErrorLayout() {
        buy_error_Layout.setVisibility(View.GONE);
    }

    private void setupFramelayout() {
        fragment_buy = (FrameLayout) home_view.findViewById(R.id.frame_buy_fragment);
    }

    private void setupCategoryViews() {
        booksCategory = (LinearLayout) home_view.findViewById(R.id.booksCategory);
        booksCategory.setOnClickListener(this);
        utilitiesCategory = (LinearLayout) home_view.findViewById(R.id.utilitiesCategory);
        utilitiesCategory.setOnClickListener(this);
        novelsCategory = (LinearLayout) home_view.findViewById(R.id.novelsCategory);
        novelsCategory.setOnClickListener(this);
        servicesCategory = (LinearLayout) home_view.findViewById(R.id.servicesCategory);
        servicesCategory.setOnClickListener(this);
        instumentsCategory = (LinearLayout) home_view.findViewById(R.id.instumentsCategory);
        instumentsCategory.setOnClickListener(this);
        othersCategory = (LinearLayout) home_view.findViewById(R.id.othersCategory);
        othersCategory.setOnClickListener(this);

    }

    public void updateViewpagerData(){
        if(varsCarousal.count > 0 ) {
            carousalItems.clear();
            for (int i = 0; i < varsCarousal.count; i++) {
                carousalItems.add(i,varsCarousal.items.get(i));
            }
            mAdapter.notifyDataSetChanged();
            if(carousalItems.size()>0){
                intro_images.setVisibility(View.VISIBLE);
                pager_indicator.setVisibility(View.VISIBLE);
                setupPageIndicatorDots();
            }else {
                intro_images.setVisibility(View.GONE);
                pager_indicator.setVisibility(View.GONE);
            }
            intro_images.setOnPageChangeListener(this);

        }

    }

    public void setupViewPager() {
        intro_images = (ViewPager) home_view.findViewById(R.id.pager_introduction);
        pager_indicator = (LinearLayout) home_view.findViewById(R.id.viewPagerIndicator);
        intro_images.setClipToPadding(false);
        intro_images.setPadding(40,0,40,0);
        intro_images.setPageMargin(16);
        carousalItems = new ArrayList<>();
        mAdapter = new ViewPagerAdapter(getContext(),carousalItems);
        intro_images.setAdapter(mAdapter);
        intro_images.setCurrentItem(0);
        if(carousalItems.size()>0){
            intro_images.setVisibility(View.VISIBLE);
            setupPageIndicatorDots();
        }else {
            intro_images.setVisibility(View.GONE);
            if(pager_indicator.getChildCount() > 0) {
                pager_indicator.setVisibility(View.GONE);
                pager_indicator.removeAllViews();
            }
        }
        intro_images.setOnPageChangeListener(this);
        Log.d("Carousal","has "+mAdapter.getCount()+" Pages");
        runnable(mAdapter.getCount());
        handler.postDelayed(animateViewPager,ANIM_VIEWPAGER_DELAY);
    }

    private void setupPageIndicatorDots() {

        if(pager_indicator.getChildCount() > 0) {
            pager_indicator.removeAllViews();
            Log.d("Page indicators","Removed");
        }
        dotsCount = mAdapter.getCount();
        dots = new ImageView[dotsCount];
        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(getContext());
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT

            );

            params.setMargins(4, 0, 4, 0);

            pager_indicator.addView(dots[i], params);
        }

        dots[0].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));
    }

    public void runnable(final int size) {
        handler = new Handler();
        animateViewPager = new Runnable() {
            public void run() {
                if (!stopSliding) {
                    if (intro_images.getCurrentItem() == mAdapter.getCount() - 1) {
                        Log.d("ViewPager","slide to 1");
                        intro_images.setCurrentItem(0);
                    } else {
                        intro_images.setCurrentItem(intro_images.getCurrentItem() + 1, true);
                        Log.d("ViewPager","slide to "+(intro_images.getCurrentItem()+1));
                    }
                    handler.postDelayed(animateViewPager, ANIM_VIEWPAGER_DELAY);
                }
            }
        };
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < dotsCount; i++) {
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));
        }
        dots[position].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void gatherExtrasWithIntent(int index){
        int status = 0;
        String whereClause= null;
        String ___class = null;
        String title = null;
        boolean poll =  false;
        String pollUrl = null;
        title = varsCategories.items.get(index).title;
        status = varsCategories.items.get(index).Status;
        poll = varsCategories.items.get(index).poll;
        pollUrl = varsCategories.items.get(index).pollUrl;
        whereClause = varsCategories.items.get(index).whereClause;
        ___class = varsCategories.items.get(index).___class;
//                 showSnack(title+" "+status+" "+whereClause);
        if(status == 1){
            showSnack(getString(R.string.optionNotAvailable));
        }else {
            openProductStoreList(title,status,whereClause,poll,pollUrl,___class);
        }

    }

    @Override
    public void onClick(View v) {



         switch (v.getId()){
             case R.id.booksCategory:
                 gatherExtrasWithIntent(0);
                break;
             case R.id.instumentsCategory:
                 gatherExtrasWithIntent(1);
                break;
             case R.id.novelsCategory:
                 gatherExtrasWithIntent(2);
                 break;
             case R.id.servicesCategory:
                 gatherExtrasWithIntent(3);
                 break;
             case R.id.utilitiesCategory:
                 gatherExtrasWithIntent(4);
                 break;
             case R.id.othersCategory:
                 gatherExtrasWithIntent(5);
                 break;
             case R.id.error_Layout:
                 refreshFragment();
                 break;
        }
    }

//    private void showProgressDialog(String showString) {
//        if (mProgressDialog == null) {
//            mProgressDialog = new ProgressDialog(getActivity());
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
    private void openProductStoreList(String productTitle, int productListStatus, String whereClause, boolean poll, String pollUrl, String productClass) {
        Intent openProductStore = new Intent(getActivity(), flo.org.exchange.app.Home.listing.productListingActivity.class);
//        openProductStore.putExtra(PRODUCT_TYPE, productType);
        openProductStore.putExtra(PRODUCT_TITLE, productTitle);
        openProductStore.putExtra(PRODUCT_STATUS, productListStatus);
        openProductStore.putExtra(PRODUCT_WHERE_CLAUSE, whereClause);
        openProductStore.putExtra(PRODUCT_CLASS,productClass);
        openProductStore.putExtra(PRODUCT_POLL,poll);
        openProductStore.putExtra(PRODUCT_POLL_URL,pollUrl);
        startActivity(openProductStore);

    }

    @Override
    public void onResume() {
        // register connection status listener
        campusExchangeApp.getInstance().setConnectivityListener(this);

        handler.removeCallbacks(animateViewPager);
        runnable(mAdapter.getCount());
        //Re-run callback
        handler.postDelayed(animateViewPager, ANIM_VIEWPAGER_DELAY);
        super.onResume();
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(animateViewPager);
        super.onPause();
    }

    private void showSnack(String snackString) {
        String message;
        message = snackString;
        Snackbar snackbar = Snackbar.make(fragment_buy, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void showProgressBar(){
        buy_progressBarLayout.setVisibility(View.VISIBLE);
        buy_scrollView.setVisibility(View.GONE);
        buy_error_Layout.setVisibility(View.GONE);
    }

    private void hideProgressBar(){
        buy_progressBarLayout.setVisibility(View.GONE);
        buy_scrollView.setVisibility(View.VISIBLE);
        buy_error_Layout.setVisibility(View.GONE);
    }

    private void fetchVariables(){
        showProgressBar();
        JsonObjectRequest fetchVariables = new JsonObjectRequest(
                Request.Method.GET,
                getString(R.string.buyPageVariables),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        saveOfflineData(response.toString());

                        String carousalString = null, categoryString = null, storesString = null;
                        try {
                            carousalString = response.get(CAROUSAL).toString();
                            categoryString = response.get(CATEGORIES).toString();
                            storesString = response.get(STORES).toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        varsCarousal = gson.fromJson(carousalString,buyFragmentVariables.carousal.class);
                        varsCategories = gson.fromJson(categoryString,buyFragmentVariables.categories.class);
                        varsStores = gson.fromJson(storesString,buyFragmentVariables.stores.class);
//                        showProgressDialog(varsCategories.items.get(0).Status + "\n\n" /*+ varsCategories.items.toString()+ "\n\n" +varsStores.items.toString()*/);
                        updateViewpagerData();
                        updateStoresView();
                        hideProgressBar();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("volley error",error.toString());
                        showErrorLayout();
                    }
                }
        );

        campusExchangeApp.getInstance().addToRequestQueue(fetchVariables,"FetchingVariables");

    }

    private void saveOfflineData(String responseJSONString){
        buyFragmentVariablesObject.setOfflineJson(responseJSONString);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.d("networkChanged",isConnected?"Connected":"Not Connected");
        NETWORK_STATE = isConnected;
        if (isConnected) {
            showSnack(getString(R.string.good_connection));
            refreshFragment();
        } else {
            showSnack(getString(R.string.NetworkFaliure));
        }

    }

    private void refreshFragment() {

        showProgressBar();
        fetchVariables();

    }
}
