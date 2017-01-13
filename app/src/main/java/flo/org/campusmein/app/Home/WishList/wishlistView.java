package flo.org.campusmein.app.Home.WishList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.util.HashMap;
import java.util.Map;

import flo.org.campusmein.R;
import flo.org.campusmein.app.Home.cart.cartView;
import flo.org.campusmein.app.utils.ConnectivityReceiver;
import flo.org.campusmein.app.utils.Products;
import flo.org.campusmein.app.utils.RealmUtils.RealmController;
import flo.org.campusmein.app.utils.campusExchangeApp;
import flo.org.campusmein.app.utils.cartViewUtils.BadgeDrawable;
import flo.org.campusmein.app.utils.wishListObject;
import io.realm.RealmResults;

public class wishlistView extends AppCompatActivity implements View.OnClickListener,ConnectivityReceiver.ConnectivityReceiverListener{

    private static final String TAG = wishlistView.class.getSimpleName();
    private static final String PRODUCT_OBJECT_ID = "objectId";

    private static final String ___CLASS = "products";
    private static final String LOAD_RELATIONS ="loadRelations=book%2Csubject%2Ccollege%2Cspecialization%2Ccombopack%2Ccombopack.books%2Ccombopack.instruments%2Cinstrument";
    private static final String SLASH = "/";
    private static final String QUERY = "?";
    
    private boolean NETWORK_STATE = false;
    private LayerDrawable mCartMenuIcon;
    private CardView emptyLayout;
    private CardView error_Layout;
    private LinearLayout wishesListLayout;
    private CoordinatorLayout activity_wishes_listing;
    private CardView[] wishlist_object_views;
    private ProgressDialog mProgressDialog;
    private int totalItemsInt;

