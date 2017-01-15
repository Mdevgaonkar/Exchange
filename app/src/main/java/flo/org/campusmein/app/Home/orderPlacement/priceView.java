package flo.org.campusmein.app.Home.orderPlacement;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import flo.org.campusmein.R;
import flo.org.campusmein.app.Home.MainHomeActivity;
import flo.org.campusmein.app.Login.spinnerAdapter;
import flo.org.campusmein.app.utils.ConnectivityReceiver;
import flo.org.campusmein.app.utils.Person;
import flo.org.campusmein.app.utils.PersonGSON;
import flo.org.campusmein.app.utils.Products;
import flo.org.campusmein.app.utils.RealmUtils.RealmController;
import flo.org.campusmein.app.utils.campusExchangeApp;
import flo.org.campusmein.app.utils.cartObject;
import flo.org.campusmein.app.utils.orders;
import io.realm.RealmResults;

public class priceView extends AppCompatActivity implements View.OnClickListener,ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = priceView.class.getSimpleName();
    private static final String PRODUCT_OBJECT_ID = "objectId";

    private static final String ___CLASS = "products";
    private static final String LOAD_RELATIONS ="loadRelations=book%2Csubject%2Ccollege%2Cspecialization%2Ccombopack%2Ccombopack.books%2Ccombopack.instruments%2Cinstrument";
    private static final String SLASH = "/";
    private static final String QUERY = "?";
    private static final String PHONE_REGEX = "((\\+*)((0[ -]+)*|(91 )*)(\\d{12}+|\\d{10}+))|\\d{5}([- ]*)\\d{6}";

    private ProgressDialog mProgressDialog;
    private boolean NETWORK_STATE = false;
    private CoordinatorLayout activity_priceView_listing;
    private CardView errorLayout;
    private CardView receiptView;
    private TextView priceView_totalItems,priceView_totalPrice,priceView_totalPricePayable,priceView_college;
    private LinearLayout allProductsView;
    private Button placeOrder;
    private int totalPriceFloat=0;
    private int totalItemsInt=0;
    private CardView[] priceView_object_views;
    private TextView priceView_itemTitle,priceView_itemSubTitle,priceView_price,priceView_mrPrice,priceView_discount,priceView_itemQuantity;
    private ImageView priceView_ItemImage;
    private ArrayList<String> productObjectIds = new ArrayList<>();
    private ArrayList<Integer> prices = new ArrayList<>();
    private ArrayList<Integer> productQuantities = new ArrayList<>();
    private ArrayList<String> productTitles = new ArrayList<>();
    private ArrayList<Integer> errorOrders = new ArrayList<>();
    private ArrayList<Integer> SuccessOrders = new ArrayList<>();
    private Button continueShopping;

    private NestedScrollView orderCompletionLayout;
    private LinearLayout orderSuccess;
    private TextView orderSuccessText, orderFaliureText;
    private String orderSuccessTextString = "";
    private boolean shown_completionLayout=false, shown_orderSuccessLayout=false, shown_orderFailureLayout=false;

    private String source = "cart";
    private String product_ObjectId = "";
    private static final String SRC_STR_KEY = "source";
    private static final String PDCT_ID_KEY = "productId";
    private Bundle sourceBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_view);
        checkConnection();
        setupCoordinatorLayout();
        setupErrorLayout();
        setupReceiptView();
        setupListLayout();
        setCollegeName();
        setupOrderCompleteLayout();
        setupSource();

    }

    private void setupSource() {
        Intent sourceIntent = getIntent();
        sourceBundle = sourceIntent.getExtras();
        if(sourceBundle != null){
            source = sourceBundle.getString(SRC_STR_KEY);
            product_ObjectId = sourceBundle.getString(PDCT_ID_KEY);
        }

    }

    private void setupArrays() {
        productObjectIds.clear();
        productTitles.clear();
        errorOrders.clear();
        prices.clear();
        productQuantities.clear();
        SuccessOrders.clear();
    }

    private void setCollegeName() {
        String CollegeName = campusExchangeApp.getInstance().getUniversalPerson().getCollegeName();
        String CollegeLoc = campusExchangeApp.getInstance().getUniversalPerson().getPersonCollegeLocation();
        priceView_college.setText(getString(R.string.deliveryAt)+CollegeName+"("+CollegeLoc+")");
    }

    private void loadList() {
        if(NETWORK_STATE){
            totalPriceFloat = 0;
            totalItemsInt = 0;
            if(allProductsView.getChildCount()>0){
                allProductsView.removeAllViews();
            }
            if(source.equals("cart")){
                RealmResults<cartObject> listOfCartItems = RealmController.getInstance().getItems();
                if(listOfCartItems.size() > 0){
                    showAllProductsView();
                    int cartSize = listOfCartItems.size();
                    priceView_object_views = new CardView[cartSize];
                    fetchProductsArray(cartSize,listOfCartItems);
                }else {
                    showErrorLayout();
                }
            }else if(source.equals("product")){
                if(!product_ObjectId.equals("")){
                    showAllProductsView();
                    int buySize = 1;
                    priceView_object_views = new CardView[buySize];
                    fetchSingleProduct(buySize,product_ObjectId);
                }else {
                    showErrorLayout();
                }
            }
        }else {
            showErrorLayout();
            showSnack(getString(R.string.NetworkFaliure));
        }

    }

    private void fetchSingleProduct(int size, String productObjectId) {
        fetchProduct(productObjectId,1, 0);
    }

    private void fetchProductsArray(int size, RealmResults<cartObject> listofCartItems) {
        for (int i = 0; i < size; i++){
            String productObjectId = listofCartItems.get(i).getProductId();
            int productQuantity = listofCartItems.get(i).getQuantity();
            fetchProduct(productObjectId,productQuantity, i);
        }

    }

    private void fetchProduct(String product_objectId, final int quantity, final int position) {
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
                        addItemsViews(quantity,product,position);
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

    private void addItemsViews(final int quantity, Products product, final int position) {
        View ListItemView = getLayoutInflater().inflate(R.layout.price_view_object,priceView_object_views[position],false);
        priceView_object_views[position]= (CardView) ListItemView.findViewById(R.id.cart_object_card);
        setupCard(ListItemView);
//        int quantity = listofCarts.get(position).getQuantity();

//        final String productId = listofCarts.get(position).getProductId();
        showProductProperties(product,quantity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        int pixelsLR = dpTopixels(6);
        int pixelsTB = dpTopixels(6);
        params.setMargins(pixelsLR,pixelsTB,pixelsLR,pixelsTB);

        allProductsView.addView(ListItemView,params);

    }

    private void showProductProperties(Products product, int quantity) {
        if(product.removed){
            RealmController.getInstance().removeCartItem(product.objectId);
//            showRemovedView(product);
        }else {
            showItem(product,quantity);
        }
    }

    private void showItem(Products product, int quantity) {
        String type = product.type;
        if(type.equals(getString(R.string.bookType))){
            setBookProperties(product,quantity);
            productTitles.add(product.book.title);
        }else if(type.equals(getString(R.string.instrumentType))){
            setInstrumentProperties(product,quantity);
            productTitles.add(product.instrument.instrumentName);
        }else if(type.equals(getString(R.string.comboType))){
            setCombopackProperties(product,quantity);
            productTitles.add(product.combopack.title);
        }else {
            removedlayout("Item Not Found", "Maybe it was removed");
        }
        addToTotal(product.listPrice*quantity);
        productObjectIds.add(product.objectId);
        productQuantities.add(quantity);
        addToTotalItems();
    }

    private void addToTotalItems() {
        totalItemsInt = totalItemsInt +1;
        priceView_totalItems.setText(getString(R.string.totalItemsText)+totalItemsInt);
    }

    public void addToTotal(int priceToBeAdded){
        totalPriceFloat = totalPriceFloat + priceToBeAdded;
        priceView_totalPrice.setText(getString(R.string.rupeesSymbol)+totalPriceFloat+getString(R.string.priceEndSymbol));
        priceView_totalPricePayable.setText(getString(R.string.rupeesSymbol)+totalPriceFloat+getString(R.string.priceEndSymbol));
        prices.add(priceToBeAdded);
    }

    private void setCombopackProperties(Products product, int quantityInt) {
        priceView_itemTitle.setText(product.combopack.title);
        priceView_itemSubTitle.setText(product.combopack.subTitle);
        priceView_price.setText(getString(R.string.rupeesSymbol)+(product.listPrice*quantityInt)+getString(R.string.priceEndSymbol));
        priceView_mrPrice.setText(getString(R.string.rupeesSymbol)+(product.mrp*quantityInt)+getString(R.string.priceEndSymbol));
        priceView_mrPrice.setPaintFlags(priceView_mrPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        priceView_discount.setText(discount(product.mrp, product.listPrice)+getString(R.string.percentageSymbol));
        priceView_ItemImage.setImageResource(R.drawable.ic_combo_default);
        priceView_itemQuantity.setText(getString(R.string.quantity)+quantityInt);
        try {
            Glide.with(this).load(product.combopack.photoUrl).placeholder(R.drawable.ic_combo_default).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.ALL).into(priceView_ItemImage);
        }catch (NullPointerException e){
            priceView_ItemImage.setImageResource(R.drawable.ic_combo_error);
        }

    }

    private void setInstrumentProperties(Products product, int quantityInt) {
        priceView_itemTitle.setText(product.instrument.instrumentName);
        priceView_itemSubTitle.setText(product.instrument.instrumentSubtitle);
        priceView_price.setText(getString(R.string.rupeesSymbol)+(product.listPrice*quantityInt)+getString(R.string.priceEndSymbol));
        priceView_mrPrice.setText(getString(R.string.rupeesSymbol)+(product.mrp*quantityInt)+getString(R.string.priceEndSymbol));
        priceView_mrPrice.setPaintFlags(priceView_mrPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        priceView_discount.setText(discount(product.mrp, product.listPrice)+getString(R.string.percentageSymbol));
        priceView_ItemImage.setImageResource(R.drawable.ic_instrument_default);
        priceView_itemQuantity.setText(getString(R.string.quantity)+quantityInt);
        try {
            Glide.with(this).load(product.instrument.photoFile).placeholder(R.drawable.ic_instrument_default).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.ALL).into(priceView_ItemImage);
        }catch (NullPointerException e){
            priceView_ItemImage.setImageResource(R.drawable.ic_instrument_error);
        }

    }

    private void setBookProperties(Products product, int quantityInt) {
        priceView_itemTitle.setText(product.book.title);
        priceView_itemSubTitle.setText(product.book.author);
        priceView_price.setText(getString(R.string.rupeesSymbol)+(product.listPrice*quantityInt)+getString(R.string.priceEndSymbol));
        priceView_mrPrice.setText(getString(R.string.rupeesSymbol)+(product.mrp*quantityInt)+getString(R.string.priceEndSymbol));
        priceView_mrPrice.setPaintFlags(priceView_mrPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        priceView_discount.setText(discount(product.mrp, product.listPrice)+getString(R.string.percentageSymbol));
        priceView_ItemImage.setImageResource(R.drawable.ic_book_default);
        priceView_itemQuantity.setText(getString(R.string.quantity)+quantityInt);
        try {
            Glide.with(this).load(product.book.photofile).placeholder(R.drawable.ic_book_default).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.ALL).into(priceView_ItemImage);
        }catch (NullPointerException e){
            priceView_ItemImage.setImageResource(R.drawable.ic_book_error);
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

//    private void showRemovedView(Products product) {
//        String type = product.type;
//        if(type.equals(getString(R.string.bookType))){
//            removedlayout(product.book.title, product.reasonRemoved);
//        }else if(type.equals(getString(R.string.instrumentType))){
//            removedlayout(product.book.title, product.reasonRemoved);
//        }else if(type.equals(getString(R.string.comboType))){
//            removedlayout(product.book.title, product.reasonRemoved);
//        }else {
//            removedlayout("Item Not Found", "Maybe it was removed");
//        }
//    }

    private void removedlayout(String Title,String reasonRemoved){
        priceView_itemTitle.setText(Title);
        priceView_itemSubTitle.setText(R.string.removedText);
        priceView_price.setText(reasonRemoved);
        priceView_mrPrice.setVisibility(View.GONE);
        priceView_itemQuantity.setVisibility(View.GONE);
        priceView_discount.setVisibility(View.GONE);
    }

    private void setupCard(View ListItemView){
        priceView_itemTitle = (TextView) ListItemView.findViewById(R.id.priceView_itemTitle);
        priceView_itemSubTitle = (TextView) ListItemView.findViewById(R.id.priceView_itemSubTitle);
        priceView_price = (TextView) ListItemView.findViewById(R.id.priceView_price);
        priceView_mrPrice = (TextView) ListItemView.findViewById(R.id.priceView_mrPrice);
        priceView_itemQuantity = (TextView) ListItemView.findViewById(R.id.priceView_itemQuantity);
        priceView_discount = (TextView) ListItemView.findViewById(R.id.priceView_discount);
        priceView_ItemImage= (ImageView) ListItemView.findViewById(R.id.priceView_ItemImage);
    }

    public int dpTopixels(int dps){
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dps,
                getResources().getDisplayMetrics());
    }

    private void setupListLayout() {
        allProductsView = (LinearLayout) findViewById(R.id.allProductsView);
        hideAllProductsView();
    }

    private void hideAllProductsView() {
        allProductsView.setVisibility(View.GONE);
        hideReceiptView();
    }

    private void showAllProductsView() {
        allProductsView.setVisibility(View.VISIBLE);
        showReceiptView();
    }

    private void setupReceiptView() {
        receiptView = (CardView) findViewById(R.id.receiptView);

        priceView_totalItems = (TextView) findViewById(R.id.priceView_totalItems);
        priceView_totalPrice = (TextView) findViewById(R.id.priceView_totalPrice);
        priceView_totalPricePayable = (TextView) findViewById(R.id.priceView_totalPricePayable);
        priceView_college= (TextView) findViewById(R.id.priceView_college);
        placeOrder = (Button) findViewById(R.id.placeOrder);
        placeOrder.setOnClickListener(this);
        hideReceiptView();
    }

    private void hideReceiptView() {
        receiptView.setVisibility(View.GONE);
        placeOrder.setVisibility(View.GONE);
    }

    private void showReceiptView() {
        receiptView.setVisibility(View.VISIBLE);
        placeOrder.setVisibility(View.VISIBLE);
    }

    private void setupErrorLayout() {
        errorLayout = (CardView) findViewById(R.id.error_Layout);
        hideErrorLayout();
    }

    private void hideErrorLayout() {
        errorLayout.setVisibility(View.GONE);
    }

    private void showErrorLayout() {
        errorLayout.setVisibility(View.VISIBLE);
        hideAllProductsView();
    }

    private void setupCoordinatorLayout() {
        activity_priceView_listing = (CoordinatorLayout) findViewById(R.id.activity_priceView_listing);
    }

    @Override
    protected void onResume() {
        campusExchangeApp.getInstance().setConnectivityListener(this);
        super.onResume();
        if(!shown_completionLayout) {
            setupArrays();
            loadList();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.placeOrder:
                //method call to post order data
                showMobilenumberConfirmationDialog();
                break;
            case R.id.continueShopping:
                onStop();
                break;
        }

    }

    private void showMobilenumberConfirmationDialog(){

        AlertDialog.Builder confirmMobileNumber;
        final EditText mobileNumber_editText = new EditText(this);

        confirmMobileNumber = new AlertDialog.Builder(this);
        confirmMobileNumber.setTitle(getString(R.string.EnterYourMobileNumber));
        confirmMobileNumber.setMessage(R.string.confirm_mobile_number_message_text);

        LinearLayout mobileNumDialog_layout = new LinearLayout(this);
        mobileNumDialog_layout.setOrientation(LinearLayout.VERTICAL);
        mobileNumDialog_layout.setPadding(dpTopixels(12),dpTopixels(12),dpTopixels(12),dpTopixels(12));

        mobileNumber_editText.setHint(R.string.confirmation_mobile_number_edit_text);
        mobileNumber_editText.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
        mobileNumber_editText.setPadding(dpTopixels(8),dpTopixels(8),dpTopixels(8),dpTopixels(8));
        mobileNumber_editText.setInputType(InputType.TYPE_CLASS_PHONE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        int pixelsLR = dpTopixels(6);
        int pixelsTB = dpTopixels(6);
        params.setMargins(pixelsLR,pixelsTB,pixelsLR,pixelsTB);
        mobileNumber_editText.setLayoutParams(params);
        if(campusExchangeApp.getInstance().getUniversalPerson().getPhoneNumber().equals("")){
            mobileNumber_editText.setText(R.string.mobile_number_country_code);
        }else {
            mobileNumber_editText.setText(""+campusExchangeApp.getInstance().getUniversalPerson().getPhoneNumber());
        }


        mobileNumDialog_layout.addView(mobileNumber_editText);
        confirmMobileNumber.setView(mobileNumDialog_layout);

        String positiveText = getString(android.R.string.ok);
        confirmMobileNumber.setPositiveButton(positiveText,null);

        String negativeText = getString(android.R.string.cancel);
        confirmMobileNumber.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // negative button logic
                        dialog.dismiss();
                    }
                });

        final AlertDialog dialog = confirmMobileNumber.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // positive button logic
                        if(mobileNumber_editText.getText().toString().isEmpty()){
                            Toast.makeText(getApplicationContext(), R.string.confirm_mob_number_invalid_mob_number, Toast.LENGTH_LONG).show();
                        }else {
                            if(isPersonNumberValid(mobileNumber_editText.getText().toString())){
                                if (!mobileNumber_editText.getText().equals(campusExchangeApp.getInstance().getUniversalPerson())) {
                                    String oldPhoneNumber = campusExchangeApp.getInstance().getUniversalPerson().getPhoneNumber();
                                    String newPhoneNumber = mobileNumber_editText.getText().toString();
                                    if(oldPhoneNumber.equals(newPhoneNumber)){
                                        updateMobileNumber(mobileNumber_editText.getText().toString());
                                    }
                                    detailsUpload();
                                    placeBulkOrder();
                                    dialog.dismiss();
                                }
                            }else {
                                Toast.makeText(getApplicationContext(), R.string.confirm_mob_number_invalid_mob_number, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
        });
        // display dialog
        dialog.show();
    }

    private static boolean isPersonNumberValid(String number) {
        return number != null && Pattern.matches(PHONE_REGEX, number);
    }

    private void updateMobileNumber(String number) {

        campusExchangeApp.getInstance().getUniversalPerson().setPhoneNumber(number);
    }

    private void placeBulkOrder() {
        int size = productObjectIds.size();
        for (int i = 0; i < size; i++){
            String productObjectId = productObjectIds.get(i);
            int quantity = productQuantities.get(i);
            int cost = prices.get(i);
            int position = i;
            placeOrder(productObjectId,cost,quantity,position);
        }

    }

    private void setupOrderCompleteLayout(){
        orderCompletionLayout = (NestedScrollView) findViewById(R.id.orderCompletion);
        hideOrderCompleteLayout();
        orderSuccess = (LinearLayout) findViewById(R.id.orderSuccess);
        hideOrderSuccess();
        orderSuccessText = (TextView) findViewById(R.id.orderSuccessText);
        orderFaliureText = (TextView) findViewById(R.id.orderFailure);
        hideOrderSFaliure();
        continueShopping = (Button) findViewById(R.id.continueShopping);
        continueShopping.setOnClickListener(this);
    }

    private void hideOrderSuccess() {
        orderSuccess.setVisibility(View.GONE);
    }

    private void showOrderSuccess() {
        orderSuccess.setVisibility(View.VISIBLE);
    }

    private void hideOrderSFaliure() {
        orderFaliureText.setVisibility(View.GONE);
    }

    private void showOrderFaliure() {
        orderFaliureText.setVisibility(View.VISIBLE);
    }

    private void hideOrderCompleteLayout() {
        orderCompletionLayout.setVisibility(View.GONE);
    }

    private void showOrderCompleteLayout() {
        orderCompletionLayout.setVisibility(View.VISIBLE);
        hideAllProductsView();
        hideReceiptView();
        hideErrorLayout();
    }

    public int generateOrderCode() {

        //generate a 4 digit integer 1000 <10000
        int randomPIN = (int)(Math.random()*9000)+1000;

        //Store integer in a string
        return randomPIN;

    }

    public String getOrderedTimeStamp() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return String.valueOf(timestamp.getTime());
    }

    public String getDeliveryExpectedTimeStamp() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);
        cal.add(Calendar.DAY_OF_WEEK, 5);
        timestamp.setTime(cal.getTime().getTime());
        return String.valueOf(timestamp.getTime());
    }

    private void placeOrder(String productId, int cost, int quantity, final int position) {
        showProgressDialog(getResources().getString(R.string.placingOrders));

        orders order = new orders();
        orders.postOrder postOrder = order.getPostOrder();

        postOrder.product.setObjectId(productId);
        postOrder.buyerId.setObjectId(campusExchangeApp.getInstance().getUniversalPerson().getPersonObjectId());
        postOrder.setOrderCost(cost);
        postOrder.setQuantity(quantity);
        postOrder.setOrderCode(generateOrderCode());
        postOrder.setOrderedDate(getOrderedTimeStamp());
        postOrder.setStatus(-1);
        postOrder.setDeliveryExpected(getDeliveryExpectedTimeStamp());
        postOrder.setPaymentMode(getString(R.string.paymentModeCOD));

        Gson gtoj = campusExchangeApp.getInstance().getGson();
        String jsonObj = gtoj.toJson(postOrder);
        Log.d("Order",jsonObj);
        try {
            JSONObject jsonOrder = new JSONObject(jsonObj);

            String createOrderString = getResources().getString(R.string.classOrders);

            JsonObjectRequest createNewOrder = new JsonObjectRequest(
                    Request.Method.POST,
                    createOrderString,
                    jsonOrder,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            hideProgressDialog();
                            try {
                            if (response.isNull("objectId")) {
                                Toast.makeText(getApplicationContext(),getString(R.string.order_not_placed)+productTitles.get(position),Toast.LENGTH_LONG).show();
                                errorOrders.add(position);
                                showOrderFaliure();
                            }else {
                                showOrderSuccess();

                                    orderSuccessText.append("\n"+productTitles.get(position)+"\t\t\t"+getString(R.string.rupeesSymbol)+response.getString("orderCost")+getString(R.string.priceEndSymbol));

                                SuccessOrders.add(position);
                                RealmController.getInstance().removeCartItem(productObjectIds.get(position));
                            }
                            controlCompletionLayout();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(),getString(R.string.order_not_placed)+productTitles.get(position),Toast.LENGTH_LONG).show();
                            errorOrders.add(position);
                            showOrderFaliure();
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
//
            campusExchangeApp.getInstance().addToRequestQueue(createNewOrder,TAG);

        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(),getString(R.string.order_not_placed)+productTitles.get(position),Toast.LENGTH_LONG).show();
            errorOrders.add(position);
            showOrderFaliure();
        }
    }

    private void controlCompletionLayout(){
        if(shown_completionLayout){
            if(errorOrders.size()>0){
                if (!shown_orderFailureLayout) {
                    showOrderFaliure();
                    shown_orderFailureLayout = true;
                }
            }else {
                if(!shown_orderSuccessLayout){
                    showOrderSuccess();
                    shown_orderSuccessLayout = true;
                }
            }
        }else {
            showOrderCompleteLayout();
            shown_completionLayout=true;
        }


    }

    private void showProgressDialog(String progressString) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(progressString);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    finish();
                }
            });
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

    @Override
    protected void onStop() {
        super.onStop();
        if(source.equals("product")){
            finish();
        }else {
            finish();
            Intent mainHome = new Intent(this, MainHomeActivity.class);
            mainHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainHome);
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
        } else {
            showSnack(getString(R.string.NetworkFaliure));
        }
    }

    private void showSnack(String snackString) {
        String message;
        message = snackString;
        Snackbar snackbar = Snackbar.make(activity_priceView_listing, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void detailsUpload(){

        showProgressDialog(getString(R.string.uploading));

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
            if(newPerson.getPhoneNumber().equals("")){
                jsonPerson.remove(gsonPerson.getKey_contactNumber());
            }
            jsonPerson.remove(gsonPerson.getKey_email());
            jsonPerson.remove("password");

            String createUserString = getString(R.string.classUsers);
            createUserString = createUserString+"/"+newPerson.getPersonObjectId();

            JsonObjectRequest updateNewUser = new JsonObjectRequest(
                    Request.Method.PUT,
                    createUserString,
                    jsonPerson,
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
                                Snackbar.make(activity_priceView_listing, R.string.NetworkError, Snackbar.LENGTH_LONG).show();
                                rollbackIfError();
                            }else {
                                Snackbar.make(activity_priceView_listing, R.string.NumberVerified, Snackbar.LENGTH_LONG).show();
//                                finish();
//                            StartMainHomeActivity();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            rollbackIfError();
                            Snackbar.make(activity_priceView_listing, R.string.NetworkError, Snackbar.LENGTH_LONG).show();
                            VolleyLog.d(TAG+" :while updating user", "Error: " + error.getMessage());
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

            campusExchangeApp.getInstance().addToRequestQueue(updateNewUser,TAG);

        } catch (JSONException e) {
            e.printStackTrace();
            rollbackIfError();
            Snackbar.make(activity_priceView_listing, R.string.NetworkError, Snackbar.LENGTH_LONG).show();
        }
    }

    private void rollbackIfError() {

    }


}
