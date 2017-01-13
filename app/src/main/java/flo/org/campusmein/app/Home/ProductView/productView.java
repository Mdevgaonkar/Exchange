package flo.org.campusmein.app.Home.ProductView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import flo.org.campusmein.R;
import flo.org.campusmein.app.Home.cart.cartView;
import flo.org.campusmein.app.Home.orderPlacement.priceView;
import flo.org.campusmein.app.utils.College;
import flo.org.campusmein.app.utils.Books;
import flo.org.campusmein.app.utils.ConnectivityReceiver;
import flo.org.campusmein.app.utils.Instruments;
import flo.org.campusmein.app.utils.Products;
import flo.org.campusmein.app.utils.RealmUtils.RealmController;
import flo.org.campusmein.app.utils.Subjects;
import flo.org.campusmein.app.utils.campusExchangeApp;
import flo.org.campusmein.app.utils.cartObject;
import flo.org.campusmein.app.utils.cartViewUtils.BadgeDrawable;


public class productView extends AppCompatActivity
        implements View.OnClickListener,ConnectivityReceiver.ConnectivityReceiverListener{

//    http://api.backendless.com/test/data/products?
    private static final String ___CLASS = "products";
    private static final String LOAD_RELATIONS ="loadRelations=book%2Csubject%2Ccollege%2Cspecialization%2Ccombopack%2Ccombopack.books%2Ccombopack.instruments%2Cinstrument";
    private static final String SLASH = "/";
    private static final String QUERY = "?";
    private static final String TAG = productView.class.getSimpleName();
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
    private Button btn_buyNow;
    private LinearLayout error_Layout,progressBarLayout,removedView;
    private LinearLayout product_layout;

    private TextView tab_title_details,tab_title_description;
    private LinearLayout tab_indicator_details,tab_indicator_description;
    private LinearLayout tab_content_details,tab_content_description;
    private TextView tab_content_text_details,tab_content_text_description;


    private static final String PRODUCT_OBJECT_ID = "objectId";
    private String product_objectId;


    private Products product;

    private static final String SRC_STR_KEY = "source";
    private static final String PDCT_ID_KEY = "productId";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_view);
        Bundle b = getExtrasFromIntent();
//        String type = b.getString(PRODUCT_TYPE);

        product_objectId = b.getString(PRODUCT_OBJECT_ID);
