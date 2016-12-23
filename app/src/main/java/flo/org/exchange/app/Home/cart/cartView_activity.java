package flo.org.exchange.app.Home.cart;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import flo.org.exchange.R;
import flo.org.exchange.app.Home.ProductView.productView;
import flo.org.exchange.app.utils.Books;
import flo.org.exchange.app.utils.Combopacks;
import flo.org.exchange.app.utils.Instruments;
import flo.org.exchange.app.utils.Products;
import flo.org.exchange.app.utils.RealmUtils.RealmController;
import flo.org.exchange.app.utils.campusExchangeApp;
import flo.org.exchange.app.utils.cartObject;
import io.realm.RealmResults;

public class cartView_activity extends AppCompatActivity {

    private static final String PRODUCT_OBJECT_ID = "objectId";

    private static final String ___CLASS = "products";
    private static final String LOAD_RELATIONS ="loadRelations=book%2Csubject%2Ccollege%2Cspecialization%2Ccombopack%2Ccombopack.books%2Ccombopack.instruments%2Cinstrument";
    private static final String SLASH = "/";
    private static final String QUERY = "?";
    private static final String TAG = productView.class.getSimpleName();
    private boolean NETWORK_STATE = false;
    private int default_quantity = 1;


    private LinearLayout cartListLayout;
    private CardView[] cart_object_views;
    private LinearLayout plus,minus;
    private TextView itemTitle,itemSubTitle,price,mrPrice,discount,quantity,moveToWishList,remove;
    private ImageView cart_itemImage;

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_view);

        RealmResults<cartObject> listofCarts = RealmController.getInstance().getItems();

        setupLayout();

        if(listofCarts.size() > 0){
            showProgressDialog(getString(R.string.loadingCart));
            addItemsViews(listofCarts.size(),listofCarts);
            hideProgressDialog();
        }


    }

    private void addItemsViews(int size, RealmResults<cartObject> listofCarts) {
        cart_object_views = new CardView[size];
        for (int i = 0; i < size; i++){
            View cartItemView = getLayoutInflater().inflate(R.layout.cart_object_view,cart_object_views[i],false);
            cart_object_views [i]= (CardView) cartItemView.findViewById(R.id.cart_object_card);
            setupCard(cartItemView);
            String productId = listofCarts.get(i).getProductId();
            int quantity = listofCarts.get(i).getQuantity();
            fetchProduct(productId);
            setQuantity(quantity, productId);
            setupQuantityUpdaters(quantity,productId);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

//            params.leftMargin = dpTopixels(8);
            params.setMargins(0,dpTopixels(12),0,0);

            cartListLayout.addView(cartItemView,params);
        }
    }

    private void setupQuantityUpdaters(final int quantityInt, final String productId) {
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setQuantity(quantityInt+1,productId);
            }
        });
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setQuantity(quantityInt-1,productId);
            }
        });
    }

    private void setQuantity(int i,String product_id) {
        if(i>0) {
            quantity.setText(getString(R.string.quantity)+i+"");
        }else {
            RealmController.getInstance().setDefaultCartItemQuantity(product_id);
            quantity.setText(getString(R.string.quantity)+1+"");
        }
    }

    private void fetchProduct(String product_objectId) {
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
                        Products product = new Products();
                        product = gson.fromJson(responseData,Products.class);

                        if(product.removed){
                            showRemovedView(product);
                        }else {
                            showItem(product);
                        }

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
        setOnCickListners(product);
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

    private void setCombopackProperties(Products product) {
        itemTitle.setText(product.combopack.title);
        itemSubTitle.setText(product.combopack.subTitle);
        price.setText(getString(R.string.rupeesSymbol)+product.listPrice+getString(R.string.priceEndSymbol));
        mrPrice.setText(product.mrp);
        mrPrice.setPaintFlags(mrPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        discount.setText(discount(product.mrp, product.listPrice)+getString(R.string.percentageSymbol));

    }

    private float discount(int mrp, int listPrice) {
        if(mrp != 0){
            int diff = mrp - listPrice;
            float discount = (diff / mrp)*100;
            return discount;
        }else return 0;
    }

    private void setInstrumentProperties(Products product) {
        itemTitle.setText(product.instrument.instrumentName);
        itemSubTitle.setText(product.instrument.instrumentSubtitle);
        price.setText(getString(R.string.rupeesSymbol)+product.listPrice+getString(R.string.priceEndSymbol));
        mrPrice.setText(product.mrp);
        mrPrice.setPaintFlags(mrPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        discount.setText(discount(product.mrp, product.listPrice)+getString(R.string.percentageSymbol));

    }

    private void setBookProperties(Products product) {
        itemTitle.setText(product.book.title);
        itemSubTitle.setText(product.book.author);
        price.setText(getString(R.string.rupeesSymbol)+product.listPrice+getString(R.string.priceEndSymbol));
//        mrPrice.setText(getString(R.string.rupeesSymbol)+product.mrp+getString(R.string.priceEndSymbol));
//        mrPrice.setPaintFlags(mrPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        discount.setText(discount(product.mrp, product.listPrice)+getString(R.string.percentageSymbol));

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
        itemTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProduct(product);
            }
        });
    }

    public int dpTopixels(int dps){
        int pixels = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dps,
                getResources().getDisplayMetrics());
        return pixels;
    }

    private void setupLayout() {
        cartListLayout = (LinearLayout) findViewById(R.id.cartListLayout);
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

    private void openProduct(Products product) {
        Intent productView = new Intent(this, flo.org.exchange.app.Home.ProductView.productView.class);
        productView.putExtra(PRODUCT_OBJECT_ID, product.type);
        productView.putExtra(PRODUCT_OBJECT_ID, product.objectId);
        startActivity(productView);
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

}
