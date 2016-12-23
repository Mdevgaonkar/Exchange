package flo.org.exchange.app.Home.cart;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
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

import flo.org.exchange.R;
import flo.org.exchange.app.utils.ConnectivityReceiver;
import flo.org.exchange.app.utils.Products;
import flo.org.exchange.app.utils.RealmUtils.RealmController;
import flo.org.exchange.app.utils.campusExchangeApp;
import flo.org.exchange.app.utils.cartObject;
import io.realm.RealmResults;

/**
 * Created by Mayur on 22/12/16.
 */

public class cartView extends AppCompatActivity implements View.OnClickListener,ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = cartView.class.getSimpleName();
    private static final String PRODUCT_OBJECT_ID = "objectId";

    private static final String ___CLASS = "products";
    private static final String LOAD_RELATIONS ="loadRelations=book%2Csubject%2Ccollege%2Cspecialization%2Ccombopack%2Ccombopack.books%2Ccombopack.instruments%2Cinstrument";
    private static final String SLASH = "/";
    private static final String QUERY = "?";
    private boolean NETWORK_STATE = false;

    private LinearLayout cartListLayout;
    private ProgressDialog mProgressDialog;
    private CardView[] cart_object_views;
    private LinearLayout plus,minus;
    private TextView itemTitle,itemSubTitle,price,mrPrice,discount,quantity,moveToWishList,remove;
    private ImageView cart_itemImage;

    LinearLayout priceView;
    TextView totalPrice,totalItems;
    Button continueChkOut;

    private CoordinatorLayout activity_cart_listing;
    private LinearLayout emptyLayout;

    private float totalPriceFloat;
    private int totalItemsInt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_view);
        checkConnection();
        setupActionbar(getString(R.string.myCart));
        setupCoordinatorLayout();
        setupLayout();
        setupEmptyLayout();
        setupPriceView();

        loadCart();


    }

    private void setupPriceView() {
        priceView = (LinearLayout) findViewById(R.id.priceView);
        totalPrice = (TextView) findViewById(R.id.totalPrice);
        totalItems = (TextView) findViewById(R.id.totalItems);
        continueChkOut = (Button) findViewById(R.id.continueChkOut);
        continueChkOut.setOnClickListener(this);
        hidePriceView();
    }

    private void hidePriceView() {
        priceView.setVisibility(View.GONE);
    }

    private void showPriceView() {
        priceView.setVisibility(View.VISIBLE);
    }

    private void setupEmptyLayout() {
        emptyLayout = (LinearLayout) findViewById(R.id.emptyLayout);
        Button shopNow = (Button) findViewById(R.id.shopNow);
        shopNow.setOnClickListener(this);
        hideEmptyLayout();
    }

    private void hideEmptyLayout() {
        emptyLayout.setVisibility(View.GONE);
    }

    private void showEmptyLayout() {
        emptyLayout.setVisibility(View.VISIBLE);
        hideCartListing();
        hidePriceView();
    }

    private void hideCartListing() {
        cartListLayout.setVisibility(View.GONE);
    }

    private void showCartListLayout() {
        cartListLayout.setVisibility(View.VISIBLE);
        Log.d("showCartListLayout", "called");
        hideEmptyLayout();
        showPriceView();
    }

    private void setupCoordinatorLayout() {
        activity_cart_listing = (CoordinatorLayout) findViewById(R.id.activity_cart_listing);
    }

    private void setupActionbar(String title) {
        Toolbar activityToolbar = (Toolbar) findViewById(R.id.action_bar_cart_listings);
        setSupportActionBar(activityToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(title);
    }

    private void loadCart() {
        if(NETWORK_STATE){
            totalPriceFloat = 0;
            totalItemsInt = 0;
            if(cartListLayout.getChildCount()>0){
                cartListLayout.removeAllViews();
            }
            RealmResults<cartObject> listofCartItems = RealmController.getInstance().getItems();
            if(listofCartItems.size() > 0){
                showCartListLayout();
                int cartSize = listofCartItems.size();
                cart_object_views = new CardView[cartSize];
                fetchProductsArray(cartSize,listofCartItems);
            }else {
                showEmptyLayout();
            }
        }else showSnack(getString(R.string.NetworkFaliure));

    }

    private void addItemsViews(RealmResults<cartObject> listofCarts, Products product, final int position) {
            View cartItemView = getLayoutInflater().inflate(R.layout.cart_object_view,cart_object_views[position],false);
            cart_object_views [position]= (CardView) cartItemView.findViewById(R.id.cart_object_card);
            setupCard(cartItemView);
            int quantity = listofCarts.get(position).getQuantity();
            final String productId = listofCarts.get(position).getProductId();
            showProductProperties(product,quantity);
            addToTotal(product.listPrice*quantity);
            addToTotalItems();

            setupQuantityUpdaters(product);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            int pixelsLR = dpTopixels(6);
            int pixelsTB = dpTopixels(6);
            params.setMargins(pixelsLR,pixelsTB,pixelsLR,pixelsTB);

            cartListLayout.addView(cartItemView,params);

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NETWORK_STATE){
                    RealmController.getInstance().removeCartItem(productId);
                    loadCart();
                    Toast.makeText(getApplicationContext(), R.string.removedFromCart,Toast.LENGTH_SHORT).show();
                }else {
                    showSnack(getString(R.string.NetworkFaliure));
                }

            }
        });

        moveToWishList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NETWORK_STATE){
                    RealmController.getInstance().addToWishList(productId);
                    RealmController.getInstance().removeCartItem(productId);
                    Toast.makeText(getApplicationContext(), R.string.movedToWishlist,Toast.LENGTH_SHORT).show();
                    loadCart();
                }else {
                    showSnack(getString(R.string.NetworkFaliure));
                }

            }
        });

    }

    private void addToTotalItems() {
        totalItemsInt = totalItemsInt +1;
        totalItems.setText(getString(R.string.totalItemsText)+totalItemsInt);
    }

    public void addToTotal(int priceToBeAdded){
        totalPriceFloat = totalPriceFloat + priceToBeAdded;
        totalPrice.setText(getString(R.string.rupeesSymbol)+totalPriceFloat+getString(R.string.priceEndSymbol));
    }

    public void subtractFromTotal(int priceToBeSubtracted){
        totalPriceFloat = totalPriceFloat - priceToBeSubtracted;
        totalPrice.setText(getString(R.string.rupeesSymbol)+totalPriceFloat+getString(R.string.priceEndSymbol));

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

    private void setupQuantityUpdaters(final Products product) {
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantityInt = RealmController.getInstance().getQuantityWithProductId(product.objectId);
                setQuantity(quantityInt+1,product, v);
                if (quantityInt+1>0 && quantityInt+1<=5) {
                    addToTotal(product.listPrice);
                }
            }
        });
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantityInt = RealmController.getInstance().getQuantityWithProductId(product.objectId);
                setQuantity(quantityInt-1,product, v);
                if (quantityInt-1>0 && quantityInt-1<=5) {
                    subtractFromTotal(product.listPrice);
                }
            }
        });
    }

    private void setQuantity(int i, Products product, View v) {
        if(i>Integer.valueOf(getString(R.string.maximumQuantityLimit))){
            Toast.makeText(this, R.string.maxItemLimitReached,Toast.LENGTH_SHORT).show();
        }
        else if(i>0) {
            RealmController.getInstance().updateCartItemQuantity(product.objectId,i);
            LinearLayout lp = (LinearLayout) v.getParent();
            LinearLayout lpp = (LinearLayout) lp.getParent();
            TextView quantity = (TextView) lp.findViewById(R.id.quantity);
            TextView price = (TextView) lpp.findViewById(R.id.price);
            TextView mrp = (TextView) lpp.findViewById(R.id.mrPrice);
            price.setText(getString(R.string.rupeesSymbol)+(product.listPrice*i)+getString(R.string.priceEndSymbol));
            mrp.setText(getString(R.string.rupeesSymbol)+(product.mrp*i)+getString(R.string.priceEndSymbol));
            mrp.setPaintFlags(mrPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            quantity.setText(i+"");


        }else {
            RealmController.getInstance().setDefaultCartItemQuantity(product.objectId);
            LinearLayout lp = (LinearLayout) v.getParent();
            LinearLayout lpp = (LinearLayout) lp.getParent();
            TextView quantity = (TextView) lp.findViewById(R.id.quantity);
            TextView price = (TextView) lpp.findViewById(R.id.price);
            TextView mrp = (TextView) lpp.findViewById(R.id.mrPrice);
            price.setText(getString(R.string.rupeesSymbol)+(product.listPrice)+getString(R.string.priceEndSymbol));
            mrp.setText(getString(R.string.rupeesSymbol)+(product.mrp)+getString(R.string.priceEndSymbol));
            mrp.setPaintFlags(mrPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            quantity.setText(1+"");

        }
    }

    public int dpTopixels(int dps){
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dps,
                getResources().getDisplayMetrics());
    }

    private void showProductProperties(Products product, int quantity) {
        if(product.removed){
            showRemovedView(product);
        }else {
            showItem(product,quantity);
        }
    }

    private void showItem(Products product, int quantity) {
        String type = product.type;
        if(type.equals(getString(R.string.bookType))){
            setBookProperties(product,quantity);
        }else if(type.equals(getString(R.string.instrumentType))){
            setInstrumentProperties(product,quantity);
        }else if(type.equals(getString(R.string.comboType))){
            setCombopackProperties(product,quantity);
        }else {
            removedlayout("Item Not Found", "Maybe it was removed");
        }
        setOnCickListners(product);
    }

    private void setOnCickListners(final Products product) {
        itemTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProduct(product);
            }
        });
        itemSubTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProduct(product);
            }
        });
        cart_itemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProduct(product);
            }
        });


    }

    private void openProduct(Products product) {
        Intent productView = new Intent(this, flo.org.exchange.app.Home.ProductView.productView.class);
        productView.putExtra(PRODUCT_OBJECT_ID, product.type);
        productView.putExtra(PRODUCT_OBJECT_ID, product.objectId);
        startActivity(productView);
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
        itemTitle.setText(Title);
        itemSubTitle.setText(R.string.removedText);
        price.setText(reasonRemoved);
        mrPrice.setVisibility(View.GONE);
        quantity.setVisibility(View.GONE);
        plus.setVisibility(View.GONE);
        minus.setVisibility(View.GONE);
        moveToWishList.setVisibility(View.GONE);
        discount.setVisibility(View.GONE);
    }

    private void setCombopackProperties(Products product, int quantityInt) {
        itemTitle.setText(product.combopack.title);
        itemSubTitle.setText(product.combopack.subTitle);
        price.setText(getString(R.string.rupeesSymbol)+(product.listPrice*quantityInt)+getString(R.string.priceEndSymbol));
        mrPrice.setText(getString(R.string.rupeesSymbol)+(product.mrp*quantityInt)+getString(R.string.priceEndSymbol));
        mrPrice.setPaintFlags(mrPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        discount.setText(discount(product.mrp, product.listPrice)+getString(R.string.percentageSymbol));
        cart_itemImage.setImageResource(R.drawable.ic_combo_default);
        quantity.setText(""+quantityInt);
        try {
            Glide.with(this).load(product.combopack.photoUrl).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.ALL).into(cart_itemImage);
        }catch (NullPointerException e){
            cart_itemImage.setImageResource(R.drawable.ic_combo_error);
        }

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

    private void setInstrumentProperties(Products product, int quantityInt) {
        itemTitle.setText(product.instrument.instrumentName);
        itemSubTitle.setText(product.instrument.instrumentSubtitle);
        price.setText(getString(R.string.rupeesSymbol)+(product.listPrice*quantityInt)+getString(R.string.priceEndSymbol));
        mrPrice.setText(getString(R.string.rupeesSymbol)+(product.mrp*quantityInt)+getString(R.string.priceEndSymbol));
        mrPrice.setPaintFlags(mrPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        discount.setText(discount(product.mrp, product.listPrice)+getString(R.string.percentageSymbol));
        cart_itemImage.setImageResource(R.drawable.ic_instrument_default);
        quantity.setText(""+quantityInt);
        try {
            Glide.with(this).load(product.instrument.photoFile).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.ALL).into(cart_itemImage);
        }catch (NullPointerException e){
            cart_itemImage.setImageResource(R.drawable.ic_instrument_error);
        }

    }

    private void setBookProperties(Products product, int quantityInt) {
        itemTitle.setText(product.book.title);
        itemSubTitle.setText(product.book.author);
        price.setText(getString(R.string.rupeesSymbol)+(product.listPrice*quantityInt)+getString(R.string.priceEndSymbol));
        mrPrice.setText(getString(R.string.rupeesSymbol)+(product.mrp*quantityInt)+getString(R.string.priceEndSymbol));
        mrPrice.setPaintFlags(mrPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        discount.setText(discount(product.mrp, product.listPrice)+getString(R.string.percentageSymbol));
        cart_itemImage.setImageResource(R.drawable.ic_book_default);
        quantity.setText(""+quantityInt);
        try {
            Glide.with(this).load(product.book.photofile).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.ALL).into(cart_itemImage);
        }catch (NullPointerException e){
            cart_itemImage.setImageResource(R.drawable.ic_book_error);
        }

    }

    private void setupCard(View cartItemView){
        itemTitle = (TextView) cartItemView.findViewById(R.id.itemTitle);
        itemSubTitle = (TextView) cartItemView.findViewById(R.id.itemSubTitle);
        price = (TextView) cartItemView.findViewById(R.id.price);
        mrPrice = (TextView) cartItemView.findViewById(R.id.mrPrice);
        quantity = (TextView) cartItemView.findViewById(R.id.quantity);
        plus = (LinearLayout) cartItemView.findViewById(R.id.plus);
        minus = (LinearLayout) cartItemView.findViewById(R.id.minus);
        moveToWishList = (TextView) cartItemView.findViewById(R.id.moveToWishList);
        remove = (TextView) cartItemView.findViewById(R.id.remove);
        discount = (TextView) cartItemView.findViewById(R.id.discount);
        cart_itemImage = (ImageView) cartItemView.findViewById(R.id.cart_itemImage);

    }

    private void fetchProductsArray(int size, RealmResults<cartObject> listofCartItems) {
        for (int i = 0; i < size; i++){
            String productObjectId = listofCartItems.get(i).getProductId();
            fetchProduct(productObjectId,listofCartItems, i);
        }

    }

    private void fetchProduct(String product_objectId, final RealmResults<cartObject> listofCartItems, final int position) {
        showProgressDialog(getString(R.string.loadingCart));
//        http://api.backendless.com/test/data/products/E0686264-7306-2371-FFBC-5826F3B11F00?
        String productRequest = getString(R.string.baseBackendUrl);
//        productRequest = productRequest+getString(R.string.products);
        productRequest = productRequest+___CLASS+SLASH+product_objectId+QUERY+LOAD_RELATIONS;


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
                        addItemsViews(listofCartItems,product,position);
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

    private void setupLayout() {
        cartListLayout = (LinearLayout) findViewById(R.id.cartListLayout);
    }

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
    protected void onDestroy() {
        super.onDestroy();
        if(mProgressDialog != null){
            mProgressDialog.dismiss();
        }

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
        Snackbar snackbar = Snackbar.make(activity_cart_listing, message, Snackbar.LENGTH_LONG);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.shopNow:
                finish();
                break;
            case R.id.continueChkOut:
                //continue to checkout
                if (NETWORK_STATE){

                }else {
                    showSnack(getString(R.string.NetworkFaliure));
                }

                break;
        }
    }
}