//        String bic_objectId = b.getString(BIC_OBJECT_ID);

        setupActionbar(getString(R.string.emptyString));
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

        campusExchangeApp.getInstance().addToRequestQueue(getProduct,TAG);
    }

    private void updateUI() {
//        method calls to show proper fields
        showProductLayout();
        setGeneralProperties();
        String productType = product.type;
        setActionBarTitle(productType);
        if(productType.equals(getString(R.string.bookType))){
            setBookProperties();
        }else if(productType.equals(getString(R.string.instrumentType))){
            setInstrumentProperties();
        }else if(productType.equals(getString(R.string.comboType))){
            setCombopackProperties();
        }else {
            showErrorLayout();
        }
        invalidateOptionsMenu();
    }

    private void setActionBarTitle(String type) {
        String Title = getTitle(type);
        getSupportActionBar().setTitle(Title);
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
        Details = Details + greyFont + getString(R.string.category) + endGreyFont+ product.instrument.type +breakTag+breakTag;
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
            CardView[] itemBookView = new CardView[number_of_books];
            for (int i = 0; i < number_of_books; i++){
                View comboBookView = getLayoutInflater().inflate(R.layout.combo_items_view, itemBookView[i],false);
                itemBookView[i]= (CardView) comboBookView.findViewById(R.id.comboItemsViewCard);
                TextView tv = (TextView) comboBookView.findViewById(R.id.itemDescription);
                tv.setText(Html.fromHtml(getComboBookDetails(i)));
                tv.setMovementMethod(LinkMovementMethod.getInstance());
                ImageView iv = (ImageView) comboBookView.findViewById(R.id.itemImage);
                iv.setImageResource(R.drawable.ic_combo_default);
                try{
                    Glide.with(this)
                            .load(books.get(i).photofile)
                            .placeholder(R.drawable.ic_book_default)
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
                params.setMargins(dpTopixels(8),0,0,dpTopixels(4));

                comboItemsViewLayout.addView(comboBookView,params);

            }

            comboItemsView.setVisibility(View.VISIBLE);

        }
        if(instruments.size()>0){

            int number_of_instruments= instruments.size();
            CardView[] itemInstrumentView = new CardView[number_of_instruments];
            for (int i = 0; i < number_of_instruments; i++){
                View comboInstrumentView = getLayoutInflater().inflate(R.layout.combo_items_view, itemInstrumentView[i],false);
                itemInstrumentView[i]= (CardView) comboInstrumentView.findViewById(R.id.comboItemsViewCard);
                TextView tv = (TextView) comboInstrumentView.findViewById(R.id.itemDescription);
                tv.setText(Html.fromHtml(getComboInstrumentDetails(i)));
                tv.setMovementMethod(LinkMovementMethod.getInstance());
                ImageView iv = (ImageView) comboInstrumentView.findViewById(R.id.itemImage);
                iv.setImageResource(R.drawable.ic_combo_default);
                try{
                    Glide.with(this)
                            .load(instruments.get(i).photoFile)
                            .thumbnail(0.5f)
                            .placeholder(R.drawable.ic_instrument_default)
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

        Details = Details + product.combopack.instruments.get(position).description+breakTag+breakTag; //description

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
                    .placeholder(R.color.colorDarkGray)
                    .error(R.drawable.sidebar_header_background)
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
        Button btn_addToCart = (Button) findViewById(R.id.btn_addToCart);
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
            setImage(product.book.photofile, R.drawable.ic_book_error,R.drawable.ic_book_default);

        }else if(type.equals(getString(R.string.instrumentType))){
            productImageView.setImageResource(R.drawable.ic_instrument_default);
            setImage(product.instrument.photoFile, R.drawable.ic_instrument_error,R.drawable.ic_instrument_default);

        }else if(type.equals(getString(R.string.comboType))){
            productImageView.setImageResource(R.drawable.ic_combo_default);
            setImage(product.combopack.photoUrl, R.drawable.ic_combo_error,R.drawable.ic_combo_default);

        }
    }

    public void setImage(String imageUrl, int errorResource, int placeholderRecource){
        try{
            Glide.with(this).load(imageUrl)
                    .thumbnail(0.5f)
                    .placeholder(placeholderRecource)
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.product_view_menu, menu);
        LayerDrawable mCartMenuIcon = (LayerDrawable) menu.findItem(R.id.action_cart).getIcon();
        int cartSize = RealmController.getInstance().getItems().size();
        setBadgeCount(this, mCartMenuIcon, String.valueOf(cartSize));

        Drawable drawable = menu.getItem(0).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.colorCard), PorterDuff.Mode.SRC_ATOP);
            }
        if(product != null){
            setColorWishlistIcon(menu,product);
        }


        Drawable drawablecart = menu.getItem(1).getIcon();
        if(drawablecart != null) {
            drawablecart.mutate();
            drawablecart.setColorFilter(getResources().getColor(R.color.colorCard), PorterDuff.Mode.SRC_ATOP);
        }



        return true;
    }

    private void setColorWishlistIcon(Menu menu, Products product){
        if(RealmController.getInstance().isItemInWishlist(product.objectId)) {
            menu.getItem(0).setTitle("Remove From Wishlist");
            menu.getItem(0).setIcon(R.drawable.ic_favorite_black_24dp);
        }else {
            menu.getItem(0).setIcon(R.drawable.ic_favorite_border_black_);
        }
        Drawable drawable = menu.getItem(0).getIcon();
        if(drawable != null) {
            drawable.mutate();
            drawable.setColorFilter(getResources().getColor(R.color.colorCard), PorterDuff.Mode.SRC_ATOP);
        }
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

    public void openCart() {
        Intent cart = new Intent(this,cartView.class);
        startActivity(cart);
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
            case R.id.action_add_wishlist:
                addOrRemoveWish(product);
        }
        return super.onOptionsItemSelected(item);
    }

    private void addOrRemoveWish(Products product) {
        if(RealmController.getInstance().isItemInWishlist(product.objectId)) {
            RealmController.getInstance().removewishListItem(product.objectId);
            Toast.makeText(this, R.string.removedFromWishlist,Toast.LENGTH_SHORT).show();

        }else {
            RealmController.getInstance().addToWishList(product.objectId);
            Toast.makeText(this, R.string.addedToWishlist,Toast.LENGTH_SHORT).show();

//                            ActivityCompat.invalidateOptionsMenu();
        }
        invalidateOptionsMenu();
    }

//    private void addToWishList(Products product) {
//        if(RealmController.getInstance().isItemInWishlist(product.objectId)) {
//            Toast.makeText(this, R.string.alreadyInWishlist,Toast.LENGTH_SHORT).show();
//        }else {
//            RealmController.getInstance().addToWishList(product.objectId);
//            Toast.makeText(this, R.string.addedToWishlist,Toast.LENGTH_SHORT).show();
////                            ActivityCompat.invalidateOptionsMenu();
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        campusExchangeApp.getInstance().setConnectivityListener(this);
        invalidateOptionsMenu();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_addToCart:
                //method call for updating cart
                addToCart(product.objectId);
                break;
            case R.id.btn_buyNow:
                //method call for buy checkout
                buyItem();
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

    private void buyItem() {
        Intent buyThisItem = new Intent(this, priceView.class);
        buyThisItem.putExtra(SRC_STR_KEY,"product");
        buyThisItem.putExtra(PDCT_ID_KEY,product_objectId);
        startActivity(buyThisItem);
    }

    private void addToCart(String s) {
        if(isItemInCart(s)) {
            Toast.makeText(this,getString(R.string.alreadyInCart),Toast.LENGTH_LONG).show();
        }else {
            cartObject item = new cartObject();
            item.setId((int) (RealmController.getInstance().getItems().size()+ System.currentTimeMillis()));
            item.setProductId(s);
            item.setQuantity(1);
            Toast.makeText(this,getString(R.string.addedToCart),Toast.LENGTH_LONG).show();
            campusExchangeApp.getInstance().getRealm().beginTransaction();
            campusExchangeApp.getInstance().getRealm().copyToRealm(item);
            campusExchangeApp.getInstance().getRealm().commitTransaction();
            invalidateOptionsMenu();
        }
    }

    private boolean isItemInCart(String s) {
        if (RealmController.getInstance().hasItems()){
            if(RealmController.getInstance().getCartObjectWithProductId(s) == null){
                return false;
            }else {
                return true;
            }
        }else {
            return false;
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

    @Override
    public void onBackPressed() {
        if(NETWORK_STATE){
            campusExchangeApp.getInstance().getmRequestQueue().cancelAll(TAG);
        }
        finish();
        super.onBackPressed();

    }


}