    private TextView wishlist_itemTitle,wishlist_itemSubTitle,wishlist_price,wishlist_mrPrice,wishlist_discount;
    private ImageView wishlist_itemImage,wishlist_remove;
    private Button removeAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist_view);
        checkConnection();
        setupActionbar();
        setupCoordinatorLayout();
        setupEmptyLayout();
        setupErrorLayout();
        setupWishesListLayout();
        setupRemoveAllBtn();
    }

    private void setupRemoveAllBtn() {
        removeAll = (Button) findViewById(R.id.removeAll);
        removeAll.setOnClickListener(this);
        hideRemoveAllBtn();
    }

    private void hideRemoveAllBtn() {
        removeAll.setVisibility(View.GONE);
    }

    private void showRemoveAllBtn() {
        removeAll.setVisibility(View.VISIBLE);
    }

    private void setupErrorLayout() {
        error_Layout = (CardView) findViewById(R.id.error_Layout);
        hideErrorLayout();
    }

    private void hideErrorLayout() {
        error_Layout.setVisibility(View.GONE);
    }

    private void showErrorLayout() {
        error_Layout.setVisibility(View.VISIBLE);
        hideWishesLayout();
        hideEmptyLayout();
        hideRemoveAllBtn();
    }

    private void loadWishList() {
        setActionBarTitle(getString(R.string.myWishlist));
        if(NETWORK_STATE){

            totalItemsInt = 0;
            if(wishesListLayout.getChildCount()>0){
                wishesListLayout.removeAllViews();
            }
            RealmResults<wishListObject> listOfWishedItems = RealmController.getInstance().getAllWishedItems();
            if(listOfWishedItems.size() > 0){
                showishesListLayout();
                int wishListSize = listOfWishedItems.size();
                wishlist_object_views = new CardView[wishListSize];
                fetchProductsArray(wishListSize,listOfWishedItems);
            }else {
                showEmptyLayout();
            }
        }else {
            showErrorLayout();
            showSnack(getString(R.string.NetworkFaliure));
        }
    }

    private void fetchProductsArray(int wishListSize, RealmResults<wishListObject> listOfWishedItems) {
        for (int i = 0; i < wishListSize; i++){
            String productObjectId = listOfWishedItems.get(i).getProductId();
            fetchProduct(productObjectId,listOfWishedItems, i);
        }

    }

    private void fetchProduct(String productObjectId, final RealmResults<wishListObject> listOfWishedItems, final int position) {
        showProgressDialog(getString(R.string.loadingCart));
//        http://api.backendless.com/test/data/products/E0686264-7306-2371-FFBC-5826F3B11F00?
        String productRequest = getString(R.string.baseBackendUrl);
//        productRequest = productRequest+getString(R.string.products);
        productRequest = productRequest+___CLASS+SLASH+productObjectId+QUERY+LOAD_RELATIONS;


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
                        Products product = gson.fromJson(responseData,Products.class);
//                        productsInCart.add(product);
                        addItemsViews(listOfWishedItems,product,position);
                        hideProgressDialog();
//                        setDetailsText(response.toString());
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("volley error : ",error.toString());
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

    private void addItemsViews(RealmResults<wishListObject> listOfWishedItems, final Products product, int position) {

        View ItemView = getLayoutInflater().inflate(R.layout.wishlist_object_view,wishlist_object_views[position],false);
        wishlist_object_views [position]= (CardView) ItemView.findViewById(R.id.cart_object_card);
        setupCard(ItemView);
        final String productId = listOfWishedItems.get(position).getProductId();
        showProductProperties(product);

        addToTotalItems();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        int pixelsLR = dpTopixels(6);
        int pixelsTB = dpTopixels(6);
        params.setMargins(pixelsLR,pixelsTB,pixelsLR,pixelsTB);

        wishesListLayout.addView(ItemView,params);

        wishlist_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NETWORK_STATE){
                    RealmController.getInstance().removewishListItem(productId);
                    invalidateOptionsMenu();
                    loadWishList();
                    Toast.makeText(getApplicationContext(), R.string.removedFromWishlist,Toast.LENGTH_SHORT).show();
                }else {
                    showSnack(getString(R.string.NetworkFaliure));
                }

            }
        });
        ItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProduct(product);
            }
        });

    }

    private void showProductProperties(Products product) {
        if(product.removed){
            showRemovedView(product);
        }else {
            showItem(product);
        }
    }

    private void showItem(Products product) {
        String type = product.type;
        if(type.equals(getString(R.string.bookType))){
            setBookProperties(product);
        }else if(type.equals(getString(R.string.instrumentType))){
            setInstrumentProperties(product);
        }else if(type.equals(getString(R.string.comboType))){
            setCombopackProperties(product);
        }else {
            removedlayout("Item Not Found", "Maybe it was removed");
        }
    }

    private void openProduct(Products product) {
        Intent productView = new Intent(this, flo.org.campusmein.app.Home.ProductView.productView.class);
        productView.putExtra(PRODUCT_OBJECT_ID, product.type);
        productView.putExtra(PRODUCT_OBJECT_ID, product.objectId);
        startActivity(productView);
    }

    private float discount(int mrp, int listPrice) {
        Log.d("mrp and list", mrp+" "+listPrice);
        if(mrp != 0){
            int diff = mrp - listPrice;
            Log.d("diff", diff+"");
            float discount = (diff *100)/ mrp;
            Log.d("discount", discount+"");
            return discount;
        }else return 0;
    }

    private void setBookProperties(Products product) {
        wishlist_itemTitle.setText(product.book.title);
        wishlist_itemSubTitle.setText(product.book.author);
        wishlist_price.setText(getString(R.string.rupeesSymbol)+(product.listPrice)+getString(R.string.priceEndSymbol));
        wishlist_mrPrice.setText(getString(R.string.rupeesSymbol)+(product.mrp)+getString(R.string.priceEndSymbol));
        wishlist_mrPrice.setPaintFlags(wishlist_mrPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        wishlist_discount.setText(discount(product.mrp, product.listPrice)+getString(R.string.percentageSymbol));
        wishlist_itemImage.setImageResource(R.drawable.ic_book_default);
        try {
            Glide.with(this).load(product.book.photofile).placeholder(R.drawable.ic_book_default).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.ALL).into(wishlist_itemImage);
        }catch (NullPointerException e){
            wishlist_itemImage.setImageResource(R.drawable.ic_book_error);
        }

    }

    private void setInstrumentProperties(Products product) {
        wishlist_itemTitle.setText(product.instrument.instrumentName);
        wishlist_itemSubTitle.setText(product.instrument.instrumentSubtitle);
        wishlist_price.setText(getString(R.string.rupeesSymbol)+(product.listPrice)+getString(R.string.priceEndSymbol));
        wishlist_mrPrice.setText(getString(R.string.rupeesSymbol)+(product.mrp)+getString(R.string.priceEndSymbol));
        wishlist_mrPrice.setPaintFlags(wishlist_mrPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        wishlist_discount.setText(discount(product.mrp, product.listPrice)+getString(R.string.percentageSymbol));
        wishlist_itemImage.setImageResource(R.drawable.ic_instrument_default);

        try {
            Glide.with(this).load(product.instrument.photoFile).placeholder(R.drawable.ic_instrument_default).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.ALL).into(wishlist_itemImage);
        }catch (NullPointerException e){
            wishlist_itemImage.setImageResource(R.drawable.ic_instrument_error);
        }

    }

    private void setCombopackProperties(Products product) {
        wishlist_itemTitle.setText(product.combopack.title);
        wishlist_itemSubTitle.setText(product.combopack.subTitle);
        wishlist_price.setText(getString(R.string.rupeesSymbol)+(product.listPrice)+getString(R.string.priceEndSymbol));
        wishlist_mrPrice.setText(getString(R.string.rupeesSymbol)+(product.mrp)+getString(R.string.priceEndSymbol));
        wishlist_mrPrice.setPaintFlags(wishlist_mrPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        wishlist_discount.setText(discount(product.mrp, product.listPrice)+getString(R.string.percentageSymbol));
        wishlist_itemImage.setImageResource(R.drawable.ic_combo_default);

        try {
            Glide.with(this).load(product.combopack.photoUrl).placeholder(R.drawable.ic_combo_default).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.ALL).into(wishlist_itemImage);
        }catch (NullPointerException e){
            wishlist_itemImage.setImageResource(R.drawable.ic_combo_error);
        }

    }

    private void showRemovedView(Products product) {
        String type = product.type;
        if(type.equals(getString(R.string.bookType))){
            removedlayout(product.book.title, product.reasonRemoved);
        }else if(type.equals(getString(R.string.instrumentType))){
            removedlayout(product.book.title, product.reasonRemoved);
        }else if(type.equals(getString(R.string.comboType))){
            removedlayout(product.book.title, product.reasonRemoved);
        }else {
            removedlayout("Item Not Found", "Maybe it was removed");
        }
    }

    private void removedlayout(String Title,String reasonRemoved){
        wishlist_itemTitle.setText(Title);
        wishlist_itemSubTitle.setText(R.string.removedText);
        wishlist_price.setText(reasonRemoved);
        wishlist_mrPrice.setVisibility(View.GONE);
        wishlist_discount.setVisibility(View.GONE);
        wishlist_itemImage.setVisibility(View.GONE);
    }

    private void setupCard(View itemView) {
        wishlist_itemTitle = (TextView) itemView.findViewById(R.id.wishlist_itemTitle);
        wishlist_itemSubTitle = (TextView) itemView.findViewById(R.id.wishlist_itemSubTitle);
        wishlist_itemImage = (ImageView) itemView.findViewById(R.id.wishlist_itemImage);
        wishlist_price = (TextView) itemView.findViewById(R.id.wishlist_price);
        wishlist_mrPrice = (TextView) itemView.findViewById(R.id.wishlist_mrPrice);
        wishlist_remove = (ImageView) itemView.findViewById(R.id.wishlist_remove_button);
        wishlist_discount = (TextView) itemView.findViewById(R.id.wishlist_discount);
    }

    private void addToTotalItems() {
        totalItemsInt = totalItemsInt +1;
        setActionBarTitle(getString(R.string.myWishlist)+" ("+totalItemsInt+")");
    }

    public int dpTopixels(int dps){
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dps,
                getResources().getDisplayMetrics());
    }

    private void setupCoordinatorLayout() {
        activity_wishes_listing = (CoordinatorLayout) findViewById(R.id.activity_wishes_listing);
    }

    private void setupWishesListLayout() {
        wishesListLayout = (LinearLayout) findViewById(R.id.wishesListLayout);
        hideWishesLayout();
    }

    private void hideWishesLayout() {
        wishesListLayout.setVisibility(View.GONE);
    }

    private void showishesListLayout(){
        wishesListLayout.setVisibility(View.VISIBLE);
        hideEmptyLayout();
        hideErrorLayout();
        showRemoveAllBtn();
    }

    private void setupEmptyLayout() {
        emptyLayout = (CardView) findViewById(R.id.emptyLayout);
        hideEmptyLayout();

    }

    private void hideEmptyLayout() {
        emptyLayout.setVisibility(View.GONE);
    }

    private void showEmptyLayout() {
        emptyLayout.setVisibility(View.VISIBLE);
        hideWishesLayout();
        hideErrorLayout();
        hideRemoveAllBtn();
    }

    private void setupActionbar() {
        Toolbar activityToolbar = (Toolbar) findViewById(R.id.action_bar_wishlist_view);
        setSupportActionBar(activityToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    private void setActionBarTitle(String title) {
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        campusExchangeApp.getInstance().setConnectivityListener(this);
        invalidateOptionsMenu();
        loadWishList();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.removeAll:
                RealmController.getInstance().clearWishlist();
                loadWishList();
                showSnack("Wish list cleared");
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
        Snackbar snackbar = Snackbar.make(activity_wishes_listing, message, Snackbar.LENGTH_LONG);
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
        if(mProgressDialog != null){
            mProgressDialog.dismiss();
        }

    }
}
