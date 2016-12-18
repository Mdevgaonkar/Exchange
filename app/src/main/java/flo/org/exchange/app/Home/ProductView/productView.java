package flo.org.exchange.app.Home.ProductView;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flo.org.exchange.R;
import flo.org.exchange.app.Login.College;
import flo.org.exchange.app.utils.BackendlessResponse;
import flo.org.exchange.app.utils.Books;
import flo.org.exchange.app.utils.ConnectivityReceiver;
import flo.org.exchange.app.utils.Instruments;
import flo.org.exchange.app.utils.Products;
import flo.org.exchange.app.utils.Subjects;
import flo.org.exchange.app.utils.campusExchangeApp;



public class productView extends AppCompatActivity
        implements View.OnClickListener,ConnectivityReceiver.ConnectivityReceiverListener{

//    http://api.backendless.com/test/data/products?
    private static final String ___CLASS = "products";
    private static final String LOAD_RELATIONS ="loadRelations=book%2Csubject%2Ccollege%2Cspecialization%2Ccombopack%2Ccombopack.books%2Ccombopack.instruments%2Cinstrument";
    private static final String SLASH = "/";
    private static final String QUERY = "?";
    private boolean NETWORK_STATE = false;

    private Toolbar activityToolbar;
    private CoordinatorLayout activity_product_view;
    private NestedScrollView product_scrollView;
    private HorizontalScrollView comboItemsView;
    private LinearLayout comboItemsViewLayout;
    private CardView bannerView,disclaimerView;
    private TextView disclaimerText;
    private ImageView banner;
    private ImageView productImageView;
    private TextView productTitle,productSubtitle;
    private TextView removedText;
    private LinearLayout MRPView;
    private TextView MRP;
    private Button btn_addToCart,btn_buyNow;
    private LinearLayout error_Layout,progressBarLayout,removedView;
    private LinearLayout product_layout;

    private TextView tab_title_details,tab_title_description;
    private LinearLayout tab_indicator_details,tab_indicator_description;
    private LinearLayout tab_content_details,tab_content_description;
    private TextView tab_content_text_details,tab_content_text_description;

    private static final String PRODUCT_TYPE = "type";
    private static final String PRODUCT_OBJECT_ID = "objectId";
    private static final String BIC_OBJECT_ID = "bicObjectId";

    private Products product;
    private CardView[] itemInstrumentView;
    private CardView[] itemBookView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_view);
        Bundle b = getExtrasFromIntent();
        String type = b.getString(PRODUCT_TYPE);
        String product_objectId = b.getString(PRODUCT_OBJECT_ID);
        String bic_objectId = b.getString(BIC_OBJECT_ID);

        setupActionbar(type);
        checkConnection();
        setupCoordinatorLayout();
        setupProduct_scrollView();
        setupBannerView();
        setupDisclaimerView();
        if(NETWORK_STATE) {
            fetchProduct(product_objectId);
        }else {
            showSnack(getString(R.string.NetworkFaliure));
            showErrorLayout();
        }

    }

    private void fetchProduct(String product_objectId) {
//        http://api.backendless.com/test/data/products/E0686264-7306-2371-FFBC-5826F3B11F00?
        String productRequest = getString(R.string.baseBackendUrl);
//        productRequest = productRequest+getString(R.string.products);
        productRequest = productRequest+___CLASS+SLASH+product_objectId+QUERY+LOAD_RELATIONS;
        showProgressLayout();

        JsonObjectRequest getProduct = new JsonObjectRequest(
                Request.Method.GET,
                productRequest,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        try {
                            Gson gson = campusExchangeApp.getInstance().getGson();
//                            BackendlessResponse responseData = gson.fromJson(response.toString(),BackendlessResponse.class);
                            String responseData = response.toString();
                            product = gson.fromJson(responseData,Products.class);

                        if(product.removed){
                            showremovedView(product.reasonRemoved);
                        }else {
                            updateUI();
                        }

//                        setDetailsText(response.toString());
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

        campusExchangeApp.getInstance().addToRequestQueue(getProduct,"gettingProduct");
    }

    private void updateUI() {
//        method calls to show proper fields
        showProductLayout();
        setGeneralProperties();
        String productType = product.type;
        if(productType.equals(getString(R.string.bookType))){
            setBookProperties();
        }else if(productType.equals(getString(R.string.instrumentType))){
            setInstrumentProperties();
        }else if(productType.equals(getString(R.string.comboType))){
            setCombopackProperties();
        }else {
            showErrorLayout();
        }
    }

    private void setGeneralProperties(){
        setProductImageView(product.type);
        if(product.bannerUrl == null){
            hideBannerView();
        }else {
            showBannerView(product.bannerUrl);
        }
        showMRPView(String.valueOf(product.mrp));
        setPrice(String.valueOf(product.listPrice));
        if(product.disclaimerText == null){
           hideDisclaimerView();
        }else {
            showDisclaimerView(product.disclaimerText);
        }

    }

    private void setBookProperties(){
        setTitle(product.book.title);
        setSubTitle(product.book.publisher);

        String details = getBookDetails();
        setDetailsText(details);
        setDescriptionText(product.book.description);
    }

    private String getBookDetails(){
        String Details="";
        final String bold= "<b>";
        final String endBold = "</b>";
        final String greyFont= "<font color=grey>";
        final String endGreyFont = "</font>";
        final String breakTag = "<br>";

        Details = Details + greyFont + getString(R.string.title) + endGreyFont + product.book.title+breakTag+breakTag;
        String condition=getCondition();
        Details = Details + greyFont + getString(R.string.condition) + endGreyFont + condition+breakTag+breakTag;
        Details = Details + greyFont + getString(R.string.ISBN) + endGreyFont + product.book.ISBN+breakTag+breakTag;
        Details = Details + greyFont + getString(R.string.author) + endGreyFont + product.book.author+breakTag+breakTag;
        Details = Details + greyFont + getString(R.string.publisher) + endGreyFont + product.book.publisher+breakTag+breakTag;
        Details = Details + greyFont + getString(R.string.edition) + endGreyFont + product.book.edition+breakTag+breakTag;
        Details = Details + greyFont + getString(R.string.pubYear) + endGreyFont + product.book.publicationYear+breakTag+breakTag;
        String subjects = getSubjects();
        Details = Details + greyFont + getString(R.string.subjects) + endGreyFont + subjects+breakTag+breakTag;
        Details = Details + greyFont + getString(R.string.term) + endGreyFont + product.term+breakTag+breakTag;
        String courses = getCourses();
        Details = Details + greyFont + getString(R.string.course) + endGreyFont + courses;

        return Details;
    }

    private String getCourses() {
        String courses = "";
        for(College.Branch b: product.specialization){
            if(courses.equals("")){
                courses = courses + b.branch;
            }else {
                courses = courses + ", " + b.branch;
            }
        }
        return courses;
    }

    private String getSubjects(){
        String subjects = "";
        for(Subjects subs : product.subject){
            if(subjects.equals("")){
                subjects = subjects + subs.subject;
            }else {
                subjects = subjects + ", " + subs.subject;
            }
        }
        return subjects;
    }

    private String getCondition(){
        String condition="";
        if(product.condition == 0){
            condition = getString(R.string.newCondition);
        }else if(product.condition == 1){
            condition = getString(R.string.good_connection);
        }else if(product.condition == -1){
            condition = getString(R.string.badCondition);
        }else {
            condition = getString(R.string.not_available);
        }
        return condition;
    }

    private void setInstrumentProperties(){
        setTitle(product.instrument.instrumentName);
        setSubTitle(product.instrument.instrumentSubtitle);
        String details = getInstrumentDetails();
        setDetailsText(details);
        setDescriptionText(product.instrument.description);
    }

    private String getInstrumentDetails(){
        String Details="";
        final String bold= "<b>";
        final String endBold = "</b>";
        final String greyFont= "<font color=grey>";
        final String endGreyFont = "</font>";
        final String breakTag = "<br>";

        Details = Details + greyFont + getString(R.string.title) + endGreyFont + product.instrument.instrumentName+" ("+product.instrument.instrumentSubtitle+")"+breakTag+breakTag;
        String condition=getCondition();
        Details = Details + greyFont + getString(R.string.condition) + endGreyFont + condition+breakTag+breakTag;
        Details = Details + greyFont + getString(R.string.category) + greyFont + product.instrument.type +breakTag+breakTag;
        String subjects = getSubjects();
        Details = Details + greyFont + getString(R.string.subjects) + endGreyFont + subjects+breakTag+breakTag;
        Details = Details + greyFont + getString(R.string.term) + endGreyFont + product.term+breakTag+breakTag;
        String courses = getCourses();
        Details = Details + greyFont + getString(R.string.course) + endGreyFont + courses;

        return Details;
    }

    private void setCombopackProperties(){
        setTitle(product.combopack.title);
        setSubTitle(product.combopack.subTitle);
        addCombopackItems(product.combopack.books, product.combopack.instruments);
        setDetailsText(getGeneralComboDetails());
        setDescriptionText(product.combopack.description);

    }

    private String getGeneralComboDetails() {
        String Details="";
        final String bold= "<b>";
        final String endBold = "</b>";
        final String greyFont= "<font color=grey>";
        final String endGreyFont = "</font>";
        final String breakTag = "<br>";

        Details = Details + greyFont + getString(R.string.title) + endGreyFont + product.combopack.title+" ("+product.combopack.subTitle+")"+breakTag+breakTag;
        String condition=getCondition();
        Details = Details + greyFont + getString(R.string.condition) + endGreyFont + condition+breakTag+breakTag;
        String subjects = getSubjects();
        Details = Details + greyFont + getString(R.string.subjects) + endGreyFont + subjects+breakTag+breakTag;
        Details = Details + greyFont + getString(R.string.term) + endGreyFont + product.term+breakTag+breakTag;
        String courses = getCourses();
        Details = Details + greyFont + getString(R.string.course) + endGreyFont + courses;

        return Details;
    }

    private void addCombopackItems(ArrayList<Books> books, ArrayList<Instruments> instruments) {
        if(comboItemsViewLayout.getChildCount()>0){
            comboItemsViewLayout.removeAllViews();
        }
        if(books.size()>0){

            int number_of_books= books.size();
            itemBookView = new CardView[number_of_books];
            for (int i = 0; i < number_of_books; i++){
                View comboBookView = getLayoutInflater().inflate(R.layout.combo_items_view,itemBookView[i],false);
                itemBookView [i]= (CardView) comboBookView.findViewById(R.id.comboItemsViewCard);
                TextView tv = (TextView) comboBookView.findViewById(R.id.itemDescription);
                tv.setText(getComboBookDetails(i));
                ImageView iv = (ImageView) comboBookView.findViewById(R.id.itemImage);
                iv.setImageResource(R.drawable.ic_combo_default);
                try{
                    Glide.with(this)
                            .load(books.get(i).photofile)
                            .thumbnail(0.5f)
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(iv);
                }catch (NullPointerException e){
                    e.printStackTrace();
                    Log.d("comboBook :", "Photo Not Loaded");
                    iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_combo_error));
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        dpTopixels(320),
                        dpTopixels(128)
                );

//            params.leftMargin = dpTopixels(8);
                params.setMargins(dpTopixels(8),0,0,0);

                comboItemsViewLayout.addView(comboBookView,params);

            }

            comboItemsView.setVisibility(View.VISIBLE);

        }
        if(instruments.size()>0){

            int number_of_instruments= instruments.size();
            itemInstrumentView = new CardView[number_of_instruments];
            for (int i = 0; i < number_of_instruments; i++){
                View comboInstrumentView = getLayoutInflater().inflate(R.layout.combo_items_view,itemBookView[i],false);
                itemBookView [i]= (CardView) comboInstrumentView.findViewById(R.id.comboItemsViewCard);
                TextView tv = (TextView) comboInstrumentView.findViewById(R.id.itemDescription);
                tv.setText(getComboInstrumentDetails(i));
                ImageView iv = (ImageView) comboInstrumentView.findViewById(R.id.itemImage);
                iv.setImageResource(R.drawable.ic_combo_default);
                try{
                    Glide.with(this)
                            .load(instruments.get(i).photofile)
                            .thumbnail(0.5f)
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(iv);
                }catch (NullPointerException e){
                    e.printStackTrace();
                    Log.d("comboInstrument :", "Photo Not Loaded");
                    iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_combo_error));
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        dpTopixels(320),
                        dpTopixels(128)
                );

//            params.leftMargin = dpTopixels(8);
                params.setMargins(dpTopixels(8),0,0,0);

                comboItemsViewLayout.addView(comboInstrumentView,params);

            }

            comboItemsView.setVisibility(View.VISIBLE);

        }

    }

    private String getComboInstrumentDetails(int position) {
        String Details="";
        final String bold= "<b>";
        final String endBold = "</b>";
        final String italic = "<i>";
        final String endItalic = "</i>";
        final String greyFont= "<font color=grey>";
        final String endGreyFont = "</font>";
        final String breakTag = "<br>";
        final String colon = " : ";

        Details = Details + bold+product.combopack.instruments.get(position).instrumentName+endBold+breakTag+breakTag; // title

        Details = Details + italic +getString(R.string.byString) + product.combopack.instruments.get(position).instrumentSubtitle+endItalic+breakTag+breakTag; //Subtitle

        Details = Details + getString(R.string.categoryNoSpace) + product.combopack.instruments.get(position).type +breakTag+breakTag; //category

        Details = Details + getString(R.string.categoryNoSpace) + product.combopack.instruments.get(position).description+breakTag+breakTag; //description

        return Details;
    }

    public int dpTopixels(int dps){
        int pixels = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dps,
                getResources().getDisplayMetrics());
        return pixels;
    }

    private String getComboBookDetails(int position){
        String Details="";
        final String bold= "<b>";
        final String endBold = "</b>";
        final String italic = "<i>";
        final String endItalic = "</i>";
        final String greyFont= "<font color=grey>";
        final String endGreyFont = "</font>";
        final String breakTag = "<br>";
        final String colon = " : ";

        Details = Details + bold+product.combopack.books.get(position).title+endBold+breakTag+breakTag; // title

        Details = Details + italic +getString(R.string.byString) + product.combopack.books.get(position).author+endItalic+breakTag+breakTag; //author

        Details = Details + product.combopack.books.get(position).description+breakTag+breakTag; //description

        Details = Details + product.combopack.books.get(position).publisher+breakTag; // publisher

        Details = Details + getString(R.string.editionNoSpace)+ product.combopack.books.get(position).edition+breakTag;// edition

        Details = Details + getString(R.string.pubYearNoSpace)+ product.combopack.books.get(position).publicationYear+breakTag; //publication year

        Details = Details + getString(R.string.ISBNnoSpace)+colon+ product.combopack.books.get(position).ISBN+breakTag; //ISBN

        return Details;
    }

    private void hideErrorLayout() {
        error_Layout.setVisibility(View.GONE);
    }

    private void showErrorLayout() {
        error_Layout.setVisibility(View.VISIBLE);
        hideProductlayout();
        hideProgressLayout();
        hideremovedView();
    }

    private void hideProgressLayout() {
        progressBarLayout.setVisibility(View.GONE);
    }

    private void showProgressLayout() {
        progressBarLayout.setVisibility(View.VISIBLE);
        hideErrorLayout();
        hideProductlayout();
        hideremovedView();
    }

    private void setupDisclaimerView(){
        disclaimerView = (CardView) findViewById(R.id.disclaimerView);
        disclaimerText = (TextView) findViewById(R.id.disclaimerText);
        hideDisclaimerView();
    }

    private void showDisclaimerView(String disclaimerString){
        disclaimerView.setVisibility(View.VISIBLE);
        disclaimerText.setText(Html.fromHtml(disclaimerString));
        disclaimerText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void hideDisclaimerView(){
        disclaimerView.setVisibility(View.GONE);
    }

    private void setupBannerView() {
        bannerView = (CardView) findViewById(R.id.bannerView);
        banner = (ImageView) findViewById(R.id.banner);
        hideBannerView();
    }

    private void showBannerView(String bannerUrl){
        bannerView.setVisibility(View.VISIBLE);
        try {
            Glide.with(this).load(bannerUrl)
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(banner);
        }catch (NullPointerException e){
            banner.setImageResource(R.drawable.sidebar_header_background);
            hideBannerView();
        }
    }

    private void hideBannerView(){
        bannerView.setVisibility(View.GONE);
    }

    private void setupProduct_scrollView() {
        product_layout= (LinearLayout) findViewById(R.id.product_layout);
        error_Layout = (LinearLayout) findViewById(R.id.error_Layout);
        error_Layout.setOnClickListener(this);
        hideErrorLayout();
        removedView = (LinearLayout) findViewById(R.id.removedView);
        hideremovedView();
        removedText = (TextView) findViewById(R.id.removedText);
        progressBarLayout = (LinearLayout) findViewById(R.id.progressBarLayout);
        product_scrollView = (NestedScrollView) findViewById(R.id.product_scrollView);
        productImageView = (ImageView) findViewById(R.id.productImageView);
        productTitle = (TextView) findViewById(R.id.productTitle);
        productSubtitle = (TextView) findViewById(R.id.productSubtitle);
        setupMRPView();
        btn_addToCart = (Button) findViewById(R.id.btn_addToCart);
        btn_addToCart.setOnClickListener(this);
        btn_buyNow = (Button) findViewById(R.id.btn_buyNow);
        btn_buyNow.setOnClickListener(this);
        setupDetailsAndDescriptionView();
        setupComboItemsView();
    }

    private void setupComboItemsView() {
        comboItemsView = (HorizontalScrollView) findViewById(R.id.comboItemsView);
        comboItemsViewLayout = (LinearLayout) findViewById(R.id.comboItemsViewLayout);
        hidecomboItemsView();
    }

    private void hidecomboItemsView() {
        comboItemsView.setVisibility(View.GONE);
    }

    private void showremovedView(String removedString){
        removedView.setVisibility(View.VISIBLE);
        removedText.setText(Html.fromHtml(removedString));
        removedText.setMovementMethod(LinkMovementMethod.getInstance());
        hideErrorLayout();
        hideProgressLayout();
        hideProductlayout();

    }

    private void hideremovedView() {
        removedView.setVisibility(View.GONE);
    }

    private void showProductLayout(){
        product_layout.setVisibility(View.VISIBLE);
        hideProgressLayout();
        hideErrorLayout();
        hideremovedView();
    }

    private void hideProductlayout(){
        product_layout.setVisibility(View.GONE);
    }

    private void setupDetailsAndDescriptionView() {
        tab_title_details = (TextView) findViewById(R.id.tab_title_details);
        tab_title_details.setOnClickListener(this);
        tab_title_description = (TextView) findViewById(R.id.tab_title_description);
        tab_title_description.setOnClickListener(this);
        tab_indicator_details = (LinearLayout) findViewById(R.id.tab_indicator_details);
        tab_indicator_description= (LinearLayout) findViewById(R.id.tab_indicator_description);
        tab_content_details = (LinearLayout) findViewById(R.id.tab_content_details);
        tab_content_description = (LinearLayout) findViewById(R.id.tab_content_description);
        tab_content_text_details = (TextView) findViewById(R.id.tab_content_text_details);
        tab_content_text_description = (TextView) findViewById(R.id.tab_content_text_description);
        showDetails();
    }

    private void showDetails(){
        //show details title bold
        tab_title_details.setTypeface(null, Typeface.BOLD);
        tab_title_description.setTypeface(null, Typeface.NORMAL);

        //show details Indicator
        tab_indicator_details.setVisibility(View.VISIBLE);
        tab_indicator_description.setVisibility(View.INVISIBLE);

        //show details content
        tab_content_details.setVisibility(View.VISIBLE);
        tab_content_description.setVisibility(View.GONE);

    }

    private void showDescription(){
        //show details title bold
        tab_title_details.setTypeface(null, Typeface.NORMAL);
        tab_title_description.setTypeface(null, Typeface.BOLD);

        //show details Indicator
        tab_indicator_details.setVisibility(View.INVISIBLE);
        tab_indicator_description.setVisibility(View.VISIBLE);

        //show details content
        tab_content_details.setVisibility(View.GONE);
        tab_content_description.setVisibility(View.VISIBLE);
    }

    private void setDetailsText(String Details){
        //set details text
        tab_content_text_details.setText(Html.fromHtml(Details));
        tab_content_text_details. setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setDescriptionText(String Description){
        //set description text
        tab_content_text_description.setText(Html.fromHtml(Description));
        tab_content_text_details. setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupMRPView() {
        MRPView = (LinearLayout) findViewById(R.id.MRPView);
        MRP = (TextView) findViewById(R.id.MRP);
        hideMRPView();
    }

    private void showMRPView(String MRPtext){
        MRPView.setVisibility(View.VISIBLE);
        MRP.setText(getString(R.string.mrp)+getString(R.string.rupeesSymbol)+MRPtext+getString(R.string.priceEndSymbol));
        MRP.setPaintFlags(MRP.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    private void setPrice(String price){
        btn_buyNow.setText(getString(R.string.buyNow)+" "+getString(R.string.rupeesSymbol)+price+getString(R.string.priceEndSymbol));
    }

    private void setTitle(String title){
        productTitle.setText(title);
    }

    private void setSubTitle(String subTitle){
        productSubtitle.setText(subTitle);
    }

    private void setProductImageView (String type){
        if(type.equals(getString(R.string.bookType))){
            productImageView.setImageResource(R.drawable.ic_book_default);
            setImage(product.book.photofile, R.drawable.ic_book_error);

        }else if(type.equals(getString(R.string.instrumentType))){
            productImageView.setImageResource(R.drawable.ic_instrument_default);
            setImage(product.instrument.photofile, R.drawable.ic_instrument_error);

        }else if(type.equals(getString(R.string.comboType))){
            productImageView.setImageResource(R.drawable.ic_combo_default);
            setImage(product.combopack.photoUrl, R.drawable.ic_combo_error);

        }
    }

    public void setImage(String imageUrl, int errorResource){
        try{
            Glide.with(this).load(imageUrl)
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(productImageView);
        }catch (NullPointerException e){
            productImageView.setImageResource(errorResource);
        }
    }

    private void hideMRPView() {
        MRPView.setVisibility(View.GONE);
    }

    private void setupCoordinatorLayout() {
        activity_product_view = (CoordinatorLayout) findViewById(R.id.activity_product_view);
    }

    private Bundle getExtrasFromIntent() {
        Intent productView = getIntent();
        Bundle b = productView.getExtras();
        return b;
    }

    private void setupActionbar(String type) {
        String title = getTitle(type);
        activityToolbar = (Toolbar) findViewById(R.id.action_bar_product_view);
        setSupportActionBar(activityToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(title);
    }

    private void showActionbar(){
        activityToolbar.setVisibility(View.VISIBLE);
    }

    private void hideActionbar(){
        activityToolbar.setVisibility(View.GONE);
    }

    private String getTitle(String type) {
        String title="";
        if(type.equals(getString(R.string.bookType))){
            title = getString(R.string.book);
        }else if(type.equals(getString(R.string.instrumentType))){
            title = getString(R.string.instrument);
        }else if (type.equals(getString(R.string.comboType))){
            title = getString(R.string.combopack);
        }
        return title;
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_addToCart:
                //method call for updating cart

                break;
            case R.id.btn_buyNow:
                //method call for buy checkout

                break;
            case R.id.tab_title_details:
                showDetails();
                break;
            case R.id.tab_title_description:
                showDescription();
                break;
            case R.id.error_Layout:

                break;

        }
        
    }

    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        NETWORK_STATE = isConnected;
//        showSnack(isConnected);

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
    private void showSnack(String snackString) {
        String message;
        message = snackString;
        Snackbar snackbar = Snackbar.make(activity_product_view, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }


}
